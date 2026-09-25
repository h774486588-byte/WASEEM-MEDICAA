package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileNumber: String,
    val name: String,
    val phone: String,
    val gender: String, // "ذكر", "أنثى"
    val dateOfBirth: String,
    val address: String,
    val maritalStatus: String, // "أعزب", "متزوج", "أخرى"
    val profession: String,
    val referralSource: String,
    val doctorId: Long?,
    val therapistId: Long?,
    val departmentId: Long?,
    val serviceId: Long? = null,
    val diagnosis: String,
    val complaint: String,
    val notes: String,
    val balanceDue: Double = 0.0, // رصيد مستحق على المريض
    val branchId: Long = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val specialization: String,
    val phone: String,
    val departmentId: Long,
    val workingDays: String,
    val workingHours: String,
    val servicePrice: Double,
    val commissionRate: Double = 25.0, // نسبة مئوية
    val isActive: Boolean = true
)

@Entity(tableName = "therapists")
data class Therapist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val specialization: String,
    val phone: String,
    val departmentId: Long,
    val workingDays: String,
    val workingHours: String,
    val commissionRate: Double = 20.0,
    val isActive: Boolean = true
)

@Entity(tableName = "departments")
data class Department(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val iconName: String = "health"
)

@Entity(tableName = "services")
data class MedicalService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val departmentId: Long,
    val price: Double,
    val durationMinutes: Int = 45,
    val doctorId: Long? = null
)

@Entity(tableName = "diagnoses")
data class DiagnosisItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val description: String
)
