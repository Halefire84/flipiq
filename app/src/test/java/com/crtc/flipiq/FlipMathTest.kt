package com.crtc.flipiq

import com.crtc.flipiq.model.FlipMath
import org.junit.Assert.*
import org.junit.Test

class FlipMathTest {

    private val plan = FlipMath.PlanTargets(minRoiPct = 30.0, minProfitDollars = 5.0)

    @Test fun `basic BUY — good deal`() {
        val r = FlipMath.calculate(10.0, 25.0, 12.9, 4.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.BUY, r.verdict)
        assertTrue("profit > 5", r.profit > 5.0)
        assertNotNull(r.roi)
        assertTrue("roi > 30", r.roi!! > 30.0)
    }

    @Test fun `basic PASS — buying too high`() {
        val r = FlipMath.calculate(30.0, 25.0, 12.9, 4.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.PASS, r.verdict)
        assertTrue("profit negative", r.profit < 0)
    }

    @Test fun `RISKY — profitable but under target ROI`() {
        val r = FlipMath.calculate(18.0, 25.0, 12.9, 4.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.RISKY, r.verdict)
    }

    @Test fun `zero buy price — roi is null`() {
        val r = FlipMath.calculate(0.0, 25.0, 12.9, 4.0, 0.0, plan)
        assertNull(r.roi)
    }

    @Test fun `negative buy price — ERROR`() {
        val r = FlipMath.calculate(-1.0, 25.0, 12.9, 0.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.ERROR, r.verdict)
    }

    @Test fun `fee 100 pct — ERROR`() {
        val r = FlipMath.calculate(10.0, 25.0, 100.0, 0.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.ERROR, r.verdict)
    }

    @Test fun `infinity sell price — ERROR`() {
        val r = FlipMath.calculate(10.0, Double.POSITIVE_INFINITY, 12.9, 0.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.ERROR, r.verdict)
    }

    @Test fun `buyer premium increases allIn`() {
        val noPrem = FlipMath.calculate(10.0, 25.0, 12.9, 0.0, 0.0, plan)
        val withPrem = FlipMath.calculate(10.0, 25.0, 12.9, 0.0, 20.0, plan)
        assertTrue("allIn higher with premium", withPrem.allIn > noPrem.allIn)
        assertTrue("profit lower with premium", withPrem.profit < noPrem.profit)
    }

    @Test fun `maxBuy is below buy price on PASS`() {
        val r = FlipMath.calculate(30.0, 25.0, 12.9, 4.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.PASS, r.verdict)
        assertTrue("maxBuy < buy", r.maxBuy < 30.0)
    }

    @Test fun `huge number — ERROR`() {
        val r = FlipMath.calculate(10.0, 1_500_000.0, 12.9, 0.0, 0.0, plan)
        assertEquals(FlipMath.Verdict.ERROR, r.verdict)
    }
}
