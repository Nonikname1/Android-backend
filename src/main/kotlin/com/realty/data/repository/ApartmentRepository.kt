package com.realty.data.repository

import com.realty.data.dto.*
import com.realty.data.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class ApartmentRepository {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun findAll(filters: ApartmentFilters): Pair<List<ApartmentDto>, Int> = transaction {
        fun buildQuery() = Apartments.selectAll().apply {
            filters.status?.let {
                andWhere { Apartments.status eq ApartmentStatus.valueOf(it) }
            }
            filters.listingType?.let {
                andWhere { Apartments.listingType eq ListingType.valueOf(it) }
            }
            filters.rooms?.let {
                andWhere { Apartments.rooms eq it.toShort() }
            }
            filters.minPrice?.let {
                andWhere { Apartments.price greaterEq it }
            }
            filters.maxPrice?.let {
                andWhere { Apartments.price lessEq it }
            }
            filters.search?.let { s ->
                andWhere { Apartments.fullAddress.lowerCase() like "%${s.lowercase()}%" }
            }
        }

        val total = buildQuery().count().toInt()

        val sortColumn: Column<*> = if (filters.sortBy == "price") Apartments.price else Apartments.createdAt
        val sortOrder = if (filters.sortOrder == "asc") SortOrder.ASC else SortOrder.DESC
        val offset = ((filters.page - 1) * filters.pageSize).toLong()

        val items = buildQuery()
            .orderBy(sortColumn to sortOrder)
            .limit(filters.pageSize, offset)
            .map { it.toDto() }

        items to total
    }

    fun findById(id: UUID): ApartmentDto? = transaction {
        exec("UPDATE apartments SET views_count = views_count + 1 WHERE id = '$id'")
        Apartments.select { Apartments.id eq id }
            .singleOrNull()
            ?.toDtoWithPhotos()
    }

    fun create(agentId: UUID, request: CreateApartmentRequest): ApartmentDto = transaction {
        val id = UUID.randomUUID()
        val now = OffsetDateTime.now()

        Apartments.insert {
            it[Apartments.id] = id
            it[Apartments.agentId] = agentId
            it[Apartments.buildingId] = request.buildingId?.let { bid -> UUID.fromString(bid) }
            it[Apartments.fullAddress] = request.fullAddress
            it[Apartments.apartmentNumber] = request.apartmentNumber
            it[Apartments.floor] = request.floor.toShort()
            it[Apartments.totalFloors] = request.totalFloors?.toShort()
            it[Apartments.rooms] = request.rooms.toShort()
            it[Apartments.totalArea] = request.totalArea.toBigDecimal()
            it[Apartments.livingArea] = request.livingArea?.toBigDecimal()
            it[Apartments.kitchenArea] = request.kitchenArea?.toBigDecimal()
            it[Apartments.price] = request.price
            it[Apartments.listingType] = ListingType.valueOf(request.listingType)
            it[Apartments.status] = ApartmentStatus.valueOf(request.status)
            it[Apartments.description] = request.description
            it[Apartments.notes] = request.notes
            it[Apartments.viewsCount] = 0
            it[Apartments.createdAt] = now
            it[Apartments.updatedAt] = now
        }

        request.photoUrls.forEachIndexed { index, url ->
            ApartmentPhotos.insert {
                it[ApartmentPhotos.apartmentId] = id
                it[ApartmentPhotos.url] = url
                it[ApartmentPhotos.sortOrder] = index.toShort()
                it[ApartmentPhotos.isMain] = index == 0
                it[ApartmentPhotos.createdAt] = now
            }
        }

        findById(id) ?: throw IllegalStateException("Failed to create apartment")
    }

    fun update(id: UUID, agentId: UUID, request: UpdateApartmentRequest): ApartmentDto = transaction {
        val existing = Apartments.select { Apartments.id eq id }
            .singleOrNull() ?: throw NoSuchElementException("Apartment $id not found")

        val oldStatus = existing[Apartments.status]
        val now = OffsetDateTime.now()

        Apartments.update({ Apartments.id eq id }) {
            request.fullAddress?.let { v -> it[Apartments.fullAddress] = v }
            request.apartmentNumber?.let { v -> it[Apartments.apartmentNumber] = v }
            request.floor?.let { v -> it[Apartments.floor] = v.toShort() }
            request.totalFloors?.let { v -> it[Apartments.totalFloors] = v.toShort() }
            request.rooms?.let { v -> it[Apartments.rooms] = v.toShort() }
            request.totalArea?.let { v -> it[Apartments.totalArea] = v.toBigDecimal() }
            request.livingArea?.let { v -> it[Apartments.livingArea] = v.toBigDecimal() }
            request.kitchenArea?.let { v -> it[Apartments.kitchenArea] = v.toBigDecimal() }
            request.price?.let { v -> it[Apartments.price] = v }
            request.listingType?.let { v -> it[Apartments.listingType] = ListingType.valueOf(v) }
            request.status?.let { v ->
                val newStatus = ApartmentStatus.valueOf(v)
                it[Apartments.status] = newStatus
                if (newStatus == ApartmentStatus.sold) it[Apartments.soldAt] = now
            }
            request.description?.let { v -> it[Apartments.description] = v }
            request.notes?.let { v -> it[Apartments.notes] = v }
            it[Apartments.updatedAt] = now
        }

        request.status?.let { statusStr ->
            val newStatus = ApartmentStatus.valueOf(statusStr)
            if (newStatus != oldStatus) {
                ApartmentStatusHistory.insert {
                    it[ApartmentStatusHistory.apartmentId] = id
                    it[ApartmentStatusHistory.changedBy] = agentId
                    it[ApartmentStatusHistory.oldStatus] = oldStatus
                    it[ApartmentStatusHistory.newStatus] = newStatus
                    it[ApartmentStatusHistory.comment] = request.statusComment
                    it[ApartmentStatusHistory.changedAt] = now
                }
            }
        }

        request.photoUrls?.let { urls ->
            ApartmentPhotos.deleteWhere { ApartmentPhotos.apartmentId eq id }
            urls.forEachIndexed { index, url ->
                ApartmentPhotos.insert {
                    it[ApartmentPhotos.apartmentId] = id
                    it[ApartmentPhotos.url] = url
                    it[ApartmentPhotos.sortOrder] = index.toShort()
                    it[ApartmentPhotos.isMain] = index == 0
                    it[ApartmentPhotos.createdAt] = now
                }
            }
        }

        findById(id) ?: throw NoSuchElementException("Apartment $id not found after update")
    }

    fun delete(id: UUID): Unit = transaction {
        ApartmentPhotos.deleteWhere { ApartmentPhotos.apartmentId eq id }
        ApartmentStatusHistory.deleteWhere { ApartmentStatusHistory.apartmentId eq id }
        val deleted = Apartments.deleteWhere { Apartments.id eq id }
        if (deleted == 0) throw NoSuchElementException("Apartment $id not found")
    }

    private fun ResultRow.toDto() = ApartmentDto(
        id = this[Apartments.id].toString(),
        buildingId = this[Apartments.buildingId]?.toString(),
        agentId = this[Apartments.agentId].toString(),
        fullAddress = this[Apartments.fullAddress],
        apartmentNumber = this[Apartments.apartmentNumber],
        floor = this[Apartments.floor].toInt(),
        totalFloors = this[Apartments.totalFloors]?.toInt(),
        rooms = this[Apartments.rooms].toInt(),
        totalArea = this[Apartments.totalArea].toDouble(),
        livingArea = this[Apartments.livingArea]?.toDouble(),
        kitchenArea = this[Apartments.kitchenArea]?.toDouble(),
        price = this[Apartments.price],
        pricePerSqm = (this[Apartments.price].toDouble() / this[Apartments.totalArea].toDouble()).toLong(),
        listingType = this[Apartments.listingType].name,
        status = this[Apartments.status].name,
        description = this[Apartments.description],
        notes = this[Apartments.notes],
        viewsCount = this[Apartments.viewsCount],
        photos = emptyList(),
        createdAt = this[Apartments.createdAt].format(formatter),
        updatedAt = this[Apartments.updatedAt].format(formatter),
        soldAt = this[Apartments.soldAt]?.format(formatter)
    )

    private fun ResultRow.toDtoWithPhotos(): ApartmentDto {
        val apartmentId = this[Apartments.id]
        val photos = ApartmentPhotos
            .select { ApartmentPhotos.apartmentId eq apartmentId }
            .orderBy(ApartmentPhotos.sortOrder to SortOrder.ASC)
            .map { row ->
                PhotoDto(
                    id = row[ApartmentPhotos.id].toString(),
                    url = row[ApartmentPhotos.url],
                    thumbnailUrl = row[ApartmentPhotos.thumbnailUrl],
                    sortOrder = row[ApartmentPhotos.sortOrder].toInt(),
                    isMain = row[ApartmentPhotos.isMain]
                )
            }
        return toDto().copy(photos = photos)
    }
}
