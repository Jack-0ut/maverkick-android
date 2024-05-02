package com.maverkick.data.api

data class ApiResult<T>(
    val body: T?,
    val isSuccess: Boolean,
    val errorMessage: String? = null
)
