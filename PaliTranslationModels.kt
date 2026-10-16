package com.dhamma.palitranslator.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PaliTranslationResponse(
    val sourceTextSummary: String = "",
    val lines: List<TranslationLine> = emptyList()
)

@Serializable
data class TranslationLine(
    val lineNumber: Int,
    val paliLine: String,
    val translations: TranslationSet,
    val words: List<WordBreakdown> = emptyList()
)

@Serializable
data class TranslationSet(
    val classicalPts: SinglePerspective,
    val autonomousAi: SinglePerspective,
    val thaiForestMaster: SinglePerspective
)

@Serializable
data class SinglePerspective(
    val englishText: String,
    val phraseRationale: String
)

@Serializable
data class WordBreakdown(
    val paliWord: String,
    val rootAndGrammar: String,
    val ptsGloss: String,
    val aiGloss: String,
    val forestMonkInsight: String
)
