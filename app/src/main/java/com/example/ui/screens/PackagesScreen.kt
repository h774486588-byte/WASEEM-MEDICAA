package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.example.data.models.Patient
import com.example.data.models.PatientPackage
import com.example.ui.components.StatusBadge
import com.example.ui.components.formatArabicCurrency
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()

    var showAddPackageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("باقات المرضى والجلسات (${packages.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPackageDialog = true },
                containerColor = MedicalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_package_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إنشاء باقة جديدة")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (packages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PriceCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد باقات مسجلة حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(packages) { pkg ->
                        val patient = patients.find { it.id == pkg.patientId }
                        PackageCardItem(
                            pkg = pkg,
                            patientName = patient?.name ?: "مريض غير محدد",
                            onSendWhatsAppAlert = {
                                if (patient != null) {
                                    val msg = "تنبيه للمريض ${patient.name} بخصوص باقة ${pkg.packageName}:\nالجلسات المتبقية: ${pkg.remainingSessions} من إجمالي ${pkg.totalSessions} جلسة.\nنتمنى لكم دوام الصحة والعافية."
                                    viewModel.sendWhatsApp(context, patient.phone, msg)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddPackageDialog) {
        AddPackageDialogModal(
            viewModel = viewModel,
            onDismiss = { showAddPackageDialog = false }
        )
    }
}

@Composable
fun PackageCardItem(
    pkg: PatientPackage,
    patientName: String,
    onSendWhatsAppAlert: () -> Unit
) {
    val progress = if (pkg.totalSessions > 0) pkg.usedSessions.toFloat() / pkg.totalSessions.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("package_card_${pkg.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = patientName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = pkg.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MedicalBlue
                    )
                }
                StatusBadge(status = pkg.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المستخدم: ${pkg.usedSessions} جلسات",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المتبقي: ${pkg.remainingSessions} جلسة",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (pkg.remainingSessions <= 1) MedicalRed else if (pkg.remainingSessions <= 3) MedicalAmber else MedicalGreen
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (pkg.remainingSessions <= 1) MedicalRed else MedicalBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القيمة: ${formatArabicCurrency(pkg.price)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )

                if (pkg.remainingSessions in 1..3) {
                    Button(
                        onClick = onSendWhatsAppAlert,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pkg.remainingSessions == 1) MedicalRed else MedicalAmber
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pkg.remainingSessions == 1) "تنبيه: جلسة أخيرة" else "تنبيه المتبقي",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddPackageDialogModal(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsStateWithLifecycle()

    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var patientDropdownExpanded by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    var packageName by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var totalSessionsStr by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(viewModel.todayDateStr) }
    var endDate by remember {
        mutableStateOf(
            Calendar.getInstance()
                .apply { add(Calendar.DAY_OF_YEAR, 30) }
                .time
                .let(dateFormatter::format)
        )
    }
    var notes by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    fun formatPickerDate(millis: Long): String = dateFormatter.format(Date(millis))
    fun parseDateMillis(value: String): Long? = runCatching { dateFormatter.parse(value)?.time }.getOrNull()

    if (showStartDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = parseDateMillis(startDate))
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startDate = formatPickerDate(it) }
                    showStartDatePicker = false
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("إلغاء") }
            }
        ) { DatePicker(state = state) }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = parseDateMillis(endDate))
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endDate = formatPickerDate(it) }
                    showEndDatePicker = false
                }) { Text("تأكيد") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("إلغاء") }
            }
        ) { DatePicker(state = state) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "إنشاء باقة علاجية لمريض",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.name} (${it.fileNumber})" } ?: "اختر المريض",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المريض *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { patientDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = patientDropdownExpanded,
                        onDismissRequest = { patientDropdownExpanded = false }
                    ) {
                        patients.forEach { pat ->
                            DropdownMenuItem(
                                text = { Text("${pat.name} - ${pat.fileNumber}") },
                                onClick = {
                                    selectedPatient = pat
                                    patientDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("اسم الباقة *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                        label = { Text("السعر (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalSessionsStr,
                        onValueChange = { totalSessionsStr = it.filter { c -> c.isDigit() } },
                        label = { Text("عدد الجلسات") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(14.dp))

                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     OutlinedTextField(
                         value = startDate,
                         onValueChange = {},
                         readOnly = true,
                         label = { Text("تاريخ البداية") },
                         modifier = Modifier.weight(1f).clickable { showStartDatePicker = true }
                     )
                     OutlinedTextField(
                         value = endDate,
                         onValueChange = {},
                         readOnly = true,
                         label = { Text("تاريخ النهاية") },
                         modifier = Modifier.weight(1f).clickable { showEndDatePicker = true }
                     )
                 }

                 Spacer(modifier = Modifier.height(8.dp))

                 OutlinedTextField(
                     value = notes,
                     onValueChange = { notes = it },
                     label = { Text("ملاحظات الباقة") },
                     minLines = 2,
                     modifier = Modifier.fillMaxWidth()
                 )

                 Spacer(modifier = Modifier.height(16.dp))

Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pat = selectedPatient
                            val price = priceStr.toDoubleOrNull() ?: 0.0
                            val sessions = totalSessionsStr.toIntOrNull() ?: 0
                            if (pat == null || sessions <= 0) {
                                Toast.makeText(context, "يرجى تحديد مريض وعدد جلسات صحيح", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.createPackage(
                                patientId = pat.id,
                                packageName = packageName,
                                price = price,
                                totalSessions = sessions,
                                startDate = startDate,
                                endDate = endDate,
                                notes = notes
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                    ) {
                        Text("حفظ الباقة")
                    }
                }
            }
        }
    }
}
