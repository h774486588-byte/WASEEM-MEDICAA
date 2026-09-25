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

    fun searchPatients(query: String): Flow<List<Patient>> = dao.searchPatients(query.trim())
    fun getAppointmentsByDate(date: String): Flow<List<Appointment>> = dao.getAppointmentsByDate(date)
    fun getSessionsByDate(date: String): Flow<List<ClinicSession>> = dao.getSessionsByDate(date)
    fun getPackagesByPatient(patientId: Long): Flow<List<PatientPackage>> = dao.getPackagesByPatient(patientId)
    fun getReceiptsByPatient(patientId: Long): Flow<List<ReceiptVoucher>> = dao.getReceiptsByPatient(patientId)

    suspend fun registerPatient(name: String, phone: String, gender: String, dateOfBirth: String, address: String, maritalStatus: String, profession: String, referralSource: String, doctorId: Long?, therapistId: Long?, departmentId: Long?, serviceId: Long? = null, diagnosis: String, complaint: String, notes: String, initialBalance: Double = 0.0, branchId: Long = 1, autoCreateFirstSessionOrAppt: Boolean = true, notifyDoctor: Boolean = true): Result<Long> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("اسم المريض مطلوب"))
        if (phone.isBlank()) return Result.failure(IllegalArgumentException("رقم الهاتف مطلوب"))
        val timestamp = System.currentTimeMillis()
        val fileNumber = "WM-$timestamp"
        val patient = Patient(fileNumber=fileNumber,name=name.trim(),phone=phone.trim(),gender=gender,dateOfBirth=dateOfBirth,address=address.trim(),maritalStatus=maritalStatus,profession=profession.trim(),referralSource=referralSource.trim(),doctorId=doctorId,therapistId=therapistId,departmentId=departmentId,serviceId=serviceId,diagnosis=diagnosis.trim(),complaint=complaint.trim(),notes=notes.trim(),balanceDue=initialBalance,branchId=branchId)
        val id = dao.insertPatient(patient)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date(timestamp))
        if (autoCreateFirstSessionOrAppt) {
            val suffix=timestamp.toString()
            dao.insertSession(ClinicSession(sessionNumber="SES-$suffix",patientId=id,doctorId=doctorId,therapistId=therapistId,departmentId=departmentId,serviceId=serviceId,date=todayStr,time="10:00 ص",status="مجدولة",notes="جلسة أولى مسجلة تلقائياً مع فتح الملف - ${diagnosis.trim()}",branchId=branchId))
            dao.insertAppointment(Appointment(appointmentNumber="APT-$suffix",patientId=id,doctorId=doctorId,therapistId=therapistId,departmentId=departmentId,serviceId=serviceId,date=todayStr,timeSlot="10:00 ص",status="محجوز",notes="موعد استشارة وجلسة أولى - ${diagnosis.trim()}",branchId=branchId))
        }
        dao.insertAuditLog(AuditLog(user="الاستقبال",action="تسجيل مريض جديد",details="تم تسجيل المريض $name - رقم الملف: $fileNumber - فرع $branchId",newData="الهاتف: $phone, التشخيص: $diagnosis"))
        if (notifyDoctor && doctorId != null) dao.getDoctorById(doctorId)?.let { doctor ->
            dao.insertNotification(AppNotification(title="حالة مريض جديدة - د. ${doctor.name}",message="تم تسجيل المريض $name وإسناده إليكم.",type="طبيب",relatedId=id))
            dao.insertMessage(AppMessage(recipientName=doctor.name,recipientPhone=doctor.phone,content="د. ${doctor.name}\nتم تسجيل حالة جديدة:\nاسم المريض: $name\nالتشخيص: $diagnosis\nيرجى مراجعة ملف المريض.",templateType="DOCTOR_CASE"))
        }
        return Result.success(id)
    }

    suspend fun updatePatient(patient: Patient) { val old=dao.getPatientById(patient.id); dao.updatePatient(patient); dao.insertAuditLog(AuditLog(user="المدير",action="تعديل بيانات مريض",details="تعديل ملف ${patient.name} (${patient.fileNumber})",previousData="هاتف: ${old?.phone}, تشخيص: ${old?.diagnosis}",newData="هاتف: ${patient.phone}, تشخيص: ${patient.diagnosis}")) }
    suspend fun softDeletePatient(id: Long) { val patient=dao.getPatientById(id); dao.softDeletePatient(id); dao.insertAuditLog(AuditLog(user="المدير",action="أرشفة ملف مريض",details="تمت أرشفة ملف المريض ${patient?.name}")) }

    suspend fun bookAppointment(patientId: Long, doctorId: Long?, therapistId: Long?, departmentId: Long?, serviceId: Long?, date: String, timeSlot: String, notes: String, branchId: Long=1): Result<Long> {
        val patient=dao.getPatientById(patientId) ?: return Result.failure(IllegalArgumentException("المريض غير موجود"))
        if (doctorId != null && dao.checkDoctorAppointmentConflict(doctorId,date,timeSlot)!=null) return Result.failure(IllegalStateException("يوجد موعد محجوز مسبقاً لنفس الطبيب في هذا التوقيت ($timeSlot)، يرجى اختيار وقت آخر."))
        if (therapistId != null && dao.checkTherapistAppointmentConflict(therapistId,date,timeSlot)!=null) return Result.failure(IllegalStateException("يوجد موعد محجوز مسبقاً لنفس المعالج في هذا التوقيت ($timeSlot)، يرجى اختيار وقت آخر."))
        val apptNumber="APT-${System.currentTimeMillis()}"
        val id=dao.insertAppointment(Appointment(appointmentNumber=apptNumber,patientId=patientId,doctorId=doctorId,therapistId=therapistId,departmentId=departmentId,serviceId=serviceId,date=date,timeSlot=timeSlot,status="محجوز",notes=notes,branchId=branchId))
        val doctor=doctorId?.let{dao.getDoctorById(it)}
        dao.insertNotification(AppNotification(title="حجز موعد جديد",message="موعد للمريض ${patient.name} بتاريخ $date الساعة $timeSlot.",type="موعد",relatedId=id))
        dao.insertMessage(AppMessage(recipientName=patient.name,recipientPhone=patient.phone,content="مرحبًا ${patient.name}\nتم حجز موعدكم بنجاح في مركز وسيم الطبي.\nالتاريخ: $date\nالوقت: $timeSlot\nالطبيب: ${doctor?.name ?: "الاستشاري المناوب"}\nنتمنى لكم دوام الصحة والعافية.",templateType="APPOINTMENT"))
        dao.insertAuditLog(AuditLog(user="الاستقبال",action="حجز موعد",details="حجز موعد رقم $apptNumber للمريض ${patient.name} بتوقيت $date $timeSlot"))
        return Result.success(id)
    }

    suspend fun updateAppointmentStatus(appointmentId: Long,newStatus: String): Result<Unit> {
        val allowedStatuses = setOf("محجوز", "حضر", "لم يحضر", "أُلغي", "مكتمل")
        if (newStatus !in allowedStatuses) return Result.failure(IllegalArgumentException("حالة الموعد غير صالحة"))
        val appointment = dao.getAllAppointmentsSnapshot().firstOrNull { it.id == appointmentId }
            ?: return Result.failure(IllegalArgumentException("الموعد غير موجود"))
        if (appointment.status == newStatus) return Result.success(Unit)
        dao.updateAppointment(appointment.copy(status = newStatus))
        dao.insertAuditLog(AuditLog(user="الاستقبال",action="تحديث حالة موعد",details="الموعد ${appointment.appointmentNumber}: ${appointment.status} ← $newStatus"))
        return Result.success(Unit)
    }

    suspend fun attendSession(sessionId: Long): Result<String> {
        val session=dao.getSessionById(sessionId) ?: return Result.failure(IllegalArgumentException("الجلسة غير موجودة"))
        if(session.status=="حضر"||session.isDeductedFromPackage)return Result.failure(IllegalStateException("تم تسجيل الحضور مسبقاً لهذه الجلسة ولن يتم الخصم مرة أخرى."))
        val patient=dao.getPatientById(session.patientId) ?: return Result.failure(IllegalArgumentException("المريض غير موجود"))
        val now=System.currentTimeMillis()
        val pkg=session.packageId?.let{dao.getPackageById(it)}?:dao.getActivePackageForPatient(patient.id)
        if(pkg!=null&&pkg.remainingSessions>0){
            val newUsed=pkg.usedSessions+1; val newRemaining=pkg.remainingSessions-1
            dao.updateSession(session.copy(status="حضر",isDeductedFromPackage=true,attendedAt=now))
            dao.updatePackage(pkg.copy(usedSessions=newUsed,remainingSessions=newRemaining,status=if(newRemaining==0)"مكتملة" else "نشطة"))
            dao.insertPackageSession(PackageSession(packageId=pkg.id,patientId=patient.id,sessionId=session.id,deductedAt=now,reason="حضور جلسة علاج طبيعي (${session.sessionNumber})",sessionsDeducted=1,remainingAfter=newRemaining))
            dao.insertNotification(AppNotification(title="خصم جلسة من الباقة",message="تم تسجيل حضور المريض ${patient.name}. تم خصم جلسة واحدة. المتبقي: $newRemaining جلسات.",type="خصم باقة",relatedId=session.id))
            dao.insertMessage(AppMessage(recipientName=patient.name,recipientPhone=patient.phone,content="مرحبًا ${patient.name}\n\nتم تسجيل حضوركم في جلسة العلاج الطبيعي اليوم.\nتم خصم جلسة واحدة من الباقة.\nالجلسات المستخدمة: $newUsed\nالجلسات المتبقية: $newRemaining\nالباقة: ${pkg.packageName}\n\nنتمنى لكم الشفاء والعافية.",templateType="SESSION_DEDUCTION"))
            if(newRemaining in 1..3)dao.insertNotification(AppNotification(title=if(newRemaining==1)"تنبيه مهم: جلسة أخيرة" else "تنبيه قرب انتهاء الباقة",message="تبقى للمريض ${patient.name} $newRemaining جلسات فقط في باقته.",type="تنبيه باقة",relatedId=pkg.id))
            if(newRemaining==0)dao.insertNotification(AppNotification(title="انتهاء الباقة",message="انتهت جميع جلسات باقة المريض ${patient.name}. يرجى التجديد لمواصلة العلاج.",type="تنبيه باقة",relatedId=pkg.id))
            val deductionMessage="تم تسجيل الحضور وخصم جلسة من الباقة. المتبقي: $newRemaining جلسات."
            dao.insertAuditLog(AuditLog(user="الاستقبال / المعالج",action="تسجيل حضور جلسة",details="جلسة رقم ${session.sessionNumber} للمريض ${patient.name} - $deductionMessage"))
            return Result.success(deductionMessage)
        }
        dao.updateSession(session.copy(status="حضر",isDeductedFromPackage=false,attendedAt=now))
        val message="تم تسجيل حضور الجلسة بنجاح. لا توجد باقة نشطة للخصم."
        dao.insertAuditLog(AuditLog(user="الاستقبال / المعالج",action="تسجيل حضور جلسة",details="جلسة رقم ${session.sessionNumber} للمريض ${patient.name} - $message"))
        return Result.success(message)
    }

    suspend fun createSession(patientId:Long,doctorId:Long?,therapistId:Long?,departmentId:Long?,serviceId:Long?,packageId:Long?=null,date:String,time:String,status:String="مجدولة",notes:String="",branchId:Long=1):Result<Long>{
        val patient=dao.getPatientById(patientId)?:return Result.failure(IllegalArgumentException("المريض غير موجود"))
        val sessionNum="SES-${System.currentTimeMillis()}"
        val id=dao.insertSession(ClinicSession(sessionNumber=sessionNum,patientId=patientId,doctorId=doctorId?:patient.doctorId,therapistId=therapistId?:patient.therapistId,departmentId=departmentId?:patient.departmentId,serviceId=serviceId?:patient.serviceId,packageId=packageId,date=date,time=time,status=status,notes=notes.trim(),branchId=branchId))
        dao.insertAuditLog(AuditLog(user="الاستقبال",action="تسجيل جلسة جديدة",details="تسجيل جلسة $sessionNum للمريض ${patient.name} بتوقيت $date $time")); return Result.success(id)
    }
    suspend fun updateSessionStatus(sessionId:Long,status:String){dao.getSessionById(sessionId)?.let{session->dao.updateSession(session.copy(status=status));dao.insertAuditLog(AuditLog(user="المعالج",action="تحديث حالة جلسة",details="جلسة ${session.sessionNumber} أصبحت $status"))}}

    suspend fun createPackage(patientId:Long,packageName:String,price:Double,totalSessions:Int,startDate:String,endDate:String,notes:String,departmentId:Long?=null,serviceId:Long?=null,doctorId:Long?=null,therapistId:Long?=null,branchId:Long=1,generateSessions:Boolean=true):Result<Long>{
        val patient=dao.getPatientById(patientId)?:return Result.failure(IllegalArgumentException("المريض غير موجود")); if(totalSessions<=0)return Result.failure(IllegalArgumentException("عدد الجلسات يجب أن يكون أكبر من صفر"))
        val pkg=PatientPackage(patientId=patientId,packageName=packageName.trim(),price=price,totalSessions=totalSessions,usedSessions=0,remainingSessions=totalSessions,startDate=startDate,endDate=endDate,status="نشطة",notes=notes.trim(),departmentId=departmentId?:patient.departmentId,serviceId=serviceId?:patient.serviceId,doctorId=doctorId?:patient.doctorId,therapistId=therapistId?:patient.therapistId,branchId=branchId)
        val id=dao.insertPackage(pkg)
        if(generateSessions)dao.insertSessions((1..totalSessions).map{idx->ClinicSession(sessionNumber="PKG-$id-$idx",patientId=patientId,packageId=id,doctorId=doctorId?:patient.doctorId,therapistId=therapistId?:patient.therapistId,departmentId=departmentId?:patient.departmentId,serviceId=serviceId?:patient.serviceId,date=startDate,time="10:00 ص",status="مجدولة",notes="جلسة رقم $idx من إجمالي $totalSessions في باقة $packageName",branchId=branchId)})
        dao.updatePatient(patient.copy(balanceDue=patient.balanceDue+price)); dao.insertAuditLog(AuditLog(user="الاستقبال",action="إنشاء باقة جديدة",details="تم إنشاء $packageName للمريض ${patient.name} بقيمة $price ر.ي بعدد $totalSessions جلسات - فرع $branchId")); return Result.success(id)
    }

    suspend fun createReceiptVoucher(patientId:Long,amount:Double,paymentMethod:String,statement:String,branchId:Long=1):Result<Long>{
        if(amount<=0)return Result.failure(IllegalArgumentException("المبلغ يجب أن يكون أكبر من صفر")); val patient=dao.getPatientById(patientId)?:return Result.failure(IllegalArgumentException("المريض غير موجود"))
        val voucherNumber="RV-${System.currentTimeMillis()}"; val today=SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(Date()); val prevBalance=patient.balanceDue; val remainingBalance=max(0.0,prevBalance-amount)
        dao.updatePatient(patient.copy(balanceDue=remainingBalance)); val id=dao.insertReceipt(ReceiptVoucher(voucherNumber=voucherNumber,date=today,patientId=patientId,amount=amount,paymentMethod=paymentMethod,statement=statement.trim(),previousBalance=prevBalance,remainingBalance=remainingBalance,branchId=branchId))
        dao.insertNotification(AppNotification(title="سند قبض مسجل",message="تم تسجيل سند قبض رقم $voucherNumber للمريض ${patient.name} بمبلغ $amount ر.ي.",type="قبض",relatedId=id))
        dao.insertMessage(AppMessage(recipientName=patient.name,recipientPhone=patient.phone,content="مرحبًا ${patient.name}\n\nتم استلام مبلغ: ${"%,.0f".format(Locale.ENGLISH,amount)} ر.ي\nسند رقم: $voucherNumber\nالرصيد السابق: ${"%,.0f".format(Locale.ENGLISH,prevBalance)} ر.ي\nالرصيد المتبقي: ${"%,.0f".format(Locale.ENGLISH,remainingBalance)} ر.ي",templateType="RECEIPT"))
        dao.insertAuditLog(AuditLog(user="المحاسب",action="إنشاء سند قبض",details="سند رقم $voucherNumber للمريض ${patient.name} بمبلغ $amount ر.ي - فرع $branchId",previousData="الرصيد السابق: $prevBalance",newData="الرصيد الجديد: $remainingBalance")); return Result.success(id)
    }

    suspend fun createReceiptVoucherFull(patientId:Long,amount:Double,paymentMethod:String,statement:String,branchId:Long=1):Result<Pair<ReceiptVoucher,Patient>>{
        if(amount<=0)return Result.failure(IllegalArgumentException("المبلغ يجب أن يكون أكبر من صفر")); val patient=dao.getPatientById(patientId)?:return Result.failure(IllegalArgumentException("المريض غير موجود")); val voucherNumber="RV-${System.currentTimeMillis()}"; val today=SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(Date()); val prevBalance=patient.balanceDue; val remainingBalance=max(0.0,prevBalance-amount); val updatedPatient=patient.copy(balanceDue=remainingBalance); dao.updatePatient(updatedPatient)
        val receipt=ReceiptVoucher(voucherNumber=voucherNumber,date=today,patientId=patientId,amount=amount,paymentMethod=paymentMethod,statement=statement.trim().ifBlank{"دفعة سداد فاتورة علاجية"},previousBalance=prevBalance,remainingBalance=remainingBalance,branchId=branchId); val id=dao.insertReceipt(receipt); val createdReceipt=receipt.copy(id=id)
        dao.insertNotification(AppNotification(title=if(remainingBalance<=0)"سداد كامل الفاتورة" else "سداد جزئي للفاتورة",message="تم تسليم وسداد فاتورة رقم $voucherNumber للمريض ${patient.name} بمبلغ ${"%,.0f".format(Locale.ENGLISH,amount)} ر.ي.",type="قبض",relatedId=id))
        dao.insertMessage(AppMessage(recipientName=patient.name,recipientPhone=patient.phone,content="مرحبًا ${patient.name}\n\nتم استلام دفعة من الفاتورة: ${"%,.0f".format(Locale.ENGLISH,amount)} ر.ي\nسند قبض رقم: $voucherNumber\nالرصيد السابق: ${"%,.0f".format(Locale.ENGLISH,prevBalance)} ر.ي\nالمبلغ المسدد: ${"%,.0f".format(Locale.ENGLISH,amount)} ر.ي\nالرصيد المتبقي: ${"%,.0f".format(Locale.ENGLISH,remainingBalance)} ر.ي",templateType="RECEIPT"))
        dao.insertAuditLog(AuditLog(user="المحاسب",action="تسليم وسداد فاتورة",details="سند رقم $voucherNumber للمريض ${patient.name} بمبلغ $amount ر.ي (متبقي: $remainingBalance) - فرع $branchId",previousData="الرصيد السابق: $prevBalance",newData="الرصيد الجديد: $remainingBalance")); return Result.success(Pair(createdReceipt,updatedPatient))
    }

    suspend fun createExpenseVoucher(beneficiaryType:String,beneficiaryName:String,amount:Double,paymentMethod:String,category:String,statement:String,branchId:Long=1):Result<Long>{
        if(amount<=0)return Result.failure(IllegalArgumentException("المبلغ يجب أن يكون أكبر من صفر")); if(beneficiaryName.isBlank())return Result.failure(IllegalArgumentException("اسم المستفيد مطلوب")); val voucherNumber="PV-${System.currentTimeMillis()}"; val today=SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(Date()); val id=dao.insertExpense(ExpenseVoucher(voucherNumber=voucherNumber,date=today,beneficiaryType=beneficiaryType,beneficiaryName=beneficiaryName.trim(),amount=amount,paymentMethod=paymentMethod,category=category.trim(),statement=statement.trim(),branchId=branchId)); dao.insertNotification(AppNotification(title="سند صرف مسجل",message="تم تسجيل سند صرف رقم $voucherNumber للمستفيد $beneficiaryName بمبلغ $amount ر.ي.",type="صرف",relatedId=id)); dao.insertAuditLog(AuditLog(user="المحاسب",action="إنشاء سند صرف",details="سند رقم $voucherNumber - المستفيد: $beneficiaryName - مبلغ: $amount ر.ي - بيان: $statement")); return Result.success(id)
    }

    suspend fun addEmployee(employee:Employee):Long=dao.insertEmployee(employee)
    suspend fun updateEmployee(employee:Employee)=dao.updateEmployee(employee)
    suspend fun addDeduction(deduction:SalaryDeduction):Long{val id=dao.insertDeduction(deduction);val emp=dao.getEmployeeById(deduction.employeeId);dao.insertNotification(AppNotification(title="استقطاع راتب",message="تم تسجيل ${deduction.deductionType} للموظف ${emp?.name?:""} بمبلغ ${deduction.amount} ر.ي.",type="نظام"));return id}
    suspend fun addDoctor(doctor:Doctor):Long=dao.insertDoctor(doctor)
    suspend fun updateDoctor(doctor:Doctor)=dao.updateDoctor(doctor)
    suspend fun addTherapist(therapist:Therapist):Long=dao.insertTherapist(therapist)
    suspend fun updateTherapist(therapist:Therapist)=dao.updateTherapist(therapist)
    suspend fun addDepartment(department:Department):Long=dao.insertDepartment(department)
    suspend fun addService(service:MedicalService):Long=dao.insertService(service)
    suspend fun addInventoryItem(item:InventoryItem):Long=dao.insertInventoryItem(item)
    suspend fun updateInventoryItem(item:InventoryItem)=dao.updateInventoryItem(item)
    suspend fun markAllNotificationsRead()=dao.markAllNotificationsRead()
    suspend fun clearAllNotifications()=dao.clearAllNotifications()
    suspend fun updateMessageStatus(id:Long,status:String)=dao.updateMessageStatus(id,status)
    suspend fun updateTemplate(template:MessageTemplate)=dao.updateTemplate(template)
    suspend fun activateLicense(customerName:String,centerName:String,licenseNumber:String,licenseType:String="PRO_PERPETUAL"):Result<Unit>{val now=System.currentTimeMillis();val oneYearMillis=365L*24L*60L*60L*1000L;dao.setLicense(AppLicense(id=1,customerName=customerName.trim(),centerName=centerName.trim(),licenseNumber=licenseNumber.trim(),startDate=now,endDate=if(licenseType=="PRO_PERPETUAL")now+100L*oneYearMillis else now+oneYearMillis,licenseType=licenseType,status="ACTIVE",installationId=dao.getLicenseDirect()?.installationId?:"INST-AUTO-1001"));dao.insertAuditLog(AuditLog(user="المدير",action="تفعيل ترخيص النظام",details="تم تفعيل الترخيص ($licenseType) للعميل $customerName برقم $licenseNumber"));return Result.success(Unit)}
    suspend fun updateSettings(settings:CenterSettings)=dao.updateSettings(settings)
    suspend fun addBranch(branch:Branch):Long{val id=dao.insertBranch(branch);dao.insertAuditLog(AuditLog(user="المدير العام",action="إنشاء فرع جديد",details="تم إنشاء فرع: ${branch.name} في ${branch.city}"));return id}
    suspend fun updateBranch(branch:Branch){dao.updateBranch(branch);dao.insertAuditLog(AuditLog(user="المدير العام",action="تعديل بيانات فرع",details="تم تعديل فرع: ${branch.name}"))}
    suspend fun deleteBranch(id:Long){dao.deleteBranch(id);dao.insertAuditLog(AuditLog(user="المدير العام",action="حذف فرع",details="تم حذف الفرع رقم $id"))}

    suspend fun authenticate(username:String,password:String):AppUser?{
        val u=username.trim(); val p=password.trim(); var user=dao.authenticate(u,p)
        if(user==null)user=dao.getAllUsersDirect().firstOrNull{it.username.equals(u,true)&&it.passwordHash==p}
        if(user!=null)dao.insertAuditLog(AuditLog(user=user.fullName,action="تسجيل دخول ناجح",details="تم تسجيل الدخول باسم: ${user.username} (${user.role})"))
        return user
    }
    suspend fun getUserByUsername(username:String):AppUser?=dao.getUserByUsername(username.trim())
    suspend fun addUser(user:AppUser):Long{val id=dao.insertUser(user);dao.insertAuditLog(AuditLog(user="المدير العام",action="إضافة مستخدم جديد",details="تم إنشاء حساب المستخدم: ${user.fullName} (${user.username})"));return id}
    suspend fun updateUser(user:AppUser){dao.updateUser(user);dao.insertAuditLog(AuditLog(user="المدير العام",action="تحديث بيانات مستخدم",details="تم تحديث صلاحيات/بيانات: ${user.fullName}"))}
    suspend fun updateUserPassword(userId:Long,newPassword:String,performerName:String="المدير العام"){dao.updateUserPassword(userId,newPassword.trim());dao.insertAuditLog(AuditLog(user=performerName,action="تغيير / استعادة كلمة المرور",details="تم إعادة تعيين كلمة المرور للمستخدم رقم $userId بنجاح"))}
    suspend fun deleteUser(id:Long){dao.deleteUser(id);dao.insertAuditLog(AuditLog(user="المدير العام",action="حذف مستخدم",details="تم حذف حساب المستخدم رقم $id"))}
    suspend fun resetOperationalData(performer:String="م. وسيم الفرح (المالك)"){dao.clearPatients();dao.clearAppointments();dao.clearSessions();dao.clearPackages();dao.clearPackageSessions();dao.clearReceipts();dao.clearExpenses();dao.clearDeductions();dao.clearAllNotifications();dao.insertNotification(AppNotification(title="تصفير وتهيئة النظام",message="تم تصفير البيانات التشغيلية للنظام بنجاح مع الاحتفاظ بكافة الفروع والمستخدمين والصلاحيات والترخيص.",type="نظام"));dao.insertAuditLog(AuditLog(user=performer,action="تصفير النظام الشامل",details="تم تصفير البيانات التشغيلية بنجاح وبدء تشغيل دورة العمل الجديدة"))}
}
