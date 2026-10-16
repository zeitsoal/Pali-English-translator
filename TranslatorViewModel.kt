package com.dhamma.palitranslator.ui.translator

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dhamma.palitranslator.data.local.AppDatabase
import com.dhamma.palitranslator.data.local.PaliTranslationEntity
import com.dhamma.palitranslator.data.model.PaliTranslationResponse
import com.dhamma.palitranslator.data.model.WordBreakdown
import com.dhamma.palitranslator.data.remote.GeminiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class TranslatorUiState(
    val paliInput: String = "Mano-pubbaṅgamā dhammā, mano-seṭṭhā mano-mayā;\nManasā ce paduṭṭhena, bhāsati vā karoti vā,\nTato naṁ dukkhamanveti, cakkaṁva vahato padaṁ.",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val translationResponse: PaliTranslationResponse? = null,
    val currentEntityId: Long? = null,
    val selectedTabIndex: Int = 0, // 0: PTS, 1: Argon AI, 2: Thai Forest, 3: Notes
    val userNotes: String = "",
    val isBookmarked: Boolean = false,
    val selectedWordBreakdown: WordBreakdown? = null,
    val apiKey: String = "",
    val selectedModelId: String = GeminiService.DEFAULT_MODEL_ARGON,
    val signedInGoogleAccount: String? = null,
    val synchronizedScrollIndex: Int = 0,
    val synchronizedScrollOffset: Int = 0
)

class TranslatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val paliDao = db.paliDao()
    private val prefs = application.getSharedPreferences("pali_app_prefs", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _uiState = MutableStateFlow(
        TranslatorUiState(
            apiKey = prefs.getString("gemini_api_key", "") ?: "",
            selectedModelId = prefs.getString("gemini_model_id", GeminiService.DEFAULT_MODEL_ARGON)
                ?: GeminiService.DEFAULT_MODEL_ARGON,
            signedInGoogleAccount = prefs.getString("google_account_email", null)
        )
    )
    val uiState: StateFlow<TranslatorUiState> = _uiState.asStateFlow()

    private val geminiService = GeminiService(
        apiKeyProvider = { _uiState.value.apiKey },
        modelIdProvider = { _uiState.value.selectedModelId }
    )

    fun onPaliInputChanged(newText: String) {
        _uiState.update { it.copy(paliInput = newText) }
    }

    fun appendDiacritic(char: String) {
        _uiState.update { it.copy(paliInput = it.paliInput + char) }
    }

    fun saveSettings(newKey: String, newModelId: String, googleEmail: String?) {
        prefs.edit()
            .putString("gemini_api_key", newKey)
            .putString("gemini_model_id", newModelId)
            .putString("google_account_email", googleEmail)
            .apply()
        _uiState.update {
            it.copy(
                apiKey = newKey,
                selectedModelId = newModelId,
                signedInGoogleAccount = googleEmail
            )
        }
    }

    fun setSelectedTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun updateSynchronizedScroll(index: Int, offset: Int) {
        _uiState.update {
            it.copy(
                synchronizedScrollIndex = index,
                synchronizedScrollOffset = offset
            )
        }
    }

    fun selectWord(word: WordBreakdown?) {
        _uiState.update { it.copy(selectedWordBreakdown = word) }
    }

    fun updateUserNotes(notes: String) {
        _uiState.update { it.copy(userNotes = notes) }
        val entityId = _uiState.value.currentEntityId ?: return
        viewModelScope.launch {
            val existing = paliDao.getById(entityId) ?: return@launch
            paliDao.updateTranslation(existing.copy(userNotes = notes))
        }
    }

    fun appendToNotes(textSnippet: String) {
        val updated = if (_uiState.value.userNotes.isBlank()) {
            textSnippet
        } else {
            "${_uiState.value.userNotes}\n\n$textSnippet"
        }
        updateUserNotes(updated)
    }

    fun toggleBookmark() {
        val newBookmark = !_uiState.value.isBookmarked
        _uiState.update { it.copy(isBookmarked = newBookmark) }
        val entityId = _uiState.value.currentEntityId ?: return
        viewModelScope.launch {
            val existing = paliDao.getById(entityId) ?: return@launch
            paliDao.updateTranslation(existing.copy(isBookmarked = newBookmark))
        }
    }

    fun translate() {
        val text = _uiState.value.paliInput.trim()
        if (text.isBlank()) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = geminiService.translatePali(text)
            result.onSuccess { response ->
                val fullJson = json.encodeToString(response)
                val entity = PaliTranslationEntity(
                    sourcePaliText = text,
                    summary = response.sourceTextSummary,
                    fullResponseJson = fullJson,
                    isBookmarked = false,
                    userNotes = ""
                )
                val newId = paliDao.insertTranslation(entity)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        translationResponse = response,
                        currentEntityId = newId,
                        isBookmarked = false,
                        userNotes = "",
                        synchronizedScrollIndex = 0,
                        synchronizedScrollOffset = 0
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Translation failed."
                    )
                }
            }
        }
    }

    fun loadFromEntity(entity: PaliTranslationEntity) {
        try {
            val parsed = json.decodeFromString<PaliTranslationResponse>(entity.fullResponseJson)
            _uiState.update {
                it.copy(
                    paliInput = entity.sourcePaliText,
                    translationResponse = parsed,
                    currentEntityId = entity.id,
                    isBookmarked = entity.isBookmarked,
                    userNotes = entity.userNotes,
                    selectedTabIndex = 0,
                    synchronizedScrollIndex = 0,
                    synchronizedScrollOffset = 0
                )
            }
        } catch (_: Exception) {}
    }
}
