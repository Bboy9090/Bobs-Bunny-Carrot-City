package com.example.myapplication.ui.game3d

import androidx.compose.ui.graphics.Color
import com.example.myapplication.game.CartoonStageEngine
import com.example.myapplication.model.CartoonHealthState
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.PlatformKind
import com.example.myapplication.model.PowerUpType

/**
 * Render-only data. No mutable engine entities, Compose mutable state or
 * mutable engine collections cross the UI/game-loop -> GLES thread boundary.
 * These value objects must never hold references to EnemyEntity, ParticleDot
 * or the source MutableList instances.
 */
data class DioramaPlatform(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val kind: PlatformKind
)

data class DioramaCarrot(
    val x: Float,
    val y: Float,
    val isGoldenShard: Boolean,
    val collected: Boolean
)

data class DioramaPowerUp(
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    val collected: Boolean
)

data class DioramaEnemy(
    val x: Float,
    val y: Float,
    val kind: EnemyKind,
    val isFlattenedAccordion: Boolean
)

data class DioramaParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val alpha: Float
)

data class DioramaStageSnapshot(
    val goalX: Float,
    val platforms: List<DioramaPlatform>,
    val carrots: List<DioramaCarrot>,
    val powerUps: List<DioramaPowerUp>,
    val enemies: List<DioramaEnemy>
)

data class DioramaFrameSnapshot(
    val skyTop: Color,
    val playerX: Float,
    val playerY: Float,
    val playerVx: Float,
    val facingRight: Boolean,
    val frameTick: Int,
    val screenShakeTimer: Float,
    val isGrounded: Boolean,
    val invulnerableTimer: Float,
    val isBurrowing: Boolean,
    val trollDashTimer: Float,
    val squashStretchX: Float,
    val squashStretchY: Float,
    val healthState: CartoonHealthState,
    val isHelicopterGliding: Boolean,
    val isShowingDontLookDown: Boolean,
    val earSpinAngle: Float,
    val stageData: DioramaStageSnapshot,
    val particles: List<DioramaParticle>
) {
    companion object {
        /** Only call on the gameplay/Compose thread, after engine.stepFrame. */
        fun capture(engine: CartoonStageEngine): DioramaFrameSnapshot {
            val stage = engine.stageData
            return DioramaFrameSnapshot(
                skyTop = engine.stageConfig.biome.skyTop,
                playerX = engine.playerX,
                playerY = engine.playerY,
                playerVx = engine.playerVx,
                facingRight = engine.facingRight,
                frameTick = engine.frameTick,
                screenShakeTimer = engine.screenShakeTimer,
                isGrounded = engine.isGrounded,
                invulnerableTimer = engine.invulnerableTimer,
                isBurrowing = engine.isBurrowing,
                trollDashTimer = engine.trollDashTimer,
                squashStretchX = engine.squashStretchX,
                squashStretchY = engine.squashStretchY,
                healthState = engine.healthState,
                isHelicopterGliding = engine.isHelicopterGliding,
                isShowingDontLookDown = engine.isShowingDontLookDown,
                earSpinAngle = engine.earSpinAngle,
                stageData = DioramaStageSnapshot(
                    goalX = stage.goalX,
                    platforms = stage.platforms.map { DioramaPlatform(it.x, it.y, it.width, it.height, it.kind) },
                    carrots = stage.carrots.map { DioramaCarrot(it.x, it.y, it.isGoldenShard, it.collected) },
                    powerUps = stage.powerUps.map { DioramaPowerUp(it.x, it.y, it.type, it.collected) },
                    enemies = stage.enemies.map { DioramaEnemy(it.x, it.y, it.kind, it.isFlattenedAccordion) }
                ),
                particles = engine.particles.map { DioramaParticle(it.x, it.y, it.color, it.alpha) }
            )
        }
    }
}

/**
 * Single-producer / single-consumer handoff.
 *
 * Volatile publication provides the OpenGL renderer with one fully constructed
 * immutable frame. It never traverses live Compose state or engine collections.
 */
class DioramaFrameBridge(engine: CartoonStageEngine) {
    @Volatile
    private var latestFrame: DioramaFrameSnapshot = DioramaFrameSnapshot.capture(engine)

    fun publish(engine: CartoonStageEngine) {
        latestFrame = DioramaFrameSnapshot.capture(engine)
    }

    fun snapshot(): DioramaFrameSnapshot = latestFrame
}
