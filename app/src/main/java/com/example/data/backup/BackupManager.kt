package com.example.data.backup

import androidx.room.withTransaction
import com.example.data.database.WaseemDatabase
import com.example.data.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

data class BackupPayload(
    val formatVersion: Int = 1,
    val app: String = "WaseemMedicalPro",
    val exportedAt: Long = System.currentTimeMillis(),
    val patients: List<Patient> = emptyList(),
    val doctors: List<Doctor> = emptyList(),
    val therapists: List<Therapist> = emptyList(),
    val departments: List<Department> = emptyList(),
    val services: List<MedicalService> = emptyList(),
    val diagnoses: List<DiagnosisItem> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val packages: List<PatientPackage> = emptyList(),
    val packageSessions: List<PackageSession> = emptyList(),
    val sessions: List<ClinicSession> = emptyList(),
    val receipts: List<ReceiptVoucher> = emptyList(),
    val expenses: List<ExpenseVoucher> = emptyList(),
    val employees: List<Employee> = emptyList(),
    val deductions: List<SalaryDeduction> = emptyList(),
    val inventory: List<InventoryItem> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val messages: List<AppMessage> = emptyList(),
    val templates: List<MessageTemplate> = emptyList(),
    val auditLogs: List<AuditLog> = emptyList(),
    val licenses: List<AppLicense> = emptyList(),
    val settings: List<CenterSettings> = emptyList(),
    val branches: List<Branch> = emptyList(),
    val users: List<AppUser> = emptyList()
)

class BackupManager(private val database: WaseemDatabase) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(BackupPayload::class.java)

    suspend fun exportJson(): String {
        val dao = database.clinicDao()
        val payload = BackupPayload(
            patients = dao.getAllPatientsSnapshot(),
            doctors = dao.getAllDoctorsSnapshot(),
            therapists = dao.getAllTherapistsSnapshot(),
            departments = dao.getAllDepartmentsSnapshot(),
            services = dao.getAllServicesSnapshot(),
            diagnoses = dao.getAllDiagnosesSnapshot(),
            appointments = dao.getAllAppointmentsSnapshot(),
            packages = dao.getAllPackagesSnapshot(),
            packageSessions = dao.getAllPackageSessionsSnapshot(),
            sessions = dao.getAllSessionsSnapshot(),
            receipts = dao.getAllReceiptsSnapshot(),
            expenses = dao.getAllExpensesSnapshot(),
            employees = dao.getAllEmployeesSnapshot(),
            deductions = dao.getAllDeductionsSnapshot(),
            inventory = dao.getAllInventorySnapshot(),
            notifications = dao.getAllNotificationsSnapshot(),
            messages = dao.getAllMessagesSnapshot(),
            templates = dao.getAllTemplatesSnapshot(),
            auditLogs = dao.getAllAuditLogsSnapshot(),
            licenses = dao.getAllLicensesSnapshot(),
            settings = dao.getAllSettingsSnapshot(),
            branches = dao.getAllBranchesDirect(),
            users = dao.getAllUsersDirect()
        )
        return adapter.toJson(payload)
    }

    suspend fun restoreJson(json: String): Result<Unit> {
        val payload = try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("ملف النسخة الاحتياطية غير صالح أو تالف", e))
        } ?: return Result.failure(IllegalArgumentException("ملف النسخة الاحتياطية فارغ"))

        if (payload.formatVersion != 1) {
            return Result.failure(IllegalArgumentException("إصدار النسخة الاحتياطية غير مدعوم"))
        }
        if (payload.app != "WaseemMedicalPro") {
            return Result.failure(IllegalArgumentException("هذه النسخة ليست لنظام وسيم الطبي PRO"))
        }

        // Never replace a live database with a backup that cannot restore
        // the minimum administrative configuration required to operate the app.
        if (payload.branches.isEmpty() || payload.users.isEmpty() || payload.settings.isEmpty()) {
            return Result.failure(
                IllegalArgumentException("النسخة الاحتياطية ناقصة: يجب أن تحتوي على الفروع والمستخدمين وإعدادات المركز")
            )
        }
        if (payload.users.none { it.isSystemOwner || it.role == "SUPER_ADMIN" }) {
            return Result.failure(
                IllegalArgumentException("النسخة الاحتياطية لا تحتوي على حساب مالك/مدير عام صالح")
            )
        }
        val branchIds = payload.branches.map { it.id }.toSet()
        if (branchIds.size != payload.branches.size) {
            return Result.failure(IllegalArgumentException("النسخة الاحتياطية تحتوي على فروع مكررة"))
        }
        if (payload.patients.any { it.branchId !in branchIds }) {
            return Result.failure(IllegalArgumentException("توجد ملفات مرضى مرتبطة بفروع غير موجودة في النسخة الاحتياطية"))
        }

        return try {
            database.withTransaction {
                val dao = database.clinicDao()

                dao.clearPackageSessions()
                dao.clearSessions()
                dao.clearAppointments()
                dao.clearReceipts()
                dao.clearExpenses()
                dao.clearDeductions()
                dao.clearPackages()
                dao.clearPatients()
                dao.clearInventory()
                dao.clearEmployees()
                dao.clearNotifications()
                dao.clearMessages()
                dao.clearTemplates()
                dao.clearAuditLogs()
                dao.clearServices()
                dao.clearDiagnoses()
                dao.clearDepartments()
                dao.clearTherapists()
                dao.clearDoctors()
                dao.clearBranches()
                dao.clearUsers()
                dao.clearSettings()
                dao.clearLicenses()

                payload.branches.forEach { dao.insertBranch(it) }
                payload.users.forEach { dao.insertUser(it) }
                payload.settings.forEach { dao.updateSettings(it) }
                payload.licenses.forEach { dao.setLicense(it) }
                payload.departments.forEach { dao.insertDepartment(it) }
                payload.doctors.forEach { dao.insertDoctor(it) }
                payload.therapists.forEach { dao.insertTherapist(it) }
                payload.services.forEach { dao.insertService(it) }
                payload.diagnoses.forEach { dao.insertDiagnosis(it) }
                payload.patients.forEach { dao.insertPatient(it) }
                payload.packages.forEach { dao.insertPackage(it) }
                payload.appointments.forEach { dao.insertAppointment(it) }
                payload.sessions.forEach { dao.insertSession(it) }
                payload.packageSessions.forEach { dao.insertPackageSession(it) }
                payload.receipts.forEach { dao.insertReceipt(it) }
                payload.expenses.forEach { dao.insertExpense(it) }
                payload.employees.forEach { dao.insertEmployee(it) }
                payload.deductions.forEach { dao.insertDeduction(it) }
                payload.inventory.forEach { dao.insertInventoryItem(it) }
                payload.notifications.forEach { dao.insertNotification(it) }
                payload.messages.forEach { dao.insertMessage(it) }
                payload.templates.forEach { dao.insertTemplate(it) }
                payload.auditLogs.forEach { dao.insertAuditLog(it) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(IllegalStateException("تعذر استعادة النسخة الاحتياطية: ${e.message}", e))
        }
    }
}
