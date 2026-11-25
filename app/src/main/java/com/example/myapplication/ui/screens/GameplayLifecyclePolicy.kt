package com.example.myapplication.ui.screens

import androidx.lifecycle.Lifecycle

/**
 * Decides how a running stage reacts to Activity lifecycle changes. Kept free of Compose and
 * Android state so the rule is unit-testable on the JVM.
 *
 * Backgrounding the app while a stage is live must pause it: otherwise the player returns to a
 * running game (and, for touch users, to controls whose release events were never delivered).
 * A cleared or failed stage already shows its own result overlay, so it is left alone.
 */
object GameplayLifecyclePolicy {
    fun shouldAutoPause(event: Lifecycle.Event, isStageCleared: Boolean, isStageFailed: Boolean): Boolean =
        event == Lifecycle.Event.ON_PAUSE && !isStageCleared && !isStageFailed

    /** Held keys/buttons must always be released when the app leaves the foreground. */
    fun shouldReleaseHeldInputs(event: Lifecycle.Event): Boolean =
        event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP
}
