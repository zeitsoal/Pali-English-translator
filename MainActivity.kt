package com.dhamma.palitranslator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dhamma.palitranslator.data.local.AppDatabase
import com.dhamma.palitranslator.data.remote.GeminiService
import com.dhamma.palitranslator.ui.history.HistoryScreen
import com.dhamma.palitranslator.ui.theme.PaliTranslatorTheme
import com.dhamma.palitranslator.ui.translator.TranslatorScreen
import com.dhamma.palitranslator.ui.translator.TranslatorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TranslatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)

        setContent {
            PaliTranslatorTheme {
                MainAppScaffold(viewModel = viewModel, db = db)
            }
        }
    }
}

@Composable
fun MainAppScaffold(
    viewModel: TranslatorViewModel,
    db: AppDatabase
) {
    var selectedNavIndex by remember { mutableStateOf(0) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = { Icon(Icons.Default.Translate, contentDescription = "Translator") },
                    label = { Text("Translate") }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History & Search") },
                    label = { Text("History") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedNavIndex) {
                0 -> TranslatorScreen(
                    viewModel = viewModel,
                    onOpenSettings = { showSettingsDialog = true }
                )
                1 -> HistoryScreen(
                    db = db,
                    onSelectEntity = { entity ->
                        viewModel.loadFromEntity(entity)
                        selectedNavIndex = 0
                    }
                )
            }

            if (showSettingsDialog) {
                ArgonSettingsDialog(
                    currentKey = uiState.apiKey,
                    currentModel = uiState.selectedModelId,
                    currentGoogleAccount = uiState.signedInGoogleAccount,
                    onDismiss = { showSettingsDialog = false },
                    onSave = { key, model, email ->
                        viewModel.saveSettings(key, model, email)
                        showSettingsDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ArgonSettingsDialog(
    currentKey: String,
    currentModel: String,
    currentGoogleAccount: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, String?) -> Unit
) {
    var keyInput by remember { mutableStateOf(currentKey) }
    var selectedModel by remember { mutableStateOf(currentModel) }
    var googleAccountInput by remember { mutableStateOf(currentGoogleAccount ?: "") }

    val models = listOf(
        GeminiService.DEFAULT_MODEL_ARGON to "Gemini 4 Argon (Frontier Reasoning - Recommended)",
        GeminiService.MODEL_PRO_1_5 to "Gemini 1.5 Pro (Deep Philology)",
        GeminiService.MODEL_FLASH_1_5 to "Gemini 1.5 Flash (Fast Lookup)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Google Account & Gemini 4 Argon Setup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = googleAccountInput,
                    onValueChange = { googleAccountInput = it },
                    label = { Text("Google Account Email") },
                    placeholder = { Text("you@gmail.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Gemini API Key (ai.google.dev)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Select AI Engine:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )

                models.forEach { (id, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedModel == id,
                            onClick = { selectedModel = id }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        keyInput.trim(),
                        selectedModel,
                        googleAccountInput.trim().ifBlank { null }
                    )
                }
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
