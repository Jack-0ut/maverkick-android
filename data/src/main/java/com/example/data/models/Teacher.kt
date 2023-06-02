package com.example.data.models


/**
 * Class storage for the Teacher objects
 * @param teacherId - id of the current object
 * @param userId - id of the user who is teacher
 * @param skills - the list of the skills teacher would like to share
 **/
data class Teacher(
    val teacherId: String,
    val userId: String,
    val skills: List<String>
)
