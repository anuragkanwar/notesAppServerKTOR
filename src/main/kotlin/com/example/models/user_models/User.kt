package com.example.models.user_models

import io.ktor.server.auth.*

data class User(
    val userId : Int,
    val email : String,
    val name : String,
    val password : String,
    val safePassword : String?,
    val imageUrl : String
) : Principal
