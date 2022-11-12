package com.example.models.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table


object NoteTable : Table() {

    val noteId : Column<String> = varchar("noteId",512)
    val userId : Column<Int> = integer("userId").references(UserTable.userId, onDelete = ReferenceOption.CASCADE)

    val date : Column<Long> = long("date")
    val title : Column<String> = text("title")
    val description : Column<String> = text("description")
    val label : Column<String> = varchar("label",32)

    val color : Column<Int> = integer("color")
    val locked : Column<Boolean> = bool("locked")


    override val primaryKey: PrimaryKey = PrimaryKey(noteId)
}