package com.realty.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApartmentDto(
    val id: String,
    val buildingId: String? = null,
    val agentId: String,
    val fullAddress: String,
    val apartmentNumber: String? = null,
    val floor: Int,
    val totalFloors: Int? = null,
    val rooms: Int,
    val totalArea: Double,
    val livingArea: Double? = null,
    val kitchenArea: Double? = null,
    val price: Long,
    val pricePerSqm: Long? = null,
    val listingType: String,
    val status: String,
    val description: String? = null,
    val notes: String? = null,
    val viewsCount: Int,
    val photos: List<PhotoDto> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val soldAt: String? = null
)

@Serializable
data class PhotoDto(
    val id: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val sortOrder: Int,
    val isMain: Boolean
)

@Serializable
data class CreateApartmentRequest(
    val buildingId: String? = null,
    val fullAddress: String,
    val apartmentNumber: String? = null,
    val floor: Int,
    val totalFloors: Int? = null,
    val rooms: Int,
    val totalArea: Double,
    val livingArea: Double? = null,
    val kitchenArea: Double? = null,
    val price: Long,
    val listingType: String = "sale",
    val status: String = "available",
    val description: String? = null,
    val notes: String? = null,
    val photoUrls: List<String> = emptyList()
)

@Serializable
data class UpdateApartmentRequest(
    val fullAddress: String? = null,
    val apartmentNumber: String? = null,
    val floor: Int? = null,
    val totalFloors: Int? = null,
    val rooms: Int? = null,
    val totalArea: Double? = null,
    val livingArea: Double? = null,
    val kitchenArea: Double? = null,
    val price: Long? = null,
    val listingType: String? = null,
    val status: String? = null,
    val description: String? = null,
    val notes: String? = null,
    val photoUrls: List<String>? = null,
    val statusComment: String? = null
)

@Serializable
data class ApartmentListResponse(
    val items: List<ApartmentDto>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

@Serializable
data class ApartmentFilters(
    val status: String? = null,
    val listingType: String? = null,
    val rooms: Int? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val search: String? = null,
    val sortBy: String? = "created_at",
    val sortOrder: String? = "desc",
    val page: Int = 1,
    val pageSize: Int = 20
)
