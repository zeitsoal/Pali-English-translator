package com.dhamma.palitranslator.ui.translator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhamma.palitranslator.data.model.TranslationLine
import com.dhamma.palitranslator.data.model.WordBreakdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    viewModel: TranslatorViewModel,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isTabletOrFoldable = configuration.screenWidthDp >= 600

    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Pali Dhamma Translator", fontWeight = FontWeight.Bold)
                        AssistChip(
                            onClick = onOpenSettings,
                            label = {
                                Text(
                                    text = if (state.selectedModelId == "gemini-4-argon") "Gemini 4 Argon" else state.selectedModelId,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )
                    }
                },
                actions = {
                    if (state.currentEntityId != null) {
                        IconButton(onClick = { viewModel.toggleBookmark() }) {
                            Icon(
                                if (state.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (state.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            if (state.signedInGoogleAccount != null) Icons.Default.AccountCircle else Icons.Default.Settings,
                            contentDescription = "Account & API Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isTabletOrFoldable) {
                // 50/50 Side-by-Side Split on Foldables & Tablets
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PaliInputSection(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        viewModel = viewModel,
                        state = state
                    )
                    TranslationOutputSection(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        viewModel = viewModel,
                        state = state,
                        clipboard = clipboard,
                        context = context
                    )
                }
            } else {
                // Top and Bottom Stacked Cards on Standard Phones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaliInputSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.44f),
                        viewModel = viewModel,
                        state = state
                    )
                    TranslationOutputSection(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.56f),
                        viewModel = viewModel,
                        state = state,
                        clipboard = clipboard,
                        context = context
                    )
                }
            }

            // Word breakdown modal bottom sheet
            state.selectedWordBreakdown?.let { word ->
                ModalBottomSheet(
                    onDismissRequest = { viewModel.selectWord(null) },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    WordBreakdownSheetContent(
                        word = word,
                        onCopy = {
                            clipboard.setPrimaryClip(ClipData.newPlainText("Pali Breakdown", it))
                            Toast.makeText(context, "Copied word analysis to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onAddToNotes = {
                            viewModel.appendToNotes(it)
                            Toast.makeText(context, "Added word breakdown to Notes", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PaliInputSection(
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel,
    state: TranslatorUiState
) {
    val diacritics = listOf("ā", "ī", "ū", "ṃ", "ṭ", "ḍ", "ṇ", "ñ", "ḷ", "ṅ")

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pali Source Text (IAST)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (state.apiKey.isBlank()) {
                    Text(
                        "Set API Key in Settings",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }
            }

            OutlinedTextField(
                value = state.paliInput,
                onValueChange = { viewModel.onPaliInputChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Paste Romanized Pali text here...") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif)
            )

            // IAST Diacritics Helper Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(diacritics) { char ->
                    SuggestionChip(
                        onClick = { viewModel.appendDiacritic(char) },
                        label = { Text(char, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    )
                }
            }

            Button(
                onClick = { viewModel.translate() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.paliInput.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Analyzing with Gemini 4 Argon...")
                } else {
                    Icon(Icons.Default.Translate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Translate Across 3 Standards (Argon)")
                }
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TranslationOutputSection(
    modifier: Modifier = Modifier,
    viewModel: TranslatorViewModel,
    state: TranslatorUiState,
    clipboard: ClipboardManager,
    context: Context
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val tabs = listOf("Classical (PTS)", "Argon AI", "Thai Forest", "Notes")
            TabRow(
                selectedTabIndex = state.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTabIndex == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                fontWeight = if (state.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (state.selectedTabIndex) {
                3 -> {
                    NotesEditorTab(
                        notes = state.userNotes,
                        onNotesChanged = { viewModel.updateUserNotes(it) },
                        onCopyNotes = {
                            clipboard.setPrimaryClip(ClipData.newPlainText("Dhamma Notes", state.userNotes))
                            Toast.makeText(context, "Notes copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                else -> {
                    val response = state.translationResponse
                    if (response == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Paste Pali text and tap Translate to generate synchronized line-by-line translations and rationales powered by Gemini 4 Argon.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        SynchronizedTranslationList(
                            lines = response.lines,
                            standardIndex = state.selectedTabIndex,
                            onWordSelected = { viewModel.selectWord(it) },
                            onAddToNotes = { snippet ->
                                viewModel.appendToNotes(snippet)
                                Toast.makeText(context, "Saved passage to Notes tab", Toast.LENGTH_SHORT).show()
                            },
                            onCopyLine = { lineText ->
                                clipboard.setPrimaryClip(ClipData.newPlainText("Translation", lineText))
                                Toast.makeText(context, "Copied line to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            synchronizedIndex = state.synchronizedScrollIndex,
                            synchronizedOffset = state.synchronizedScrollOffset,
                            onScrollChanged = { index, offset ->
                                viewModel.updateSynchronizedScroll(index, offset)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SynchronizedTranslationList(
    lines: List<TranslationLine>,
    standardIndex: Int,
    onWordSelected: (WordBreakdown) -> Unit,
    onAddToNotes: (String) -> Unit,
    onCopyLine: (String) -> Unit,
    synchronizedIndex: Int,
    synchronizedOffset: Int,
    onScrollChanged: (Int, Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = synchronizedIndex,
        initialFirstVisibleItemScrollOffset = synchronizedOffset
    )

    // Keep all 3 translation tabs locked to the exact same scroll item & pixel offset
    LaunchedEffect(standardIndex) {
        if (listState.firstVisibleItemIndex != synchronizedIndex ||
            listState.firstVisibleItemScrollOffset != synchronizedOffset
        ) {
            listState.scrollToItem(synchronizedIndex, synchronizedOffset)
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        onScrollChanged(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
    }

    val standardLabel = when (standardIndex) {
        0 -> "Standard 1: Classical PTS & Commentary"
        1 -> "Standard 2: Gemini 4 Argon Autonomous Philology"
        else -> "Standard 3: Enlightened Thai Forest Realization"
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(lines, key = { it.lineNumber }) { line ->
            val perspective = when (standardIndex) {
                0 -> line.translations.classicalPts
                1 -> line.translations.autonomousAi
                else -> line.translations.thaiForestMaster
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Line ${line.lineNumber} · $standardLabel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    onCopyLine(
                                        "${line.paliLine}\n${perspective.englishText}\n\nRationale ($standardLabel):\n${perspective.phraseRationale}"
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = {
                                    onAddToNotes(
                                        "[Line ${line.lineNumber} - $standardLabel]\nPali: ${line.paliLine}\nEnglish: ${perspective.englishText}\nRationale: ${perspective.phraseRationale}"
                                    )
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = "Add to Notes", modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Text(
                        line.paliLine,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        perspective.englishText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (line.words.isNotEmpty()) {
                        Text("Tap a Pali word for root & 3-standard breakdown:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(line.words) { word ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .clickable { onWordSelected(word) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        word.paliWord,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Why It Was Translated This Way:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                perspective.phraseRationale,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesEditorTab(
    notes: String,
    onNotesChanged: (String) -> Unit,
    onCopyNotes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Personal Study Notes & Favorite Passages",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onCopyNotes) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy all notes")
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChanged,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Tap the '+' note icon on any translation line or word to collect your favorite passages here, or type your own study reflections...") }
        )
    }
}

@Composable
fun WordBreakdownSheetContent(
    word: WordBreakdown,
    onCopy: (String) -> Unit,
    onAddToNotes: (String) -> Unit
) {
    val formattedSummary = """
        Pali Word: ${word.paliWord}
        Root & Grammar: ${word.rootAndGrammar}
        1. Classical PTS: ${word.ptsGloss}
        2. Gemini 4 Argon Philology: ${word.aiGloss}
        3. Thai Forest Master: ${word.forestMonkInsight}
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                word.paliWord,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
            Row {
                IconButton(onClick = { onCopy(formattedSummary) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                }
                IconButton(onClick = { onAddToNotes(formattedSummary) }) {
                    Icon(Icons.Default.NoteAdd, contentDescription = "Add to Notes")
                }
            }
        }

        Text(
            "Root & Grammar: ${word.rootAndGrammar}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider()

        Text("Word Rationale Across All 3 Standards:", style = MaterialTheme.typography.labelLarge)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("1. Classical PTS & Commentary:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(word.ptsGloss, fontSize = 13.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("2. Gemini 4 Argon Autonomous Philology:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(word.aiGloss, fontSize = 13.sp)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("3. Enlightened Thai Forest Monk:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(word.forestMonkInsight, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
