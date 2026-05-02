package com.realty.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.realty.data.dto.AuthResponse
import com.realty.data.dto.TokenResponse
import com.realty.data.models.RefreshTokens
import com.realty.data.repository.UserRepository
import io.ktor.server.config.*
import org.mindrot.jbcrypt.BCrypt
import java.time.OffsetDateTime
import java.util.Date
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val config: ApplicationConfig
) {
    private val secret = config.property("jwt.secret").getString()
    private val issuer = config.property("jwt.issuer").getString()
    private val audience = config.property("jwt.audience").getString()
    private val accessTokenExpiryMs = config.property("jwt.accessTokenExpiryMs").getString().toLong()
    private val refreshTokenExpiryDays = config.property("jwt.refreshTokenExpiryDays").getString().toLong()

    fun register(fullName: String, email: String, password: String, phone: String?): AuthResponse {
        require(fullName.isNotBlank()) { "Full name is required" }
        require(email.matches(Regex("^[^@]+@[^@]+\\.[^@]+"))) { "Invalid email format" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        require(userRepository.findByEmail(email) == null) { "User with this email already exists" }

        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = userRepository.create(fullName, email, hash, phone)
        return buildAuthResponse(user.id, user.toDto())
    }

    fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmail(email)
            ?: throw SecurityException("Invalid email or password")
        if (!user.isActive) throw SecurityException("Account is deactivated")
        if (!BCrypt.checkpw(password, user.passwordHash))
            throw SecurityException("Invalid email or password")
        return buildAuthResponse(user.id, user.toDto())
    }

    fun refresh(refreshToken: String): TokenResponse {
        val row = userRepository.findRefreshToken(refreshToken)
            ?: throw SecurityException("Invalid or expired refresh token")
        val userId = row[RefreshTokens.userId]
        userRepository.revokeRefreshToken(refreshToken)

        val newAccess = generateAccessToken(userId)
        val newRefresh = UUID.randomUUID().toString()
        userRepository.saveRefreshToken(
            userId, newRefresh,
            OffsetDateTime.now().plusDays(refreshTokenExpiryDays)
        )
        return TokenResponse(accessToken = newAccess, refreshToken = newRefresh)
    }

    fun logout(refreshToken: String) {
        userRepository.revokeRefreshToken(refreshToken)
    }

    private fun buildAuthResponse(userId: UUID, userDto: com.realty.data.dto.UserDto): AuthResponse {
        val access = generateAccessToken(userId)
        val refresh = UUID.randomUUID().toString()
        userRepository.saveRefreshToken(
            userId, refresh,
            OffsetDateTime.now().plusDays(refreshTokenExpiryDays)
        )
        return AuthResponse(accessToken = access, refreshToken = refresh, user = userDto)
    }

    private fun generateAccessToken(userId: UUID): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + accessTokenExpiryMs))
            .sign(Algorithm.HMAC256(secret))
}
