package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private object BootstrapPasswordHasher {
    private const val PREFIX = "PBKDF2_SHA1:"
    private const val ITERATIONS = 120_000
    private const val SALT_BYTES = 16
    private const val KEY_LENGTH = 256

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES)
        java.security.SecureRandom().nextBytes(salt)
        val spec = javax.crypto.spec.PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return try {
            val derived = javax.crypto.SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1")
                .generateSecret(spec)
                .encoded
            PREFIX + ITERATIONS + ":" + salt.toHexString() + ":" + derived.toHexString()
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
}

@Database(
    entities = [
        Patient::class,
        Doctor::class,
        Therapist::class,
        Department::class,
        MedicalService::class,
        DiagnosisItem::class,
        Appointment::class,
        PatientPackage::class,
        PackageSession::class,
        ClinicSession::class,
        ReceiptVoucher::class,
        ExpenseVoucher::class,
        Employee::class,
        SalaryDeduction::class,
        InventoryItem::class,
        AppNotification::class,
        AppMessage::class,
        MessageTemplate::class,
        AuditLog::class,
        AppLicense::class,
        CenterSettings::class,
        Branch::class,
        AppUser::class
    ],
    version = 4,
    exportSchema = false
)
abstract class WaseemDatabase : RoomDatabase() {

    abstract fun clinicDao(): ClinicDao

