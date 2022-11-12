package com.example.plugins

import com.example.auth.JwtService
import com.example.auth.hashPassword
import com.example.repository.NoteRepository
import com.example.repository.UserRepository
import com.example.routes.todoRoute
import com.example.routes.userRoute
import io.ktor.server.routing.*
import io.ktor.server.locations.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureRouting() {
    install(Locations) {
    }

    val userDb = UserRepository()
    val jwt = JwtService()
    val todoDb = NoteRepository()
    val hash = {s : String -> hashPassword(s)}

    routing {

        get("/") {
            call.respond("Hello There!")
        }

        userRoute(
            userDb = userDb,
            jwtService = jwt,
            todoDb = todoDb,
            hash = hash
        )

        todoRoute(
            userDb= userDb,
            todoDb = todoDb,
            hash = hash
        )


//        get("/token"){
//            val name = call.request.queryParameters["name"]!!
//            val email = call.request.queryParameters["email"]!!
//            val password = call.request.queryParameters["password"]!!
//
//            val userId =
//
//            val user = User(
//                name = name,
//                email= email,
//                password = hash(password)
//            )
//
//        }
    }
}