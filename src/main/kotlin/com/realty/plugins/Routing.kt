package com.realty.plugins

import com.realty.data.repository.AnalyticsRepository
import com.realty.data.repository.ApartmentRepository
import com.realty.data.repository.UserRepository
import com.realty.domain.service.ApartmentService
import com.realty.domain.service.AuthService
import com.realty.routes.analyticsRoutes
import com.realty.routes.apartmentRoutes
import com.realty.routes.authRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (cause.message ?: "Bad request")))
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, mapOf("error" to (cause.message ?: "Not found")))
        }
        exception<SecurityException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (cause.message ?: "Unauthorized")))
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Internal server error"))
        }
    }

    val userRepository = UserRepository()
    val apartmentRepository = ApartmentRepository()
    val analyticsRepository = AnalyticsRepository()
    val authService = AuthService(userRepository, environment.config)
    val apartmentService = ApartmentService(apartmentRepository)

    routing {
        authRoutes(authService)
        apartmentRoutes(apartmentService)
        analyticsRoutes(analyticsRepository)
    }
}
