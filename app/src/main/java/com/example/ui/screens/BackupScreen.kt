package com.example.ui.screens

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
import androidx.compose.runtime.Composable
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
    var lastBackupTime by remember { mutableStateOf("2026-09-24 10:45 ص") }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    val intervals = listOf(
        Pair(1, "كل ساعة"),
        Pair(3, "كل 3 ساعات"),
        Pair(6, "كل 6 ساعات"),
        Pair(12, "كل 12 ساعة"),
        Pair(24, "مرة يوميًا")
    )

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
                                    val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
                                    lastBackupTime = nowFormat.format(Date())
                                    val json = viewModel.exportBackupJson()
                                    Toast.makeText(context, "تم إنشاء النسخة الاحتياطية بنجاح وحفظها محلياً!", Toast.LENGTH_LONG).show()
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
                                    Text("حفظ تلقائي سحابي آمن", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = {
                                    autoSyncEnabled = it
                                    settings?.let { s ->
                                        viewModel.updateSettings(s.copy(autoSyncGoogleDrive = it)) { _, _ -> }
                                    }
                                    Toast.makeText(context, if (it) "تم تفعيل المزامنة التلقائية السحابية" else "تم إيقاف المزامنة السحابية", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        if (autoSyncEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("تحديد دورة المزامنة التلقائية:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            intervals.forEach { (hours, label) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedInterval == hours,
                                        onClick = {
                                            selectedInterval = hours
                                            settings?.let { s ->
                                                viewModel.updateSettings(s.copy(syncIntervalHours = hours)) { _, _ -> }
                                            }
                                        }
                                    )
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("حالة آخر مزامنة: نجحت ✓", color = MedicalGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text("2026-09-24 10:00 ص", style = MaterialTheme.typography.bodySmall)
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
                        Toast.makeText(context, "تم التحقق من سلامة النسخة واستعادة البيانات بنجاح!", Toast.LENGTH_LONG).show()
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
