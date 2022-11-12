package com.example.repository

import com.example.models.user_models.User
import com.example.data.dao.UserDao
import com.example.models.table.UserTable
import org.jetbrains.exposed.sql.*

class UserRepository : UserDao {

    override suspend fun createUser(name: String, email: String, password: String, imageUrl: String) : User? {
        val statement = DatabaseFactory.dbQuery {
            UserTable.insert { tbl ->
                tbl[UserTable.name] = name
                tbl[UserTable.email] = email
                tbl[UserTable.password] = password
                tbl[UserTable.safePassword] = null
                tbl[UserTable.imageUrl] = imageUrl
            }
        }
        return rowToUser(row = statement.resultedValues?.get(0))
    }


    override suspend fun findUserById(userId: Int): User? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.userId.eq(userId) }
            .map {
                rowToUser(it)
            }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String): User? = DatabaseFactory.dbQuery {
        UserTable.select { UserTable.email.eq(email) }
            .map {
                rowToUser(it)
            }.singleOrNull()
    }

    override suspend fun deleteUser(userId: Int): Int = DatabaseFactory.dbQuery {
        UserTable.deleteWhere { UserTable.userId.eq(userId) }
    }

    override suspend fun updateUser(userId: Int, name : String,email : String,imageUrl : String): Int =
        DatabaseFactory.dbQuery {
            UserTable.update({ UserTable.userId.eq(userId) }) { tbl ->
                tbl[UserTable.name] = name
                tbl[UserTable.email] = email
                tbl[UserTable.imageUrl] = imageUrl
            }
        }

    override suspend fun updatePassword(userId: Int,password: String) : Int =
        DatabaseFactory.dbQuery {
            UserTable.update(where = { UserTable.userId.eq(userId)}) { tbl->
                tbl[UserTable.password] = password
            }
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
}