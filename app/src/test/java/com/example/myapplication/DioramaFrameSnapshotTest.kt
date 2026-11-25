package com.example.myapplication

import androidx.compose.ui.graphics.Color
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.game.CartoonStageEngine
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.PlayableHero
import com.example.myapplication.ui.game3d.DioramaFrameBridge
import com.example.myapplication.ui.game3d.DioramaFrameSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class DioramaFrameSnapshotTest {
    private fun newEngine() = CartoonStageEngine(
        stageConfig = GameCatalog.stages.first { it.code == "W1-1" },
        selectedHero = PlayableHero.BOBS_BUNNY,
        equippedBadgeId = "badge_coyote_plus",
        trailColor = Color.White,
        soundEngine = CartoonSoundEngine().apply { soundEnabled = false }
    )

    @Test
    fun captureDetachesMutableGameStateAndCollections() {
        val engine = newEngine()
        val frame = DioramaFrameSnapshot.capture(engine)
        assertNotSame(engine.stageData.enemies, frame.stageData.enemies)
        assertNotSame(engine.stageData.carrots, frame.stageData.carrots)
        assertNotSame(engine.stageData.platforms, frame.stageData.platforms)
        assertTrue(frame.stageData.enemies.isNotEmpty())
        assertTrue(frame.stageData.carrots.isNotEmpty())

        val originalPlayerX = frame.playerX
        val enemyX = frame.stageData.enemies.first().x
        val collected = frame.stageData.carrots.first().collected

        engine.playerX += 222f
        engine.stageData.enemies.first().x += 156f
        engine.stageData.carrots.first().collected = true
        engine.stageData.platforms.clear()

        assertEquals(originalPlayerX, frame.playerX)
        assertEquals(enemyX, frame.stageData.enemies.first().x)
        assertEquals(collected, frame.stageData.carrots.first().collected)
        assertTrue(frame.stageData.platforms.isNotEmpty())
    }

    @Test
    fun bridgeUpdatesOnlyWhenPublished() {
        val engine = newEngine()
        val bridge = DioramaFrameBridge(engine)
        val original = bridge.snapshot()
        engine.playerX += 200f
        assertEquals(original.playerX, bridge.snapshot().playerX)
        bridge.publish(engine)
        assertEquals(engine.playerX, bridge.snapshot().playerX)
        assertNotSame(original, bridge.snapshot())
    }

    @Test
    fun publishedCollectionsRejectMutationThroughMutableListCast() {
        val frame = DioramaFrameSnapshot.capture(newEngine())

        assertThrows(UnsupportedOperationException::class.java) {
            (frame.stageData.platforms as MutableList<*>).clear()
        }
        assertThrows(UnsupportedOperationException::class.java) {
            (frame.stageData.carrots as MutableList<*>).clear()
        }
        assertThrows(UnsupportedOperationException::class.java) {
            (frame.particles as MutableList<*>).clear()
        }
    }

    @Test
    fun renderThreadAlwaysReadsSingleInternallyConsistentPublishedFrame() {
        val engine = newEngine()
        val bridge = DioramaFrameBridge(engine)
        // Assign related values before publication on this producer thread.
        engine.frameTick = 0
        engine.playerX = 0f
        bridge.publish(engine)
        val failure = AtomicReference<String?>(null)
        val consumer = thread(start = true) {
            repeat(3000) {
                val frame = bridge.snapshot()
                if (frame.playerX != frame.frameTick.toFloat()) {
                    failure.set("torn render frame: x=${frame.playerX}, tick=${frame.frameTick}")
                    return@thread
                }
            }
        }
        for (i in 1..200) {
            engine.frameTick = i
            engine.playerX = i.toFloat()
            bridge.publish(engine)
        }
        consumer.join()
        assertEquals(null, failure.get())
    }
}
