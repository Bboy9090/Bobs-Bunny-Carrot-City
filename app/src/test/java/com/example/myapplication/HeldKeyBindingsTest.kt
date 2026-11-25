package com.example.myapplication

import com.example.myapplication.game.hasHeldBinding
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeldKeyBindingsTest {
    @Test
    fun releasingOneAliasDoesNotCancelTheOther() {
        val held = mutableSetOf("left", "a")
        assertTrue(hasHeldBinding(held, "left", "a"))
        held.remove("a")
        assertTrue(hasHeldBinding(held, "left", "a"))
        held.remove("left")
        assertFalse(hasHeldBinding(held, "left", "a"))
    }

    @Test
    fun unrelatedKeysDoNotActivateOtherActions() {
        assertFalse(hasHeldBinding(setOf("right"), "left", "a"))
        assertTrue(hasHeldBinding(setOf("right", "jump"), "jump", "up", "gamepad-a"))
        assertFalse(hasHeldBinding(emptySet<String>(), "jump", "up"))
    }
}
