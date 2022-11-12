package com.example.models.note_models

data class Note(
    val noteId: String,
    val userId: Int,
    val date: Long,
    val title: String,
    val description: String,
    val color: Int,
    val locked: Boolean,
    val label : String
)
