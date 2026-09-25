package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalTeal

data class MenuItemData(
    val id: String,
    val number: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit
) {
    val menuItems = listOf(
        MenuItemData("dashboard", 1, "الرئيسية", "لوحة الإحصائيات والاختصارات", Icons.Default.Home, MedicalBlue),
        MenuItemData("patients", 2, "المرضى والملفات", "دليل المرضى وسجلات الحالات والتشخيص", Icons.Default.Group, MedicalBlue),
        MenuItemData("appointments", 3, "المواعيد", "جدول الحجوزات والمواعيد الطبية", Icons.Default.DateRange, MedicalTeal),
        MenuItemData("sessions", 4, "الجلسات والحالات", "حضور الجلسات وخصم الباقات التلقائي", Icons.Default.FitnessCenter, MedicalAmber),
        MenuItemData("doctors", 5, "الأطباء والمعالجون", "طاقم الاستشاريين ونسب العمولات", Icons.Default.MedicalServices, MedicalBlue),
        MenuItemData("departments", 6, "الأقسام والخدمات", "دليل التخصصات وقائمة أسعار الخدمات", Icons.Default.MedicalServices, MedicalTeal),
        MenuItemData("packages", 7, "الباقات والجلسات", "باقات العلاج الطبيعي والتأهيل الحركي", Icons.Default.PriceCheck, MedicalGreen),
        MenuItemData("financial", 8, "الإدارة المالية", "سندات القبض وحسابات وأرصدة المرضى", Icons.Default.Receipt, MedicalGreen),
        MenuItemData("financial", 9, "المصروفات وسندات الصرف", "مصروفات التشغيل وسندات الصرف", Icons.Default.TrendingDown, MedicalRed),
        MenuItemData("payroll", 10, "الرواتب والاستقطاعات", "إدارة رواتب الموظفين والسلف والخصميات", Icons.Default.Badge, MedicalAmber),
        MenuItemData("inventory", 11, "المخزون والمستلزمات", "مراقبة مستلزمات وأجهزة المركز الطبي", Icons.Default.Inventory, MedicalBlue),
        MenuItemData("reports", 12, "التقارير الشاملة", "التقارير المالية والطبية ومعدل الجلسات", Icons.Default.Assessment, MedicalTeal),
        MenuItemData("messages", 13, "الرسائل والإشعارات", "مركز قوالب الرسائل ورسائل WhatsApp", Icons.Default.Message, MedicalGreen),
        MenuItemData("backup", 14, "النسخ الاحتياطي", "النسخ المحلي والمزامنة مع Google Drive", Icons.Default.CloudSync, MedicalBlue),
        MenuItemData("settings", 15, "الإعدادات وسجل العمليات", "بيانات المركز، سجل الرقابة والتدقيق", Icons.Default.Settings, MedicalBlue),
        MenuItemData("license", 16, "الترخيص والاشتراك", "بيانات الفترة التجريبية وتفعيل PRO", Icons.Default.Key, MedicalAmber),
        MenuItemData("settings", 17, "حول النظام والمطور", "وسيم الفرح (774486588)", Icons.Default.Info, MedicalTeal)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قائمة النظام والأقسام", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(item.id) }
                        .testTag("menu_item_${item.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = item.color.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(22.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Text(
                                        text = "${item.number}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
