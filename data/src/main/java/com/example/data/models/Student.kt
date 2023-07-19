package com.example.data.models

/**
 * Class storage for the Student objects
 * @param studentId  the id of the student in the database
 * @param age age to make better recommendations and greater personalization
 * @param dailyStudyTimeMinutes  the number of minutes student would like study daily
 * @param interests  the list of Tags(disciplines) in which student interested
 * @param bricksCollected shows the total number of lessons completed
 * @param enrolledCourses
 **/
data class Student(
    val studentId: String,
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>,
    var bricksCollected: Int = 0,
    var enrolledCourses: List<String> = emptyList()
) {
    fun toFirebaseStudent(): FirebaseStudent {
        return FirebaseStudent(
            age = this.age,
            dailyStudyTimeMinutes = this.dailyStudyTimeMinutes,
            interests = this.interests,
            bricksCollected = this.bricksCollected,
            enrolledCourses = this.enrolledCourses
        )
    }
}


data class FirebaseStudent(
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>,
    val bricksCollected: Int = 0,
    val enrolledCourses: List<String> = emptyList()
){
    fun toStudent(studentId: String): Student {
        return Student(
            studentId,
            age,
            dailyStudyTimeMinutes,
            interests,
            bricksCollected,
            enrolledCourses
        )
    }
}
