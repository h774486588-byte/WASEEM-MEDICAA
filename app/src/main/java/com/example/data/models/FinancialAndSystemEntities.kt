package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptVoucher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherNumber: String,
    val date: String,
    val patientId: Long,
    val amount: Double,
    val paymentMethod: String = "نقدًا", // "نقدًا", "تحويل", "شبكة", "أخرى"
    val statement: String,
    val previousBalance: Double,
    val remainingBalance: Double,
    val createdBy: String = "المدير",
    val branchId: Long = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseVoucher(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val voucherNumber: String,
    val date: String,
    val beneficiaryType: String, // "موظف", "طبيب", "معالج", "مورد", "مريض", "جهة أخرى"
    val beneficiaryName: String,
    val amount: Double,
    val paymentMethod: String = "نقدًا",
    val category: String,
    val statement: String,
    val createdBy: String = "المدير",
    val branchId: Long = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val jobTitle: String,
    val department: String,
    val phone: String,
    val basicSalary: Double,
    val allowances: Double = 0.0,
    val incentives: Double = 0.0,
    val hireDate: String,
    val isActive: Boolean = true
)

@Entity(tableName = "salary_deductions")
data class SalaryDeduction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val deductionType: String, // "استقطاع غياب", "استقطاع سلفة", "استقطاع تأخير", "استقطاع إداري", "خصم آخر"
    val amount: Double,
    val reason: String,
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val quantity: Int,
    val minLimit: Int = 5,
    val unitPrice: Double,
    val unit: String = "قطعة",
    val barcode: String = ""
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "موعد", "جلسة", "خصم باقة", "تنبيه باقة", "قبض", "صرف", "طبيب", "نظام"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedId: Long? = null
)

@Entity(tableName = "messages")
data class AppMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientName: String,
    val recipientPhone: String,
    val content: String,
    val templateType: String,
    val status: String = "جاهز للإرسال", // "مسودة", "جاهز للإرسال", "تم الإرسال"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateKey: String,
    val title: String,
    val templateText: String
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user: String = "المدير",
    val action: String,
    val details: String,
    val previousData: String? = null,
    val newData: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "licenses")
data class AppLicense(
    @PrimaryKey val id: Long = 1,
    val customerName: String,
    val centerName: String,
    val licenseNumber: String,
    val startDate: Long,
    val endDate: Long,
    val licenseType: String = "TRIAL", // "TRIAL", "PRO_PERPETUAL", "ANNUAL"
    val status: String = "ACTIVE", // "ACTIVE", "EXPIRED"
    val installationId: String
)

@Entity(tableName = "settings")
data class CenterSettings(
    @PrimaryKey val id: Long = 1,
    val centerName: String = "مركز وسيم للعلاج الطبيعي والتأهيل",
    val developerName: String = "وسيم الفرح",
    val developerPhone: String = "772357240",
    val address: String = "صنعاء - شارع الستين الغربي",
    val phone: String = "772357240",
    val whatsapp: String = "772357240",
    val currency: String = "ر.ي",
    val autoSyncGoogleDrive: Boolean = false,
    val syncIntervalHours: Int = 6,
    val isDarkMode: Boolean = false,
    val enableDoctorAlerts: Boolean = true
)

@Entity(tableName = "branches")
data class Branch(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // "الفرع الرئيسي", "فرع صنعاء", "فرع إب", "فرع تعز"
    val city: String = "صنعاء",
    val address: String = "",
    val phone: String = "",
    val isMainBranch: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class AppUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // "admin", "Waseem", "mohammed", "ahmed"
    val passwordHash: String, // كلمة المرور مخزنة كـ hash مملّح
    val fullName: String,
    val role: String, // "SUPER_ADMIN", "ADMIN", "RECEPTIONIST", "ACCOUNTANT", "DOCTOR", "STAFF"
    val branchId: Long? = null, // null = all branches
    val phone: String = "",
    val permPatients: Boolean = true,
    val permAppointments: Boolean = true,
    val permSessions: Boolean = true,
    val permDoctors: Boolean = true,
    val permDepartments: Boolean = true,
    val permFinancial: Boolean = true,
    val permPayroll: Boolean = true,
    val permInventory: Boolean = true,
    val permReports: Boolean = true,
    val permSettings: Boolean = true,
    val permUsers: Boolean = true,
    val permBackup: Boolean = true,
    val permLicense: Boolean = true,
    val isActive: Boolean = true,
    val isSystemOwner: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
