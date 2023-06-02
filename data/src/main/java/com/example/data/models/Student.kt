package com.example.data.models

/**
 * Class storage for the Student objects
 * @param studentId - Int
 * @param userId - Int
 * @param age - Int
 * @param dailyStudyTimeMinutes - Int
 * @param interests - List<String>
 **/

data class Student(
    val studentId: String,
    val userId: String,
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>
)