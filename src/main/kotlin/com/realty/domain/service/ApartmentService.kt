package com.realty.domain.service

import com.realty.data.dto.*
import com.realty.data.models.ApartmentStatus
import com.realty.data.models.ListingType
import com.realty.data.repository.ApartmentRepository
import java.util.UUID

class ApartmentService(private val repository: ApartmentRepository) {

    fun getAll(filters: ApartmentFilters): ApartmentListResponse {
        val (items, total) = repository.findAll(filters)
        val totalPages = if (filters.pageSize > 0) (total + filters.pageSize - 1) / filters.pageSize else 1
        return ApartmentListResponse(
            items = items,
            total = total,
            page = filters.page,
            pageSize = filters.pageSize,
            totalPages = totalPages
        )
    }

    fun getById(id: String): ApartmentDto =
        repository.findById(UUID.fromString(id))
            ?: throw NoSuchElementException("Apartment $id not found")

    fun create(agentId: String, request: CreateApartmentRequest): ApartmentDto {
        validate(request)
        return repository.create(UUID.fromString(agentId), request)
    }

    fun update(id: String, agentId: String, request: UpdateApartmentRequest): ApartmentDto =
        repository.update(UUID.fromString(id), UUID.fromString(agentId), request)

    fun delete(id: String) =
        repository.delete(UUID.fromString(id))

    private fun validate(r: CreateApartmentRequest) {
        require(r.fullAddress.isNotBlank()) { "Address is required" }
        require(r.floor > 0) { "Floor must be positive" }
        require(r.rooms > 0) { "Rooms count must be positive" }
        require(r.totalArea > 0) { "Total area must be positive" }
        require(r.price > 0) { "Price must be positive" }
        try { ApartmentStatus.valueOf(r.status) }
        catch (e: IllegalArgumentException) { throw IllegalArgumentException("Invalid status: ${r.status}") }
        try { ListingType.valueOf(r.listingType) }
        catch (e: IllegalArgumentException) { throw IllegalArgumentException("Invalid listing type: ${r.listingType}") }
    }
}
