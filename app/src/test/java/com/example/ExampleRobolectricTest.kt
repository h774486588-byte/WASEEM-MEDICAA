package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.WaseemDatabase
import com.example.data.models.ClinicSession
import com.example.data.models.Doctor
import com.example.data.models.Patient
import com.example.data.models.PatientPackage
import com.example.data.repository.ClinicRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: WaseemDatabase
    private lateinit var repository: ClinicRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WaseemDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ClinicRepository(db.clinicDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("نظام وسيم الطبي PRO", appName)
    }

    @Test
    fun testRegisterPatientAndBalance() = runBlocking {
        val result = repository.registerPatient(
            name = "أحمد محمد الحاشدي",
            phone = "0771122334",
            gender = "ذكر",
            dateOfBirth = "1990-01-01",
            address = "صنعاء",
            maritalStatus = "متزوج",
            profession = "موظف",
            referralSource = "طبيب",
            doctorId = null,
            therapistId = null,
            departmentId = null,
            diagnosis = "آلام أسفل الظهر",
            complaint = "ألم حاد",
            notes = "ملاحظات",
            initialBalance = 20000.0,
            notifyDoctor = false
        )
        assertTrue(result.isSuccess)
        val patientId = result.getOrThrow()
        val patient = db.clinicDao().getPatientById(patientId)
        assertNotNull(patient)
        assertEquals("أحمد محمد الحاشدي", patient?.name)
        assertEquals(20000.0, patient?.balanceDue ?: 0.0, 0.01)

        // Test receipt creation updates patient balance
        val receiptResult = repository.createReceiptVoucher(
            patientId = patientId,
            amount = 10000.0,
            paymentMethod = "نقدًا",
            statement = "سداد دفعة"
        )
        assertTrue(receiptResult.isSuccess)
        val updatedPatient = db.clinicDao().getPatientById(patientId)
        assertEquals(10000.0, updatedPatient?.balanceDue ?: 0.0, 0.01)
    }

    @Test
    fun testSessionAttendanceAutoDeductionAndDuplicateBlock() = runBlocking {
        // 1. Create Patient
        val patId = db.clinicDao().insertPatient(
            Patient(
                fileNumber = "WM-TEST",
                name = "محمد علي",
                phone = "0772233445",
                gender = "ذكر",
                dateOfBirth = "1992-02-02",
                address = "صنعاء",
                maritalStatus = "متزوج",
                profession = "مهندس",
                referralSource = "صديق",
                doctorId = null,
                therapistId = null,
                departmentId = null,
                diagnosis = "خشونة ركبة",
                complaint = "ألم",
                notes = ""
            )
        )

        // 2. Create Package with 10 sessions (used 0, remaining 10)
        val pkgId = db.clinicDao().insertPackage(
            PatientPackage(
                patientId = patId,
                packageName = "باقة 10 جلسات",
                price = 20000.0,
                totalSessions = 10,
                usedSessions = 0,
                remainingSessions = 10,
                startDate = "2026-09-24",
                endDate = "2026-10-24",
                status = "نشطة"
            )
        )

        // 3. Create Session
        val sessionId = db.clinicDao().insertSession(
            ClinicSession(
                sessionNumber = "SES-TEST-1",
                patientId = patId,
                packageId = pkgId,
                date = "2026-09-24",
                time = "10:00 ص",
                status = "مجدولة",
                isDeductedFromPackage = false
            )
        )

        // 4. Attend session: should deduct 1 session
        val firstAttendResult = repository.attendSession(sessionId)
        assertTrue(firstAttendResult.isSuccess)

        val updatedPkg = db.clinicDao().getPackageById(pkgId)
        assertEquals(1, updatedPkg?.usedSessions)
        assertEquals(9, updatedPkg?.remainingSessions)

        // 5. Try attending again: MUST BE PREVENTED (منع الخصم المكرر)
        val secondAttendResult = repository.attendSession(sessionId)
        assertFalse(secondAttendResult.isSuccess)
        assertTrue(secondAttendResult.exceptionOrNull()?.message?.contains("تم تسجيل الحضور مسبقاً") == true)

        // Verify remaining sessions did not change again
        val finalPkg = db.clinicDao().getPackageById(pkgId)
        assertEquals(9, finalPkg?.remainingSessions)
    }

    @Test
    fun testAppointmentConflictPrevention() = runBlocking {
        val patId = db.clinicDao().insertPatient(
            Patient(
                fileNumber = "WM-TEST-2",
                name = "ياسر عبدالله",
                phone = "0773344556",
                gender = "ذكر",
                dateOfBirth = "1995-05-05",
                address = "صنعاء",
                maritalStatus = "أعزب",
                profession = "محاسب",
                referralSource = "إعلان",
                doctorId = null,
                therapistId = null,
                departmentId = null,
                diagnosis = "تمزق أربطة",
                complaint = "",
                notes = ""
            )
        )

        val docId = db.clinicDao().insertDoctor(
            Doctor(
                name = "د. وسيم الفرح",
                specialization = "استشاري تأهيل",
                phone = "774486588",
                departmentId = 1,
                workingDays = "السبت - الخميس",
                workingHours = "09:00 ص - 01:00 م",
                servicePrice = 10000.0,
                commissionRate = 30.0
            )
        )

        // Book 1st appointment
        val appt1 = repository.bookAppointment(
            patientId = patId,
            doctorId = docId,
            therapistId = null,
            departmentId = 1,
            serviceId = 1,
            date = "2026-09-25",
            timeSlot = "10:00 ص",
            notes = "كشف أول"
        )
        assertTrue(appt1.isSuccess)

        // Try booking conflicting appointment for same doctor at same date and time slot
        val appt2 = repository.bookAppointment(
            patientId = patId,
            doctorId = docId,
            therapistId = null,
            departmentId = 1,
            serviceId = 1,
            date = "2026-09-25",
            timeSlot = "10:00 ص",
            notes = "كشف متعارض"
        )
        assertFalse(appt2.isSuccess)
        assertTrue(appt2.exceptionOrNull()?.message?.contains("يوجد موعد محجوز مسبقاً") == true)
    }
}
