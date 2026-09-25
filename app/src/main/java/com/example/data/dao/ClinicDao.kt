package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface ClinicDao {

    // --- PATIENTS ---
    @Query("SELECT * FROM patients WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Long): Patient?

    @Query("SELECT * FROM patients WHERE (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR fileNumber LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY name ASC")
    fun searchPatients(query: String): Flow<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Query("UPDATE patients SET isArchived = 1 WHERE id = :id")
    suspend fun softDeletePatient(id: Long)

    @Query("SELECT COUNT(*) FROM patients WHERE isArchived = 0")
    fun getPatientsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientsCountDirect(): Int

    // --- DOCTORS & THERAPISTS ---
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors WHERE id = :id")
    suspend fun getDoctorById(id: Long): Doctor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor): Long

    @Update
    suspend fun updateDoctor(doctor: Doctor)

    @Query("DELETE FROM doctors WHERE id = :id")
    suspend fun deleteDoctor(id: Long)

    @Query("SELECT * FROM therapists ORDER BY name ASC")
    fun getAllTherapists(): Flow<List<Therapist>>

    @Query("SELECT * FROM therapists WHERE id = :id")
    suspend fun getTherapistById(id: Long): Therapist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTherapist(therapist: Therapist): Long

    @Update
    suspend fun updateTherapist(therapist: Therapist)

    @Query("DELETE FROM therapists WHERE id = :id")
    suspend fun deleteTherapist(id: Long)

    // --- DEPARTMENTS & SERVICES ---
    @Query("SELECT * FROM departments ORDER BY name ASC")
    fun getAllDepartments(): Flow<List<Department>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: Department): Long

    @Update
    suspend fun updateDepartment(department: Department)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartment(id: Long)

    @Query("SELECT * FROM services ORDER BY name ASC")
    fun getAllServices(): Flow<List<MedicalService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: MedicalService): Long

    @Update
    suspend fun updateService(service: MedicalService)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: Long)

    // --- DIAGNOSES ---
    @Query("SELECT * FROM diagnoses ORDER BY name ASC")
    fun getAllDiagnoses(): Flow<List<DiagnosisItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(diagnosis: DiagnosisItem): Long

    // --- APPOINTMENTS ---
    @Query("SELECT * FROM appointments ORDER BY date DESC, timeSlot ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date = :date ORDER BY timeSlot ASC")
    fun getAppointmentsByDate(date: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND date = :date AND timeSlot = :timeSlot AND status != 'أُلغي' LIMIT 1")
    suspend fun checkDoctorAppointmentConflict(doctorId: Long, date: String, timeSlot: String): Appointment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointment(id: Long)

    // --- PACKAGES ---
    @Query("SELECT * FROM packages ORDER BY id DESC")
    fun getAllPackages(): Flow<List<PatientPackage>>

    @Query("SELECT * FROM packages WHERE patientId = :patientId ORDER BY id DESC")
    fun getPackagesByPatient(patientId: Long): Flow<List<PatientPackage>>

    @Query("SELECT * FROM packages WHERE patientId = :patientId AND status = 'نشطة' AND remainingSessions > 0 LIMIT 1")
    suspend fun getActivePackageForPatient(patientId: Long): PatientPackage?

    @Query("SELECT * FROM packages WHERE id = :id")
    suspend fun getPackageById(id: Long): PatientPackage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PatientPackage): Long

    @Update
    suspend fun updatePackage(pkg: PatientPackage)

    @Query("DELETE FROM packages WHERE id = :id")
    suspend fun deletePackage(id: Long)

    // --- PACKAGE SESSIONS (DEDUCTIONS) ---
    @Query("SELECT * FROM package_sessions WHERE packageId = :packageId ORDER BY deductedAt DESC")
    fun getPackageSessions(packageId: Long): Flow<List<PackageSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackageSession(deduction: PackageSession): Long

    // --- CLINIC SESSIONS ---
    @Query("SELECT * FROM sessions ORDER BY date DESC, time ASC")
    fun getAllSessions(): Flow<List<ClinicSession>>

    @Query("SELECT * FROM sessions WHERE date = :date ORDER BY time ASC")
    fun getSessionsByDate(date: String): Flow<List<ClinicSession>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): ClinicSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ClinicSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<ClinicSession>): List<Long>

    @Update
    suspend fun updateSession(session: ClinicSession)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)

    // --- RECEIPTS & EXPENSES ---
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<ReceiptVoucher>>

    @Query("SELECT * FROM receipts WHERE patientId = :patientId ORDER BY id DESC")
    fun getReceiptsByPatient(patientId: Long): Flow<List<ReceiptVoucher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptVoucher): Long

    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseVoucher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseVoucher): Long

    // --- EMPLOYEES & SALARIES ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Long): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployee(id: Long)

    @Query("SELECT * FROM salary_deductions ORDER BY date DESC")
    fun getAllDeductions(): Flow<List<SalaryDeduction>>

    @Query("SELECT * FROM salary_deductions WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getDeductionsByEmployee(employeeId: Long): Flow<List<SalaryDeduction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeduction(deduction: SalaryDeduction): Long

    @Query("DELETE FROM salary_deductions WHERE id = :id")
    suspend fun deleteDeduction(id: Long)

    // --- INVENTORY ---
    @Query("SELECT * FROM inventory ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem): Long

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Query("DELETE FROM inventory WHERE id = :id")
    suspend fun deleteInventoryItem(id: Long)

    // --- NOTIFICATIONS ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()

    // --- MESSAGES & TEMPLATES ---
    @Query("SELECT * FROM messages ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<AppMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AppMessage): Long

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: Long, status: String)

    @Query("SELECT * FROM message_templates")
    fun getAllTemplates(): Flow<List<MessageTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplate): Long

    @Update
    suspend fun updateTemplate(template: MessageTemplate)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog): Long

    // --- LICENSE & SETTINGS ---
    @Query("SELECT * FROM licenses WHERE id = 1")
    fun getLicense(): Flow<AppLicense?>

    @Query("SELECT * FROM licenses WHERE id = 1")
    suspend fun getLicenseDirect(): AppLicense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLicense(license: AppLicense)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<CenterSettings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsDirect(): CenterSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSettings(settings: CenterSettings)

    // Clear all for restore
    @Query("DELETE FROM patients")
    suspend fun clearPatients()
    @Query("DELETE FROM appointments")
    suspend fun clearAppointments()
    @Query("DELETE FROM sessions")
    suspend fun clearSessions()
    @Query("DELETE FROM packages")
    suspend fun clearPackages()
    @Query("DELETE FROM receipts")
    suspend fun clearReceipts()
    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    // --- BRANCHES ---
    @Query("SELECT * FROM branches ORDER BY isMainBranch DESC, name ASC")
    fun getAllBranches(): Flow<List<Branch>>

    @Query("SELECT * FROM branches WHERE id = :id")
    suspend fun getBranchById(id: Long): Branch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: Branch): Long

    @Update
    suspend fun updateBranch(branch: Branch)

    @Query("DELETE FROM branches WHERE id = :id AND isMainBranch = 0")
    suspend fun deleteBranch(id: Long)

    @Query("SELECT COUNT(*) FROM branches")
    suspend fun getBranchesCountDirect(): Int

    @Query("SELECT * FROM branches")
    suspend fun getAllBranchesDirect(): List<Branch>

    // --- USERS & AUTHENTICATION ---
    @Query("SELECT * FROM users ORDER BY isSystemOwner DESC, id ASC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersDirect(): List<AppUser>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Long): AppUser?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AppUser?

    @Query("SELECT * FROM users WHERE username = :username AND passwordHash = :password LIMIT 1")
    suspend fun authenticate(username: String, password: String): AppUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser): Long

    @Update
    suspend fun updateUser(user: AppUser)

    @Query("UPDATE users SET passwordHash = :newPassword WHERE id = :userId")
    suspend fun updateUserPassword(userId: Long, newPassword: String)

    @Query("DELETE FROM users WHERE id = :id AND isSystemOwner = 0")
    suspend fun deleteUser(id: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCountDirect(): Int

    // Reset Operational Data (Preserves system owner, license, settings, branches)
    @Query("DELETE FROM package_sessions")
    suspend fun clearPackageSessions()

    @Query("DELETE FROM salary_deductions")
    suspend fun clearDeductions()

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()
}
