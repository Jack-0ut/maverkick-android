package com.example.data.models

/**
 * Class storage for the Student objects
 * @param studentId  the id of the student in the database
 * @param age age to make better recommendations and greater personalization
 * @param dailyStudyTimeMinutes  the number of minutes student would like study daily
 * @param interests  the list of Tags(disciplines) in which student interested
 **/

data class Student(
    val studentId: String,
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>
) {
    fun toFirebaseStudent(): FirebaseStudent {
        return FirebaseStudent(
            age = this.age,
            dailyStudyTimeMinutes = this.dailyStudyTimeMinutes,
            interests = this.interests
        )
    }
}


data class FirebaseStudent(
    val age: Int,
    val dailyStudyTimeMinutes: Int,
    val interests: List<String>
){
    fun toStudent(studentId: String): Student {
        return Student(
            studentId,
            age,
            dailyStudyTimeMinutes,
            interests
        )
    }
}
