package com.maverkick.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject

/**
 * A concrete implementation of [AuthenticationService] using Firebase's authentication.
 *
 * This class abstracts the Firebase authentication, allowing for more flexible and
 * testable code, by depending on abstractions rather than concrete implementations.
 *
 * @property firebaseAuth An instance of FirebaseAuth to handle authentication operations.
 */
class FirebaseAuthenticationService @Inject constructor(private val firebaseAuth: FirebaseAuth) : AuthenticationService {

    /**
     * Retrieves the currently authenticated user from Firebase.
     * Returns `null` if no user is authenticated.
     */
    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Signs out the currently authenticated user using Firebase's authentication mechanism.
     */
    override fun logout() {
        firebaseAuth.signOut()
    }
}