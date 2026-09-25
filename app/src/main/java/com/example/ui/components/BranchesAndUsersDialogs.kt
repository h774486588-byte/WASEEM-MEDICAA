package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.AppUser
import com.example.data.models.Branch
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.ClinicViewModel

// --- 1. USER ACCOUNT & PROFILE DIALOG ---
@Composable
fun UserAccountDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()

    var showChangePasswordSection by remember { mutableStateOf(false) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var passError by remember { mutableStateOf<String?>(null) }

    val userBranchName = if (currentUser?.branchId != null) {
        branches.firstOrNull { it.id == currentUser?.branchId }?.name ?: "الفرع الرئيسي"
    } else {
        "🌐 كافة الفروع (إشراف كامل)"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = MedicalBlue.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MedicalBlue)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = currentUser?.fullName ?: "حساب المستخدم",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "اسم المستخدم: ${currentUser?.username ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الدور الوظيفي:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = when (currentUser?.role) {
                                    "SUPER_ADMIN" -> "👑 المالك والمطور الرئيسي"
                                    "ADMIN" -> "مدير عام المركز"
                                    "RECEPTIONIST" -> "موظف استقبال"
                                    "ACCOUNTANT" -> "محاسب مالي"
                                    else -> currentUser?.role ?: "مستخدم"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MedicalBlue)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الفرع المرتبط:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = userBranchName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Password Change Section
                if (!showChangePasswordSection) {
                    OutlinedButton(
                        onClick = { showChangePasswordSection = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تغيير كلمة المرور")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "تغيير كلمة المرور الخاصة بك",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (passError != null) {
                                Text(passError!!, color = MedicalRed, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = oldPass,
                                onValueChange = { oldPass = it },
                                label = { Text("كلمة المرور الحالية") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { newPass = it },
                                label = { Text("كلمة المرور الجديدة") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = confirmPass,
                                onValueChange = { confirmPass = it },
                                label = { Text("تأكيد كلمة المرور") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showChangePasswordSection = false }) {
                                    Text("إلغاء")
                                }
                                Button(
                                    onClick = {
                                        if (newPass.length < 4) {
                                            passError = "كلمة المرور يجب أن تكون 4 خانات على الأقل"
                                            return@Button
                                        }
                                        if (newPass != confirmPass) {
                                            passError = "كلمتا المرور غير متطابقتين"
                                            return@Button
                                        }
                                        currentUser?.let { user ->
                                            viewModel.changePassword(user.id, oldPass, newPass) { success, msg ->
                                                if (success) {
                                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                                    showChangePasswordSection = false
                                                } else {
                                                    passError = msg
                                                }
                                            }
                                        }
                                    }
                                ) {
                                    Text("حفظ التغيير")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Button(
                    onClick = {
                        onDismiss()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسجيل الخروج من النظام")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

// --- 2. BRANCH SWITCHER DIALOG ---
@Composable
fun BranchSwitcherDialog(
    viewModel: ClinicViewModel,
    onOpenManageBranches: () -> Unit,
    onDismiss: () -> Unit
) {
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()
    val activeBranch by viewModel.activeBranch.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val canManageBranches = currentUser?.isSystemOwner == true || currentUser?.role in listOf("SUPER_ADMIN", "ADMIN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = MedicalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🏢 الانتقال بين الفروع", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "حدد الفرع المطلوب لعرض ومتابعة عملياته وسجلاته:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Option: All Branches (for Super Admin & Admins)
                if (canManageBranches) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.switchBranch(null)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeBranch == null) MedicalBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌐 جميع الفروع (إجمالي مجمع)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (activeBranch == null) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeBranch == null) MedicalBlue else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (activeBranch == null) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MedicalBlue)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Branches List
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(branches) { branch ->
                        val isSelected = activeBranch?.id == branch.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.switchBranch(branch)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MedicalBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = branch.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MedicalBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        if (branch.isMainBranch) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = MedicalGreen.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "رئيسي",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(color = MedicalGreen, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                    if (branch.address.isNotBlank()) {
                                        Text(
                                            text = "${branch.city} - ${branch.address}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MedicalBlue)
                                }
                            }
                        }
                    }
                }

                if (canManageBranches) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenManageBranches()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إدارة الفروع وإضافة فرع جديد")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

// --- 3. MANAGE BRANCHES DIALOG ---
@Composable
fun ManageBranchesDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()

    var showAddForm by remember { mutableStateOf(false) }
    var branchName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("صنعاء") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("772357240") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, contentDescription = null, tint = MedicalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("🏢 إدارة الفروع والمؤسسة", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!showAddForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "قائمة فروع المركز (${branches.size}):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { showAddForm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة فرع", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(branches) { b ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(b.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("${b.city} - ${b.address} (${b.phone})", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (!b.isMainBranch) {
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteBranch(b.id) {
                                                    Toast.makeText(context, "تم حذف الفرع", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MedicalRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("إضافة فرع جديد للمركز:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("اسم الفرع (مثال: فرع تعز - الحوبان)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("المدينة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان بالتفصيل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم هاتف التواصل للفرع") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddForm = false }) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (branchName.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة اسم الفرع", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.addBranch(branchName, city, address, phone) {
                                    Toast.makeText(context, "تمت إضافة الفرع بنجاح", Toast.LENGTH_SHORT).show()
                                    showAddForm = false
                                    branchName = ""
                                    address = ""
                                }
                            }
                        ) {
                            Text("حفظ الفرع")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

// --- 4. MANAGE USERS & GRANULAR PERMISSIONS DIALOG ---
@Composable
fun ManageUsersDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val branches by viewModel.allBranches.collectAsStateWithLifecycle()

    var showAddUserForm by remember { mutableStateOf(false) }
    var resetPasswordTargetUser by remember { mutableStateOf<AppUser?>(null) }

    // User Form fields
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("1234") }
    var selectedRole by remember { mutableStateOf("RECEPTIONIST") }
    var selectedBranchId by remember { mutableStateOf<Long?>(1) }

    // Granular Permissions Checkboxes
    var permPatients by remember { mutableStateOf(true) }
    var permAppointments by remember { mutableStateOf(true) }
    var permSessions by remember { mutableStateOf(true) }
    var permDoctors by remember { mutableStateOf(false) }
    var permDepartments by remember { mutableStateOf(false) }
    var permFinancial by remember { mutableStateOf(false) }
    var permPayroll by remember { mutableStateOf(false) }
    var permInventory by remember { mutableStateOf(false) }
    var permReports by remember { mutableStateOf(false) }
    var permSettings by remember { mutableStateOf(false) }
    var permUsers by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = MedicalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("👥 المستخدمون والصلاحيات", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!showAddUserForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المستخدمون المسجلون (${users.size}):",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = {
                                fullName = ""
                                username = ""
                                password = "1234"
                                selectedRole = "RECEPTIONIST"
                                permPatients = true
                                permAppointments = true
                                permSessions = true
                                permFinancial = false
                                permReports = false
                                permSettings = false
                                showAddUserForm = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة مستخدم", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(users) { u ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(u.fullName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                if (u.isSystemOwner) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(color = MedicalAmber.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                                        Text("المالك", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall.copy(color = MedicalAmber, fontWeight = FontWeight.Bold))
                                                    }
                                                }
                                            }
                                            Text(
                                                "اسم الدخول: ${u.username} • الدور: ${u.role}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row {
                                            // Reset Password Button
                                            IconButton(
                                                onClick = { resetPasswordTargetUser = u }
                                            ) {
                                                Icon(Icons.Default.LockReset, contentDescription = "تغيير كلمة المرور", tint = MedicalTeal)
                                            }

                                            // Delete user (cannot delete owner)
                                            if (!u.isSystemOwner) {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteUser(u.id) {
                                                            Toast.makeText(context, "تم حذف المستخدم", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MedicalRed)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "الصلاحيات: " + listOfNotNull(
                                            if (u.permPatients) "مرضى" else null,
                                            if (u.permAppointments) "مواعيد" else null,
                                            if (u.permSessions) "جلسات" else null,
                                            if (u.permFinancial) "مالية" else null,
                                            if (u.permReports) "تقارير" else null,
                                            if (u.permSettings) "إعدادات" else null
                                        ).joinToString(" • "),
                                        style = MaterialTheme.typography.labelSmall.copy(color = MedicalBlue)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Create New User Form
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        item {
                            Text("إضافة مستخدم جديد وتحديد صلاحياته:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("الاسم الكامل (مثال: محمد أحمد — استقبال)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("اسم المستخدم للدخول (مثال: mohammed)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("كلمة المرور") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("الدور الأساسي:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = selectedRole == "RECEPTIONIST",
                                    onClick = {
                                        selectedRole = "RECEPTIONIST"
                                        permPatients = true
                                        permAppointments = true
                                        permSessions = true
                                        permFinancial = false
                                        permReports = false
                                        permSettings = false
                                    },
                                    label = { Text("استقبال") }
                                )
                                FilterChip(
                                    selected = selectedRole == "ACCOUNTANT",
                                    onClick = {
                                        selectedRole = "ACCOUNTANT"
                                        permPatients = true
                                        permAppointments = false
                                        permSessions = false
                                        permFinancial = true
                                        permReports = true
                                        permSettings = false
                                    },
                                    label = { Text("محاسب") }
                                )
                                FilterChip(
                                    selected = selectedRole == "ADMIN",
                                    onClick = {
                                        selectedRole = "ADMIN"
                                        permPatients = true
                                        permAppointments = true
                                        permSessions = true
                                        permFinancial = true
                                        permReports = true
                                        permSettings = true
                                    },
                                    label = { Text("مشرف/مدير") }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("الصلاحيات التفصيلية:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                            PermissionCheckboxItem("المرضى والملفات الطبية", permPatients) { permPatients = it }
                            PermissionCheckboxItem("المواعيد والحجوزات", permAppointments) { permAppointments = it }
                            PermissionCheckboxItem("الجلسات وحالات التأهيل", permSessions) { permSessions = it }
                            PermissionCheckboxItem("الإدارة المالية (القبض، الصرف، الفواتير)", permFinancial) { permFinancial = it }
                            PermissionCheckboxItem("التقارير الشاملة وميزان المراجعة", permReports) { permReports = it }
                            PermissionCheckboxItem("إعدادات النظام والنسخ الاحتياطي", permSettings) { permSettings = it }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showAddUserForm = false }) {
                                    Text("إلغاء")
                                }
                                Button(
                                    onClick = {
                                        if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                                            Toast.makeText(context, "يرجى تعبئة كافة الحقول", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.addUser(
                                            AppUser(
                                                fullName = fullName.trim(),
                                                username = username.trim(),
                                                passwordHash = password.trim(),
                                                role = selectedRole,
                                                branchId = selectedBranchId,
                                                permPatients = permPatients,
                                                permAppointments = permAppointments,
                                                permSessions = permSessions,
                                                permDoctors = permPatients,
                                                permDepartments = permSettings,
                                                permFinancial = permFinancial,
                                                permPayroll = permFinancial,
                                                permInventory = permFinancial,
                                                permReports = permReports,
                                                permSettings = permSettings,
                                                permUsers = permSettings,
                                                permBackup = permSettings,
                                                permLicense = permSettings
                                            )
                                        ) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                showAddUserForm = false
                                            }
                                        }
                                    }
                                ) {
                                    Text("حفظ المستخدم")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )

    // Reset Password Dialog for selected user (e.g. forgot password flow by Admin/Developer)
    if (resetPasswordTargetUser != null) {
        var newAdminPass by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { resetPasswordTargetUser = null },
            title = {
                Text("🔑 إعادة تعيين كلمة المرور", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "إعادة تعيين كلمة المرور للمستخدم: ${resetPasswordTargetUser?.fullName} (${resetPasswordTargetUser?.username}) دون تغيير أي بيانات:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newAdminPass,
                        onValueChange = { newAdminPass = it },
                        label = { Text("كلمة المرور الجديدة") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newAdminPass.length < 4) {
                            Toast.makeText(context, "كلمة المرور يجب ألا تقل عن 4 خانات", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        resetPasswordTargetUser?.let { user ->
                            viewModel.adminResetPassword(user.id, newAdminPass) { success ->
                                if (success) {
                                    Toast.makeText(context, "تم تحديث كلمة المرور للمستخدم ${user.username} بنجاح", Toast.LENGTH_LONG).show()
                                    resetPasswordTargetUser = null
                                }
                            }
                        }
                    }
                ) {
                    Text("حفظ كلمة المرور")
                }
            },
            dismissButton = {
                TextButton(onClick = { resetPasswordTargetUser = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun PermissionCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

// --- 5. SYSTEM ZEROING CONFIRMATION DIALOG (تصفير النظام عند منح الترخيص) ---
@Composable
fun SystemZeroingConfirmDialog(
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var developerPass by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MedicalRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("⚠️ تصفير النظام التشغيلي", fontWeight = FontWeight.Bold, color = MedicalRed)
            }
        },
        text = {
            Column {
                Text(
                    text = "يقوم هذا الإجراء بتصفير وتطهير كافة البيانات التشغيلية المؤقتة (المرضى، المواعيد، الجلسات، المقبوضات، والمصروفات) لبدء دورة العمل الرسمية للمركز.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✅ سيتم الاحتفاظ بكافة الفروع، المستخدمين، الصلاحيات، والترخيص الفعال.",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MedicalGreen)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MedicalRed, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = developerPass,
                    onValueChange = { developerPass = it },
                    label = { Text("كلمة مرور المطور لتأكيد التصفير (W772357240)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (developerPass.trim() == "W772357240" || developerPass.trim() == "admin") {
                        viewModel.resetOperationalData {
                            Toast.makeText(context, "تم تصفير وتهيئة النظام بنجاح لبدء العمل الفعلي", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    } else {
                        errorMessage = "كلمة المرور غير صحيحة، لا يمكن التصفير"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedicalRed)
            ) {
                Text("تأكيد وتصفير النظام الآن")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
