package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.models.AppLicense
import com.example.data.models.AppMessage
import com.example.data.models.AppNotification
import com.example.data.models.AppUser
import com.example.data.models.Appointment
import com.example.data.models.AuditLog
import com.example.data.models.Branch
import com.example.data.models.CenterSettings
import com.example.data.models.ClinicSession
import com.example.data.models.Department
import com.example.data.models.DiagnosisItem
import com.example.data.models.Doctor
import com.example.data.models.Employee
import com.example.data.models.ExpenseVoucher
import com.example.data.models.InventoryItem
import com.example.data.models.MedicalService
import com.example.data.models.MessageTemplate
import com.example.data.models.Patient
import com.example.data.models.PatientPackage
import com.example.data.models.ReceiptVoucher
import com.example.data.models.SalaryDeduction
import com.example.data.models.Therapist
import com.example.data.repository.ClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class UniversalSearchResults(
    val matchingPatients: List<Patient> = emptyList(),
    val matchingAppointments: List<Appointment> = emptyList(),
    val matchingSessions: List<ClinicSession> = emptyList(),
    val matchingDoctors: List<Doctor> = emptyList(),
    val matchingServices: List<MedicalService> = emptyList(),
    val matchingReceipts: List<ReceiptVoucher> = emptyList()
) {
    val totalCount: Int
        get() = matchingPatients.size + matchingAppointments.size + matchingSessions.size +
                matchingDoctors.size + matchingServices.size + matchingReceipts.size
    val isEmpty: Boolean get() = totalCount == 0
}

