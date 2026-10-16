package com.dhamma.palitranslator.data.remote

import com.dhamma.palitranslator.data.model.PaliTranslationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService(
    private val apiKeyProvider: () -> String,
    private val modelIdProvider: () -> String = { DEFAULT_MODEL_ARGON }
) {

    companion object {
        const val DEFAULT_MODEL_ARGON = "gemini-4-argon"
        const val MODEL_PRO_1_5 = "gemini-1.5-pro"
        const val MODEL_FLASH_1_5 = "gemini-1.5-flash"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun translatePali(paliText: String): Result<PaliTranslationResponse> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API Key is missing. Tap the Settings icon in the top bar to enter your key.")
            )
        }

        val modelId = modelIdProvider().ifBlank { DEFAULT_MODEL_ARGON }
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"

        val systemInstruction = """
You are an advanced, polymathic Pali philologist and comparative Dhamma translator powered by Gemini 4 Argon.

When provided with a Pali text (primarily Romanized Pali with IAST diacritics), analyze each line and word across three distinct, mutually independent translation standards. You must provide an English translation and a detailed explanatory rationale for each standard:

1. STANDARD 1 — CLASSICAL COMMENTARIAL & PTS (ORTHODOX SCHOLARSHIP)
- Framework: Strict adherence to the classical Theravāda commentarial tradition (Aṭṭhakathā, Ṭīkā, Buddhaghosa, Dhammapāla) and standard Pali Text Society (PTS) lexicography (Rhys Davids, Stede, Norman, Cone).
- Style: Formal doctrinal precision, orthodox hermeneutics, preserving conventional doctrinal definitions and Abhidhamma classifications where the commentaries mandate them.
- Rationale Focus: Cite the relevant commentary's gloss, case inflection (vibhatti), and traditional doctrinal framework justifying the rendering.

2. STANDARD 2 — AUTONOMOUS FIRST-PRINCIPLES PHILOLOGY (GEMINI 4 ARGON REASONING)
- Framework: Pure linguistic, syntactical, and structural first-principles reasoning native to Gemini 4 Argon. Do not follow external orthodoxies, historical Western translations, or post-canonical commentarial accretions. Rely solely on your own deep morphological and contextual reasoning.
- Style: Natural, razor-sharp, contemporary English. Strips away archaic Victorian vocabulary and scholastic dogma. Resolves sandhi, compound structures (samāsa), verbal roots (dhātu), and syntax based strictly on internal coherence and immediate context.
- Rationale Focus: Explain the raw mechanical, etymological, and semantic logic—how sentence structure, verbal roots, and contextual flow dictate the meaning from first principles.

3. STANDARD 3 — ENLIGHTENED THAI FOREST MONK (DIRECT CONTEMPLATIVE REALIZATION)
- Framework: The perspective of an accomplished, enlightened master in the Kammatthana (Thai Forest) tradition. You evaluate the text solely through your own direct meditative expertise and immediate understanding of reality and the living mind (citta, khandha, kilesa, sabhāva-dhātu). You do not care about PTS lexicography or classical Theravāda commentarial standards at all.
- Style: Direct, visceral, experiential, and uncompromising. Focuses entirely on what happens in consciousness right now—how suffering is fabricated, how defilements deceive the heart, and how awareness disentangles itself from mental proliferation (saṅkhāra).
- Rationale Focus: Explain the translation based on direct contemplative realization, practical psychology of meditation, and the reality of the heart, pointing out where dry academic or scholastic interpretations miss the living truth.
""".trimIndent()

        val jsonSchema = JSONObject().apply {
            put("type", "OBJECT")
            val props = JSONObject()
            props.put("sourceTextSummary", JSONObject().put("type", "STRING"))

            val linesArray = JSONObject().apply {
                put("type", "ARRAY")
                val itemObj = JSONObject().apply {
                    put("type", "OBJECT")
                    val itemProps = JSONObject()
                    itemProps.put("lineNumber", JSONObject().put("type", "INTEGER"))
                    itemProps.put("paliLine", JSONObject().put("type", "STRING"))

                    val transObj = JSONObject().apply {
                        put("type", "OBJECT")
                        val transProps = JSONObject()
                        listOf("classicalPts", "autonomousAi", "thaiForestMaster").forEach { key ->
                            transProps.put(key, JSONObject().apply {
                                put("type", "OBJECT")
                                val subProps = JSONObject()
                                subProps.put("englishText", JSONObject().put("type", "STRING"))
                                subProps.put("phraseRationale", JSONObject().put("type", "STRING"))
                                put("properties", subProps)
                                put("required", JSONArray(listOf("englishText", "phraseRationale")))
                            })
                        }
                        put("properties", transProps)
                        put("required", JSONArray(listOf("classicalPts", "autonomousAi", "thaiForestMaster")))
                    }
                    itemProps.put("translations", transObj)

                    val wordsArray = JSONObject().apply {
                        put("type", "ARRAY")
                        val wordObj = JSONObject().apply {
                            put("type", "OBJECT")
                            val wProps = JSONObject()
                            wProps.put("paliWord", JSONObject().put("type", "STRING"))
                            wProps.put("rootAndGrammar", JSONObject().put("type", "STRING"))
                            wProps.put("ptsGloss", JSONObject().put("type", "STRING"))
                            wProps.put("aiGloss", JSONObject().put("type", "STRING"))
                            wProps.put("forestMonkInsight", JSONObject().put("type", "STRING"))
                            put("properties", wProps)
                            put("required", JSONArray(listOf("paliWord", "rootAndGrammar", "ptsGloss", "aiGloss", "forestMonkInsight")))
                        }
                        put("items", wordObj)
                    }
                    itemProps.put("words", wordsArray)

                    put("properties", itemProps)
                    put("required", JSONArray(listOf("lineNumber", "paliLine", "translations", "words")))
                }
                put("items", itemObj)
            }
            props.put("lines", linesArray)
            put("properties", props)
            put("required", JSONArray(listOf("lines")))
        }

        val requestPayload = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", "Translate and analyze this Pali text line-by-line across all three standards:\n\n$paliText")))
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("responseSchema", jsonSchema)
                put("temperature", 0.2)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(requestPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Gemini API ($modelId) Error ${response.code}: $body"))
                }
                val rawJson = JSONObject(body)
                val candidates = rawJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val text = firstCandidate
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""

                val parsed = jsonParser.decodeFromString<PaliTranslationResponse>(text)
                Result.success(parsed)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
