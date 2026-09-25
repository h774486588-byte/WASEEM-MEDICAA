package com.example.data.repository

import com.example.data.dao.ClinicDao
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
import com.example.data.models.PackageSession
import com.example.data.models.Patient
import com.example.data.models.PatientPackage
import com.example.data.models.ReceiptVoucher
import com.example.data.models.SalaryDeduction
import com.example.data.models.Therapist
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class ClinicRepository(private val dao: ClinicDao) {
    val allBranches: Flow<List<Branch>> = dao.getAllBranches()
    val allUsers: Flow<List<AppUser>> = dao.getAllUsers()
    val allPatients: Flow<List<Patient>> = dao.getAllPatients()
    val allDoctors: Flow<List<Doctor>> = dao.getAllDoctors()
    val allTherapists: Flow<List<Therapist>> = dao.getAllTherapists()
    val allDepartments: Flow<List<Department>> = dao.getAllDepartments()
    val allServices: Flow<List<MedicalService>> = dao.getAllServices()
    val allDiagnoses: Flow<List<DiagnosisItem>> = dao.getAllDiagnoses()
    val allAppointments: Flow<List<Appointment>> = dao.getAllAppointments()
    val allPackages: Flow<List<PatientPackage>> = dao.getAllPackages()
    val allSessions: Flow<List<ClinicSession>> = dao.getAllSessions()
    val allReceipts: Flow<List<ReceiptVoucher>> = dao.getAllReceipts()
    val allExpenses: Flow<List<ExpenseVoucher>> = dao.getAllExpenses()
    val allEmployees: Flow<List<Employee>> = dao.getAllEmployees()
    val allDeductions: Flow<List<SalaryDeduction>> = dao.getAllDeductions()
    val allInventory: Flow<List<InventoryItem>> = dao.getAllInventory()
    val allNotifications: Flow<List<AppNotification>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationsCount()
    val allMessages: Flow<List<AppMessage>> = dao.getAllMessages()
    val allTemplates: Flow<List<MessageTemplate>> = dao.getAllTemplates()
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()
    val licenseFlow: Flow<AppLicense?> = dao.getLicense()
    val settingsFlow: Flow<CenterSettings?> = dao.getSettings()

    fun searchPatients(query: String): Flow<List<Patient>> = dao.searchPatients(query)
    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> = dao.getAppointmentsByDate(date)
    fun getSessionsByDate(date: String): Flow<List<ClinicSession>> = dao.getSessionsByDate(date)
    fun getPackagesByPatient(patientId: Long): Flow<List<PatientPackage>> = dao.getPackagesByPatient(patientId)
    fun getReceiptsByPatient(patientId: Long): Flow<List<ReceiptVoucher>> = dao.getReceiptsByPatient(patientId)

    suspend fun registerPatient(
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
        branchId: Long = 1,
        autoCreateFirstSessionOrAppt: Boolean = true,
        notifyDoctor: Boolean = true
    ): Result<Long> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("اسم المريض مطلوب"))
        if (phone.isBlank()) return Result.failure(IllegalArgumentException("رقم الهاتف مطلوب"))

        val timestamp = System.currentTimeMillis()
        // Millisecond-based identifier avoids the previous 9,000-value cyclic range and reduces collisions.
        val fileNumber = "WM-$timestamp"
        val patient = Patient(
            fileNumber = fileNumber,
            name = name.trim(), phone = phone.trim(), gender = gender,
            dateOfBirth = dateOfBirth, address = address.trim(), maritalStatus = maritalStatus,
            profession = profession.trim(), referralSource = referralSource.trim(),
            doctorId = doctorId, therapistId = therapistId, departmentId = departmentId,
            serviceId = serviceId, diagnosis = diagnosis.trim(), complaint = complaint.trim(),
            notes = notes.trim(), balanceDue = initialBalance, branchId = branchId
        )
        val id = dao.insertPatient(patient)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val todayStr = dateFormat.format(Date(timestamp))
        if (autoCreateFirstSessionOrAppt) {
            val uniqueSuffix = timestamp.toString()
            dao.insertSession(ClinicSession(
                sessionNumber = "SES-$uniqueSuffix", patientId = id, doctorId = doctorId,
                therapistId = therapistId, departmentId = departmentId, serviceId = serviceId,
                date = todayStr, time = "10:00 ص", status = "مجدولة",
                notes = "جلسة أولى مسجلة تلقائياً مع فتح الملف - ${diagnosis.trim()}", branchId = branchId
            ))
            dao.insertAppointment(Appointment(
                appointmentNumber = "APT-$uniqueSuffix", patientId = id, doctorId = doctorId,
                therapistId = therapistId, departmentId = departmentId, serviceId = serviceId,
                date = todayStr, timeSlot = "10:00 ص", status = "محجوز",
                notes = "موعد استشارة وجلسة أولى - ${diagnosis.trim()}", branchId = branchId
            ))
        }
        dao.insertAuditLog(AuditLog(
            user = "الاستقبال", action = "تسجيل مريض جديد",
            details = "تم تسجيل المريض $name - رقم الملف: $fileNumber - فرع $branchId",
            newData = "الهاتف: $phone, التشخيص: $diagnosis"
        ))
        if (notifyDoctor && doctorId != null) {
            val doctor = dao.getDoctorById(doctorId)
            if (doctor != null) {
                dao.insertNotification(AppNotification(
                    title = "حالة مريض جديدة - د. ${doctor.name}",
                    message = "تم تسجيل المريض $name وإسناده إليكم.", type = "طبيب", relatedId = id
                ))
                dao.insertMessage(AppMessage(
                    recipientName = doctor.name, recipientPhone = doctor.phone,
                    content = "د. ${doctor.name}\nتم تسجيل حالة جديدة:\nاسم المريض: $name\nالتشخيص: $diagnosis\nيرجى مراجعة ملف المريض.",
                    templateType = "DOCTOR_CASE"
                ))
            }
        }
        return Result.success(id)
    }

    suspend fun updatePatient(patient: Patient) {
        val old = dao.getPatientById(patient.id)
        dao.updatePatient(patient)
        dao.insertAuditLog(AuditLog(
            user = "المدير", action = "تعديل بيانات مريض",
            details = "تعديل ملف ${patient.name} (${patient.fileNumber})",
            previousData = "هاتف: ${old?.phone}, تشخيص: ${old?.diagnosis}",
            newData = "هاتف: ${patient.phone}, تشخيص: ${patient.diagnosis}"
        ))
    }

    suspend fun softDeletePatient(id: Long) {
        val patient = dao.getPatientById(id)
        dao.softDeletePatient(id)
        dao.insertAuditLog(AuditLog(user = "المدير", action = "أرشفة ملف مريض", details = "تمت أرشفة ملف المريض ${patient?.name}"))
    }

    suspend fun bookAppointment(
        patientId: Long, doctorId: Long?, therapistId: Long?, departmentId: Long?, serviceId: Long?,
        date: String, timeSlot: String, notes: String, branchId: Long = 1
    ): Result<Long> {
        val patient = dao.getPatientById(patientId) ?: return Result.failure(IllegalArgumentException("المريض غير موجود"))
        if (doctorId != null && dao.checkDoctorAppointmentConflict(doctorId, date, timeSlot) != null) {
            return Result.failure(IllegalStateException("يوجد موعد محجوز مسبقاً لنفس الطبيب في هذا التوقيت ($timeSlot)، يرجى اختيار وقت آخر."))
        }
        if (therapistId != null && dao.checkTherapistAppointmentConflict(therapistId, date, timeSlot) != null) {
            return Result.failure(IllegalStateException("يوجد موعد محجوز مسبقاً لنفس المعالج في هذا التوقيت ($timeSlot)، يرجى اختيار وقت آخر."))
        }
        val uniqueSuffix = System.currentTimeMillis().toString()
        val apptNumber = "APT-$uniqueSuffix"
        val id = dao.insertAppointment(Appointment(
            appointmentNumber = apptNumber, patientId = patientId, doctorId = doctorId,
            therapistId = therapistId, departmentId = departmentId, serviceId = serviceId,
            date = date, timeSlot = timeSlot, status = "محجوز", notes = notes, branchId = branchId
        ))
        val doctor = doctorId?.let { dao.getDoctorById(it) }
        dao.insertNotification(AppNotification(
            title = "حجز موعد جديد", message = "موعد للمريض ${patient.name} بتاريخ $date الساعة $timeSlot.",
            type = "موعد", relatedId = id
        ))
        dao.insertMessage(AppMessage(
            recipientName = patient.name, recipientPhone = patient.phone,
            content = "مرحبًا ${patient.name}\nتم حجز موعدكم بنجاح في مركز وسيم الطبي.\nالتاريخ: $date\nالوقت: $timeSlot\nالطبيب: ${doctor?.name ?: "الاستشاري المناوب"}\nنتمنى لكم دوام الصحة والعافية.",
            templateType = "APPOINTMENT"
        ))
        dao.insertAuditLog(AuditLog(
            user = "الاستقبال", action = "حجز موعد",
            details = "حجز موعد رقم $apptNumber للمريض ${patient.name} بتوقيت $date $timeSlot"
        ))
        return Result.success(id)
    }

    suspend fun updateAppointmentStatus(appointmentId: Long, newStatus: String) { /* handled by UI-specific flow */ }

    suspend fun attendSession(sessionId: Long): Result<String> {
        val session = dao.getSessionById(sessionId) ?: return Result.failure(IllegalArgumentException("الجلسة غير موجودة"))
        if (session.status == "حضر" || session.isDeductedFromPackage) return Result.failure(IllegalStateException("تم تسجيل الحضور مسبقاً لهذه الجلسة ولن يتم الخصم مرة أخرى."))
        val patient = dao.getPatientById(session.patientId) ?: return Result.failure(IllegalArgumentException("المريض غير موجود"))
        val now = System.currentTimeMillis()
        dao.updateSession(session.copy(status = "حضر", isDeductedFromPackage = true, attendedAt = now))
        var deductionMessage = "تم تسجيل حضور الجلسة بنجاح."
        val pkg = session.packageId?.let { dao.getPackageById(it) } ?: dao.getActivePackageForPatient(patient.id)
        if (pkg != null && pkg.remainingSessions > 0) {
            val newUsed = pkg.usedSessions + 1
            val newRemaining = pkg.remainingSessions - 1
            dao.updatePackage(pkg.copy(usedSessions = newUsed, remainingSessions = newRemaining, status = if (newRemaining == 0) "مكتملة" else "نشطة"))
            dao.insertPackageSession(PackageSession(packageId = pkg.id, patientId = patient.id, sessionId = session.id, deductedAt = now, reason = "حضور جلسة علاج طبيعي (${session.sessionNumber})", sessionsDeducted = 1, remainingAfter = newRemaining))
            val notificationText = "تم تسجيل حضور المريض ${patient.name}. تم خصم جلسة واحدة. المتبقي: $newRemaining جلسات."
            dao.insertNotification(AppNotification(title = "خصم جلسة من الباقة", message = notificationText, type = "خصم باقة", relatedId = session.id))
            dao.insertMessage(AppMessage(recipientName = patient.name, recipientPhone = patient.phone, content = "مرحبًا ${patient.name}\n\nتم تسجيل حضوركم في جلسة العلاج الطبيعي اليوم.\nتم خصم جلسة واحدة من الباقة.\nالجلسات المستخدمة: $newUsed\nالجلسات المتبقية: $newRemaining\nالباقة: ${pkg.packageName}\n\nنتمنى لكم الشفاء والعافية.", templateType = "SESSION_DEDUCTION"))
            if (newRemaining in 1..3) dao.insertNotification(AppNotification(title = if (newRemaining == 1) "تنبيه مهم: جلسة أخيرة" else "تنبيه قرب انتهاء الباقة", message = "تبقى للمريض ${patient.name} $newRemaining جلسات فقط في باقته.", type = "تنبيه باقة", relatedId = pkg.id))
            if (newRemaining == 0) dao.insertNotification(AppNotification(title = "انتهاء الباقة", message = "انتهت جميع جلسات باقة المريض ${patient.name}.", type = "تنبيه باقة", relatedId = pkg.id))
            deductionMessage = "تم تسجيل الحضور وخصم جلسة من الباقة. المتبقي: $newRemaining جلسات."
        }
        dao.insertAuditLog(AuditLog(user = "الاستقبال / المعالج", action = "تسجيل حضور جلسة", details = "جلسة رقم ${session.sessionNumber} للمريض ${patient.name} - $deductionMessage"))
        return Result.success(deductionMessage)
    }
}
