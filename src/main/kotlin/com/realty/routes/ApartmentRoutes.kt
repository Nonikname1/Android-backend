package com.realty.routes

import com.realty.data.dto.ApartmentFilters
import com.realty.data.dto.CreateApartmentRequest
import com.realty.data.dto.UpdateApartmentRequest
import com.realty.domain.service.ApartmentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.apartmentRoutes(apartmentService: ApartmentService) {
    authenticate("jwt-auth") {
        route("/apartments") {
            get {
                val p = call.request.queryParameters
                val filters = ApartmentFilters(
                    status = p["status"],
                    listingType = p["listingType"],
                    rooms = p["rooms"]?.toIntOrNull(),
                    minPrice = p["minPrice"]?.toLongOrNull(),
                    maxPrice = p["maxPrice"]?.toLongOrNull(),
                    search = p["search"],
                    sortBy = p["sortBy"] ?: "created_at",
                    sortOrder = p["sortOrder"] ?: "desc",
                    page = p["page"]?.toIntOrNull() ?: 1,
                    pageSize = p["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                )
                call.respond(HttpStatusCode.OK, apartmentService.getAll(filters))
            }

            post {
                val agentId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asString()
                val req = call.receive<CreateApartmentRequest>()
                val apartment = apartmentService.create(agentId, req)
                call.respond(HttpStatusCode.Created, apartment)
            }

            get("/{id}") {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("ID is required")
                call.respond(HttpStatusCode.OK, apartmentService.getById(id))
            }

            put("/{id}") {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("ID is required")
                val agentId = call.principal<JWTPrincipal>()!!
                    .payload.getClaim("userId").asString()
                val req = call.receive<UpdateApartmentRequest>()
                call.respond(HttpStatusCode.OK, apartmentService.update(id, agentId, req))
            }

            delete("/{id}") {
                val id = call.parameters["id"]
                    ?: throw IllegalArgumentException("ID is required")
                apartmentService.delete(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
