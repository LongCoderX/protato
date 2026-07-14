package com.protato.app.ui.screens.templates

import com.protato.app.*

import com.protato.app.ai.*
import com.protato.app.util.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.protato.app.ui.components.SecondaryHeader
import com.protato.app.ui.screens.templates.TemplateCard
import com.protato.app.ui.screens.templates.TemplateAiDialog
import com.protato.app.ui.screens.templates.TemplateEditorDialog

@Composable
internal fun TemplateScreen(
    templates: List<RecordTemplate>,
    selectedTemplateId: String,
    providers: List<LlmProviderSettings>,
    agents: List<AgentSettings>,
    nickname: String,
    onSelectedTemplate: (String) -> Unit,
    onUpsert: (RecordTemplate) -> Unit,
    onDelete: (RecordTemplate) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val requestController = remember(scope) { TemplateSuggestionController(scope) }
    var editingTemplate by remember { mutableStateOf<RecordTemplate?>(null) }
    var showingAiTemplate by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SecondaryHeader(
                title = "记录模板",
                subtitle = "自定义番茄结束后要回答的问题",
                onBack = onBack
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(onClick = { editingTemplate = emptyTemplate() }) {
                    Icon(Icons.Outlined.Add, contentDescription = "新建模板")
                }
                FilledIconButton(onClick = { showingAiTemplate = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_robot_svg),
                        contentDescription = "AI 生成模板"
                    )
                }
                Text(
                    "${templates.size} 个模板",
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        items(templates, key = { it.id }) { template ->
            TemplateCard(
                template = template,
                fieldLines = template.fields.mapIndexed { index, field ->
                    "${index + 1}. ${field.title} · ${field.type.label()}"
                },
                selected = template.id == selectedTemplateId,
                canDelete = templates.size > 1,
                onSelect = { onSelectedTemplate(template.id) },
                onEdit = { editingTemplate = template },
                onDelete = { onDelete(template) }
            )
        }
    }

    editingTemplate?.let { template ->
        TemplateEditorDialog(
            initialTemplate = template,
            newFieldId = ::newId,
            fieldTypeLabel = { it.label() },
            onDismiss = { editingTemplate = null },
            onSave = {
                onUpsert(it)
                editingTemplate = null
            }
        )
    }

    if (showingAiTemplate) {
        val agent = selectTemplateAgent(agents)
        val provider = agent?.providerId?.let { id -> providers.firstOrNull { it.id == id } }
            ?: providers.firstOrNull { it.isConfiguredForChat() }
        TemplateAiDialog(
            controller = requestController,
            generateSuggestion = { rawInput ->
                requestController.submit(rawInput, agent, provider, nickname)
            },
            fieldTypeLabel = { it.label() },
            onDismiss = { showingAiTemplate = false },
            onAddTemplate = { template ->
                onUpsert(template)
                showingAiTemplate = false
            }
        )
    }
}
