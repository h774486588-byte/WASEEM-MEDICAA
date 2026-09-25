package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalBlueDark
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalTeal
import com.example.ui.viewmodel.ClinicViewModel

@Composable
fun LoginScreen(
    viewModel: ClinicViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showDeveloperEmergencyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MedicalBlueDark,
                        MedicalBlue,
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header: System Logo & Title
            Surface(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_waseem_logo),
                        contentDescription = "شعار وسيم الطبي",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "نظام وسيم الطبي PRO",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "بوابة تسجيل الدخول الآمنة وإدارة الفروع",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.85f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "تسجيل الدخول للنظام",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MedicalBlueDark
                        )
                    )
                    Text(
                        text = "أدخل بيانات اعتماد حسابك للوصول إلى صلاحياتك",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick-fill buttons for immediate 1-tap selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    username = "admin"
                                    password = "admin"
                                },
                            color = MedicalBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "⚡ دخول المدير (admin)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalBlue
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    username = "Waseem"
                                    password = "W772357240"
                                },
                            color = MedicalAmber.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "👑 المالك (Waseem)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Error Message
                    AnimatedVisibility(visible = loginError != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = MedicalRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MedicalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = loginError ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MedicalRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Field 1: Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("اسم المستخدم") },
                        placeholder = { Text("مثال: admin أو Waseem") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MedicalBlue)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedicalBlue,
                            focusedLabelColor = MedicalBlue
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field 2: Password with Show/Hide Toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        placeholder = { Text("أدخل كلمة المرور") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MedicalBlue)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.testTag("toggle_password_visibility")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedicalBlue,
                            focusedLabelColor = MedicalBlue
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val u = username.trim()
                                val p = password.trim()
                                if (u.isNotBlank() && p.isNotBlank()) {
                                    isLoading = true
                                    viewModel.login(u, p) { success ->
                                        isLoading = false
                                        if (success) onLoginSuccess()
                                    }
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = true },
                            modifier = Modifier.testTag("forgot_password_btn")
                        ) {
                            Text(
                                text = "نسيت كلمة المرور؟",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MedicalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            val u = username.trim()
                            val p = password.trim()
                            if (u.isBlank() || p.isBlank()) {
                                Toast.makeText(context, "يرجى إدخال اسم المستخدم وكلمة المرور", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            viewModel.login(u, p) { success ->
                                isLoading = false
                                if (success) {
                                    onLoginSuccess()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_submit_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ التحقق...")
                        } else {
                            Text(
                                text = "تسجيل الدخول",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Access Information Card for Trial & Developer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.12f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF67E8F9),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بيانات الدخول المعتمدة للنظام:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• الفترة التجريبية: اسم المستخدم admin / كلمة المرور admin",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                    Text(
                        text = "• مالك النظام والمطور: اسم المستخدم Waseem / كلمة المرور W772357240",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }
            }
        }
    }

    // --- FORGOT PASSWORD DIALOG ---
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MedicalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔑 استعادة الحساب وكلمة المرور", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "في حال نسيان كلمة المرور، لا تقلق! بياناتك وملفاتك وسجلاتك محفوظة بأمان تام.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "يرجى الاتصال بمالك ومطور النظام (م. وسيم الفرح) للتحقق وإعادة تعيين كلمة المرور فورياً دون المساس بأي من بياناتك:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "م. وسيم الفرح (مالك ومطور النظام)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "هاتف الدعم المباشر والواتساب: 772357240",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:772357240"))
                                context.startActivity(callIntent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = {
                                viewModel.sendWhatsApp(
                                    context = context,
                                    phone = "772357240",
                                    messageText = "مرحبًا م. وسيم، أحتاج مساعدة في استعادة كلمة المرور لنظام وسيم الطبي PRO."
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal)
                        ) {
                            Text("واتساب", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            showForgotPasswordDialog = false
                            showDeveloperEmergencyDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("دخول طوارئ المطور (Waseem)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    // --- DEVELOPER EMERGENCY RECOVERY DIALOG ---
    if (showDeveloperEmergencyDialog) {
        var devPassword by remember { mutableStateOf("") }
        var devError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDeveloperEmergencyDialog = false },
            title = {
                Text("🛡️ دخول المطور لاستعادة الحسابات", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "يقوم المطور (Waseem) بإدخال كلمة المرور المعتمدة للدخول واستعادة كلمة مرور أي مستخدم فوراً دون المساس بالبيانات:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (devError != null) {
                        Text(text = devError!!, color = MedicalRed, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    OutlinedTextField(
                        value = "Waseem",
                        onValueChange = {},
                        label = { Text("حساب المطور") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = devPassword,
                        onValueChange = { devPassword = it },
                        label = { Text("كلمة مرور المطور (W772357240)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (devPassword.trim() == "W772357240") {
                            viewModel.login("Waseem", "W772357240") { success ->
                                if (success) {
                                    showDeveloperEmergencyDialog = false
                                    onLoginSuccess()
                                    Toast.makeText(context, "مرحبًا بك م. وسيم (مالك النظام)", Toast.LENGTH_LONG).show()
                                }
                            }
                        } else {
                            devError = "كلمة مرور المطور غير صحيحة"
                        }
                    }
                ) {
                    Text("تأكيد ودخول")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeveloperEmergencyDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
