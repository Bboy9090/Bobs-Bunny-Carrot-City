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

    @Test
    fun canonicalBobDesign_hasGoldenCreamFurAndApprovedDefaultSwag() {
        val bob = PlayableHero.BOBS_BUNNY
        assertEquals(Color(0xFFF4D58D), bob.bodyColor)
        assertEquals(Color(0xFFFFD600), bob.earOrHelmetColor)
        assertEquals(Color(0xFFFF6D00), bob.accentColor)

        val defaultHead = GameCatalog.swagItems.first { it.id == "head_classic_shades" }
        val defaultOuterwear = GameCatalog.swagItems.first { it.id == "hoodie_orange_drip" }
        val defaultKicks = GameCatalog.swagItems.first { it.id == "kicks_high_tops" }
        assertTrue(defaultHead.name.contains("Aviator Cap"))
        assertEquals(Color(0xFFFFD600), defaultHead.secondaryColor)
        assertEquals(Color(0xFFFF6D00), defaultOuterwear.primaryColor)
        assertEquals(Color(0xFFFFFBF2), defaultOuterwear.secondaryColor)
        assertEquals(Color(0xFFFFFFFF), defaultKicks.primaryColor)
        assertEquals(Color(0xFFFFD600), defaultKicks.secondaryColor)
    }

    @Test
    fun allTwentyStages_buildValidPlatformsCarrotsAndEnemies() {
        for (stage in GameCatalog.stages) {
            val engine = CartoonStageEngine(
                stageConfig = stage,
                selectedHero = PlayableHero.BOBS_BUNNY,
                equippedBadgeId = "badge_coyote_plus",
                trailColor = Color.White,
                soundEngine = sound
            )
            assertTrue("Stage ${stage.code} should have platforms", engine.stageData.platforms.isNotEmpty())
            assertTrue("Stage ${stage.code} should have carrots", engine.stageData.carrots.isNotEmpty())
            assertTrue("Stage ${stage.code} should have enemies", engine.stageData.enemies.isNotEmpty())
            assertTrue("Stage ${stage.code} goalX should be positive", engine.stageData.goalX > 500f)
        }
    }

    @Test
    fun coreMovement_runJumpHelicopterGlideAndBurrowWork() {
        val stage = GameCatalog.stages.first { it.code == "W1-1" }
        val engine = CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
        val startX = engine.playerX
        engine.inputRight = true
        repeat(15) { engine.stepFrame(0.03f) }
        assertTrue("Bob should move right", engine.playerX > startX)
        assertTrue("Bob should face right", engine.facingRight)

        engine.triggerJump()
        engine.inputJumpHeld = true
        assertFalse("Bob should be airborne after jump", engine.isGrounded)
        assertTrue("Bob should have upward velocity", engine.playerVy < 0f)

        // Advance until descending with jump held -> Helicopter Ears glide
        repeat(40) { engine.stepFrame(0.03f) }
        assertTrue("Bob should activate Helicopter Ears glide when holding jump while falling", engine.isHelicopterGliding)

        // Stop horizontal movement, land back, and test Burrow Blitz
        engine.inputRight = false
        engine.inputJumpHeld = false
        repeat(80) { engine.stepFrame(0.03f) }
        assertTrue("Bob should be grounded after landing", engine.isGrounded)
        engine.inputBurrowHeld = true
        engine.stepFrame(0.03f)
        assertTrue("Bob should burrow when grounded and holding burrow", engine.isBurrowing)
    }

    @Test
    fun donFoxoneBoss_threePhaseShowdownAndDefeatClearsStage() {
        val bossStage = GameCatalog.stages.first { it.code == "W1-BOSS" }
        val engine = CartoonStageEngine(
            stageConfig = bossStage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
        val boss = engine.stageData.enemies.first { it.kind == EnemyKind.DON_FOXONE_BOSS }
        assertEquals(3, boss.bossMaxHp)
        assertEquals(3, boss.bossHp)

        // Simulate 3 stomps / burrow flips across 3 phases (clearing bossStunTimer between hits)
        for (expectedHp in listOf(2, 1, 0)) {
            boss.bossStunTimer = 0f
            engine.playerX = boss.x
            engine.playerY = boss.y - 30f
            engine.isGrounded = false
            engine.playerVy = 250f
            engine.stepFrame(0.02f)
            assertEquals(expectedHp, boss.bossHp)
        }
        assertTrue("Defeating Don Foxone should clear W1-BOSS", engine.isStageCleared)
        assertTrue("Defeating Don Foxone should award bonus carrots", engine.collectedCarrots >= 75)
        assertTrue("Defeating Don Foxone should award Golden Shards", engine.collectedGoldenShards >= 2)
    }

    @Test
    fun checkpointSpringRescue_restoresBobToLastSafePlatform() {
        val stage = GameCatalog.stages.first { it.code == "W1-1" }
        val engine = CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
        engine.lastSafeX = 480f
        engine.lastSafeY = 440f
        engine.playerX = 900f
        engine.playerY = 900f
        engine.isGrounded = false
        engine.stepFrame(0.02f)

        assertEquals(CartoonHealthState.SCUFFED, engine.healthState)
        assertEquals(480f, engine.playerX, 0.1f)
        assertTrue(engine.playerY <= 420f)
        assertTrue(engine.invulnerableTimer > 0f)
    }

    @Test
    fun carrotCollectionAndEnemyStomp_updateStashAndSwag() {
        val stage = GameCatalog.stages.first { it.code == "W1-1" }
        val engine = CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
        val firstCarrot = engine.stageData.carrots.first { !it.isGoldenShard }
        engine.playerX = firstCarrot.x
        engine.playerY = firstCarrot.y + 36f
        engine.stepFrame(0.02f)
        assertTrue("Carrot should be collected", firstCarrot.collected)
        assertTrue("Collected carrots should increment", engine.collectedCarrots >= 1)

        val minion = engine.stageData.enemies.first { !it.kind.isBoss }
        engine.playerX = minion.x
        engine.playerY = minion.y - 25f
        engine.isGrounded = false
        engine.playerVy = 220f
        engine.stepFrame(0.02f)
        assertTrue("Stomping minion should flatten it into accordion", minion.isFlattenedAccordion)
    }
}

