package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.CenterSettings
import com.example.data.models.ClinicSession
import com.example.data.models.ExpenseVoucher
import com.example.data.models.Patient
import com.example.data.models.ReceiptVoucher
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrintAndShareHelper {

    // --- Format Yemeni Phone Number for WhatsApp & SMS ---
    fun formatPhoneNumberForWhatsApp(rawPhone: String): String {
        val digits = rawPhone.replace(Regex("[^0-9]"), "")
        return when {
            digits.startsWith("00967") -> digits.removePrefix("00")
            digits.startsWith("+967") -> digits.removePrefix("+")
            digits.startsWith("967") -> digits
            digits.startsWith("0") -> "967" + digits.substring(1)
            digits.length == 9 -> "967$digits"
            else -> "967$digits"
        }
    }

    // --- Launch WhatsApp ---
    fun sendWhatsApp(context: Context, phone: String, message: String) {
        try {
            val formatted = formatPhoneNumberForWhatsApp(phone)
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formatted&text=" + Uri.encode(message))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: General share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة الإشعار عبر:"))
        }
    }

    // --- Launch SMS ---
    fun sendSMS(context: Context, phone: String, message: String) {
        try {
            val uri = Uri.parse("smsto:${phone.trim()}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح تطبيق الرسائل النصية: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Share Text General ---
    fun shareText(context: Context, title: String, text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    // --- Native Android Printing via WebView ---
    fun printHtml(context: Context, htmlContent: String, jobName: String, isThermal: Boolean = false) {
        try {
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter(jobName)
                    val attributesBuilder = PrintAttributes.Builder()
                    if (isThermal) {
                        attributesBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_A6)
                    } else {
                        attributesBuilder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    }
                    printManager?.print(jobName, printAdapter, attributesBuilder.build())
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر إرسال أمر الطباعة: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // --- Generate Monospaced Thermal Receipt Text ---
    fun generateThermalReceiptText(
        receipt: ReceiptVoucher,
        patient: Patient,
        centerName: String = "مركز وسيم الطبي والتأهيلي",
        phone: String = "774486588"
    ): String {
        return buildString {
            appendLine("================================")
            appendLine("       $centerName       ")
            appendLine("   عيادات واستشارات وتأهيل حركي   ")
            appendLine("      هاتف / واتساب: $phone      ")
            appendLine("================================")
            appendLine("سند قبض وتسليم فاتورة: ${receipt.voucherNumber}")
            appendLine("التاريخ: ${receipt.date}")
            appendLine("--------------------------------")
            appendLine("المريض: ${patient.name}")
            appendLine("رقم الملف: ${patient.fileNumber}")
            appendLine("الهاتف: ${patient.phone}")
            appendLine("--------------------------------")
            appendLine("البيان: ${receipt.statement}")
            appendLine("طريقة الدفع: ${receipt.paymentMethod}")
            appendLine("--------------------------------")
            appendLine("الرصيد السابق:   ${"%,.0f".format(Locale.ENGLISH, receipt.previousBalance)} ر.ي")
            appendLine("المبلغ المقبوض:  ${"%,.0f".format(Locale.ENGLISH, receipt.amount)} ر.ي")
            appendLine("--------------------------------")
            appendLine("الرصيد المتبقي:  ${"%,.0f".format(Locale.ENGLISH, receipt.remainingBalance)} ر.ي")
            appendLine("================================")
            appendLine("حالة الحساب: " + if (receipt.remainingBalance <= 0) "مسدد بالكامل ✓" else "متبقي رصيد مستحق")
            appendLine("المستلم: ${receipt.createdBy}")
            appendLine("================================")
            appendLine("   شكراً لثقتكم ونتمنى لكم الشفاء   ")
            appendLine("    نظام وسيم الطبي PRO - برمجة وسيم   ")
            appendLine("================================")
        }
    }

    // --- Generate HTML for 80mm Thermal Receipt ---
    fun generateThermalReceiptHtml(
        receipt: ReceiptVoucher,
        patient: Patient,
        centerName: String = "مركز وسيم الطبي والتأهيلي",
        phone: String = "774486588"
    ): String {
        val amountStr = "%,.0f".format(Locale.ENGLISH, receipt.amount)
        val prevStr = "%,.0f".format(Locale.ENGLISH, receipt.previousBalance)
        val remStr = "%,.0f".format(Locale.ENGLISH, receipt.remainingBalance)

        return """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
            <meta charset="utf-8">
            <style>
              @page { size: 80mm auto; margin: 0; }
              body {
                font-family: 'Courier New', monospace, sans-serif;
                width: 78mm;
                margin: 0 auto;
                padding: 6mm 2mm;
                color: #000;
                font-size: 13px;
                line-height: 1.35;
              }
              .center { text-align: center; }
              .bold { font-weight: bold; }
              .title { font-size: 16px; margin-bottom: 2px; }
              .subtitle { font-size: 11px; margin-bottom: 4px; }
              .divider { border-top: 1px dashed #000; margin: 6px 0; }
              .double-divider { border-top: 2px solid #000; margin: 6px 0; }
              .row { display: flex; justify-content: space-between; margin: 3px 0; }
              .total-box {
                border: 1.5px solid #000;
                padding: 4px;
                margin: 6px 0;
                text-align: center;
                font-size: 15px;
                font-weight: bold;
              }
              .barcode {
                font-family: monospace;
                letter-spacing: 4px;
                font-size: 14px;
                margin-top: 6px;
              }
            </style>
            </head>
            <body>
              <div class="center bold title">$centerName</div>
              <div class="center subtitle">عيادات تخصصية - تأهيل حركي - علاج طبيعي</div>
              <div class="center subtitle">هاتف: $phone</div>
              <div class="double-divider"></div>
              
              <div class="center bold">سند قبض وتسليم فاتورة</div>
              <div class="center">رقم السند: ${receipt.voucherNumber}</div>
              <div class="center">التاريخ: ${receipt.date}</div>
              
              <div class="divider"></div>
              <div class="row"><span>المريض:</span><span class="bold">${patient.name}</span></div>
              <div class="row"><span>رقم الملف:</span><span>${patient.fileNumber}</span></div>
              <div class="row"><span>الهاتف:</span><span>${patient.phone}</span></div>
              
              <div class="divider"></div>
              <div class="row"><span>البيان:</span><span>${receipt.statement}</span></div>
              <div class="row"><span>طريقة الدفع:</span><span>${receipt.paymentMethod}</span></div>
              
              <div class="divider"></div>
              <div class="row"><span>الرصيد السابق:</span><span>$prevStr ر.ي</span></div>
              <div class="total-box">المبلغ المدفوع: $amountStr ر.ي</div>
              <div class="row"><span>الرصيد المتبقي:</span><span class="bold">$remStr ر.ي</span></div>
              
              <div class="double-divider"></div>
              <div class="center bold">${if (receipt.remainingBalance <= 0) "تم سداد كامل الفاتورة ✓" else "حساب معلق - متبقي مستحقات"}</div>
              <div class="center subtitle">المستلم: ${receipt.createdBy}</div>
              <div class="center barcode">*${receipt.voucherNumber}*</div>
              <div class="center subtitle" style="margin-top: 6px;">نتمنى لكم دوام الصحة والعافية</div>
              <div class="center subtitle">نظام وسيم الطبي PRO</div>
            </body>
            </html>
        """.trimIndent()
    }

    // --- Generate HTML for Standard A4 Formal Invoice ---
    fun generateStandardInvoiceHtml(
        receipt: ReceiptVoucher,
        patient: Patient,
        centerName: String = "مركز وسيم للعلاج الطبيعي والتأهيل",
        address: String = "صنعاء - شارع الستين الغربي",
        phone: String = "774486588"
    ): String {
        val amountStr = "%,.0f".format(Locale.ENGLISH, receipt.amount)
        val prevStr = "%,.0f".format(Locale.ENGLISH, receipt.previousBalance)
        val remStr = "%,.0f".format(Locale.ENGLISH, receipt.remainingBalance)

        return """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
            <meta charset="utf-8">
            <style>
              body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                margin: 20px;
                color: #1e293b;
                line-height: 1.5;
              }
              .header {
                display: flex;
                justify-content: space-between;
                align-items: center;
                border-bottom: 3px solid #0284c7;
                padding-bottom: 12px;
                margin-bottom: 20px;
              }
              .header-title { font-size: 24px; font-weight: bold; color: #0284c7; }
              .badge {
                background-color: #e0f2fe;
                color: #0369a1;
                padding: 4px 12px;
                border-radius: 6px;
                font-weight: bold;
                font-size: 14px;
              }
              .meta-table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 20px;
              }
              .meta-table td {
                padding: 8px 12px;
                border: 1px solid #e2e8f0;
                font-size: 14px;
              }
              .meta-table .label {
                background-color: #f8fafc;
                font-weight: bold;
                width: 20%;
                color: #475569;
              }
              .details-table {
                width: 100%;
                border-collapse: collapse;
                margin: 20px 0;
              }
              .details-table th {
                background-color: #0284c7;
                color: white;
                padding: 10px;
                text-align: right;
                font-size: 14px;
              }
              .details-table td {
                padding: 10px;
                border: 1px solid #cbd5e1;
                font-size: 14px;
              }
              .summary-box {
                margin-right: auto;
                width: 320px;
                border: 2px solid #0284c7;
                border-radius: 8px;
                overflow: hidden;
                margin-top: 15px;
              }
              .summary-row {
                display: flex;
                justify-content: space-between;
                padding: 8px 14px;
                border-bottom: 1px solid #e2e8f0;
              }
              .summary-total {
                background-color: #0284c7;
                color: white;
                font-weight: bold;
                font-size: 16px;
              }
              .footer {
                margin-top: 40px;
                display: flex;
                justify-content: space-between;
                padding-top: 20px;
                border-top: 1px solid #e2e8f0;
              }
              .sign-box {
                text-align: center;
                width: 200px;
              }
              .sign-line {
                margin-top: 40px;
                border-top: 1px dashed #64748b;
              }
            </style>
            </head>
            <body>
              <div class="header">
                <div>
                  <div class="header-title">$centerName</div>
                  <div style="font-size: 13px; color: #64748b;">$address | هاتف: $phone</div>
                </div>
                <div style="text-align: left;">
                  <div class="badge">فاتورة وسند قبض رسمي</div>
                  <div style="font-size: 13px; margin-top: 4px;">رقم: ${receipt.voucherNumber}</div>
                  <div style="font-size: 13px;">التاريخ: ${receipt.date}</div>
                </div>
              </div>

              <table class="meta-table">
                <tr>
                  <td class="label">اسم المريض:</td>
                  <td><strong>${patient.name}</strong></td>
                  <td class="label">رقم الملف:</td>
                  <td><strong>${patient.fileNumber}</strong></td>
                </tr>
                <tr>
                  <td class="label">رقم الهاتف:</td>
                  <td>${patient.phone}</td>
                  <td class="label">التشخيص الطبي:</td>
                  <td>${patient.diagnosis.ifBlank { "استشارة وفحص عام" }}</td>
                </tr>
              </table>

              <table class="details-table">
                <thead>
                  <tr>
                    <th>م</th>
                    <th>البيان / الخدمة الطبية</th>
                    <th>طريقة الدفع</th>
                    <th>المبلغ المستلم</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>1</td>
                    <td>${receipt.statement}</td>
                    <td>${receipt.paymentMethod}</td>
                    <td style="font-weight: bold; color: #0f766e;">$amountStr ر.ي</td>
                  </tr>
                </tbody>
              </table>

              <div class="summary-box">
                <div class="summary-row">
                  <span>الرصيد السابق للفاتورة:</span>
                  <span>$prevStr ر.ي</span>
                </div>
                <div class="summary-row summary-total">
                  <span>المبلغ المدفوع:</span>
                  <span>$amountStr ر.ي</span>
                </div>
                <div class="summary-row" style="background-color: #f8fafc; font-weight: bold;">
                  <span>الرصيد المتبقي بذمة المريض:</span>
                  <span style="color: ${if (receipt.remainingBalance > 0) "#dc2626" else "#16a34a"};">$remStr ر.ي</span>
                </div>
              </div>

              <div class="footer">
                <div class="sign-box">
                  <div>توقيع المستلم / المحاسب</div>
                  <div class="sign-line">${receipt.createdBy}</div>
                </div>
                <div class="sign-box">
                  <div>الختم الرسمي للمركز</div>
                  <div class="sign-line"></div>
                </div>
                <div class="sign-box">
                  <div>توقيع المريض / المرافق</div>
                  <div class="sign-line"></div>
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    // --- Generate Attendance Ticket Monospace Text ---
    fun generateAttendanceTicketText(
        session: ClinicSession,
        patient: Patient,
        remainingSessions: Int,
        centerName: String = "مركز وسيم الطبي والتأهيلي"
    ): String {
        return buildString {
            appendLine("================================")
            appendLine("       $centerName       ")
            appendLine("     تذكرة حضور جلسة علاجية     ")
            appendLine("================================")
            appendLine("المريض: ${patient.name}")
            appendLine("رقم الملف: ${patient.fileNumber}")
            appendLine("رقم الجلسة: ${session.sessionNumber}")
            appendLine("التاريخ: ${session.date} - ${session.time}")
            appendLine("حالة الحضور: حاضر ومثبت ✓")
            appendLine("--------------------------------")
            appendLine("تم خصم جلسة واحدة من الباقة")
            appendLine("الجلسات المتبقية في الباقة: $remainingSessions")
            appendLine("================================")
            appendLine("نتمنى لكم الشفاء العاجل والتحسن المستمر")
            appendLine("================================")
        }
    }
}

// -------------------------------------------------------------
// UI DIALOG: Thermal Receipt Preview Modal
// -------------------------------------------------------------
@Composable
fun ThermalReceiptPreviewDialog(
    receipt: ReceiptVoucher,
    patient: Patient,
    centerSettings: CenterSettings?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val centerName = centerSettings?.centerName ?: "مركز وسيم الطبي والتأهيلي"
    val phone = centerSettings?.phone ?: "774486588"

    val receiptText = PrintAndShareHelper.generateThermalReceiptText(receipt, patient, centerName, phone)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = MedicalGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معاينة الفاتورة الحرارية (80mm)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Realistic Thermal Paper Slip Container
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF9F9F6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E2D8), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = receiptText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val html = PrintAndShareHelper.generateThermalReceiptHtml(receipt, patient, centerName, phone)
                            PrintAndShareHelper.printHtml(context, html, "Thermal_Receipt_${receipt.voucherNumber}", isThermal = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة حرارية", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            PrintAndShareHelper.sendWhatsApp(
                                context = context,
                                phone = patient.phone,
                                message = receiptText
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = MedicalGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("واتساب", color = MedicalGreen, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            PrintAndShareHelper.sendSMS(context, patient.phone, receiptText)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إرسال SMS", style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(receiptText))
                            Toast.makeText(context, "تم نسخ نص الإيصال إلى الحافظة", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("نسخ النص", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UI DIALOG: Standard A4 Invoice Preview Modal
// -------------------------------------------------------------
@Composable
fun StandardInvoicePreviewDialog(
    receipt: ReceiptVoucher,
    patient: Patient,
    centerSettings: CenterSettings?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val centerName = centerSettings?.centerName ?: "مركز وسيم للعلاج الطبيعي والتأهيل"
    val address = centerSettings?.address ?: "صنعاء - شارع الستين الغربي"
    val phone = centerSettings?.phone ?: "774486588"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MedicalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الفاتورة الطبية الرسمية (A4)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(centerName, fontWeight = FontWeight.Bold, color = MedicalBlue)
                            Text(receipt.voucherNumber, fontWeight = FontWeight.Bold)
                        }
                        Text("التاريخ: ${receipt.date}", style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("المريض: ${patient.name} (${patient.fileNumber})", fontWeight = FontWeight.SemiBold)
                        Text("الهاتف: ${patient.phone}", style = MaterialTheme.typography.bodySmall)
                        Text("البيان: ${receipt.statement}", style = MaterialTheme.typography.bodySmall)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الرصيد السابق:", style = MaterialTheme.typography.bodySmall)
                            Text(formatArabicCurrency(receipt.previousBalance), style = MaterialTheme.typography.bodySmall)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المبلغ المقبوض:", fontWeight = FontWeight.Bold)
                            Text(formatArabicCurrency(receipt.amount), fontWeight = FontWeight.Bold, color = MedicalGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المتبقي بذمة المريض:", fontWeight = FontWeight.Bold)
                            Text(
                                formatArabicCurrency(receipt.remainingBalance),
                                fontWeight = FontWeight.Bold,
                                color = if (receipt.remainingBalance > 0) MedicalRed else MedicalGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val html = PrintAndShareHelper.generateStandardInvoiceHtml(receipt, patient, centerName, address, phone)
                        PrintAndShareHelper.printHtml(context, html, "Invoice_${receipt.voucherNumber}", isThermal = false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة الفاتورة A4 عبر الطابعة", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val msg = "مرحبًا ${patient.name}\nنرفق لكم تفاصيل الفاتورة الرسمية رقم ${receipt.voucherNumber}.\nالمبلغ المسدد: ${formatArabicCurrency(receipt.amount)}\nالمتبقي: ${formatArabicCurrency(receipt.remainingBalance)}\nنتمنى لكم دوام الصحة والعافية - $centerName"
                            PrintAndShareHelper.sendWhatsApp(context, patient.phone, msg)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إرسال بالواتساب", color = MedicalGreen, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val msg = "مرحبًا ${patient.name}\nتم تسجيل فاتورة رقم ${receipt.voucherNumber} بمبلغ ${formatArabicCurrency(receipt.amount)}، الرصيد المتبقي: ${formatArabicCurrency(receipt.remainingBalance)}. مركز وسيم الطبي."
                            PrintAndShareHelper.sendSMS(context, patient.phone, msg)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إرسال SMS")
                    }
                }
            }
        }
    }
}
