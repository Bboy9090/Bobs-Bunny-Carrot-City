package com.example.myapplication.ui.game3d

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.myapplication.game.LevelBuilder
import com.example.myapplication.model.CartoonHealthState
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.PlatformKind
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import java.util.concurrent.atomic.AtomicInteger

/**
 * Real-time OpenGL ES 3.0 3D Diorama renderer for Bob's Bunny: Carrot City.
 * Reads an immutable, atomically published [DioramaFrameSnapshot] while rendering
 * 3D geometry, directional + rim lighting, projected shadows, and an
 * articulated 3D Bob character rig.
 */
class CarrotCity3DRenderer(
    private val frames: DioramaFrameBridge,
    private val onFirstFrame: () -> Unit = {},
    private val onInitializationFailure: (String) -> Unit = {}
) : GLSurfaceView.Renderer {

    companion object {
        private const val WORLD_SCALE = 0.01f

        /**
         * Instrumentation receipt incremented only AFTER a complete GLES3 scene
         * draw. Avoids Compose's idle detector during the 60-FPS game loop.
         * Does not expose gameplay state or modify the renderer's draw path.
         */
        val completedFirstFrames = AtomicInteger(0)
    }

    private lateinit var shader: DioramaShader
    private lateinit var box: Mesh3D
    private lateinit var sphere: Mesh3D
    private lateinit var cylinder: Mesh3D
    private lateinit var cone: Mesh3D

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Smooth 3D camera state
    private var camX3d = 1.6f
    private var camY3d = 1.2f

    // Track FPS for performance telemetry
    @Volatile
    var currentFps: Int = 60
        private set
    private var fpsFrames = 0
    private var fpsLastTimeNs = System.nanoTime()
    private var initialized = false
    private var firstFrameReported = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        initialized = false
        firstFrameReported = false
        try {
            Log.i("BobsBunnyDiorama", "onSurfaceCreated GLES version=" +
                GLES30.glGetString(GLES30.GL_VERSION) + " renderer=" +
                GLES30.glGetString(GLES30.GL_RENDERER))
            GLES30.glEnable(GLES30.GL_DEPTH_TEST)
            GLES30.glEnable(GLES30.GL_CULL_FACE)
            GLES30.glEnable(GLES30.GL_BLEND)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            shader = DioramaShader()
            box = MeshPrimitives.createBox()
            sphere = MeshPrimitives.createSphere()
            cylinder = MeshPrimitives.createCylinder()
            cone = MeshPrimitives.createCone()
            initialized = true
        } catch (error: RuntimeException) {
            onInitializationFailure(error.message ?: "OpenGL ES 3.0 initialization failed")
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspect = width.toFloat() / height.coerceAtLeast(1).toFloat()
        Matrix.perspectiveM(projMatrix, 0, 42f, aspect, 0.5f, 90f)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (!initialized) {
            GLES30.glClearColor(0.07f, 0.12f, 0.22f, 1f)
            GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
            return
        }
        fpsFrames++
        val now = System.nanoTime()
        if (now - fpsLastTimeNs >= 1_000_000_000L) {
            currentFps = fpsFrames
            fpsFrames = 0
            fpsLastTimeNs = now
        }

        // Read exactly one published snapshot for this GL frame; never mix updates.
        val frame = frames.snapshot()
        val sky = frame.skyTop
        GLES30.glClearColor(
            sky.red.coerceIn(0.18f, 0.55f),
            sky.green.coerceIn(0.45f, 0.85f),
            sky.blue.coerceIn(0.75f, 1.0f),
            1f
        )
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        GLES30.glUseProgram(shader.programId)
        GLES30.glUniform3f(shader.lightDirLoc, 0.45f, 0.85f, 0.55f)

        val playerX3d = frame.playerX * WORLD_SCALE
        val playerY3d = (LevelBuilder.GROUND_Y - frame.playerY) * WORLD_SCALE
        val lookAhead = if (frame.facingRight) 2.1f else -1.2f
        val targetCamX = (playerX3d + lookAhead).coerceAtLeast(3.5f)
        val targetCamY = (playerY3d * 0.45f + 1.35f).coerceIn(1.1f, 4.5f)

        camX3d += (targetCamX - camX3d) * 0.12f
        camY3d += (targetCamY - camY3d) * 0.10f

        val shakeX = if (frame.screenShakeTimer > 0f) sin(frame.frameTick * 2.1f) * 0.08f else 0f
        val shakeY = if (frame.screenShakeTimer > 0f) cos(frame.frameTick * 2.7f) * 0.06f else 0f

        Matrix.setLookAtM(
            viewMatrix, 0,
            camX3d + shakeX, camY3d + 1.35f + shakeY, 10.8f,
            camX3d + shakeX, camY3d + 0.15f + shakeY, 0f,
            0f, 1f, 0f
        )
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        val t = frame.frameTick * 0.05f

        drawDistantCarrotCitySkyline(camX3d)
        drawMidgroundDioramaProps(camX3d, t)
        drawPlatforms3D(camX3d, t, frame)
        drawCarrots3D(camX3d, t, frame)
        drawPowerUps3D(camX3d, t, frame)
        drawEnemies3D(camX3d, t, frame)
        drawGoalSafehouse3D(t, frame)
        drawBob3D(playerX3d, playerY3d, t, frame)
        drawParticles3D(frame)
        drawForegroundFraming(camX3d)
        if (!firstFrameReported) {
            firstFrameReported = true
            completedFirstFrames.incrementAndGet()
            onFirstFrame()
        }
    }

    private fun drawMesh(
        mesh: Mesh3D,
        x: Float, y: Float, z: Float,
        sx: Float, sy: Float, sz: Float,
        r: Float, g: Float, b: Float, a: Float = 1f,
        rotX: Float = 0f, rotY: Float = 0f, rotZ: Float = 0f
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        if (rotY != 0f) Matrix.rotateM(modelMatrix, 0, rotY, 0f, 1f, 0f)
        if (rotZ != 0f) Matrix.rotateM(modelMatrix, 0, rotZ, 0f, 0f, 1f)
        if (rotX != 0f) Matrix.rotateM(modelMatrix, 0, rotX, 1f, 0f, 0f)
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)

        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        GLES30.glUniformMatrix4fv(shader.mvpLoc, 1, false, mvpMatrix, 0)
        GLES30.glUniformMatrix4fv(shader.modelLoc, 1, false, modelMatrix, 0)
        GLES30.glUniform4f(shader.colorLoc, r, g, b, a)
        mesh.draw(shader.posLoc, shader.normLoc)
    }

    private fun drawDistantCarrotCitySkyline(camX: Float) {
        // Distant rolling green N64 hills
        for (i in -2..12) {
            val hx = i * 6.5f
            if (abs(hx - camX) > 22f) continue
            drawMesh(sphere, hx, -1.5f, -13f, 9f, 5.5f, 4f, 0.22f, 0.62f, 0.30f)
        }
        // Distant Carrot City towers
        for (i in 0..14) {
            val bx = i * 4.2f - 2f
            if (abs(bx - camX) > 24f) continue
            val h = 4.5f + (i % 3) * 2.2f
            drawMesh(box, bx, h * 0.5f - 1.5f, -16f, 2.2f, h, 2.2f, 0.28f, 0.36f, 0.54f)
            // Glowing tower roof accent
            drawMesh(cone, bx, h - 1.1f, -16f, 2.4f, 1.0f, 2.4f, 1.0f, 0.52f, 0.10f)
        }
    }

    private fun drawMidgroundDioramaProps(camX: Float, t: Float) {
        for (i in 0..18) {
            val mx = i * 3.4f - 1f
            if (abs(mx - camX) > 16f) continue
            if (i % 3 == 0) {
                // Stylized 3D Carrot City townhouse
                drawMesh(box, mx, 1.2f, -5.2f, 2.2f, 2.6f, 1.8f, 0.96f, 0.88f, 0.72f)
                drawMesh(cone, mx, 2.95f, -5.2f, 2.7f, 1.1f, 2.3f, 0.92f, 0.35f, 0.14f, rotY = 45f)
                // Warm storefront and readable lit windows along the avenue.
                // Cheap geometric trim keeps the retro 3D look without textures.
                drawMesh(box, mx, 0.18f, -4.27f, 0.84f, 0.82f, 0.08f, 0.23f, 0.31f, 0.46f)
                drawMesh(box, mx, 2.06f, -4.25f, 1.55f, 0.12f, 0.12f, 0.70f, 0.39f, 0.20f)
                for (window in -1..1 step 2) {
                    val wx = mx + window * 0.48f
                    drawMesh(box, wx, 1.45f, -4.27f, 0.42f, 0.58f, 0.09f, 1.0f, 0.82f, 0.36f)
                    drawMesh(box, wx, 1.45f, -4.18f, 0.045f, 0.60f, 0.06f, 0.37f, 0.22f, 0.15f)
                }
            } else {
                // Layered 3D N64 pine/forest tree
                drawMesh(cylinder, mx, 0.7f, -4.4f, 0.36f, 1.8f, 0.36f, 0.45f, 0.28f, 0.15f)
                val sway = sin(t + i) * 2.5f
                drawMesh(cone, mx, 2.1f, -4.4f, 1.6f, 1.6f, 1.6f, 0.16f, 0.58f, 0.24f, rotZ = sway)
                drawMesh(cone, mx, 3.0f, -4.4f, 1.2f, 1.3f, 1.2f, 0.22f, 0.68f, 0.28f, rotZ = sway)
            }
        }
    }

    private fun drawPlatforms3D(camX: Float, t: Float, frame: DioramaFrameSnapshot) {
        for (plat in frame.stageData.platforms) {
            val px = (plat.x + plat.width * 0.5f) * WORLD_SCALE
            val pw = plat.width * WORLD_SCALE
            if (abs(px - camX) > pw * 0.5f + 13f) continue

            val topY = (LevelBuilder.GROUND_Y - plat.y) * WORLD_SCALE
            val ph = (plat.height * WORLD_SCALE).coerceAtLeast(0.32f)
            val centerY = topY - ph * 0.5f

            when (plat.kind) {
                PlatformKind.BOUNCY_AWNING -> {
                    drawMesh(box, px, centerY, 0f, pw, ph * 0.7f, 2.2f, 0.95f, 0.18f, 0.32f)
                    drawMesh(box, px, topY - 0.04f, 0f, pw * 0.92f, 0.10f, 2.24f, 1.0f, 1.0f, 1.0f)
                }
                PlatformKind.UPDRAFT_VENT -> {
                    drawMesh(cylinder, px, centerY, 0f, pw, ph, 1.8f, 0.35f, 0.45f, 0.52f)
                    drawMesh(box, px, topY + 0.04f, 0f, pw * 0.8f, 0.08f, 0.35f, 0.0f, 0.90f, 1.0f, rotY = t * 140f)
                }
                PlatformKind.PAINTED_TUNNEL_WALL -> {
                    drawMesh(box, px, centerY, 0f, pw, ph, 1.8f, 0.52f, 0.32f, 0.24f)
                    drawMesh(cylinder, px, topY - ph * 0.45f, 0.92f, pw * 0.65f, ph * 0.7f, 0.2f, 0.08f, 0.08f, 0.12f)
                }
                PlatformKind.RAINBOW_BRIDGE -> {
                    drawMesh(box, px, centerY, 0f, pw, 0.22f, 2.0f, 1.0f, 0.84f, 0.15f, 0.92f)
                }
                else -> {
                    // Earthy 3D cliff/stone body + lush green turf cap
                    drawMesh(box, px, centerY - 0.08f, 0f, pw, ph, 2.4f, 0.54f, 0.36f, 0.22f)
                    drawMesh(box, px, topY - 0.08f, 0f, pw + 0.06f, 0.22f, 2.52f, 0.24f, 0.74f, 0.28f)
                }
            }
        }
    }

    private fun drawCarrots3D(camX: Float, t: Float, frame: DioramaFrameSnapshot) {
        for ((idx, c) in frame.stageData.carrots.withIndex()) {
            if (c.collected) continue
            val cx = c.x * WORLD_SCALE
            if (abs(cx - camX) > 12f) continue
            val cy = (LevelBuilder.GROUND_Y - c.y) * WORLD_SCALE + sin(t * 2f + idx) * 0.08f
            val spin = (t * 95f + idx * 25f) % 360f
            val cr = if (c.isGoldenShard) 1.0f else 1.0f
            val cg = if (c.isGoldenShard) 0.84f else 0.45f
            val cb = if (c.isGoldenShard) 0.0f else 0.05f
            val scale = if (c.isGoldenShard) 1.45f else 1.0f
            // Carrot body + leafy green crown
            drawMesh(cone, cx, cy, 0f, 0.24f * scale, 0.42f * scale, 0.24f * scale, cr, cg, cb, rotX = 180f, rotY = spin)
            drawMesh(cone, cx, cy + 0.26f * scale, 0f, 0.22f * scale, 0.20f * scale, 0.22f * scale, 0.10f, 0.88f, 0.35f, rotY = spin)
        }
    }

    private fun drawPowerUps3D(camX: Float, t: Float, frame: DioramaFrameSnapshot) {
        for (p in frame.stageData.powerUps) {
            if (p.collected) continue
            val px = p.x * WORLD_SCALE
            if (abs(px - camX) > 12f) continue
            val py = (LevelBuilder.GROUND_Y - p.y) * WORLD_SCALE + sin(t * 2.5f) * 0.1f
            drawMesh(box, px, py, 0f, 0.48f, 0.48f, 0.48f, p.type.color.red, p.type.color.green, p.type.color.blue, rotY = t * 70f, rotX = 20f)
        }
    }

    private fun drawEnemies3D(camX: Float, t: Float, frame: DioramaFrameSnapshot) {
        for (enemy in frame.stageData.enemies) {
            val ex = enemy.x * WORLD_SCALE
            if (abs(ex - camX) > 13f) continue
            val ey = (LevelBuilder.GROUND_Y - enemy.y) * WORLD_SCALE

            // Projected ground contact shadow
            drawMesh(cylinder, ex, ey + 0.02f, 0f, 0.72f, 0.03f, 0.72f, 0.05f, 0.05f, 0.08f, 0.45f)

            if (enemy.isFlattenedAccordion) {
                for (layer in 0..2) {
                    drawMesh(cylinder, ex, ey + 0.07f + layer * 0.08f, 0f, 0.78f, 0.07f, 0.65f, 1.0f, 0.45f, 0.0f)
                }
                continue
            }

            if (enemy.kind == EnemyKind.SLY_SCOUT) {
                // 3D Bush disguise + peering eyes
                drawMesh(sphere, ex, ey + 0.34f, 0f, 0.68f, 0.62f, 0.68f, 0.18f, 0.55f, 0.22f)
                drawMesh(sphere, ex - 0.12f, ey + 0.36f, 0.32f, 0.13f, 0.13f, 0.10f, 1f, 1f, 1f)
                drawMesh(sphere, ex + 0.12f, ey + 0.36f, 0.32f, 0.13f, 0.13f, 0.10f, 1f, 1f, 1f)
                drawMesh(sphere, ex - 0.12f, ey + 0.36f, 0.36f, 0.06f, 0.06f, 0.06f, 0.05f, 0.05f, 0.05f)
                drawMesh(sphere, ex + 0.12f, ey + 0.36f, 0.36f, 0.06f, 0.06f, 0.06f, 0.05f, 0.05f, 0.05f)
            } else {
                // 3D Fox Syndicate minion
                drawMesh(cylinder, ex, ey + 0.38f, 0f, 0.46f, 0.56f, 0.42f, 0.88f, 0.32f, 0.05f)
                drawMesh(sphere, ex, ey + 0.78f, 0f, 0.44f, 0.40f, 0.42f, 0.96f, 0.45f, 0.06f)
                drawMesh(cone, ex - 0.14f, ey + 1.06f, 0f, 0.14f, 0.24f, 0.14f, 0.88f, 0.32f, 0.05f)
                drawMesh(cone, ex + 0.14f, ey + 1.06f, 0f, 0.14f, 0.24f, 0.14f, 0.88f, 0.32f, 0.05f)
            }
        }
    }

    private fun drawGoalSafehouse3D(t: Float, frame: DioramaFrameSnapshot) {
        val gx = frame.stageData.goalX * WORLD_SCALE
        drawMesh(box, gx, 0.9f, 0f, 1.8f, 1.8f, 1.6f, 1.0f, 0.84f, 0.15f)
        drawMesh(cone, gx, 2.25f, 0f, 2.2f, 1.1f, 2.0f, 1.0f, 0.45f, 0.05f, rotY = 45f)
        drawMesh(sphere, gx, 3.05f + sin(t * 2f) * 0.1f, 0f, 0.4f, 0.4f, 0.4f, 0.0f, 0.92f, 1.0f)
    }

    /**
     * Renders the articulated 3D Canonical Bob's Bunny character rig:
     * tall athletic proportions, golden-cream & warm-white fur, expressive blue eyes,
     * leather aviator cap with yellow goggles, yellow scarf, orange-and-white outerwear,
     * midnight-navy pants, utility belt, gloves, and white-and-yellow footwear.
     */
    private fun drawBob3D(bx: Float, by: Float, t: Float, frame: DioramaFrameSnapshot) {
        // Ground contact shadow projected beneath Bob
        val shadowY = if (frame.isGrounded) by + 0.02f else 0.02f
        val shadowScale = (1f - (by * 0.18f)).coerceIn(0.45f, 1f)
        drawMesh(cylinder, bx, shadowY, 0f, 0.65f * shadowScale, 0.03f, 0.65f * shadowScale, 0.04f, 0.04f, 0.08f, 0.45f)

        if (frame.invulnerableTimer > 0f && (frame.frameTick / 3) % 2 == 1) return

        // Burrow Blitz 3D mound + peeking golden-cream ears
        if (frame.isBurrowing) {
            drawMesh(sphere, bx, by + 0.16f, 0f, 0.92f, 0.42f, 0.82f, 0.36f, 0.24f, 0.16f)
            drawMesh(cylinder, bx - 0.11f, by + 0.52f, 0f, 0.11f, 0.42f, 0.08f, 0.96f, 0.84f, 0.55f, rotZ = 8f)
            drawMesh(cylinder, bx + 0.11f, by + 0.52f, 0f, 0.11f, 0.42f, 0.08f, 0.96f, 0.84f, 0.55f, rotZ = -8f)
            return
        }

        val dir = if (frame.facingRight) 1f else -1f
        val yaw = if (frame.facingRight) 22f else -22f
        val isRunning = abs(frame.playerVx) > 40f && frame.isGrounded
        val isDashing = frame.trollDashTimer > 0f
        val sx = frame.squashStretchX
        val sy = frame.squashStretchY

        val stride = if (isRunning) sin(t * 7.5f) * 32f else if (!frame.isGrounded) 18f else 0f
        val bobBounce = if (isRunning) abs(cos(t * 7.5f)) * 0.06f else sin(t * 2.4f) * 0.025f
        val lean = if (isRunning || isDashing) -dir * 10f else 0f
        val pancakeSy = if (frame.healthState == CartoonHealthState.PANCAKE) 0.35f else sy

        val baseY = by + bobBounce

        // 1. Midnight-Navy Athletic Pants + White-and-Yellow Footwear
        val legLX = bx - 0.12f
        val legRX = bx + 0.12f
        drawMesh(cylinder, legLX, baseY + 0.36f * pancakeSy, 0.05f, 0.15f * sx, 0.46f * pancakeSy, 0.15f, 0.09f, 0.13f, 0.25f, rotZ = stride * dir)
        drawMesh(cylinder, legRX, baseY + 0.36f * pancakeSy, -0.05f, 0.15f * sx, 0.46f * pancakeSy, 0.15f, 0.09f, 0.13f, 0.25f, rotZ = -stride * dir)

        // White-and-Yellow Sneakers
        drawMesh(box, legLX + dir * 0.04f, baseY + 0.10f * pancakeSy, 0.05f, 0.26f * sx, 0.14f * pancakeSy, 0.18f, 1.0f, 1.0f, 1.0f, rotZ = stride * 0.5f * dir)
        drawMesh(box, legLX + dir * 0.04f, baseY + 0.04f * pancakeSy, 0.05f, 0.27f * sx, 0.05f * pancakeSy, 0.19f, 1.0f, 0.84f, 0.0f)
        drawMesh(box, legRX + dir * 0.04f, baseY + 0.10f * pancakeSy, -0.05f, 0.26f * sx, 0.14f * pancakeSy, 0.18f, 1.0f, 1.0f, 1.0f, rotZ = -stride * 0.5f * dir)
        drawMesh(box, legRX + dir * 0.04f, baseY + 0.04f * pancakeSy, -0.05f, 0.27f * sx, 0.05f * pancakeSy, 0.19f, 1.0f, 0.84f, 0.0f)

        // Cream tail has its own readable silhouette at sprint and idle.
        drawMesh(sphere, bx - dir * 0.32f, baseY + 0.74f * pancakeSy, -0.24f, 0.24f * sx, 0.25f * pancakeSy, 0.24f, 0.98f, 0.94f, 0.78f)

        // 2. Utility Belt + Gold Buckle
        val waistY = baseY + 0.62f * pancakeSy
        drawMesh(cylinder, bx, waistY, 0f, 0.38f * sx, 0.10f * pancakeSy, 0.30f, 0.30f, 0.20f, 0.18f, rotY = yaw)
        drawMesh(box, bx + dir * 0.15f, waistY, 0.11f, 0.10f, 0.08f, 0.06f, 1.0f, 0.84f, 0.31f, rotY = yaw)

        // 3. Athletic Orange-and-White Outerwear Torso
        val torsoY = baseY + 0.88f * pancakeSy
        drawMesh(cylinder, bx, torsoY, 0f, 0.42f * sx, 0.48f * pancakeSy, 0.32f, 1.0f, 0.43f, 0.0f, rotZ = lean, rotY = yaw)
        drawMesh(box, bx + dir * 0.14f, torsoY, 0.10f, 0.16f * sx, 0.40f * pancakeSy, 0.18f, 1.0f, 0.98f, 0.95f, rotZ = lean, rotY = yaw)
        // Jacket zip and hem establish orange/white layering at handheld scale.
        drawMesh(box, bx + dir * 0.12f, torsoY, 0.235f, 0.035f, 0.31f * pancakeSy, 0.03f, 0.24f, 0.18f, 0.17f, rotZ = lean)
        drawMesh(box, bx, torsoY - 0.27f * pancakeSy, 0.13f, 0.66f * sx, 0.065f, 0.27f, 0.97f, 0.97f, 0.94f)

        // 4. Articulated Arms + White Cartoon Gloves
        val armSwing = if (isRunning) sin(t * 7.5f) * 26f else -10f
        drawMesh(cylinder, bx - dir * 0.24f, torsoY - 0.04f, 0.16f, 0.12f, 0.36f * pancakeSy, 0.12f, 1.0f, 0.43f, 0.0f, rotZ = -armSwing)
        drawMesh(sphere, bx - dir * 0.26f, torsoY - 0.22f, 0.18f, 0.16f, 0.16f, 0.16f, 1.0f, 1.0f, 1.0f)
        drawMesh(cylinder, bx + dir * 0.24f, torsoY - 0.04f, -0.16f, 0.12f, 0.36f * pancakeSy, 0.12f, 1.0f, 0.43f, 0.0f, rotZ = armSwing)
        drawMesh(sphere, bx + dir * 0.26f, torsoY - 0.22f, -0.18f, 0.16f, 0.16f, 0.16f, 1.0f, 1.0f, 1.0f)
        // Dark cuffs and tiny glove knuckles communicate articulation.
        drawMesh(cylinder, bx - dir * 0.25f, torsoY - 0.14f, 0.17f, 0.14f, 0.065f, 0.14f, 0.13f, 0.18f, 0.29f)
        drawMesh(cylinder, bx + dir * 0.25f, torsoY - 0.14f, -0.17f, 0.14f, 0.065f, 0.14f, 0.13f, 0.18f, 0.29f)

        // 5. Dynamic Fluttering Yellow Scarf
        val neckY = baseY + 1.13f * pancakeSy
        drawMesh(cylinder, bx, neckY, 0f, 0.34f * sx, 0.11f * pancakeSy, 0.32f, 1.0f, 0.84f, 0.0f, rotY = yaw)
        val scarfWave = sin(t * 4.5f) * 10f
        drawMesh(box, bx - dir * 0.28f, neckY - 0.04f, -0.05f, 0.38f, 0.09f, 0.14f, 1.0f, 0.65f, 0.0f, rotZ = scarfWave + dir * 18f)
        drawMesh(box, bx - dir * 0.48f, neckY - 0.12f + sin(t * 5.2f) * 0.035f, -0.08f, 0.30f, 0.075f, 0.12f, 1.0f, 0.75f, 0.10f, rotZ = scarfWave - dir * 9f)

        // 6. Golden-Cream Head + Warm-White Muzzle + Expressive Blue Eyes
        val headY = baseY + 1.38f * pancakeSy
        drawMesh(sphere, bx, headY, 0f, 0.46f * sx, 0.44f * pancakeSy, 0.44f, 0.96f, 0.84f, 0.55f, rotY = yaw)
        // Warm-white muzzle
        drawMesh(sphere, bx + dir * 0.18f, headY - 0.07f, 0.14f, 0.26f, 0.18f, 0.24f, 1.0f, 0.98f, 0.95f)
        // Two white cheek lobes and a smiling muzzle bring back Bob's expression.
        drawMesh(sphere, bx + dir * 0.13f - 0.09f, headY - 0.14f, 0.37f, 0.16f, 0.12f, 0.12f, 0.98f, 0.97f, 0.93f)
        drawMesh(sphere, bx + dir * 0.13f + 0.09f, headY - 0.14f, 0.37f, 0.16f, 0.12f, 0.12f, 0.98f, 0.97f, 0.93f)
        drawMesh(box, bx + dir * 0.17f, headY - 0.23f, 0.49f, 0.12f, 0.055f, 0.035f, 0.35f, 0.23f, 0.23f)
        drawMesh(box, bx + dir * 0.16f, headY - 0.28f, 0.47f, 0.11f, 0.10f, 0.04f, 1.0f, 1.0f, 0.96f)
        // Pink rabbit nose
        drawMesh(sphere, bx + dir * 0.28f, headY - 0.02f, 0.46f, 0.08f, 0.06f, 0.08f, 1.0f, 0.25f, 0.51f)

        // Expressive Blue Eyes
        val eyeZ = 0.38f
        drawMesh(sphere, bx + dir * 0.10f - 0.08f, headY + 0.04f, eyeZ, 0.11f, 0.14f, 0.08f, 1f, 1f, 1f)
        drawMesh(sphere, bx + dir * 0.10f + 0.08f, headY + 0.04f, eyeZ, 0.11f, 0.14f, 0.08f, 1f, 1f, 1f)
        drawMesh(sphere, bx + dir * 0.12f - 0.08f, headY + 0.04f, eyeZ + 0.03f, 0.065f, 0.085f, 0.05f, 0.12f, 0.53f, 0.90f)
        drawMesh(sphere, bx + dir * 0.12f + 0.08f, headY + 0.04f, eyeZ + 0.03f, 0.065f, 0.085f, 0.05f, 0.12f, 0.53f, 0.90f)
        // Small eye catches read clearly even in landscape play.
        drawMesh(sphere, bx + dir * 0.13f - 0.08f, headY + 0.085f, eyeZ + 0.072f, 0.025f, 0.033f, 0.018f, 1.0f, 1.0f, 1.0f)
        drawMesh(sphere, bx + dir * 0.13f + 0.08f, headY + 0.085f, eyeZ + 0.072f, 0.025f, 0.033f, 0.018f, 1.0f, 1.0f, 1.0f)

        // 7. Leather Aviator Cap + Yellow Goggles
        drawMesh(sphere, bx, headY + 0.10f, 0f, 0.48f * sx, 0.30f * pancakeSy, 0.46f, 0.36f, 0.25f, 0.22f)
        // Goggles have an exposed strap, metallic frames and cool glass centers.
        drawMesh(box, bx, headY + 0.17f, 0.29f, 0.66f, 0.08f, 0.07f, 0.27f, 0.19f, 0.15f)
        for (lens in -1..1 step 2) {
            val lx = bx + dir * 0.12f + lens * 0.08f
            drawMesh(cylinder, lx, headY + 0.17f, 0.37f, 0.13f, 0.065f, 0.13f, 1.0f, 0.84f, 0.0f, rotX = 90f)
            drawMesh(sphere, lx, headY + 0.17f, 0.455f, 0.083f, 0.07f, 0.022f, 0.34f, 0.77f, 0.90f)
        }

        // 8. Expressive Tall Ears (Helicopter Rotor Spin or Dynamic Tilt)
        if (frame.isHelicopterGliding || frame.isShowingDontLookDown) {
            val rotorYaw = (frame.earSpinAngle) % 360f
            drawMesh(box, bx, headY + 0.36f, 0f, 0.88f, 0.09f, 0.16f, 0.96f, 0.84f, 0.55f, rotY = rotorYaw)
        } else {
            val earTilt = if (isRunning) dir * 24f else sin(t * 2f) * 5f
            drawMesh(cylinder, bx - 0.11f, headY + 0.48f, -0.02f, 0.11f, 0.54f, 0.09f, 0.96f, 0.84f, 0.55f, rotZ = earTilt + 6f)
            drawMesh(cylinder, bx + 0.11f, headY + 0.48f, 0.02f, 0.11f, 0.54f, 0.09f, 0.96f, 0.84f, 0.55f, rotZ = earTilt - 6f)
            // Blush-pink ear interiors follow the same animation angle.
            drawMesh(cylinder, bx - 0.11f, headY + 0.48f, 0.082f, 0.058f, 0.43f, 0.023f, 0.94f, 0.55f, 0.58f, rotZ = earTilt + 6f)
            drawMesh(cylinder, bx + 0.11f, headY + 0.48f, 0.122f, 0.058f, 0.43f, 0.023f, 0.94f, 0.55f, 0.58f, rotZ = earTilt - 6f)
        }
    }

    private fun drawParticles3D(frame: DioramaFrameSnapshot) {
        for (p in frame.particles) {
            val px = p.x * WORLD_SCALE
            val py = (LevelBuilder.GROUND_Y - p.y) * WORLD_SCALE
            drawMesh(sphere, px, py, 0.15f, 0.14f, 0.14f, 0.14f, p.color.red, p.color.green, p.color.blue, p.alpha.coerceIn(0f, 1f))
        }
    }

    private fun drawForegroundFraming(camX: Float) {
        for (i in 0..8) {
            val fx = i * 7.5f + 2f
            if (abs(fx - camX) > 13f) continue
            drawMesh(sphere, fx, -0.35f, 3.4f, 1.3f, 0.8f, 1.0f, 0.10f, 0.38f, 0.16f)
        }
    }
}
