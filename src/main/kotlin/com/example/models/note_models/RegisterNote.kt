package com.example.models.note_models

data class RegisterNote(
    val noteId : String,
    val title : String,
    val description : String,
    val color : Int,
    val date : Long,
    val locked : Boolean,
    var safePassword: String? = null,
    val label : String,
    val todoCheckpoint: List<Checkpoints>
)
