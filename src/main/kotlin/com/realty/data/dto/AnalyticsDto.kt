package com.realty.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatsResponse(
    val byStatus: List<StatusStat>,
    val byDistrict: List<DistrictStat>,
    val salesDynamics: List<SalesDynamicStat>,
    val totalApartments: Long,
    val totalViews: Long
)

@Serializable
data class StatusStat(
    val status: String,
    val listingType: String,
    val count: Long,
    val avgPrice: Long,
    val minPrice: Long,
    val maxPrice: Long
)

@Serializable
data class DistrictStat(
    val district: String?,
    val city: String?,
    val total: Long,
    val avgPrice: Long,
    val avgPricePerSqm: Long,
    val available: Long,
    val sold: Long,
    val reserved: Long
)

@Serializable
data class SalesDynamicStat(
    val month: String,
    val listingType: String,
    val dealsCount: Long,
    val totalRevenue: Long,
    val avgPrice: Long
)
