package com.realty.data.repository

import com.realty.data.dto.UserDto
import com.realty.data.models.RefreshTokens
import com.realty.data.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.util.UUID

data class UserEntity(
    val id: UUID,
    val fullName: String,
    val email: String,
    val phone: String?,
    val passwordHash: String,
    val avatarUrl: String?,
    val isActive: Boolean
) {
    fun toDto() = UserDto(
        id = id.toString(),
        fullName = fullName,
        email = email,
        phone = phone,
        avatarUrl = avatarUrl
    )
}

class UserRepository {

    fun findByEmail(email: String): UserEntity? = transaction {
        Users.select { Users.email eq email }
            .singleOrNull()
            ?.toUserEntity()
    }

    fun findById(id: UUID): UserEntity? = transaction {
        Users.select { Users.id eq id }
            .singleOrNull()
            ?.toUserEntity()
    }

    fun create(
        fullName: String,
        email: String,
        passwordHash: String,
        phone: String?
    ): UserEntity = transaction {
        val id = UUID.randomUUID()
        val now = OffsetDateTime.now()
        Users.insert {
            it[Users.id] = id
            it[Users.fullName] = fullName
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.phone] = phone
            it[Users.isActive] = true
            it[Users.createdAt] = now
            it[Users.updatedAt] = now
        }
        findById(id) ?: throw IllegalStateException("Failed to create user")
    }

    fun saveRefreshToken(userId: UUID, token: String, expiresAt: OffsetDateTime) = transaction {
        RefreshTokens.insert {
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.token] = token
            it[RefreshTokens.expiresAt] = expiresAt
            it[RefreshTokens.createdAt] = OffsetDateTime.now()
            it[RefreshTokens.revoked] = false
        }
    }

    fun findRefreshToken(token: String): ResultRow? = transaction {
        RefreshTokens.select {
            (RefreshTokens.token eq token) and
            (RefreshTokens.revoked eq false) and
            (RefreshTokens.expiresAt greater OffsetDateTime.now())
        }.singleOrNull()
    }

    fun revokeRefreshToken(token: String) = transaction {
        RefreshTokens.update({ RefreshTokens.token eq token }) {
            it[revoked] = true
        }
    }

    private fun ResultRow.toUserEntity() = UserEntity(
        id = this[Users.id],
        fullName = this[Users.fullName],
        email = this[Users.email],
        phone = this[Users.phone],
        passwordHash = this[Users.passwordHash],
        avatarUrl = this[Users.avatarUrl],
        isActive = this[Users.isActive]
    )
}
