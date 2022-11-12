package com.example.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.example.auth.JwtService
import com.example.auth.MySession
import com.example.models.response.SimpleResponse
import com.example.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.sessions.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSecurity() {

    val userDb = UserRepository()
    val jwt = JwtService()

    authentication {
        jwt("jwt") {
            verifier(jwt.verifier)
            realm = "Todo server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("userId")
                val claimInt = claim.asInt()
                val user = userDb.findUserById(claimInt)
                user
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    SimpleResponse(false,"Token is not valid or has expired")
                )
            }
        }
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    routing {
//        get("/session/increment") {
//            val session = call.sessions.get<MySession>() ?: MySession()
//            call.sessions.set(session.copy(count = session.count + 1))
//            call.respondText("Counter is ${session.count}. Refresh to increment.")
//        }
    }
}