    companion object {
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE inventory ADD COLUMN barcode TEXT NOT NULL DEFAULT ''")
            }
        }

        @Volatile
        private var INSTANCE: WaseemDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WaseemDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaseemDatabase::class.java,
                    "waseem_medical_pro.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .build()
                INSTANCE = instance

                // Initialize only essential operational configuration once the database is ready.
                scope.launch(Dispatchers.IO) {
                    try {
                        ensureEssentialData(instance.clinicDao())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                instance
            }
        }

        suspend fun ensureEssentialData(dao: ClinicDao) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val todayStr = dateFormat.format(Date(now))

            // 1. Ensure Branches exist
            if (dao.getBranchesCountDirect() == 0) {
                dao.insertBranch(
                    Branch(
                        id = 1,
                        name = "الفرع الرئيسي - صنعاء",
                        city = "صنعاء",
                        address = "شارع الستين الغربي - بجوار المستشفى الاستشاري",
                        phone = "772357240",
                        isMainBranch = true
                    )
                )
                dao.insertBranch(
                    Branch(
                        id = 2,
                        name = "فرع صنعاء - حدة",
                        city = "صنعاء",
                        address = "شارع حدة العام - جولة الرويشان",
                        phone = "772357240"
                    )
                )
                dao.insertBranch(
                    Branch(
                        id = 3,
                        name = "فرع إب - الدائري",
                        city = "إب",
                        address = "الشارع الدائري الغربي",
                        phone = "772357240"
                    )
                )
                dao.insertBranch(
                    Branch(
                        id = 4,
                        name = "فرع تعز - شارع جمال",
                        city = "تعز",
                        address = "شارع جمال عبد الناصر",
                        phone = "772357240"
                    )
                )
            }

            // 2. Ensure Users exist (admin, Waseem, mohammed, ahmed)
            if (dao.getUserByUsername("admin") == null) {
                dao.insertUser(
                    AppUser(
                        username = "admin",
                        passwordHash = BootstrapPasswordHasher.hash("admin"),
                        fullName = "مدير المركز (إدارة عامة)",
                        role = "ADMIN",
                        branchId = 1,
                        phone = "772357240",
                        isSystemOwner = false,
                        permPatients = true,
                        permAppointments = true,
                        permSessions = true,
                        permDoctors = true,
                        permDepartments = true,
                        permFinancial = true,
                        permPayroll = true,
                        permInventory = true,
                        permReports = true,
                        permSettings = true,
                        permUsers = true,
                        permBackup = true,
                        permLicense = true
                    )
                )
            }

            if (dao.getUserByUsername("Waseem") == null) {
                dao.insertUser(
                    AppUser(
                        username = "Waseem",
                        passwordHash = BootstrapPasswordHasher.hash("W772357240"),
                        fullName = "م. وسيم الفرح (مالك ومطور النظام)",
                        role = "SUPER_ADMIN",
                        branchId = null,
                        phone = "772357240",
                        isSystemOwner = true,
                        permPatients = true,
                        permAppointments = true,
                        permSessions = true,
                        permDoctors = true,
                        permDepartments = true,
                        permFinancial = true,
                        permPayroll = true,
                        permInventory = true,
                        permReports = true,
                        permSettings = true,
                        permUsers = true,
                        permBackup = true,
                        permLicense = true
                    )
                )
            }

            if (dao.getUserByUsername("mohammed") == null) {
                dao.insertUser(
                    AppUser(
                        username = "mohammed",
                        passwordHash = BootstrapPasswordHasher.hash("1234"),
                        fullName = "محمد أحمد — استقبال",
                        role = "RECEPTIONIST",
                        branchId = 1,
                        phone = "771122334",
                        isSystemOwner = false,
                        permPatients = true,
                        permAppointments = true,
                        permSessions = true,
                        permDoctors = true,
                        permDepartments = true,
                        permFinancial = false,
                        permPayroll = false,
                        permInventory = false,
                        permReports = false,
                        permSettings = false,
                        permUsers = false,
                        permBackup = false,
                        permLicense = false
                    )
                )
            }

            if (dao.getUserByUsername("ahmed") == null) {
                dao.insertUser(
                    AppUser(
                        username = "ahmed",
                        passwordHash = BootstrapPasswordHasher.hash("1234"),
                        fullName = "أحمد علي — محاسب",
                        role = "ACCOUNTANT",
                        branchId = 1,
                        phone = "775566778",
                        isSystemOwner = false,
                        permPatients = true,
                        permAppointments = false,
                        permSessions = false,
                        permDoctors = false,
                        permDepartments = false,
                        permFinancial = true,
                        permPayroll = true,
                        permInventory = true,
                        permReports = true,
                        permSettings = false,
                        permUsers = false,
                        permBackup = false,
                        permLicense = false
                    )
                )
            }

            // 3. Ensure Settings and License exist
            if (dao.getSettingsDirect() == null) {
                dao.updateSettings(
                    CenterSettings(
                        id = 1,
                        centerName = "مركز وسيم للعلاج الطبيعي والتأهيل",
                        developerName = "وسيم الفرح",
                        developerPhone = "772357240",
                        address = "اليمن - صنعاء - شارع الستين الغربي",
                        phone = "772357240",
                        whatsapp = "772357240",
                        currency = "ر.ي",
                        autoSyncGoogleDrive = false,
                        syncIntervalHours = 6,
                        isDarkMode = false,
                        enableDoctorAlerts = true
                    )
                )
            }

            if (dao.getLicenseDirect() == null) {
                val thirtyDaysMillis = 30L * 24L * 60L * 60L * 1000L
                dao.setLicense(
                    AppLicense(
                        id = 1,
                        customerName = "مركز وسيم الطبي والتأهيلي",
                        centerName = "نظام وسيم الطبي PRO",
                        licenseNumber = "WMP-TRIAL-" + UUID.randomUUID().toString().take(8).uppercase(),
                        startDate = now,
                        endDate = now + thirtyDaysMillis,
                        licenseType = "TRIAL",
                        status = "ACTIVE",
                        installationId = "INST-" + UUID.randomUUID().toString().take(12).uppercase()
                    )
                )
            }

            // Do not create sample patients, appointments, financial records, or inventory automatically.
            // Production data must be entered or restored explicitly by the center owner.
        }

        private suspend fun populateInitialData(dao: ClinicDao) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val todayStr = dateFormat.format(Date(now))

            // 1. Branches & Organization Hierarchy
            val branchMain = dao.insertBranch(
                Branch(
                    id = 1,
                    name = "الفرع الرئيسي - صنعاء",
                    city = "صنعاء",
                    address = "شارع الستين الغربي - بجوار المستشفى الاستشاري",
                    phone = "772357240",
                    isMainBranch = true
                )
            )
            val branchSanaa = dao.insertBranch(
                Branch(
                    id = 2,
                    name = "فرع صنعاء - حدة",
                    city = "صنعاء",
                    address = "شارع حدة العام - جولة الرويشان",
                    phone = "772357240"
                )
            )
            val branchIbb = dao.insertBranch(
                Branch(
                    id = 3,
                    name = "فرع إب - الدائري",
                    city = "إب",
                    address = "الشارع الدائري الغربي",
                    phone = "772357240"
                )
            )
            val branchTaiz = dao.insertBranch(
                Branch(
                    id = 4,
                    name = "فرع تعز - شارع جمال",
                    city = "تعز",
                    address = "شارع جمال عبد الناصر",
                    phone = "772357240"
                )
            )

            // 2. Users & Roles & Permissions
            // Super Admin / Owner: Waseem
            dao.insertUser(
                AppUser(
                    username = "Waseem",
                    passwordHash = BootstrapPasswordHasher.hash("W772357240"),
                    fullName = "م. وسيم الفرح (مالك ومطور النظام)",
                    role = "SUPER_ADMIN",
                    branchId = null, // All branches
                    phone = "772357240",
                    isSystemOwner = true,
                    permPatients = true,
                    permAppointments = true,
                    permSessions = true,
                    permDoctors = true,
                    permDepartments = true,
                    permFinancial = true,
                    permPayroll = true,
                    permInventory = true,
                    permReports = true,
                    permSettings = true,
                    permUsers = true,
                    permBackup = true,
                    permLicense = true
                )
            )

            // Trial Admin: admin / admin
            dao.insertUser(
                AppUser(
                    username = "admin",
                    passwordHash = BootstrapPasswordHasher.hash("admin"),
                    fullName = "مدير المركز (إدارة عامة)",
                    role = "ADMIN",
                    branchId = 1,
                    phone = "772357240",
                    isSystemOwner = false,
                    permPatients = true,
                    permAppointments = true,
                    permSessions = true,
                    permDoctors = true,
                    permDepartments = true,
                    permFinancial = true,
                    permPayroll = true,
                    permInventory = true,
                    permReports = true,
                    permSettings = true,
                    permUsers = true,
                    permBackup = true,
                    permLicense = true
                )
            )

            // Receptionist: Mohammed Ahmed
            dao.insertUser(
                AppUser(
                    username = "mohammed",
                    passwordHash = BootstrapPasswordHasher.hash("1234"),
                    fullName = "محمد أحمد — استقبال",
                    role = "RECEPTIONIST",
                    branchId = 1,
                    phone = "771122334",
                    isSystemOwner = false,
                    permPatients = true,
                    permAppointments = true,
                    permSessions = true,
                    permDoctors = true,
                    permDepartments = true,
                    permFinancial = false,
                    permPayroll = false,
                    permInventory = false,
                    permReports = false,
                    permSettings = false,
                    permUsers = false,
                    permBackup = false,
                    permLicense = false
                )
            )

            // Accountant: Ahmed Ali
            dao.insertUser(
                AppUser(
                    username = "ahmed",
                    passwordHash = BootstrapPasswordHasher.hash("1234"),
                    fullName = "أحمد علي — محاسب",
                    role = "ACCOUNTANT",
                    branchId = 1,
                    phone = "775566778",
                    isSystemOwner = false,
                    permPatients = true,
                    permAppointments = false,
                    permSessions = false,
                    permDoctors = false,
                    permDepartments = false,
                    permFinancial = true,
                    permPayroll = true,
                    permInventory = true,
                    permReports = true,
                    permSettings = false,
                    permUsers = false,
                    permBackup = false,
                    permLicense = false
                )
            )

            // 3. Settings & License (30-day trial)
            val thirtyDaysMillis = 30L * 24L * 60L * 60L * 1000L
            val license = AppLicense(
                id = 1,
                customerName = "مركز وسيم الطبي والتأهيلي",
                centerName = "نظام وسيم الطبي PRO",
                licenseNumber = "WMP-TRIAL-" + UUID.randomUUID().toString().take(8).uppercase(),
                startDate = now,
                endDate = now + thirtyDaysMillis,
                licenseType = "TRIAL",
                status = "ACTIVE",
                installationId = "INST-" + UUID.randomUUID().toString().take(12).uppercase()
            )
            dao.setLicense(license)

            val settings = CenterSettings(
                id = 1,
                centerName = "مركز وسيم للعلاج الطبيعي والتأهيل",
                developerName = "وسيم الفرح",
                developerPhone = "772357240",
                address = "اليمن - صنعاء - شارع الستين الغربي",
                phone = "772357240",
                whatsapp = "772357240",
                currency = "ر.ي",
                autoSyncGoogleDrive = false,
                syncIntervalHours = 6,
                isDarkMode = false,
                enableDoctorAlerts = true
            )
            dao.updateSettings(settings)

            // 2. Departments
            val dept1 = dao.insertDepartment(Department(name = "العلاج الطبيعي والتأهيل", description = "جلسات تأهيل حركي وتمارين وتقوية"))
            val dept2 = dao.insertDepartment(Department(name = "العظام والمفاصل والعمود الفقري", description = "علاج الانزلاق الغضروفي والخشونة"))
            val dept3 = dao.insertDepartment(Department(name = "المخ والأعصاب والتأهيل العصبي", description = "إعادة تأهيل الجلطات والشلل الدماغي"))
            val dept4 = dao.insertDepartment(Department(name = "علاج وتأهيل الأطفال", description = "تأهيل الشلل الدماغي وتأخر المشي"))

            // 3. Doctors & Therapists
            val doc1 = dao.insertDoctor(
                Doctor(
                    name = "د. وسيم الفرح",
                    specialization = "استشاري علاج طبيعي وتأهيل حركي",
                    phone = "774486588",
                    departmentId = dept1,
                    workingDays = "السبت - الخميس",
                    workingHours = "09:00 ص - 01:00 م | 04:00 ع - 09:00 م",
                    servicePrice = 10000.0,
                    commissionRate = 30.0
                )
            )
            val doc2 = dao.insertDoctor(
                Doctor(
                    name = "د. محمد علي الحكيمي",
                    specialization = "استشاري جراحة العظام والمفاصل",
                    phone = "771234567",
                    departmentId = dept2,
                    workingDays = "السبت - الأربعاء",
                    workingHours = "10:00 ص - 02:00 م",
                    servicePrice = 8000.0,
                    commissionRate = 25.0
                )
            )
            val doc3 = dao.insertDoctor(
                Doctor(
                    name = "د. سارة أحمد الشامي",
                    specialization = "أخصائية تأهيل المخ والأعصاب",
                    phone = "773456789",
                    departmentId = dept3,
                    workingDays = "الأحد - الخميس",
                    workingHours = "04:00 ع - 08:30 م",
                    servicePrice = 9000.0,
                    commissionRate = 25.0
                )
            )

            val ther1 = dao.insertTherapist(
                Therapist(
                    name = "أ. خالد النجار",
                    specialization = "أخصائي علاج طبيعي وإصابات رياضية",
                    phone = "770112233",
                    departmentId = dept1,
                    workingDays = "يومياً عدا الجمعة",
                    workingHours = "09:00 ص - 05:00 م",
                    commissionRate = 20.0
                )
            )
            val ther2 = dao.insertTherapist(
                Therapist(
                    name = "أ. أمل القدسي",
                    specialization = "أخصائية علاج وظيفي وتأهيل حركي",
                    phone = "772334455",
                    departmentId = dept1,
                    workingDays = "السبت - الخميس",
                    workingHours = "09:00 ص - 04:00 م",
                    commissionRate = 20.0
                )
            )

            // 4. Services
            val srv1 = dao.insertService(MedicalService(name = "جلسة علاج طبيعي متكاملة", departmentId = dept1, price = 8000.0, durationMinutes = 45, doctorId = doc1))
            val srv2 = dao.insertService(MedicalService(name = "جلسة تأهيل حركي وتمارين", departmentId = dept1, price = 10000.0, durationMinutes = 60, doctorId = doc1))
            val srv3 = dao.insertService(MedicalService(name = "جلسة تحفيز كهربائي وليزر", departmentId = dept1, price = 6000.0, durationMinutes = 30, doctorId = doc1))
            val srv4 = dao.insertService(MedicalService(name = "جلسة تأهيل عصبي مكثف", departmentId = dept3, price = 12000.0, durationMinutes = 60, doctorId = doc3))
            val srv5 = dao.insertService(MedicalService(name = "كشف واستشارة استشاري", departmentId = dept2, price = 5000.0, durationMinutes = 30, doctorId = doc2))

            // 5. Diagnoses
            dao.insertDiagnosis(DiagnosisItem(name = "انزلاق غضروفي قطني وآلام أسفل الظهر", category = "عمود فقري", description = "ضغط جذور الأعصاب القطنية"))
            dao.insertDiagnosis(DiagnosisItem(name = "خشونة مفصل الركبة وتآكل الغضاريف", category = "مفاصل", description = "احتكاك في مفصل الركبة من الدرجة الثانية"))
            dao.insertDiagnosis(DiagnosisItem(name = "إعادة تأهيل بعد الجلطة الدماغية (شلل نصفي)", category = "أعصاب", description = "ضعف عضلي وتشنج جانبي"))
            dao.insertDiagnosis(DiagnosisItem(name = "التهاب وتمزق أوتار الكتف", category = "عضلات", description = "صعوبة رفع الذراع ومحدودية الحركة"))
            dao.insertDiagnosis(DiagnosisItem(name = "شلل العصب الوجهي السابع", category = "أعصاب", description = "ارتخاء عضلات جانب الوجه"))

            // 6. Patients
            val pat1 = dao.insertPatient(
                Patient(
                    fileNumber = "WM-1001",
                    name = "أحمد محمد الحاشدي",
                    phone = "0771122334",
                    gender = "ذكر",
                    dateOfBirth = "1988-05-14",
                    address = "صنعاء - مذبح",
                    maritalStatus = "متزوج",
                    profession = "موظف حكومي",
                    referralSource = "تحويل طبيب",
                    doctorId = doc1,
                    therapistId = ther1,
                    departmentId = dept1,
                    diagnosis = "انزلاق غضروفي قطني وآلام أسفل الظهر",
                    complaint = "ألم حاد يمتد إلى الساق اليمنى وصعوبة المشي",
                    notes = "يفضل المواعيد الصباحية. استجاب للجلسة الأولى بشكل ممتاز.",
                    balanceDue = 10000.0
                )
            )

            val pat2 = dao.insertPatient(
                Patient(
                    fileNumber = "WM-1002",
                    name = "محمد علي الصنعاني",
                    phone = "0772233445",
                    gender = "ذكر",
                    dateOfBirth = "1972-11-20",
                    address = "صنعاء - حدة",
                    maritalStatus = "متزوج",
                    profession = "أعمال حرة",
                    referralSource = "صديق",
                    doctorId = doc1,
                    therapistId = ther1,
                    departmentId = dept1,
                    diagnosis = "خشونة مفصل الركبة وتآكل الغضاريف",
                    complaint = "طقطقة وألم عند صعود الدرج وثني الركبة",
                    notes = "باقة 6 جلسات - متبقي جلستان فقط",
                    balanceDue = 5000.0
                )
            )

            val pat3 = dao.insertPatient(
                Patient(
                    fileNumber = "WM-1003",
                    name = "عبدالله أحمد الريمي",
                    phone = "0773344556",
                    gender = "ذكر",
                    dateOfBirth = "1965-02-10",
                    address = "صنعاء - الأصبحي",
                    maritalStatus = "متزوج",
                    profession = "متقاعد",
                    referralSource = "د. سارة الشامي",
                    doctorId = doc3,
                    therapistId = ther2,
                    departmentId = dept3,
                    diagnosis = "إعادة تأهيل بعد الجلطة الدماغية (شلل نصفي)",
                    complaint = "صعوبة حركة اليد والرجل اليسرى",
                    notes = "تبقت له جلسة واحدة في الباقة - يحتاج تجديد",
                    balanceDue = 0.0
                )
            )

            val pat4 = dao.insertPatient(
                Patient(
                    fileNumber = "WM-1004",
                    name = "فاطمة صالح الزبيري",
                    phone = "0774455667",
                    gender = "أنثى",
                    dateOfBirth = "1993-08-19",
                    address = "صنعاء - بغداد",
                    maritalStatus = "متزوجة",
                    profession = "معلمة",
                    referralSource = "مواقع التواصل",
                    doctorId = doc1,
                    therapistId = ther2,
                    departmentId = dept1,
                    diagnosis = "التهاب وتمزق أوتار الكتف",
                    complaint = "ألم مستمر ليلاً وصعوبة رفع الذراع",
                    notes = "جلسات علاج طبيعي وتمارين مدى حركي",
                    balanceDue = 8000.0
                )
            )

            val pat5 = dao.insertPatient(
                Patient(
                    fileNumber = "WM-1005",
                    name = "ياسر عبدالرحمن المطري",
                    phone = "0775566778",
                    gender = "ذكر",
                    dateOfBirth = "1997-03-25",
                    address = "صنعاء - شملان",
                    maritalStatus = "أعزب",
                    profession = "طالب جامعي",
                    referralSource = "عيادة د. الحكيمي",
                    doctorId = doc2,
                    therapistId = ther1,
                    departmentId = dept2,
                    diagnosis = "خشونة مفصل الركبة وتآكل الغضاريف",
                    complaint = "إصابة رياضية في الركبة اليمنى",
                    notes = "يحتاج جلسات تقوية العضلات الرباعية",
                    balanceDue = 0.0
                )
            )

            // 7. Packages
            // Package for pat1 (Ahmed): 10 sessions, 3 used, 7 remaining
            val pkg1 = dao.insertPackage(
                PatientPackage(
                    patientId = pat1,
                    packageName = "باقة علاج طبيعي وتأهيل شهرية",
                    price = 25000.0,
                    totalSessions = 10,
                    usedSessions = 3,
                    remainingSessions = 7,
                    startDate = todayStr,
                    endDate = dateFormat.format(Date(now + 30L * 24 * 60 * 60 * 1000)),
                    status = "نشطة",
                    notes = "جلسات 3 أيام أسبوعياً"
                )
            )

            // Package for pat2 (Mohamed): 6 sessions, 4 used, 2 remaining (Warning <= 3)
            val pkg2 = dao.insertPackage(
                PatientPackage(
                    patientId = pat2,
                    packageName = "باقة خشونة الركبة المكثفة",
                    price = 18000.0,
                    totalSessions = 6,
                    usedSessions = 4,
                    remainingSessions = 2,
                    startDate = todayStr,
                    endDate = dateFormat.format(Date(now + 20L * 24 * 60 * 60 * 1000)),
                    status = "نشطة",
                    notes = "تنبيه: متبقي جلستان فقط"
                )
            )

            // Package for pat3 (Abdullah): 12 sessions, 11 used, 1 remaining (Critical Warning = 1)
            val pkg3 = dao.insertPackage(
                PatientPackage(
                    patientId = pat3,
                    packageName = "باقة التأهيل العصبي المتقدم",
                    price = 40000.0,
                    totalSessions = 12,
                    usedSessions = 11,
                    remainingSessions = 1,
                    startDate = todayStr,
                    endDate = dateFormat.format(Date(now + 10L * 24 * 60 * 60 * 1000)),
                    status = "نشطة",
                    notes = "تنبيه حرج: متبقي جلسة واحدة فقط من الباقة"
                )
            )

            // 8. Appointments & Sessions for Today
            val appt1 = dao.insertAppointment(
                Appointment(
                    appointmentNumber = "APT-101",
                    patientId = pat1,
                    doctorId = doc1,
                    therapistId = ther1,
                    departmentId = dept1,
                    serviceId = srv1,
                    date = todayStr,
                    timeSlot = "09:30 ص",
                    status = "محجوز",
                    notes = "الجلسة الرابعة في باقة الظهر"
                )
            )
            dao.insertSession(
                ClinicSession(
                    sessionNumber = "SES-101",
                    patientId = pat1,
                    appointmentId = appt1,
                    packageId = pkg1,
                    doctorId = doc1,
                    therapistId = ther1,
                    serviceId = srv1,
                    date = todayStr,
                    time = "09:30 ص",
                    status = "مجدولة",
                    isDeductedFromPackage = false,
                    notes = "جلسة علاج طبيعي مجهزة للحضور والخصم"
                )
            )

            val appt2 = dao.insertAppointment(
                Appointment(
                    appointmentNumber = "APT-102",
                    patientId = pat2,
                    doctorId = doc1,
                    therapistId = ther1,
                    departmentId = dept1,
                    serviceId = srv1,
                    date = todayStr,
                    timeSlot = "10:30 ص",
                    status = "محجوز",
                    notes = "تأهيل مفصل الركبة"
                )
            )
            dao.insertSession(
                ClinicSession(
                    sessionNumber = "SES-102",
                    patientId = pat2,
                    appointmentId = appt2,
                    packageId = pkg2,
                    doctorId = doc1,
                    therapistId = ther1,
                    serviceId = srv1,
                    date = todayStr,
                    time = "10:30 ص",
                    status = "مجدولة",
                    isDeductedFromPackage = false,
                    notes = "باقة الركبة - متبقي 2"
                )
            )

            val appt3 = dao.insertAppointment(
                Appointment(
                    appointmentNumber = "APT-103",
                    patientId = pat3,
                    doctorId = doc3,
                    therapistId = ther2,
                    departmentId = dept3,
                    serviceId = srv4,
                    date = todayStr,
                    timeSlot = "11:30 ص",
                    status = "محجوز",
                    notes = "تأهيل عصبي - الجلسة قبل الأخيرة"
                )
            )
            dao.insertSession(
                ClinicSession(
                    sessionNumber = "SES-103",
                    patientId = pat3,
                    appointmentId = appt3,
                    packageId = pkg3,
                    doctorId = doc3,
                    therapistId = ther2,
                    serviceId = srv4,
                    date = todayStr,
                    time = "11:30 ص",
                    status = "مجدولة",
                    isDeductedFromPackage = false,
                    notes = "جلسة تأهيل عصبي"
                )
            )

            // 9. Receipts & Expenses
            dao.insertReceipt(
                ReceiptVoucher(
                    voucherNumber = "RV-00015",
                    date = todayStr,
                    patientId = pat1,
                    amount = 10000.0,
                    paymentMethod = "نقدًا",
                    statement = "دفعة من قيمة باقة العلاج الطبيعي",
                    previousBalance = 20000.0,
                    remainingBalance = 10000.0
                )
            )
            dao.insertReceipt(
                ReceiptVoucher(
                    voucherNumber = "RV-00016",
                    date = todayStr,
                    patientId = pat2,
                    amount = 13000.0,
                    paymentMethod = "تحويل بنكي",
                    statement = "سداد باقة الركبة والمفاصل",
                    previousBalance = 18000.0,
                    remainingBalance = 5000.0
                )
            )

            dao.insertExpense(
                ExpenseVoucher(
                    voucherNumber = "PV-00021",
                    date = todayStr,
                    beneficiaryType = "مورد",
                    beneficiaryName = "مؤسسة الشفاء للمستلزمات الطبية",
                    amount = 15000.0,
                    paymentMethod = "نقدًا",
                    category = "مستلزمات طبية",
                    statement = "شراء جل التراساوند وأشرطة لاصقة طبية"
                )
            )
            dao.insertExpense(
                ExpenseVoucher(
                    voucherNumber = "PV-00022",
                    date = todayStr,
                    beneficiaryType = "جهة أخرى",
                    beneficiaryName = "شركة الكهرباء والطاقة",
                    amount = 8500.0,
                    paymentMethod = "نقدًا",
                    category = "خدمات وتشغيل",
                    statement = "فاتورة الكهرباء الشهرية للمركز"
                )
            )

            // 10. Employees & Deductions
            val emp1 = dao.insertEmployee(
                Employee(
                    name = "مروان العبسي",
                    jobTitle = "مسؤول الاستقبال والمواعيد",
                    department = "الإدارة والاستقبال",
                    phone = "775511223",
                    basicSalary = 80000.0,
                    allowances = 10000.0,
                    incentives = 5000.0,
                    hireDate = "2024-01-15"
                )
            )
            val emp2 = dao.insertEmployee(
                Employee(
                    name = "سامي الحداد",
                    jobTitle = "المحاسب المالي",
                    department = "الشؤون المالية",
                    phone = "776622334",
                    basicSalary = 100000.0,
                    allowances = 15000.0,
                    incentives = 0.0,
                    hireDate = "2023-06-01"
                )
            )
            val emp3 = dao.insertEmployee(
                Employee(
                    name = "هناء الصبري",
                    jobTitle = "ممرضة وفنية أجهزة",
                    department = "العلاج الطبيعي",
                    phone = "777733445",
                    basicSalary = 75000.0,
                    allowances = 10000.0,
                    incentives = 3000.0,
                    hireDate = "2024-03-01"
                )
            )

            dao.insertDeduction(
                SalaryDeduction(
                    employeeId = emp1,
                    deductionType = "استقطاع سلفة",
                    amount = 10000.0,
                    reason = "سلفة منتصف الشهر للموظف مروان",
                    date = todayStr
                )
            )
            dao.insertDeduction(
                SalaryDeduction(
                    employeeId = emp1,
                    deductionType = "استقطاع غياب",
                    amount = 5000.0,
                    reason = "غياب يوم بدون إذن مسبق",
                    date = todayStr
                )
            )

            // 11. Inventory
            dao.insertInventoryItem(InventoryItem(name = "جل التراساوند طبي (عبوة 5 لتر)", category = "مستهلكات طبية", quantity = 8, minLimit = 3, unitPrice = 4500.0, unit = "جالون"))
            dao.insertInventoryItem(InventoryItem(name = "أشرطة كينيزيو تيب علاجية لاصقة", category = "مستهلكات طبية", quantity = 15, minLimit = 5, unitPrice = 2000.0, unit = "رول"))
            dao.insertInventoryItem(InventoryItem(name = "إبر جافة علاجية (Dry Needling)", category = "أدوات علاجية", quantity = 25, minLimit = 10, unitPrice = 3500.0, unit = "علبة"))
            dao.insertInventoryItem(InventoryItem(name = "أقطاب كهربائية لاصقة للأجهزة TENS", category = "مستهلكات أجهزة", quantity = 40, minLimit = 10, unitPrice = 500.0, unit = "زوج"))
            dao.insertInventoryItem(InventoryItem(name = "زيوت مساج وتدليك طبيعي للأعصاب", category = "علاج طبيعي", quantity = 6, minLimit = 4, unitPrice = 2500.0, unit = "زجاجة"))

            // 12. Message Templates
            dao.insertTemplate(
                MessageTemplate(
                    templateKey = "APPOINTMENT",
                    title = "قالب تسجيل موعد",
                    templateText = "مرحبًا {اسم_المريض}\n\nتم تسجيل موعدكم في {اسم_المركز}.\nالتاريخ: {التاريخ}\nالوقت: {الوقت}\nالطبيب: {اسم_الطبيب}\nالقسم: {القسم}\n\nنتمنى لكم الشفاء والعافية."
                )
            )
            dao.insertTemplate(
                MessageTemplate(
                    templateKey = "SESSION_DEDUCTION",
                    title = "قالب خصم جلسة",
                    templateText = "مرحبًا {اسم_المريض}\n\nتم تسجيل حضوركم في جلسة {اسم_الخدمة}.\nتم خصم جلسة واحدة من الباقة.\nالجلسات المستخدمة: {المستخدم}\nالجلسات المتبقية: {المتبقي}\nالباقة: {اسم_الباقة}\n\nشكرًا لكم ونتمنى لكم دوام الصحة."
                )
            )
            dao.insertTemplate(
                MessageTemplate(
                    templateKey = "PACKAGE_EXPIRED",
                    title = "قالب انتهاء الباقة",
                    templateText = "تنبيه للمريض {اسم_المريض}\n\nلقد انتهت جلسات الباقة الحالية ({اسم_الباقة}).\nيرجى مراجعة إدارة المركز لتجديد الباقة ومواصلة البرنامج التأهيلي.\n\nمركز وسيم الطبي: 774486588"
                )
            )
            dao.insertTemplate(
                MessageTemplate(
                    templateKey = "RECEIPT",
                    title = "قالب سند القبض",
                    templateText = "مرحبًا {اسم_المريض}\n\nتم استلام مبلغ: {المبلغ} ر.ي\nسند رقم: {رقم_السند}\nالرصيد السابق: {الرصيد_السابق} ر.ي\nالمبلغ المقبوض: {المبلغ} ر.ي\nالرصيد بعد السداد: {الرصيد_النهائي} ر.ي\n\nشكرًا لكم - {اسم_المركز}."
                )
            )
            dao.insertTemplate(
                MessageTemplate(
                    templateKey = "DOCTOR_CASE",
                    title = "قالب إشعار الحالة للطبيب",
                    templateText = "د. {اسم_الطبيب}\n\nتم تسجيل حالة مريض جديدة:\nاسم المريض: {اسم_المريض}\nالتشخيص: {التشخيص}\nالقسم: {القسم}\nالخدمة: {الخدمة}\nالتاريخ: {التاريخ}\n\nيرجى مراجعة ملف الحالة في نظام وسيم الطبي PRO."
                )
            )

            // 13. Notifications
            dao.insertNotification(
                AppNotification(
                    title = "تنبيه باقة مريض",
                    message = "تبقت للمريض محمد علي الصنعاني جلستان فقط في باقة خشونة الركبة.",
                    type = "تنبيه باقة"
                )
            )
            dao.insertNotification(
                AppNotification(
                    title = "تنبيه انتهاء باقة وشيك",
                    message = "تبقت جلسة واحدة للمريض عبدالله أحمد الريمي في باقة التأهيل العصبي.",
                    type = "تنبيه باقة"
                )
            )
            dao.insertNotification(
                AppNotification(
                    title = "سند قبض مسجل",
                    message = "تم تسجيل سند قبض رقم RV-00015 للمريض أحمد محمد بمبلغ 10,000 ر.ي.",
                    type = "قبض"
                )
            )

            // 14. Audit Log
            dao.insertAuditLog(
                AuditLog(
                    user = "المدير (وسيم الفرح)",
                    action = "تهيئة النظام",
                    details = "تم تشغيل نظام وسيم الطبي PRO وتهيئة قاعدة البيانات الأولية بنجاح"
                )
            )
        }
    }
}
