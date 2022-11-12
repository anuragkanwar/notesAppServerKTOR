package com.example.data.dao

import com.example.models.user_models.User

interface UserDao {

    suspend fun createUser(
        name: String,
        email: String,
        password: String,
        imageUrl: String
    ) : User?

    suspend fun findUserById(
        userId: Int
    ): User?

    suspend fun findUserByEmail(
        email: String
    ): User?

    suspend fun deleteUser(
        userId: Int,
    ): Int

    suspend fun updatePassword(
        userId: Int,
        password: String
    ): Int

    suspend fun updateUser(
        userId: Int,
        name: String,
        email: String,
        imageUrl: String
    ): Int
}