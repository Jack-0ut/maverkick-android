package com.example.data.models


/**
 * Class storage for the Teacher objects
 * @param teacherId - id of the current object
 * @param userId - id of the user who is teacher
 * @param fullName - the name,which will be displayed, when student look at the course
 * @param expertise - the list of teacher areas of expertise
 **/
data class Teacher(
    var teacherId: String,
    val userId: String,
    val fullName: String,
    val expertise: List<String>
)