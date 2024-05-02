package com.maverkick.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.maverkick.auth.RegistrationActivity
import com.maverkick.auth.onboarding.student.StudentOnboarding
import com.maverkick.data.sharedpref.SharedPrefManager
import com.maverkick.student.StudentMainActivity
import com.maverkick.teacher.TeacherMainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.crypto.AEADBadTagException
import javax.inject.Inject

/**The starter class for our application
 * It decides whether we should proceed with the
 * authentication or to the Student/Teacher main page
 */
@AndroidEntryPoint
class LaunchActivity : AppCompatActivity() {

    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var sharedPrefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val currentUser = auth.currentUser
        if(currentUser != null) {
            // check for the shared preferences encryption key exception
            try {
                checkUserRoleAndNavigate()
            } catch (e: AEADBadTagException) {
                handleDecryptionError()
            }
        } else {
            redirectToRegistration()
        }
    }

    private fun handleDecryptionError() {
        sharedPrefManager.clearPreferences()
        // Stop further execution and redirect to registration immediately.
        redirectToRegistration()
    }

    private fun redirectToRegistration() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }

    /** Check the role of user (Student or Teacher) from shared preferences
     * and navigate to the main page of the role
     */
    private fun checkUserRoleAndNavigate() {
        val role = sharedPrefManager.getActiveRole() // This might throw AEADBadTagException
        navigateToMainActivity(role)
    }

    /** Navigate to the main activity based on the role **/
    private fun navigateToMainActivity(role: String?) {
        val intent = when(role) {
            "student" -> Intent(this, StudentMainActivity::class.java)
            "teacher" -> Intent(this, TeacherMainActivity::class.java)
            else -> Intent(this, StudentOnboarding::class.java)
        }
        startActivity(intent)
        finish()
    }
}
