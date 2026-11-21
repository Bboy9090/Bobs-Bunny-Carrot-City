package com.example.myapplication

import androidx.lifecycle.Lifecycle
import com.example.myapplication.ui.screens.GameplayLifecyclePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameplayLifecyclePolicyTest {
    @Test
    fun backgroundingALiveStagePausesIt() {
        assertTrue(GameplayLifecyclePolicy.shouldAutoPause(Lifecycle.Event.ON_PAUSE, isStageCleared = false, isStageFailed = false))
    }

    @Test
    fun finishedStagesKeepTheirResultOverlayInsteadOfPausing() {
        assertFalse(GameplayLifecyclePolicy.shouldAutoPause(Lifecycle.Event.ON_PAUSE, isStageCleared = true, isStageFailed = false))
        assertFalse(GameplayLifecyclePolicy.shouldAutoPause(Lifecycle.Event.ON_PAUSE, isStageCleared = false, isStageFailed = true))
    }

    @Test
    fun otherLifecycleEventsNeverAutoPause() {
        for (e in listOf(Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_START, Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY)) {
            assertFalse("$e must not pause", GameplayLifecyclePolicy.shouldAutoPause(e, isStageCleared = false, isStageFailed = false))
        }
    }

    @Test
    fun heldInputsAreReleasedWhenLeavingTheForeground() {
        assertTrue(GameplayLifecyclePolicy.shouldReleaseHeldInputs(Lifecycle.Event.ON_PAUSE))
        assertTrue(GameplayLifecyclePolicy.shouldReleaseHeldInputs(Lifecycle.Event.ON_STOP))
        assertFalse(GameplayLifecyclePolicy.shouldReleaseHeldInputs(Lifecycle.Event.ON_RESUME))
        assertFalse(GameplayLifecyclePolicy.shouldReleaseHeldInputs(Lifecycle.Event.ON_START))
    }
}
