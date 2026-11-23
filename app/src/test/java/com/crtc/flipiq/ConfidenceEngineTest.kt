package com.crtc.flipiq

import com.crtc.flipiq.model.ConfidenceEngine
import org.junit.Assert.*
import org.junit.Test

class ConfidenceEngineTest {

    @Test fun `high confidence — tight prices, many comps`() {
        val prices = List(20) { 45.0 + it * 0.5 }   // $45–$54.75, tight
        val signal = ConfidenceEngine.MarketSignal(prices, avgDaysToSell = 5.0, sellThroughRate = 0.9)
        val r = ConfidenceEngine.score(signal)
        assertTrue("score >= 60", r.score >= 60)
        assertEquals("HIGH", r.label)
    }

    @Test fun `low confidence — wide prices, few comps`() {
        val prices = listOf(10.0, 90.0, 5.0, 150.0)
        val signal = ConfidenceEngine.MarketSignal(prices, avgDaysToSell = 60.0, sellThroughRate = 0.2)
        val r = ConfidenceEngine.score(signal)
        assertTrue("score < 50", r.score < 50)
    }

    @Test fun `empty comps — score 0`() {
        val r = ConfidenceEngine.score(ConfidenceEngine.MarketSignal(emptyList()))
        assertEquals(0, r.score)
        assertEquals("LOW", r.label)
    }

    @Test fun `single comp — no crash`() {
        val r = ConfidenceEngine.score(ConfidenceEngine.MarketSignal(listOf(50.0)))
        assertTrue("score >= 0", r.score >= 0)
    }

    @Test fun `scoreFromRange — returns valid result`() {
        val r = ConfidenceEngine.scoreFromRange(40.0, 60.0, 15)
        assertTrue("score 0–100", r.score in 0..100)
        assertEquals(50.0, r.priceMid, 1.0)
    }

    @Test fun `filters zero and negative prices`() {
        val prices = listOf(-5.0, 0.0, 50.0, 55.0, 52.0)
        val r = ConfidenceEngine.score(ConfidenceEngine.MarketSignal(prices))
        assertEquals(3, r.sampleSize)
    }
}
