package com.example.myapplication

import androidx.compose.ui.graphics.Color
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.game.CartoonStageEngine
import com.example.myapplication.model.CartoonHealthState
import com.example.myapplication.model.DailyWeatherMutator
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.PlatformKind
import com.example.myapplication.model.PlayableHero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    private val sound = CartoonSoundEngine().apply { soundEnabled = false }

    @Test
    fun phase5Catalog_hasTwentyStagesElevenWorldsAndSixHeroes() {
        assertEquals(20, GameCatalog.stages.size)
        assertEquals(6, PlayableHero.entries.size)
        assertEquals(GameCatalog.stages.size, GameCatalog.stages.map { it.id }.toSet().size)
        val worlds = GameCatalog.stages.map { it.worldNumber }.toSet()
        assertTrue(worlds.containsAll((1..11).toList()))
        assertEquals("W10-RUSH", GameCatalog.stages.first { it.isBossRush }.code)
        val bosses = GameCatalog.stages.mapNotNull { it.bossEnemyKind }.toSet()
        assertTrue(bosses.contains(EnemyKind.DON_FOXONE_BOSS))
        assertTrue(bosses.contains(EnemyKind.GREAT_FURTINI_BOSS))
        assertTrue(bosses.contains(EnemyKind.WRENCH_BOSS))
        assertTrue(bosses.contains(EnemyKind.EMPRESS_BUNZILLA_BOSS))
        assertTrue(bosses.contains(EnemyKind.GOLDEN_GUARDIAN_BOSS))
        assertTrue(bosses.contains(EnemyKind.OMEGA_ANIMATOR_BOSS))
    }

    @Test
    fun phase4BossRush_spawnsFourBossesAndHeroSpecialsWork() {
        val bossRushStage = GameCatalog.stages.first { it.isBossRush }
        val engine = CartoonStageEngine(
            stageConfig = bossRushStage,
            selectedHero = PlayableHero.GOLDEN_BOBS_CHAMPION,
            weatherMutator = DailyWeatherMutator.GOLDEN_STAR_RAIN,
            equippedBadgeId = "badge_24k_rainbow_stride",
            trailColor = Color.Yellow,
            soundEngine = sound
        )
        assertEquals(4, engine.stageData.enemies.count { it.kind.isBoss })

        val initialPlatforms = engine.stageData.platforms.size
        engine.triggerTrollDashOrAction()
        assertTrue(engine.stageData.platforms.size > initialPlatforms)
        assertEquals(PlatformKind.RAINBOW_BRIDGE, engine.stageData.platforms.last().kind)
        assertEquals(CartoonHealthState.FULL_SWAG, engine.healthState)

        val empressEngine = CartoonStageEngine(
            stageConfig = bossRushStage,
            selectedHero = PlayableHero.COSMIC_EMPRESS_BUNZILLA,
            weatherMutator = DailyWeatherMutator.MOON_BOUNCE_GALAXY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.Magenta,
            soundEngine = sound
        )
        empressEngine.triggerTrollDashOrAction()
        assertTrue(empressEngine.floatingTexts.any { it.text.contains("STARLIGHT NOVA") })
    }

    @Test
    fun phase5OmegaBoss_hasSevenHpAndDonFoxoneSteamrollerWorks() {
        val omegaStage = GameCatalog.stages.first { it.bossEnemyKind == EnemyKind.OMEGA_ANIMATOR_BOSS }
        val engine = CartoonStageEngine(
            stageConfig = omegaStage,
            selectedHero = PlayableHero.REFORMED_DON_FOXONE,
            weatherMutator = DailyWeatherMutator.LOONEY_INK_OVERDRIVE,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.Red,
            soundEngine = sound
        )
        val omega = engine.stageData.enemies.first { it.kind == EnemyKind.OMEGA_ANIMATOR_BOSS }
        assertEquals(7, omega.bossMaxHp)

        engine.triggerTrollDashOrAction()
        assertTrue(engine.floatingTexts.any { it.text.contains("STEAMROLLER BLITZ") })
        assertTrue(engine.invulnerableTimer > 0f)
    }

    @Test
    fun phase5HoloGhostAndSidekickDrone_trackAndToggle() {
        val stage = GameCatalog.stages.first { it.code == "W11-1" }
        val engine = CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
        val ghostStart = engine.ghostRivalX
        repeat(30) { engine.stepFrame(0.033f) }
        assertTrue(engine.ghostRivalX > ghostStart)

        assertTrue(engine.sidekickDroneEnabled)
        engine.toggleSidekickDrone()
        assertFalse(engine.sidekickDroneEnabled)
    }

    @Test
    fun cartoonPhysics_screenShakeAndSquashStretch_behaveCorrectly() {
        val stage = GameCatalog.stages.first()
        val engine = CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
        assertEquals(0f, engine.screenShakeTimer)
        assertEquals(1f, engine.squashStretchX)
        assertEquals(1f, engine.squashStretchY)

        engine.triggerScreenShake(duration = 0.4f, intensity = 12f)
        assertEquals(0.4f, engine.screenShakeTimer)
        assertEquals(12f, engine.screenShakeIntensity)

        engine.squashStretchX = 1.35f
        engine.squashStretchY = 0.65f
        engine.stepFrame(0.1f)
        assertTrue(engine.screenShakeTimer < 0.4f)
        assertTrue(engine.squashStretchX < 1.35f)
        assertTrue(engine.squashStretchY > 0.65f)
    }
}

