package com.example.teacher.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.sharedpref.SharedPrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the TeacherProfileSettingsFragment
 * This ViewModel class fetches the Teacher data from either cloud repository or shared preferences
 * and put it's areas of expertise tags on the screen
 **/

@HiltViewModel
class TeacherProfileSettingsViewModel @Inject constructor(
    private val sharedPrefManager: SharedPrefManager
) : ViewModel() {

    private val _expertiseList = MutableLiveData<List<String>>()
    val expertiseList: LiveData<List<String>> get() = _expertiseList

    init {
        fetchTeacherData()
    }
    private fun fetchTeacherData(){
        _expertiseList.value = sharedPrefManager.getTeacher()?.expertise ?: emptyList()
    }


}
