package com.example.data.sharedpref

import android.content.SharedPreferences
import com.example.data.models.Student
import com.example.data.models.Teacher
import com.example.data.models.User
import com.google.gson.Gson
import javax.inject.Inject

/**
 * Class that manages Shared Preferences:
 * userID, studentID,teacherID,role
 **/
class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {
    private val gson = Gson()

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

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString("user", userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString("user", null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun saveStudent(student: Student) {
        val studentJson = gson.toJson(student)
        sharedPreferences.edit().putString("student", studentJson).apply()
    }

    fun getStudent(): Student? {
        val studentJson = sharedPreferences.getString("student", null)
        return studentJson?.let { gson.fromJson(it, Student::class.java) }
    }

    fun saveTeacher(teacher: Teacher) {
        val teacherJson = gson.toJson(teacher)
        sharedPreferences.edit().putString("teacher", teacherJson).apply()
    }

    fun getTeacher(): Teacher? {
        val teacherJson = sharedPreferences.getString("teacher", null)
        return teacherJson?.let { gson.fromJson(it, Teacher::class.java) }
    }

    /** Clear the entire preferences */
    fun clearPreferences() {
        sharedPreferences.edit().clear().apply()
    }
}
