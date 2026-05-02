package com.realty.routes

import com.realty.data.dto.LoginRequest
import com.realty.data.dto.RefreshTokenRequest
import com.realty.data.dto.RegisterRequest
import com.realty.domain.service.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()
            val response = authService.register(req.fullName, req.email, req.password, req.phone)
            call.respond(HttpStatusCode.Created, response)
        }
        post("/login") {
            val req = call.receive<LoginRequest>()
            val response = authService.login(req.email, req.password)
            call.respond(HttpStatusCode.OK, response)
        }
        post("/refresh") {
            val req = call.receive<RefreshTokenRequest>()
            val response = authService.refresh(req.refreshToken)
            call.respond(HttpStatusCode.OK, response)
        }
        post("/logout") {
            val req = call.receive<RefreshTokenRequest>()
            authService.logout(req.refreshToken)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
