package com.example.maverkick

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

/**
 * The SplashActivity class is responsible for displaying a splash screen
 * when the app is launched and then navigating the user to the appropriate activity
 * depending on whether they are signed in or not.
 */
class SplashActivity : AppCompatActivity() {
    // Define the duration of the splash screen in milliseconds
    private val splashDuration = 3000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the activity layout to activity_splash.xml
        setContentView(R.layout.activity_splash)

        // Wait for the duration of the splash screen, then execute the code inside the Handler
        Handler(Looper.getMainLooper()).postDelayed({
            // Define the activity to start depending on whether the user is signed in
            val activityToStart = if (isUserSignedIn()) {
                MainActivity::class.java // If the user is signed in, start MainActivity
            } else {
                OnboardActivity::class.java // If the user is not signed in, start OnboardActivity
            }
            // Start the activity and finish this activity to remove it from the stack
            startActivity(Intent(this, activityToStart))
            finish()
        }, splashDuration)
    }

    // Check if the user is signed in by reading from the user_preferences shared preferences file
    private fun isUserSignedIn(): Boolean {
        val sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE)
        return sharedPreferences.getBoolean("is_signed_in", false)
    }
}

