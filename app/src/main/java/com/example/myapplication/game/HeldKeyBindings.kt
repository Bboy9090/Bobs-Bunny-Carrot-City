package com.example.myapplication.game

/**
 * A logical action remains held until every equivalent keyboard/controller
 * binding is released. Avoid cancelling movement when the player overlaps
 * a D-pad key and its keyboard alias.
 */
fun <T> hasHeldBinding(heldKeys: Set<T>, vararg bindings: T): Boolean =
    bindings.any { it in heldKeys }
