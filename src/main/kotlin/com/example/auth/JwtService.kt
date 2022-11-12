package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.models.user_models.User
import java.util.Date

class JwtService {

    private val issuer = "Server"
    private val jwtSecret = System.getenv("JWT_SECRET")

    private val algo = Algorithm.HMAC512(jwtSecret)

    val verifier : JWTVerifier = JWT
        .require(algo)
        .withIssuer(issuer)
        .build()

    fun generateToken(user: User) : String =
        JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("userId",user.userId)
        .withExpiresAt(expireToken())
        .sign(algo)

    private fun expireToken() = Date(System.currentTimeMillis() + 36_00_00 * 100)

}