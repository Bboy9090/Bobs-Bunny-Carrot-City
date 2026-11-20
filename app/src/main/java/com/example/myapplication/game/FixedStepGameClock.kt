package com.example.myapplication.game

/**
 * Stable 60-Hz gameplay simulation independent of display refresh rate.
 * Caps catch-up work after a pause, dropped frame, or a busy emulator.
 *
 * One clock lives per stage attempt; pausing intentionally discards the old
 * clock instead of fast-forwarding the game when play resumes.
 */
class FixedStepGameClock {
    companion object {
        const val STEP_SECONDS: Float = 1f / 60f
        private const val MAX_STEPS_PER_FRAME = 4
    }

    private var accumulatedSeconds = 0f

    fun advance(realSeconds: Float, onFixedStep: (Float) -> Unit): Int {
        if (!realSeconds.isFinite() || realSeconds <= 0f) return 0
        accumulatedSeconds = (accumulatedSeconds + realSeconds.coerceAtMost(0.1f))
            .coerceAtMost(STEP_SECONDS * MAX_STEPS_PER_FRAME)
        var steps = 0
        while (accumulatedSeconds + 0.000001f >= STEP_SECONDS && steps < MAX_STEPS_PER_FRAME) {
            accumulatedSeconds = (accumulatedSeconds - STEP_SECONDS).coerceAtLeast(0f)
            onFixedStep(STEP_SECONDS)
            steps++
        }
        return steps
    }
}
