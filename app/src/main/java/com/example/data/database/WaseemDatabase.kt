package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
            val derived = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                .generateSecret(spec).encoded
            PREFIX + ITERATIONS + ":" + salt.toHexString() + ":" + derived.toHexString()
        } finally { spec.clearPassword() }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }
}

@Database(
    entities = [
        Patient::class, Doctor::class, Therapist::class, Department::class, MedicalService::class,
        DiagnosisItem::class, Appointment::class, PatientPackage::class, PackageSession::class,
        ClinicSession::class, ReceiptVoucher::class, ExpenseVoucher::class, Employee::class,
        SalaryDeduction::class, InventoryItem::class, AppNotification::class, AppMessage::class,
        MessageTemplate::class, AuditLog::class, AppLicense::class, CenterSettings::class,
        Branch::class, AppUser::class
    ],
    version = 5,
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

        @Volatile private var INSTANCE: WaseemDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): WaseemDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WaseemDatabase::class.java,
                    "waseem_medical_pro.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    // Version 5 intentionally resets incompatible development databases.
                    // This prevents startup crashes caused by schema changes that were made
                    // without a matching Room migration in earlier trial builds.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance ->
                        INSTANCE = instance
                        scope.launch(Dispatchers.IO) {
                            runCatching { ensureEssentialData(instance.clinicDao()) }
                                .onFailure { it.printStackTrace() }
                        }
                    }
            }
        }

        suspend fun ensureEssentialData(dao: ClinicDao) {
            val now = System.currentTimeMillis()
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(now))

            if (dao.getBranchesCountDirect() == 0) {
                dao.insertBranch(Branch(id=1,name="الفرع الرئيسي - صنعاء",city="صنعاء",address="شارع الستين الغربي - بجوار المستشفى الاستشاري",phone="772357240",isMainBranch=true))
                dao.insertBranch(Branch(id=2,name="فرع صنعاء - حدة",city="صنعاء",address="شارع حدة العام - جولة الرويشان",phone="772357240"))
                dao.insertBranch(Branch(id=3,name="فرع إب - الدائري",city="إب",address="الشارع الدائري الغربي",phone="772357240"))
                dao.insertBranch(Branch(id=4,name="فرع تعز - شارع جمال",city="تعز",address="شارع جمال عبد الناصر",phone="772357240"))
            }

            if (dao.getUserByUsername("admin") == null) dao.insertUser(AppUser(username="admin",passwordHash=BootstrapPasswordHasher.hash("admin"),fullName="مدير المركز (إدارة عامة)",role="ADMIN",branchId=1,phone="772357240",isSystemOwner=false,permPatients=true,permAppointments=true,permSessions=true,permDoctors=true,permDepartments=true,permFinancial=true,permPayroll=true,permInventory=true,permReports=true,permSettings=true,permUsers=true,permBackup=true,permLicense=true))
            if (dao.getUserByUsername("Waseem") == null) dao.insertUser(AppUser(username="Waseem",passwordHash=BootstrapPasswordHasher.hash("W772357240"),fullName="م. وسيم الفرح (مالك ومطور النظام)",role="SUPER_ADMIN",branchId=null,phone="772357240",isSystemOwner=true,permPatients=true,permAppointments=true,permSessions=true,permDoctors=true,permDepartments=true,permFinancial=true,permPayroll=true,permInventory=true,permReports=true,permSettings=true,permUsers=true,permBackup=true,permLicense=true))
            if (dao.getUserByUsername("mohammed") == null) dao.insertUser(AppUser(username="mohammed",passwordHash=BootstrapPasswordHasher.hash("1234"),fullName="محمد أحمد — استقبال",role="RECEPTIONIST",branchId=1,phone="771122334",isSystemOwner=false,permPatients=true,permAppointments=true,permSessions=true,permDoctors=true,permDepartments=true,permFinancial=false,permPayroll=false,permInventory=false,permReports=false,permSettings=false,permUsers=false,permBackup=false,permLicense=false))
            if (dao.getUserByUsername("ahmed") == null) dao.insertUser(AppUser(username="ahmed",passwordHash=BootstrapPasswordHasher.hash("1234"),fullName="أحمد علي — محاسب",role="ACCOUNTANT",branchId=1,phone="775566778",isSystemOwner=false,permPatients=true,permAppointments=false,permSessions=false,permDoctors=false,permDepartments=false,permFinancial=true,permPayroll=true,permInventory=true,permReports=true,permSettings=false,permUsers=false,permBackup=false,permLicense=false))

            if (dao.getSettingsDirect() == null) dao.updateSettings(CenterSettings(id=1,centerName="مركز وسيم للعلاج الطبيعي والتأهيل",developerName="وسيم الفرح",developerPhone="772357240",address="اليمن - صنعاء - شارع الستين الغربي",phone="772357240",whatsapp="772357240",currency="ر.ي",autoSyncGoogleDrive=false,syncIntervalHours=6,isDarkMode=false,enableDoctorAlerts=true))
            if (dao.getLicenseDirect() == null) dao.setLicense(AppLicense(id=1,customerName="مركز وسيم الطبي والتأهيلي",centerName="نظام وسيم الطبي PRO",licenseNumber="WMP-TRIAL-"+UUID.randomUUID().toString().take(8).uppercase(),startDate=now,endDate=now+30L*24L*60L*60L*1000L,licenseType="TRIAL",status="ACTIVE",installationId="INST-"+UUID.randomUUID().toString().take(12).uppercase()))

            @Suppress("UNUSED_VARIABLE") val unusedToday = todayStr
        }
    }
}
