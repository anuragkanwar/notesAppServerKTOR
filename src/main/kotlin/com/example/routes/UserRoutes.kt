package com.example.routes

import com.example.auth.JwtService
import com.example.models.response.SimpleResponse
import com.example.models.response.UserResponse
import com.example.models.user_models.*
import com.example.repository.NoteRepository
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


const val API_VERSION = "/v1"
const val USERS = "$API_VERSION/users"
const val REGISTER_REQUEST = "$USERS/register"
const val LOGIN_REQUEST = "$USERS/login"


fun Route.userRoute(
    userDb: UserRepository, todoDb: NoteRepository, jwtService: JwtService, hash: (String) -> String
) {

    get("/v1") {
        call.respondText(
            "Hello there", status = HttpStatusCode.OK
        )
    }


    post("/v1/users/register_user") {
        val registerRequest = try {
            call.receive<RegisterRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest, SimpleResponse(success = false, message = e.message.toString())
            )
            return@post
        }

        try {
            val currUser = userDb.createUser(
                registerRequest.name, registerRequest.email, hash(registerRequest.password), registerRequest.imageUrl
            )

            if (currUser == null) {
                call.respond(
                    HttpStatusCode.BadRequest, SimpleResponse(false, "Can't Create User...")
                )
                return@post
            }
            call.respond(
                HttpStatusCode.OK, SimpleResponse(
                    success = true,
                    message = "User Created Successfully",
                    data = UserResponse(
                        token = jwtService.generateToken(currUser),
                        user = ResultedUser(name = currUser.name, email = currUser.email, imageUrl = currUser.imageUrl)
                    )
                )
            )
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Conflict,
                SimpleResponse(success = false, message = e.message ?: "Some Problem occurred.")
            )
        }
    }

    post("/v1/users/login") {
        val loginRequest = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.BadRequest, SimpleResponse(success = false, message = e.message.toString())
            )
            return@post
        }

        try {
            val currUser = userDb.findUserByEmail(loginRequest.email)

            if (currUser == null) {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Wrong Email Id"))
                return@post
            }

            if (currUser.password == hash(loginRequest.password)) {
                call.respond(
                    HttpStatusCode.OK, SimpleResponse(
                        success = true,
                        message = "User Logged In..",
                        data = UserResponse(
                            token = jwtService.generateToken(currUser),
                            user = ResultedUser(
                                name = currUser.name, email = currUser.email, imageUrl = currUser.imageUrl
                            )
                        )
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest, SimpleResponse(false, "Password Incorrect!!"))
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem occurred.")
            )
        }
    }

    authenticate("jwt") {

        put("/v1/users/edit_profile") {
            val parameter = try {
                call.receive<UpdateProfile>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, SimpleResponse(success = false, message = e.message.toString())
                )
                return@put
            }

            try {
                val userId = call.principal<User>()!!.userId
                userDb.updateUser(userId, parameter.name, parameter.email, parameter.imageUrl)
                val currUser = userDb.findUserByEmail(parameter.email)!!
                call.respond(
                    HttpStatusCode.OK, SimpleResponse(
                        success = true,
                        message = "Edited profile...",
                        data = ResultedUser(
                            name = currUser.name, email = currUser.email, imageUrl = currUser.imageUrl
                        )
                    )
                )
            } catch (e: Throwable) {
                call.respond(
                    HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem occurred.")
                )
            }
        }


//        put("/v1/users/update_password") {
//            val parameter = try {
//                call.receive<UpdatePassword>()
//            } catch (e: Exception) {
//                call.respond(
//                    HttpStatusCode.BadRequest,
//                    SimpleResponse(false, "Missing Fields")
//                )
//                return@put
//            }
//
//            try {
//                val userId = call.principal<User>()!!.userId
//
//                val user = userDb.findUserById(userId)
//                if (user!!.email != parameter.email) {
//                    call.respond(
//                        HttpStatusCode.BadRequest,
//                        SimpleResponse(false, "Wrong Email Id")
//                    )
//                    return@put
//                }
//
//                userDb.updatePassword(userId, hash(parameter.newPassword))
//                call.respond(HttpStatusCode.OK, SimpleResponse(true, "Updated Successfully.."))
//            } catch (e: Throwable) {
//                call.respond(
//                    HttpStatusCode.Conflict,
//                    SimpleResponse(false, e.message ?: "Some Problem occurred.")
//                )
//            }
//        }


        delete("/v1/users/delete_account") {
            try {
                val userId = call.principal<User>()!!.userId
                userDb.deleteUser(userId)
                call.respond(
                    HttpStatusCode.OK, SimpleResponse(true, "User Deleted Successfully")
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Conflict, SimpleResponse(false, e.message ?: "Some Problem occurred.")
                )
            }
        }
    }

}