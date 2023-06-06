package com.example.data.models

/**
 * Class storage for the Student objects
 * @param studentId  the id of the student in the database
 * @param userId  the id of the user, which student is
 * @param age age to make better recommendations and greater personalization
 * @param dailyStudyTimeMinutes  the number of minutes student would like study daily
 * @param interests  the list of Tags(disciplines) in which student interested
 **/

data class Student(
    var studentId: String,
    val userId: String,
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>
)