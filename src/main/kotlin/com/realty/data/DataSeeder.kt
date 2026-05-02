package com.realty.data

import com.realty.data.models.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.UUID

object DataSeeder {

    private val log = LoggerFactory.getLogger("DataSeeder")

    fun seedIfEmpty() {
        transaction {
            val count = Users.selectAll().count()
            if (count > 0L) {
                log.info("DB already has data ($count users), skipping seed.")
                return@transaction
            }

            log.info("Seeding database with test data...")

            val agentId = insertAgent()
            val b1 = insertBuilding(
                name = "ЖК Северный", city = "Москва", district = "Северный",
                street = "ул. Садовая", houseNumber = "15А", totalFloors = 16, yearBuilt = 2020
            )
            val b2 = insertBuilding(
                name = "ЖК Центральный", city = "Москва", district = "Центральный",
                street = "пр. Ленина", houseNumber = "42", totalFloors = 24, yearBuilt = 2019
            )
            val b3 = insertBuilding(
                name = "ЖК Берёзовый", city = "Москва", district = "Южный",
                street = "ул. Берёзовая", houseNumber = "7", totalFloors = 9, yearBuilt = 2015
            )

            insertApartments(agentId, b1, b2, b3)

            log.info("Seed complete. Login: agent@realty.ru / password123")
        }
    }

    private fun insertAgent(): UUID {
        val id = UUID.randomUUID()
        Users.insert {
            it[Users.id] = id
            it[Users.fullName] = "Иван Петров"
            it[Users.email] = "agent@realty.ru"
            it[Users.phone] = "+7 (900) 123-45-67"
            it[Users.passwordHash] = BCrypt.hashpw("password123", BCrypt.gensalt())
            it[Users.isActive] = true
            it[Users.createdAt] = OffsetDateTime.now()
            it[Users.updatedAt] = OffsetDateTime.now()
        }
        return id
    }

    private fun insertBuilding(
        name: String, city: String, district: String,
        street: String, houseNumber: String,
        totalFloors: Int, yearBuilt: Int
    ): UUID {
        val id = UUID.randomUUID()
        Buildings.insert {
            it[Buildings.id] = id
            it[Buildings.name] = name
            it[Buildings.city] = city
            it[Buildings.district] = district
            it[Buildings.street] = street
            it[Buildings.houseNumber] = houseNumber
            it[Buildings.totalFloors] = totalFloors.toShort()
            it[Buildings.yearBuilt] = yearBuilt.toShort()
            it[Buildings.createdAt] = OffsetDateTime.now()
            it[Buildings.updatedAt] = OffsetDateTime.now()
        }
        return id
    }

    private data class Apt(
        val buildingId: UUID?,
        val address: String,
        val number: String?,
        val floor: Int,
        val totalFloors: Int,
        val rooms: Int,
        val totalArea: BigDecimal,
        val livingArea: BigDecimal?,
        val kitchenArea: BigDecimal?,
        val price: Long,
        val type: ListingType,
        val status: ApartmentStatus,
        val description: String?
    )

