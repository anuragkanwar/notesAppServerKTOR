package com.example.models.note_models

data class NoteItem(
    val noteId : String,
    val title : String,
    val description : String,
    val color : Int,
    val date : Long,
    val locked : Boolean,
    val label : String,
    val todoCheckpoint: List<Checkpoints>
)