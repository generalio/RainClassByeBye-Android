package com.rainclass.feature.exam.model.repository

import com.rainclass.core.database.AppDatabase
import com.rainclass.core.database.ExamStateEntity
import com.rainclass.core.config.model.AppSettings
import com.rainclass.feature.exam.model.api.ExamApi
import com.rainclass.feature.exam.model.api.ExamTokenApi
import com.rainclass.feature.exam.model.bean.AnsweredRecord
import com.rainclass.feature.exam.model.bean.ExamGenTokenRequest
import com.rainclass.feature.exam.model.bean.ExamStatus
import com.rainclass.feature.exam.model.bean.FailedRecord
import com.rainclass.feature.exam.model.bean.LogEntry
import com.rainclass.feature.exam.model.bean.LogLevel
import com.rainclass.feature.exam.model.bean.ProblemsEntity
import com.rainclass.feature.exam.model.bean.StartExamPaperRequest
import com.rainclass.feature.exam.model.bean.SubmitAnswerRequest
import com.rainclass.feature.exam.model.bean.SubmitAnswerResult
import com.rainclass.feature.exam.model.bean.SubmitPaperRequest
import com.rainclass.feature.exam.model.bean.SubmitPaperResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.*

data class ExamProgress(
  val status: ExamStatus = ExamStatus.PENDING,
  val totalProblems: Int = 0,
  val answeredCount: Int = 0,
  val failedCount: Int = 0,
  val examTitle: String = "",
  val logs: List<LogEntry> = emptyList(),
  val lastError: String = "",
  val isRunning: Boolean = false
)

