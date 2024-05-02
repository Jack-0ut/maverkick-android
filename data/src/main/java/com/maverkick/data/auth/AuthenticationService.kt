package com.maverkick.data.auth

import com.google.firebase.auth.FirebaseUser

/**
 * Defines the contract for an authentication service, abstracting the
 * underlying authentication mechanism.
 */
interface AuthenticationService {

    /**
     * Represents the currently authenticated user. Returns `null` if no user is authenticated.
     */
    val currentUser: FirebaseUser?

    /**
     * Performs a logout operation, signing out the authenticated user.
     */
    fun logout()
}