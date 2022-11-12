package com.example.routes

import com.example.models.note_models.RegisterNote
import com.example.models.note_models.SafePassword
import com.example.models.response.SimpleResponse
import com.example.models.user_models.User
import com.example.repository.NoteRepository
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.todoRoute(
    userDb: UserRepository,
    todoDb: NoteRepository,
    hash: (String) -> String
) {
    authenticate("jwt") {

        post("/v1/notes/create_note") {
            val note = try {
                call.receive<RegisterNote>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@post
            }

            try {
                val userId = call.principal<User>()!!.userId

                todoDb.createNote(userId, note)
                todoDb.createNoteCheckpoints(userId, note.noteId, note.todoCheckpoint)

                if (note.locked) {
                    if (note.safePassword == null) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            SimpleResponse(false, "Required a safe password for a locked note...")
                        )
                        return@post
                    }
                    val safePassword = hash(note.safePassword!!)
                    val res = todoDb.setLock(userId, note.noteId, safePassword)
                    if(res != 1)
                    {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            SimpleResponse(false, "Password Does not Match initial password!")
                        )
                    }
                }

                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Created Successfully..."))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Some problem occurred...")
                )
            }
        }

        get("/v1/notes/load_notes") {
            try {
                val userId = call.principal<User>()!!.userId

                val noteItems = todoDb.getAllNotes(userId)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, message = "Loaded Notes...", data = noteItems)
                )

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(success = false, message = e.message ?: "Some problem occurred...")
                )
            }
        }


        post("/v1/notes/{id}") {
            val noteId = try {
                call.parameters["id"]!!.toString()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@post
            }

            val spwd = try {
                call.receive<SafePassword>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@post
            }

            try {
                val userId = call.principal<User>()!!.userId
                val user = userDb.findUserById(userId)!!

                val noteItem = todoDb.getNoteById(noteId, userId)

                if (noteItem.locked) {
                    if (spwd.safePassword != null) {
                        if (user.safePassword == hash(spwd.safePassword)) {
                            call.respond(
                                HttpStatusCode.OK,
                                SimpleResponse(true, message = "Here's the note...", data = noteItem)
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                SimpleResponse(false, "Safe Password Does not match...")
                            )
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            SimpleResponse(false, "Safe Password Not Provided...")
                        )
                    }
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(true, message = "Here's the note!!", noteItem)
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Some problem occurred...")
                )
            }
        }


        put("/v1/notes/update_note") {
            val note = try {
                call.receive<RegisterNote>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@put
            }
            try {
                val userId = call.principal<User>()!!.userId

                val cnt1 = todoDb.updateNoteItem(noteId = note.noteId, userId = userId, note = note)
                todoDb.deleteNoteCheckPoints(userId, note.noteId)
                todoDb.createNoteCheckpoints(userId, note.noteId, note.todoCheckpoint)

                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Updated Successfully..."))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Some problem occurred...")
                )
            }
        }


        put("/v1/notes/set_lock/{id}") {
            val noteId = try {
                call.parameters["id"]!!.toString()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@put
            }

            val spwd = try {
                call.receive<SafePassword>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@put
            }

            try {
                val userId = call.principal<User>()!!.userId
                val cnt = todoDb.setLock(userId, noteId, hash(spwd.safePassword!!))
                if (cnt == 1) {
                    call.respond(
                        HttpStatusCode.OK,
                        SimpleResponse(true, "Lock Placed..")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SimpleResponse(false, "Failed...")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Some problem occurred...")
                )
            }
        }


        delete("/v1/notes/delete_all_notes") {
            try {
                val userId = call.principal<User>()!!.userId
                val cnt = todoDb.deleteAllNotes(userId)
                call.respond(
                    HttpStatusCode.OK,
                    SimpleResponse(true, message = "Deleted All Notes", data = cnt)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Some problem occurred...")
                )
            }
        }

        delete("/v1/notes/delete_note/{id}") {
            val noteId = try {
                call.parameters["id"]!!.toString()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    SimpleResponse(false, e.message.toString())
                )
                return@delete
            }

            try {
                val userId = call.principal<User>()!!.userId

                val cnt = todoDb.deleteNote(noteId, userId)
                if (cnt == 0) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        SimpleResponse(false, "Could Not delete...")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK, SimpleResponse(true, "Note Deleted Successfully...")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict,
                    SimpleResponse(false, e.message ?: "Some problem occurred...")
                )
            }
        }
    }
}