    private fun insertApartments(agentId: UUID, b1: UUID, b2: UUID, b3: UUID) {
        val now = OffsetDateTime.now()

        val list = listOf(
            Apt(b1, "ул. Садовая, 15А, кв. 45", "45", 5, 16, 2,
                bd("54.5"), bd("32.0"), bd("10.5"),
                7_500_000L, ListingType.sale, ApartmentStatus.available,
                "Уютная двухкомнатная квартира с современным ремонтом. Развитая инфраструктура."),

            Apt(b1, "ул. Садовая, 15А, кв. 12", "12", 2, 16, 1,
                bd("38.2"), bd("20.0"), bd("9.0"),
                4_800_000L, ListingType.sale, ApartmentStatus.reserved,
                "Однокомнатная квартира на втором этаже. Свежий косметический ремонт."),

            Apt(b1, "ул. Садовая, 15А, кв. 103", "103", 14, 16, 3,
                bd("78.0"), bd("48.5"), bd("14.0"),
                12_200_000L, ListingType.sale, ApartmentStatus.sold,
                "Просторная трёхкомнатная квартира на высоком этаже. Панорамный вид на город."),

            Apt(b1, "ул. Садовая, 15А, кв. 67", "67", 8, 16, 1,
                bd("36.0"), bd("18.5"), bd("8.0"),
                28_000L, ListingType.rent, ApartmentStatus.available,
                "Студия на среднем этаже. Отличный вид из окна. Вся мебель включена."),

            Apt(b1, "ул. Садовая, 15А, кв. 200", "200", 15, 16, 2,
                bd("60.0"), bd("37.0"), bd("12.0"),
                10_500_000L, ListingType.sale, ApartmentStatus.available,
                "Двухкомнатная квартира на предпоследнем этаже. Панорамные окна."),

            Apt(b2, "пр. Ленина, 42, кв. 201", "201", 8, 24, 2,
                bd("62.0"), bd("38.0"), bd("12.0"),
                9_100_000L, ListingType.sale, ApartmentStatus.available,
                "Двухкомнатная квартира в центре города. Рядом метро и торговые центры."),

            Apt(b2, "пр. Ленина, 42, кв. 78", "78", 3, 24, 1,
                bd("41.5"), bd("22.0"), bd("10.5"),
                35_000L, ListingType.rent, ApartmentStatus.available,
                "Уютная однокомнатная квартира в аренду. Полностью меблирована."),

            Apt(b2, "пр. Ленина, 42, кв. 312", "312", 20, 24, 4,
                bd("105.0"), bd("68.0"), bd("18.0"),
                18_500_000L, ListingType.sale, ApartmentStatus.available,
                "Четырёхкомнатная квартира с террасой. Элитный ремонт, два санузла."),

            Apt(b2, "пр. Ленина, 42, кв. 150", "150", 6, 24, 2,
                bd("65.0"), bd("40.0"), bd("13.0"),
                8_400_000L, ListingType.sale, ApartmentStatus.cancelled,
                "Двухкомнатная квартира. Объявление снято с продажи по решению владельца."),

            Apt(b2, "пр. Ленина, 42, кв. 400", "400", 22, 24, 3,
                bd("88.0"), bd("55.0"), bd("16.0"),
                65_000L, ListingType.rent, ApartmentStatus.available,
                "Трёхкомнатная квартира на высоком этаже с видом на город."),

            Apt(b3, "ул. Берёзовая, 7, кв. 23", "23", 4, 9, 2,
                bd("58.0"), bd("36.0"), bd("11.0"),
                6_800_000L, ListingType.sale, ApartmentStatus.available,
                "Двухкомнатная квартира в тихом районе. Закрытый двор, парковка."),

            Apt(b3, "ул. Берёзовая, 7, кв. 55", "55", 7, 9, 3,
                bd("72.0"), bd("44.0"), bd("13.5"),
                50_000L, ListingType.rent, ApartmentStatus.reserved,
                "Трёхкомнатная квартира в аренду. Свежий ремонт, вся необходимая техника."),

            Apt(b3, "ул. Берёзовая, 7, кв. 5", "5", 1, 9, 1,
                bd("32.0"), bd("16.0"), bd("7.5"),
                3_100_000L, ListingType.sale, ApartmentStatus.sold,
                "Однокомнатная квартира на первом этаже."),

            Apt(null, "ул. Цветочная, 3, кв. 8", "8", 2, 5, 2,
                bd("52.0"), bd("30.0"), bd("10.0"),
                5_900_000L, ListingType.sale, ApartmentStatus.sold,
                "Двухкомнатная квартира во вторичном фонде. Хорошее состояние."),

            Apt(null, "ул. Цветочная, 3, кв. 1", "1", 1, 5, 3,
                bd("80.5"), bd("52.0"), bd("15.0"),
                45_000L, ListingType.rent, ApartmentStatus.available,
                "Просторная трёхкомнатная квартира. Собственный выход во двор."),

            Apt(null, "ул. Молодёжная, 12, кв. 34", "34", 3, 12, 1,
                bd("40.0"), bd("21.0"), bd("9.5"),
                3_200_000L, ListingType.sale, ApartmentStatus.available,
                "Однокомнатная квартира в новом доме. Чистовая отделка."),

            Apt(null, "ул. Молодёжная, 12, кв. 89", "89", 9, 12, 2,
                bd("56.0"), bd("34.0"), bd("11.0"),
                7_100_000L, ListingType.sale, ApartmentStatus.reserved,
                "Двухкомнатная квартира с видом на парк."),

            Apt(null, "ул. Гагарина, 18, кв. 14", "14", 5, 10, 3,
                bd("75.0"), bd("47.0"), bd("14.0"),
                55_000L, ListingType.rent, ApartmentStatus.available,
                "Трёхкомнатная квартира после капитального ремонта. Закрытый двор."),

            Apt(null, "ул. Гагарина, 18, кв. 60", "60", 10, 10, 2,
                bd("61.0"), bd("38.5"), bd("12.0"),
                8_900_000L, ListingType.sale, ApartmentStatus.available,
                "Двухкомнатная квартира на последнем этаже. Тёплый чердак в собственности."),

            Apt(b1, "ул. Садовая, 15А, кв. 33", "33", 4, 16, 3,
                bd("82.0"), bd("51.0"), bd("16.0"),
                13_800_000L, ListingType.sale, ApartmentStatus.available,
                "Трёхкомнатная квартира с авторским дизайн-проектом. Встроенная кухня."),
        )

        list.forEach { apt ->
            val createdDaysAgo = (0L..90L).random()

            Apartments.insert {
                it[Apartments.agentId] = agentId
                it[Apartments.buildingId] = apt.buildingId
                it[Apartments.fullAddress] = apt.address
                it[Apartments.apartmentNumber] = apt.number
                it[Apartments.floor] = apt.floor.toShort()
                it[Apartments.totalFloors] = apt.totalFloors.toShort()
                it[Apartments.rooms] = apt.rooms.toShort()
                it[Apartments.totalArea] = apt.totalArea
                it[Apartments.livingArea] = apt.livingArea
                it[Apartments.kitchenArea] = apt.kitchenArea
                it[Apartments.price] = apt.price
                it[Apartments.listingType] = apt.type
                it[Apartments.status] = apt.status
                it[Apartments.description] = apt.description
                it[Apartments.viewsCount] = (5..300).random()
                it[Apartments.createdAt] = now.minusDays(createdDaysAgo)
                it[Apartments.updatedAt] = now
                it[Apartments.soldAt] = if (apt.status == ApartmentStatus.sold)
                    now.minusDays((1L..30L).random()) else null
            }
        }
    }

    private fun bd(value: String) = BigDecimal(value)
}
