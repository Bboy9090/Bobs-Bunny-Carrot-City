package com.example.myapplication

import androidx.compose.ui.graphics.Color
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.game.CarrotLegendMove
import com.example.myapplication.game.CarrotStyleComboEngine
import com.example.myapplication.game.CarrotStyleRank
import com.example.myapplication.game.CartoonStageEngine
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.PlayableHero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CarrotStyleComboEngineTest {
    private val sound = CartoonSoundEngine().apply { soundEnabled = false }

    @Test
    fun allFiveLegendMoves_progressRanksToLegendAndTrackPeak() {
        assertEquals(5, CarrotLegendMove.entries.size)
        assertEquals(CarrotStyleRank.D, CarrotStyleRank.fromScore(0))
        assertEquals(CarrotStyleRank.C, CarrotStyleRank.fromScore(45))
        assertEquals(CarrotStyleRank.B, CarrotStyleRank.fromScore(100))
        assertEquals(CarrotStyleRank.A, CarrotStyleRank.fromScore(175))
        assertEquals(CarrotStyleRank.S, CarrotStyleRank.fromScore(275))
        assertEquals(CarrotStyleRank.LEGEND, CarrotStyleRank.fromScore(420))

        val engine = CarrotStyleComboEngine()
        val sequence = listOf(
            CarrotLegendMove.EAR_COPTER_SPIN,
            CarrotLegendMove.TURBO_BURROW_POP,
            CarrotLegendMove.CARROT_RAIL_GRIND,
            CarrotLegendMove.QUAKE_CARROT_SLAM,
            CarrotLegendMove.EAR_COPTER_SPIN,
            CarrotLegendMove.TURBO_BURROW_POP,
            CarrotLegendMove.CARROT_RAIL_GRIND,
            CarrotLegendMove.QUAKE_CARROT_SLAM,
            CarrotLegendMove.CARROT_CROWN_FINALE
        )
        for (move in sequence) {
            engine.recordMove(move, swagMeter = 1.0f)
        }
        assertEquals(CarrotStyleRank.LEGEND, engine.rank)
        assertEquals(CarrotStyleRank.LEGEND, engine.peakRank)
        assertEquals(9, engine.streak)
    }

    @Test
    fun varietyFreshnessOutscoresRepeatedSpam() {
        val varied = CarrotStyleComboEngine()
        varied.recordMove(CarrotLegendMove.EAR_COPTER_SPIN)
        varied.recordMove(CarrotLegendMove.TURBO_BURROW_POP)
        varied.recordMove(CarrotLegendMove.CARROT_RAIL_GRIND)

        val spammed = CarrotStyleComboEngine()
        spammed.recordMove(CarrotLegendMove.EAR_COPTER_SPIN)
        spammed.recordMove(CarrotLegendMove.EAR_COPTER_SPIN)
        spammed.recordMove(CarrotLegendMove.EAR_COPTER_SPIN)

        assertTrue(varied.score > spammed.score)
        assertEquals(3, varied.recordMove(CarrotLegendMove.EAR_COPTER_SPIN).varietyCount)
    }

    @Test
    fun finaleGatingRequiresRankAAndHighSwag_thenAwardsBonusCarrots() {
        val engine = CarrotStyleComboEngine()
        val blockedEarly = engine.recordMove(CarrotLegendMove.CARROT_CROWN_FINALE, swagMeter = 1.0f)
        assertFalse(blockedEarly.accepted)

        repeat(6) { idx ->
            val m = if (idx % 2 == 0) CarrotLegendMove.CARROT_RAIL_GRIND else CarrotLegendMove.QUAKE_CARROT_SLAM
            engine.recordMove(m, swagMeter = 0.5f)
        }
        assertTrue(engine.rank.ordinal >= CarrotStyleRank.A.ordinal)

        val blockedLowSwag = engine.recordMove(CarrotLegendMove.CARROT_CROWN_FINALE, swagMeter = 0.4f)
        assertFalse(blockedLowSwag.accepted)

        val accepted = engine.recordMove(CarrotLegendMove.CARROT_CROWN_FINALE, swagMeter = 0.90f)
        assertTrue(accepted.accepted)
        assertEquals(15, accepted.bonusCarrots)
        assertEquals(1, engine.finaleCount)
    }

    @Test
    fun decayWindowPreservesPeakRankAndGuardsInvalidDt() {
        val engine = CarrotStyleComboEngine()
        engine.recordMove(CarrotLegendMove.QUAKE_CARROT_SLAM)
        val peak = engine.peakRank
        val initialScore = engine.score

        engine.decay(Float.NaN)
        engine.decay(-1.0f)
        assertEquals(initialScore, engine.score)

        engine.decay(1.0f)
        assertEquals(initialScore, engine.score)
        assertTrue(engine.decayTimerSeconds < 3.5f)

        engine.decay(20.0f)
        assertEquals(0, engine.score)
        assertEquals(CarrotStyleRank.D, engine.rank)
        assertEquals(0, engine.streak)
        assertEquals(peak, engine.peakRank)
    }

    @Test
    fun stageEngineIntegration_awardsCarrotsAndUpdatesHudStateOnFinale() {
        val stage = GameCatalog.stages.first()
        val stageEngine = CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.Yellow,
            soundEngine = sound
        )

        repeat(6) { idx ->
            val move = if (idx % 2 == 0) CarrotLegendMove.CARROT_RAIL_GRIND else CarrotLegendMove.QUAKE_CARROT_SLAM
            stageEngine.performStyleMove(move)
        }
        stageEngine.swagMeter = 0.95f
        stageEngine.performStyleMove(CarrotLegendMove.EAR_COPTER_SPIN)
        assertTrue(stageEngine.carrotFinaleReady)

        val carrotsBefore = stageEngine.collectedCarrots
        assertTrue(stageEngine.triggerCarrotCrownFinale())
        assertEquals(carrotsBefore + 15, stageEngine.collectedCarrots)
        assertTrue(stageEngine.styleScore > 0)
        assertTrue(stageEngine.floatingTexts.any { it.text.contains("24K CARROT FINALE") })
    }
}
