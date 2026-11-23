package com.crtc.flipiq.model

import java.util.UUID

/** Combined result of a single FlipIQ scan. Stored locally. */
data class FlipResult(
    val id: String = UUID.randomUUID().toString(),
    val itemDescription: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val feesPct: Double,
    val shippingCost: Double,
    val buyerPremiumPct: Double,
    val mathResult: FlipMath.Result,
    val confidence: ConfidenceEngine.ConfidenceResult,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val displayVerdict: String get() = when (mathResult.verdict) {
        FlipMath.Verdict.BUY -> "BUY"
        FlipMath.Verdict.RISKY -> "RISKY"
        FlipMath.Verdict.PASS -> "PASS"
        FlipMath.Verdict.ERROR -> "ERROR"
    }

    val verdictColor: Long get() = when (mathResult.verdict) {
        FlipMath.Verdict.BUY -> 0xFF2E7D32   // green
        FlipMath.Verdict.RISKY -> 0xFFE65100  // orange
        FlipMath.Verdict.PASS -> 0xFFB71C1C   // red
        FlipMath.Verdict.ERROR -> 0xFF757575  // grey
    }
}
