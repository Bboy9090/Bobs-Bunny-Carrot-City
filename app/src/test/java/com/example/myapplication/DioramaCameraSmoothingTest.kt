package com.example.myapplication

import com.example.myapplication.ui.game3d.DioramaCameraSmoothing
import org.junit.Assert.assertEquals
import org.junit.Test

class DioramaCameraSmoothingTest {

    @Test
    fun sameElapsedTimeHasSameCameraPositionAt30And60Fps() {
        val at30 = DioramaCameraSmoothing.approach(0f, 10f, 7.67f, 1f / 30f)
        val first60 = DioramaCameraSmoothing.approach(0f, 10f, 7.67f, 1f / 60f)
        val second60 = DioramaCameraSmoothing.approach(first60, 10f, 7.67f, 1f / 60f)
        assertEquals(at30, second60, 0.0001f)
    }

    @Test
    fun cameraRespondsSmoothlyWithoutOvershoot() {
        var position = -5f
        repeat(90) {
            val next = DioramaCameraSmoothing.approach(position, 20f, 6.32f, 1f / 60f)
            org.junit.Assert.assertTrue(next >= position && next <= 20f)
            position = next
        }
        org.junit.Assert.assertTrue(position > 19f)
    }

    @Test
    fun stalledFrameIsLimitedTo50MillisecondsOfCameraTravel() {
        val resumed = DioramaCameraSmoothing.approach(0f, 10f, 7.67f, 8f)
        val bounded = DioramaCameraSmoothing.approach(0f, 10f, 7.67f, 0.05f)
        assertEquals(bounded, resumed, 0.000001f)
    }

    @Test
    fun invalidOrZeroTimeNeverMovesCamera() {
        assertEquals(3f, DioramaCameraSmoothing.approach(3f, 10f, 7.67f, 0f), 0f)
        assertEquals(3f, DioramaCameraSmoothing.approach(3f, 10f, 7.67f, Float.NaN), 0f)
        assertEquals(3f, DioramaCameraSmoothing.approach(3f, 10f, 0f, 0.05f), 0f)
        assertEquals(3f, DioramaCameraSmoothing.approach(3f, Float.NaN, 7.67f, 0.05f), 0f)
    }
}
