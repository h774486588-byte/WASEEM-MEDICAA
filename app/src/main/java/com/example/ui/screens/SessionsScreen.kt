package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.ClinicSession
import com.example.data.models.Patient
import com.example.ui.components.StatusBadge
import com.example.ui.theme.MedicalAmber
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalGreen
import com.example.ui.theme.MedicalRed
import com.example.ui.theme.MedicalRedLight
import com.example.ui.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val todaySessions by viewModel.todaySessions.collectAsStateWithLifecycle()
    val allSessions by viewModel.sessions.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    val displayList = if (selectedTab == 0) todaySessions else allSessions

    var attendanceTargetSession by remember { mutableStateOf<ClinicSession?>(null) }

    // Dialog for attendance with WhatsApp and SMS
    if (attendanceTargetSession != null) {
        SessionAttendanceDialog(
            session = attendanceTargetSession!!,
            viewModel = viewModel,
            onDismiss = { attendanceTargetSession = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الجلسات والحضور", fontWeight = FontWeight.Bold) },
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
                    text = { Text("جلسات اليوم (${todaySessions.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("سجل كل الجلسات (${allSessions.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == 0) "لا توجد جلسات مجدولة لهذا اليوم" else "لا توجد جلسات مسجلة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayList) { session ->
                        val patient = patients.find { it.id == session.patientId }
                        SessionCardItem(
                            session = session,
                            patient = patient,
                            onOpenAttendanceDialog = {
                                attendanceTargetSession = session
                            },
                            onNoShow = {
                                viewModel.updateSessionStatus(session.id, "لم يحضر")
                                Toast.makeText(context, "تم تسجيل عدم الحضور", Toast.LENGTH_SHORT).show()
                            },
                            onStart = {
                                viewModel.updateSessionStatus(session.id, "جارية")
                                Toast.makeText(context, "بدأت الجلسة", Toast.LENGTH_SHORT).show()
                            },
                            onFinish = {
                                viewModel.updateSessionStatus(session.id, "مكتملة")
                                Toast.makeText(context, "اكتملت الجلسة بنجاح", Toast.LENGTH_SHORT).show()
                            },
                            onCancel = {
                                viewModel.updateSessionStatus(session.id, "أُلغي")
                                Toast.makeText(context, "تم إلغاء الجلسة", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionCardItem(
    session: ClinicSession,
    patient: Patient?,
    onOpenAttendanceDialog: () -> Unit,
    onNoShow: () -> Unit,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}"),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = patient?.name ?: "مريض غير محدد",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (patient != null && patient.balanceDue > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = MedicalRedLight) {
                                Text(
                                    text = "معلّق (عليه فاتورة)",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MedicalRed, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                    Text(
                        text = "رقم الجلسة: ${session.sessionNumber} | التاريخ: ${session.date} - ${session.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = session.status)
            }

            if (session.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات: ${session.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // One-tap [حضور حالة] opens attendance modal (Attend, WhatsApp, SMS, Ticket)
                if (session.status != "حضر" && !session.isDeductedFromPackage) {
                    Button(
                        onClick = onOpenAttendanceDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f).testTag("attend_action_btn_${session.id}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حضور حالة", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MedicalGreen.copy(alpha = 0.12f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text("تم الحضور والخصم ✓", color = MedicalGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (session.status == "مجدولة" || session.status == "حضر") {
                    OutlinedButton(
                        onClick = onStart,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text("بدء")
                    }
                }

                if (session.status == "جارية") {
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("إنهاء")
                    }
                }

                if (session.status != "حضر" && session.status != "مكتملة") {
                    OutlinedButton(
                        onClick = onNoShow,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("لم يحضر")
                    }
                }
            }
        }
    }
}
