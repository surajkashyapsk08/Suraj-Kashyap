package com.example.data.auth

data class StudentProfile(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val studentClass: String = "",
    val role: String = "student",
    val createdAt: Long = System.currentTimeMillis(),
    val streakDays: Int = 0,
    val studyMinutes: Int = 0,
    val completedChapters: Int = 0
)
