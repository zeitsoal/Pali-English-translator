package com.dhamma.palitranslator.ui.history

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhamma.palitranslator.data.local.AppDatabase
import com.dhamma.palitranslator.data.local.PaliTranslationEntity
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    db: AppDatabase,
    onSelectEntity: (PaliTranslationEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyBookmarks by remember { mutableStateOf(false) }

    val dao = db.paliDao()
    val scope = rememberCoroutineScope()

    val entitiesFlow = remember(searchQuery, showOnlyBookmarks) {
        when {
            searchQuery.isNotBlank() -> dao.searchTranslations(searchQuery)
            showOnlyBookmarks -> dao.getBookmarks()
            else -> dao.getAllHistory()
        }
    }

    val entities by entitiesFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (showOnlyBookmarks) "Bookmarks" else "Translation History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Pali text, English, or Notes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true
            )

            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !showOnlyBookmarks,
                    onClick = { showOnlyBookmarks = false },
                    label = { Text("All History") }
                )
                FilterChip(
                    selected = showOnlyBookmarks,
                    onClick = { showOnlyBookmarks = true },
                    label = { Text("Bookmarks") },
                    leadingIcon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            if (entities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isNotEmpty()) "No matching translations found." else "No history yet. Start translating!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(entities, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            onClick = { onSelectEntity(item) },
                            onToggleBookmark = {
                                scope.launch {
                                    dao.updateTranslation(item.copy(isBookmarked = !item.isBookmarked))
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    dao.deleteTranslation(item)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    item: PaliTranslationEntity,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = DateFormat.format("MMM dd, yyyy · HH:mm", Date(item.timestamp)).toString()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateString, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Row {
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(24.dp)) {
                        Icon(
                            if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (item.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                item.sourcePaliText.lines().firstOrNull() ?: item.sourcePaliText,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Serif),
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )

            if (item.summary.isNotBlank()) {
                Text(
                    item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (item.userNotes.isNotBlank()) {
                Text(
                    "Notes: ${item.userNotes}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}
