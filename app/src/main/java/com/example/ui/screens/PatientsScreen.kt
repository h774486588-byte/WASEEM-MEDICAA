package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Patient
import com.example.ui.components.PrintAndShareHelper
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit,
    onOpenAddPatient: () -> Unit
) {
    val context = LocalContext.current
    val patients by viewModel.filteredPatients.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var selectedPatientForSheet by remember { mutableStateOf<Patient?>(null) }
    var deliverInvoicePatient by remember { mutableStateOf<Patient?>(null) }
    var cardPreviewPatient by remember { mutableStateOf<Patient?>(null) }

    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: الكل, 1: النشطين, 2: المعلقين

    val displayPatients = when (selectedFilterTab) {
        1 -> patients.filter { it.balanceDue <= 0 }
        2 -> patients.filter { it.balanceDue > 0 }
        else -> patients
    }

    val suspendedCount = patients.count { it.balanceDue > 0 }

    // Dialog: Deliver & Settle Invoice
    if (deliverInvoicePatient != null) {
        AddReceiptDialog(
            viewModel = viewModel,
            targetPatient = deliverInvoicePatient,
            onDismiss = { deliverInvoicePatient = null }
        )
    }

    // Dialog: Card Preview
    if (cardPreviewPatient != null) {
        PatientCardPreviewDialog(
            patient = cardPreviewPatient!!,
            centerName = settings?.centerName ?: "مركز وسيم الطبي والتأهيلي",
            onDismiss = { cardPreviewPatient = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المرضى والملفات الطبية (${patients.size})", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddPatient,
                containerColor = MedicalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_patient_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مريض جديد")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("بحث فوري بالاسم (أح..)، الهاتف، رقم الملف...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("patient_search_field"),
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Tabs (الكل، النشطين، المعلقين)
            TabRow(selectedTabIndex = selectedFilterTab) {
                Tab(
                    selected = selectedFilterTab == 0,
                    onClick = { selectedFilterTab = 0 },
                    text = { Text("الكل (${patients.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == 1,
                    onClick = { selectedFilterTab = 1 },
                    text = { Text("النشطون (${patients.size - suspendedCount})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedFilterTab == 2,
                    onClick = { selectedFilterTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("المعلقون ($suspendedCount)", fontWeight = FontWeight.Bold, color = if (suspendedCount > 0) MedicalRed else MaterialTheme.colorScheme.onSurface)
                            if (suspendedCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = CircleShape, color = MedicalRed) {
                                    Box(modifier = Modifier.size(7.dp))
                                }
                            }
                        }
                    }
                )
            }

            if (displayPatients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderShared,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "لا توجد نتائج مطابقة للبحث"
                            else if (selectedFilterTab == 2) "لا توجد أي حالات معلقة حالياً ✓"
                            else "لا يوجد مرضى مسجلين حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayPatients) { patient ->
                        PatientItemCard(
                            patient = patient,
                            onClick = { selectedPatientForSheet = patient },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${patient.phone}"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = {
                                PrintAndShareHelper.sendWhatsApp(
                                    context = context,
                                    phone = patient.phone,
                                    message = "مرحبًا ${patient.name}، نأمل أن تكون بخير وصحة. معكم مركز وسيم الطبي والتأهيلي."
                                )
                            },
                            onSettleInvoice = {
                                deliverInvoicePatient = patient
                            },
                            onPrintCard = {
                                cardPreviewPatient = patient
                            }
                        )
                    }
                }
            }
        }
    }

    // Patient Details Bottom Sheet
    selectedPatientForSheet?.let { pat ->
        ModalBottomSheet(
            onDismissRequest = { selectedPatientForSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            PatientDetailsSheet(
                patient = pat,
                viewModel = viewModel,
                onClose = { selectedPatientForSheet = null },
                onSettleClick = {
                    deliverInvoicePatient = pat
                    selectedPatientForSheet = null
                },
                onPrintCardClick = {
                    cardPreviewPatient = pat
                    selectedPatientForSheet = null
                }
            )
        }
    }
}

@Composable
fun PatientItemCard(
    patient: Patient,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onSettleInvoice: () -> Unit,
    onPrintCard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("patient_card_${patient.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (patient.balanceDue > 0) MedicalRed.copy(alpha = 0.15f)
                        else if (patient.gender == "أنثى") MedicalTeal.copy(alpha = 0.15f)
                        else MedicalBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (patient.balanceDue > 0) Icons.Default.Warning else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (patient.balanceDue > 0) MedicalRed
                                else if (patient.gender == "أنثى") MedicalTeal
                                else MedicalBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = patient.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ملف: ${patient.fileNumber} | ${patient.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badge: Suspended vs Active
                if (patient.balanceDue > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedicalRedLight
                    ) {
                        Text(
                            text = "معلّق: ${formatArabicCurrency(patient.balanceDue)}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MedicalRed
                            )
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedicalGreenLight
                    ) {
                        Text(
                            text = "نشط ✓",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MedicalGreen
                            )
                        )
                    }
                }
            }

            if (patient.diagnosis.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "التشخيص: ${patient.diagnosis}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If patient has invoice, offer direct settlement button right on card
                if (patient.balanceDue > 0) {
                    Button(
                        onClick = onSettleInvoice,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("سداد الفاتورة", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                } else {
                    OutlinedButton(
                        onClick = onPrintCard,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("كرت الملف", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MedicalBlue.copy(alpha = 0.1f))
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "اتصال", tint = MedicalBlue, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("واتساب", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

@Composable
fun PatientDetailsSheet(
    patient: Patient,
    viewModel: ClinicViewModel,
    onClose: () -> Unit,
    onSettleClick: () -> Unit,
    onPrintCardClick: () -> Unit
) {
    val context = LocalContext.current
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val doctor = doctors.find { it.id == patient.doctorId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "رقم الملف الطبي: ${patient.fileNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MedicalBlue
                )
            }
            if (patient.balanceDue > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedicalRedLight
                ) {
                    Text(
                        text = "معلّق: ${formatArabicCurrency(patient.balanceDue)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MedicalRed)
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedicalGreenLight
                ) {
                    Text(
                        text = "الحالة نشطة ✓",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MedicalGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        DetailRow(label = "رقم الهاتف", value = patient.phone)
        DetailRow(label = "الجنس / تاريخ الميلاد", value = "${patient.gender} | ${patient.dateOfBirth}")
        DetailRow(label = "العنوان", value = patient.address.ifBlank { "غير محدد" })
        DetailRow(label = "الحالة الاجتماعية / المهنة", value = "${patient.maritalStatus} | ${patient.profession.ifBlank { "غير محدد" }}")
        DetailRow(label = "الطبيب المشرف", value = doctor?.name ?: "الاستشاري المناوب")
        DetailRow(label = "التشخيص الطبي", value = patient.diagnosis.ifBlank { "لم يحدد" })
        DetailRow(label = "الشكوى الرئيسية", value = patient.complaint.ifBlank { "لا توجد" })
        DetailRow(label = "الملاحظات", value = patient.notes.ifBlank { "لا توجد ملاحظات" })

        Spacer(modifier = Modifier.height(16.dp))

        // Direct Settle Invoice button if suspended
        if (patient.balanceDue > 0) {
            Button(
                onClick = onSettleClick,
                colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسليم وسداد الفاتورة وفك التعليق", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    PrintAndShareHelper.sendWhatsApp(
                        context,
                        patient.phone,
                        "مرحبًا ${patient.name}، يرجى العلم بخصوص موعدكم وملفكم الطبي في مركز وسيم الطبي."
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("واتساب", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    val msg = "مرحبًا ${patient.name}، ملفكم رقم ${patient.fileNumber} جاهز بمركز وسيم الطبي. للاستفسار: 774486588."
                    PrintAndShareHelper.sendSMS(context, patient.phone, msg)
                },
                modifier = Modifier.weight(0.9f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("رسالة SMS", style = MaterialTheme.typography.bodySmall)
            }

            OutlinedButton(
                onClick = onPrintCardClick,
                modifier = Modifier.weight(0.9f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("كرت", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}
