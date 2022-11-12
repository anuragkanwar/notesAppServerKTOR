package com.example.models.user_models

data class UpdatePassword(
    val email : String,
    val newPassword : String
)
