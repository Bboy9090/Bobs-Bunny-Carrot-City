package com.example.myapplication.ui.game3d

import kotlin.math.exp

/**
 * Time-based camera follow for the native diorama.
 *
 * Fixed per-render-frame lerps make the camera sluggish at 30 Hz and snappy
 * at 120 Hz. An exponential response produces the same position after the
 * same elapsed time, regardless of how many frames were rendered.
 */
internal object DioramaCameraSmoothing {
    fun approach(
        current: Float,
        target: Float,
        responsePerSecond: Float,
        frameSeconds: Float
    ): Float {
        if (!target.isFinite()) return current
        if (!current.isFinite()) return target
        if (!responsePerSecond.isFinite() || responsePerSecond <= 0f) return current
        if (!frameSeconds.isFinite() || frameSeconds <= 0f) return current

        // Clamping a stalled frame prevents an EGL resume from snapping the
        // camera across the level. Never move more than the elapsed 50 ms step.
        val dt = frameSeconds.coerceAtMost(0.05f)
        val follow = 1f - exp(-responsePerSecond * dt)
        return current + (target - current) * follow
    }
}
