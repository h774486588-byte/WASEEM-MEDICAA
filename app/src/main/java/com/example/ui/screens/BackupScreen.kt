package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var autoSyncEnabled by remember { mutableStateOf(settings?.autoSyncGoogleDrive ?: false) }
    var selectedInterval by remember { mutableIntStateOf(settings?.syncIntervalHours ?: 6) }
    var lastBackupTime by remember { mutableStateOf("لا توجد نسخة محفوظة بعد") }
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(settings) {
        settings?.let {
            autoSyncEnabled = it.autoSyncGoogleDrive
            selectedInterval = it.syncIntervalHours
        }
    }

    val intervals = listOf(
        Pair(1, "كل ساعة"),
        Pair(3, "كل 3 ساعات"),
        Pair(6, "كل 6 ساعات"),
        Pair(12, "كل 12 ساعة"),
        Pair(24, "مرة يوميًا")
    )

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val json = pendingBackupJson
        pendingBackupJson = null
        if (uri == null || json == null) return@rememberLauncherForActivityResult

        val saved = runCatching {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(json) }
                ?: error("تعذر فتح ملف الحفظ")
        }.isSuccess

        if (saved) {
            lastBackupTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())
            Toast.makeText(context, "تم حفظ النسخة الاحتياطية كاملةً بنجاح.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "تعذر حفظ النسخة الاحتياطية.", Toast.LENGTH_LONG).show()
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val json = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: error("تعذر قراءة الملف")
        }.getOrElse {
            Toast.makeText(context, "تعذر قراءة ملف النسخة الاحتياطية.", Toast.LENGTH_LONG).show()
            return@rememberLauncherForActivityResult
        }

        viewModel.restoreBackupJson(json) { success, message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("النسخ الاحتياطي والمزامنة", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Local Backup Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = MedicalBlue)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("النسخ الاحتياطي المحلي", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "يتم حفظ قاعدة البيانات والملفات والعمليات في مجلد النسخ الاحتياطي بالهاتف.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("آخر نسخة احتياطية محلية: $lastBackupTime", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    viewModel.createBackup { success, result ->
                                        if (!success) {
                                            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                                        } else {
                                            pendingBackupJson = result
                                            val fileName = "WaseemMedicalPro_Backup_" +
                                                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date()) +
                                                ".json"
                                            createBackupLauncher.launch(fileName)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                modifier = Modifier.weight(1f).testTag("create_backup_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("إنشاء نسخة الآن", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showRestoreConfirmDialog = true },
                                modifier = Modifier.weight(1f).testTag("restore_backup_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("استعادة نسخة")
                            }
                        }
                    }
                }
            }

            // Google Drive Cloud Sync Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = MedicalGreen)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("المزامنة مع Google Drive", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("الربط السحابي غير مفعّل في هذه النسخة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = false,
                                onCheckedChange = {},
                                enabled = false
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = MedicalBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "النسخ الاحتياطي المحلي يعمل حاليًا. يمكنك إنشاء ملف JSON ثم حفظه أو رفعه يدويًا إلى Google Drive.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        }
                    }
                }
            }
        }
    }

    // Restore Warning Confirmation Dialog
    if (showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MedicalRed) },
            title = { Text("تحذير استعادة البيانات") },
            text = {
                Text("سيتم استبدال البيانات الحالية بالنسخة المحددة. هل أنت متأكد من رغبتك في متابعة الاستعادة؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreConfirmDialog = false
                        restoreBackupLauncher.launch(arrayOf("application/json", "text/plain"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
                ) {
                    Text("نعم، استعادة الآن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
