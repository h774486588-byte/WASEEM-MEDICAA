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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.ClinicSession
import com.example.data.models.Department
import com.example.data.models.Doctor
import com.example.data.models.MedicalService
import com.example.data.models.Patient
import com.example.data.models.ReceiptVoucher
import com.example.data.models.Therapist
import com.example.ui.components.PrintAndShareHelper
import com.example.ui.components.SearchableDepartmentPicker
import com.example.ui.components.SearchableDoctorPicker
import com.example.ui.components.SearchablePatientPicker
import com.example.ui.components.SearchableServicePicker
import com.example.ui.components.SearchableTherapistPicker
import com.example.ui.components.StandardInvoicePreviewDialog
import com.example.ui.components.ThermalReceiptPreviewDialog
import com.example.ui.components.formatArabicCurrency
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberLight
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenLight
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

// =====================================================================
// 1. ADD PATIENT DIALOG (تسجيل حالة لأول مرة)
// Options: حفظ فقط، حفظ وإرسال واتساب، حفظ وإرسال SMS، طباعة كرت المريض
// =====================================================================
@Composable
fun AddPatientDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allDoctors by viewModel.doctors.collectAsStateWithLifecycle()
    val allTherapists by viewModel.therapists.collectAsStateWithLifecycle()
    val departments by viewModel.departments.collectAsStateWithLifecycle()
    val allServices by viewModel.services.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("ذكر") }
    var dateOfBirth by remember { mutableStateOf("") }
    var showDateOfBirthPicker by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var maritalStatus by remember { mutableStateOf("متزوج") }
    var profession by remember { mutableStateOf("") }
    var referralSource by remember { mutableStateOf("") }

    // Department & Service & Doctor & Specialist Linkage
    var selectedDepartment by remember(departments) { mutableStateOf<Department?>(departments.firstOrNull()) }

    val filteredServices = remember(selectedDepartment, allServices) {
        if (selectedDepartment == null) allServices
        else allServices.filter { it.departmentId == selectedDepartment!!.id }
    }
    var selectedService by remember(filteredServices) { mutableStateOf<MedicalService?>(filteredServices.firstOrNull()) }

    val filteredDoctors = remember(selectedDepartment, allDoctors) {
        if (selectedDepartment == null) allDoctors
        else {
            val docs = allDoctors.filter { it.departmentId == selectedDepartment!!.id }
            if (docs.isEmpty()) allDoctors else docs
        }
    }
    var selectedDoctor by remember(filteredDoctors) { mutableStateOf<Doctor?>(filteredDoctors.firstOrNull()) }

    val filteredTherapists = remember(selectedDepartment, allTherapists) {
        if (selectedDepartment == null) allTherapists
        else {
            val thers = allTherapists.filter { it.departmentId == selectedDepartment!!.id }
            if (thers.isEmpty()) allTherapists else thers
        }
    }
    var selectedTherapist by remember(filteredTherapists) { mutableStateOf<Therapist?>(filteredTherapists.firstOrNull()) }

    var diagnosis by remember { mutableStateOf("") }
    var complaint by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var initialBalanceStr by remember { mutableStateOf("0") }
    var autoCreateFirstSession by remember { mutableStateOf(false) }
    var notifyDoctor by remember { mutableStateOf(true) }

    var createdPatientForCard by remember { mutableStateOf<Patient?>(null) }

    if (showDateOfBirthPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateOfBirthPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dateOfBirth = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(it))
                        }
                        showDateOfBirthPicker = false
                    }
                ) { Text("اختيار") }
            },
            dismissButton = {
                TextButton(onClick = { showDateOfBirthPicker = false }) { Text("إلغاء") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (createdPatientForCard != null) {
        PatientCardPreviewDialog(
            patient = createdPatientForCard!!,
            centerName = settings?.centerName ?: "مركز وسيم الطبي والتأهيلي",
            onDismiss = {
                createdPatientForCard = null
                onDismiss()
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تسجيل حالة جديدة (لأول مرة)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ربط مباشر بالقسم والخدمة والمختص والطبيب المعالج",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المريض الثلاثي / الرباعي *") },
                        placeholder = { Text("مثال: محمد أحمد صالح") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("patient_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف المحمول (واتساب) *") },
                        placeholder = { Text("مثال: 771234567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("patient_phone_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Gender Selection
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("الجنس:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(10.dp))
                        RadioButton(selected = gender == "ذكر", onClick = { gender = "ذكر" })
                        Text("ذكر")
                        Spacer(modifier = Modifier.width(12.dp))
                        RadioButton(selected = gender == "أنثى", onClick = { gender = "أنثى" })
                        Text("أنثى")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("تاريخ الميلاد") },
                        placeholder = { Text("اضغط لاختيار تاريخ الميلاد") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "اختيار تاريخ الميلاد")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDateOfBirthPicker = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان / المدينة والمنطقة") },
                        placeholder = { Text("مثال: صنعاء - شارع حدة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Department Selector (ربط القسم)
                    SearchableDepartmentPicker(
                        departments = departments,
                        selectedDepartment = selectedDepartment,
                        onDepartmentSelected = { dept ->
                            selectedDepartment = dept
                            val svcs = allServices.filter { it.departmentId == dept.id }
                            selectedService = svcs.firstOrNull()
                            if (selectedService != null) {
                                initialBalanceStr = selectedService!!.price.toInt().toString()
                            }
                            val docs = allDoctors.filter { it.departmentId == dept.id }
                            if (docs.isNotEmpty()) selectedDoctor = docs.first()
                            val thers = allTherapists.filter { it.departmentId == dept.id }
                            if (thers.isNotEmpty()) selectedTherapist = thers.first()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Medical Service Selector (ربط الخدمة مع تسعيرتها)
                    SearchableServicePicker(
                        services = filteredServices,
                        selectedService = selectedService,
                        onServiceSelected = { srv ->
                            selectedService = srv
                            initialBalanceStr = srv.price.toInt().toString()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Specialist / Therapist Selector (ربط المختص / المعالج)
                    SearchableTherapistPicker(
                        therapists = filteredTherapists,
                        selectedTherapist = selectedTherapist,
                        onTherapistSelected = { selectedTherapist = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Doctor Selector (ربط الطبيب المشرف)
                    SearchableDoctorPicker(
                        doctors = filteredDoctors,
                        selectedDoctor = selectedDoctor,
                        onDoctorSelected = { selectedDoctor = it }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = { Text("التشخيص الطبي الأولي") },
                        placeholder = { Text("مثال: تأهيل ركبة، انزلاق غضروفي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = complaint,
                        onValueChange = { complaint = it },
                        label = { Text("الشكوى الرئيسية والأعراض") },
                        placeholder = { Text("ألم عند المشي، صعوبة الحركة...") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = initialBalanceStr,
                        onValueChange = { initialBalanceStr = it.filter { c -> c.isDigit() } },
                        label = { Text("رسوم فتح الملف / فاتورة أولية مستحقة (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if ((initialBalanceStr.toDoubleOrNull() ?: 0.0) > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MedicalAmberLight,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = "تنبيه: سيتم وضع الحالة كـ (معلّق) حتى سداد الفاتورة الأولية",
                                style = MaterialTheme.typography.bodySmall.copy(color = MedicalAmber, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Options Checkboxes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoCreateFirstSession, onCheckedChange = { autoCreateFirstSession = it })
                        Text(
                            text = "جدولة موعد وجلسة أولى تلقائياً في النظام للمختص والطبيب",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = notifyDoctor, onCheckedChange = { notifyDoctor = it })
                        Text(
                            text = "إشعار الطبيب والمختص بالحالة الجديدة في المركز",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ACTION BUTTONS SECTION (حفظ، حفظ + واتساب، حفظ + SMS، طباعة كرت)
                    Text("خيارات الحفظ والإشعار:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: Save Only & Save + WhatsApp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة اسم المريض ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val initialBal = initialBalanceStr.toDoubleOrNull() ?: 0.0
                                viewModel.registerPatientWithCommunication(
                                    name = name,
                                    phone = phone,
                                    gender = gender,
                                    dateOfBirth = dateOfBirth,
                                    address = address,
                                    maritalStatus = maritalStatus,
                                    profession = profession,
                                    referralSource = referralSource,
                                    doctorId = selectedDoctor?.id,
                                    therapistId = selectedTherapist?.id,
                                    departmentId = selectedDepartment?.id,
                                    serviceId = selectedService?.id,
                                    diagnosis = diagnosis.ifBlank { selectedService?.name ?: "استشارة وفحص عام" },
                                    complaint = complaint,
                                    notes = notes,
                                    initialBalance = initialBal,
                                    branchId = viewModel.currentBranchId,
                                    autoCreateFirstSessionOrAppt = autoCreateFirstSession,
                                    notifyDoctor = notifyDoctor,
                                    notifyWhatsApp = false,
                                    notifySMS = false,
                                    context = context
                                ) { success, _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            modifier = Modifier.weight(1f).testTag("save_patient_btn")
                        ) {
                            Text("حفظ فقط", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة اسم المريض ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val initialBal = initialBalanceStr.toDoubleOrNull() ?: 0.0
                                viewModel.registerPatientWithCommunication(
                                    name = name,
                                    phone = phone,
                                    gender = gender,
                                    dateOfBirth = dateOfBirth,
                                    address = address,
                                    maritalStatus = maritalStatus,
                                    profession = profession,
                                    referralSource = referralSource,
                                    doctorId = selectedDoctor?.id,
                                    therapistId = selectedTherapist?.id,
                                    departmentId = selectedDepartment?.id,
                                    serviceId = selectedService?.id,
                                    diagnosis = diagnosis.ifBlank { selectedService?.name ?: "استشارة وفحص عام" },
                                    complaint = complaint,
                                    notes = notes,
                                    initialBalance = initialBal,
                                    branchId = viewModel.currentBranchId,
                                    autoCreateFirstSessionOrAppt = autoCreateFirstSession,
                                    notifyDoctor = notifyDoctor,
                                    notifyWhatsApp = true,
                                    notifySMS = false,
                                    context = context
                                ) { success, _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ + واتساب", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Save + SMS & Print Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (name.isBlank() || phone.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة اسم المريض ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                val initialBal = initialBalanceStr.toDoubleOrNull() ?: 0.0
                                viewModel.registerPatientWithCommunication(
                                    name = name,
                                    phone = phone,
                                    gender = gender,
                                    dateOfBirth = dateOfBirth,
                                    address = address,
                                    maritalStatus = maritalStatus,
                                    profession = profession,
                                    referralSource = referralSource,
                                    doctorId = selectedDoctor?.id,
                                    therapistId = selectedTherapist?.id,
                                    departmentId = selectedDepartment?.id,
                                    serviceId = selectedService?.id,
                                    diagnosis = diagnosis.ifBlank { selectedService?.name ?: "استشارة وفحص عام" },
                                    complaint = complaint,
                                    notes = notes,
                                    initialBalance = initialBal,
                                    branchId = viewModel.currentBranchId,
                                    autoCreateFirstSessionOrAppt = autoCreateFirstSession,
                                    notifyDoctor = notifyDoctor,
                                    notifyWhatsApp = false,
                                    notifySMS = true,
                                    context = context
                                ) { success, _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ + رسالة SMS", style = MaterialTheme.typography.bodySmall)
                        }

                        OutlinedButton(
                            onClick = {
                                if (name.isBlank() || phone.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة اسم المريض ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                val initialBal = initialBalanceStr.toDoubleOrNull() ?: 0.0
                                viewModel.registerPatientWithCommunication(
                                    name = name,
                                    phone = phone,
                                    gender = gender,
                                    dateOfBirth = dateOfBirth,
                                    address = address,
                                    maritalStatus = maritalStatus,
                                    profession = profession,
                                    referralSource = referralSource,
                                    doctorId = selectedDoctor?.id,
                                    therapistId = selectedTherapist?.id,
                                    departmentId = selectedDepartment?.id,
                                    serviceId = selectedService?.id,
                                    diagnosis = diagnosis.ifBlank { selectedService?.name ?: "استشارة وفحص عام" },
                                    complaint = complaint,
                                    notes = notes,
                                    initialBalance = initialBal,
                                    branchId = viewModel.currentBranchId,
                                    autoCreateFirstSessionOrAppt = autoCreateFirstSession,
                                    notifyDoctor = notifyDoctor,
                                    notifyWhatsApp = false,
                                    notifySMS = false,
                                    context = context
                                ) { success, createdPat, msg ->
                                    if (success && createdPat != null) {
                                        createdPatientForCard = createdPat
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة كرت المريض", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 2. DELIVER & SETTLE INVOICE DIALOG (تسليم وسداد الفاتورة وسند القبض)
// Options: حفظ، حفظ وإرسال واتساب، حفظ وإرسال SMS، طباعة حرارية 80mm، طباعة عادية A4
// =====================================================================
@Composable
fun AddReceiptDialog(
    viewModel: ClinicViewModel,
    targetPatient: Patient? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var patientSearch by remember { mutableStateOf("") }
    var selectedPatient by remember { mutableStateOf<Patient?>(targetPatient) }
    var amountStr by remember {
        mutableStateOf(
            if (targetPatient != null && targetPatient.balanceDue > 0)
                targetPatient.balanceDue.toInt().toString()
            else ""
        )
    }
    var paymentMethod by remember { mutableStateOf("نقدًا") }
    var statement by remember {
        mutableStateOf(
            if (targetPatient != null && targetPatient.balanceDue > 0)
                "سداد دفعة من فاتورة مستحقة"
            else "دفعة نقدية لحساب المريض"
        )
    }

    // Modal preview states for Thermal and Standard Print
    var showThermalPreview by remember { mutableStateOf<Pair<ReceiptVoucher, Patient>?>(null) }
    var showStandardPreview by remember { mutableStateOf<Pair<ReceiptVoucher, Patient>?>(null) }

    val filtered = if (patientSearch.isBlank()) patients.take(6)
    else patients.filter {
        it.name.contains(patientSearch, ignoreCase = true) ||
                it.phone.contains(patientSearch) ||
                it.fileNumber.contains(patientSearch, ignoreCase = true)
    }

    if (showThermalPreview != null) {
        val (rcpt, pat) = showThermalPreview!!
        ThermalReceiptPreviewDialog(
            receipt = rcpt,
            patient = pat,
            centerSettings = settings,
            onDismiss = {
                showThermalPreview = null
                onDismiss()
            }
        )
    } else if (showStandardPreview != null) {
        val (rcpt, pat) = showStandardPreview!!
        StandardInvoicePreviewDialog(
            receipt = rcpt,
            patient = pat,
            centerSettings = settings,
            onDismiss = {
                showStandardPreview = null
                onDismiss()
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تسليم وسداد الفاتورة / سند قبض",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحديث رصيد المريض وفك التعليق تلقائياً فور السداد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Patient Picker
                    if (selectedPatient == null) {
                        OutlinedTextField(
                            value = patientSearch,
                            onValueChange = { patientSearch = it },
                            label = { Text("بحث عن المريض (الاسم، الهاتف، الملف)...") },
                            placeholder = { Text("اكتب اسم المريض لاختياره") },
                            modifier = Modifier.fillMaxWidth().testTag("receipt_patient_search")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        filtered.forEach { pat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedPatient = pat
                                        if (pat.balanceDue > 0) {
                                            amountStr = pat.balanceDue.toInt().toString()
                                            statement = "سداد فاتورة مستحقة للمريض"
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (pat.balanceDue > 0) MedicalRedLight.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pat.name, fontWeight = FontWeight.Bold)
                                        Text("ملف: ${pat.fileNumber} | ${pat.phone}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (pat.balanceDue > 0) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = MedicalRed) {
                                            Text(
                                                text = "معلّق: ${formatArabicCurrency(pat.balanceDue)}",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    } else {
                                        Text("الرصيد: 0", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    } else {
                        // Selected Patient Card
                        val pat = selectedPatient!!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (pat.balanceDue > 0) MedicalRedLight else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("رقم الملف: ${pat.fileNumber} | هاتف: ${pat.phone}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (targetPatient == null) {
                                        OutlinedButton(onClick = { selectedPatient = null }) {
                                            Text("تغيير المريض")
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("الفاتورة / الرصيد المستحق:", fontWeight = FontWeight.Bold)
                                    Text(
                                        formatArabicCurrency(pat.balanceDue),
                                        fontWeight = FontWeight.Bold,
                                        color = if (pat.balanceDue > 0) MedicalRed else MedicalGreen
                                    )
                                }
                                if (pat.balanceDue > 0) {
                                    Text(
                                        text = "⚠️ المريض معلّق في النظام بسبب هذه الفاتورة. فور السداد سيعود نشطاً.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MedicalRed,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Amount Input
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { char -> char.isDigit() } },
                        label = { Text("المبلغ المسدد حالياً (ر.ي) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text("أدخل المبلغ المسدد") },
                        modifier = Modifier.fillMaxWidth().testTag("receipt_amount_input")
                    )

                    // Live calculation of remaining balance
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val currentDue = selectedPatient?.balanceDue ?: 0.0
                    val remAfter = max(0.0, currentDue - amt)

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المتبقي بعد هذا السداد:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatArabicCurrency(remAfter),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = if (remAfter > 0) MedicalRed else MedicalGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Payment Method
                    Text("طريقة الدفع:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        listOf("نقدًا", "تحويل", "شبكة").forEach { method ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = paymentMethod == method, onClick = { paymentMethod = method })
                                Text(method, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = statement,
                        onValueChange = { statement = it },
                        label = { Text("البيان والغرض من السند") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. ACTION BUTTONS (حفظ، حفظ وإرسال واتساب، حفظ وإرسال SMS، طباعة حرارية، طباعة عادية)
                    Text("خيارات تسليم الفاتورة والطباعة:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: حفظ فقط & حفظ + إرسال واتساب
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val pat = selectedPatient
                                if (pat == null) {
                                    Toast.makeText(context, "يرجى تحديد مريض أولاً", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (amt <= 0) {
                                    Toast.makeText(context, "يرجى إدخال مبلغ صحيح أكبر من صفر", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.deliverAndSettleInvoice(
                                    patientId = pat.id,
                                    amount = amt,
                                    paymentMethod = paymentMethod,
                                    statement = statement,
                                    sendWhatsApp = false,
                                    sendSMS = false,
                                    context = context
                                ) { success, _, _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            modifier = Modifier.weight(1f).testTag("save_receipt_btn")
                        ) {
                            Text("حفظ فقط", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val pat = selectedPatient
                                if (pat == null) {
                                    Toast.makeText(context, "يرجى تحديد مريض أولاً", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (amt <= 0) {
                                    Toast.makeText(context, "يرجى إدخال مبلغ صحيح أكبر من صفر", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.deliverAndSettleInvoice(
                                    patientId = pat.id,
                                    amount = amt,
                                    paymentMethod = paymentMethod,
                                    statement = statement,
                                    sendWhatsApp = true,
                                    sendSMS = false,
                                    context = context
                                ) { success, _, _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) onDismiss()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ + واتساب", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: حفظ + إرسال SMS
                    OutlinedButton(
                        onClick = {
                            val pat = selectedPatient
                            if (pat == null) {
                                Toast.makeText(context, "يرجى تحديد مريض أولاً", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            if (amt <= 0) {
                                Toast.makeText(context, "يرجى إدخال مبلغ صحيح أكبر من صفر", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            viewModel.deliverAndSettleInvoice(
                                patientId = pat.id,
                                amount = amt,
                                paymentMethod = paymentMethod,
                                statement = statement,
                                sendWhatsApp = false,
                                sendSMS = true,
                                context = context
                            ) { success, _, _, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ + إرسال رسالة نصية SMS", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: طباعة حرارية (Thermal 80mm) & طباعة عادية (Standard A4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val pat = selectedPatient
                                if (pat == null) {
                                    Toast.makeText(context, "يرجى تحديد مريض أولاً", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (amt <= 0) {
                                    Toast.makeText(context, "يرجى إدخال مبلغ صحيح أكبر من صفر", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.deliverAndSettleInvoice(
                                    patientId = pat.id,
                                    amount = amt,
                                    paymentMethod = paymentMethod,
                                    statement = statement,
                                    sendWhatsApp = false,
                                    sendSMS = false,
                                    context = context
                                ) { success, receipt, updatedPatient, _ ->
                                    if (success && receipt != null && updatedPatient != null) {
                                        showThermalPreview = Pair(receipt, updatedPatient)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة حرارية", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                val pat = selectedPatient
                                if (pat == null) {
                                    Toast.makeText(context, "يرجى تحديد مريض أولاً", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                if (amt <= 0) {
                                    Toast.makeText(context, "يرجى إدخال مبلغ صحيح أكبر من صفر", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                viewModel.deliverAndSettleInvoice(
                                    patientId = pat.id,
                                    amount = amt,
                                    paymentMethod = paymentMethod,
                                    statement = statement,
                                    sendWhatsApp = false,
                                    sendSMS = false,
                                    context = context
                                ) { success, receipt, updatedPatient, _ ->
                                    if (success && receipt != null && updatedPatient != null) {
                                        showStandardPreview = Pair(receipt, updatedPatient)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة عادية A4", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// 3. SESSION ATTENDANCE DIALOG (تسجيل حضور حالة وجلسة علاج مع واتس وSMS)
// Options: تأكيد الحضور فقط، حضور + واتساب، حضور + SMS، طباعة تذكرة حضور حرارية
// =====================================================================
@Composable
fun SessionAttendanceDialog(
    session: ClinicSession,
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val patient = patients.find { it.id == session.patientId }
    val pkg = packages.find { it.id == session.packageId || it.patientId == session.patientId }
    val remaining = pkg?.remainingSessions ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MedicalGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedicalGreen)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "تسجيل حضور جلسة علاج",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "خصم تلقائي من الباقة ومنع الخصم المكرر",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Patient Info Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = patient?.name ?: "مريض غير محدد",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ملف: ${patient?.fileNumber ?: "-"} | هاتف: ${patient?.phone ?: "-"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رقم الجلسة:", style = MaterialTheme.typography.bodySmall)
                            Text(session.sessionNumber, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تاريخ ووقت الجلسة:", style = MaterialTheme.typography.bodySmall)
                            Text("${session.date} - ${session.time}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (pkg != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("الباقة الحالية:", style = MaterialTheme.typography.bodySmall)
                                Text("${pkg.packageName} (متبقي $remaining)", fontWeight = FontWeight.Bold, color = MedicalBlue)
                            }
                        }
                        if (patient != null && patient.balanceDue > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = MedicalRedLight) {
                                Text(
                                    text = "تنبيه: على المريض فاتورة معلقة بقيمة ${formatArabicCurrency(patient.balanceDue)}",
                                    color = MedicalRed,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("خيارات تسجيل الحضور والإشعار:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(8.dp))

                // Actions:
                // 1. حضور فقط
                Button(
                    onClick = {
                        viewModel.attendSessionWithCommunication(
                            sessionId = session.id,
                            notifyWhatsApp = false,
                            notifySMS = false,
                            context = context
                        ) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد الحضور والخصم فقط", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. حضور + واتساب
                Button(
                    onClick = {
                        viewModel.attendSessionWithCommunication(
                            sessionId = session.id,
                            notifyWhatsApp = true,
                            notifySMS = false,
                            context = context
                        ) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            if (success) onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حضور + إرسال إشعار واتساب للمريض", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Row: حضور + SMS & طباعة تذكرة حرارية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.attendSessionWithCommunication(
                                sessionId = session.id,
                                notifyWhatsApp = false,
                                notifySMS = true,
                                context = context
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("حضور + SMS", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.attendSessionWithCommunication(
                                sessionId = session.id,
                                notifyWhatsApp = false,
                                notifySMS = false,
                                context = context
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success && patient != null) {
                                    val text = PrintAndShareHelper.generateAttendanceTicketText(
                                        session = session,
                                        patient = patient,
                                        remainingSessions = max(0, remaining - 1),
                                        centerName = settings?.centerName ?: "مركز وسيم الطبي والتأهيلي"
                                    )
                                    PrintAndShareHelper.shareText(context, "تذكرة حضور جلسة", text)
                                }
                                if (success) onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة تذكرة", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// =====================================================================
// 4. ADD APPOINTMENT DIALOG (حجز موعد)
// =====================================================================
@Composable
fun AddAppointmentDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val services by viewModel.services.collectAsStateWithLifecycle()

    var selectedPatient by remember { mutableStateOf<Patient?>(patients.firstOrNull()) }
    var patientDropdownExpanded by remember { mutableStateOf(false) }

    var selectedDoctor by remember { mutableStateOf<Doctor?>(doctors.firstOrNull()) }
    var doctorDropdownExpanded by remember { mutableStateOf(false) }

    var date by remember { mutableStateOf(viewModel.todayDateStr) }
    var timeSlot by remember { mutableStateOf("09:30 ص") }
    var notes by remember { mutableStateOf("") }

    val times = listOf("09:00 ص", "09:30 ص", "10:00 ص", "10:30 ص", "11:00 ص", "11:30 ص", "04:00 ع", "04:30 ع", "05:00 ع", "06:00 ع")
    var timeDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "حجز موعد طبي جديد",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "يتحقق النظام من عدم تعارض المواعيد للطبيب",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Patient Picker
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

                // Doctor Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedDoctor?.name ?: "اختر الطبيب",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("الطبيب / المعالج *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { doctorDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = doctorDropdownExpanded,
                        onDismissRequest = { doctorDropdownExpanded = false }
                    ) {
                        doctors.forEach { doc ->
                            DropdownMenuItem(
                                text = { Text("${doc.name} (${doc.specialization})") },
                                onClick = {
                                    selectedDoctor = doc
                                    doctorDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("التاريخ (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Slot Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = timeSlot,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("وقت الموعد *") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { timeDropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = timeDropdownExpanded,
                        onDismissRequest = { timeDropdownExpanded = false }
                    ) {
                        times.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    timeSlot = t
                                    timeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val pat = selectedPatient
                            if (pat == null) {
                                Toast.makeText(context, "يرجى اختيار مريض", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.bookAppointment(
                                patientId = pat.id,
                                doctorId = selectedDoctor?.id,
                                therapistId = null,
                                departmentId = selectedDoctor?.departmentId,
                                serviceId = services.firstOrNull()?.id,
                                date = date,
                                timeSlot = timeSlot,
                                notes = notes
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                    ) {
                        Text("تأكيد الحجز")
                    }
                }
            }
        }
    }
}

// =====================================================================
// 5. ADD EXPENSE VOUCHER DIALOG (سند صرف)
// =====================================================================
@Composable
fun AddExpenseDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var beneficiaryType by remember { mutableStateOf("مورد") }
    var beneficiaryName by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("نقدًا") }
    var category by remember { mutableStateOf("مستلزمات طبية") }
    var statement by remember { mutableStateOf("") }

    val types = listOf("موظف", "طبيب", "معالج", "مورد", "جهة أخرى")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "إنشاء سند صرف جديد",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("نوع المستفيد:", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    types.forEach { type ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = beneficiaryType == type, onClick = { beneficiaryType = type })
                            Text(type, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = beneficiaryName,
                    onValueChange = { beneficiaryName = it },
                    label = { Text("اسم المستفيد *") },
                    placeholder = { Text("مثال: شركة الأدوية / اسم الموظف") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("المبلغ المصروف (لوحة أرقام) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("البيان والغرض من الصرف") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (beneficiaryName.isBlank() || amt <= 0) {
                                Toast.makeText(context, "يرجى كتابة اسم المستفيد وإدخال مبلغ صحيح", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.createExpenseVoucher(
                                beneficiaryType = beneficiaryType,
                                beneficiaryName = beneficiaryName,
                                amount = amt,
                                paymentMethod = paymentMethod,
                                category = category,
                                statement = statement
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
                    ) {
                        Text("حفظ سند الصرف")
                    }
                }
            }
        }
    }
}

// =====================================================================
// 6. PATIENT CARD PREVIEW DIALOG (طباعة كرت المريض)
// =====================================================================
@Composable
fun PatientCardPreviewDialog(
    patient: Patient,
    centerName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("كرت ملف المريض الطبي", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(centerName, fontWeight = FontWeight.Bold, color = MedicalBlue)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("اسم المريض: ${patient.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("رقم الملف الطبي: ${patient.fileNumber}", fontWeight = FontWeight.Bold, color = MedicalTeal)
                        Text("رقم الهاتف: ${patient.phone}")
                        Text("العنوان: ${patient.address.ifBlank { "صنعاء" }}")
                        Text("التشخيص: ${patient.diagnosis.ifBlank { "استشارة عامة" }}")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val text = "================================\n$centerName\nبطاقة المريض الطبية\nرقم الملف: ${patient.fileNumber}\nالمريض: ${patient.name}\nالهاتف: ${patient.phone}\nالتشخيص: ${patient.diagnosis}\n================================"
                        PrintAndShareHelper.shareText(context, "بطاقة المريض", text)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة / مشاركة كرت المريض", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
