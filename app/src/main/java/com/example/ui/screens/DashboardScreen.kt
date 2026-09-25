package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.models.ClinicSession
import com.example.data.models.Patient
import com.example.ui.components.BranchSwitcherDialog
import com.example.ui.components.ManageBranchesDialog
import com.example.ui.components.ManageUsersDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrialBanner
import com.example.ui.components.UserAccountDialog
import com.example.ui.components.formatArabicCurrency
import com.example.ui.components.formatNumber
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalAmberLight
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalBlueLight
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalGreenLight
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.ClinicViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ClinicViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToScreen: (String) -> Unit,
    onOpenAddPatient: () -> Unit,
    onOpenAddAppointment: () -> Unit,
    onOpenAddReceipt: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenLicense: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // Live Date & Time state
    var liveDateTimeStr by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("EEEE d MMMM yyyy • hh:mm:ss a", Locale("ar"))
        while (true) {
            liveDateTimeStr = sdf.format(Date())
            delay(1000)
        }
    }

    // ViewModel State Flows
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeBranch by viewModel.activeBranch.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val suspendedPatients by viewModel.suspendedPatients.collectAsStateWithLifecycle()
    val suspendedCount by viewModel.suspendedCount.collectAsStateWithLifecycle()
    val totalSuspendedDue by viewModel.totalSuspendedDue.collectAsStateWithLifecycle()

    val todayAppointments by viewModel.todayAppointments.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaySessions.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val trialDays by viewModel.trialDaysRemaining.collectAsStateWithLifecycle()
    val isExpired by viewModel.isTrialExpired.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val universalResults by viewModel.universalSearchResults.collectAsStateWithLifecycle()

    // Dialog Trigger States
    var showUserAccountDialog by remember { mutableStateOf(false) }
    var showBranchSwitcherDialog by remember { mutableStateOf(false) }
    var showManageBranchesDialog by remember { mutableStateOf(false) }
    var deliverInvoicePatient by remember { mutableStateOf<Patient?>(null) }
    var attendanceTargetSession by remember { mutableStateOf<ClinicSession?>(null) }

    // Direct Receipt / Deliver Invoice Dialog
    if (deliverInvoicePatient != null) {
        AddReceiptDialog(
            viewModel = viewModel,
            targetPatient = deliverInvoicePatient,
            onDismiss = { deliverInvoicePatient = null }
        )
    }

    // Direct Attendance Dialog
    if (attendanceTargetSession != null) {
        SessionAttendanceDialog(
            session = attendanceTargetSession!!,
            viewModel = viewModel,
            onDismiss = { attendanceTargetSession = null }
        )
    }

    // User Account & Profile Dialog
    if (showUserAccountDialog) {
        UserAccountDialog(
            viewModel = viewModel,
            onDismiss = { showUserAccountDialog = false },
            onLogout = onLogout
        )
    }

    // Branch Switcher Dialog
    if (showBranchSwitcherDialog) {
        BranchSwitcherDialog(
            viewModel = viewModel,
            onOpenManageBranches = { showManageBranchesDialog = true },
            onDismiss = { showBranchSwitcherDialog = false }
        )
    }

    // Manage Branches Dialog
    if (showManageBranchesDialog) {
        ManageBranchesDialog(
            viewModel = viewModel,
            onDismiss = { showManageBranchesDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // =========================================================================
        // 1. 🔷 الترويسة العلوية (Header Bar with Modern Blue Gradient)
        // =========================================================================
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MedicalBlueDark,
                                MedicalBlue,
                                Color(0xFF0284C7)
                            )
                        )
                    )
                    .padding(top = 12.dp, bottom = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Top Row: Hamburger Menu (Left in RTL is Start) - Center Info - Controls (Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left / Start: Hamburger Menu Button
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .testTag("menu_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "القائمة الجانبية",
                                tint = Color.White
                            )
                        }

                        // Central Content: Logo, System Name, Branch, Live Clock
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape),
                                    color = Color.White
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_waseem_logo),
                                        contentDescription = "شعار وسيم",
                                        modifier = Modifier.fillMaxSize().padding(2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "«نظام وسيم الطبي PRO»",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            // Branch Selector Chip
                            val branchTitle = activeBranch?.name ?: "🌐 جميع الفروع (إجمالي مجمع)"
                            Surface(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { showBranchSwitcherDialog = true },
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${settings?.centerName ?: "مركز وسيم"} - $branchTitle",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "تبديل الفرع",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Right Side Controls: Notifications & User Profile Avatar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 🔔 Notification Bell with live unread badge
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = MedicalRed) {
                                            Text(unreadCount.toString(), color = Color.White)
                                        }
                                    }
                                }
                            ) {
                                IconButton(
                                    onClick = { onNavigateToScreen("notifications") },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "التنبيهات",
                                        tint = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // 👤 User Avatar / Account Profile
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { showUserAccountDialog = true }
                                    .testTag("user_profile_avatar"),
                                color = Color.White
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = (currentUser?.fullName?.take(1) ?: "و"),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MedicalBlue
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Date and Time continuous ticker bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.Black.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = if (liveDateTimeStr.isNotBlank()) liveDateTimeStr else "اليوم والوقت المباشر لنظام وسيم الطبي",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White.copy(alpha = 0.95f),
                                letterSpacing = 0.3.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 2. 🔍 شريط البحث الشامل (Universal Search Bar)
        // =========================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "🔎 ابحث في النظام (مريض، موعد، جلسة، موظف، خدمة، حساب...)",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MedicalBlue)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("universal_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedicalBlue,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        )
                    )

                    // Interactive Live Universal Search Results Container
                    AnimatedVisibility(visible = searchQuery.isNotBlank() && !universalResults.isEmpty) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, start = 4.dp, end = 4.dp)
                        ) {
                            Text(
                                text = "نتائج البحث الفوري (${universalResults.totalCount} نتائج):",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // 1. Patients Matches
                            universalResults.matchingPatients.forEach { patient ->
                                SearchResultRow(
                                    title = patient.name,
                                    subtitle = "ملف: ${patient.fileNumber} • هاتف: ${patient.phone} • تشخيص: ${patient.diagnosis}",
                                    icon = Icons.Default.Group,
                                    color = MedicalBlue,
                                    onClick = { onNavigateToScreen("patients") }
                                )
                            }

                            // 2. Appointments Matches
                            universalResults.matchingAppointments.forEach { appt ->
                                SearchResultRow(
                                    title = "موعد رقم ${appt.appointmentNumber}",
                                    subtitle = "التاريخ: ${appt.date} • الوقت: ${appt.timeSlot} • الحالة: ${appt.status}",
                                    icon = Icons.Default.DateRange,
                                    color = MedicalTeal,
                                    onClick = { onNavigateToScreen("appointments") }
                                )
                            }

                            // 3. Sessions Matches
                            universalResults.matchingSessions.forEach { sess ->
                                SearchResultRow(
                                    title = "جلسة رقم ${sess.sessionNumber}",
                                    subtitle = "التاريخ: ${sess.date} • الحالة: ${sess.status}",
                                    icon = Icons.Default.FitnessCenter,
                                    color = MedicalAmber,
                                    onClick = { onNavigateToScreen("sessions") }
                                )
                            }

                            // 4. Staff Matches
                            universalResults.matchingDoctors.forEach { doc ->
                                SearchResultRow(
                                    title = "د. ${doc.name}",
                                    subtitle = "التخصص: ${doc.specialization} • الهاتف: ${doc.phone}",
                                    icon = Icons.Default.MedicalServices,
                                    color = MedicalGreen,
                                    onClick = { onNavigateToScreen("doctors") }
                                )
                            }

                            // 5. Receipts Matches
                            universalResults.matchingReceipts.forEach { rcpt ->
                                SearchResultRow(
                                    title = "سند قبض: ${rcpt.voucherNumber} (${formatArabicCurrency(rcpt.amount)})",
                                    subtitle = "البيان: ${rcpt.statement} • التاريخ: ${rcpt.date}",
                                    icon = Icons.Default.Receipt,
                                    color = MedicalBlueDark,
                                    onClick = { onNavigateToScreen("financial") }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Trial License Warning Banner (if active)
        item {
            TrialBanner(
                daysRemaining = trialDays,
                isExpired = isExpired,
                onActivateClick = onOpenLicense
            )
        }

        // =========================================================================
        // 3. 📊 بطاقات الإحصائيات الحية (Real-Time Analytics Cards)
        // =========================================================================
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: إجمالي المرضى
                    StatCard(
                        title = "👥 إجمالي المرضى",
                        value = formatNumber(patients.size),
                        icon = Icons.Default.Group,
                        accentColor = MedicalBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("patients") }
                    )

                    // Card 2: مواعيد اليوم
                    StatCard(
                        title = "📅 مواعيد اليوم",
                        value = formatNumber(todayAppointments.size),
                        icon = Icons.Default.DateRange,
                        accentColor = MedicalTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("appointments") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: جلسات اليوم
                    StatCard(
                        title = "🩺 جلسات اليوم",
                        value = "${todaySessions.count { it.status == "حضر" }} / ${todaySessions.size}",
                        icon = Icons.Default.FitnessCenter,
                        accentColor = MedicalAmber,
                        subValue = "محضورة ومجدولة",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("sessions") }
                    )

                    // Card 4: المستحقات المالية
                    StatCard(
                        title = "💰 المستحقات المالية",
                        value = formatArabicCurrency(totalSuspendedDue),
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = MedicalRed,
                        subValue = "$suspendedCount حالة معلّقة",
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("financial") }
                    )
                }
            }
        }

        // =========================================================================
        // 4. 🗂️ شبكة أزرار الوظائف والأقسام الرئيسية (Main Action Grid - 9 Units)
        // =========================================================================
        item {
            SectionHeader(title = "الأقسام والوظائف الرئيسية")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Row 1: المرضى • المواعيد • الجلسات
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MainGridCard(
                        title = "المرضى والملفات",
                        icon = Icons.Default.Group,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("patients") }
                    )
                    MainGridCard(
                        title = "المواعيد",
                        icon = Icons.Default.DateRange,
                        color = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("appointments") }
                    )
                    MainGridCard(
                        title = "الجلسات والحالات",
                        icon = Icons.Default.FitnessCenter,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("sessions") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: الأطباء • الأقسام • الإدارة المالية
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MainGridCard(
                        title = "الأطباء والموظفون",
                        icon = Icons.Default.MedicalServices,
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("doctors") }
                    )
                    MainGridCard(
                        title = "الأقسام والخدمات",
                        icon = Icons.Default.MedicalServices,
                        color = Color(0xFF4F46E5),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("departments") }
                    )
                    MainGridCard(
                        title = "الإدارة المالية",
                        icon = Icons.Default.Receipt,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("financial") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: المخزون • التقارير • الإشعارات والرسائل
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MainGridCard(
                        title = "المخزون",
                        icon = Icons.Default.Inventory,
                        color = Color(0xFF9333EA),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("inventory") }
                    )
                    MainGridCard(
                        title = "التقارير",
                        icon = Icons.Default.Assessment,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("reports") }
                    )
                    MainGridCard(
                        title = "الإشعارات والرسائل",
                        icon = Icons.Default.Message,
                        color = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToScreen("messages") }
                    )
                }
            }
        }

        // =========================================================================
        // 5. شريط الإجراءات السريعة (Quick Actions Bar)
        // =========================================================================
        item {
            SectionHeader(title = "الإجراءات والعمليات السريعة")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    QuickActionChip(
                        label = "تسجيل حالة جديدة",
                        icon = Icons.Default.PersonAdd,
                        color = MedicalBlue,
                        onClick = onOpenAddPatient
                    )
                }
                item {
                    QuickActionChip(
                        label = "سند قبض / تسليم فاتورة",
                        icon = Icons.Default.Receipt,
                        color = MedicalGreen,
                        onClick = onOpenAddReceipt
                    )
                }
                item {
                    QuickActionChip(
                        label = "تسجيل حضور جلسة",
                        icon = Icons.Default.CheckCircle,
                        color = MedicalTeal,
                        onClick = {
                            val nextSession = todaySessions.firstOrNull { it.status != "حضر" }
                            if (nextSession != null) {
                                attendanceTargetSession = nextSession
                            } else {
                                onNavigateToScreen("sessions")
                            }
                        }
                    )
                }
                item {
                    QuickActionChip(
                        label = "سند صرف مصروفات",
                        icon = Icons.Default.TrendingDown,
                        color = MedicalRed,
                        onClick = onOpenAddExpense
                    )
                }
                item {
                    QuickActionChip(
                        label = "حجز موعد جديد",
                        icon = Icons.Default.EventNote,
                        color = MedicalTeal,
                        onClick = onOpenAddAppointment
                    )
                }
                item {
                    QuickActionChip(
                        label = "المعاملات المالية",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = MedicalBlue,
                        onClick = { onNavigateToScreen("financial") }
                    )
                }
            }
        }

        // =========================================================================
        // 6. قسم الحالات المعلقة المستقل (المرضى الذين عليهم فواتير معلقة)
        // =========================================================================
        item {
            SectionHeader(
                title = "الحالات المعلقة بسبب فواتير مستحقة",
                actionText = "كافة الفواتير",
                onActionClick = { onNavigateToScreen("financial") }
            )
            if (suspendedPatients.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalGreenLight.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedicalGreen)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "رائع! لا توجد حالياً أي فواتير معلقة على المرضى.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MedicalGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suspendedPatients.take(6)) { patient ->
                        SuspendedPatientCard(
                            patient = patient,
                            onDeliverInvoice = { deliverInvoicePatient = patient },
                            onRemindWhatsApp = {
                                viewModel.sendWhatsApp(
                                    context = context,
                                    phone = patient.phone,
                                    messageText = "مرحبًا ${patient.name}\nنود تذكيركم بوجود فاتورة معلقة بقيمة ${formatArabicCurrency(patient.balanceDue)} لدى ${settings?.centerName ?: "المركز"}.\nيرجى التفضل بمراجعة الإدارة المالية."
                                )
                            }
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 7. جلسات اليوم المباشرة (Daily Sessions Schedule)
        // =========================================================================
        item {
            SectionHeader(
                title = "جلسات اليوم المجدولة",
                actionText = "عرض الكل (${todaySessions.size})",
                onActionClick = { onNavigateToScreen("sessions") }
            )
            if (todaySessions.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "لا توجد جلسات مسجلة لليوم حتى الآن.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    todaySessions.take(4).forEach { session ->
                        val patient = patients.firstOrNull { it.id == session.patientId }
                        TodaySessionItemCard(
                            session = session,
                            patient = patient,
                            onTakeAttendance = { attendanceTargetSession = session }
                        )
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS FOR DASHBOARD ---

@Composable
private fun MainGridCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(98.dp)
            .clickable(onClick = onClick)
            .testTag("main_grid_${title}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun SuspendedPatientCard(
    patient: Patient,
    onDeliverInvoice: () -> Unit,
    onRemindWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .testTag("suspended_patient_card_${patient.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = "معلّق - عليه فاتورة")
                Surface(
                    color = MedicalRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = formatArabicCurrency(patient.balanceDue),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MedicalRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = patient.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "ملف: ${patient.fileNumber} • ${patient.phone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onDeliverInvoice,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("سداد الفاتورة", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = onRemindWhatsApp,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Text("واتساب", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun TodaySessionItemCard(
    session: ClinicSession,
    patient: Patient?,
    onTakeAttendance: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp).clip(CircleShape),
                color = if (session.status == "حضر") MedicalGreen.copy(alpha = 0.15f) else MedicalAmber.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (session.status == "حضر") Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = if (session.status == "حضر") MedicalGreen else MedicalAmber
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patient?.name ?: "مريض غير معروف",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (patient?.balanceDue ?: 0.0 > 0.0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MedicalRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "معلّق",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(color = MedicalRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                Text(
                    text = "جلسة رقم: ${session.sessionNumber} • الوقت: ${session.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (session.status != "حضر") {
                Button(
                    onClick = onTakeAttendance,
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("حضور", style = MaterialTheme.typography.labelSmall)
                }
            } else {
                Surface(
                    color = MedicalGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "تم الحضور",
                        color = MedicalGreen,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
