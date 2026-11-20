package com.example.myapplication

import com.example.myapplication.game.FixedStepGameClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedStepGameClockTest {
    private fun simulate(frames: Int, displayHz: Float): Pair<Int, Float> {
        val clock = FixedStepGameClock()
        var steps = 0
        var gameSeconds = 0f
        repeat(frames) {
            steps += clock.advance(1f / displayHz) { dt -> gameSeconds += dt }
        }
        return steps to gameSeconds
    }

    @Test
    fun oneSecondRunsAtSameSimulationRateAcrossRefreshRates() {
        val thirty = simulate(30, 30f)
        val sixty = simulate(60, 60f)
        val highRefresh = simulate(120, 120f)
        assertEquals(60, thirty.first)
        assertEquals(60, sixty.first)
        assertEquals(60, highRefresh.first)
        assertEquals(thirty.second, sixty.second, 0.00001f)
        assertEquals(sixty.second, highRefresh.second, 0.00001f)
    }

    @Test
    fun longFrameNeverCausesUnlimitedCatchUp() {
        val clock = FixedStepGameClock()
        var count = 0
        assertEquals(4, clock.advance(15f) { count++ })
        assertEquals(4, count)
        assertTrue(clock.advance(1f / 60f) { count++ } <= 4)
    }

    @Test
    fun invalidAndZeroDurationsDoNotAdvance() {
        val clock = FixedStepGameClock()
        assertEquals(0, clock.advance(0f) { throw AssertionError("Should not run") })
        assertEquals(0, clock.advance(Float.NaN) { throw AssertionError("Should not run") })
        assertEquals(0, clock.advance(-5f) { throw AssertionError("Should not run") })
    }
}
