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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberLight
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenLight
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val license by viewModel.license.collectAsStateWithLifecycle()
    val daysRemaining by viewModel.trialDaysRemaining.collectAsStateWithLifecycle()
    val isExpired by viewModel.isTrialExpired.collectAsStateWithLifecycle()

    var showActivateModal by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الترخيص والاشتراك", fontWeight = FontWeight.Bold) },
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
            // Status Banner
            item {
                val isPro = license?.licenseType == "PRO_PERPETUAL" || license?.licenseType == "ANNUAL"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPro) MedicalGreenLight else if (isExpired) MedicalRedLight else MedicalBlue.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPro) Icons.Default.VerifiedUser else if (isExpired) Icons.Default.Warning else Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isPro) MedicalGreen else if (isExpired) MedicalRed else MedicalBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isPro) "نسخة مرخصة ومفعلة بالكامل (PRO)"
                                else if (isExpired) "انتهت الفترة التجريبية للنظام"
                                else "الفترة التجريبية (متبقي $daysRemaining يومًا)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPro) MedicalGreen else if (isExpired) MedicalRed else MedicalBlue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isExpired) {
                                "للاستمرار في استخدام جميع خصائص النظام، يرجى التواصل مع إدارة نظام وسيم الطبي.\nرقم التواصل: 774486588 (وسيم الفرح)"
                            } else if (isPro) {
                                "تم تفعيل الترخيص الدائم لـ ${license?.centerName ?: "المركز"}. جميع الخصائص مفعلة بدون قيود."
                            } else {
                                "النظام يعمل بكامل طاقته في فترة التجربة المجانية (30 يوماً). بياناتك محفوظة ولن تُحذف أبداً."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // License Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("بيانات الترخيص المسجلة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                        Spacer(modifier = Modifier.height(12.dp))

                        DetailItem(label = "اسم العميل", value = license?.customerName ?: "مركز وسيم الطبي")
                        DetailItem(label = "اسم المركز", value = license?.centerName ?: "مركز وسيم للعلاج الطبيعي والتأهيل")
                        DetailItem(label = "رقم الترخيص", value = license?.licenseNumber ?: "WMP-TRIAL-884920")
                        DetailItem(label = "نوع الترخيص", value = when(license?.licenseType) {
                            "PRO_PERPETUAL" -> "ترخيص دائم PRO"
                            "ANNUAL" -> "ترخيص سنوي"
                            else -> "نسخة تجريبية 30 يوم"
                        })
                        DetailItem(label = "تاريخ البدء", value = license?.let { dateFormat.format(Date(it.startDate)) } ?: "2026/09/24")
                        DetailItem(label = "تاريخ الانتهاء", value = license?.let { dateFormat.format(Date(it.endDate)) } ?: "2026/10/24")
                        DetailItem(label = "معرف التثبيت", value = license?.installationId ?: "INST-AUTO-1001")

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showActivateModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("open_activate_license_btn")
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تفعيل أو ترقية الترخيص (PRO)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Developer Contact Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("الدعم الفني وتراخيص النظام", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مطور ومالك النظام: م. وسيم الفرح", style = MaterialTheme.typography.bodyMedium)
                        Text("رقم التواصل المباشر والواتساب: 772357240", style = MaterialTheme.typography.bodyMedium, color = MedicalBlue, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:772357240"))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اتصال")
                            }

                            Button(
                                onClick = {
                                    viewModel.sendWhatsApp(context, "772357240", "السلام عليكم م. وسيم، أرغب في تفعيل ترخيص نظام وسيم الطبي PRO.")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("واتساب")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showActivateModal) {
        ActivateLicenseModal(
            viewModel = viewModel,
            onDismiss = { showActivateModal = false }
        )
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun ActivateLicenseModal(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("مركز وسيم للعلاج الطبيعي والتأهيل") }
    var centerName by remember { mutableStateOf("نظام وسيم الطبي PRO") }
    var licenseNumber by remember { mutableStateOf("WMP-PRO-2026-998877") }
    var licenseType by remember { mutableStateOf("PRO_PERPETUAL") }
    var zeroSystemUponActivation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("تفعيل ترخيص النظام", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text("أدخل مفتاح الترخيص المعتمد من المطور م. وسيم الفرح (772357240)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("اسم العميل") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = centerName, onValueChange = { centerName = it }, label = { Text("اسم المركز") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("رقم الترخيص (Key)") }, modifier = Modifier.fillMaxWidth().testTag("license_key_input"))

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { zeroSystemUponActivation = !zeroSystemUponActivation },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = zeroSystemUponActivation,
                        onCheckedChange = { zeroSystemUponActivation = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تصفير البيانات التشغيلية لبدء تشغيل رسمي نظيف",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.activateLicense(customerName, centerName, licenseNumber, licenseType) { success, msg ->
                                if (success) {
                                    if (zeroSystemUponActivation) {
                                        viewModel.resetOperationalData {
                                            Toast.makeText(context, "تم تفعيل الترخيص وتصفير النظام بنجاح!", Toast.LENGTH_LONG).show()
                                            onDismiss()
                                        }
                                    } else {
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    }
                                } else {
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                        modifier = Modifier.testTag("submit_activate_btn")
                    ) {
                        Text("تفعيل الآن")
                    }
                }
            }
        }
    }
}
