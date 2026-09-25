package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.formatArabicCurrency
import com.example.ui.components.formatNumber
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val totalReceipts by viewModel.totalReceiptsAmount.collectAsStateWithLifecycle()
    val totalExpenses by viewModel.totalExpensesAmount.collectAsStateWithLifecycle()
    val totalOutstanding by viewModel.totalOutstandingBalances.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableIntStateOf(0) }
    val periods = listOf("اليوم", "هذا الأسبوع", "هذا الشهر", "هذه السنة", "الكل")

    val attendedSessions = sessions.count { it.status == "حضر" }
    val totalSessionsCount = sessions.size
    val attendanceRate = if (totalSessionsCount > 0) (attendedSessions.toFloat() / totalSessionsCount.toFloat()) else 0f

    val netProfit = totalReceipts - totalExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التقارير المالية والطبية الشاملة", fontWeight = FontWeight.Bold) },
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
            // Period Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    periods.forEachIndexed { index, period ->
                        FilterChip(
                            selected = selectedPeriod == index,
                            onClick = { selectedPeriod = index },
                            label = { Text(period, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "الملخص المالي (${periods[selectedPeriod]})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("إجمالي المقبوضات", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = formatArabicCurrency(totalReceipts),
                                    style = MaterialTheme.typography.titleMedium.copy(color = MedicalGreen, fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text("إجمالي المصروفات", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = formatArabicCurrency(totalExpenses),
                                    style = MaterialTheme.typography.titleMedium.copy(color = MedicalRed, fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("صافي الحركة المالية", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = formatArabicCurrency(netProfit),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (netProfit >= 0) MedicalGreen else MedicalRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Column {
                                Text("الديون المستحقة", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = formatArabicCurrency(totalOutstanding),
                                    style = MaterialTheme.typography.titleMedium.copy(color = MedicalAmber, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Clinical & Sessions Performance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "مؤشرات الجلسات والتأهيل الحركي",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نسبة حضور الجلسات", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${(attendanceRate * 100).toInt()}% ($attendedSessions من $totalSessionsCount)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MedicalGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { attendanceRate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp),
                            color = MedicalGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ReportMetricBox(title = "إجمالي المرضى", value = patients.size.toString(), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            ReportMetricBox(title = "المواعيد المسجلة", value = appointments.size.toString(), modifier = Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            ReportMetricBox(title = "الباقات النشطة", value = packages.count { it.status == "نشطة" }.toString(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportMetricBox(title: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MedicalBlue))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}
