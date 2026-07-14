package com.protato.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.protato.app.LlmProviderSettings
import kotlinx.coroutines.launch

@Composable
fun RecordPolishProviderCard(
    providers: List<LlmProviderSettings>,
    selectedProviderId: String,
    providerLabel: (LlmProviderSettings) -> String,
    providerStatus: (LlmProviderSettings?) -> String,
    onProviderSelected: (String) -> Unit
) {
    var providerMenuExpanded by remember { mutableStateOf(false) }
    val selectedProvider = providers.firstOrNull { it.id == selectedProviderId }

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("总结润色模型", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Box {
                OutlinedButton(
                    enabled = providers.isNotEmpty(),
                    onClick = { providerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedProvider?.let(providerLabel) ?: "选择 Provider",
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = providerMenuExpanded,
                    onDismissRequest = { providerMenuExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(providerLabel(provider)) },
                            onClick = {
                                onProviderSelected(provider.id)
                                providerMenuExpanded = false
                            }
                        )
                    }
                }
            }
            Text(
                providerStatus(selectedProvider),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LlmProviderEditDialog(
    initialProvider: LlmProviderSettings,
    allowDelete: Boolean,
    providerPresets: List<LlmProviderSettings>,
    fetchModels: suspend (endpoint: String, apiKey: String) -> List<String>,
    onDismiss: () -> Unit,
    onSave: (LlmProviderSettings) -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var provider by remember(initialProvider.id) { mutableStateOf(initialProvider) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var modelMenuExpanded by remember { mutableStateOf(false) }
    var fetchedModels by remember { mutableStateOf<List<String>>(emptyList()) }
    var loadingModels by remember { mutableStateOf(false) }
    var modelFetchError by remember { mutableStateOf<String?>(null) }
    val apiKeyMask = "***"
    val apiKeyDisplay = if (provider.apiKey.isBlank()) "" else apiKeyMask

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(provider) }) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                if (allowDelete) {
                    TextButton(onClick = onDelete) {
                        Text("删除")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        },
        title = { Text("编辑 Provider") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box {
                    OutlinedButton(
                        onClick = { providerMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(provider.name, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                        Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = providerMenuExpanded,
                        onDismissRequest = { providerMenuExpanded = false }
                    ) {
                        providerPresets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.name) },
                                onClick = {
                                    provider = provider.copy(
                                        providerKey = preset.providerKey,
                                        name = preset.name,
                                        endpoint = provider.endpoint.ifBlank { preset.endpoint }
                                    )
                                    providerMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = provider.name,
                    onValueChange = { provider = provider.copy(name = it.take(40)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("显示名称") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = provider.endpoint,
                    onValueChange = {
                        provider = provider.copy(endpoint = it.take(240))
                        fetchedModels = emptyList()
                        modelFetchError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("接口地址") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        enabled = provider.endpoint.isNotBlank() && !loadingModels,
                        onClick = {
                            scope.launch {
                                loadingModels = true
                                modelFetchError = null
                                fetchedModels = runCatching {
                                    fetchModels(provider.endpoint, provider.apiKey)
                                }.getOrElse { error ->
                                    modelFetchError = error.message ?: "获取模型失败"
                                    emptyList()
                                }
                                loadingModels = false
                            }
                        }
                    ) {
                        if (loadingModels) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("获取模型")
                    }
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(
                            enabled = fetchedModels.isNotEmpty(),
                            onClick = { modelMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                provider.modelName.ifBlank { "选择模型" },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Start
                            )
                            Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = modelMenuExpanded,
                            onDismissRequest = { modelMenuExpanded = false }
                        ) {
                            fetchedModels.forEach { model ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            model,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        provider = provider.copy(modelName = model)
                                        modelMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (fetchedModels.isEmpty()) {
                    OutlinedTextField(
                        value = provider.modelName,
                        onValueChange = { provider = provider.copy(modelName = it.take(80)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("模型名称") },
                        singleLine = true
                    )
                }
                modelFetchError?.let { error ->
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(
                    value = apiKeyDisplay,
                    onValueChange = { value ->
                        provider = when {
                            value.isBlank() -> provider.copy(apiKey = "")
                            value == apiKeyMask -> provider
                            value.startsWith(apiKeyMask) -> {
                                provider.copy(apiKey = value.removePrefix(apiKeyMask).take(240))
                            }
                            else -> provider.copy(apiKey = value.take(240))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("API Key") },
                    singleLine = true
                )
            }
        }
    )
}

@Composable
fun LlmProviderListCard(
    providers: List<LlmProviderSettings>,
    newProvider: () -> LlmProviderSettings,
    defaultProviderIds: Set<String>,
    providerPresets: List<LlmProviderSettings>,
    providerLabel: (LlmProviderSettings) -> String,
    fetchModels: suspend (endpoint: String, apiKey: String) -> List<String>,
    onUpsert: (LlmProviderSettings) -> Unit,
    onDelete: (LlmProviderSettings) -> Unit
) {
    var editingProvider by remember { mutableStateOf<LlmProviderSettings?>(null) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var selectedProviderId by rememberSaveable(providers.map { it.id }.joinToString("|")) {
        mutableStateOf(providers.firstOrNull()?.id.orEmpty())
    }
    val selectedProvider = providers.firstOrNull { it.id == selectedProviderId }
        ?: providers.firstOrNull()

    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "大模型 Provider",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = { editingProvider = newProvider() }) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("自定义")
                }
            }
            Box {
                OutlinedButton(
                    onClick = { providerMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedProvider?.let(providerLabel) ?: "选择 Provider",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = providerMenuExpanded,
                    onDismissRequest = { providerMenuExpanded = false }
                ) {
                    providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(providerLabel(provider)) },
                            onClick = {
                                selectedProviderId = provider.id
                                providerMenuExpanded = false
                            }
                        )
                    }
                }
            }
            selectedProvider?.let { provider ->
                LlmProviderRow(provider = provider, onClick = { editingProvider = provider })
            }
        }
    }

    editingProvider?.let { provider ->
        LlmProviderEditDialog(
            initialProvider = provider,
            allowDelete = provider.id !in defaultProviderIds,
            providerPresets = providerPresets,
            fetchModels = fetchModels,
            onDismiss = { editingProvider = null },
            onSave = {
                onUpsert(it)
                editingProvider = null
            },
            onDelete = {
                onDelete(provider)
                editingProvider = null
            }
        )
    }
}

@Composable
fun LlmProviderRow(provider: LlmProviderSettings, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(provider.name, fontWeight = FontWeight.SemiBold)
                Text(
                    listOf(
                        provider.modelName.ifBlank { "未选择模型" },
                        provider.endpoint.ifBlank { "未设置接口" }
                    ).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                if (provider.apiKey.isBlank()) "未配置" else "已配置",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
