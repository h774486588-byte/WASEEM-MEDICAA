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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.ExpenseVoucher
import com.example.data.models.Patient
import com.example.data.models.ReceiptVoucher
import com.example.ui.components.PrintAndShareHelper
import com.example.ui.components.StandardInvoicePreviewDialog
import com.example.ui.components.ThermalReceiptPreviewDialog
import com.example.ui.components.formatArabicCurrency
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberLight
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueLight
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenLight
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit,
    onOpenAddReceipt: () -> Unit,
    onOpenAddExpense: () -> Unit
) {
    val context = LocalContext.current
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val suspendedPatients by viewModel.suspendedPatients.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val totalReceipts by viewModel.totalReceiptsAmount.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpensesAmount.collectAsStateWithLifecycle()
    val totalOutstanding by viewModel.totalOutstandingBalances.collectAsStateWithLifecycle()
    val netBalance = totalReceipts - totalExpenses

    var selectedTab by remember { mutableIntStateOf(0) }

    // Direct preview states
    var previewThermalReceipt by remember { mutableStateOf<Pair<ReceiptVoucher, Patient>?>(null) }
    var previewStandardInvoice by remember { mutableStateOf<Pair<ReceiptVoucher, Patient>?>(null) }
    var deliverInvoicePatient by remember { mutableStateOf<Patient?>(null) }

    // Thermal Receipt Preview Dialog
    if (previewThermalReceipt != null) {
        val (rcpt, pat) = previewThermalReceipt!!
        ThermalReceiptPreviewDialog(
            receipt = rcpt,
            patient = pat,
            centerSettings = settings,
            onDismiss = { previewThermalReceipt = null }
        )
    }

    // Standard A4 Invoice Preview Dialog
    if (previewStandardInvoice != null) {
        val (rcpt, pat) = previewStandardInvoice!!
        StandardInvoicePreviewDialog(
            receipt = rcpt,
            patient = pat,
            centerSettings = settings,
            onDismiss = { previewStandardInvoice = null }
        )
    }

    // Deliver & Settle Dialog
    if (deliverInvoicePatient != null) {
        AddReceiptDialog(
            viewModel = viewModel,
            targetPatient = deliverInvoicePatient,
            onDismiss = { deliverInvoicePatient = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المعاملات المالية والفواتير", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAddReceipt) {
                        Icon(Icons.Default.Receipt, contentDescription = "سند قبض جديد", tint = MedicalGreen)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 2) onOpenAddExpense() else onOpenAddReceipt()
                },
                containerColor = if (selectedTab == 2) MedicalRed else MedicalGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("financial_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (selectedTab == 2) "سند صرف جديد" else "سند قبض وتسليم فاتورة"
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
            // Summary Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي المقبوضات", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatArabicCurrency(totalReceipts),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalGreen
                                )
                            )
                        }
                        Column {
                            Text("إجمالي المصروفات", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatArabicCurrency(totalExpenses),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalRed
                                )
                            )
                        }
                        Column {
                            Text("صافي الإيراد", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatArabicCurrency(netBalance),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (netBalance >= 0) MedicalGreen else MedicalRed
                                )
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MedicalRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الفواتير المعلقة (${suspendedPatients.size} حالات):", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            text = formatArabicCurrency(totalOutstanding),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MedicalRed)
                        )
                    }
                }
            }

            // 4 Specialized Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("سندات القبض (${receipts.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("الفواتير المعلقة (${suspendedPatients.size})", fontWeight = FontWeight.Bold)
                            if (suspendedPatients.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = CircleShape, color = MedicalRed) {
                                    Box(modifier = Modifier.size(8.dp))
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("سندات الصرف (${expenses.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("ميزان المراجعة والتقرير", fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Content
            when (selectedTab) {
                // 1. سندات القبض والفواتير المسددة
                0 -> {
                    if (receipts.isEmpty()) {
                        EmptyFinancialState("لا توجد سندات قبض مسجلة حالياً")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(receipts) { receipt ->
                                val pat = patients.find { it.id == receipt.patientId }
                                ReceiptCardWithActions(
                                    receipt = receipt,
                                    patient = pat,
                                    onThermalPrint = {
                                        if (pat != null) previewThermalReceipt = Pair(receipt, pat)
                                    },
                                    onStandardPrint = {
                                        if (pat != null) previewStandardInvoice = Pair(receipt, pat)
                                    },
                                    onWhatsApp = {
                                        if (pat != null) {
                                            val text = PrintAndShareHelper.generateThermalReceiptText(
                                                receipt = receipt,
                                                patient = pat,
                                                centerName = settings?.centerName ?: "مركز وسيم الطبي",
                                                phone = settings?.phone ?: "774486588"
                                            )
                                            PrintAndShareHelper.sendWhatsApp(context, pat.phone, text)
                                        }
                                    },
                                    onSMS = {
                                        if (pat != null) {
                                            val msg = "مرحبًا ${pat.name}\nتم تسجيل سند قبض رقم ${receipt.voucherNumber} بمبلغ ${formatArabicCurrency(receipt.amount)}. المتبقي: ${formatArabicCurrency(receipt.remainingBalance)}. مركز وسيم الطبي."
                                            PrintAndShareHelper.sendSMS(context, pat.phone, msg)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. الفواتير والحالات المعلقة
                1 -> {
                    if (suspendedPatients.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Receipt, contentDescription = null, tint = MedicalGreen, modifier = Modifier.size(54.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("لا توجد أي فواتير معلقة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("جميع حسابات المرضى مسددة بالكامل بحمد الله ✓", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(suspendedPatients) { patient ->
                                SuspendedInvoiceCard(
                                    patient = patient,
                                    onSettleClick = { deliverInvoicePatient = patient },
                                    onWhatsAppRemind = {
                                        val text = "مرحبًا ${patient.name}\nنود تذكيركم بوجود فاتورة معلقة بقيمة ${formatArabicCurrency(patient.balanceDue)} في مركز وسيم الطبي.\nيرجى التكرم بالسداد في زيارتكم القادمة أو عبر التحويل.\nشاكرين تعاونكم - مركز وسيم الطبي."
                                        PrintAndShareHelper.sendWhatsApp(context, patient.phone, text)
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. سندات الصرف والمصروفات
                2 -> {
                    if (expenses.isEmpty()) {
                        EmptyFinancialState("لا توجد سندات صرف مسجلة")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(expenses) { expense ->
                                ExpenseCardWithActions(
                                    expense = expense,
                                    onShare = {
                                        val text = "================================\nسند صرف: ${expense.voucherNumber}\nالمستفيد: ${expense.beneficiaryName} (${expense.beneficiaryType})\nالمبلغ: ${formatArabicCurrency(expense.amount)}\nالتاريخ: ${expense.date}\nالبيان: ${expense.statement}\n================================"
                                        PrintAndShareHelper.shareText(context, "سند صرف", text)
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. ميزان المراجعة والتقرير اليومي
                3 -> {
                    FinancialAuditOverview(
                        totalReceipts = totalReceipts,
                        totalExpenses = totalExpenses,
                        netBalance = netBalance,
                        totalOutstanding = totalOutstanding,
                        suspendedCount = suspendedPatients.size,
                        receipts = receipts,
                        expenses = expenses
                    )
                }
            }
        }
    }
}

@Composable
fun ReceiptCardWithActions(
    receipt: ReceiptVoucher,
    patient: Patient?,
    onThermalPrint: () -> Unit,
    onStandardPrint: () -> Unit,
    onWhatsApp: () -> Unit,
    onSMS: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = patient?.name ?: "مريض غير محدد",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "سند رقم: ${receipt.voucherNumber} | ${receipt.date} (${receipt.paymentMethod})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MedicalGreenLight) {
                    Text(
                        text = "+ ${formatArabicCurrency(receipt.amount)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MedicalGreen)
                    )
                }
            }

            if (receipt.statement.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "البيان: ${receipt.statement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الرصيد السابق: ${formatArabicCurrency(receipt.previousBalance)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "الرصيد المتبقي: ${formatArabicCurrency(receipt.remainingBalance)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = if (receipt.remainingBalance > 0) MedicalRed else MedicalGreen)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Action Buttons: Thermal, Standard, WhatsApp, SMS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onThermalPrint,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حراري 80mm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onStandardPrint,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عادي A4", fontSize = 12.sp)
                }

                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onSMS,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    Text("SMS", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SuspendedInvoiceCard(
    patient: Patient,
    onSettleClick: () -> Unit,
    onWhatsAppRemind: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MedicalRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MedicalRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(patient.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("ملف: ${patient.fileNumber} | هاتف: ${patient.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MedicalRedLight
                ) {
                    Text(
                        text = "معلّق: ${formatArabicCurrency(patient.balanceDue)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MedicalRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSettleClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.3f)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تسليم وسداد الفاتورة", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onWhatsAppRemind,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = MedicalGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تذكير واتس", color = MedicalGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExpenseCardWithActions(
    expense: ExpenseVoucher,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${expense.beneficiaryName} (${expense.beneficiaryType})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "سند صرف: ${expense.voucherNumber} | ${expense.date} - ${expense.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(8.dp), color = MedicalRedLight) {
                    Text(
                        text = "- ${formatArabicCurrency(expense.amount)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MedicalRed)
                    )
                }
            }
            if (expense.statement.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "البيان: ${expense.statement}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("طباعة / مشاركة سند الصرف", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun FinancialAuditOverview(
    totalReceipts: Double,
    totalExpenses: Double,
    netBalance: Double,
    totalOutstanding: Double,
    suspendedCount: Int,
    receipts: List<ReceiptVoucher>,
    expenses: List<ExpenseVoucher>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ميزان المراجعة والمركز المالي العام", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    AuditRow("إجمالي المقبوضات النقدية والتحويلية:", formatArabicCurrency(totalReceipts), MedicalGreen)
                    AuditRow("إجمالي المصروفات والتشغيل:", formatArabicCurrency(totalExpenses), MedicalRed)
                    AuditRow("صافي الربح / الحركة التشغيلية:", formatArabicCurrency(netBalance), if (netBalance >= 0) MedicalGreen else MedicalRed)
                    AuditRow("إجمالي الفواتير المعلقة في ذمة المرضى:", formatArabicCurrency(totalOutstanding), MedicalAmber)
                    AuditRow("عدد الحالات المعلقة حالياً:", "$suspendedCount حالة", MedicalRed)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("حركة طرق الدفع للمقبوضات", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    val cashTotal = receipts.filter { it.paymentMethod == "نقدًا" }.sumOf { it.amount }
                    val transferTotal = receipts.filter { it.paymentMethod == "تحويل" }.sumOf { it.amount }
                    val posTotal = receipts.filter { it.paymentMethod == "شبكة" }.sumOf { it.amount }

                    AuditRow("نقدًا (الصندوق الرئيسي):", formatArabicCurrency(cashTotal), MedicalBlue)
                    AuditRow("تحويل بنكي / محفظة:", formatArabicCurrency(transferTotal), MedicalTeal)
                    AuditRow("شبكة نقاط البيع:", formatArabicCurrency(posTotal), MedicalAmber)
                }
            }
        }
    }
}

@Composable
fun AuditRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun EmptyFinancialState(msg: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(msg, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
