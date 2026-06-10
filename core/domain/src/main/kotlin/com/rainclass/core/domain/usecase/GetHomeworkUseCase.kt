package com.rainclass.core.domain.usecase

import com.rainclass.core.model.*
import com.rainclass.core.network.api.RainClassApi

class GetHomeworkUseCase(private val api: RainClassApi) {
    suspend fun getList(cid: Long): Result<List<ChapterNode>> = runCatching {
        api.getHomeworkInfo(cid, cid).data.courseChapter
    }

    suspend fun getDetails(cid: Long, leafId: Long): Result<HomeworkDetailData> = runCatching {
        api.getHomeworkDetails(cid, leafId, cid.toString()).data
    }

    suspend fun getCover(cid: Long, examId: Long): Result<HomeworkCoverData> = runCatching {
        if (examId <= 0) {
            throw Exception("无有效 Exam ID")
        }
        val response = api.getHomeworkCover(examId, cid)
        if (!response.success) {
            throw Exception(response.msg.ifEmpty { "考试信息获取失败: status=${response.status}" })
        }
        response.data
    }
}