class ClinicViewModel(private val repository: ClinicRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val todayDateStr: String = dateFormat.format(Date())

    // --- Search State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Organization & Branches ---
    val allBranches: StateFlow<List<Branch>> = repository.allBranches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<AppUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _activeBranch = MutableStateFlow<Branch?>(null) // null = جميع الفروع
    val activeBranch: StateFlow<Branch?> = _activeBranch.asStateFlow()

    val currentBranchId: Long
        get() = _activeBranch.value?.id ?: _currentUser.value?.branchId ?: 1L

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // --- Core Flows ---
    val patients: StateFlow<List<Patient>> = repository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPatients: StateFlow<List<Patient>> = combine(patients, searchQuery, _activeBranch) { list, query, branch ->
        val branchScoped = if (branch == null) list else list.filter { it.branchId == branch.id }
        if (query.isBlank()) branchScoped
        else {
            val q = query.trim().lowercase()
            branchScoped.filter {
                it.name.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.fileNumber.lowercase().contains(q) ||
                        it.diagnosis.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors: StateFlow<List<Doctor>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val therapists: StateFlow<List<Therapist>> = repository.allTherapists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments: StateFlow<List<Department>> = repository.allDepartments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val services: StateFlow<List<MedicalService>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diagnoses: StateFlow<List<DiagnosisItem>> = repository.allDiagnoses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = combine(repository.allAppointments, _activeBranch) { appts, branch ->
        if (branch == null) appts else appts.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAppointments: StateFlow<List<Appointment>> = combine(repository.getAppointmentsByDate(todayDateStr), _activeBranch) { appts, branch ->
        if (branch == null) appts else appts.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val packages: StateFlow<List<PatientPackage>> = repository.allPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val branchScopedPackages: StateFlow<List<PatientPackage>> = combine(packages, _activeBranch) { list, branch ->
        if (branch == null) list else list.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<ClinicSession>> = combine(repository.allSessions, _activeBranch) { sess, branch ->
        if (branch == null) sess else sess.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySessions: StateFlow<List<ClinicSession>> = combine(repository.getSessionsByDate(todayDateStr), _activeBranch) { sess, branch ->
        if (branch == null) sess else sess.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receipts: StateFlow<List<ReceiptVoucher>> = combine(repository.allReceipts, _activeBranch) { rcpts, branch ->
        if (branch == null) rcpts else rcpts.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseVoucher>> = combine(repository.allExpenses, _activeBranch) { exps, branch ->
        if (branch == null) exps else exps.filter { it.branchId == branch.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Universal Live Search Results ---
    val universalSearchResults: StateFlow<UniversalSearchResults> = combine(
        searchQuery,
        filteredPatients,
        appointments,
        sessions,
        doctors
    ) { query, ptList, apptList, sessList, docList ->
        if (query.isBlank() || query.length < 2) {
            UniversalSearchResults()
        } else {
            val q = query.trim().lowercase()
            UniversalSearchResults(
                matchingPatients = ptList.filter {
                    it.name.lowercase().contains(q) ||
                        it.phone.contains(q) ||
                        it.fileNumber.lowercase().contains(q) ||
                        it.diagnosis.lowercase().contains(q)
                }.take(5),
                matchingAppointments = apptList.filter {
                    it.appointmentNumber.lowercase().contains(q) ||
                        it.date.contains(q) ||
                        it.notes.lowercase().contains(q)
                }.take(5),
                matchingSessions = sessList.filter {
                    it.sessionNumber.lowercase().contains(q) ||
                        it.date.contains(q) ||
                        it.notes.lowercase().contains(q)
                }.take(5),
                matchingDoctors = docList.filter {
                    it.name.lowercase().contains(q) ||
                        it.specialization.lowercase().contains(q) ||
                        it.phone.contains(q)
                }.take(5)
            )
        }
    }
    .combine(services) { results, srvList ->
        val q = searchQuery.value.trim().lowercase()
        if (q.length < 2) results
        else results.copy(
            matchingServices = srvList.filter { it.name.lowercase().contains(q) }.take(5)
        )
    }
    .combine(receipts) { results, rcptList ->
        val q = searchQuery.value.trim().lowercase()
        if (q.length < 2) results
        else results.copy(
            matchingReceipts = rcptList.filter {
                it.voucherNumber.lowercase().contains(q) || it.statement.lowercase().contains(q)
            }.take(5)
        )
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UniversalSearchResults())

    val employees: StateFlow<List<Employee>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deductions: StateFlow<List<SalaryDeduction>> = repository.allDeductions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventory: StateFlow<List<InventoryItem>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val messages: StateFlow<List<AppMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<MessageTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val license: StateFlow<AppLicense?> = repository.licenseFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val settings: StateFlow<CenterSettings?> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Dashboard Metrics & Computations ---
    val todayRevenue: StateFlow<Double> = receipts.combine(todaySessions) { receiptList, _ ->
        receiptList.filter { it.date == todayDateStr }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalReceiptsAmount: StateFlow<Double> = receipts.combine(packages) { list, _ ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpensesAmount: StateFlow<Double> = expenses.combine(employees) { list, _ ->
        list.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalOutstandingBalances: StateFlow<Double> = combine(patients, _activeBranch) { list, branch ->
        if (branch == null) list else list.filter { it.branchId == branch.id }
    }.map { list -> list.sumOf { it.balanceDue } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val suspendedPatients: StateFlow<List<Patient>> = combine(patients, searchQuery, _activeBranch) { list, q, branch ->
        val branchScoped = if (branch == null) list else list.filter { it.branchId == branch.id }
        val filtered = if (q.isBlank()) branchScoped else branchScoped.filter {
            it.name.contains(q, ignoreCase = true) ||
                it.phone.contains(q) ||
                it.fileNumber.contains(q, ignoreCase = true)
        }
        filtered.filter { it.balanceDue > 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suspendedCount: StateFlow<Int> = suspendedPatients.combine(patients) { list, _ ->
        list.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSuspendedDue: StateFlow<Double> = suspendedPatients.combine(patients) { list, _ ->
        list.sumOf { it.balanceDue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val activePackagesCount: StateFlow<Int> = branchScopedPackages
        .map { list -> list.count { it.status == "نشطة" && it.remainingSessions > 0 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRemainingSessions: StateFlow<Int> = branchScopedPackages
        .map { list -> list.filter { it.status == "نشطة" }.sumOf { it.remainingSessions } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val packageAlerts: StateFlow<List<PatientPackage>> = branchScopedPackages
        .map { pkgs -> pkgs.filter { it.status == "نشطة" && it.remainingSessions in 1..3 } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Trial & License Calculations ---
    val trialDaysRemaining: StateFlow<Int> = license.combine(settings) { lic, _ ->
        if (lic == null) 30
        else {
            val now = System.currentTimeMillis()
            val diff = lic.endDate - now
            max(0, (diff / (24L * 60L * 60L * 1000L)).toInt())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val isTrialExpired: StateFlow<Boolean> = license.combine(trialDaysRemaining) { lic, days ->
        if (lic == null) false
        else lic.licenseType == "TRIAL" && (days <= 0 || System.currentTimeMillis() > lic.endDate)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- Actions ---

    fun attendSession(sessionId: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.attendSession(sessionId)
            res.fold(
                onSuccess = { onResult(true, it) },
                onFailure = { onResult(false, it.message ?: "حدث خطأ أثناء تسجيل الحضور") }
            )
        }
    }

    fun updateSessionStatus(sessionId: Long, status: String) {
        viewModelScope.launch {
            repository.updateSessionStatus(sessionId, status)
        }
    }

    fun registerPatient(
        name: String,
        phone: String,
        gender: String,
        dateOfBirth: String,
        address: String,
        maritalStatus: String,
        profession: String,
        referralSource: String,
        doctorId: Long?,
        therapistId: Long?,
        departmentId: Long?,
        serviceId: Long? = null,
        diagnosis: String,
        complaint: String,
        notes: String,
        initialBalance: Double = 0.0,
        branchId: Long? = null,
        autoCreateFirstSessionOrAppt: Boolean = true,
        notifyDoctor: Boolean = true,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.registerPatient(
                name = name,
                phone = phone,
                gender = gender,
                dateOfBirth = dateOfBirth,
                address = address,
                maritalStatus = maritalStatus,
                profession = profession,
                referralSource = referralSource,
                doctorId = doctorId,
                therapistId = therapistId,
                departmentId = departmentId,
                serviceId = serviceId,
                diagnosis = diagnosis,
                complaint = complaint,
                notes = notes,
                initialBalance = initialBalance,
                branchId = bId,
                autoCreateFirstSessionOrAppt = autoCreateFirstSessionOrAppt,
                notifyDoctor = notifyDoctor
            )
            res.fold(
                onSuccess = { onResult(true, "تم حفظ المريض بنجاح برقم تعريف $it") },
                onFailure = { onResult(false, it.message ?: "فشل حفظ المريض") }
            )
        }
    }

    fun updatePatient(patient: Patient, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updatePatient(patient)
                onResult(true, "تم تحديث بيانات المريض بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل تحديث البيانات")
            }
        }
    }

    fun archivePatient(id: Long, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.softDeletePatient(id)
                onResult(true, "تمت أرشفة المريض بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل أرشفة المريض")
            }
        }
    }

    fun bookAppointment(
        patientId: Long,
        doctorId: Long?,
        therapistId: Long?,
        departmentId: Long?,
        serviceId: Long?,
        date: String,
        timeSlot: String,
        notes: String,
        branchId: Long? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.bookAppointment(
                patientId, doctorId, therapistId, departmentId, serviceId, date, timeSlot, notes, bId
            )
            res.fold(
                onSuccess = { onResult(true, "تم حجز الموعد بنجاح") },
                onFailure = { onResult(false, it.message ?: "فشل حجز الموعد") }
            )
        }
    }

    fun createSession(
        patientId: Long,
        doctorId: Long?,
        therapistId: Long?,
        departmentId: Long?,
        serviceId: Long?,
        packageId: Long? = null,
        date: String,
        time: String,
        status: String = "مجدولة",
        notes: String = "",
        branchId: Long? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.createSession(
                patientId, doctorId, therapistId, departmentId, serviceId, packageId, date, time, status, notes, bId
            )
            res.fold(
                onSuccess = { onResult(true, "تم تسجيل وتأكيد الجلسة بنجاح") },
                onFailure = { onResult(false, it.message ?: "فشل تسجيل الجلسة") }
            )
        }
    }

    fun createPackage(
        patientId: Long,
        packageName: String,
        price: Double,
        totalSessions: Int,
        startDate: String,
        endDate: String,
        notes: String,
        departmentId: Long? = null,
        serviceId: Long? = null,
        doctorId: Long? = null,
        therapistId: Long? = null,
        branchId: Long? = null,
        generateSessions: Boolean = true,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.createPackage(
                patientId = patientId,
                packageName = packageName,
                price = price,
                totalSessions = totalSessions,
                startDate = startDate,
                endDate = endDate,
                notes = notes,
                departmentId = departmentId,
                serviceId = serviceId,
                doctorId = doctorId,
                therapistId = therapistId,
                branchId = bId,
                generateSessions = generateSessions
            )
            res.fold(
                onSuccess = { onResult(true, "تم إنشاء الباقة وتوليد جلساتها بنجاح") },
                onFailure = { onResult(false, it.message ?: "فشل إنشاء الباقة") }
            )
        }
    }

    fun createReceiptVoucher(
        patientId: Long,
        amount: Double,
        paymentMethod: String,
        statement: String,
        branchId: Long? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.createReceiptVoucher(patientId, amount, paymentMethod, statement, bId)
            res.fold(
                onSuccess = { onResult(true, "تم إنشاء سند القبض وتحديث رصيد المريض بنجاح") },
                onFailure = { onResult(false, it.message ?: "فشل إنشاء سند القبض") }
            )
        }
    }

    // --- Deliver & Settle Invoice (Full: returns ReceiptVoucher & Patient for Thermal/Standard Print) ---
    fun deliverAndSettleInvoice(
        patientId: Long,
        amount: Double,
        paymentMethod: String,
        statement: String,
        branchId: Long? = null,
        sendWhatsApp: Boolean = false,
        sendSMS: Boolean = false,
        context: Context? = null,
        onResult: (Boolean, ReceiptVoucher?, Patient?, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.createReceiptVoucherFull(patientId, amount, paymentMethod, statement, bId)
            res.fold(
                onSuccess = { (receipt, updatedPatient) ->
                    if (context != null) {
                        val formattedAmount = "%,.0f".format(Locale.ENGLISH, receipt.amount)
                        val formattedRemaining = "%,.0f".format(Locale.ENGLISH, receipt.remainingBalance)
                        val msg = "مرحبًا ${updatedPatient.name}\nتم تسليم وسداد دفعة من الفاتورة بمبلغ $formattedAmount ر.ي.\nسند رقم: ${receipt.voucherNumber}\nالمتبقي بذمتكم: $formattedRemaining ر.ي.\nشكرًا لتعاملكم مع مركز وسيم الطبي."
                        if (sendWhatsApp) {
                            sendWhatsApp(context, updatedPatient.phone, msg)
                        }
                        if (sendSMS) {
                            sendSMS(context, updatedPatient.phone, msg)
                        }
                    }
                    onResult(true, receipt, updatedPatient, "تم حفظ وسداد الفاتورة بنجاح")
                },
                onFailure = {
                    onResult(false, null, null, it.message ?: "فشل سداد الفاتورة")
                }
            )
        }
    }

    // --- Attend Session with WhatsApp & SMS Communication ---
    fun attendSessionWithCommunication(
        sessionId: Long,
        notifyWhatsApp: Boolean,
        notifySMS: Boolean,
        context: Context,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.attendSession(sessionId)
            res.fold(
                onSuccess = { msg ->
                    // Fetch session and patient to send WhatsApp/SMS
                    val currentSession = repository.getSessionById(sessionId)
                    val patient = currentSession?.let { repository.getPatientById(it.patientId) }
                    if (patient != null && currentSession != null) {
                        val pkg = currentSession.packageId?.let { repository.getPackageById(it) }
                            ?: repository.getPatientById(patient.id)?.let { repository.getPackageById(it.id) }
                        val remaining = pkg?.remainingSessions ?: 0
                        val commMessage = "مرحبًا ${patient.name}\n\nتم تسجيل حضوركم في جلسة العلاج الطبيعي اليوم (جلسة رقم ${currentSession.sessionNumber}).\nالجلسات المتبقية في باقتكم: $remaining جلسة.\n\nنتمنى لكم دوام الصحة والعافية - مركز وسيم الطبي."

                        if (notifyWhatsApp) {
                            sendWhatsApp(context, patient.phone, commMessage)
                        }
                        if (notifySMS) {
                            sendSMS(context, patient.phone, commMessage)
                        }
                    }
                    onResult(true, msg)
                },
                onFailure = {
                    onResult(false, it.message ?: "حدث خطأ أثناء تسجيل الحضور")
                }
            )
        }
    }

    // --- Register Patient with Welcome WhatsApp / SMS ---
    fun registerPatientWithCommunication(
        name: String,
        phone: String,
        gender: String,
        dateOfBirth: String,
        address: String,
        maritalStatus: String,
        profession: String,
        referralSource: String,
        doctorId: Long?,
        therapistId: Long?,
        departmentId: Long?,
        serviceId: Long? = null,
        diagnosis: String,
        complaint: String,
        notes: String,
        initialBalance: Double = 0.0,
        branchId: Long? = null,
        autoCreateFirstSessionOrAppt: Boolean = true,
        notifyDoctor: Boolean = true,
        notifyWhatsApp: Boolean = false,
        notifySMS: Boolean = false,
        context: Context? = null,
        onResult: (Boolean, Patient?, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.registerPatient(
                name = name,
                phone = phone,
                gender = gender,
                dateOfBirth = dateOfBirth,
                address = address,
                maritalStatus = maritalStatus,
                profession = profession,
                referralSource = referralSource,
                doctorId = doctorId,
                therapistId = therapistId,
                departmentId = departmentId,
                serviceId = serviceId,
                diagnosis = diagnosis,
                complaint = complaint,
                notes = notes,
                initialBalance = initialBalance,
                branchId = bId,
                autoCreateFirstSessionOrAppt = autoCreateFirstSessionOrAppt,
                notifyDoctor = notifyDoctor
            )
            res.fold(
                onSuccess = { newId ->
                    val newlyRegistered = repository.getPatientById(newId)
                        ?: return@fold onResult(false, null, "تم حفظ المريض لكن تعذر قراءة الملف الذي تم إنشاؤه")

                    if (context != null) {
                        val welcomeMsg = "أهلاً وسهلاً بكم ${newlyRegistered.name} في مركز وسيم الطبي والتأهيلي.\nتم فتح ملف طبي لكم برقم: ${newlyRegistered.fileNumber}.\nنحن سعداء بخدمتكم ونتمنى لكم موفور الصحة.\nللاستفسار: 772357240"
                        if (notifyWhatsApp) {
                            sendWhatsApp(context, phone, welcomeMsg)
                        }
                        if (notifySMS) {
                            sendSMS(context, phone, welcomeMsg)
                        }
                    }
                    onResult(true, newlyRegistered, "تم تسجيل المريض بنجاح برقم ملف ${newlyRegistered.fileNumber}")
                },
                onFailure = {
                    onResult(false, null, it.message ?: "فشل تسجيل المريض")
                }
            )
        }
    }

    fun createExpenseVoucher(
        beneficiaryType: String,
        beneficiaryName: String,
        amount: Double,
        paymentMethod: String,
        category: String,
        statement: String,
        branchId: Long? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val bId = branchId ?: currentBranchId
            val res = repository.createExpenseVoucher(
                beneficiaryType, beneficiaryName, amount, paymentMethod, category, statement, bId
            )
            res.fold(
                onSuccess = { onResult(true, "تم تسجيل سند الصرف بنجاح") },
                onFailure = { onResult(false, it.message ?: "فشل تسجيل سند الصرف") }
            )
        }
    }

    fun addEmployee(employee: Employee, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addEmployee(employee)
                onResult(true, "تمت إضافة الموظف بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل إضافة الموظف")
            }
        }
    }

    fun addDeduction(deduction: SalaryDeduction, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addDeduction(deduction)
                onResult(true, "تم تسجيل الاستقطاع بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل تسجيل الاستقطاع")
            }
        }
    }

    fun addDoctor(doctor: Doctor, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addDoctor(doctor)
                onResult(true, "تمت إضافة الطبيب بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل إضافة الطبيب")
            }
        }
    }

    fun addTherapist(therapist: Therapist, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addTherapist(therapist)
                onResult(true, "تمت إضافة المعالج بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل إضافة المعالج")
            }
        }
    }

    fun addDepartment(department: Department, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addDepartment(department)
                onResult(true, "تمت إضافة القسم بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل إضافة القسم")
            }
        }
    }

    fun addService(service: MedicalService, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addService(service)
                onResult(true, "تمت إضافة الخدمة بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل إضافة الخدمة")
            }
        }
    }

    fun addInventoryItem(item: InventoryItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.addInventoryItem(item)
                onResult(true, "تمت إضافة الصنف بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل إضافة الصنف")
            }
        }
    }

    fun updateInventoryItem(item: InventoryItem, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateInventoryItem(item)
                onResult(true, "تم تحديث الصنف بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل تحديث الصنف")
            }
        }
    }

    fun findInventoryByBarcode(barcode: String, onResult: (InventoryItem?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.findInventoryByBarcode(barcode.trim()))
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    fun updateTemplate(template: MessageTemplate, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateTemplate(template)
                onResult(true, "تم حفظ قالب الرسالة بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل حفظ القالب")
            }
        }
    }

    fun activateLicense(
        customerName: String,
        centerName: String,
        licenseNumber: String,
        licenseType: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.activateLicense(customerName, centerName, licenseNumber, licenseType)
            res.fold(
                onSuccess = { onResult(true, "تم تفعيل الترخيص بنجاح!") },
                onFailure = { onResult(false, it.message ?: "فشل تفعيل الترخيص") }
            )
        }
    }

    fun updateSettings(settings: CenterSettings, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateSettings(settings)
                onResult(true, "تم حفظ الإعدادات بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "فشل حفظ الإعدادات")
            }
        }
    }

    // --- AUTHENTICATION & LOGIN ---
    fun login(username: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            val u = username.trim()
            val p = password.trim()
            val user = repository.authenticate(u, p)
            if (user != null) {
                _currentUser.value = user
                if (user.branchId != null) {
                    _activeBranch.value = allBranches.value.firstOrNull { it.id == user.branchId }
                } else {
                    _activeBranch.value = null // All branches for super admin
                }
                onResult(true)
            } else {
                _loginError.value = "اسم المستخدم أو كلمة المرور غير صحيحة"
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _activeBranch.value = null
        _searchQuery.value = ""
    }

    fun switchBranch(branch: Branch?) {
        _activeBranch.value = branch
    }

    // Change own password
    fun changePassword(userId: Long, oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = allUsers.value.firstOrNull { it.id == userId }
            if (user == null) {
                onResult(false, "المستخدم غير موجود")
                return@launch
            }
            if (!repository.verifyUserPassword(userId, oldPass)) {
                onResult(false, "كلمة المرور الحالية غير صحيحة")
                return@launch
            }
            try {
                repository.updateUserPassword(userId, newPass, user.fullName)
                _currentUser.value = repository.getUserByUsername(user.username)
                onResult(true, "تم تغيير كلمة المرور بنجاح")
            } catch (e: Exception) {
                onResult(false, e.message ?: "تعذر تغيير كلمة المرور")
            }
        }
    }

    // Admin reset password for any user without affecting their data
    fun adminResetPassword(targetUserId: Long, newPass: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "م. وسيم الفرح (مالك النظام)"
            try {
                repository.updateUserPassword(targetUserId, newPass.trim(), performer)
                onResult(true)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    // --- BRANCH MANAGEMENT ---
    fun addBranch(name: String, city: String, address: String, phone: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.addBranch(
                Branch(
                    name = name.trim(),
                    city = city.trim(),
                    address = address.trim(),
                    phone = phone.trim()
                )
            )
            onResult(true)
        }
    }

    fun updateBranch(branch: Branch, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.updateBranch(branch)
            onResult(true)
        }
    }

    fun deleteBranch(branchId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteBranch(branchId)
            if (_activeBranch.value?.id == branchId) {
                _activeBranch.value = null
            }
            onResult(true)
        }
    }

    // --- USERS & PERMISSIONS MANAGEMENT ---
    fun addUser(user: AppUser, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByUsername(user.username)
            if (existing != null) {
                onResult(false, "اسم المستخدم مستخدم مسبقاً، يرجى اختيار اسم آخر")
                return@launch
            }
            repository.addUser(user)
            onResult(true, "تم إنشاء حساب المستخدم بنجاح")
        }
    }

    fun updateUser(user: AppUser, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            repository.updateUser(user)
            if (_currentUser.value?.id == user.id) {
                _currentUser.value = user
            }
            onResult(true, "تم تحديث بيانات المستخدم والصلاحيات بنجاح")
        }
    }

    fun deleteUser(userId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            onResult(true)
        }
    }

    // --- ZEROING / RESET OPERATIONAL SYSTEM DATA (تصفير النظام) ---
    fun resetOperationalData(onComplete: () -> Unit) {
        viewModelScope.launch {
            val performer = _currentUser.value?.fullName ?: "م. وسيم الفرح (مالك ومطور النظام)"
            repository.resetOperationalData(performer)
            onComplete()
        }
    }

    // Helper: Check if current user has permission
    fun canAccess(check: (AppUser) -> Boolean): Boolean {
        val user = _currentUser.value ?: return true // during setup or default
        if (user.isSystemOwner || user.role == "SUPER_ADMIN") return true
        return check(user)
    }

    // --- WhatsApp Standardizer & Sender ---
    fun sendWhatsApp(context: Context, phone: String, messageText: String) {
        try {
            // Yemeni phone sanitizer: "077xxxxxxx" -> "96777xxxxxxx", "77xxxxxxx" -> "96777xxxxxxx"
            val digits = phone.replace(Regex("[^0-9]"), "")
            val formatted = when {
                digits.startsWith("00967") -> digits.removePrefix("00")
                digits.startsWith("+967") -> digits.removePrefix("+")
                digits.startsWith("967") -> digits
                digits.startsWith("0") -> "967" + digits.substring(1)
                digits.length == 9 -> "967$digits"
                else -> "967$digits"
            }
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formatted&text=" + Uri.encode(messageText))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح تطبيق واتساب مباشرة: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- SMS Sender ---
    fun sendSMS(context: Context, phone: String, messageText: String) {
        try {
            val uri = Uri.parse("smsto:${phone.trim()}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تعذر فتح تطبيق الرسائل النصية: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Local Backup & Restore ---
    fun createBackup(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportBackupJson()
                onResult(true, json)
            } catch (e: Exception) {
                onResult(false, e.message ?: "تعذر إنشاء النسخة الاحتياطية")
            }
        }
    }

    fun restoreBackupJson(json: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreBackupJson(json)
            result.fold(
                onSuccess = { onResult(true, "تمت استعادة النسخة الاحتياطية بنجاح") },
                onFailure = { onResult(false, it.message ?: "تعذر استعادة النسخة الاحتياطية") }
            )
        }
    }
}

class ClinicViewModelFactory(private val repository: ClinicRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClinicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ClinicViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
