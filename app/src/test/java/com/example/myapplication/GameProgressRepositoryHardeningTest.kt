package com.example.myapplication

import android.content.SharedPreferences
import com.example.myapplication.data.GameProgressRepository
import org.junit.Assert.*
import org.junit.Test

private class InMemorySharedPreferences : SharedPreferences {
    private val map = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = map.toMutableMap()
    override fun getString(key: String?, defValue: String?): String? = (map[key] as? String) ?: defValue
    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        (map[key] as? Set<String>)?.toMutableSet() ?: defValues
    override fun getInt(key: String?, defValue: Int): Int = (map[key] as? Int) ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = (map[key] as? Long) ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = (map[key] as? Float) ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = (map[key] as? Boolean) ?: defValue
    override fun contains(key: String?): Boolean = map.containsKey(key)
    override fun edit(): SharedPreferences.Editor = EditorImpl()
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {}

    private inner class EditorImpl : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clearRequested = false

        override fun putString(key: String?, value: String?) = apply { if (key != null) pending[key] = value }
        override fun putStringSet(key: String?, values: MutableSet<String>?) = apply {
            if (key != null) pending[key] = values?.toSet()
        }
        override fun putInt(key: String?, value: Int) = apply { if (key != null) pending[key] = value }
        override fun putLong(key: String?, value: Long) = apply { if (key != null) pending[key] = value }
        override fun putFloat(key: String?, value: Float) = apply { if (key != null) pending[key] = value }
        override fun putBoolean(key: String?, value: Boolean) = apply { if (key != null) pending[key] = value }
        override fun remove(key: String?) = apply { if (key != null) pending[key] = REMOVE }
        override fun clear() = apply { clearRequested = true }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearRequested) map.clear()
            for ((k, v) in pending) {
                if (v === REMOVE) map.remove(k) else map[k] = v
            }
        }
    }

    private object REMOVE
}

class GameProgressRepositoryHardeningTest {

    @Test
    fun corruptedNegativeAndNaNPrefs_areSafelyClampedOnLoad() {
        val prefs = InMemorySharedPreferences()
        prefs.edit()
            .putInt("total_carrots", -500)
            .putInt("golden_shards", -20)
            .putInt("mp_wins", -3)
            .putInt("stage_stars_1", 99)
            .putFloat("stage_best_time_1", Float.NaN)
            .apply()

        val repo = GameProgressRepository(prefs)
        val state = repo.state.value
        assertEquals(0, state.totalCarrots)
        assertEquals(0, state.goldenShards)
        assertEquals(0, state.multiplayerWins)
        assertEquals(3, state.stageStars[1])
        assertNull(state.bestStageTimesSec[1])
    }

    @Test
    fun purchaseUnknownCategoryOrNegativeCost_doesNotDeductCarrots() {
        val prefs = InMemorySharedPreferences()
        val repo = GameProgressRepository(prefs)
        val startingCarrots = repo.state.value.totalCarrots

        assertFalse(repo.purchaseOrEquipItem("unknown_item", "INVALID_CATEGORY", 50))
        assertEquals(startingCarrots, repo.state.value.totalCarrots)

        assertFalse(repo.purchaseOrEquipItem("head_test", "HEADGEAR", -10))
        assertEquals(startingCarrots, repo.state.value.totalCarrots)
    }

    @Test
    fun recordStageClear_ignoresInvalidStageIdAndNonFiniteTimeAndNegativeRewards() {
        val prefs = InMemorySharedPreferences()
        val repo = GameProgressRepository(prefs)
        val initialCarrots = repo.state.value.totalCarrots

        repo.recordStageClear(stageId = 999, carrotsEarned = 100, shardsEarned = 5, starsEarned = 3, clearTimeSeconds = 10f)
        assertEquals(initialCarrots, repo.state.value.totalCarrots)

        repo.recordStageClear(stageId = 1, carrotsEarned = -50, shardsEarned = -2, starsEarned = 5, clearTimeSeconds = Float.POSITIVE_INFINITY)
        assertEquals(initialCarrots, repo.state.value.totalCarrots)
        assertEquals(3, repo.state.value.stageStars[1])
        assertNull(repo.state.value.bestStageTimesSec[1])

        // Re-loading from prefs preserves state reliably
        repo.recordStageClear(stageId = 2, carrotsEarned = 30, shardsEarned = 1, starsEarned = 2, clearTimeSeconds = 18.5f)
        val reloaded = GameProgressRepository(prefs)
        assertEquals(initialCarrots + 30, reloaded.state.value.totalCarrots)
        assertEquals(18.5f, reloaded.state.value.bestStageTimesSec[2]!!, 0.001f)
    }
}
