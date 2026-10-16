package com.dhamma.palitranslator.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pali_translations")
data class PaliTranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sourcePaliText: String,
    val summary: String,
    val fullResponseJson: String,
    val isBookmarked: Boolean = false,
    val userNotes: String = ""
)
