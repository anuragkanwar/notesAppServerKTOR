package com.example.models.response

data class SimpleResponse(
    val success : Boolean,
    val message : String,
    val data : Any? = null
)
