package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appointmentNumber: String,
    val patientId: Long,
    val doctorId: Long?,
    val therapistId: Long?,
    val departmentId: Long?,
    val serviceId: Long?,
    val date: String, // YYYY-MM-DD
    val timeSlot: String, // e.g. "09:30 ص"
    val status: String = "محجوز", // "محجوز", "حضر", "لم يحضر", "أُلغي", "مكتمل"
    val notes: String = "",
    val branchId: Long = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "packages")
data class PatientPackage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val packageName: String,
    val price: Double,
    val totalSessions: Int,
    val usedSessions: Int = 0,
    val remainingSessions: Int,
    val startDate: String,
    val endDate: String,
    val status: String = "نشطة", // "نشطة", "مكتملة", "منتهية"
    val notes: String = "",
    val departmentId: Long? = null,
    val serviceId: Long? = null,
    val doctorId: Long? = null,
    val therapistId: Long? = null,
    val branchId: Long = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "package_sessions")
data class PackageSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageId: Long,
    val patientId: Long,
    val sessionId: Long,
    val deductedAt: Long = System.currentTimeMillis(),
    val reason: String,
    val sessionsDeducted: Int = 1,
    val remainingAfter: Int
)

@Entity(tableName = "sessions")
data class ClinicSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionNumber: String,
    val patientId: Long,
    val appointmentId: Long? = null,
    val packageId: Long? = null,
    val doctorId: Long? = null,
    val therapistId: Long? = null,
    val departmentId: Long? = null,
    val serviceId: Long? = null,
    val date: String,
    val time: String,
    val status: String = "مجدولة", // "مجدولة", "حضر", "لم يحضر", "أُلغي", "جارية", "مكتملة"
    val isDeductedFromPackage: Boolean = false,
    val notes: String = "",
    val branchId: Long = 1,
    val attendedAt: Long? = null
)
