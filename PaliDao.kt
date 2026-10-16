package com.dhamma.palitranslator.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PaliDao {
    @Query("SELECT * FROM pali_translations ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<PaliTranslationEntity>>

    @Query("SELECT * FROM pali_translations WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarks(): Flow<List<PaliTranslationEntity>>

    @Query("""
        SELECT * FROM pali_translations 
        WHERE sourcePaliText LIKE '%' || :query || '%' 
           OR fullResponseJson LIKE '%' || :query || '%' 
           OR userNotes LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchTranslations(query: String): Flow<List<PaliTranslationEntity>>

    @Query("SELECT * FROM pali_translations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PaliTranslationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(entity: PaliTranslationEntity): Long

    @Update
    suspend fun updateTranslation(entity: PaliTranslationEntity)

    @Delete
    suspend fun deleteTranslation(entity: PaliTranslationEntity)
}
