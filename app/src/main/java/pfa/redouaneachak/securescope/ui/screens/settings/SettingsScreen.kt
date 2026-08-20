package pfa.redouaneachak.securescope.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.data.model.AppLanguage
import pfa.redouaneachak.securescope.data.model.AppTheme

@Composable
fun SettingsScreen(
    onNavigateToGuide: () -> Unit,
    onNavigateToUserAgreement: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(28.dp)) {
            LanguageRow(selected = language, onSelect = viewModel::setLanguage)

            SettingsToggleRow(
                label = "Dark mode",
                checked = theme == AppTheme.DARK,
                onCheckedChange = { isDark -> viewModel.setTheme(if (isDark) AppTheme.DARK else AppTheme.LIGHT) }
            )

            SettingsActionRow(Icons.Filled.Share, "Share") {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out Secure Scope, a security monitor for Android.")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Secure Scope"))
            }

            SettingsActionRow(Icons.Filled.MenuBook, "App Guide", onNavigateToGuide)
            SettingsActionRow(Icons.Filled.Description, "User Agreement", onNavigateToUserAgreement)
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SECURE SCOPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Powered by Inovext", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LanguageRow(selected: AppLanguage, onSelect: (AppLanguage) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text("Language", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Box {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { expanded = true }) {
                Text(if (selected == AppLanguage.ENGLISH) "En" else "Fr", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("English") }, onClick = { onSelect(AppLanguage.ENGLISH); expanded = false })
                DropdownMenuItem(text = { Text("Français") }, onClick = { onSelect(AppLanguage.FRENCH); expanded = false })
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFF0F7B6C), modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null)
    }
}