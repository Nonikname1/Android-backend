package com.realty.routes

import com.realty.data.repository.AnalyticsRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.analyticsRoutes(analyticsRepository: AnalyticsRepository) {
    authenticate("jwt-auth") {
        route("/analytics") {
            get("/stats") {
                call.respond(HttpStatusCode.OK, analyticsRepository.getStats())
            }
        }
    }
}
