package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.DailyWeatherMutator
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.PlayableHero
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlayerProfileState(
    val totalCarrots: Int = 250,
    val goldenShards: Int = 5,
    val highestUnlockedStage: Int = 20,
    val stageStars: Map<Int, Int> = (1..20).associateWith { 0 },
    val bestStageTimesSec: Map<Int, Float> = emptyMap(),
    val selectedHeroId: String = PlayableHero.BOBS_BUNNY.id,
    val activeMutatorId: String = DailyWeatherMutator.CLEAR_SKIES.id,
    val claimedQuestIds: Set<String> = emptySet(),
    val savedStudioCode: String = "GS-RAINBOW-24K",
    val ownedItemIds: Set<String> = setOf(
        "head_classic_shades",
        "hoodie_orange_drip",
        "kicks_high_tops",
        "trail_stardust",
        "badge_coyote_plus"
    ),
    val equippedHeadgearId: String = "head_classic_shades",
    val equippedHoodieId: String = "hoodie_orange_drip",
    val equippedKicksId: String = "kicks_high_tops",
    val equippedTrailId: String = "trail_stardust",
    val equippedBadgeId: String = "badge_coyote_plus",
    val multiplayerWins: Int = 0
) {
    val selectedHero: PlayableHero
        get() = PlayableHero.entries.firstOrNull { it.id == selectedHeroId } ?: PlayableHero.BOBS_BUNNY

    val activeMutator: DailyWeatherMutator
        get() = DailyWeatherMutator.entries.firstOrNull { it.id == activeMutatorId } ?: DailyWeatherMutator.CLEAR_SKIES

    val totalStarsEarned: Int
        get() = stageStars.values.sum()

    val bossStagesClearedCount: Int
        get() = GameCatalog.stages.count { it.isBossStage && (stageStars[it.id] ?: 0) > 0 }

    val goldenStatueTierName: String
        get() = when {
            goldenShards >= 10 -> "Tier 5: 24-Karat Golden World Portal (FULLY POWERED!)"
            goldenShards >= 6 -> "Tier 4: Solid 24-Karat Bobs Monument"
            goldenShards >= 4 -> "Tier 3: Gleaming Silver Street Statue"
            goldenShards >= 2 -> "Tier 2: Polished Bronze Bunny Plaza"
            else -> "Tier 1: Wooden Carrot Crate Pedestal"
        }

    val goldenWorldProgressFraction: Float
        get() = (goldenShards.coerceAtMost(10)) / 10f
}

