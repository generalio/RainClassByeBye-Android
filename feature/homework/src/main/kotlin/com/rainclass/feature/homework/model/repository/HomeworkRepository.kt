package com.rainclass.feature.homework.model.repository

import com.rainclass.feature.homework.model.api.HomeworkApi
import com.rainclass.feature.homework.model.bean.ChapterNode
import com.rainclass.feature.homework.model.bean.HomeworkCoverData
import com.rainclass.feature.homework.model.bean.HomeworkDetailData

class HomeworkRepository(private val api: HomeworkApi) {
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