class ExamRunner(
  private val examTokenApi: ExamTokenApi,
  private val examApi: ExamApi,
  private val solver: LLMSolver,
  private val database: AppDatabase,
  private val settings: AppSettings
) {
  private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
  private val _progress = MutableStateFlow(ExamProgress())
  val progress: StateFlow<ExamProgress> = _progress

  private val answered = mutableMapOf<Long, AnsweredRecord>()
  private val failed = mutableMapOf<Long, FailedRecord>()
  private var job: Job? = null

  suspend fun execute(cid: Long, examId: Long, isResume: Boolean = false) {
    val stateId = "${cid}_${examId}"

    try {
      // Load existing state if resuming
      if (isResume) {
        val existing = database.examStateDao().getById(stateId)
        if (existing != null) {
          loadState(existing)
        }
      }

      addLog(LogLevel.INFO, "当前模型: ${solver.modelName()}")
      addLog(LogLevel.INFO, "Worker 池: ${settings.workers}")

      // Enter exam
      addLog(LogLevel.INFO, "进入考试环境...")
      val tokenResp = examTokenApi.examGenToken(
        ExamGenTokenRequest(examId.toString(), cid.toString())
      )
      if (!tokenResp.success) {
        throw Exception("生成考试 Token 失败: status=${tokenResp.status} ${tokenResp.msg}")
      }
      if (tokenResp.data.token.isBlank() || tokenResp.data.userId <= 0) {
        throw Exception("生成考试 Token 失败: 返回凭证为空")
      }

      val nextUrl = "https://changjiang-exam.yuketang.cn/start/$examId?isFrom=2"
      val loginResp = examApi.examLogin(
        examId = examId,
        userId = tokenResp.data.userId,
        crypt = tokenResp.data.token,
        next = nextUrl
      )
      if (loginResp.code() != 302) {
        throw Exception("考试登录失败: HTTP ${loginResp.code()}")
      }

      val startResp = examApi.startExam(examId)
      if (!startResp.isSuccessful) {
        throw Exception("进入考试失败: HTTP ${startResp.code()}")
      }

      val startPaperResp = examApi.startExamPaper(StartExamPaperRequest(examId.toString()))
      if (startPaperResp.errcode != 0) {
        throw Exception("开始试卷失败: errcode=${startPaperResp.errcode}")
      }
      addLog(LogLevel.SUCCESS, "已进入考试环境")

      // Get questions
      val paper = examApi.getExamPaperQuestion(examId)
      if (paper.errcode != 0) {
        throw Exception("获取试卷失败: errcode=${paper.errcode}")
      }
      val problems = paper.data.problems
      val title = paper.data.title

      _progress.value = _progress.value.copy(
        totalProblems = problems.size,
        examTitle = title,
        isRunning = true,
        status = ExamStatus.RUNNING
      )

      // Filter pending
      val pending = problems.filter { !answered.containsKey(it.problemId) }

      if (pending.isEmpty()) {
        addLog(LogLevel.SUCCESS, "没有待处理题目")
        finishIfNeeded(examId, problems, stateId)
        return
      }

      addLog(LogLevel.INFO, "开始自动答题，剩余 ${pending.size} / ${problems.size}")

      // Concurrent solving
      val semaphore = Semaphore(settings.workers)
      coroutineScope {
        pending.map { problem ->
          async {
            semaphore.withPermit {
              solveProblem(problem, examId, stateId)
            }
          }
        }.awaitAll()
      }

      // Check remaining
      val remaining = problems.filter { !answered.containsKey(it.problemId) }
      if (remaining.isNotEmpty()) {
        _progress.value = _progress.value.copy(status = ExamStatus.PARTIAL)
        addLog(LogLevel.WARNING, "仍有 ${remaining.size} 道题未完成")
        saveState(stateId, cid, examId)
        return
      }

      finishIfNeeded(examId, problems, stateId)

    } catch (e: CancellationException) {
      _progress.value = _progress.value.copy(
        status = ExamStatus.INTERRUPTED,
        isRunning = false
      )
      addLog(LogLevel.WARNING, "任务被中断")
      saveState(stateId, cid, examId)
      throw e
    } catch (e: Exception) {
      _progress.value = _progress.value.copy(
        lastError = e.message ?: "未知错误",
        isRunning = false
      )
      addLog(LogLevel.ERROR, "执行失败: ${e.message}")
      saveState(stateId, cid, examId)
    }
  }

  private suspend fun solveProblem(problem: ProblemsEntity, examId: Long, stateId: String) {
    val startTime = System.currentTimeMillis()
    try {
      val (answer, raw, err) = solver.solve(problem)

      if (err != null && answer.result.isEmpty()) {
        markFailed(problem.problemId, err)
        addLog(LogLevel.ERROR, "problem=${problem.problemId} 求解失败: ${err.message}")
        saveState(stateId, 0, examId)
        return
      }

      // Normalize
      val normalized = AnswerParser.normalizeForSubmission(problem, answer)
      val finalAnswer = normalized ?: AnswerParser.randomChoiceFallback(problem)

      if (finalAnswer == null || finalAnswer.result.isEmpty()) {
        markFailed(problem.problemId, Exception("答案为空"))
        addLog(LogLevel.ERROR, "problem=${problem.problemId} 答案为空")
        saveState(stateId, 0, examId)
        return
      }

      // Submit
      val submitRequest = SubmitAnswerRequest(
        results = listOf(SubmitAnswerResult(
          problemId = problem.problemId,
          result = finalAnswer.result,
          time = System.currentTimeMillis()
        )),
        examId = examId
      )
      val resp = examApi.submitAnswer(submitRequest)

      if (resp.errcode != 0) {
        markFailed(problem.problemId, Exception("errcode=${resp.errcode} ${resp.errmsg}"))
        addLog(LogLevel.ERROR, "problem=${problem.problemId} 提交失败: ${resp.errmsg}")
        saveState(stateId, 0, examId)
        return
      }

      // Mark answered
      markAnswered(problem, finalAnswer.result, raw)
      val duration = System.currentTimeMillis() - startTime
      addLog(
        LogLevel.SUCCESS,
        "problem=${problem.problemId} 已提交 答案=${finalAnswer.result.joinToString(",")} " +
        "进度=${answered.size}/${_progress.value.totalProblems} 耗时=${duration}ms"
      )
      saveState(stateId, 0, examId)

    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      markFailed(problem.problemId, e)
      addLog(LogLevel.ERROR, "problem=${problem.problemId} 异常: ${e.message}")
      saveState(stateId, 0, examId)
    }
  }

  private suspend fun finishIfNeeded(examId: Long, problems: List<ProblemsEntity>, stateId: String) {
    if (!settings.submitPaper) {
      _progress.value = _progress.value.copy(status = ExamStatus.READY_TO_SUBMIT, isRunning = false)
      addLog(LogLevel.SUCCESS, "全部题目已提交完成")
      addLog(LogLevel.WARNING, "未自动交卷，可在设置中开启或手动交卷")
      saveState(stateId, 0, examId)
      return
    }

    submitPaper(examId, problems, stateId)
  }

  suspend fun submitPaper(examId: Long, problems: List<ProblemsEntity>, stateId: String) {
    addLog(LogLevel.INFO, "开始交卷...")

    val results = problems.map { problem ->
      val record = answered[problem.problemId]
        ?: throw Exception("problem ${problem.problemId} 尚未完成")
      SubmitPaperResult(
        problemId = problem.problemId,
        result = record.result,
        time = record.submittedAtUnixMs,
        showAnswer = "",
        isAnswered = true,
        isSave = true
      )
    }

    val request = SubmitPaperRequest(
      results = results,
      examId = examId.toString()
    )

    val resp = examApi.submitPaper(request)
    if (resp.errcode != 0) {
      addLog(LogLevel.ERROR, "交卷失败: ${resp.errmsg}")
      throw Exception("交卷失败: errcode=${resp.errcode} ${resp.errmsg}")
    }

    _progress.value = _progress.value.copy(status = ExamStatus.COMPLETED, isRunning = false)
    addLog(LogLevel.SUCCESS, "交卷完成!")
    saveState(stateId, 0, examId)
  }

  fun cancel() {
    job?.cancel()
  }

  private fun markAnswered(problem: ProblemsEntity, result: List<String>, rawOutput: String) {
    answered[problem.problemId] = AnsweredRecord(
      problemId = problem.problemId,
      problemIndex = problem.index,
      problemType = problem.typeText,
      result = result,
      model = solver.modelName(),
      modelRawOutput = rawOutput,
      submittedAtUnixMs = System.currentTimeMillis()
    )
    failed.remove(problem.problemId)
    _progress.value = _progress.value.copy(
      answeredCount = answered.size,
      failedCount = failed.size
    )
  }

  private fun markFailed(problemId: Long, error: Exception) {
    val existing = failed[problemId]
    failed[problemId] = FailedRecord(
      problemId = problemId,
      attempts = (existing?.attempts ?: 0) + 1,
      lastError = error.message ?: "",
      updatedAt = System.currentTimeMillis()
    )
    _progress.value = _progress.value.copy(
      failedCount = failed.size,
      lastError = error.message ?: ""
    )
  }

  private suspend fun saveState(stateId: String, cid: Long, examId: Long) {
    val entity = ExamStateEntity(
      id = stateId,
      cid = cid,
      examId = examId,
      examTitle = _progress.value.examTitle,
      status = _progress.value.status.name.lowercase(),
      totalProblems = _progress.value.totalProblems,
      answeredJson = json.encodeToString(answered.mapKeys { it.key.toString() }),
      failedJson = json.encodeToString(failed.mapKeys { it.key.toString() }),
      lastError = _progress.value.lastError,
      updatedAt = System.currentTimeMillis()
    )
    database.examStateDao().upsert(entity)
  }

  private fun loadState(entity: ExamStateEntity) {
    // Restore answered and failed maps from JSON
    try {
      val answeredMap: Map<String, AnsweredRecord> = json.decodeFromString(entity.answeredJson)
      answered.clear()
      answeredMap.forEach { (key, value) -> answered[key.toLong()] = value }
    } catch (_: Exception) {}

    try {
      val failedMap: Map<String, FailedRecord> = json.decodeFromString(entity.failedJson)
      failed.clear()
      failedMap.forEach { (key, value) -> failed[key.toLong()] = value }
    } catch (_: Exception) {}

    _progress.value = _progress.value.copy(
      examTitle = entity.examTitle,
      totalProblems = entity.totalProblems,
      answeredCount = answered.size,
      failedCount = failed.size,
      lastError = entity.lastError
    )
  }

  private fun addLog(level: LogLevel, message: String) {
    val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    val entry = LogEntry(time, level, message)
    _progress.value = _progress.value.copy(
      logs = _progress.value.logs + entry
    )
  }
}
