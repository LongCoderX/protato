package com.protato.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.protato.app.JIANGUOYUN_WEBDAV_BASE_URL
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_NAME
import com.protato.app.JIANGUOYUN_WEBDAV_FOLDER_URL
import com.protato.app.JIANGUOYUN_WEBDAV_PROVIDER
import com.protato.app.JIANGUOYUN_WEBDAV_BACKUP_FILE_URL
import com.protato.app.R
import com.protato.app.WebDavBackupSettings

@Composable
fun WebDavBackupSettingsCard(
    settings: WebDavBackupSettings,
    backingUp: Boolean,
    onBackup: () -> Unit,
    onChange: (WebDavBackupSettings) -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("坚果云 WebDAV 备份", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "协议强制 HTTPS · /${JIANGUOYUN_WEBDAV_FOLDER_NAME}/",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.enabled,
                    onCheckedChange = {
                        onChange(
                            settings.copy(
                                enabled = it,
                                endpoint = JIANGUOYUN_WEBDAV_BASE_URL,
                                cloudProvider = JIANGUOYUN_WEBDAV_PROVIDER
                            )
                        )
                    }
                )
            }
            OutlinedTextField(
                value = JIANGUOYUN_WEBDAV_FOLDER_URL,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备份目录（固定）") },
                singleLine = true,
                readOnly = true
            )
            OutlinedTextField(
                value = JIANGUOYUN_WEBDAV_BACKUP_FILE_URL,
                onValueChange = { },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备份文件（固定）") },
                singleLine = true,
                readOnly = true
            )
            OutlinedTextField(
                value = settings.username,
                onValueChange = {
                    onChange(
                        settings.copy(
                            username = it.take(120),
                            endpoint = JIANGUOYUN_WEBDAV_BASE_URL,
                            cloudProvider = JIANGUOYUN_WEBDAV_PROVIDER
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("用户名（坚果云注册邮箱）") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = settings.authToken,
                onValueChange = { value ->
                    onChange(
                        settings.copy(
                            authToken = value.take(240),
                            endpoint = JIANGUOYUN_WEBDAV_BASE_URL,
                            cloudProvider = JIANGUOYUN_WEBDAV_PROVIDER
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("密码（第三方应用密码）") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Text(
                "必须使用网页端“安全选项”中生成的第三方应用密码，不可使用坚果云登录密码。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                enabled = settings.enabled &&
                    settings.username.isNotBlank() &&
                    settings.authToken.isNotBlank() &&
                    !backingUp,
                onClick = onBackup,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (backingUp) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_webdav_sync),
                        contentDescription = "立即备份",
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(if (backingUp) "正在备份" else "立即备份")
            }
        }
    }
}

@Composable
fun PermissionCheckbox(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}

@Composable
fun SettingLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}
