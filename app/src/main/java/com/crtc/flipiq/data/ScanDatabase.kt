package com.crtc.flipiq.data

import android.content.Context
import androidx.room.*
import com.crtc.flipiq.model.FlipMath
import com.crtc.flipiq.model.ConfidenceEngine
import com.crtc.flipiq.model.FlipResult

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey val id: String,
    val itemDescription: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val feesPct: Double,
    val shippingCost: Double,
    val buyerPremiumPct: Double,
    val profit: Double,
    val roi: Double?,
    val verdictOrdinal: Int,
    val confidenceScore: Int,
    val confidenceLabel: String,
    val priceLow: Double,
    val priceMid: Double,
    val priceHigh: Double,
    val timestamp: Long,
    val notes: String
)

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun allScans(): List<ScanEntity>

    @Query("SELECT * FROM scans ORDER BY timestamp DESC LIMIT :n")
    fun recentScans(n: Int): List<ScanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(scan: ScanEntity)

    @Query("DELETE FROM scans WHERE id = :id")
    fun delete(id: String)

    @Query("SELECT COUNT(*) FROM scans WHERE timestamp >= :sinceMs")
    fun countSince(sinceMs: Long): Int

    @Query("DELETE FROM scans")
    fun deleteAll()
}

@Database(entities = [ScanEntity::class], version = 1, exportSchema = false)
abstract class ScanDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        @Volatile private var INSTANCE: ScanDatabase? = null

        fun get(context: Context): ScanDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScanDatabase::class.java,
                    "flipiq.db"
                ).allowMainThreadQueries().build().also { INSTANCE = it }
            }
    }
}

fun ScanEntity.toFlipResult(): FlipResult {
    val verdict = FlipMath.Verdict.entries[verdictOrdinal]
    val mathResult = FlipMath.Result(
        buyPrice, sellPrice, feesPct, shippingCost, buyerPremiumPct,
        net = sellPrice * (1 - feesPct / 100) - shippingCost,
        allIn = buyPrice * (1 + buyerPremiumPct / 100),
        profit = profit, roi = roi,
        maxBuy = 0.0, verdict = verdict
    )
    val confidence = ConfidenceEngine.ConfidenceResult(
        confidenceScore, confidenceLabel, priceLow, priceMid, priceHigh,
        0.0, 0, 14.0, 0.7, "$confidenceLabel confidence"
    )
    return FlipResult(id, itemDescription, buyPrice, sellPrice, feesPct,
        shippingCost, buyerPremiumPct, mathResult, confidence, timestamp, notes)
}

fun FlipResult.toEntity() = ScanEntity(
    id, itemDescription, buyPrice, sellPrice, feesPct, shippingCost, buyerPremiumPct,
    mathResult.profit, mathResult.roi,
    mathResult.verdict.ordinal,
    confidence.score, confidence.label,
    confidence.priceLow, confidence.priceMid, confidence.priceHigh,
    timestamp, notes
)
