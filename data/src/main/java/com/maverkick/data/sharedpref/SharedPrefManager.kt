package com.maverkick.data.sharedpref

import android.content.SharedPreferences
import com.google.gson.Gson
import com.maverkick.data.models.Student
import com.maverkick.data.models.Teacher
import com.maverkick.data.models.User
import javax.inject.Inject

/**
 * Class that manages Shared Preferences:
 * User,Student,Teacher objects and role User has right now
 **/
class SharedPrefManager @Inject constructor(private val sharedPreferences: SharedPreferences) {
    private val gson = Gson()

    fun saveActiveRole(role: String) {
        sharedPreferences.edit().putString("activeRole", role).apply()
    }

    fun getActiveRole(): String? {
        return sharedPreferences.getString("activeRole", null)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString("user", userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString("user", null)
        return userJson?.let { gson.fromJson(it, User::class.java) }
    }

    fun clearUser() {
        sharedPreferences.edit().remove("user").apply()
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

    fun setNeedsRefresh(value: Boolean) {
        sharedPreferences.edit().putBoolean(NEEDS_REFRESH_KEY, value).apply()
    }

    fun needsRefresh(): Boolean {
        return sharedPreferences.getBoolean(NEEDS_REFRESH_KEY, false)
    }

    fun decrementCourseGenerationTries() {
        getStudent()?.let { student ->
            if (student.courseGenerationTries > 0) {
                student.courseGenerationTries -= 1
                saveStudent(student)
            }
        }
    }

    /** Add new course to the list of finished **/
    fun updateFinishedCourse(courseId: String) {
        getStudent()?.let { student ->
            val updatedCourses = student.finishedCourses + courseId
            val updatedStudent = student.copy(finishedCourses = updatedCourses)
            saveStudent(updatedStudent)
        }
    }

    /** When registered, set that user is onboarded as Student | Teacher**/
    fun setIsOnboarded(value: Boolean) {
        sharedPreferences.edit().putBoolean("isOnboarded", value).apply()
    }

    /** When registered, check if user is onboarded as Student | Teacher**/
    fun getIsOnboarded(): Boolean {
        return sharedPreferences.getBoolean("isOnboarded", false)
    }

    companion object {
        private const val NEEDS_REFRESH_KEY = "NEEDS_REFRESH"
    }

}
