package com.maverkick.profile

import android.net.Uri
import androidx.lifecycle.LiveData


/** Base interface that defines the common functional for the User Profile **/
interface ProfileViewModelInterface {
    val username: LiveData<String>
    val profilePicture: LiveData<Uri>
    fun clearPreferences()
    fun logout()
    fun updateProfilePicture(uri: Uri)
}
