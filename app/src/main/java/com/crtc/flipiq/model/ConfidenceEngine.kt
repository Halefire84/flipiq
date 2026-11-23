package com.crtc.flipiq.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Statistical confidence scoring for reseller decisions.
 *
 * Inputs: a list of recent sold prices for similar items.
 * Outputs: confidence score (0–100), price range, sell estimate.
 *
 * The score reflects how predictable the market is:
 *   - High score = tight price band, many comps, fast turns
 *   - Low score = wide spread, few comps, slow category
 */
object ConfidenceEngine {

    data class MarketSignal(
        val soldPrices: List<Double>,   // recent sold prices (from eBay or user-entered)
        val avgDaysToSell: Double = 14.0, // typical days to sell in this category
        val sellThroughRate: Double = 0.7  // sold / (sold + unsold), 0.0–1.0
    )

    data class ConfidenceResult(
        val score: Int,             // 0–100
        val label: String,          // "HIGH", "MEDIUM", "LOW"
        val priceLow: Double,       // 25th percentile
        val priceMid: Double,       // median
        val priceHigh: Double,      // 75th percentile
        val priceStdDev: Double,
        val sampleSize: Int,
        val daysToSell: Double,
        val sellThroughRate: Double,
        val summary: String         // human-readable
    )

    fun score(signal: MarketSignal): ConfidenceResult {
        val prices = signal.soldPrices.filter { it > 0 && it.isFinite() }.sorted()
        val n = prices.size

        if (n == 0) return noData()

        val mean = prices.average()
        val stdDev = if (n > 1) {
            sqrt(prices.sumOf { (it - mean) * (it - mean) } / (n - 1))
        } else 0.0

        val mid = median(prices)
        val low = percentile(prices, 25)
        val high = percentile(prices, 75)

        // coefficient of variation: lower = more consistent pricing
        val cv = if (mean > 0) stdDev / mean else 1.0

        // score components (each 0–100)
        val spreadScore = max(0, 100 - (cv * 200).toInt())     // tight spread = good
        val sampleScore = min(100, n * 10)                      // more data = good
        val turnScore = max(0, 100 - signal.avgDaysToSell.toInt() * 2) // fast = good
        val sthScore = (signal.sellThroughRate * 100).toInt()   // high sell-through = good

        val composite = (spreadScore * 0.35 + sampleScore * 0.25 +
                turnScore * 0.20 + sthScore * 0.20).toInt()

        val label = when {
            composite >= 70 -> "HIGH"
            composite >= 40 -> "MEDIUM"
            else -> "LOW"
        }

        val summary = buildSummary(label, n, mid, low, high, signal.avgDaysToSell, signal.sellThroughRate)

        return ConfidenceResult(composite, label, low, mid, high, stdDev, n,
            signal.avgDaysToSell, signal.sellThroughRate, summary)
    }

    /** Confidence from a simple user-entered price range (no comps list). */
    fun scoreFromRange(lowPrice: Double, highPrice: Double, sampleSize: Int = 5): ConfidenceResult {
        val prices = buildList {
            add(lowPrice)
            add(highPrice)
            add((lowPrice + highPrice) / 2)
        }
        return score(MarketSignal(prices.filter { it > 0 },
            avgDaysToSell = 14.0,
            sellThroughRate = if (sampleSize > 10) 0.75 else 0.5))
    }

    private fun median(sorted: List<Double>): Double {
        val n = sorted.size
        return if (n % 2 == 0) (sorted[n / 2 - 1] + sorted[n / 2]) / 2 else sorted[n / 2]
    }

    private fun percentile(sorted: List<Double>, pct: Int): Double {
        val idx = (sorted.size * pct / 100.0).toInt().coerceIn(0, sorted.size - 1)
        return sorted[idx]
    }

    private fun noData() = ConfidenceResult(0, "LOW", 0.0, 0.0, 0.0, 0.0, 0,
        0.0, 0.0, "No comp data — enter sold prices to get a confidence score.")

    private fun buildSummary(
        label: String, n: Int, mid: Double, low: Double, high: Double,
        days: Double, sth: Double
    ): String {
        val pct = (sth * 100).toInt()
        return "$label confidence · ${n} comps · " +
               "sells ${"$%.0f".format(low)}–${"$%.0f".format(high)} · " +
               "~${days.toInt()}d to sell · $pct% sell-through"
    }
}
