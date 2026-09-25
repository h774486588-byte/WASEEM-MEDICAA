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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.Employee
import com.example.data.models.SalaryDeduction
import com.example.ui.components.formatArabicCurrency
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val deductions by viewModel.deductions.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var showAddDeductionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الرواتب والاستقطاعات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddEmployeeDialog = true else showAddDeductionDialog = true
                },
                containerColor = MedicalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("payroll_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (selectedTab == 0) "إضافة موظف" else "تسجيل استقطاع"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("الموظفون والرواتب (${employees.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("سجل الاستقطاعات (${deductions.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(employees) { emp ->
                        val empDeductions = deductions.filter { it.employeeId == emp.id }.sumOf { it.amount }
                        val netSalary = emp.basicSalary + emp.allowances + emp.incentives - empDeductions

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = emp.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(
                                            text = "${emp.jobTitle} - ${emp.department}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                        Text(
                                            text = "صافي: ${formatArabicCurrency(netSalary)}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("أساسي: ${formatArabicCurrency(emp.basicSalary)}", style = MaterialTheme.typography.bodySmall)
                                    Text("بدلات: ${formatArabicCurrency(emp.allowances)}", style = MaterialTheme.typography.bodySmall)
                                    Text("استقطاعات: ${formatArabicCurrency(empDeductions)}", style = MaterialTheme.typography.bodySmall, color = MedicalRed)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(deductions) { ded ->
                        val emp = employees.find { it.id == ded.employeeId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = emp?.name ?: "موظف", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${ded.deductionType} | السبب: ${ded.reason} (${ded.date})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "- ${formatArabicCurrency(ded.amount)}",
                                    style = MaterialTheme.typography.titleSmall.copy(color = MedicalRed, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEmployeeDialog) {
        AddEmployeeDialogModal(
            viewModel = viewModel,
            onDismiss = { showAddEmployeeDialog = false }
        )
    }

    if (showAddDeductionDialog) {
        AddDeductionDialogModal(
            viewModel = viewModel,
            onDismiss = { showAddDeductionDialog = false }
        )
    }
}

@Composable
fun AddEmployeeDialogModal(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("العلاج الطبيعي") }
    var phone by remember { mutableStateOf("") }
    var basicSalaryStr by remember { mutableStateOf("80000") }
    var allowancesStr by remember { mutableStateOf("10000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("إضافة موظف جديد", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم الموظف *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = jobTitle, onValueChange = { jobTitle = it }, label = { Text("المسمى الوظيفي *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = basicSalaryStr, onValueChange = { basicSalaryStr = it.filter { c -> c.isDigit() } }, label = { Text("الراتب الأساسي (ر.ي) *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val salary = basicSalaryStr.toDoubleOrNull() ?: 0.0
                            if (name.isBlank() || salary <= 0) {
                                Toast.makeText(context, "يرجى تعبئة الحقول المطلوبة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.addEmployee(
                                Employee(
                                    name = name,
                                    jobTitle = jobTitle,
                                    department = department,
                                    phone = phone,
                                    basicSalary = salary,
                                    allowances = allowancesStr.toDoubleOrNull() ?: 0.0,
                                    hireDate = viewModel.todayDateStr
                                )
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                    ) { Text("حفظ الموظف") }
                }
            }
        }
    }
}

@Composable
fun AddDeductionDialogModal(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    var selectedEmployee by remember { mutableStateOf<Employee?>(employees.firstOrNull()) }
    var deductionType by remember { mutableStateOf("استقطاع سلفة") }
    var amountStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val types = listOf("استقطاع غياب", "استقطاع سلفة", "استقطاع تأخير", "استقطاع إداري", "خصم آخر")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("تسجيل استقطاع جديد", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = selectedEmployee?.name ?: "اختر موظف",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("الموظف") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("مبلغ الاستقطاع (ر.ي) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الاستقطاع / ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val emp = selectedEmployee
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            if (emp == null || amt <= 0) {
                                Toast.makeText(context, "يرجى تعبئة الحقول المطلوبة", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.addDeduction(
                                SalaryDeduction(
                                    employeeId = emp.id,
                                    deductionType = deductionType,
                                    amount = amt,
                                    reason = reason,
                                    date = viewModel.todayDateStr
                                )
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                if (success) onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
                    ) { Text("تسجيل الخصم") }
                }
            }
        }
    }
}
