package com.realty.data.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
import org.postgresql.util.PGobject

enum class ApartmentStatus { available, reserved, sold, cancelled }
enum class ListingType { sale, rent }

object Users : Table("users") {
    val id = uuid("id").autoGenerate()
    val fullName = varchar("full_name", 255)
    val email = varchar("email", 255)
    val phone = varchar("phone", 50).nullable()
    val passwordHash = text("password_hash")
    val avatarUrl = text("avatar_url").nullable()
    val isActive = bool("is_active").default(true)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokens : Table("refresh_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Users.id)
    val token = text("token")
    val expiresAt = timestampWithTimeZone("expires_at")
    val createdAt = timestampWithTimeZone("created_at")
    val revoked = bool("revoked").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Buildings : Table("buildings") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255).nullable()
    val city = varchar("city", 100)
    val district = varchar("district", 100).nullable()
    val street = varchar("street", 255)
    val houseNumber = varchar("house_number", 20)
    val totalFloors = short("total_floors").nullable()
    val yearBuilt = short("year_built").nullable()
    val description = text("description").nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object Apartments : Table("apartments") {
    val id = uuid("id").autoGenerate()
    val buildingId = uuid("building_id").references(Buildings.id).nullable()
    val agentId = uuid("agent_id").references(Users.id)
    val fullAddress = text("full_address")
    val apartmentNumber = varchar("apartment_number", 20).nullable()
    val floor = short("floor")
    val totalFloors = short("total_floors").nullable()
    val rooms = short("rooms")
    val totalArea = decimal("total_area", 8, 2)
    val livingArea = decimal("living_area", 8, 2).nullable()
    val kitchenArea = decimal("kitchen_area", 8, 2).nullable()
    val price = long("price")
    val listingType = customEnumeration(
        name = "listing_type",
        sql = "listing_type",
        fromDb = { value -> ListingType.valueOf(value as String) },
        toDb = { PGobject().apply { type = "listing_type"; this.value = it.name } }
    )
    val status = customEnumeration(
        name = "status",
        sql = "apartment_status",
        fromDb = { value -> ApartmentStatus.valueOf(value as String) },
        toDb = { PGobject().apply { type = "apartment_status"; this.value = it.name } }
    )
    val description = text("description").nullable()
    val notes = text("notes").nullable()
    val viewsCount = integer("views_count").default(0)
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")
    val soldAt = timestampWithTimeZone("sold_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object ApartmentPhotos : Table("apartment_photos") {
    val id = uuid("id").autoGenerate()
    val apartmentId = uuid("apartment_id").references(Apartments.id)
    val url = text("url")
    val thumbnailUrl = text("thumbnail_url").nullable()
    val sortOrder = short("sort_order").default(0)
    val isMain = bool("is_main").default(false)
    val createdAt = timestampWithTimeZone("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ApartmentStatusHistory : Table("apartment_status_history") {
    val id = uuid("id").autoGenerate()
    val apartmentId = uuid("apartment_id").references(Apartments.id)
    val changedBy = uuid("changed_by").references(Users.id)
    val oldStatus = customEnumeration(
        name = "old_status",
        sql = "apartment_status",
        fromDb = { value -> ApartmentStatus.valueOf(value as String) },
        toDb = { PGobject().apply { type = "apartment_status"; this.value = it.name } }
    ).nullable()
    val newStatus = customEnumeration(
        name = "new_status",
        sql = "apartment_status",
        fromDb = { value -> ApartmentStatus.valueOf(value as String) },
        toDb = { PGobject().apply { type = "apartment_status"; this.value = it.name } }
    )
    val comment = text("comment").nullable()
    val changedAt = timestampWithTimeZone("changed_at")
    override val primaryKey = PrimaryKey(id)
}
