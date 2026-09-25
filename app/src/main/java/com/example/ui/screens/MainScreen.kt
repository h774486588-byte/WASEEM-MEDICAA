package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.R
import com.example.ui.components.ManageBranchesDialog
import com.example.ui.components.ManageUsersDialog
import com.example.ui.components.SystemZeroingConfirmDialog
import com.example.ui.components.UserAccountDialog
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.ClinicViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(viewModel: ClinicViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val activeBranch by viewModel.activeBranch.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Force RTL layout direction for Arabic native UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        var currentRoute by remember { mutableStateOf("splash") }
        var previousRoute by remember { mutableStateOf("dashboard") }

        // Modal Dialogs state
        var showAddPatientDialog by remember { mutableStateOf(false) }
        var showAddAppointmentDialog by remember { mutableStateOf(false) }
        var showAddReceiptDialog by remember { mutableStateOf(false) }
        var showAddExpenseDialog by remember { mutableStateOf(false) }
        var showActivateLicenseModal by remember { mutableStateOf(false) }

        // Management Dialogs
        var showUserAccountDialog by remember { mutableStateOf(false) }
        var showManageBranchesDialog by remember { mutableStateOf(false) }
        var showManageUsersDialog by remember { mutableStateOf(false) }
        var showSystemZeroingDialog by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var showUpdateSystemDialog by remember { mutableStateOf(false) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        if (currentRoute == "splash") {
            SplashScreen(
                onSplashFinished = {
                    currentRoute = if (currentUser == null) "login" else "dashboard"
                }
            )
        } else if (currentRoute == "login" || currentUser == null) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { currentRoute = "dashboard" }
            )
        } else {
            // Guarded Safe Navigation lambda enforcing Role Permissions
            val safeNavigateTo: (String) -> Unit = { route ->
                val allowed = when (route) {
                    "financial" -> viewModel.canAccess { it.permFinancial }
                    "payroll" -> viewModel.canAccess { it.permPayroll }
                    "reports" -> viewModel.canAccess { it.permReports }
                    "settings" -> viewModel.canAccess { it.permSettings }
                    "backup" -> viewModel.canAccess { it.permBackup }
                    "patients" -> viewModel.canAccess { it.permPatients }
                    "appointments" -> viewModel.canAccess { it.permAppointments }
                    "sessions", "packages" -> viewModel.canAccess { it.permSessions }
                    "doctors" -> viewModel.canAccess { it.permDoctors }
                    "departments" -> viewModel.canAccess { it.permDepartments }
                    "inventory" -> viewModel.canAccess { it.permInventory }
                    "messages", "notifications" -> viewModel.canAccess { it.permSettings }
                    "license" -> viewModel.canAccess { it.permLicense }
                    else -> true
                }
                if (allowed) {
                    previousRoute = currentRoute
                    currentRoute = route
                    scope.launch { drawerState.close() }
                } else {
                    Toast.makeText(context, "عذراً، هذه الخاصية مقيدة بحسب صلاحيات حسابك. يرجى مراجعة إدارة المركز.", Toast.LENGTH_SHORT).show()
                }
            }

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .width(310.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Drawer Header: Logo, System Name, Current User & Branch
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MedicalBlue
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(52.dp).clip(CircleShape),
                                            color = Color.White
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_waseem_logo),
                                                contentDescription = "شعار وسيم الطبي",
                                                modifier = Modifier.fillMaxSize().padding(4.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "نظام وسيم الطبي PRO",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                            )
                                            Text(
                                                text = settings?.centerName ?: "مركز وسيم للعلاج الطبيعي والتأهيل",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f)),
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // User Profile Pill inside Drawer
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                scope.launch { drawerState.close() }
                                                showUserAccountDialog = true
                                            },
                                        color = Color.White.copy(alpha = 0.18f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = currentUser?.fullName ?: "مستخدم",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                                )
                                                Text(
                                                    text = "فرع: ${activeBranch?.name ?: "كافة الفروع"}",
                                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f))
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 1. حسابي
                            DrawerItem("👤 حسابي وإعدادات الدخول", Icons.Default.AccountCircle, false) {
                                scope.launch { drawerState.close() }
                                showUserAccountDialog = true
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 2. العمليات التشغيلية الأساسية
                            DrawerItem("🏠 الرئيسية", Icons.Default.Home, currentRoute == "dashboard") { safeNavigateTo("dashboard") }
                            DrawerItem("👥 المرضى والملفات الطبية", Icons.Default.Group, currentRoute == "patients") { safeNavigateTo("patients") }
                            DrawerItem("📅 المواعيد والحجوزات", Icons.Default.DateRange, currentRoute == "appointments") { safeNavigateTo("appointments") }
                            DrawerItem("🩺 الجلسات وحالات التأهيل", Icons.Default.FitnessCenter, currentRoute == "sessions") { safeNavigateTo("sessions") }
                            DrawerItem("👨‍⚕️ الأطباء والمعالجون", Icons.Default.MedicalServices, currentRoute == "doctors") { safeNavigateTo("doctors") }
                            DrawerItem("🏥 الأقسام والخدمات الطبية", Icons.Default.MedicalServices, currentRoute == "departments") { safeNavigateTo("departments") }
                            DrawerItem("🏷️ الباقات والبرامج", Icons.Default.PriceCheck, currentRoute == "packages") { safeNavigateTo("packages") }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. المالية والمخزون
                            DrawerItem("💰 الإدارة المالية (فواتير، قبض، صرف)", Icons.Default.Receipt, currentRoute == "financial") { safeNavigateTo("financial") }
                            DrawerItem("💵 الرواتب والاستقطاعات", Icons.Default.Payments, currentRoute == "payroll") { safeNavigateTo("payroll") }
                            DrawerItem("📦 المخزون والمستلزمات", Icons.Default.Inventory, currentRoute == "inventory") { safeNavigateTo("inventory") }
                            DrawerItem("📊 التقارير الشاملة والمحاسبية", Icons.Default.Assessment, currentRoute == "reports") { safeNavigateTo("reports") }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 4. الإدارة والفروع والمستخدمين (للمدير والمالك)
                            DrawerItem("🏢 بيانات المركز والفروع", Icons.Default.Business, false) {
                                if (viewModel.canAccess { it.permSettings }) {
                                    scope.launch { drawerState.close() }
                                    showManageBranchesDialog = true
                                } else {
                                    Toast.makeText(context, "عذراً، لا تملك صلاحية إدارة الفروع.", Toast.LENGTH_SHORT).show()
                                }
                            }
                            DrawerItem("👥 المستخدمون والصلاحيات", Icons.Default.Security, false) {
                                if (viewModel.canAccess { it.permUsers }) {
                                    scope.launch { drawerState.close() }
                                    showManageUsersDialog = true
                                } else {
                                    Toast.makeText(context, "عذراً، لا تملك صلاحية إدارة المستخدمين.", Toast.LENGTH_SHORT).show()
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 5. المراسلات والإشعارات والطباعة والنسخ
                            DrawerItem("💬 قوالب الرسائل (WhatsApp & SMS)", Icons.Default.Message, currentRoute == "messages") { safeNavigateTo("messages") }
                            DrawerItem("🔔 إعدادات وتنبيهات الإشعارات", Icons.Default.Notifications, currentRoute == "notifications") { safeNavigateTo("notifications") }
                            DrawerItem("🖨️ إعدادات الطباعة (A4 & 80mm)", Icons.Default.Print, currentRoute == "settings") { safeNavigateTo("settings") }
                            DrawerItem("💾 النسخ الاحتياطي والمزامنة", Icons.Default.CloudSync, currentRoute == "backup") { safeNavigateTo("backup") }
                            DrawerItem("⚙️ إعدادات النظام والرقابة", Icons.Default.Settings, currentRoute == "settings") { safeNavigateTo("settings") }
                            DrawerItem("🔐 الترخيص والتفعيل (PRO)", Icons.Default.Key, currentRoute == "license") { safeNavigateTo("license") }
                            DrawerItem("🔄 تحديث النظام", Icons.Default.Sync, false) {
                                scope.launch { drawerState.close() }
                                showUpdateSystemDialog = true
                            }
                            DrawerItem("ℹ️ حول النظام والدعم الفني", Icons.Default.Info, false) {
                                scope.launch { drawerState.close() }
                                showAboutDialog = true
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // Logout
                            DrawerItem("🚪 تسجيل الخروج", Icons.Default.ExitToApp, false) {
                                scope.launch { drawerState.close() }
                                viewModel.logout()
                                currentRoute = "login"
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            ) {
                val isBottomBarVisible = currentRoute in listOf("dashboard", "patients", "appointments", "sessions", "more")

                Scaffold(
                    bottomBar = {
                        if (isBottomBarVisible) {
                            NavigationBar(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("bottom_nav_bar"),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "dashboard",
                                    onClick = { currentRoute = "dashboard" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("الرئيسية", style = MaterialTheme.typography.labelSmall) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "patients",
                                    onClick = { safeNavigateTo("patients") },
                                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                                    label = { Text("المرضى", style = MaterialTheme.typography.labelSmall) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "appointments",
                                    onClick = { safeNavigateTo("appointments") },
                                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                                    label = { Text("المواعيد", style = MaterialTheme.typography.labelSmall) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "sessions",
                                    onClick = { safeNavigateTo("sessions") },
                                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
                                    label = { Text("الجلسات", style = MaterialTheme.typography.labelSmall) }
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "more",
                                    onClick = { currentRoute = "more" },
                                    icon = { Icon(Icons.Default.Menu, contentDescription = null) },
                                    label = { Text("المزيد", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentRoute) {
                            "dashboard" -> DashboardScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onNavigateToScreen = safeNavigateTo,
                                onOpenAddPatient = { showAddPatientDialog = true },
                                onOpenAddAppointment = { showAddAppointmentDialog = true },
                                onOpenAddReceipt = { showAddReceiptDialog = true },
                                onOpenAddExpense = { showAddExpenseDialog = true },
                                onOpenLicense = { currentRoute = "license" },
                                onLogout = {
                                    viewModel.logout()
                                    currentRoute = "login"
                                }
                            )
                            "patients" -> PatientsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" },
                                onOpenAddPatient = { showAddPatientDialog = true }
                            )
                            "appointments" -> AppointmentsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" },
                                onOpenAddAppointment = { showAddAppointmentDialog = true }
                            )
                            "sessions" -> SessionsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "packages" -> PackagesScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "financial" -> FinancialScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" },
                                onOpenAddReceipt = { showAddReceiptDialog = true },
                                onOpenAddExpense = { showAddExpenseDialog = true }
                            )
                            "payroll" -> PayrollScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "doctors" -> DoctorsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "departments" -> DepartmentsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "inventory" -> InventoryScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "reports" -> ReportsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "messages" -> MessagesScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "notifications" -> NotificationsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "backup" -> BackupScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "license" -> LicenseScreen(
                                viewModel = viewModel,
                                onBack = { currentRoute = "dashboard" }
                            )
                            "more" -> MoreScreen(onNavigate = safeNavigateTo)
                            else -> DashboardScreen(
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onNavigateToScreen = safeNavigateTo,
                                onOpenAddPatient = { showAddPatientDialog = true },
                                onOpenAddAppointment = { showAddAppointmentDialog = true },
                                onOpenAddReceipt = { showAddReceiptDialog = true },
                                onOpenAddExpense = { showAddExpenseDialog = true },
                                onOpenLicense = { currentRoute = "license" },
                                onLogout = {
                                    viewModel.logout()
                                    currentRoute = "login"
                                }
                            )
                        }
                    }
                }
            }
        }

        // Global Modals
        if (showAddPatientDialog) {
            AddPatientDialog(viewModel = viewModel, onDismiss = { showAddPatientDialog = false })
        }
        if (showAddAppointmentDialog) {
            AddAppointmentDialog(viewModel = viewModel, onDismiss = { showAddAppointmentDialog = false })
        }
        if (showAddReceiptDialog) {
            AddReceiptDialog(viewModel = viewModel, onDismiss = { showAddReceiptDialog = false })
        }
        if (showAddExpenseDialog) {
            AddExpenseDialog(viewModel = viewModel, onDismiss = { showAddExpenseDialog = false })
        }
        if (showActivateLicenseModal) {
            ActivateLicenseModal(viewModel = viewModel, onDismiss = { showActivateLicenseModal = false })
        }

        // Management Modals
        if (showUserAccountDialog) {
            UserAccountDialog(
                viewModel = viewModel,
                onDismiss = { showUserAccountDialog = false },
                onLogout = {
                    viewModel.logout()
                    currentRoute = "login"
                }
            )
        }
        if (showManageBranchesDialog) {
            ManageBranchesDialog(viewModel = viewModel, onDismiss = { showManageBranchesDialog = false })
        }
        if (showManageUsersDialog) {
            ManageUsersDialog(viewModel = viewModel, onDismiss = { showManageUsersDialog = false })
        }
        if (showSystemZeroingDialog) {
            SystemZeroingConfirmDialog(viewModel = viewModel, onDismiss = { showSystemZeroingDialog = false })
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Text("ℹ️ حول نظام وسيم الطبي PRO", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text("نظام وسيم الطبي PRO - الإصدار ${BuildConfig.VERSION_NAME} PRO", fontWeight = FontWeight.Bold, color = MedicalBlue)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("نظام إداري وطبي متكامل لإدارة العيادات والمراكز الطبية ومراكز التأهيل والعلاج الطبيعي.")
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("حقوق التطوير والملكية الفكرية: م. وسيم الفرح", fontWeight = FontWeight.SemiBold)
                        Text("رقم التواصل المباشر والواتساب: 772357240", color = MedicalBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("الميزات الرئيسية: ربط الفروع، الصلاحيات الدقيقة، الفواتير والسندات، الطباعة الحرارية والعادية، والنسخ الاحتياطي السحابي والمحلي.")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) { Text("إغلاق") }
                }
            )
        }

        // Update System Dialog
        if (showUpdateSystemDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateSystemDialog = false },
                title = {
                    Text("🔄 التحقق من تحديثات النظام", fontWeight = FontWeight.Bold)
                },
                text = {
                    Column {
                        Text("الإصدار الحالي المثبت: ${BuildConfig.VERSION_NAME} PRO.")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("النظام يعمل بكفاءة وأمان كاملين ومتصل بقاعدة البيانات المحلية السريعة.", color = MedicalGreen)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showUpdateSystemDialog = false }) { Text("تم") }
                }
            )
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)) },
        icon = { Icon(icon, contentDescription = null, tint = if (selected) MedicalBlue else MaterialTheme.colorScheme.onSurfaceVariant) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MedicalBlue.copy(alpha = 0.12f),
            selectedTextColor = MedicalBlue
        )
    )
}
