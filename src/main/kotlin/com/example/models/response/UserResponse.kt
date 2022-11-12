package com.example.models.response

import com.example.models.user_models.ResultedUser
import com.example.models.user_models.User

data class UserResponse(
    val token : String,
    val user : ResultedUser
)