class GameProgressRepository internal constructor(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences("bobs_bunny_carrot_city_prefs", Context.MODE_PRIVATE)
    )

    private val _state = MutableStateFlow(loadState())
    val state: StateFlow<PlayerProfileState> = _state.asStateFlow()

    private fun loadState(): PlayerProfileState {
        val carrots = prefs.getInt("total_carrots", 250).coerceAtLeast(0)
        val shards = prefs.getInt("golden_shards", 5).coerceAtLeast(0)
        val unlocked = maxOf(prefs.getInt("highest_unlocked_stage", 20), 20)
        val mpWins = prefs.getInt("mp_wins", 0).coerceAtLeast(0)
        val heroId = prefs.getString("selected_hero_id", PlayableHero.BOBS_BUNNY.id) ?: PlayableHero.BOBS_BUNNY.id
        val mutatorId = prefs.getString("active_mutator_id", DailyWeatherMutator.CLEAR_SKIES.id)
            ?: DailyWeatherMutator.CLEAR_SKIES.id
        val claimedQuests = prefs.getStringSet("claimed_quests", emptySet())?.toSet() ?: emptySet()
        val studioCode = prefs.getString("saved_studio_code", "GS-RAINBOW-24K") ?: "GS-RAINBOW-24K"

        val ownedDefault = setOf(
            "head_classic_shades",
            "hoodie_orange_drip",
            "kicks_high_tops",
            "trail_stardust",
            "badge_coyote_plus"
        )
        val owned = prefs.getStringSet("owned_items", ownedDefault)?.toSet() ?: ownedDefault

        val stars = (1..20).associateWith { id ->
            prefs.getInt("stage_stars_$id", 0).coerceIn(0, 3)
        }
        val bestTimes = mutableMapOf<Int, Float>()
        for (id in 1..20) {
            val t = prefs.getFloat("stage_best_time_$id", -1f)
            if (t.isFinite() && t > 0f) bestTimes[id] = t
        }

        return PlayerProfileState(
            totalCarrots = carrots,
            goldenShards = shards,
            highestUnlockedStage = unlocked,
            stageStars = stars,
            bestStageTimesSec = bestTimes,
            selectedHeroId = heroId,
            activeMutatorId = mutatorId,
            claimedQuestIds = claimedQuests,
            savedStudioCode = studioCode,
            ownedItemIds = owned,
            equippedHeadgearId = prefs.getString("eq_head", "head_classic_shades") ?: "head_classic_shades",
            equippedHoodieId = prefs.getString("eq_hoodie", "hoodie_orange_drip") ?: "hoodie_orange_drip",
            equippedKicksId = prefs.getString("eq_kicks", "kicks_high_tops") ?: "kicks_high_tops",
            equippedTrailId = prefs.getString("eq_trail", "trail_stardust") ?: "trail_stardust",
            equippedBadgeId = prefs.getString("eq_badge", "badge_coyote_plus") ?: "badge_coyote_plus",
            multiplayerWins = mpWins
        )
    }

    private fun saveState(newState: PlayerProfileState) {
        _state.value = newState
        prefs.edit().apply {
            putInt("total_carrots", newState.totalCarrots)
            putInt("golden_shards", newState.goldenShards)
            putInt("highest_unlocked_stage", newState.highestUnlockedStage)
            putInt("mp_wins", newState.multiplayerWins)
            putString("selected_hero_id", newState.selectedHeroId)
            putString("active_mutator_id", newState.activeMutatorId)
            putString("saved_studio_code", newState.savedStudioCode)
            putStringSet("claimed_quests", newState.claimedQuestIds)
            putStringSet("owned_items", newState.ownedItemIds)
            putString("eq_head", newState.equippedHeadgearId)
            putString("eq_hoodie", newState.equippedHoodieId)
            putString("eq_kicks", newState.equippedKicksId)
            putString("eq_trail", newState.equippedTrailId)
            putString("eq_badge", newState.equippedBadgeId)
            newState.stageStars.forEach { (stageId, starCount) ->
                putInt("stage_stars_$stageId", starCount)
            }
            newState.bestStageTimesSec.forEach { (stageId, bestTime) ->
                putFloat("stage_best_time_$stageId", bestTime)
            }
        }.apply()
    }

    fun selectHero(hero: PlayableHero) {
        saveState(_state.value.copy(selectedHeroId = hero.id))
    }

    fun selectWeatherMutator(mutator: DailyWeatherMutator) {
        saveState(_state.value.copy(activeMutatorId = mutator.id))
    }

    fun saveCustomStudioCode(code: String) {
        saveState(_state.value.copy(savedStudioCode = code))
    }

    fun claimCityQuest(questId: String, rewardCarrots: Int, rewardShards: Int): Boolean {
        val cleanId = questId.trim()
        if (cleanId.isEmpty()) return false
        val current = _state.value
        if (current.claimedQuestIds.contains(cleanId)) return false
        saveState(
            current.copy(
                totalCarrots = current.totalCarrots + rewardCarrots.coerceAtLeast(0),
                goldenShards = current.goldenShards + rewardShards.coerceAtLeast(0),
                claimedQuestIds = current.claimedQuestIds + cleanId
            )
        )
        return true
    }

    fun recordStageClear(
        stageId: Int,
        carrotsEarned: Int,
        shardsEarned: Int,
        starsEarned: Int,
        clearTimeSeconds: Float = 0f
    ) {
        if (stageId !in 1..20) return
        val current = _state.value
        val previousStars = current.stageStars[stageId] ?: 0
        val updatedStars = current.stageStars.toMutableMap().apply {
            put(stageId, maxOf(previousStars, starsEarned.coerceIn(1, 3)))
        }
        val updatedTimes = current.bestStageTimesSec.toMutableMap()
        if (clearTimeSeconds.isFinite() && clearTimeSeconds > 0f) {
            val prevBest = updatedTimes[stageId]
            if (prevBest == null || clearTimeSeconds < prevBest) {
                updatedTimes[stageId] = clearTimeSeconds
            }
        }
        val nextUnlocked = maxOf(current.highestUnlockedStage, (stageId + 1).coerceAtMost(20))
        saveState(
            current.copy(
                totalCarrots = current.totalCarrots + carrotsEarned.coerceAtLeast(0),
                goldenShards = current.goldenShards + shardsEarned.coerceAtLeast(0),
                highestUnlockedStage = nextUnlocked,
                stageStars = updatedStars,
                bestStageTimesSec = updatedTimes
            )
        )
    }

    fun addCarrotsAndShards(carrots: Int, shards: Int = 0, mpWin: Boolean = false) {
        val current = _state.value
        saveState(
            current.copy(
                totalCarrots = (current.totalCarrots + carrots).coerceAtLeast(0),
                goldenShards = (current.goldenShards + shards).coerceAtLeast(0),
                multiplayerWins = current.multiplayerWins + (if (mpWin) 1 else 0)
            )
        )
    }

    fun purchaseOrEquipItem(itemId: String, categoryName: String, cost: Int): Boolean {
        if (cost < 0 || itemId.isBlank()) return false
        val current = _state.value
        val alreadyOwned = current.ownedItemIds.contains(itemId)
        if (!alreadyOwned && current.totalCarrots < cost) {
            return false
        }
        val newCarrots = if (alreadyOwned) current.totalCarrots else current.totalCarrots - cost
        val newOwned = current.ownedItemIds + itemId

        val updated = when (categoryName) {
            "HEADGEAR" -> current.copy(totalCarrots = newCarrots, ownedItemIds = newOwned, equippedHeadgearId = itemId)
            "HOODIE" -> current.copy(totalCarrots = newCarrots, ownedItemIds = newOwned, equippedHoodieId = itemId)
            "KICKS" -> current.copy(totalCarrots = newCarrots, ownedItemIds = newOwned, equippedKicksId = itemId)
            "TRAIL" -> current.copy(totalCarrots = newCarrots, ownedItemIds = newOwned, equippedTrailId = itemId)
            "BADGE" -> current.copy(totalCarrots = newCarrots, ownedItemIds = newOwned, equippedBadgeId = itemId)
            else -> return false
        }
        saveState(updated)
        return true
    }
}
