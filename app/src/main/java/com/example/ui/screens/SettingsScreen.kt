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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MedicalBlue
import com.example.ui.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    var centerName by remember { mutableStateOf(settings?.centerName ?: "مركز وسيم للعلاج الطبيعي والتأهيل") }
    var address by remember { mutableStateOf(settings?.address ?: "اليمن - صنعاء - شارع الستين الغربي") }
    var phone by remember { mutableStateOf(settings?.phone ?: "774486588") }
    var whatsapp by remember { mutableStateOf(settings?.whatsapp ?: "774486588") }
    var currency by remember { mutableStateOf(settings?.currency ?: "ر.ي") }

    val timeFormat = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.ENGLISH)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات وسجل العمليات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
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
                    text = { Text("بيانات المركز والنظام", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("سجل التدقيق والرقابة (${auditLogs.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("إعدادات وهوية المركز الطبي", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(value = centerName, onValueChange = { centerName = it }, label = { Text("اسم المركز") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("عنوان المركز") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("هاتف الاستقبال") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("رقم واتساب المعتمد") }, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("رمز العملة") }, modifier = Modifier.fillMaxWidth())

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        settings?.let { s ->
                                            viewModel.updateSettings(
                                                s.copy(
                                                    centerName = centerName,
                                                    address = address,
                                                    phone = phone,
                                                    whatsapp = whatsapp,
                                                    currency = currency
                                                )
                                            ) { success, msg ->
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Roles Overview
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("المستخدمون والصلاحيات", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("الأدوار: مدير، استقبال، طبيب، معالج، محاسب", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("• المدير: كامل الصلاحيات، الإعدادات، التدقيق، التراخيص", style = MaterialTheme.typography.bodySmall)
                                Text("• الاستقبال: تسجيل المرضى، المواعيد، الجلسات، سندات القبض", style = MaterialTheme.typography.bodySmall)
                                Text("• الطبيب والمعالج: معاينة الحالات، تسجيل حضور الجلسات، الملاحظات", style = MaterialTheme.typography.bodySmall)
                                Text("• المحاسب: سندات القبض والصرف، الرواتب، الاستقطاعات، التقارير المالية", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    // About Developer
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("حول النظام والمطور", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("نظام وسيم الطبي PRO - WASEEM MEDICAL PRO", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MedicalBlue)
                                Text("إدارة العيادات والمراكز الطبية والتأهيلية", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("تطوير المهندس: وسيم الفرح", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text("رقم التواصل والدعم: 774486588", style = MaterialTheme.typography.bodySmall)
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
                    items(auditLogs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.action, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(log.user, style = MaterialTheme.typography.labelSmall, color = MedicalBlue)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(log.details, style = MaterialTheme.typography.bodySmall)
                                if (log.previousData != null || log.newData != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            if (log.previousData != null) Text("قبل: ${log.previousData}", style = MaterialTheme.typography.labelSmall)
                                            if (log.newData != null) Text("بعد: ${log.newData}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(timeFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}
