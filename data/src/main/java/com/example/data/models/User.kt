package com.example.data.models

/**
 * Data Class for User to store it's locally in the database
 * @param userId - id of the user
 * @param username - username for the user, which identify it among the others and displayed in the app
 * @param email - the way for user registration and login
 * @param profilePicture - the picture of the profile for the user, by default we show some template picture
 **/

data class User(
    val userId: String,
    val username: String,
    val email: String,
    val profilePicture:String
)
