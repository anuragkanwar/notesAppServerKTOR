package com.example.models.user_models


data class RegisterRequest(
    val email : String,
    val name : String,
    val password : String,
    val imageUrl : String
)