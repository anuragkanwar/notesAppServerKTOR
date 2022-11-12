package com.example.repository

import com.example.data.dao.NoteDao
import com.example.models.note_models.*
import com.example.models.table.NoteCheckpointTable
import com.example.models.table.NoteTable
import com.example.models.table.UserTable
import com.example.models.user_models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class NoteRepository : NoteDao {
    override suspend fun createNote(
        userId: Int,
        note: RegisterNote
    ): Unit = DatabaseFactory.dbQuery {
        NoteTable.insert{ tbl ->
            tbl[NoteTable.noteId] = note.noteId
            tbl[NoteTable.userId] = userId
            tbl[NoteTable.title] = note.title
            tbl[NoteTable.description] = note.description
            tbl[NoteTable.color] = note.color
            tbl[NoteTable.locked] = note.locked
            tbl[NoteTable.date] = note.date
            tbl[NoteTable.label] = note.label
        }
    }

    override suspend fun createNoteCheckpoints(userId: Int, noteId: String, noteCheckpoint: List<Checkpoints>): Unit =
        DatabaseFactory.dbQuery {
            NoteCheckpointTable.batchInsert(noteCheckpoint, shouldReturnGeneratedValues = false) { ckpt ->
                this[NoteCheckpointTable.noteId] = noteId
                this[NoteCheckpointTable.userId] = userId
                this[NoteCheckpointTable.checked] = ckpt.checked
                this[NoteCheckpointTable.content] = ckpt.content
            }
        }


    override suspend fun getAllNotes(userId: Int): List<NoteItem> = DatabaseFactory.dbQuery {
        val notes = NoteTable.select {
            NoteTable.userId.eq(userId)
        }.orderBy(NoteTable.date to SortOrder.DESC)
            .mapNotNull {
                rowToTodo(it)
            }

        val todoItems = mutableListOf<NoteItem>()

        notes.forEach { note ->
            val noteckpt = NoteCheckpointTable.select {
                NoteCheckpointTable.noteId.eq(note.noteId)
            }.mapNotNull {
                rowToCkpt(it)
            }

            val item = NoteItem(
                noteId = note.noteId,
                title = note.title,
                description = note.description,
                color = note.color,
                locked = note.locked,
                date = note.date,
                todoCheckpoint = noteckpt,
                label = note.label
            )
            todoItems.add(item)
        }

        todoItems
    }

    override suspend fun getNoteById(noteId: String, userId: Int): NoteItem = DatabaseFactory.dbQuery {
        val note = NoteTable.select {
            NoteTable.noteId.eq(noteId) and NoteTable.userId.eq(userId)
        }.mapNotNull {
            rowToTodo(it)
        }[0]

        val ckpt = (NoteCheckpointTable.select {
            NoteCheckpointTable.noteId.eq(note.noteId)
        }.mapNotNull {
            rowToCkpt(it)
        })

        val item = NoteItem(
            noteId = note.noteId,
            title = note.title,
            description = note.description,
            color = note.color,
            locked = note.locked,
            date = note.date,
            todoCheckpoint = ckpt,
            label = note.label
        )

        item
    }

    override suspend fun deleteNote(noteId: String, userId: Int): Int = DatabaseFactory.dbQuery {
        val changed = NoteTable.deleteWhere {
            NoteTable.userId.eq(userId) and NoteTable.noteId.eq(noteId)
        }
        val cnt =
            NoteTable.select { NoteTable.userId.eq(userId) and NoteTable.locked.eq(true) }
                .count().toInt()
        if (cnt == 0) {
            UserTable.update(where = { UserTable.userId.eq(userId) }) { tbl ->
                tbl[UserTable.safePassword] = null
            }
        }

        changed
    }

    override suspend fun deleteAllNotes(userId: Int): Int = DatabaseFactory.dbQuery {
        NoteTable.deleteWhere {
            NoteTable.userId.eq(userId)
        }
    }

    override suspend fun deleteNoteCheckPoints(userId: Int, noteId: String): Int = DatabaseFactory.dbQuery {
        NoteCheckpointTable.deleteWhere {
            NoteCheckpointTable.userId.eq(userId) and NoteCheckpointTable.noteId.eq(noteId)
        }
    }

    override suspend fun deleteCheckPointById(userId: Int, noteId: String, ckptId: Int): Int = DatabaseFactory.dbQuery {
        NoteCheckpointTable.deleteWhere {
            NoteCheckpointTable.userId.eq(userId) and NoteCheckpointTable.noteId.eq(noteId) and NoteCheckpointTable.ckptId.eq(
                ckptId
            )
        }
    }

    override suspend fun deleteAllCheckPoints(userId: Int): Int = DatabaseFactory.dbQuery {
        NoteCheckpointTable.deleteWhere {
            NoteCheckpointTable.userId.eq(userId)
        }
    }

    override suspend fun setLock(userId: Int, noteId: String, safePassword: String): Int = DatabaseFactory.dbQuery {
        NoteTable.update(where = { NoteTable.noteId.eq(noteId) and NoteTable.userId.eq(userId) }) { tbl ->
            tbl[NoteTable.locked] = true
        }

        val user = UserTable.select{UserTable.userId.eq(userId)}.mapNotNull {
            rowToUser(it)
        }[0]

        val currSafePassword = user.safePassword
        if(currSafePassword == null) {
            UserTable.update(where = { UserTable.userId.eq(userId) }) { tbl ->
                tbl[UserTable.safePassword] = safePassword
            }
            1
        }else{
            if(currSafePassword == safePassword)
                1
            else
                -1
        }
    }

    override suspend fun updateNoteItem(noteId: String, userId: Int, note: RegisterNote): Int = DatabaseFactory.dbQuery {
        NoteTable.update(
            where = {
                NoteTable.userId.eq(userId) and NoteTable.noteId.eq(noteId)
            }
        ) { tbl ->
            tbl[NoteTable.title] = note.title
            tbl[NoteTable.description] = note.description
            tbl[NoteTable.color] = note.color
            tbl[NoteTable.locked] = note.locked
            tbl[NoteTable.date] = note.date
            tbl[NoteTable.label] = note.label
        }


        val cnt =
            NoteTable.select { NoteTable.userId.eq(userId) and NoteTable.locked.eq(true) }
                .count().toInt()
        if (cnt == 0) {
            UserTable.update(where = { UserTable.userId.eq(userId) }) { tbl ->
                tbl[UserTable.safePassword] = null
            }
        }

        NoteTable.select { NoteTable.userId.eq(userId) and NoteTable.noteId.eq(noteId) }.count().toInt()
    }

    private fun rowToUser(row: ResultRow?): User? {
        if (row == null)
            return null
        return User(
            name = row[UserTable.name],
            userId = row[UserTable.userId],
            email = row[UserTable.email],
            password = row[UserTable.password],
            safePassword = row[UserTable.safePassword],
            imageUrl = row[UserTable.imageUrl]
        )
    }

    private fun rowToCkpt(row: ResultRow?): Checkpoints? {
        if (row == null)
            return null
        return Checkpoints(
            checked = row[NoteCheckpointTable.checked],
            content = row[NoteCheckpointTable.content]
        )
    }

    private fun rowToTodo(row: ResultRow?): Note? {
        if (row == null)
            return null
        return Note(
            noteId = row[NoteTable.noteId],
            userId = row[NoteTable.userId],
            color = row[NoteTable.color],
            date = row[NoteTable.date],
            title = row[NoteTable.title],
            description = row[NoteTable.description],
            locked = row[NoteTable.locked],
            label = row[NoteTable.label]
        )
    }
}