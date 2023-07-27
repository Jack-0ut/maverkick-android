package com.maverkick.data.models


/**
 * Class storage for the Teacher objects
 * @param teacherId - id of the current object
 * @param fullName - the name,which will be displayed, when student look at the course
 * @param expertise - the list of teacher areas of expertise
 **/
data class Teacher(
    var teacherId: String,
    val fullName: String,
    val country: String,
    val expertise: List<String>
) {
    fun toFirebaseTeacher(): FirebaseTeacher {
        return FirebaseTeacher(
            fullName = this.fullName,
            country = this.country,
            expertise = this.expertise
        )
    }
}

data class FirebaseTeacher(
    val fullName: String,
    val country: String,
    val expertise: List<String>
){
    fun toTeacher(teacherId: String): Teacher {
        return Teacher(
            teacherId,
            fullName,
            country,
            expertise
        )
    }
}
