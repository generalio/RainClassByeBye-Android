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
        api.getHomeworkCover(examId, cid).data
    }
}
