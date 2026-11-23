package com.crtc.flipiq.model

import kotlin.math.max
import kotlin.math.min

/**
 * Core profit/ROI math for reseller decisions.
 * All inputs validated: negatives, NaN, Infinity rejected.
 */
object FlipMath {

    const val MAX_USD = 1_000_000.0
    const val MAX_FEE_PCT = 99.99

    data class Result(
        val buyCost: Double,       // what you pay
        val sellPrice: Double,     // expected sale price
        val feesPct: Double,       // platform fee %
        val shippingCost: Double,  // your shipping out cost
        val buyerPremiumPct: Double, // auction buyer premium %
        val net: Double,           // sellPrice * (1 - feesPct/100) - shippingCost
        val allIn: Double,         // buyCost * (1 + buyerPremiumPct/100)
        val profit: Double,        // net - allIn
        val roi: Double?,          // profit / allIn * 100, null if allIn == 0
        val maxBuy: Double,        // max you should pay to hit minROI & minProfit
        val verdict: Verdict,
        val error: String? = null
    )

    enum class Verdict { BUY, RISKY, PASS, ERROR }

    data class PlanTargets(
        val minRoiPct: Double = 30.0,
        val minProfitDollars: Double = 5.0,
        val maxBuyCap: Double = MAX_USD
    )

    fun calculate(
        buyCost: Double,
        sellPrice: Double,
        feesPct: Double,
        shippingCost: Double = 0.0,
        buyerPremiumPct: Double = 0.0,
        plan: PlanTargets = PlanTargets()
    ): Result {
        // --- input validation ---
        for ((label, v) in listOf(
            "Buy cost" to buyCost,
            "Sell price" to sellPrice,
            "Shipping" to shippingCost
        )) {
            if (!v.isFinite() || v < 0) return error("$label must be 0 or more")
            if (v > MAX_USD) return error("$label exceeds maximum ($MAX_USD)")
        }
        for ((label, v) in listOf("Fee %" to feesPct, "Buyer premium %" to buyerPremiumPct)) {
            if (!v.isFinite() || v < 0) return error("$label must be 0 or more")
            if (v >= 100) return error("$label must be under 100")
        }

        val net = sellPrice * (1.0 - feesPct / 100.0) - shippingCost
        val allIn = buyCost * (1.0 + buyerPremiumPct / 100.0)
        val profit = net - allIn
        val roi = if (allIn == 0.0) null else profit / allIn * 100.0

        // max buy: highest price that still meets minROI AND minProfit
        val maxByRoi = if (plan.minRoiPct > 0) net / (1.0 + plan.minRoiPct / 100.0) / (1.0 + buyerPremiumPct / 100.0) else Double.MAX_VALUE
        val maxByProfit = (net - plan.minProfitDollars) / (1.0 + buyerPremiumPct / 100.0)
        val maxBuyRaw = min(maxByRoi, maxByProfit)
        val maxBuy = max(0.0, min(plan.maxBuyCap, maxBuyRaw))

        val verdict = when {
            roi == null -> Verdict.RISKY
            roi >= plan.minRoiPct && profit >= plan.minProfitDollars -> Verdict.BUY
            roi >= 0 -> Verdict.RISKY
            else -> Verdict.PASS
        }

        return Result(buyCost, sellPrice, feesPct, shippingCost, buyerPremiumPct,
            net, allIn, profit, roi, maxBuy, verdict)
    }

    private fun error(msg: String) = Result(
        0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, 0.0,
        Verdict.ERROR, msg
    )
}
