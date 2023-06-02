package com.example.data.sharedpref

import android.content.SharedPreferences
import javax.inject.Inject

/**
 * Class that manages Shared Preferences:
 * userID, studentID,teacherID,role
 **/
class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("userId", userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("userId", null)
    }

    fun saveActiveRole(role: String) {
        sharedPreferences.edit().putString("activeRole", role).apply()
    }

    fun getActiveRole(): String? {
        return sharedPreferences.getString("activeRole", null)
    }

    fun saveStudentId(studentId: String) {
        sharedPreferences.edit().putString("studentId", studentId).apply()
    }

    fun getStudentId(): String? {
        return sharedPreferences.getString("studentId", null)
    }

    fun saveTeacherId(teacherId: String) {
        sharedPreferences.edit().putString("teacherId", teacherId).apply()
    }

    fun getTeacherId(): String? {
        return sharedPreferences.getString("teacherId", null)
    }

    fun saveUsername(username:String){
        sharedPreferences.edit().putString("username",username).apply()
    }

    fun getUsername(): String?{
        return sharedPreferences.getString("username",null)
    }

    fun saveProfilePicPath(path: String) {
        sharedPreferences.edit().putString("profilePicPath", path).apply()
    }

    fun getProfilePicPath(): String? {
        return sharedPreferences.getString("profilePicPath", null)
    }

    /** Clear the entire preferences */
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}
