package com.realty.data.repository

import com.realty.data.dto.*
import org.jetbrains.exposed.sql.transactions.transaction

class AnalyticsRepository {

    fun getStats(): StatsResponse = transaction {
        val byStatus = exec(
            "SELECT status::text, listing_type::text, count, avg_price, min_price, max_price " +
            "FROM android.v_apartments_by_status"
        ) { rs ->
            val result = mutableListOf<StatusStat>()
            while (rs.next()) {
                result.add(StatusStat(
                    status = rs.getString("status") ?: "",
                    listingType = rs.getString("listing_type") ?: "",
                    count = rs.getLong("count"),
                    avgPrice = rs.getLong("avg_price"),
                    minPrice = rs.getLong("min_price"),
                    maxPrice = rs.getLong("max_price")
                ))
            }
            result
        } ?: emptyList()

        val byDistrict = exec(
            "SELECT district, city, total, avg_price, avg_price_per_sqm, available, sold, reserved " +
            "FROM android.v_apartments_by_district"
        ) { rs ->
            val result = mutableListOf<DistrictStat>()
            while (rs.next()) {
                result.add(DistrictStat(
                    district = rs.getString("district"),
                    city = rs.getString("city"),
                    total = rs.getLong("total"),
                    avgPrice = rs.getLong("avg_price"),
                    avgPricePerSqm = rs.getLong("avg_price_per_sqm"),
                    available = rs.getLong("available"),
                    sold = rs.getLong("sold"),
                    reserved = rs.getLong("reserved")
                ))
            }
            result
        } ?: emptyList()

        val salesDynamics = exec(
            "SELECT to_char(month, 'YYYY-MM') as month_str, listing_type::text, " +
            "deals_count, COALESCE(total_revenue, 0) as total_revenue, avg_price " +
            "FROM android.v_sales_dynamics ORDER BY month DESC LIMIT 12"
        ) { rs ->
            val result = mutableListOf<SalesDynamicStat>()
            while (rs.next()) {
                result.add(SalesDynamicStat(
                    month = rs.getString("month_str") ?: "",
                    listingType = rs.getString("listing_type") ?: "",
                    dealsCount = rs.getLong("deals_count"),
                    totalRevenue = rs.getLong("total_revenue"),
                    avgPrice = rs.getLong("avg_price")
                ))
            }
            result
        } ?: emptyList()

        val totalApartments = exec(
            "SELECT COUNT(*) as cnt FROM android.apartments"
        ) { rs -> if (rs.next()) rs.getLong("cnt") else 0L } ?: 0L

        val totalViews = exec(
            "SELECT COALESCE(SUM(views_count), 0) as total FROM android.apartments"
        ) { rs -> if (rs.next()) rs.getLong("total") else 0L } ?: 0L

        StatsResponse(
            byStatus = byStatus,
            byDistrict = byDistrict,
            salesDynamics = salesDynamics,
            totalApartments = totalApartments,
            totalViews = totalViews
        )
    }
}
