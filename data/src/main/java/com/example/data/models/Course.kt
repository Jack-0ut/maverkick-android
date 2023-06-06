package com.example.data.models

/**
 * Class storage for the Course objects
 * @param courseId - Int
 * @param courseName- String
 * @param language - String
 * @param poster - String
 * @param tags - List<Tag>
 **/

data class Course(
    val courseId:String,
    val courseName: String,
    val language: String,
    val poster: String,
    val tags: List<String>
)
