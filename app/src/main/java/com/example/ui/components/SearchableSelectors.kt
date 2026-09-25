package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Department
import com.example.data.models.Doctor
import com.example.data.models.MedicalService
import com.example.data.models.Patient
import com.example.data.models.Therapist
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberLight
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueLight
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenLight
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.theme.MedicalTeal

// =========================================================================
// 1. SEARCHABLE PATIENT PICKER (منتقي المرضى مع البحث المباشر والفوري)
// لا يعتمد على DropdownMenu المنبثق لتفادي أي مشاكل طبقات داخل الحوارات
// =========================================================================
@Composable
fun SearchablePatientPicker(
    patients: List<Patient>,
    selectedPatient: Patient?,
    onPatientSelected: (Patient) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "المريض *"
) {
    var isExpanded by remember { mutableStateOf(selectedPatient == null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPatients = remember(patients, searchQuery) {
        if (searchQuery.isBlank()) patients
        else {
            val q = searchQuery.trim().lowercase()
            patients.filter {
                it.name.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.fileNumber.lowercase().contains(q) ||
                        it.diagnosis.lowercase().contains(q)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (selectedPatient != null && !isExpanded) {
            // Selected Patient Card Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedPatient.balanceDue > 0) MedicalRedLight.copy(alpha = 0.5f) else MedicalBlueLight.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (selectedPatient.balanceDue > 0) MedicalRed else MedicalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedPatient.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "ملف: ${selectedPatient.fileNumber} | ${selectedPatient.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedPatient.balanceDue > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MedicalRed
                            ) {
                                Text(
                                    text = "مستحق: ${formatArabicCurrency(selectedPatient.balanceDue)}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        OutlinedButton(
                            onClick = { isExpanded = true },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("تغيير", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        } else {
            // Search Input and Live List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("🔎 ابحث باسم المريض أو هاتفه أو رقم الملف...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (filteredPatients.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا يوجد مريض مطابق للبحث الحالي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 190.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredPatients) { pat ->
                                    val isSelected = pat.id == selectedPatient?.id
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onPatientSelected(pat)
                                                isExpanded = false
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MedicalBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                }
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = pat.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                        if (System.currentTimeMillis() - pat.createdAt < 24 * 60 * 60 * 1000) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = MedicalGreenLight
                                                            ) {
                                                                Text(
                                                                    text = "جديد",
                                                                    color = MedicalGreen,
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "ملف: ${pat.fileNumber} | ${pat.phone} ${if (pat.diagnosis.isNotBlank()) " | " + pat.diagnosis else ""}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            if (pat.balanceDue > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MedicalRedLight
                                                ) {
                                                    Text(
                                                        text = formatArabicCurrency(pat.balanceDue),
                                                        color = MedicalRed,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedPatient != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "إلغاء وإبقاء (${selectedPatient.name})",
                                style = MaterialTheme.typography.labelSmall.copy(color = MedicalBlue, fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .clickable { isExpanded = false }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. SEARCHABLE / INLINE DEPARTMENT SELECTOR
// =========================================================================
@Composable
fun SearchableDepartmentPicker(
    departments: List<Department>,
    selectedDepartment: Department?,
    onDepartmentSelected: (Department) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "القسم الطبي *"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedDepartment?.name ?: "اختر القسم الطبي",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    departments.forEach { dept ->
                        val isSelected = dept.id == selectedDepartment?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDepartmentSelected(dept)
                                    expanded = false
                                }
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MedicalBlue.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dept.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MedicalBlue else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. SEARCHABLE / INLINE MEDICAL SERVICE SELECTOR (الخدمات مع عرض السعر)
// =========================================================================
@Composable
fun SearchableServicePicker(
    services: List<MedicalService>,
    selectedService: MedicalService?,
    onServiceSelected: (MedicalService) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "الخدمة الطبية المطلوبة *"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedService?.name ?: "اختر الخدمة الطبية",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (selectedService != null) {
                        Text(
                            text = "السعر: ${formatArabicCurrency(selectedService.price)} - المدة: ${selectedService.durationMinutes} دقيقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicalTeal
                        )
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    if (services.isEmpty()) {
                        Text(
                            text = "لا توجد خدمات مضافة في هذا القسم حالياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        services.forEach { srv ->
                            val isSelected = srv.id == selectedService?.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onServiceSelected(srv)
                                        expanded = false
                                    }
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MedicalTeal.copy(alpha = 0.15f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = srv.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MedicalTeal else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = "${srv.durationMinutes} دقيقة",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = formatArabicCurrency(srv.price),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MedicalTeal)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. SEARCHABLE / INLINE DOCTOR SELECTOR
// =========================================================================
@Composable
fun SearchableDoctorPicker(
    doctors: List<Doctor>,
    selectedDoctor: Doctor?,
    onDoctorSelected: (Doctor) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "الطبيب المشرف / المعالج *"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedDoctor?.name ?: "اختر الطبيب المشرف",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (selectedDoctor != null) {
                        Text(
                            text = "${selectedDoctor.specialization} - ${selectedDoctor.workingHours}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicalBlue
                        )
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    if (doctors.isEmpty()) {
                        Text(
                            text = "لا يوجد أطباء مسجلون في هذا القسم حالياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        doctors.forEach { doc ->
                            val isSelected = doc.id == selectedDoctor?.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDoctorSelected(doc)
                                        expanded = false
                                    }
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MedicalBlue.copy(alpha = 0.15f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = doc.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MedicalBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = doc.specialization,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MedicalBlue, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. SEARCHABLE / INLINE SPECIALIST / THERAPIST SELECTOR (المختص / المعالج)
// =========================================================================
@Composable
fun SearchableTherapistPicker(
    therapists: List<Therapist>,
    selectedTherapist: Therapist?,
    onTherapistSelected: (Therapist) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "المختص / أخصائي العلاج *"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedTherapist?.name ?: "اختر المختص / المعالج الطبي",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (selectedTherapist != null) {
                        Text(
                            text = "${selectedTherapist.specialization} - ${selectedTherapist.workingHours}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MedicalAmber
                        )
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        if (expanded) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    if (therapists.isEmpty()) {
                        Text(
                            text = "لا يوجد أخصائيون مسجلون في هذا القسم حالياً",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        therapists.forEach { ther ->
                            val isSelected = ther.id == selectedTherapist?.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTherapistSelected(ther)
                                        expanded = false
                                    }
                                    .padding(vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MedicalAmber.copy(alpha = 0.15f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = ther.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MedicalAmber else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = ther.specialization,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MedicalAmber, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
