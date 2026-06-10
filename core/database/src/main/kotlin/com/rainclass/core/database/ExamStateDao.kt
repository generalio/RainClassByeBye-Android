package com.rainclass.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamStateDao {
  @Query("SELECT * FROM exam_state ORDER BY updatedAt DESC")
  fun observeAll(): Flow<List<ExamStateEntity>>

  @Query("SELECT * FROM exam_state WHERE id = :id")
  suspend fun getById(id: String): ExamStateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(state: ExamStateEntity)

  @Delete
  suspend fun delete(state: ExamStateEntity)

  @Query("DELETE FROM exam_state WHERE id = :id")
  suspend fun deleteById(id: String)
}
