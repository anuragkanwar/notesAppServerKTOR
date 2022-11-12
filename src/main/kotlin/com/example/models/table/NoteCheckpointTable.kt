package com.example.models.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object NoteCheckpointTable : Table() {

    val ckptId : Column<Int> = integer("ckptId").autoIncrement()

    val noteId : Column<String> = varchar("noteId",512).references(NoteTable.noteId, onDelete = ReferenceOption.CASCADE)
    val userId : Column<Int> = integer("userId").references(UserTable.userId,onDelete = ReferenceOption.CASCADE)

    val checked : Column<Boolean> = bool("checked")
    val content : Column<String> = text("content")

    override val primaryKey: PrimaryKey = PrimaryKey(ckptId)
}