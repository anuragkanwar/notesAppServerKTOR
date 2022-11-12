package com.example.data.dao

import com.example.models.note_models.*

interface NoteDao {

    suspend fun createNote(
        userId: Int,
        note: RegisterNote
    ): Unit

    suspend fun createNoteCheckpoints(
        userId: Int,
        noteId: String,
        noteCheckpoint: List<Checkpoints>
    ): Unit

    suspend fun getAllNotes(
        userId: Int
    ): List<NoteItem>

    suspend fun getNoteById(
        noteId: String,
        userId: Int
    ): NoteItem

    suspend fun deleteNote(
        noteId: String,
        userId: Int
    ): Int

    suspend fun deleteAllNotes(
        userId: Int
    ): Int

    suspend fun deleteNoteCheckPoints(
        userId: Int,
        noteId: String
    ): Int

    suspend fun deleteCheckPointById(
        userId: Int,
        noteId: String,
        ckptId: Int,
    ): Int

    suspend fun updateNoteItem(
        noteId: String,
        userId: Int,
        note: RegisterNote,
    ): Int


    suspend fun deleteAllCheckPoints(
        userId: Int
    ): Int

    suspend fun setLock(
        userId: Int,
        noteId: String,
        safePassword: String
    ): Int

}