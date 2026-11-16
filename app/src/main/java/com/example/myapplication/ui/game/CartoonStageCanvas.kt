package com.example.myapplication.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.game.CartoonStageEngine
import com.example.myapplication.game.LevelBuilder
import com.example.myapplication.model.BiomeTheme
import com.example.myapplication.model.CartoonHealthState
import com.example.myapplication.model.EnemyEntity
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.PlatformEntity
import com.example.myapplication.model.PlatformKind
import com.example.myapplication.model.PlayableHero
import com.example.myapplication.model.PowerUpType
import com.example.myapplication.model.SwagItem
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * CartoonStageCanvas - Master Saturday-Morning Cartoon Art & Ambiance Rendering Engine
 * Features:
 * - Multi-stop atmospheric sky grading with horizon haze & volumetric light shafts
 * - Authentic Ben-Day / comic halftone dot stippling and dynamic radial sunburst
 * - Smiling cartoon sun with shades or cratered cosmic moon with orbital rings
 * - 4-layer parallax cartoon cityscape with lit art-deco windows, rooftop props & foreground canopy
 * - Cel-shaded platforms with lush scalloped grass, strata layers, and comic ink hatching
 * - Dynamic ground contact shadows beneath characters and enemies for 2.5D visual grounding
 * - Handcrafted high-detail graphics for all 6 playable heroes with specular highlights,
 *   treaded kicks, accessories, and distinct facial traits
 * - Slapstick animations (pancake accordion with circling canaries, coyote panic saucer eyes)
 * - Animated boss set-pieces (Don Foxone's steamroller, Great Furtini top hat, Omega Animator pencil)
 * - Dropped-shadow comic starburst action bubbles ("POW!", "WHAM!", "BOING!", "KAPOW!")
 */
@Composable
fun CartoonStageCanvas(
    engine: CartoonStageEngine,
    headgear: SwagItem,
    hoodie: SwagItem,
    kicks: SwagItem,
    modifier: Modifier = Modifier
) {
    val tick = engine.frameTick
    val biome = engine.stageConfig.biome

    Canvas(modifier = modifier.fillMaxSize()) {
        // WIDE-ANGLE PANORAMIC CARTOON CAMERA FRAMING:
        // Fits at least 1150 world units in landscape (or 900 in portrait),
        // preventing the camera from being glued too close to the bunny and opening up the road ahead!
        val targetWorldW = if (size.width > size.height) 1200f else 900f
        val targetWorldH = 640f
        val scale = (size.width / targetWorldW).coerceAtMost(size.height / targetWorldH)
        val scaleX = scale
        val scaleY = scale
        val visibleWorldW = size.width / scale
        val visibleWorldH = size.height / scale
        engine.viewportWorldWidth = visibleWorldW

        // Vertical alignment: ground at ~72% of visible screen height
        val worldOffsetY = (visibleWorldH * 0.72f - LevelBuilder.GROUND_Y).coerceAtLeast(0f)
        val camX = engine.cameraX

        // Hardened Screen Shake with dampened sine/cosine vibration
        val shakeX = if (engine.screenShakeTimer > 0f) {
            sin(tick * 2.1f) * engine.screenShakeIntensity * (engine.screenShakeTimer / 0.35f).coerceIn(0f, 1f)
        } else 0f
        val shakeY = if (engine.screenShakeTimer > 0f) {
            cos(tick * 2.7f) * engine.screenShakeIntensity * (engine.screenShakeTimer / 0.35f).coerceIn(0f, 1f)
        } else 0f

        // =========================================================================
        // 1. ATMOSPHERIC SKY & COLOR AMBIANCE (MULTI-STOP RAMP + HALTONE + SUNBURST)
        // =========================================================================

        // Rich 3-stop vertical celestial sky gradient
        val skyMid = Color(
            red = ((biome.skyTop.red + biome.skyBottom.red) * 0.5f).coerceIn(0f, 1f),
            green = ((biome.skyTop.green + biome.skyBottom.green) * 0.5f).coerceIn(0f, 1f),
            blue = ((biome.skyTop.blue + biome.skyBottom.blue) * 0.5f).coerceIn(0f, 1f)
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(biome.skyTop, skyMid, biome.skyBottom)
            ),
            size = size
        )

        // Comic Halftone / Ben-Day Stippling Overlay (Vintage comic book texture)
        val isCosmicOrNight = biome.worldNumber in listOf(3, 9, 10, 11) || biome == BiomeTheme.NEON_ROOFTOPS
        drawComicHalftoneAmbiance(size.width, size.height * 0.45f, isCosmicOrNight)

        // Volumetric Sunbeams or Cosmic Aurora Ribbon
        if (isCosmicOrNight) {
            drawCosmicAuroraRibbon(size.width, size.height, tick)
        } else {
            drawVolumetricSunbeams(size.width * 0.80f, size.height * 0.16f, size.width, size.height, tick)
        }

        // Celestial Body: Smiling Cartoon Sun with Shades or Cosmic Moon with Craters
        val sunX = size.width * 0.82f
        val sunY = size.height * 0.18f
        if (isCosmicOrNight) {
            drawCosmicMoonWithCraters(sunX, sunY, scaleX, scaleY, tick)
        } else {
            drawSmilingCartoonSunWithShades(sunX, sunY, scaleX, scaleY, tick)
        }

        // Drifting Puffy Cartoon Clouds with layered ink outlines & cel highlights
        val cloud1X = ((tick * 0.35f) % (size.width + 360f)) - 180f
        val cloud2X = (((tick * 0.22f) + 480f) % (size.width + 360f)) - 180f
        val cloud3X = (((tick * 0.18f) + 220f) % (size.width + 360f)) - 180f
        drawStylizedCartoonCloud(cloud1X, size.height * 0.11f, 1.15f * scaleX)
        drawStylizedCartoonCloud(cloud2X, size.height * 0.22f, 0.88f * scaleX)
        drawStylizedCartoonCloud(cloud3X, size.height * 0.06f, 0.72f * scaleX)

        // Whimsical Cartoon Sky Props: Giant Carrot Hot-Air Balloon & Flying V-Flock of Birds
        drawWhimsicalSkyElements(size.width, size.height, tick, scaleX, scaleY, isCosmicOrNight)

        // =========================================================================
        // 2. PARALLAX CARTOON SCENERY (3 DEPTH LAYERS)
        // =========================================================================

        // Layer A: Far Distant Hills / Giant Carrot Monolith Silhouettes (0.10x parallax)
        drawDistantHillsLayer(camX * 0.10f, size.width, size.height, biome, scaleX, scaleY)

        // Layer B: Mid-ground Cartoon City Skyline with Lit Windows & Neon Signs (0.24x parallax)
        drawMidgroundSkyline(camX * 0.24f, size.width, size.height, biome, scaleX, scaleY, tick)

        // Ambient Horizon Haze Band (Soft warm/cool depth blend)
        val hazeColor = if (isCosmicOrNight) Color(0x334A148C) else Color(0x33FFE082)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, hazeColor, Color.Transparent),
                startY = size.height * 0.42f,
                endY = size.height * 0.68f
            ),
            topLeft = Offset(0f, size.height * 0.42f),
            size = Size(size.width, size.height * 0.26f)
        )

        // =========================================================================
        // 3. WORLD-SPACE STAGE (SCREEN SHAKE, PLATFORMS, HERO, ENEMIES, COLLECTIBLES)
        // =========================================================================
        withTransform({
            scale(scaleX, scaleY, Offset.Zero)
            translate(left = -camX + shakeX, top = worldOffsetY + shakeY)
        }) {
            // Speed Lines when moving fast
            if (abs(engine.playerVx) > 420f || engine.trollDashTimer > 0f) {
                drawComicSpeedLines(engine.playerX, engine.playerY, engine.facingRight, tick)
            }

            // Platforms with cel-shading, textures & comic ink outlines
            for (plat in engine.stageData.platforms) {
                drawComicPlatformDetailed(plat, biome, engine, tick)
            }

            // Road-Ahead Directional Signposts along the stage
            drawRoadAheadSignposts(engine.stageData.goalX, LevelBuilder.GROUND_Y, tick)

            // Ground Contact Shadows beneath hero and enemies (2.5D visual grounding!)
            drawGroundContactShadow(engine.playerX, engine.playerY, LevelBuilder.GROUND_Y, engine.isGrounded)
            for (enemy in engine.stageData.enemies) {
                drawGroundContactShadow(enemy.x, enemy.y, LevelBuilder.GROUND_Y, true)
            }

            // World 6 Rising Molten Salsa Wave with bubbles
            if (biome.hasRisingSalsa) {
                drawMoltenSalsaWaveDetailed(engine.moltenSalsaY, camX, tick)
            }

            // Goal Golden Safehouse Burrow
            drawGoalGoldenSafehouse(engine.stageData.goalX, LevelBuilder.GROUND_Y, tick)

            // Collectible Carrots & Golden Shards (Rich foliage, 3D body ridges, golden halos)
            val bounceY = sin(tick * 0.14f) * 6f
            for (carrot in engine.stageData.carrots) {
                if (!carrot.collected) {
                    drawJuicyCarrotDetailed(carrot.x, carrot.y + bounceY, carrot.isGoldenShard, tick)
                }
            }

            // Power-Up Crates & Pickups (Glowing cartoon crates with neon rims)
            for (pickup in engine.stageData.powerUps) {
                if (!pickup.collected) {
                    drawPowerUpBoxDetailed(pickup.x, pickup.y + bounceY, pickup.type, tick)
                }
            }

            // Dropped Shades with slapstick bounce
            if (engine.droppedShades.active) {
                drawDroppedCartoonShades(engine.droppedShades.x, engine.droppedShades.y)
            }

            // Enemies & Bosses with Saturday-morning cartoon flair
            for (enemy in engine.stageData.enemies) {
                drawEnemyCartoonDetailed(enemy, tick)
            }

            // Multiverse Holo-Ghost Speedrun Rival
            if (!engine.isStageCleared) {
                drawHoloGhostBunny(engine.ghostRivalX, engine.ghostRivalY, tick)
            }

            // P2 Co-Op Sidekick Drone (flying cartoon pie-copter)
            if (engine.sidekickDroneEnabled) {
                drawSidekickDrone(engine.sidekickDroneX, engine.sidekickDroneY, tick)
            }

            // Cartoon Particles & Dust Clods with ink rims
            for (p in engine.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
                drawCircle(
                    color = Color.Black.copy(alpha = (p.alpha * 0.65f).coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x, p.y),
                    style = Stroke(width = 1.8f)
                )
            }

            // Active Hero (Handcrafted artwork for all 6 heroes)
            drawHeroCharacterDetailed(engine, headgear, hoodie, kicks, tick)

            // Comic Starburst Action Bubbles ("POW!", "WHAM!", "BOING!", "KAPOW!")
            for (item in engine.floatingTexts) {
                drawComicActionBubbleDetailed(item.text, item.x, item.y, item.color, item.lifeRemaining)
            }
        }

        // =========================================================================
        // 4. FOREGROUND AMBIANCE, CORNER CANOPY & CINEMATIC VIGNETTE
        // =========================================================================

        // Layer D: Foreground Canopy (Vines, city banners or cosmic dust)
        drawForegroundCanopyLayer(size.width, size.height, biome, scaleX, scaleY, tick)

        // Biome-specific ambient color grading wash
        val ambientWash = when (biome.worldNumber) {
            10 -> Color(0x18FFD700) // Golden Kingdom warm amber
            3 -> Color(0x1400E5FF)  // Toky-Oat cyber cyan
            6 -> Color(0x18FF1744)  // Vesuvi-Apple spicy crimson
            11 -> Color(0x18E040FB) // Looney Multiverse cosmic magenta
            else -> Color(0x0A000000)
        }
        drawRect(color = ambientWash, size = size)

        // Soft comic corner vignette for cinematic focus
        drawComicCornerVignette(size.width, size.height)
    }
}

// =========================================================================
// ATMOSPHERE, CELESTIAL & SKYLINE RENDERING HELPERS
// =========================================================================

/** Subtle vintage Ben-Day / comic halftone dot matrix across the upper sky */
private fun DrawScope.drawComicHalftoneAmbiance(width: Float, maxHeight: Float, isNight: Boolean) {
    val dotColor = if (isNight) Color(0x1A80DEEA) else Color(0x18FFFFFF)
    val spacing = 28f
    var y = 14f
    while (y < maxHeight) {
        val rowOffset = if (((y / spacing).toInt() % 2) == 0) 0f else spacing * 0.5f
        var x = rowOffset
        while (x < width) {
            val fade = (1f - (y / maxHeight)).coerceIn(0.1f, 1f)
            drawCircle(
                color = dotColor.copy(alpha = (dotColor.alpha * fade)),
                radius = 2.4f * fade,
                center = Offset(x, y)
            )
            x += spacing
        }
        y += spacing
    }
}

/** Volumetric cartoon light shafts slanting down from the sun */
private fun DrawScope.drawVolumetricSunbeams(sunX: Float, sunY: Float, width: Float, height: Float, tick: Int) {
    val beamCount = 4
    for (i in 0 until beamCount) {
        val angle = 38f + i * 18f + sin((tick + i * 20) * 0.02f) * 4f
        val rad = Math.toRadians(angle.toDouble())
        val beamWidth = 45f + i * 12f
        val path = Path().apply {
            moveTo(sunX, sunY)
            lineTo(sunX + cos(rad).toFloat() * 900f - beamWidth, sunY + sin(rad).toFloat() * 900f)
            lineTo(sunX + cos(rad).toFloat() * 900f + beamWidth, sunY + sin(rad).toFloat() * 900f)
            close()
        }
        val beamAlpha = (0.05f + sin((tick + i * 35) * 0.03f) * 0.025f).coerceIn(0.02f, 0.08f)
        drawPath(path, color = Color(0xFFFFF9C4).copy(alpha = beamAlpha))
    }
}

/** Cosmic aurora wave ribbons for night and cosmic worlds */
private fun DrawScope.drawCosmicAuroraRibbon(width: Float, height: Float, tick: Int) {
    val ribbonY = height * 0.18f
    val path = Path().apply {
        moveTo(0f, ribbonY)
        var x = 0f
        while (x <= width) {
            val wave = sin((x * 0.006f + tick * 0.03f).toDouble()).toFloat() * 28f +
                       cos((x * 0.012f - tick * 0.02f).toDouble()).toFloat() * 16f
            lineTo(x, ribbonY + wave)
            x += 24f
        }
        lineTo(width, 0f)
        lineTo(0f, 0f)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0x3300E5FF), Color(0x22E040FB), Color.Transparent),
            startY = 0f,
            endY = height * 0.32f
        )
    )

    // Twinkling Starlight Field
    for (s in 0..18) {
        val sx = (s * 87f + tick * 0.15f) % width
        val sy = 20f + ((s * 43) % (height * 0.35f).toInt())
        val twinkle = (sin((tick + s * 17) * 0.12f) * 0.5f + 0.5f).coerceIn(0.2f, 1f)
        drawCircle(color = Color.White.copy(alpha = twinkle * 0.85f), radius = 2f * twinkle, center = Offset(sx, sy))
        if (s % 4 == 0) {
            // 4-point sparkle cross
            drawLine(
                color = Color(0xFFFFD700).copy(alpha = twinkle * 0.9f),
                start = Offset(sx - 4f, sy),
                end = Offset(sx + 4f, sy),
                strokeWidth = 1.2f
            )
            drawLine(
                color = Color(0xFFFFD700).copy(alpha = twinkle * 0.9f),
                start = Offset(sx, sy - 4f),
                end = Offset(sx, sy + 4f),
                strokeWidth = 1.2f
            )
        }
    }
}

/** Saturday Morning Cartoon Sun wearing cool shades with radiating comic beams */
private fun DrawScope.drawSmilingCartoonSunWithShades(
    sunX: Float,
    sunY: Float,
    scaleX: Float,
    scaleY: Float,
    tick: Int
) {
    val outline = Color(0xFF1A1A1A)

    // Outer warm corona glow
    drawCircle(color = Color(0x44FFD54F), radius = 58f * scaleY, center = Offset(sunX, sunY))

    // 10 Radiating Cartoon Beams with bold comic outline
    for (ray in 0 until 10) {
        val angle = (ray * 36f + tick * 0.5f).toDouble()
        val rad = Math.toRadians(angle)
        val rx1 = sunX + cos(rad).toFloat() * 46f * scaleX
        val ry1 = sunY + sin(rad).toFloat() * 46f * scaleY
        val rx2 = sunX + cos(rad).toFloat() * 70f * scaleX
        val ry2 = sunY + sin(rad).toFloat() * 70f * scaleY

        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(rx1, ry1),
            end = Offset(rx2, ry2),
            strokeWidth = 7f * scaleY,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outline,
            start = Offset(rx1, ry1),
            end = Offset(rx2, ry2),
            strokeWidth = 2.2f * scaleY,
            cap = StrokeCap.Round
        )
    }

    // Sun Face disc with gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF59D), Color(0xFFFFEA00), Color(0xFFFFB300)),
            center = Offset(sunX - 10f * scaleX, sunY - 10f * scaleY),
            radius = 42f * scaleY
        ),
        radius = 42f * scaleY,
        center = Offset(sunX, sunY)
    )
    drawCircle(color = outline, radius = 42f * scaleY, center = Offset(sunX, sunY), style = Stroke(width = 3.8f))

    // Sun's Smug Cartoon Sunglasses
    val shadeTop = sunY - 12f * scaleY
    drawRoundRect(
        color = Color(0xFF111111),
        topLeft = Offset(sunX - 26f * scaleX, shadeTop),
        size = Size(52f * scaleX, 18f * scaleY),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(sunX - 26f * scaleX, shadeTop),
        size = Size(52f * scaleX, 18f * scaleY),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 2.5f)
    )
    // Comic lens gloss white glint
    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(sunX - 20f * scaleX, shadeTop + 2f * scaleY),
        size = Size(16f * scaleX, 6f * scaleY),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // Cheek Rosy Blushes
    drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 6f * scaleY, center = Offset(sunX - 22f * scaleX, sunY + 14f * scaleY))
    drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 6f * scaleY, center = Offset(sunX + 22f * scaleX, sunY + 14f * scaleY))

    // Smug Cartoon Smirk
    val smilePath = Path().apply {
        moveTo(sunX - 12f * scaleX, sunY + 18f * scaleY)
        quadraticTo(sunX, sunY + 26f * scaleY, sunX + 14f * scaleX, sunY + 16f * scaleY)
    }
    drawPath(smilePath, color = outline, style = Stroke(width = 3f, cap = StrokeCap.Round))
}

/** Cosmic Moon with craters and orbiting mini starlight satellite */
private fun DrawScope.drawCosmicMoonWithCraters(
    sunX: Float,
    sunY: Float,
    scaleX: Float,
    scaleY: Float,
    tick: Int
) {
    val outline = Color(0xFF1A1A1A)

    // Starlight aura halo
    drawCircle(color = Color(0x3300E5FF), radius = 64f * scaleY, center = Offset(sunX, sunY))
    drawCircle(color = Color(0x33FFD700), radius = 54f * scaleY, center = Offset(sunX, sunY))

    // Moon disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFF9C4), Color(0xFFE0E0E0)),
            center = Offset(sunX - 12f * scaleX, sunY - 12f * scaleY),
            radius = 44f * scaleY
        ),
        radius = 44f * scaleY,
        center = Offset(sunX, sunY)
    )
    drawCircle(color = outline, radius = 44f * scaleY, center = Offset(sunX, sunY), style = Stroke(width = 3.8f))

    // Comic craters with shadows
    drawCircle(color = Color(0xFFBDBDBD), radius = 9f * scaleY, center = Offset(sunX - 14f * scaleX, sunY - 12f * scaleY))
    drawCircle(color = outline, radius = 9f * scaleY, center = Offset(sunX - 14f * scaleX, sunY - 12f * scaleY), style = Stroke(width = 2f))

    drawCircle(color = Color(0xFFBDBDBD), radius = 13f * scaleY, center = Offset(sunX + 16f * scaleX, sunY + 10f * scaleY))
    drawCircle(color = outline, radius = 13f * scaleY, center = Offset(sunX + 16f * scaleX, sunY + 10f * scaleY), style = Stroke(width = 2f))

    drawCircle(color = Color(0xFFBDBDBD), radius = 6f * scaleY, center = Offset(sunX - 6f * scaleX, sunY + 22f * scaleY))

    // Orbiting mini starlight satellite
    val satAngle = Math.toRadians((tick * 1.8f).toDouble())
    val satX = sunX + cos(satAngle).toFloat() * 64f * scaleX
    val satY = sunY + sin(satAngle).toFloat() * 28f * scaleY
    drawCircle(color = Color(0xFFFFD700), radius = 5f * scaleY, center = Offset(satX, satY))
    drawCircle(color = outline, radius = 5f * scaleY, center = Offset(satX, satY), style = Stroke(width = 1.8f))
}

/** Puffy Saturday-morning cartoon cloud with overlapping spheres & ink contours */
private fun DrawScope.drawStylizedCartoonCloud(x: Float, y: Float, scale: Float) {
    val cloudColor = Color(0xF5FFFFFF)
    val cloudShade = Color(0xFFECEFF1)
    val outlineColor = Color(0xFF263238)
    val r = 26f * scale

    // Shaded underside
    drawRoundRect(
        color = cloudShade,
        topLeft = Offset(x - r * 0.4f, y + 4f),
        size = Size(r * 3.6f, r * 1.3f),
        cornerRadius = CornerRadius(r, r)
    )

    // Overlapping puffy circles
    drawCircle(color = cloudColor, radius = r, center = Offset(x, y))
    drawCircle(color = cloudColor, radius = r * 1.38f, center = Offset(x + 26f * scale, y - 9f * scale))
    drawCircle(color = cloudColor, radius = r * 0.98f, center = Offset(x + 54f * scale, y))
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(x - r * 0.4f, y - r * 0.35f),
        size = Size(r * 3.5f, r * 1.4f),
        cornerRadius = CornerRadius(r, r)
    )

    // White gloss highlight bubble
    drawCircle(color = Color.White, radius = r * 0.42f, center = Offset(x + 24f * scale, y - 18f * scale))

    // Comic ink strokes
    drawCircle(color = outlineColor, radius = r, center = Offset(x, y), style = Stroke(width = 3.2f))
    drawCircle(color = outlineColor, radius = r * 1.38f, center = Offset(x + 26f * scale, y - 9f * scale), style = Stroke(width = 3.2f))
    drawCircle(color = outlineColor, radius = r * 0.98f, center = Offset(x + 54f * scale, y), style = Stroke(width = 3.2f))
}

/** Whimsical sky animations: Giant Carrot Hot-Air Balloon, V-flock of cartoon birds, or Cosmic UFO */
private fun DrawScope.drawWhimsicalSkyElements(
    width: Float,
    height: Float,
    tick: Int,
    scaleX: Float,
    scaleY: Float,
    isCosmicOrNight: Boolean
) {
    val outline = Color(0xFF111111)

    if (isCosmicOrNight) {
        // Retro Cartoon Flying Saucer UFO
        val ufoPathX = ((tick * 0.45f) % (width + 240f)) - 120f
        val ufoY = height * 0.14f + sin(tick * 0.05f) * 12f

        // Glowing tractor beam cone
        val beamPath = Path().apply {
            moveTo(ufoPathX, ufoY + 6f)
            lineTo(ufoPathX - 35f, ufoY + 80f)
            lineTo(ufoPathX + 35f, ufoY + 80f)
            close()
        }
        drawPath(
            beamPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x6600E5FF), Color.Transparent),
                startY = ufoY + 6f,
                endY = ufoY + 80f
            )
        )

        // Glass Cockpit Dome with glowing alien silhouette
        drawCircle(color = Color(0xDD00E5FF), radius = 14f, center = Offset(ufoPathX, ufoY - 6f))
        drawCircle(color = outline, radius = 14f, center = Offset(ufoPathX, ufoY - 6f), style = Stroke(width = 2.4f))
        drawCircle(color = Color(0xFF00E676), radius = 5f, center = Offset(ufoPathX, ufoY - 7f))

        // Metallic Saucer Hull
        drawOval(
            brush = Brush.verticalGradient(listOf(Color(0xFFBA68C8), Color(0xFF4A148C)), startY = ufoY - 6f, endY = ufoY + 12f),
            topLeft = Offset(ufoPathX - 32f, ufoY - 6f),
            size = Size(64f, 18f)
        )
        drawOval(color = outline, topLeft = Offset(ufoPathX - 32f, ufoY - 6f), size = Size(64f, 18f), style = Stroke(width = 2.8f))

        // Blinking rim lights
        for (i in 0..4) {
            val lx = ufoPathX - 22f + i * 11f
            val lightColor = if ((tick / 15 + i) % 2 == 0) Color(0xFFFFEA00) else Color(0xFFFF1744)
            drawCircle(color = lightColor, radius = 2.5f, center = Offset(lx, ufoY + 3f))
        }
    } else {
        // 1. Drifting Giant Carrot Hot-Air Balloon
        val balloonX = ((tick * 0.16f + 200f) % (width + 300f)) - 150f
        val balloonY = height * 0.13f + sin(tick * 0.025f) * 10f

        // Giant Carrot Balloon Envelope
        val carrotPath = Path().apply {
            moveTo(balloonX - 26f, balloonY - 20f)
            cubicTo(balloonX - 32f, balloonY - 50f, balloonX + 32f, balloonY - 50f, balloonX + 26f, balloonY - 20f)
            lineTo(balloonX + 10f, balloonY + 18f)
            lineTo(balloonX - 10f, balloonY + 18f)
            close()
        }
        drawPath(
            carrotPath,
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFFFB74D), Color(0xFFFF6D00), Color(0xFFE65100)),
                startX = balloonX - 26f,
                endX = balloonX + 26f
            )
        )
        drawPath(carrotPath, color = outline, style = Stroke(width = 3.2f))

        // Yellow horizontal decorative stripe
        drawRoundRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(balloonX - 22f, balloonY - 14f),
            size = Size(44f, 8f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Green leafy carrot tops
        drawCircle(color = Color(0xFF00E676), radius = 9f, center = Offset(balloonX, balloonY - 52f))
        drawCircle(color = Color(0xFF00C853), radius = 7f, center = Offset(balloonX - 10f, balloonY - 48f))
        drawCircle(color = Color(0xFF00C853), radius = 7f, center = Offset(balloonX + 10f, balloonY - 48f))
        drawCircle(color = outline, radius = 9f, center = Offset(balloonX, balloonY - 52f), style = Stroke(width = 2.4f))

        // Suspension Ropes
        drawLine(color = outline, start = Offset(balloonX - 8f, balloonY + 18f), end = Offset(balloonX - 7f, balloonY + 30f), strokeWidth = 1.6f)
        drawLine(color = outline, start = Offset(balloonX + 8f, balloonY + 18f), end = Offset(balloonX + 7f, balloonY + 30f), strokeWidth = 1.6f)

        // Little Wicker Basket
        drawRoundRect(
            color = Color(0xFF8D6E63),
            topLeft = Offset(balloonX - 9f, balloonY + 30f),
            size = Size(18f, 14f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(balloonX - 9f, balloonY + 30f),
            size = Size(18f, 14f),
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(width = 2.2f)
        )

        // Fluttering little red pennant flag
        val flagPath = Path().apply {
            moveTo(balloonX + 9f, balloonY + 32f)
            lineTo(balloonX + 22f, balloonY + 35f + sin(tick * 0.2f) * 2f)
            lineTo(balloonX + 9f, balloonY + 40f)
            close()
        }
        drawPath(flagPath, color = Color(0xFFFF1744))

        // 2. Silhouetted Cartoon Birds flying in V-Formation
        val flockBaseX = ((tick * 0.42f) % (width + 360f)) - 120f
        val flockBaseY = height * 0.08f + sin(tick * 0.03f) * 8f
        val wingFlap = sin(tick * 0.25f) * 4f

        val birdOffsets = listOf(
            Offset(0f, 0f),
            Offset(-18f, 12f),
            Offset(-36f, 24f),
            Offset(-22f, -10f),
            Offset(-44f, -20f)
        )
        for (bo in birdOffsets) {
            val bx = flockBaseX + bo.x
            val by = flockBaseY + bo.y
            val birdPath = Path().apply {
                moveTo(bx - 8f, by - wingFlap)
                quadraticBezierTo(bx - 4f, by + 2f, bx, by)
                quadraticBezierTo(bx + 4f, by + 2f, bx + 8f, by - wingFlap)
            }
            drawPath(birdPath, color = Color(0xFF263238), style = Stroke(width = 2.6f, cap = StrokeCap.Round))
        }
    }
}

/** Layer A: Distant Hills & Giant Carrot Silhouettes (0.10x parallax) */
private fun DrawScope.drawDistantHillsLayer(
    parallaxX: Float,
    width: Float,
    height: Float,
    biome: BiomeTheme,
    scaleX: Float,
    scaleY: Float
) {
    val hillColor = biome.groundColor.copy(alpha = 0.28f)
    val hillOutline = Color(0xFF1A1A1A).copy(alpha = 0.35f)
    val baseY = height * 0.65f

    val shift = -parallaxX % 600f
    for (i in -1..5) {
        val hx = i * 600f + shift
        val path = Path().apply {
            moveTo(hx, baseY)
            cubicTo(hx + 150f, baseY - 90f * scaleY, hx + 350f, baseY - 120f * scaleY, hx + 600f, baseY)
            lineTo(hx + 600f, height)
            lineTo(hx, height)
            close()
        }
        drawPath(path, color = hillColor)
        drawPath(path, color = hillOutline, style = Stroke(width = 2.5f))

        // Giant Carrot Monument Silhouette on every 2nd hill
        if (i % 2 == 0) {
            val cx = hx + 280f
            val cy = baseY - 110f * scaleY
            val carrotMonument = Path().apply {
                moveTo(cx - 18f * scaleX, cy - 40f * scaleY)
                lineTo(cx + 18f * scaleX, cy - 40f * scaleY)
                lineTo(cx, cy + 30f * scaleY)
                close()
            }
            drawPath(carrotMonument, color = biome.accentColor.copy(alpha = 0.32f))
        }
    }
}

/** Layer B: Midground Cartoon Skyline with Lit Windows & Neon Signs (0.24x parallax) */
private fun DrawScope.drawMidgroundSkyline(
    parallaxX: Float,
    width: Float,
    height: Float,
    biome: BiomeTheme,
    scaleX: Float,
    scaleY: Float,
    tick: Int
) {
    val outline = Color(0xFF1A1A1A).copy(alpha = 0.65f)
    val shift = -parallaxX % 460f

    for (i in -1..6) {
        val bx = i * 460f + shift
        val bHeight = (230f + (abs(i * 59) % 150)) * scaleY
        val bTop = (LevelBuilder.GROUND_Y * scaleY) - bHeight
        val bWidth = 190f * scaleX

        // Skyscraper body with strata shading
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    biome.groundColor.copy(alpha = 0.55f),
                    biome.groundColor.copy(alpha = 0.75f)
                ),
                startY = bTop,
                endY = bTop + bHeight
            ),
            topLeft = Offset(bx, bTop),
            size = Size(bWidth, bHeight),
            cornerRadius = CornerRadius(16f, 16f)
        )
        // Comic ink outline
        drawRoundRect(
            color = outline,
            topLeft = Offset(bx, bTop),
            size = Size(bWidth, bHeight),
            cornerRadius = CornerRadius(16f, 16f),
            style = Stroke(width = 3.6f)
        )

        // Art-Deco Spire / Water Tower on roof
        if (i % 2 == 0) {
            // Rooftop Water Tower
            val twX = bx + 25f * scaleX
            val twY = bTop - 34f * scaleY
            drawRoundRect(
                color = Color(0xFF6D4C41).copy(alpha = 0.8f),
                topLeft = Offset(twX, twY),
                size = Size(36f * scaleX, 34f * scaleY),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(twX, twY),
                size = Size(36f * scaleX, 34f * scaleY),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2.2f)
            )
        } else {
            // Radio Antenna with blinking red aviation light
            val antX = bx + bWidth * 0.75f
            drawLine(
                color = outline,
                start = Offset(antX, bTop),
                end = Offset(antX, bTop - 45f * scaleY),
                strokeWidth = 3f
            )
            val blink = (tick / 20) % 2 == 0
            if (blink) {
                drawCircle(color = Color(0xFFFF1744), radius = 4f * scaleY, center = Offset(antX, bTop - 45f * scaleY))
            }
        }

        // Neon Billboard Sign on top of building ("ACME", "BOBS", "CARROT", "FOX")
        val signColor = when (abs(i) % 4) {
            0 -> Color(0xFFFF9100)
            1 -> Color(0xFF00E5FF)
            2 -> Color(0xFFFF1744)
            else -> Color(0xFFFFD700)
        }
        val signTop = bTop + 12f * scaleY
        drawRoundRect(
            color = Color(0xFF111111).copy(alpha = 0.85f),
            topLeft = Offset(bx + 18f * scaleX, signTop),
            size = Size(bWidth - 36f * scaleX, 22f * scaleY),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = signColor,
            topLeft = Offset(bx + 18f * scaleX, signTop),
            size = Size(bWidth - 36f * scaleX, 22f * scaleY),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2.5f)
        )

        // Grid of Lit Windows with cartoon warmth
        for (row in 0..4) {
            for (col in 0..2) {
                val wx = bx + 24f * scaleX + col * 50f * scaleX
                val wy = bTop + 48f * scaleY + row * 40f * scaleY
                if (wy < (LevelBuilder.GROUND_Y * scaleY) - 20f) {
                    val isLit = (i * 7 + row * 3 + col) % 3 != 0
                    val winColor = if (isLit) {
                        if ((row + col) % 2 == 0) Color(0xFFFFEA00).copy(alpha = 0.75f) else Color(0xFF00E5FF).copy(alpha = 0.65f)
                    } else {
                        Color(0xFF212121).copy(alpha = 0.45f)
                    }
                    drawRoundRect(
                        color = winColor,
                        topLeft = Offset(wx, wy),
                        size = Size(28f * scaleX, 20f * scaleY),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    drawRoundRect(
                        color = outline,
                        topLeft = Offset(wx, wy),
                        size = Size(28f * scaleX, 20f * scaleY),
                        cornerRadius = CornerRadius(5f, 5f),
                        style = Stroke(width = 1.8f)
                    )
                }
            }
        }
    }
}

/** Layer D: Foreground Canopy Layer (Jungle vines, urban streetlamps or cosmic crystals) */
private fun DrawScope.drawForegroundCanopyLayer(
    width: Float,
    height: Float,
    biome: BiomeTheme,
    scaleX: Float,
    scaleY: Float,
    tick: Int
) {
    val outline = Color(0xFF111111)

    if (biome.worldNumber == 1 || biome == BiomeTheme.FOREST_OUTSKIRTS || biome == BiomeTheme.SAN_FRANCIS_KALE) {
        // Overhanging cartoon jungle vine with lush leaves in top-left corner
        val vinePath = Path().apply {
            moveTo(0f, 0f)
            cubicTo(40f, 20f, 80f, 60f, 110f, 95f)
        }
        drawPath(vinePath, color = Color(0xFF2E7D32), style = Stroke(width = 7f, cap = StrokeCap.Round))
        drawPath(vinePath, color = outline, style = Stroke(width = 2.2f, cap = StrokeCap.Round))
        // 3 hanging leaves
        drawCircle(color = Color(0xFF4CAF50), radius = 14f, center = Offset(45f, 32f))
        drawCircle(color = outline, radius = 14f, center = Offset(45f, 32f), style = Stroke(width = 2.2f))
        drawCircle(color = Color(0xFF4CAF50), radius = 16f, center = Offset(88f, 74f))
        drawCircle(color = outline, radius = 16f, center = Offset(88f, 74f), style = Stroke(width = 2.2f))
    } else if (biome.worldNumber in listOf(9, 10, 11)) {
        // Floating cosmic diamond crystals in top-right corner
        val cryAngle = Math.toRadians((tick * 1.5f).toDouble())
        val cryX = width - 45f + cos(cryAngle).toFloat() * 12f
        val cryY = 40f + sin(cryAngle).toFloat() * 8f
        val crystalPath = Path().apply {
            moveTo(cryX, cryY - 18f)
            lineTo(cryX + 12f, cryY)
            lineTo(cryX, cryY + 18f)
            lineTo(cryX - 12f, cryY)
            close()
        }
        drawPath(crystalPath, color = Color(0xCC00E5FF))
        drawPath(crystalPath, color = Color.White, style = Stroke(width = 2.2f))
    }
}

/** 2.5D Ground Contact Shadows directly beneath characters */
private fun DrawScope.drawGroundContactShadow(x: Float, y: Float, groundY: Float, isGrounded: Boolean) {
    val dist = (groundY - y).coerceAtLeast(0f)
    val shadowAlpha = if (isGrounded) 0.45f else (0.45f - dist * 0.0015f).coerceIn(0.1f, 0.45f)
    val shadowWidth = if (isGrounded) 46f else (46f - dist * 0.05f).coerceIn(16f, 46f)
    val shadowHeight = shadowWidth * 0.35f

    drawOval(
        color = Color(0x66000000).copy(alpha = shadowAlpha),
        topLeft = Offset(x - shadowWidth * 0.5f, groundY - shadowHeight * 0.5f),
        size = Size(shadowWidth, shadowHeight)
    )
}

/** Soft cinematic corner vignette framing the action */
private fun DrawScope.drawComicCornerVignette(width: Float, height: Float) {
    // Top vignette bar
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent),
            startY = 0f,
            endY = 56f
        ),
        size = Size(width, 56f)
    )
    // Bottom vignette bar
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.42f)),
            startY = height - 64f,
            endY = height
        ),
        topLeft = Offset(0f, height - 64f),
        size = Size(width, 64f)
    )
}

// =========================================================================
// PLATFORM & TERRAIN GRAPHICS WITH CEL-SHADING & TEXTURES
// =========================================================================

private fun DrawScope.drawComicPlatformDetailed(
    plat: PlatformEntity,
    biome: BiomeTheme,
    engine: CartoonStageEngine,
    tick: Int
) {
    val outline = Color(0xFF111111)

    when (plat.kind) {
        PlatformKind.SOLID -> {
            // Earthen strata body with gradient
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(biome.groundColor, biome.groundColor.copy(alpha = 0.88f)),
                    startY = plat.y,
                    endY = plat.y + plat.height
                ),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(14f, 14f)
            )

            // Sedimentary stratum bands & cartoon pebble fossils
            var py = plat.y + 28f
            while (py < plat.y + plat.height - 10f) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.22f),
                    start = Offset(plat.x + 12f, py),
                    end = Offset(plat.x + plat.width - 12f, py),
                    strokeWidth = 3f
                )
                py += 24f
            }

            // Embedded cartoon fossils / carrot roots
            val rootX = plat.x + plat.width * 0.45f
            val rootY = plat.y + 36f
            drawCircle(color = Color(0xFFFF9800).copy(alpha = 0.7f), radius = 5f, center = Offset(rootX, rootY))

            // Upper Lush Grass/Moss Trim with scalloped tufts
            drawRoundRect(
                color = biome.groundTrimColor,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, 20f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Scalloped grass fringe tufts with blooming wildflowers!
            var gx = plat.x + 10f
            var flowerIdx = 0
            while (gx < plat.x + plat.width - 14f) {
                drawCircle(color = biome.groundTrimColor, radius = 6f, center = Offset(gx, plat.y + 20f))

                // Blooming Wildflowers (Buttercups, Pink daisies, White clovers)
                if (flowerIdx % 3 == 1 && plat.width > 60f) {
                    val fColor = when (flowerIdx % 9) {
                        1 -> Color(0xFFFFEA00)
                        4 -> Color(0xFFFF4081)
                        else -> Color(0xFFFFFFFF)
                    }
                    drawCircle(color = fColor, radius = 3.8f, center = Offset(gx, plat.y - 2f))
                    drawCircle(color = Color(0xFFFF6D00), radius = 1.8f, center = Offset(gx, plat.y - 2f))
                    drawCircle(color = outline, radius = 3.8f, center = Offset(gx, plat.y - 2f), style = Stroke(width = 1.4f))
                }
                gx += 16f
                flowerIdx++
            }

            // Hanging cartoon root tendrils beneath floating platforms
            if (plat.height < 120f) {
                var rx = plat.x + 24f
                while (rx < plat.x + plat.width - 24f) {
                    val rootLen = 14f + sin(rx * 0.4f) * 8f
                    val rPath = Path().apply {
                        moveTo(rx, plat.y + plat.height)
                        quadraticBezierTo(rx + 4f, plat.y + plat.height + rootLen * 0.5f, rx + 1f, plat.y + plat.height + rootLen)
                    }
                    drawPath(rPath, color = Color(0xFF4E342E), style = Stroke(width = 3f, cap = StrokeCap.Round))
                    drawCircle(color = Color(0xFF3E2723), radius = 2.4f, center = Offset(rx + 1f, plat.y + plat.height + rootLen))
                    rx += 48f
                }
            }

            // Cel-shading white gloss highlight line
            drawRect(
                color = Color.White.copy(alpha = 0.45f),
                topLeft = Offset(plat.x + 10f, plat.y + 3f),
                size = Size(plat.width - 20f, 4f)
            )

            // Heavy Comic Ink Contour Outline
            drawRoundRect(
                color = outline,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = 4f)
            )
        }

        PlatformKind.BOUNCY_AWNING -> {
            // Circus Awning: high-contrast curved red and white fabric stripes
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(20f, 20f)
            )
            var sx = plat.x + 16f
            while (sx < plat.x + plat.width - 22f) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(sx, plat.y),
                    size = Size(20f, plat.height),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                sx += 42f
            }

            // Dangling Golden Scallop Bells
            var fx = plat.x + 12f
            while (fx < plat.x + plat.width - 12f) {
                val bellBounce = sin((tick * 0.15f + fx * 0.1f).toDouble()).toFloat() * 2f
                drawCircle(color = Color(0xFFFFD600), radius = 7.5f, center = Offset(fx, plat.y + plat.height + 3f + bellBounce))
                drawCircle(color = outline, radius = 7.5f, center = Offset(fx, plat.y + plat.height + 3f + bellBounce), style = Stroke(width = 2.2f))
                fx += 24f
            }

            // Awning spring rebound highlight
            drawRoundRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(plat.x + 12f, plat.y + 3f),
                size = Size(plat.width - 24f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )

            drawRoundRect(
                color = outline,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(20f, 20f),
                style = Stroke(width = 4.2f)
            )
        }

        PlatformKind.UPDRAFT_VENT -> {
            // Industrial vent plating
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Warning Hazard Diagonal Yellow/Black Stripes
            var vx = plat.x + 10f
            while (vx < plat.x + plat.width - 14f) {
                drawLine(
                    color = Color(0xFFFFD600),
                    start = Offset(vx, plat.y + 6f),
                    end = Offset(vx + 14f, plat.y + plat.height - 6f),
                    strokeWidth = 6f
                )
                vx += 32f
            }

            drawRoundRect(
                color = outline,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 4f)
            )

            // Animated Swirling Cyan Wind Spiral Currents
            val windShift = (tick * 6) % 120
            for (w in 0..4) {
                val wx = plat.x + 22f + w * 46f
                val wy = plat.y - 20f - windShift - (w * 18f)
                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xEE00E5FF), Color(0x6600E5FF)),
                        startY = wy - 65f,
                        endY = wy
                    ),
                    start = Offset(wx, wy),
                    end = Offset(wx + sin(wy * 0.1f) * 8f, wy - 65f),
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        PlatformKind.PAINTED_TUNNEL_WALL -> {
            // Faux brick frame
            drawRect(color = Color(0xFF6D4C41), topLeft = Offset(plat.x, plat.y), size = Size(plat.width, plat.height))

            // Painted faux archway opening
            drawRoundRect(
                color = Color(0xFF0A0A0A),
                topLeft = Offset(plat.x + 6f, plat.y + 24f),
                size = Size(plat.width - 12f, plat.height - 24f),
                cornerRadius = CornerRadius(24f, 24f)
            )
            // Faux train tracks painted on the wall
            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(plat.x + 20f, plat.y + plat.height - 10f),
                end = Offset(plat.x + plat.width - 20f, plat.y + plat.height - 10f),
                strokeWidth = 4f
            )
            // "ACME TUNNEL CO." stenciled arch header
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(plat.x + 6f, plat.y + 24f),
                size = Size(plat.width - 12f, plat.height - 24f),
                cornerRadius = CornerRadius(24f, 24f),
                style = Stroke(width = 4.2f)
            )
            drawRect(color = outline, topLeft = Offset(plat.x, plat.y), size = Size(plat.width, plat.height), style = Stroke(width = 4f))
        }

        PlatformKind.RAINBOW_BRIDGE -> {
            // Shimmering 6-color Rainbow Ribbon
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF1744),
                        Color(0xFFFF9100),
                        Color(0xFFFFEA00),
                        Color(0xFF00E676),
                        Color(0xFF00E5FF),
                        Color(0xFFD500F9)
                    ),
                    startX = plat.x,
                    endX = plat.x + plat.width
                ),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, 24f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Sparkling animated 4-point starlets
            for (st in 0..4) {
                val sx = plat.x + 18f + st * (plat.width * 0.22f)
                val starTick = tick + st * 15
                val sAlpha = (sin(starTick * 0.2f) * 0.4f + 0.6f).coerceIn(0.2f, 1f)
                drawCircle(color = Color.White.copy(alpha = sAlpha), radius = 4f, center = Offset(sx, plat.y + 12f))
            }

            drawRoundRect(
                color = outline,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, 24f),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 3.8f)
            )
        }

        PlatformKind.CABLE_CAR_RAIL -> {
            drawRoundRect(
                color = Color(0xFFFFD54F),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, 16f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0xFF263238),
                topLeft = Offset(plat.x, plat.y + 12f),
                size = Size(plat.width, 14f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, 26f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 3.8f)
            )
        }

        PlatformKind.RHYTHM_BEAT_BLOCK -> {
            val alpha = if (engine.rhythmPhaseSolid) 0.95f else 0.28f
            drawRoundRect(
                color = Color(0xFF00E5FF).copy(alpha = alpha),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(14f, 14f)
            )
            // Equalizer grid bars
            for (eq in 0..3) {
                val eqX = plat.x + 14f + eq * (plat.width * 0.24f)
                val eqHeight = (plat.height - 18f) * ((eq + 1) * 0.25f)
                drawRect(
                    color = Color(0xFFFF007F).copy(alpha = alpha),
                    topLeft = Offset(eqX, plat.y + plat.height - 8f - eqHeight),
                    size = Size(10f, eqHeight)
                )
            }
            drawRoundRect(
                color = Color(0xFFFF007F).copy(alpha = alpha),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = 4.2f)
            )
        }

        PlatformKind.CRUMBLING_COLOS_STONE -> {
            drawRoundRect(
                color = Color(0xFFD7CCC8),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFFFFB300),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, 14f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Fracture crack lines
            drawLine(
                color = outline,
                start = Offset(plat.x + plat.width * 0.35f, plat.y + 4f),
                end = Offset(plat.x + plat.width * 0.42f, plat.y + plat.height - 4f),
                strokeWidth = 2.5f
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 3.8f)
            )
        }

        PlatformKind.ILLUSION_BOOKSHELF -> {
            val shimmer = 0.55f + (sin(tick * 0.12f) * 0.35f)
            drawRoundRect(
                color = Color(0xFF7E57C2).copy(alpha = shimmer.coerceIn(0.25f, 0.95f)),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Cartoon colorful book spines
            val bookColors = listOf(Color(0xFFFF1744), Color(0xFFFFD600), Color(0xFF00E5FF), Color(0xFF00E676))
            for (b in 0..5) {
                val bx = plat.x + 8f + b * 16f
                if (bx + 12f < plat.x + plat.width) {
                    drawRect(
                        color = bookColors[b % bookColors.size].copy(alpha = shimmer.coerceIn(0.25f, 0.95f)),
                        topLeft = Offset(bx, plat.y + 4f),
                        size = Size(12f, plat.height - 8f)
                    )
                }
            }
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(plat.x, plat.y),
                size = Size(plat.width, plat.height),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 3.8f)
            )
        }
    }
}

private fun DrawScope.drawMoltenSalsaWaveDetailed(salsaY: Float, camX: Float, tick: Int) {
    drawRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFF1744), Color(0xFFD50000), Color(0xFF880E4F)),
            startY = salsaY,
            endY = salsaY + 260f
        ),
        topLeft = Offset(camX - 100f, salsaY),
        size = Size(2600f, 300f)
    )

    // Animated foaming crest wavelets
    var wx = camX - 80f
    while (wx < camX + 2500f) {
        val waveH = sin((wx * 0.04f + tick * 0.2f).toDouble()).toFloat() * 8f
        drawCircle(color = Color(0xFFFFD54F), radius = 10f, center = Offset(wx, salsaY + waveH))
        wx += 28f
    }

    // Bubbling hot salsa blisters with steam puffs
    for (b in 0..14) {
        val bx = camX + (b * 180f + tick * 3.5f) % 2400f
        val by = salsaY + 12f + sin((tick + b * 22).toDouble()).toFloat() * 12f
        drawCircle(color = Color(0xFFFFD54F), radius = 13f, center = Offset(bx, by))
        drawCircle(color = Color(0xFF111111), radius = 13f, center = Offset(bx, by), style = Stroke(width = 3.2f))
        drawCircle(color = Color.White, radius = 4f, center = Offset(bx - 4f, by - 4f))

        // Steam puff
        if (b % 3 == 0) {
            val steamY = by - 24f - (tick % 40)
            drawCircle(color = Color.White.copy(alpha = 0.45f), radius = 6f, center = Offset(bx, steamY))
        }
    }
}

private fun DrawScope.drawGoalGoldenSafehouse(gx: Float, gy: Float, tick: Int) {
    val outline = Color(0xFF111111)

    // Outer Stone Masonry Portal
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(Color(0xFFFFD700), Color(0xFFFFA000), Color(0xFFFF6F00)),
            startY = gy - 185f,
            endY = gy
        ),
        topLeft = Offset(gx - 62f, gy - 185f),
        size = Size(124f, 185f),
        cornerRadius = CornerRadius(62f, 62f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(gx - 62f, gy - 185f),
        size = Size(124f, 185f),
        cornerRadius = CornerRadius(62f, 62f),
        style = Stroke(width = 4.2f)
    )

    // Cozy warm interior burrow darkness with radial golden glow
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.65f), Color(0xFF2A0845)),
            center = Offset(gx, gy - 80f),
            radius = 65f
        ),
        topLeft = Offset(gx - 44f, gy - 160f),
        size = Size(88f, 160f),
        cornerRadius = CornerRadius(44f, 44f)
    )

    // Flanking Warm Carriage Lanterns with volumetric light glow
    for (dir in listOf(-1f, 1f)) {
        val lx = gx + dir * 68f
        val ly = gy - 110f
        // Light glow cone
        drawCircle(color = Color(0x33FFEA00), radius = 26f, center = Offset(lx, ly))
        // Brass bracket & lantern body
        drawLine(color = outline, start = Offset(gx + dir * 56f, ly), end = Offset(lx, ly), strokeWidth = 3f)
        drawRoundRect(color = Color(0xFFFFD700), topLeft = Offset(lx - 7f, ly - 10f), size = Size(14f, 20f), cornerRadius = CornerRadius(3f, 3f))
        drawCircle(color = Color.White, radius = 4f, center = Offset(lx, ly))
        drawRoundRect(color = outline, topLeft = Offset(lx - 7f, ly - 10f), size = Size(14f, 20f), cornerRadius = CornerRadius(3f, 3f), style = Stroke(width = 2.4f))
    }

    // "BURROW SWEET BURROW" Glowing Marquee Sign
    drawRoundRect(
        color = Color(0xFF212121),
        topLeft = Offset(gx - 66f, gy - 212f),
        size = Size(132f, 28f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color(0xFFFFD700),
        topLeft = Offset(gx - 66f, gy - 212f),
        size = Size(132f, 28f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 3.2f)
    )

    // Roof Spire: Spinning Carrot Weather Vane & Checkered Victory Flag!
    val vaneAngle = Math.toRadians((tick * 3.5f).toDouble())
    val vaneOffset = cos(vaneAngle).toFloat() * 14f
    drawLine(color = outline, start = Offset(gx, gy - 212f), end = Offset(gx, gy - 245f), strokeWidth = 3f)
    // Mini Carrot Vane
    drawLine(
        color = Color(0xFFFF6D00),
        start = Offset(gx - vaneOffset, gy - 245f),
        end = Offset(gx + vaneOffset, gy - 245f),
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )
    // Checkered victory flag waving on spire
    val flagWave = sin(tick * 0.25f) * 3f
    val flagPath = Path().apply {
        moveTo(gx, gy - 238f)
        lineTo(gx + 24f, gy - 238f + flagWave)
        lineTo(gx + 24f, gy - 224f + flagWave)
        lineTo(gx, gy - 224f)
        close()
    }
    drawPath(flagPath, color = Color.White)
    drawPath(flagPath, color = outline, style = Stroke(width = 1.8f))
    // Flag checkers
    drawRect(color = Color.Black, topLeft = Offset(gx + 2f, gy - 238f), size = Size(10f, 7f))
    drawRect(color = Color.Black, topLeft = Offset(gx + 12f, gy - 231f), size = Size(10f, 7f))

    // Spinning Giant Golden Carrot Trophy inside Archway
    val rotCarrotY = gy - 95f + sin(tick * 0.15f) * 10f
    drawJuicyCarrotDetailed(gx, rotCarrotY, isGolden = true, tick = tick)
}

/** Juicy Cartoon Carrot with lush 3-leaf foliage, cel highlights, and 3D root ridges */
private fun DrawScope.drawJuicyCarrotDetailed(cx: Float, cy: Float, isGolden: Boolean, tick: Int) {
    val outline = Color(0xFF111111)

    // Lush leafy green top with 3 fan leaves and leafy veins
    val leafColor = if (isGolden) Color(0xFF00E5FF) else Color(0xFF00E676)
    val leafHighlight = if (isGolden) Color(0xFFE0F7FA) else Color(0xFFB9F6CA)

    for (angle in listOf(-34f, 0f, 34f)) {
        val lrad = Math.toRadians(angle.toDouble())
        val lx = cx + sin(lrad).toFloat() * 18f
        val ly = cy - 22f - cos(lrad).toFloat() * 14f

        drawCircle(color = leafColor, radius = 7.5f, center = Offset(lx, ly))
        drawCircle(color = leafHighlight, radius = 3.5f, center = Offset(lx - 2f, ly - 2f))
        drawCircle(color = outline, radius = 7.5f, center = Offset(lx, ly), style = Stroke(width = 2.5f))
    }

    // Carrot V-Body with rich gradient
    val bodyColors = if (isGolden) {
        listOf(Color(0xFFFFF59D), Color(0xFFFFD700), Color(0xFFFF8F00))
    } else {
        listOf(Color(0xFFFFAB40), Color(0xFFFF6D00), Color(0xFFE65100))
    }

    val path = Path().apply {
        moveTo(cx - 13f, cy - 10f)
        lineTo(cx + 13f, cy - 10f)
        lineTo(cx, cy + 22f)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = bodyColors,
            startY = cy - 10f,
            endY = cy + 22f
        )
    )
    drawPath(path, color = outline, style = Stroke(width = 3.6f, join = StrokeJoin.Round))

    // Horizontal growth ridges
    drawLine(color = Color.Black.copy(alpha = 0.25f), start = Offset(cx - 7f, cy - 3f), end = Offset(cx + 2f, cy - 3f), strokeWidth = 2.2f)
    drawLine(color = Color.Black.copy(alpha = 0.25f), start = Offset(cx - 4f, cy + 5f), end = Offset(cx + 5f, cy + 5f), strokeWidth = 2.2f)
    drawLine(color = Color.Black.copy(alpha = 0.25f), start = Offset(cx - 2f, cy + 12f), end = Offset(cx + 3f, cy + 12f), strokeWidth = 2.2f)

    // White cel gloss highlight strip along left edge
    drawLine(
        color = Color.White.copy(alpha = 0.85f),
        start = Offset(cx - 6f, cy - 6f),
        end = Offset(cx - 2f, cy + 12f),
        strokeWidth = 3.2f,
        cap = StrokeCap.Round
    )

    // Golden sparkling halo stars
    if (isGolden) {
        drawCircle(color = Color(0x55FFD700), radius = 32f, center = Offset(cx, cy))
        val starAngle = Math.toRadians((tick * 4f).toDouble())
        val sx = cx + cos(starAngle).toFloat() * 18f
        val sy = cy + sin(starAngle).toFloat() * 18f
        drawCircle(color = Color.White, radius = 3.5f, center = Offset(sx, sy))
    }
}

private fun DrawScope.drawPowerUpBoxDetailed(x: Float, y: Float, type: PowerUpType, tick: Int) {
    val outline = Color(0xFF111111)

    // Glowing aura circle
    drawCircle(color = type.color.copy(alpha = 0.38f), radius = 36f, center = Offset(x, y))

    // Crate chassis
    drawRoundRect(
        color = type.color,
        topLeft = Offset(x - 22f, y - 22f),
        size = Size(44f, 44f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    // Crate diagonal brace
    drawLine(
        color = Color.White.copy(alpha = 0.4f),
        start = Offset(x - 18f, y - 18f),
        end = Offset(x + 18f, y + 18f),
        strokeWidth = 3f
    )
    // Central white power badge
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x - 12f, y - 12f),
        size = Size(24f, 24f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(x - 22f, y - 22f),
        size = Size(44f, 44f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 4f)
    )
}

private fun DrawScope.drawDroppedCartoonShades(x: Float, y: Float) {
    val outline = Color(0xFF111111)
    drawRoundRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(x - 22f, y - 10f),
        size = Size(44f, 18f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRect(color = Color.Black, topLeft = Offset(x - 18f, y - 6f), size = Size(15f, 11f))
    drawRect(color = Color.Black, topLeft = Offset(x + 3f, y - 6f), size = Size(15f, 11f))
    drawRoundRect(
        color = outline,
        topLeft = Offset(x - 22f, y - 10f),
        size = Size(44f, 18f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 3.2f)
    )
}

// =========================================================================
// HANDCRAFTED CARTOON HERO RENDERING (ALL 6 HEROES)
// =========================================================================

private fun DrawScope.drawHeroCharacterDetailed(
    engine: CartoonStageEngine,
    headgear: SwagItem,
    hoodie: SwagItem,
    kicks: SwagItem,
    tick: Int
) {
    val px = engine.playerX
    val py = engine.playerY
    val hero = engine.selectedHero
    val dir = if (engine.facingRight) 1f else -1f
    val outline = Color(0xFF111111)

    // Damage flash blinking
    if (engine.invulnerableTimer > 0f && (tick / 3) % 2 == 1) {
        return
    }

    // Power-Up or Special Aura
    val auraColor = engine.activePowerUp?.color
        ?: if (engine.isRailGrinding) Color(0xFFFFD740)
        else if (hero == PlayableHero.GOLDEN_BOBS_CHAMPION) Color(0xFFFFD700)
        else if (hero == PlayableHero.COSMIC_EMPRESS_BUNZILLA) Color(0xFFE040FB)
        else null

    auraColor?.let { c ->
        drawCircle(color = c.copy(alpha = 0.32f), radius = 68f, center = Offset(px, py - 46f))
    }

    // 1. BURROW BLITZ STATE (Peeking Ears / Periscope)
    if (engine.isBurrowing) {
        drawRoundRect(
            color = Color(0xFF4E342E),
            topLeft = Offset(px - 40f, py - 28f),
            size = Size(80f, 30f),
            cornerRadius = CornerRadius(18f, 18f)
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(px - 40f, py - 28f),
            size = Size(80f, 30f),
            cornerRadius = CornerRadius(18f, 18f),
            style = Stroke(width = 3.8f)
        )
        // Peeking bunny ears
        drawRoundRect(
            color = hero.bodyColor,
            topLeft = Offset(px - 10f, py - 58f),
            size = Size(8f, 34f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = hero.bodyColor,
            topLeft = Offset(px + 4f, py - 58f),
            size = Size(8f, 34f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        return
    }

    // 2. PANCAKE SQUASH STATE (Accordion disk + circling yellow canaries!)
    if (engine.healthState == CartoonHealthState.PANCAKE) {
        withTransform({
            scale(1.72f, 0.38f, pivot = Offset(px, py))
        }) {
            drawRoundRect(
                color = hero.bodyColor,
                topLeft = Offset(px - 32f, py - 60f),
                size = Size(64f, 60f),
                cornerRadius = CornerRadius(18f, 18f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(px - 32f, py - 60f),
                size = Size(64f, 60f),
                cornerRadius = CornerRadius(18f, 18f),
                style = Stroke(width = 4.2f)
            )
        }
        // Circling cartoon yellow canaries / stars
        for (bird in 0..2) {
            val bAngle = (tick * 6.5f + bird * 120f).toDouble()
            val bx = px + cos(Math.toRadians(bAngle)).toFloat() * 42f
            val by = py - 72f + sin(Math.toRadians(bAngle)).toFloat() * 14f
            drawCircle(color = Color(0xFFFFD600), radius = 6.5f, center = Offset(bx, by))
            drawCircle(color = outline, radius = 6.5f, center = Offset(bx, by), style = Stroke(width = 2.2f))
        }
        return
    }

    // Dynamic Cartoon Squash & Stretch Matrix
    val scaleXFactor = engine.squashStretchX
    val scaleYFactor = engine.squashStretchY

    withTransform({
        scale(scaleXFactor, scaleYFactor, pivot = Offset(px, py))
    }) {
        // REFORMED DON FOXONE (DRIVING MINI STEAMROLLER!)
        if (hero == PlayableHero.REFORMED_DON_FOXONE) {
            drawReformedDonFoxoneDetailed(px, py, dir, tick, outline)
            return@withTransform
        }

        // CANONICAL APPROVED BOB'S BUNNY DESIGN
        if (hero == PlayableHero.BOBS_BUNNY) {
            drawCanonicalBobDetailed(engine, headgear, hoodie, kicks, px, py, dir, tick, outline)
            return@withTransform
        }

        // -------------------------------------------------------------
        // BOBS FLUFFY SCALLOPED COTTONTAIL PUFF (AT BACK OF HIP)
        // -------------------------------------------------------------
        if (hero == PlayableHero.BOBS_BUNNY || hero == PlayableHero.GOLDEN_BOBS_CHAMPION) {
            val tailBounce = if (abs(engine.playerVx) > 40f) sin(tick * 0.6f) * 4f else 0f
            val tx = px - dir * 27f
            val ty = py - 38f + tailBounce
            // 3-lobe fluffy cotton cluster
            drawCircle(color = Color.White, radius = 9.5f, center = Offset(tx, ty))
            drawCircle(color = Color.White, radius = 7.5f, center = Offset(tx - dir * 5f, ty - 5f))
            drawCircle(color = Color.White, radius = 7.5f, center = Offset(tx - dir * 4f, ty + 5f))
            drawCircle(color = outline, radius = 9.5f, center = Offset(tx, ty), style = Stroke(width = 3.4f))
        }

        // -------------------------------------------------------------
        // ANIMATED RUNNING LEGS / LOONEY TUNES WHEEL OF FEET
        // -------------------------------------------------------------
        val isHighSpeedDash = engine.trollDashTimer > 0f || abs(engine.playerVx) > 460f
        val isRunning = abs(engine.playerVx) > 40f && engine.isGrounded

        if (isHighSpeedDash) {
            // Iconic Cartoon "Wheel of Feet" Smoke Ring!
            val wheelCenter = Offset(px - dir * 6f, py - 14f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xEEFF6D00), Color(0x99FFD600), Color.Transparent),
                    center = wheelCenter,
                    radius = 28f
                ),
                radius = 28f,
                center = wheelCenter
            )
            // Spinning kicks around perimeter
            for (w in 0..3) {
                val wAngle = Math.toRadians((tick * 22f * dir + w * 90f).toDouble())
                val kx = wheelCenter.x + cos(wAngle).toFloat() * 20f
                val ky = wheelCenter.y + sin(wAngle).toFloat() * 12f
                drawRoundRect(
                    color = kicks.primaryColor,
                    topLeft = Offset(kx - 9f, ky - 6f),
                    size = Size(18f, 12f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawRoundRect(
                    color = outline,
                    topLeft = Offset(kx - 9f, ky - 6f),
                    size = Size(18f, 12f),
                    cornerRadius = CornerRadius(5f, 5f),
                    style = Stroke(width = 2.4f)
                )
            }
            // Trailing dust puffs
            for (p in 0..2) {
                val puffX = px - dir * (28f + p * 16f)
                val puffY = py - 4f - p * 3f
                drawCircle(color = Color(0xCCFFFFFF), radius = 7f - p * 1.5f, center = Offset(puffX, puffY))
                drawCircle(color = outline, radius = 7f - p * 1.5f, center = Offset(puffX, puffY), style = Stroke(width = 1.8f))
            }
        } else if (isRunning) {
            // Dynamic running stride: feet pumping back and forth with rubber soles and tread grooves
            val runPhase = tick * 0.45f
            val foot1X = sin(runPhase) * 16f * dir
            val foot1Y = (abs(cos(runPhase)) * -9f)
            val foot2X = -sin(runPhase) * 16f * dir
            val foot2Y = (abs(cos(runPhase + Math.PI.toFloat())) * -9f)

            // Rear Foot (Detailed High-Top Sneaker)
            drawCartoonSneaker(px - 15f + foot2X, py - 18f + foot2Y, kicks, outline)
            // Front Foot (Detailed High-Top Sneaker)
            drawCartoonSneaker(px - 7f + foot1X, py - 18f + foot1Y, kicks, outline)

            // Trailing dust curl when running
            if (tick % 4 == 0) {
                drawCircle(color = Color(0x88FFFFFF), radius = 5.5f, center = Offset(px - dir * 24f, py - 4f))
            }
        } else {
            // Idle / Airborne Feet
            val footBounce = if (!engine.isGrounded && engine.isShowingDontLookDown) {
                sin(tick * 0.9f) * 10f
            } else if (!engine.isGrounded) {
                -6f
            } else 0f

            // Left & Right High-Top Sneakers
            drawCartoonSneaker(px - 24f, py - 18f + footBounce, kicks, outline)
            drawCartoonSneaker(px + 4f, py - 18f - footBounce, kicks, outline)
        }

        // Roxy Sparks Rocket Thrusters
        if (hero == PlayableHero.ROXY_SPARKS && (!engine.isGrounded || engine.trollDashTimer > 0f)) {
            drawCircle(color = Color(0xFF00E5FF), radius = 9f, center = Offset(px - 14f, py + 4f))
            drawCircle(color = Color.White, radius = 4f, center = Offset(px - 14f, py + 4f))
            drawCircle(color = Color(0xFF00E5FF), radius = 9f, center = Offset(px + 14f, py + 4f))
            drawCircle(color = Color.White, radius = 4f, center = Offset(px + 14f, py + 4f))
        }

        // -------------------------------------------------------------
        // WHITE CARTOON 4-FINGERED GLOVE & SLEEVE (BACK ARM)
        // -------------------------------------------------------------
        val armSwing = if (isRunning) sin(tick * 0.45f) * 18f else 0f
        val backHandX = px - dir * (22f + armSwing)
        val backHandY = py - 41f + abs(armSwing * 0.4f)

        val bodyBaseColor = when (hero) {
            PlayableHero.GOLDEN_BOBS_CHAMPION -> Color(0xFFFFD700)
            PlayableHero.COSMIC_EMPRESS_BUNZILLA -> Color(0xFF8E24AA)
            PlayableHero.BRICK_BADGER -> Color(0xFF455A64)
            PlayableHero.ROXY_SPARKS -> Color(0xFFFF7043)
            else -> hoodie.primaryColor
        }

        // Back Arm Sleeve
        drawLine(
            color = bodyBaseColor,
            start = Offset(px - dir * 10f, py - 52f),
            end = Offset(backHandX, backHandY),
            strokeWidth = 11f,
            cap = StrokeCap.Round
        )
        drawCartoonGloveHand(backHandX, backHandY, outline)

        // -------------------------------------------------------------
        // SCULPTED PEAR-SHAPED CARTOON TORSO & STREETWEAR HOODIE
        // -------------------------------------------------------------
        // Bunched Hoodie Hood behind neck
        drawRoundRect(
            color = bodyBaseColor,
            topLeft = Offset(px - 22f, py - 66f),
            size = Size(44f, 14f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(px - 22f, py - 66f),
            size = Size(44f, 14f),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 3.2f)
        )

        // Sculpted Pear-Shaped Torso Path
        val torsoPath = Path().apply {
            moveTo(px - 17f, py - 60f)
            lineTo(px + 17f, py - 60f)
            cubicTo(px + 25f, py - 45f, px + 28f, py - 24f, px + 22f, py - 15f)
            lineTo(px - 22f, py - 15f)
            cubicTo(px - 28f, py - 24f, px - 25f, py - 45f, px - 17f, py - 60f)
            close()
        }
        drawPath(
            torsoPath,
            brush = Brush.verticalGradient(
                listOf(bodyBaseColor, bodyBaseColor.copy(alpha = 0.88f)),
                startY = py - 60f,
                endY = py - 15f
            )
        )

        // Ribbed Waistband Hem at bottom of hoodie
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.22f),
            topLeft = Offset(px - 22f, py - 21f),
            size = Size(44f, 6f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Plush Cream Belly Patch / Kangaroo Pocket with Golden Carrot Emblem
        val bellyColor = if (hero == PlayableHero.BOBS_BUNNY) Color(0xFFFFF9C4) else if (hero == PlayableHero.GOLDEN_BOBS_CHAMPION) Color(0xFFFFF59D) else hero.accentColor
        drawOval(
            color = bellyColor,
            topLeft = Offset(px - 15f, py - 52f),
            size = Size(30f, 31f)
        )
        drawOval(
            color = outline,
            topLeft = Offset(px - 15f, py - 52f),
            size = Size(30f, 31f),
            style = Stroke(width = 2.2f)
        )

        // Embroidered Mini Carrot Logo on Chest
        drawCircle(color = Color(0xFFFF6D00), radius = 3.5f, center = Offset(px, py - 37f))
        drawCircle(color = Color(0xFF00E676), radius = 2.2f, center = Offset(px, py - 41f))

        // Hoodie Drawstrings dangling from collar with silver aglets
        if (hero == PlayableHero.BOBS_BUNNY || hero == PlayableHero.GOLDEN_BOBS_CHAMPION) {
            drawLine(color = Color.White, start = Offset(px - 7f, py - 57f), end = Offset(px - 8f, py - 41f), strokeWidth = 2.2f)
            drawCircle(color = Color(0xFFB0BEC5), radius = 2.2f, center = Offset(px - 8f, py - 40f))
            drawLine(color = Color.White, start = Offset(px + 7f, py - 57f), end = Offset(px + 8f, py - 41f), strokeWidth = 2.2f)
            drawCircle(color = Color(0xFFB0BEC5), radius = 2.2f, center = Offset(px + 8f, py - 40f))
        }

        // White cel highlight on shoulder
        drawRoundRect(
            color = Color.White.copy(alpha = 0.38f),
            topLeft = Offset(px - 15f, py - 58f),
            size = Size(30f, 5f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawPath(torsoPath, color = outline, style = Stroke(width = 3.8f, join = StrokeJoin.Round))

        // -------------------------------------------------------------
        // WHITE CARTOON 4-FINGERED GLOVE & SLEEVE (FRONT ARM)
        // -------------------------------------------------------------
        val frontHandX = px + dir * (20f + armSwing)
        val frontHandY = py - 43f - abs(armSwing * 0.4f)
        drawLine(
            color = bodyBaseColor,
            start = Offset(px + dir * 12f, py - 52f),
            end = Offset(frontHandX, frontHandY),
            strokeWidth = 11f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = outline,
            start = Offset(px + dir * 12f, py - 52f),
            end = Offset(frontHandX, frontHandY),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = bodyBaseColor,
            start = Offset(px + dir * 12f, py - 52f),
            end = Offset(frontHandX, frontHandY),
            strokeWidth = 9f,
            cap = StrokeCap.Round
        )
        drawCartoonGloveHand(frontHandX, frontHandY, outline)

        // Roxy's Fluffy Cybernetic Fox Tail (Sculpted S-curve with white tip & glowing neon core!)
        if (hero == PlayableHero.ROXY_SPARKS) {
            val tailWag = sin(tick * 0.25f) * 9f
            val rTailPath = Path().apply {
                moveTo(px - dir * 18f, py - 34f)
                cubicTo(px - dir * 48f, py - 62f + tailWag, px - dir * 56f, py - 24f + tailWag, px - dir * 20f, py - 20f)
                close()
            }
            drawPath(rTailPath, color = Color(0xFFFF7043))
            drawCircle(color = Color.White, radius = 9f, center = Offset(px - dir * 43f, py - 42f + tailWag))
            drawCircle(color = Color(0xFF00E5FF), radius = 5.5f, center = Offset(px - dir * 32f, py - 35f + tailWag))
            drawPath(rTailPath, color = outline, style = Stroke(width = 3.6f, join = StrokeJoin.Round))
        }

        // Golden Bobs Royal Velvet Cape & Cosmic Empress Starlight Cape
        if (hero == PlayableHero.GOLDEN_BOBS_CHAMPION || hero == PlayableHero.COSMIC_EMPRESS_BUNZILLA) {
            val capeWave = sin(tick * 0.22f) * 8f
            val capeColor = if (hero == PlayableHero.GOLDEN_BOBS_CHAMPION) Color(0xFFD50000) else Color(0xFF4A148C)
            val capePath = Path().apply {
                moveTo(px - dir * 12f, py - 58f)
                cubicTo(px - dir * 42f, py - 46f + capeWave, px - dir * 48f, py - 22f - capeWave, px - dir * 34f, py - 14f)
                lineTo(px - dir * 14f, py - 18f)
                close()
            }
            drawPath(capePath, color = capeColor)
            // White royal ermine trim on Golden Bobs' cape
            if (hero == PlayableHero.GOLDEN_BOBS_CHAMPION) {
                drawCircle(color = Color.White, radius = 5f, center = Offset(px - dir * 34f, py - 16f))
                drawCircle(color = Color.White, radius = 5f, center = Offset(px - dir * 24f, py - 17f))
            } else {
                // Twinkling stars inside Empress Bunzilla's nebula cape
                drawCircle(color = Color(0xFF00E5FF), radius = 2.5f, center = Offset(px - dir * 30f, py - 34f))
                drawCircle(color = Color(0xFFFFD700), radius = 2f, center = Offset(px - dir * 24f, py - 24f))
            }
            drawPath(capePath, color = outline, style = Stroke(width = 3.2f, join = StrokeJoin.Round))
        }

        // Cosmic Empress Planetary Orbital Ring with Orbiting Moons
        if (hero == PlayableHero.COSMIC_EMPRESS_BUNZILLA) {
            drawOval(
                color = Color(0xFFFFD700),
                topLeft = Offset(px - 38f, py - 46f),
                size = Size(76f, 15f),
                style = Stroke(width = 3.4f)
            )
            val moonX = px + cos(tick * 0.12f) * 38f
            val moonY = py - 38.5f + sin(tick * 0.12f) * 7.5f
            drawCircle(color = Color(0xFF00E5FF), radius = 6f, center = Offset(moonX, moonY))
            drawCircle(color = outline, radius = 6f, center = Offset(moonX, moonY), style = Stroke(width = 2f))
        }

        // Brick Badger Spiked Pauldrons & Championship Belt
        if (hero == PlayableHero.BRICK_BADGER) {
            // Gold Heavyweight Championship Belt around waist
            drawRoundRect(color = Color(0xFF212121), topLeft = Offset(px - 24f, py - 26f), size = Size(48f, 9f), cornerRadius = CornerRadius(4f, 4f))
            drawCircle(color = Color(0xFFFFD700), radius = 7f, center = Offset(px, py - 21.5f))
            drawCircle(color = outline, radius = 7f, center = Offset(px, py - 21.5f), style = Stroke(width = 2f))
            // Spiked Shoulder Pads
            for (sx in listOf(px - 23f, px + 23f)) {
                drawCircle(color = Color(0xFF37474F), radius = 9f, center = Offset(sx, py - 54f))
                drawCircle(color = outline, radius = 9f, center = Offset(sx, py - 54f), style = Stroke(width = 2.6f))
                drawCircle(color = Color(0xFFCFD8DC), radius = 3.2f, center = Offset(sx, py - 62f))
            }
        }

        // -------------------------------------------------------------
        // SCULPTED CARTOON HEAD WITH FLUFFY CHEEK RUFFS & TOPKNOT TUFT
        // -------------------------------------------------------------
        // Cheek fluff ruffs extending outward
        val cheekFluffPath = Path().apply {
            moveTo(px - 22f, py - 82f)
            lineTo(px - 32f, py - 78f)
            lineTo(px - 26f, py - 73f)
            lineTo(px - 33f, py - 69f)
            lineTo(px - 20f, py - 66f)
            moveTo(px + 22f, py - 82f)
            lineTo(px + 32f, py - 78f)
            lineTo(px + 26f, py - 73f)
            lineTo(px + 33f, py - 69f)
            lineTo(px + 20f, py - 66f)
        }
        drawPath(cheekFluffPath, color = hero.bodyColor)
        drawPath(cheekFluffPath, color = outline, style = Stroke(width = 3.4f, join = StrokeJoin.Round))

        // Main Cranium
        drawCircle(color = hero.bodyColor, radius = 26f, center = Offset(px, py - 82f))
        drawCircle(color = Color.Black.copy(alpha = 0.10f), radius = 24f, center = Offset(px, py - 80f))
        // Rim light highlight on cranium
        drawArc(
            color = Color.White.copy(alpha = 0.7f),
            startAngle = 200f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(px - 23f, py - 105f),
            size = Size(46f, 46f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
        drawCircle(color = outline, radius = 26f, center = Offset(px, py - 82f), style = Stroke(width = 3.8f))

        // 3-Spike Forehead Hair Tuft between the ears!
        val tuftPath = Path().apply {
            moveTo(px - 6f, py - 106f)
            lineTo(px - 4f, py - 117f)
            lineTo(px, py - 109f)
            lineTo(px + 4f, py - 119f)
            lineTo(px + 7f, py - 106f)
            close()
        }
        drawPath(tuftPath, color = hero.bodyColor)
        drawPath(tuftPath, color = outline, style = Stroke(width = 2.8f, join = StrokeJoin.Round))

        // Badger facial stripes & tough underbite tusks
        if (hero == PlayableHero.BRICK_BADGER) {
            drawRoundRect(color = Color(0xFF1A1A1A), topLeft = Offset(px - 20f, py - 90f), size = Size(13f, 24f), cornerRadius = CornerRadius(6f, 6f))
            drawRoundRect(color = Color(0xFF1A1A1A), topLeft = Offset(px + 7f, py - 90f), size = Size(13f, 24f), cornerRadius = CornerRadius(6f, 6f))
            drawRoundRect(color = Color.White, topLeft = Offset(px - 6f, py - 96f), size = Size(12f, 30f), cornerRadius = CornerRadius(6f, 6f))
        }

        // -------------------------------------------------------------
        // ARTICULATED CUBIC-BEZIER EARS FOR EACH HERO SPECIES
        // -------------------------------------------------------------
        val earTilt = if (isRunning) -dir * (abs(engine.playerVx) / 500f).coerceIn(0f, 1f) * 18f else 0f

        if (engine.isHelicopterGliding || engine.isShowingDontLookDown) {
            // Rapid cartoon rotor blur with motion rings
            val spread = cos(Math.toRadians(engine.earSpinAngle.toDouble())).toFloat() * 48f
            val halfSpan = abs(spread).coerceAtLeast(44f)
            drawRoundRect(
                color = hero.bodyColor,
                topLeft = Offset(px - halfSpan, py - 114f),
                size = Size(halfSpan * 2f, 15f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(px - halfSpan, py - 114f),
                size = Size(halfSpan * 2f, 15f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 3.8f)
            )
            // Rotor air vortex ring above ears
            drawOval(
                color = Color.White.copy(alpha = 0.55f),
                topLeft = Offset(px - halfSpan * 0.9f, py - 122f),
                size = Size(halfSpan * 1.8f, 8f),
                style = Stroke(width = 2f)
            )
        } else if (hero == PlayableHero.BRICK_BADGER) {
            // Rounded Badger Ears with pink inner ear & yellow construction hard-hat!
            drawCircle(color = hero.bodyColor, radius = 9.5f, center = Offset(px - 17f, py - 105f))
            drawCircle(color = Color(0xFFFF80AB), radius = 4.5f, center = Offset(px - 17f, py - 105f))
            drawCircle(color = outline, radius = 9.5f, center = Offset(px - 17f, py - 105f), style = Stroke(width = 3.4f))

            drawCircle(color = hero.bodyColor, radius = 9.5f, center = Offset(px + 17f, py - 105f))
            drawCircle(color = Color(0xFFFF80AB), radius = 4.5f, center = Offset(px + 17f, py - 105f))
            drawCircle(color = outline, radius = 9.5f, center = Offset(px + 17f, py - 105f), style = Stroke(width = 3.4f))
        } else if (hero == PlayableHero.ROXY_SPARKS) {
            // Pointed Fennec Fox Ears with White Tips & Steampunk Brass Goggles on Forehead!
            val foxEars = Path().apply {
                moveTo(px - 22f, py - 98f)
                lineTo(px - 28f + earTilt * 0.5f, py - 142f)
                lineTo(px - 6f, py - 105f)
                moveTo(px + 6f, py - 105f)
                lineTo(px + 28f + earTilt * 0.5f, py - 142f)
                lineTo(px + 22f, py - 98f)
            }
            drawPath(foxEars, color = Color(0xFFFF7043))
            drawCircle(color = Color.White, radius = 5f, center = Offset(px - 26f + earTilt * 0.4f, py - 134f))
            drawCircle(color = Color.White, radius = 5f, center = Offset(px + 26f + earTilt * 0.4f, py - 134f))
            drawPath(foxEars, color = outline, style = Stroke(width = 3.6f, join = StrokeJoin.Round))
            // Brass Aviator Goggles strapped on Roxy's forehead
            drawCircle(color = Color(0xFFFFB300), radius = 8.5f, center = Offset(px - 9f, py - 102f))
            drawCircle(color = Color(0xFF00E5FF), radius = 5.5f, center = Offset(px - 9f, py - 102f))
            drawCircle(color = outline, radius = 8.5f, center = Offset(px - 9f, py - 102f), style = Stroke(width = 2.2f))
            drawCircle(color = Color(0xFFFFB300), radius = 8.5f, center = Offset(px + 9f, py - 102f))
            drawCircle(color = Color(0xFF00E5FF), radius = 5.5f, center = Offset(px + 9f, py - 102f))
            drawCircle(color = outline, radius = 8.5f, center = Offset(px + 9f, py - 102f), style = Stroke(width = 2.2f))
        } else {
            // Left Sculpted Bunny Ear (narrow base -> wide paddle -> expressive bent tip!)
            val leftEarPath = Path().apply {
                moveTo(px - 14f, py - 104f)
                cubicTo(px - 24f + earTilt * 0.5f, py - 126f, px - 22f + earTilt, py - 150f, px - 10f + earTilt * 1.2f, py - 154f)
                cubicTo(px - 2f + earTilt, py - 148f, px - 4f + earTilt * 0.4f, py - 124f, px - 5f, py - 104f)
                close()
            }
            drawPath(leftEarPath, color = hero.bodyColor)

            // Left Inner Pink Ear Canal with gradient
            val leftInnerPath = Path().apply {
                moveTo(px - 12f, py - 108f)
                cubicTo(px - 19f + earTilt * 0.5f, py - 124f, px - 17f + earTilt, py - 142f, px - 10f + earTilt * 1.1f, py - 145f)
                cubicTo(px - 6f + earTilt, py - 140f, px - 6f + earTilt * 0.4f, py - 122f, px - 7f, py - 108f)
                close()
            }
            drawPath(
                leftInnerPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFF80AB), Color(0xFFF50057)),
                    startY = py - 146f,
                    endY = py - 108f
                )
            )
            drawPath(leftEarPath, color = outline, style = Stroke(width = 3.8f, join = StrokeJoin.Round))

            // Right Sculpted Bunny Ear (with playful tip bend!)
            val rightTipDroop = sin(tick * 0.08f) * 3f
            val rightEarPath = Path().apply {
                moveTo(px + 5f, py - 104f)
                cubicTo(px + 4f + earTilt * 0.4f, py - 124f, px + 4f + earTilt, py - 148f, px + 14f + earTilt * 1.3f, py - 153f + rightTipDroop)
                cubicTo(px + 25f + earTilt, py - 146f + rightTipDroop, px + 24f + earTilt * 0.5f, py - 124f, px + 14f, py - 104f)
                close()
            }
            drawPath(rightEarPath, color = hero.bodyColor)

            val rightInnerPath = Path().apply {
                moveTo(px + 7f, py - 108f)
                cubicTo(px + 6f + earTilt * 0.4f, py - 122f, px + 7f + earTilt, py - 140f, px + 13f + earTilt * 1.1f, py - 144f + rightTipDroop)
                cubicTo(px + 19f + earTilt, py - 138f + rightTipDroop, px + 18f + earTilt * 0.5f, py - 122f, px + 12f, py - 108f)
                close()
            }
            drawPath(
                rightInnerPath,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFF80AB), Color(0xFFF50057)),
                    startY = py - 146f,
                    endY = py - 108f
                )
            )
            drawPath(rightEarPath, color = outline, style = Stroke(width = 3.8f, join = StrokeJoin.Round))
        }

        // Headgear / 24K Multi-Pronged Royal Crown / Celestial Tiara
        if (hero == PlayableHero.GOLDEN_BOBS_CHAMPION) {
            val cy = py - 106f
            val crownPath = Path().apply {
                moveTo(px - 20f, cy)
                lineTo(px - 22f, cy - 18f)
                lineTo(px - 10f, cy - 8f)
                lineTo(px, cy - 22f)
                lineTo(px + 10f, cy - 8f)
                lineTo(px + 22f, cy - 18f)
                lineTo(px + 20f, cy)
                close()
            }
            drawPath(crownPath, color = Color(0xFFFFD700))
            drawPath(crownPath, color = outline, style = Stroke(width = 3f, join = StrokeJoin.Round))
            // Crown Jewels (Ruby, Diamond, Emerald)
            drawCircle(color = Color(0xFFFF1744), radius = 3.2f, center = Offset(px - 12f, cy - 5f))
            drawCircle(color = Color(0xFF00E5FF), radius = 4f, center = Offset(px, cy - 7f))
            drawCircle(color = Color(0xFF00E676), radius = 3.2f, center = Offset(px + 12f, cy - 5f))
        } else if (hero == PlayableHero.COSMIC_EMPRESS_BUNZILLA) {
            // Crescent Moon Celestial Tiara
            drawCircle(color = Color(0xFFFFD700), radius = 10f, center = Offset(px, py - 108f))
            drawCircle(color = Color(0xFF00E5FF), radius = 4.5f, center = Offset(px, py - 108f))
            drawCircle(color = outline, radius = 10f, center = Offset(px, py - 108f), style = Stroke(width = 2.4f))
        } else if (headgear.id != "head_classic_shades") {
            drawRoundRect(
                color = headgear.primaryColor,
                topLeft = Offset(px - 20f, py - 106f),
                size = Size(40f, 14f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(px - 20f, py - 106f),
                size = Size(40f, 14f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3.2f)
            )
        }

        // -------------------------------------------------------------
        // SCULPTED W-SHAPED RABBIT MUZZLE, PUFFY CHEEKS & WHISKERS
        // -------------------------------------------------------------
        val cheekY = py - 76f
        // Overlapping double-lobe white muzzle puffs
        drawOval(color = Color.White, topLeft = Offset(px - 22f, cheekY - 9f), size = Size(24f, 18f))
        drawOval(color = Color.White, topLeft = Offset(px - 2f, cheekY - 9f), size = Size(24f, 18f))
        drawOval(color = outline, topLeft = Offset(px - 22f, cheekY - 9f), size = Size(24f, 18f), style = Stroke(width = 2.4f))
        drawOval(color = outline, topLeft = Offset(px - 2f, cheekY - 9f), size = Size(24f, 18f), style = Stroke(width = 2.4f))
        // Cover inner seam of muzzle
        drawCircle(color = Color.White, radius = 8f, center = Offset(px, cheekY - 1f))

        // Rosy blush ovals
        drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 5.5f, center = Offset(px - 13f, cheekY - 1f))
        drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 5.5f, center = Offset(px + 13f, cheekY - 1f))

        // Springy curved cartoon whiskers
        for (w in -1..1) {
            val wy = cheekY + w * 4f
            drawLine(color = outline, start = Offset(px - 17f, wy), end = Offset(px - 31f, wy + w * 3f), strokeWidth = 1.8f, cap = StrokeCap.Round)
            drawLine(color = outline, start = Offset(px + 17f, wy), end = Offset(px + 31f, wy + w * 3f), strokeWidth = 1.8f, cap = StrokeCap.Round)
        }

        // Cute pink triangular rabbit nose with white specular shine dot
        val nosePath = Path().apply {
            moveTo(px - 5f, py - 81f)
            lineTo(px + 5f, py - 81f)
            lineTo(px, py - 75f)
            close()
        }
        drawPath(nosePath, color = Color(0xFFFF4081))
        drawCircle(color = Color.White, radius = 1.5f, center = Offset(px - 1.5f, py - 79.5f))
        drawPath(nosePath, color = outline, style = Stroke(width = 1.8f, join = StrokeJoin.Round))
        // Vertical philtrum cleft line from nose to teeth
        drawLine(color = outline, start = Offset(px, py - 75f), end = Offset(px, py - 71f), strokeWidth = 1.8f)

        // Open grinning mouth with ruby cavity, pink tongue, and two rounded buck teeth
        val mouthY = py - 71f
        val mouthPath = Path().apply {
            moveTo(px - 10f, mouthY)
            quadraticBezierTo(px, mouthY + 11f, px + 10f, mouthY)
            close()
        }
        drawPath(mouthPath, color = Color(0xFF880E4F))
        drawCircle(color = Color(0xFFFF80AB), radius = 5f, center = Offset(px, mouthY + 6.5f))
        drawPath(mouthPath, color = outline, style = Stroke(width = 2.4f))

        // Two rounded white buck teeth with center seam
        drawRoundRect(color = Color.White, topLeft = Offset(px - 6f, mouthY - 1f), size = Size(5.8f, 7.5f), cornerRadius = CornerRadius(2f, 2f))
        drawRoundRect(color = outline, topLeft = Offset(px - 6f, mouthY - 1f), size = Size(5.8f, 7.5f), cornerRadius = CornerRadius(2f, 2f), style = Stroke(width = 1.5f))
        drawRoundRect(color = Color.White, topLeft = Offset(px + 0.2f, mouthY - 1f), size = Size(5.8f, 7.5f), cornerRadius = CornerRadius(2f, 2f))
        drawRoundRect(color = outline, topLeft = Offset(px + 0.2f, mouthY - 1f), size = Size(5.8f, 7.5f), cornerRadius = CornerRadius(2f, 2f), style = Stroke(width = 1.5f))

        // Bobbing Carrot Toothpick with green foliage
        val toothBob = sin(tick * 0.3f) * 2.2f
        drawLine(
            color = Color(0xFFFF9100),
            start = Offset(px + 4f, mouthY + 2f),
            end = Offset(px + dir * 19f, mouthY - 2f + toothBob),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawCircle(color = Color(0xFF00E676), radius = 3.5f, center = Offset(px + dir * 20f, mouthY - 2f + toothBob))

        // -------------------------------------------------------------
        // FLOATING EXPRESSIVE EYEBROWS & SUNGLASSES / CARTOON EYES
        // -------------------------------------------------------------
        // Floating arched comic eyebrows above eyes/shades
        val browRaise = if (!engine.isGrounded) -3f else 0f
        drawArc(
            color = outline,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(px - 18f, py - 101f + browRaise),
            size = Size(14f, 8f),
            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
        )
        drawArc(
            color = outline,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(px + 4f, py - 101f + browRaise),
            size = Size(14f, 8f),
            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
        )

        if (engine.isShowingDontLookDown) {
            // Coyote Panic Saucer Eyes
            drawCircle(color = Color.White, radius = 10f, center = Offset(px - 8f, py - 88f))
            drawCircle(color = Color.Red, radius = 4.5f, center = Offset(px - 8f, py - 88f))
            drawCircle(color = outline, radius = 10f, center = Offset(px - 8f, py - 88f), style = Stroke(width = 2.4f))
            drawCircle(color = Color.White, radius = 10f, center = Offset(px + 8f, py - 88f))
            drawCircle(color = Color.Red, radius = 4.5f, center = Offset(px + 8f, py - 88f))
            drawCircle(color = outline, radius = 10f, center = Offset(px + 8f, py - 88f), style = Stroke(width = 2.4f))
            drawCircle(color = Color(0xFF00E5FF), radius = 5.5f, center = Offset(px + dir * 28f, py - 94f))
        } else if (engine.healthState == CartoonHealthState.FULL_SWAG || engine.activePowerUp == PowerUpType.SWAG_SHADES || headgear.id == "head_classic_shades") {
            // Cool Shades with animated diagonal glossy glare streaks
            val shadesX = px + dir * 2f - 22f
            val shadesY = py - 94f
            drawRoundRect(
                color = headgear.primaryColor,
                topLeft = Offset(shadesX, shadesY),
                size = Size(44f, 17f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = Color(0xFF111111),
                topLeft = Offset(shadesX + 3f, shadesY + 2f),
                size = Size(38f, 13f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Animated specular shine streaks (//) sweeping across shades
            val glarePhase = (tick % 60) / 60f
            val gx = shadesX + 6f + glarePhase * 24f
            drawLine(
                color = Color.White.copy(alpha = 0.85f),
                start = Offset(gx, shadesY + 12f),
                end = Offset(gx + 6f, shadesY + 3f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(gx + 4f, shadesY + 12f),
                end = Offset(gx + 10f, shadesY + 3f),
                strokeWidth = 1.5f,
                cap = StrokeCap.Round
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(shadesX, shadesY),
                size = Size(44f, 17f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3.8f)
            )
        } else {
            // Large expressive pie-cut cartoon eyes with dual specular highlights
            drawCircle(color = Color.White, radius = 9.5f, center = Offset(px - 7f, py - 88f))
            drawCircle(color = Color(0xFF1E88E5), radius = 5.8f, center = Offset(px - 7f + dir * 2f, py - 88f))
            drawCircle(color = Color.Black, radius = 4f, center = Offset(px - 7f + dir * 2f, py - 88f))
            drawCircle(color = Color.White, radius = 2.4f, center = Offset(px - 8f + dir * 2f, py - 90f))
            drawCircle(color = Color.White, radius = 1.2f, center = Offset(px - 5f + dir * 2f, py - 86f))
            drawCircle(color = outline, radius = 9.5f, center = Offset(px - 7f, py - 88f), style = Stroke(width = 2.2f))

            drawCircle(color = Color.White, radius = 9.5f, center = Offset(px + 7f, py - 88f))
            drawCircle(color = Color(0xFF1E88E5), radius = 5.8f, center = Offset(px + 7f + dir * 2f, py - 88f))
            drawCircle(color = Color.Black, radius = 4f, center = Offset(px + 7f + dir * 2f, py - 88f))
            drawCircle(color = Color.White, radius = 2.4f, center = Offset(px + 6f + dir * 2f, py - 90f))
            drawCircle(color = Color.White, radius = 1.2f, center = Offset(px + 9f + dir * 2f, py - 86f))
            drawCircle(color = outline, radius = 9.5f, center = Offset(px + 7f, py - 88f), style = Stroke(width = 2.2f))
        }
    }
}

/**
 * Renders the approved canonical Bob's Bunny design:
 * taller/slimmer athletic proportions, golden-cream & warm-white fur,
 * expressive blue eyes, leather aviator cap with yellow goggles, yellow scarf,
 * orange-and-white outerwear, midnight-navy pants, utility belt, gloves,
 * and white-and-yellow footwear.
 */
private fun DrawScope.drawCanonicalBobDetailed(
    engine: CartoonStageEngine,
    headgear: SwagItem,
    hoodie: SwagItem,
    kicks: SwagItem,
    px: Float,
    py: Float,
    dir: Float,
    tick: Int,
    outline: Color
) {
    val furCream = Color(0xFFF4D58D)
    val furWarmWhite = Color(0xFFFFFBF2)
    val pantsNavy = Color(0xFF162040)
    val pantsHighlight = Color(0xFF283566)
    val scarfYellow = Color(0xFFFFD600)
    val scarfShade = Color(0xFFFFA000)
    val beltLeather = Color(0xFF4E342E)
    val brassGold = Color(0xFFFFD54F)
    val jacketPrimary = hoodie.primaryColor
    val jacketTrim = hoodie.secondaryColor

    val isHighSpeedDash = engine.trollDashTimer > 0f || abs(engine.playerVx) > 460f
    val isRunning = abs(engine.playerVx) > 40f && engine.isGrounded
    val idleBob = if (!isRunning && engine.isGrounded) sin(tick * 0.14f) * 1.6f else 0f

    // Fluffy warm-white cottontail at back of hip
    val tailBounce = if (isRunning) sin(tick * 0.55f) * 3.5f else 0f
    val tx = px - dir * 18f
    val ty = py - 48f + tailBounce + idleBob
    drawCircle(color = furWarmWhite, radius = 8f, center = Offset(tx, ty))
    drawCircle(color = furWarmWhite, radius = 6f, center = Offset(tx - dir * 4f, ty - 4f))
    drawCircle(color = outline, radius = 8f, center = Offset(tx, ty), style = Stroke(width = 3f))

    // -------------------------------------------------------------
    // MIDNIGHT-NAVY ATHLETIC PANTS & WHITE-AND-YELLOW FOOTWEAR
    // -------------------------------------------------------------
    if (isHighSpeedDash) {
        val wheelCenter = Offset(px - dir * 4f, py - 16f)
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xEEFF6D00), Color(0x99FFD600), Color.Transparent),
                center = wheelCenter,
                radius = 30f
            ),
            radius = 30f,
            center = wheelCenter
        )
        for (w in 0..3) {
            val wAngle = Math.toRadians((tick * 22f * dir + w * 90f).toDouble())
            val kx = wheelCenter.x + cos(wAngle).toFloat() * 20f
            val ky = wheelCenter.y + sin(wAngle).toFloat() * 11f
            drawRoundRect(
                color = kicks.primaryColor,
                topLeft = Offset(kx - 9f, ky - 5f),
                size = Size(18f, 10f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(kx - 9f, ky - 5f),
                size = Size(18f, 10f),
                cornerRadius = CornerRadius(5f, 5f),
                style = Stroke(width = 2.4f)
            )
        }
    } else if (isRunning) {
        val runPhase = tick * 0.45f
        val foot1X = sin(runPhase) * 16f * dir
        val foot1Y = abs(cos(runPhase)) * -9f
        val foot2X = -sin(runPhase) * 16f * dir
        val foot2Y = abs(cos(runPhase + Math.PI.toFloat())) * -9f

        // Back leg (midnight-navy pants)
        drawLine(
            color = pantsNavy,
            start = Offset(px - dir * 4f, py - 46f),
            end = Offset(px - 4f + foot2X, py - 15f + foot2Y),
            strokeWidth = 11f,
            cap = StrokeCap.Round
        )
        drawCartoonSneaker(px - 15f + foot2X, py - 18f + foot2Y, kicks, outline)

        // Front leg (midnight-navy pants with subtle highlight)
        drawLine(
            color = pantsNavy,
            start = Offset(px + dir * 4f, py - 46f),
            end = Offset(px + 4f + foot1X, py - 15f + foot1Y),
            strokeWidth = 11.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = pantsHighlight,
            start = Offset(px + dir * 4f, py - 44f),
            end = Offset(px + 4f + foot1X, py - 20f + foot1Y),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        drawCartoonSneaker(px - 7f + foot1X, py - 18f + foot1Y, kicks, outline)
    } else {
        val airLift = if (!engine.isGrounded && engine.isShowingDontLookDown) sin(tick * 0.9f) * 9f
        else if (!engine.isGrounded) -5f
        else 0f
        // Left & right slim athletic legs in midnight-navy pants
        drawRoundRect(
            color = pantsNavy,
            topLeft = Offset(px - 14f, py - 48f + idleBob),
            size = Size(11f, 34f),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(px - 14f, py - 48f + idleBob),
            size = Size(11f, 34f),
            cornerRadius = CornerRadius(5f, 5f),
            style = Stroke(width = 2.8f)
        )
        drawRoundRect(
            color = pantsNavy,
            topLeft = Offset(px + 3f, py - 48f + idleBob),
            size = Size(11f, 34f),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = outline,
            topLeft = Offset(px + 3f, py - 48f + idleBob),
            size = Size(11f, 34f),
            cornerRadius = CornerRadius(5f, 5f),
            style = Stroke(width = 2.8f)
        )
        drawCartoonSneaker(px - 22f, py - 18f + airLift, kicks, outline)
        drawCartoonSneaker(px + 1f, py - 18f - airLift, kicks, outline)
    }

    // -------------------------------------------------------------
    // BACK ARM & GLOVE
    // -------------------------------------------------------------
    val armSwing = if (isRunning) sin(tick * 0.45f) * 18f else if (!engine.isGrounded) -10f else 0f
    val backHandX = px - dir * (19f + armSwing)
    val backHandY = py - 54f + abs(armSwing * 0.35f) + idleBob
    drawLine(
        color = outline,
        start = Offset(px - dir * 10f, py - 76f + idleBob),
        end = Offset(backHandX, backHandY),
        strokeWidth = 12f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = jacketPrimary,
        start = Offset(px - dir * 10f, py - 76f + idleBob),
        end = Offset(backHandX, backHandY),
        strokeWidth = 8.5f,
        cap = StrokeCap.Round
    )
    drawCartoonGloveHand(backHandX, backHandY, outline)

    // -------------------------------------------------------------
    // ATHLETIC V-TAPER ORANGE-AND-WHITE OUTERWEAR & UTILITY BELT
    // -------------------------------------------------------------
    val torsoTopY = py - 84f + idleBob
    val torsoBottomY = py - 46f + idleBob
    val torsoPath = Path().apply {
        moveTo(px - 19f, torsoTopY)
        lineTo(px + 19f, torsoTopY)
        cubicTo(px + 20f, torsoTopY + 14f, px + 15f, torsoBottomY - 6f, px + 14f, torsoBottomY)
        lineTo(px - 14f, torsoBottomY)
        cubicTo(px - 15f, torsoBottomY - 6f, px - 20f, torsoTopY + 14f, px - 19f, torsoTopY)
        close()
    }
    drawPath(
        torsoPath,
        brush = Brush.verticalGradient(
            listOf(jacketPrimary, jacketPrimary.copy(alpha = 0.9f)),
            startY = torsoTopY,
            endY = torsoBottomY
        )
    )
    // Crisp warm-white athletic chest V-panel & shoulder trim
    val whiteChestPanel = Path().apply {
        moveTo(px - 11f, torsoTopY + 2f)
        lineTo(px + 11f, torsoTopY + 2f)
        lineTo(px + 6f, torsoBottomY - 4f)
        lineTo(px - 6f, torsoBottomY - 4f)
        close()
    }
    drawPath(whiteChestPanel, color = jacketTrim)
    // Subtle athletic center zipper seam
    drawLine(
        color = outline.copy(alpha = 0.45f),
        start = Offset(px, torsoTopY + 4f),
        end = Offset(px, torsoBottomY - 3f),
        strokeWidth = 1.8f
    )
    drawPath(torsoPath, color = outline, style = Stroke(width = 3.4f, join = StrokeJoin.Round))

    // Utility Belt & Brass Buckle + Side Pouches
    drawRoundRect(
        color = beltLeather,
        topLeft = Offset(px - 15f, torsoBottomY - 4f),
        size = Size(30f, 8f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(px - 15f, torsoBottomY - 4f),
        size = Size(30f, 8f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 2.4f)
    )
    // Side utility pouches
    drawRoundRect(color = Color(0xFF6D4C41), topLeft = Offset(px - 15f, torsoBottomY - 2f), size = Size(7f, 8f), cornerRadius = CornerRadius(2f, 2f))
    drawRoundRect(color = Color(0xFF6D4C41), topLeft = Offset(px + 8f, torsoBottomY - 2f), size = Size(7f, 8f), cornerRadius = CornerRadius(2f, 2f))
    // Gold belt buckle
    drawRoundRect(color = brassGold, topLeft = Offset(px - 5f, torsoBottomY - 3.5f), size = Size(10f, 7f), cornerRadius = CornerRadius(2f, 2f))
    drawRoundRect(color = outline, topLeft = Offset(px - 5f, torsoBottomY - 3.5f), size = Size(10f, 7f), cornerRadius = CornerRadius(2f, 2f), style = Stroke(width = 1.8f))

    // -------------------------------------------------------------
    // DYNAMIC FLUTTERING YELLOW SCARF
    // -------------------------------------------------------------
    val scarfWave = sin(tick * 0.28f) * 6f + (if (isRunning) -4f else 0f)
    val scarfTail = Path().apply {
        moveTo(px - dir * 8f, torsoTopY + 2f)
        cubicTo(
            px - dir * 26f, torsoTopY - 4f + scarfWave,
            px - dir * 36f, torsoTopY + 12f - scarfWave * 0.6f,
            px - dir * 42f, torsoTopY + 16f + scarfWave
        )
        lineTo(px - dir * 34f, torsoTopY + 22f + scarfWave * 0.8f)
        cubicTo(
            px - dir * 24f, torsoTopY + 14f,
            px - dir * 14f, torsoTopY + 8f,
            px - dir * 6f, torsoTopY + 8f
        )
        close()
    }
    drawPath(scarfTail, color = scarfShade)
    drawPath(scarfTail, color = outline, style = Stroke(width = 2.6f, join = StrokeJoin.Round))
    // Scarf neck wrap
    drawRoundRect(
        color = scarfYellow,
        topLeft = Offset(px - 15f, torsoTopY - 5f),
        size = Size(30f, 10f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(px - 15f, torsoTopY - 5f),
        size = Size(30f, 10f),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(width = 2.8f)
    )

    // -------------------------------------------------------------
    // FRONT ARM & GLOVE
    // -------------------------------------------------------------
    val frontHandX = px + dir * (18f + armSwing)
    val frontHandY = py - 56f - abs(armSwing * 0.35f) + idleBob
    drawLine(
        color = outline,
        start = Offset(px + dir * 11f, py - 76f + idleBob),
        end = Offset(frontHandX, frontHandY),
        strokeWidth = 12f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = jacketPrimary,
        start = Offset(px + dir * 11f, py - 76f + idleBob),
        end = Offset(frontHandX, frontHandY),
        strokeWidth = 8.5f,
        cap = StrokeCap.Round
    )
    drawCartoonGloveHand(frontHandX, frontHandY, outline)

    // -------------------------------------------------------------
    // GOLDEN-CREAM & WARM-WHITE HEAD + EXPRESSIVE EARS + AVIATOR CAP
    // -------------------------------------------------------------
    val headY = py - 103f + idleBob

    // Expressive tall rabbit ears
    val earTilt = if (isRunning) -dir * (abs(engine.playerVx) / 500f).coerceIn(0f, 1f) * 20f else 0f
    if (engine.isHelicopterGliding || engine.isShowingDontLookDown) {
        val spread = cos(Math.toRadians(engine.earSpinAngle.toDouble())).toFloat() * 48f
        val halfSpan = abs(spread).coerceAtLeast(44f)
        drawRoundRect(color = furCream, topLeft = Offset(px - halfSpan, headY - 32f), size = Size(halfSpan * 2f, 14f), cornerRadius = CornerRadius(7f, 7f))
        drawRoundRect(color = outline, topLeft = Offset(px - halfSpan, headY - 32f), size = Size(halfSpan * 2f, 14f), cornerRadius = CornerRadius(7f, 7f), style = Stroke(width = 3.4f))
        drawOval(color = Color.White.copy(alpha = 0.55f), topLeft = Offset(px - halfSpan * 0.9f, headY - 40f), size = Size(halfSpan * 1.8f, 8f), style = Stroke(width = 2f))
    } else {
        val leftEar = Path().apply {
            moveTo(px - 12f, headY - 18f)
            cubicTo(px - 22f + earTilt * 0.5f, headY - 42f, px - 20f + earTilt, headY - 66f, px - 9f + earTilt * 1.2f, headY - 70f)
            cubicTo(px - 1f + earTilt, headY - 64f, px - 3f + earTilt * 0.4f, headY - 40f, px - 4f, headY - 18f)
            close()
        }
        drawPath(leftEar, color = furCream)
        val leftInner = Path().apply {
            moveTo(px - 10f, headY - 22f)
            cubicTo(px - 17f + earTilt * 0.5f, headY - 40f, px - 15f + earTilt, headY - 58f, px - 9f + earTilt * 1.1f, headY - 61f)
            cubicTo(px - 5f + earTilt, headY - 56f, px - 5f + earTilt * 0.4f, headY - 38f, px - 6f, headY - 22f)
            close()
        }
        drawPath(leftInner, color = Color(0xFFFFAB91))
        drawPath(leftEar, color = outline, style = Stroke(width = 3.4f, join = StrokeJoin.Round))

        val rightTipDroop = sin(tick * 0.08f) * 3f + (if (engine.squashStretchY < 0.85f) 6f else 0f)
        val rightEar = Path().apply {
            moveTo(px + 4f, headY - 18f)
            cubicTo(px + 3f + earTilt * 0.4f, headY - 40f, px + 4f + earTilt, headY - 64f, px + 13f + earTilt * 1.3f, headY - 69f + rightTipDroop)
            cubicTo(px + 23f + earTilt, headY - 62f + rightTipDroop, px + 22f + earTilt * 0.5f, headY - 40f, px + 12f, headY - 18f)
            close()
        }
        drawPath(rightEar, color = furCream)
        val rightInner = Path().apply {
            moveTo(px + 6f, headY - 22f)
            cubicTo(px + 5f + earTilt * 0.4f, headY - 38f, px + 6f + earTilt, headY - 56f, px + 12f + earTilt * 1.1f, headY - 60f + rightTipDroop)
            cubicTo(px + 17f + earTilt, headY - 54f + rightTipDroop, px + 16f + earTilt * 0.5f, headY - 38f, px + 10f, headY - 22f)
            close()
        }
        drawPath(rightInner, color = Color(0xFFFFAB91))
        drawPath(rightEar, color = outline, style = Stroke(width = 3.4f, join = StrokeJoin.Round))
    }

    // Cheek fur tufts
    val cheekTufts = Path().apply {
        moveTo(px - 19f, headY)
        lineTo(px - 28f, headY + 4f)
        lineTo(px - 22f, headY + 8f)
        lineTo(px - 27f, headY + 12f)
        lineTo(px - 17f, headY + 14f)
        moveTo(px + 19f, headY)
        lineTo(px + 28f, headY + 4f)
        lineTo(px + 22f, headY + 8f)
        lineTo(px + 27f, headY + 12f)
        lineTo(px + 17f, headY + 14f)
    }
    drawPath(cheekTufts, color = furCream)
    drawPath(cheekTufts, color = outline, style = Stroke(width = 3f, join = StrokeJoin.Round))

    // Main Head Cranium (Golden-Cream Fur)
    drawCircle(color = furCream, radius = 23f, center = Offset(px, headY))
    drawCircle(color = outline, radius = 23f, center = Offset(px, headY), style = Stroke(width = 3.4f))

    // Leather Aviator Cap with Yellow Goggles (Default canonical headgear)
    if (headgear.id == "head_classic_shades") {
        // Aviator cap dome
        drawArc(
            color = Color(0xFF5D4037),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(px - 23f, headY - 25f),
            size = Size(46f, 34f)
        )
        drawArc(
            color = outline,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(px - 23f, headY - 25f),
            size = Size(46f, 34f),
            style = Stroke(width = 3f)
        )
        // Side ear-flap straps
        drawRoundRect(color = Color(0xFF4E342E), topLeft = Offset(px - 24f, headY - 10f), size = Size(6f, 16f), cornerRadius = CornerRadius(3f, 3f))
        drawRoundRect(color = Color(0xFF4E342E), topLeft = Offset(px + 18f, headY - 10f), size = Size(6f, 16f), cornerRadius = CornerRadius(3f, 3f))

        // Yellow Aviator Goggles perched on cap forehead
        for (gxOffset in listOf(-9f, 9f)) {
            val gx = px + gxOffset + dir * 1.5f
            val gy = headY - 15f
            drawCircle(color = Color(0xFF3E2723), radius = 8.5f, center = Offset(gx, gy))
            drawCircle(color = Color(0xFFFFD600), radius = 6.2f, center = Offset(gx, gy))
            drawCircle(color = Color.White.copy(alpha = 0.75f), radius = 2f, center = Offset(gx - 2f, gy - 2f))
            drawCircle(color = outline, radius = 8.5f, center = Offset(gx, gy), style = Stroke(width = 2f))
        }
        // Goggle bridge
        drawLine(color = Color(0xFF3E2723), start = Offset(px - 2f + dir * 1.5f, headY - 15f), end = Offset(px + 2f + dir * 1.5f, headY - 15f), strokeWidth = 3f)
    } else {
        drawRoundRect(color = headgear.primaryColor, topLeft = Offset(px - 20f, headY - 24f), size = Size(40f, 13f), cornerRadius = CornerRadius(6f, 6f))
        drawRoundRect(color = outline, topLeft = Offset(px - 20f, headY - 24f), size = Size(40f, 13f), cornerRadius = CornerRadius(6f, 6f), style = Stroke(width = 3f))
    }

    // Warm-white muzzle, whiskers, nose & expressive grin
    val cheekY = headY + 6f
    drawOval(color = furWarmWhite, topLeft = Offset(px - 19f, cheekY - 8f), size = Size(20f, 15f))
    drawOval(color = furWarmWhite, topLeft = Offset(px - 1f, cheekY - 8f), size = Size(20f, 15f))
    drawOval(color = outline, topLeft = Offset(px - 19f, cheekY - 8f), size = Size(20f, 15f), style = Stroke(width = 2f))
    drawOval(color = outline, topLeft = Offset(px - 1f, cheekY - 8f), size = Size(20f, 15f), style = Stroke(width = 2f))
    drawCircle(color = furWarmWhite, radius = 7f, center = Offset(px, cheekY - 1f))

    // Whiskers
    for (w in -1..1) {
        val wy = cheekY + w * 3.5f
        drawLine(color = outline, start = Offset(px - 14f, wy), end = Offset(px - 26f, wy + w * 2.5f), strokeWidth = 1.6f, cap = StrokeCap.Round)
        drawLine(color = outline, start = Offset(px + 14f, wy), end = Offset(px + 26f, wy + w * 2.5f), strokeWidth = 1.6f, cap = StrokeCap.Round)
    }

    // Pink rabbit nose & smile
    val nosePath = Path().apply {
        moveTo(px - 4.5f, headY + 1f)
        lineTo(px + 4.5f, headY + 1f)
        lineTo(px, headY + 6f)
        close()
    }
    drawPath(nosePath, color = Color(0xFFFF4081))
    drawPath(nosePath, color = outline, style = Stroke(width = 1.6f, join = StrokeJoin.Round))
    val mouthY = headY + 10f
    val mouthPath = Path().apply {
        moveTo(px - 8f, mouthY)
        quadraticBezierTo(px, mouthY + 9f, px + 8f, mouthY)
        close()
    }
    drawPath(mouthPath, color = Color(0xFF880E4F))
    drawPath(mouthPath, color = outline, style = Stroke(width = 2f))
    drawRoundRect(color = Color.White, topLeft = Offset(px - 4.5f, mouthY - 0.5f), size = Size(4.4f, 6f), cornerRadius = CornerRadius(1.5f, 1.5f))
    drawRoundRect(color = Color.White, topLeft = Offset(px + 0.1f, mouthY - 0.5f), size = Size(4.4f, 6f), cornerRadius = CornerRadius(1.5f, 1.5f))

    // Expressive eyebrows
    val browRaise = if (!engine.isGrounded) -2.5f else 0f
    drawArc(color = outline, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(px - 16f, headY - 14f + browRaise), size = Size(12f, 6f), style = Stroke(width = 2.4f, cap = StrokeCap.Round))
    drawArc(color = outline, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(px + 4f, headY - 14f + browRaise), size = Size(12f, 6f), style = Stroke(width = 2.4f, cap = StrokeCap.Round))

    // Expressive Blue Eyes (always visible unless Swag Shades power-up is active!)
    if (engine.isShowingDontLookDown) {
        for (exOffset in listOf(-7.5f, 7.5f)) {
            drawCircle(color = Color.White, radius = 8.5f, center = Offset(px + exOffset, headY - 4f))
            drawCircle(color = Color(0xFF1E88E5), radius = 4f, center = Offset(px + exOffset, headY - 4f))
            drawCircle(color = outline, radius = 8.5f, center = Offset(px + exOffset, headY - 4f), style = Stroke(width = 2.2f))
        }
    } else if (engine.activePowerUp == PowerUpType.SWAG_SHADES) {
        val shadesX = px + dir * 2f - 20f
        val shadesY = headY - 10f
        drawRoundRect(color = Color(0xFF111111), topLeft = Offset(shadesX, shadesY), size = Size(40f, 14f), cornerRadius = CornerRadius(5f, 5f))
        drawRoundRect(color = Color(0xFF00E5FF), topLeft = Offset(shadesX, shadesY), size = Size(40f, 14f), cornerRadius = CornerRadius(5f, 5f), style = Stroke(width = 2.4f))
    } else {
        val isBlinking = (tick % 180) in 0..3
        for (exOffset in listOf(-7.5f, 7.5f)) {
            val ex = px + exOffset
            val ey = headY - 4f
            if (isBlinking) {
                drawLine(color = outline, start = Offset(ex - 6f, ey), end = Offset(ex + 6f, ey), strokeWidth = 2.6f, cap = StrokeCap.Round)
            } else {
                drawCircle(color = Color.White, radius = 7.8f, center = Offset(ex, ey))
                drawCircle(color = Color(0xFF1E88E5), radius = 5.0f, center = Offset(ex + dir * 1.5f, ey))
                drawCircle(color = Color(0xFF4FC3F7), radius = 3.2f, center = Offset(ex + dir * 1.5f, ey + 1f))
                drawCircle(color = Color(0xFF0D1B2A), radius = 2.7f, center = Offset(ex + dir * 1.8f, ey))
                drawCircle(color = Color.White, radius = 2.0f, center = Offset(ex - 1f + dir * 1.5f, ey - 2f))
                drawCircle(color = outline, radius = 7.8f, center = Offset(ex, ey), style = Stroke(width = 2.1f))
            }
        }
    }
}

/** Draws a high-top streetwear cartoon sneaker with white toe-cap, X-laces, ankle star patch & rubber sole */
private fun DrawScope.drawCartoonSneaker(
    x: Float,
    y: Float,
    kicks: SwagItem,
    outline: Color
) {
    // Main shoe upper
    drawRoundRect(
        color = kicks.primaryColor,
        topLeft = Offset(x, y),
        size = Size(23f, 18f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // White rubber toe-cap
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(x + 14f, y + 6f),
        size = Size(9f, 10f),
        cornerRadius = CornerRadius(5f, 5f)
    )
    // Thick vulcanized midsole (uses secondaryColor trim when primary is white)
    val midsoleColor = if (kicks.primaryColor == Color.White) kicks.secondaryColor else Color.White
    drawRect(color = midsoleColor, topLeft = Offset(x, y + 13f), size = Size(23f, 5f))
    // Laces
    val laceColor = if (kicks.primaryColor == Color.White) kicks.secondaryColor else Color.White
    drawLine(color = laceColor, start = Offset(x + 9f, y + 4f), end = Offset(x + 14f, y + 7f), strokeWidth = 1.6f)
    drawLine(color = laceColor, start = Offset(x + 9f, y + 7f), end = Offset(x + 14f, y + 4f), strokeWidth = 1.6f)
    // Ankle star emblem circle
    drawCircle(color = Color.White, radius = 3.2f, center = Offset(x + 6f, y + 7f))
    drawCircle(color = kicks.secondaryColor, radius = 1.8f, center = Offset(x + 6f, y + 7f))
    // Bold comic outline
    drawRoundRect(
        color = outline,
        topLeft = Offset(x, y),
        size = Size(23f, 18f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 3.6f)
    )
}

/** Draws a classic 1940s white cartoon glove with rolled wrist cuff and 3 pintuck darts */
private fun DrawScope.drawCartoonGloveHand(
    hx: Float,
    hy: Float,
    outline: Color
) {
    // Rolled wrist cuff ring
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(hx - 8f, hy - 11f),
        size = Size(16f, 6f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(hx - 8f, hy - 11f),
        size = Size(16f, 6f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 2.4f)
    )
    // Puffy palm & fingers
    drawCircle(color = Color.White, radius = 9.5f, center = Offset(hx, hy))
    drawCircle(color = outline, radius = 9.5f, center = Offset(hx, hy), style = Stroke(width = 3f))
    // 3 classic black pintuck darts on back of glove
    drawLine(color = outline, start = Offset(hx - 3.2f, hy - 4f), end = Offset(hx - 3.2f, hy + 2.5f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    drawLine(color = outline, start = Offset(hx, hy - 5f), end = Offset(hx, hy + 3f), strokeWidth = 1.5f, cap = StrokeCap.Round)
    drawLine(color = outline, start = Offset(hx + 3.2f, hy - 4f), end = Offset(hx + 3.2f, hy + 2.5f), strokeWidth = 1.5f, cap = StrokeCap.Round)
}

private fun DrawScope.drawReformedDonFoxoneDetailed(
    px: Float,
    py: Float,
    dir: Float,
    tick: Int,
    outline: Color
) {
    // Mini Steamroller Chassis with rich candy apple red
    drawRoundRect(
        color = Color(0xFFD50000),
        topLeft = Offset(px - 38f, py - 50f),
        size = Size(76f, 38f),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(px - 38f, py - 50f),
        size = Size(76f, 38f),
        cornerRadius = CornerRadius(10f, 10f),
        style = Stroke(width = 4f)
    )

    // Glowing Golden Headlight casting beam forward
    val lightX = px + dir * 36f
    val lightY = py - 40f
    drawCircle(color = Color(0xFFFFEA00), radius = 7f, center = Offset(lightX, lightY))
    drawCircle(color = outline, radius = 7f, center = Offset(lightX, lightY), style = Stroke(width = 2.2f))
    // Forward light cone
    val conePath = Path().apply {
        moveTo(lightX, lightY)
        lineTo(lightX + dir * 140f, lightY - 30f)
        lineTo(lightX + dir * 140f, lightY + 30f)
        close()
    }
    drawPath(conePath, color = Color(0x22FFF59D))

    // Spinning Spiked Front Drum
    val drumX = px + dir * 36f
    val drumY = py - 18f
    drawCircle(color = Color(0xFFFFD700), radius = 23f, center = Offset(drumX, drumY))
    drawCircle(color = outline, radius = 23f, center = Offset(drumX, drumY), style = Stroke(width = 3.8f))
    for (spk in 0..5) {
        val sAngle = Math.toRadians((tick * 8.5f * dir + spk * 60f).toDouble())
        val sx = drumX + cos(sAngle).toFloat() * 21f
        val sy = drumY + sin(sAngle).toFloat() * 21f
        drawCircle(color = Color(0xFFFF1744), radius = 4.5f, center = Offset(sx, sy))
    }

    // Heavy Tread Rear Wheel
    drawCircle(color = Color(0xFF212121), radius = 19f, center = Offset(px - dir * 28f, py - 18f))
    drawCircle(color = outline, radius = 19f, center = Offset(px - dir * 28f, py - 18f), style = Stroke(width = 3.2f))

    // Smokestack with cartoon exhaust rings
    val stackX = px - dir * 20f
    val stackY = py - 68f
    drawRect(color = Color(0xFFEEEEEE), topLeft = Offset(stackX - 4.5f, stackY), size = Size(9f, 20f))
    drawCircle(color = Color(0xCCFFFFFF), radius = 9f, center = Offset(stackX, stackY - 10f))

    // Don Foxone in Driver Cockpit
    drawCircle(color = Color(0xFFFF6D00), radius = 19f, center = Offset(px, py - 62f))
    drawCircle(color = outline, radius = 19f, center = Offset(px, py - 62f), style = Stroke(width = 3.8f))

    // Red Fedora with Gold Pin
    val fedoraY = py - 82f
    drawRoundRect(color = Color(0xFFD50000), topLeft = Offset(px - 20f, fedoraY), size = Size(40f, 14f), cornerRadius = CornerRadius(4f, 4f))
    drawRect(color = Color.Black, topLeft = Offset(px - 16f, fedoraY + 7f), size = Size(32f, 4f))
    drawRoundRect(color = outline, topLeft = Offset(px - 20f, fedoraY), size = Size(40f, 14f), cornerRadius = CornerRadius(4f, 4f), style = Stroke(width = 3.2f))
}

// =========================================================================
// HANDCRAFTED CARTOON ENEMIES & BOSSES
// =========================================================================

private fun DrawScope.drawEnemyCartoonDetailed(enemy: EnemyEntity, tick: Int) {
    val ex = enemy.x
    val ey = enemy.y
    val outline = Color(0xFF111111)

    // Boss Battle Entities
    if (enemy.kind.isBoss) {
        drawCartoonBossDetailed(enemy, ex, ey, tick, outline)
        return
    }

    // Accordion Flattened State (Looney Tunes Slapstick Accordion!)
    if (enemy.isFlattenedAccordion) {
        for (layer in 0..3) {
            val ly = ey - 8f - layer * 7f
            drawRoundRect(
                color = if (layer % 2 == 0) Color(0xFFFF6D00) else Color(0xFFFFD54F),
                topLeft = Offset(ex - 36f, ly),
                size = Size(72f, 7f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = outline,
                topLeft = Offset(ex - 36f, ly),
                size = Size(72f, 7f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 2.6f)
            )
        }
        return
    }

    // Minion Types
    val foxColor = when (enemy.kind) {
        EnemyKind.SLY_SCOUT -> Color(0xFFE65100)
        EnemyKind.STREET_SWEEPER -> Color(0xFFAD1457)
        EnemyKind.WRENCH_FOX -> Color(0xFF4527A0)
        EnemyKind.MECHA_FOX -> Color(0xFF00ACC1)
        EnemyKind.GLADIATOR_FOX -> Color(0xFFC62828)
        EnemyKind.ILLUSION_PHANTOM -> Color(0xFF7E57C2)
        else -> Color(0xFFD50000)
    }

    // Sly Scout: Camouflaged Bush Disguise!
    if (enemy.kind == EnemyKind.SLY_SCOUT) {
        drawCircle(color = Color(0xFF2E7D32), radius = 25f, center = Offset(ex, ey - 30f))
        drawCircle(color = Color(0xFF388E3C), radius = 19f, center = Offset(ex - 13f, ey - 34f))
        drawCircle(color = Color(0xFF388E3C), radius = 19f, center = Offset(ex + 13f, ey - 34f))
        drawCircle(color = outline, radius = 25f, center = Offset(ex, ey - 30f), style = Stroke(width = 3.6f))
        // Shaking eyes peering out from bush
        val eyeShift = sin(tick * 0.3f) * 2.5f
        drawCircle(color = Color.White, radius = 6.5f, center = Offset(ex - 9f + eyeShift, ey - 30f))
        drawCircle(color = Color.Black, radius = 3.5f, center = Offset(ex - 9f + eyeShift, ey - 30f))
        drawCircle(color = Color.White, radius = 6.5f, center = Offset(ex + 9f + eyeShift, ey - 30f))
        drawCircle(color = Color.Black, radius = 3.5f, center = Offset(ex + 9f + eyeShift, ey - 30f))
        return
    }

    // Minion Body
    drawRoundRect(
        color = foxColor,
        topLeft = Offset(ex - 24f, ey - 56f),
        size = Size(48f, 46f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    // Cream fox belly patch
    drawOval(
        color = Color(0xFFFFF3E0),
        topLeft = Offset(ex - 13f, ey - 48f),
        size = Size(26f, 26f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(ex - 24f, ey - 56f),
        size = Size(48f, 46f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 3.6f)
    )

    // Bushy Fox Tail with white tip wagging at back
    val tailWag = sin(tick * 0.2f) * 6f
    val tailPath = Path().apply {
        moveTo(ex + 18f, ey - 28f)
        cubicTo(ex + 42f, ey - 46f + tailWag, ex + 48f, ey - 18f + tailWag, ex + 20f, ey - 16f)
        close()
    }
    drawPath(tailPath, color = foxColor)
    drawCircle(color = Color.White, radius = 6.5f, center = Offset(ex + 38f, ey - 31f + tailWag))
    drawPath(tailPath, color = outline, style = Stroke(width = 3f, join = StrokeJoin.Round))

    // Minion Head & Pointed Snout
    drawCircle(color = Color(0xFFFF6D00), radius = 21f, center = Offset(ex, ey - 70f))
    drawCircle(color = outline, radius = 21f, center = Offset(ex, ey - 70f), style = Stroke(width = 3.6f))
    // White Fox Snout & Black Nose
    drawOval(color = Color(0xFFFFF3E0), topLeft = Offset(ex - 14f, ey - 68f), size = Size(22f, 13f))
    drawCircle(color = Color.Black, radius = 3.5f, center = Offset(ex - 12f, ey - 63f))
    // Sly Scowling Eyes
    drawCircle(color = Color(0xFFFFEA00), radius = 4.5f, center = Offset(ex - 6f, ey - 74f))
    drawCircle(color = Color.Black, radius = 2.2f, center = Offset(ex - 7f, ey - 74f))
    drawCircle(color = Color(0xFFFFEA00), radius = 4.5f, center = Offset(ex + 6f, ey - 74f))
    drawCircle(color = Color.Black, radius = 2.2f, center = Offset(ex + 5f, ey - 74f))
    // Angry V-brow
    drawLine(color = outline, start = Offset(ex - 11f, ey - 79f), end = Offset(ex - 2f, ey - 76f), strokeWidth = 2.4f)
    drawLine(color = outline, start = Offset(ex + 11f, ey - 79f), end = Offset(ex + 2f, ey - 76f), strokeWidth = 2.4f)

    // Fox Ears with dark inner canals
    val earsPath = Path().apply {
        moveTo(ex - 17f, ey - 80f)
        lineTo(ex - 9f, ey - 105f)
        lineTo(ex - 2f, ey - 82f)
        moveTo(ex + 2f, ey - 82f)
        lineTo(ex + 9f, ey - 105f)
        lineTo(ex + 17f, ey - 80f)
    }
    drawPath(earsPath, color = foxColor)
    drawPath(earsPath, color = outline, style = Stroke(width = 3.2f))

    // Minion White Cartoon Gloves holding Archetype Props!
    drawCircle(color = Color.White, radius = 6.5f, center = Offset(ex - 22f, ey - 36f))
    drawCircle(color = outline, radius = 6.5f, center = Offset(ex - 22f, ey - 36f), style = Stroke(width = 2.2f))

    // Special Archetype Props & Costumes
    when (enemy.kind) {
        EnemyKind.STREET_SWEEPER -> {
            // Spinning Yellow Push-Broom & Roller Skates
            drawCircle(color = Color(0xFFFFD600), radius = 7.5f, center = Offset(ex - 14f, ey - 8f))
            drawCircle(color = outline, radius = 7.5f, center = Offset(ex - 14f, ey - 8f), style = Stroke(width = 2.2f))
            drawCircle(color = Color(0xFFFFD600), radius = 7.5f, center = Offset(ex + 14f, ey - 8f))
            drawCircle(color = outline, radius = 7.5f, center = Offset(ex + 14f, ey - 8f), style = Stroke(width = 2.2f))
            // Broomstick & Bristles
            drawLine(color = Color(0xFF8D6E63), start = Offset(ex - 22f, ey - 36f), end = Offset(ex - 36f, ey - 6f), strokeWidth = 4f, cap = StrokeCap.Round)
            drawRoundRect(color = Color(0xFFFFEA00), topLeft = Offset(ex - 46f, ey - 12f), size = Size(20f, 10f), cornerRadius = CornerRadius(3f, 3f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 46f, ey - 12f), size = Size(20f, 10f), cornerRadius = CornerRadius(3f, 3f), style = Stroke(width = 2f))
        }
        EnemyKind.WRENCH_FOX -> {
            // Blue Mechanic Cap & Oversized C-Shaped Pipe Wrench
            drawRoundRect(color = Color(0xFF1E88E5), topLeft = Offset(ex - 18f, ey - 92f), size = Size(32f, 10f), cornerRadius = CornerRadius(4f, 4f))
            drawLine(color = Color(0xFF90A4AE), start = Offset(ex - 24f, ey - 36f), end = Offset(ex - 38f, ey - 58f), strokeWidth = 6f, cap = StrokeCap.Round)
            drawCircle(color = Color(0xFFCFD8DC), radius = 9f, center = Offset(ex - 40f, ey - 62f))
            drawCircle(color = outline, radius = 9f, center = Offset(ex - 40f, ey - 62f), style = Stroke(width = 2.4f))
        }
        EnemyKind.MECHA_FOX -> {
            // Glowing Cyber Visor, Shoulder Cannon & Chest Reactor
            drawRoundRect(color = Color(0xFF00E5FF), topLeft = Offset(ex - 16f, ey - 78f), size = Size(32f, 10f), cornerRadius = CornerRadius(4f, 4f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 16f, ey - 78f), size = Size(32f, 10f), cornerRadius = CornerRadius(4f, 4f), style = Stroke(width = 2.2f))
            drawCircle(color = Color(0xFF00E5FF), radius = 6f, center = Offset(ex, ey - 36f))
            drawCircle(color = Color.White, radius = 3f, center = Offset(ex, ey - 36f))
            // Antenna with red blinking light
            drawLine(color = outline, start = Offset(ex, ey - 90f), end = Offset(ex, ey - 108f), strokeWidth = 2.5f)
            drawCircle(color = Color(0xFFFF1744), radius = 4f, center = Offset(ex, ey - 110f))
        }
        EnemyKind.GLADIATOR_FOX -> {
            // Roman Centurion Scarlet Plume Crest & Golden Shield
            drawRoundRect(color = Color(0xFFFF1744), topLeft = Offset(ex - 12f, ey - 116f), size = Size(24f, 26f), cornerRadius = CornerRadius(10f, 10f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 12f, ey - 116f), size = Size(24f, 26f), cornerRadius = CornerRadius(10f, 10f), style = Stroke(width = 2.4f))
            // Golden Round Buckler Shield
            drawCircle(color = Color(0xFFFFD700), radius = 15f, center = Offset(ex - 22f, ey - 34f))
            drawCircle(color = Color(0xFFD50000), radius = 6f, center = Offset(ex - 22f, ey - 34f))
            drawCircle(color = outline, radius = 15f, center = Offset(ex - 22f, ey - 34f), style = Stroke(width = 2.8f))
        }
        EnemyKind.ILLUSION_PHANTOM -> {
            // Floating Mystic Hood & Orbiting Purple Fire Orbs
            for (orb in 0..2) {
                val oAngle = Math.toRadians((tick * 5f + orb * 120f).toDouble())
                val ox = ex + cos(oAngle).toFloat() * 32f
                val oy = ey - 45f + sin(oAngle).toFloat() * 14f
                drawCircle(color = Color(0xFFE040FB), radius = 5.5f, center = Offset(ox, oy))
                drawCircle(color = Color.White, radius = 2.5f, center = Offset(ox, oy))
            }
        }
        else -> {}
    }
}

private fun DrawScope.drawCartoonBossDetailed(
    enemy: EnemyEntity,
    ex: Float,
    ey: Float,
    tick: Int,
    outline: Color
) {
    val isStunned = enemy.bossStunTimer > 0f
    val bossColor = if (isStunned) Color(0xFFFF8A80) else when (enemy.kind) {
        EnemyKind.GREAT_FURTINI_BOSS -> Color(0xFF6A1B9A)
        EnemyKind.WRENCH_BOSS -> Color(0xFFFF8F00)
        EnemyKind.EMPRESS_BUNZILLA_BOSS -> Color(0xFFAA00FF)
        EnemyKind.OMEGA_ANIMATOR_BOSS -> Color(0xFF212121)
        EnemyKind.GOLDEN_GUARDIAN_BOSS -> Color(0xFFFFD700)
        else -> Color(0xFFD50000)
    }

    // Heavy Armored Boss Chassis with Metallic Gradient & Rivets
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(bossColor, bossColor.copy(alpha = 0.82f)),
            startY = ey - 108f,
            endY = ey - 26f
        ),
        topLeft = Offset(ex - 80f, ey - 108f),
        size = Size(160f, 82f),
        cornerRadius = CornerRadius(22f, 22f)
    )
    // Chassis bolts/rivets along top edge
    for (r in 0..5) {
        drawCircle(color = Color(0xFFCFD8DC), radius = 3.2f, center = Offset(ex - 60f + r * 24f, ey - 98f))
    }
    drawRoundRect(
        color = outline,
        topLeft = Offset(ex - 80f, ey - 108f),
        size = Size(160f, 82f),
        cornerRadius = CornerRadius(22f, 22f),
        style = Stroke(width = 4.8f)
    )

    // Twin Exhaust Smokestacks Belching Cartoon Smoke Rings!
    for (sx in listOf(ex + 46f, ex + 64f)) {
        drawRect(color = Color(0xFF455A64), topLeft = Offset(sx - 6f, ey - 142f), size = Size(12f, 34f))
        drawRect(color = outline, topLeft = Offset(sx - 6f, ey - 142f), size = Size(12f, 34f), style = Stroke(width = 2.8f))
        val puffLift = (tick * 1.8f) % 28f
        drawCircle(color = Color(0x99ECEFF1), radius = 8f + puffLift * 0.2f, center = Offset(sx, ey - 148f - puffLift))
    }

    // Boss Villain Head & Expressive Villain Face inside Glass Canopy
    // Glass Cockpit Bubble
    drawCircle(color = Color(0x4400E5FF), radius = 36f, center = Offset(ex, ey - 124f))
    drawCircle(color = outline, radius = 36f, center = Offset(ex, ey - 124f), style = Stroke(width = 3f))
    // Villain Head
    drawCircle(color = Color(0xFFFF6D00), radius = 28f, center = Offset(ex, ey - 122f))
    drawCircle(color = outline, radius = 28f, center = Offset(ex, ey - 122f), style = Stroke(width = 4f))
    // Villain Snout, Cigar/Grin & Glowing Eyes
    drawOval(color = Color(0xFFFFF3E0), topLeft = Offset(ex - 18f, ey - 120f), size = Size(30f, 16f))
    if (isStunned) {
        // Stunned dizzy spiral eyes & orbiting stars!
        drawCircle(color = Color.White, radius = 7f, center = Offset(ex - 9f, ey - 126f))
        drawCircle(color = Color.Red, radius = 3f, center = Offset(ex - 9f, ey - 126f))
        drawCircle(color = Color.White, radius = 7f, center = Offset(ex + 9f, ey - 126f))
        drawCircle(color = Color.Red, radius = 3f, center = Offset(ex + 9f, ey - 126f))
        for (st in 0..2) {
            val sAng = Math.toRadians((tick * 8f + st * 120f).toDouble())
            drawCircle(
                color = Color(0xFFFFD600),
                radius = 5.5f,
                center = Offset(ex + cos(sAng).toFloat() * 36f, ey - 154f + sin(sAng).toFloat() * 10f)
            )
        }
    } else {
        // Menacing yellow villain eyes & gold monocle
        drawCircle(color = Color(0xFFFFEA00), radius = 6f, center = Offset(ex - 9f, ey - 126f))
        drawCircle(color = Color.Black, radius = 3f, center = Offset(ex - 10f, ey - 126f))
        drawCircle(color = Color(0xFFFFEA00), radius = 6f, center = Offset(ex + 9f, ey - 126f))
        drawCircle(color = Color.Black, radius = 3f, center = Offset(ex + 8f, ey - 126f))
        // Golden Monocle on left eye
        drawCircle(color = Color(0xFFFFD700), radius = 8.5f, center = Offset(ex - 9f, ey - 126f), style = Stroke(width = 2.2f))
    }

    // Giant Caterpillar Treads / Wheels with Spinning Hubcaps & Spokes!
    for (wx in listOf(ex - 52f, ex + 52f)) {
        val wy = ey - 28f
        drawCircle(color = Color(0xFF212121), radius = 34f, center = Offset(wx, wy))
        drawCircle(color = Color(0xFFFFD700), radius = 16f, center = Offset(wx, wy))
        drawCircle(color = outline, radius = 16f, center = Offset(wx, wy), style = Stroke(width = 2.8f))
        // Rotating wheel spokes
        for (sp in 0..3) {
            val spAngle = Math.toRadians((tick * 6f + sp * 90f).toDouble())
            drawLine(
                color = outline,
                start = Offset(wx, wy),
                end = Offset(wx + cos(spAngle).toFloat() * 32f, wy + sin(spAngle).toFloat() * 32f),
                strokeWidth = 3.2f
            )
        }
        drawCircle(color = outline, radius = 34f, center = Offset(wx, wy), style = Stroke(width = 4.2f))
    }

    // Boss Signature Weapons & Set-Pieces
    when (enemy.kind) {
        EnemyKind.OMEGA_ANIMATOR_BOSS -> {
            // Colossal Animator's Yellow No. 2 HB Pencil, Brass Ferrule, Pink Eraser & Graphite Tip!
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(ex + 58f, ey - 210f),
                size = Size(22f, 130f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Brass Ferrule Band
            drawRect(color = Color(0xFFFFB300), topLeft = Offset(ex + 56f, ey - 216f), size = Size(26f, 10f))
            // Pink Eraser
            drawRoundRect(
                color = Color(0xFFFF80AB),
                topLeft = Offset(ex + 56f, ey - 238f),
                size = Size(26f, 22f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Sharpened Wood Cone & Black Graphite Lead Tip at bottom!
            val conePath = Path().apply {
                moveTo(ex + 58f, ey - 80f)
                lineTo(ex + 80f, ey - 80f)
                lineTo(ex + 69f, ey - 52f)
                close()
            }
            drawPath(conePath, color = Color(0xFFFFCC80))
            drawCircle(color = Color(0xFF212121), radius = 4.5f, center = Offset(ex + 69f, ey - 55f))
            drawPath(conePath, color = outline, style = Stroke(width = 3f))
            drawRoundRect(
                color = outline,
                topLeft = Offset(ex + 56f, ey - 238f),
                size = Size(26f, 158f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 3.8f)
            )
        }
        EnemyKind.GREAT_FURTINI_BOSS -> {
            // Giant Magician Silk Top Hat, Orbiting Ace Cards & Magic Wand!
            drawRect(color = Color(0xFF1A1A1A), topLeft = Offset(ex - 36f, ey - 142f), size = Size(72f, 8f))
            drawRoundRect(color = Color(0xFF1A1A1A), topLeft = Offset(ex - 24f, ey - 186f), size = Size(48f, 48f), cornerRadius = CornerRadius(6f, 6f))
            drawRoundRect(color = Color(0xFFFF1744), topLeft = Offset(ex - 24f, ey - 148f), size = Size(48f, 10f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 24f, ey - 186f), size = Size(48f, 48f), cornerRadius = CornerRadius(6f, 6f), style = Stroke(width = 3.8f))
            // Orbiting Ace Playing Cards
            for (c in 0..2) {
                val cAngle = Math.toRadians((tick * 4f + c * 120f).toDouble())
                val cx = ex + cos(cAngle).toFloat() * 56f
                val cy = ey - 155f + sin(cAngle).toFloat() * 16f
                drawRoundRect(color = Color.White, topLeft = Offset(cx - 7f, cy - 10f), size = Size(14f, 20f), cornerRadius = CornerRadius(2f, 2f))
                drawCircle(color = Color(0xFFFF1744), radius = 3f, center = Offset(cx, cy))
                drawRoundRect(color = outline, topLeft = Offset(cx - 7f, cy - 10f), size = Size(14f, 20f), cornerRadius = CornerRadius(2f, 2f), style = Stroke(width = 1.8f))
            }
        }
        EnemyKind.WRENCH_BOSS -> {
            // Articulated Hydraulic Pincer Claw & Spinning Buzzsaw!
            drawRoundRect(color = Color(0xFF78909C), topLeft = Offset(ex - 96f, ey - 150f), size = Size(22f, 68f), cornerRadius = CornerRadius(6f, 6f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 96f, ey - 150f), size = Size(22f, 68f), cornerRadius = CornerRadius(6f, 6f), style = Stroke(width = 3.2f))
            // Spinning Buzzsaw Blade at front
            drawCircle(color = Color(0xFFCFD8DC), radius = 24f, center = Offset(ex - 85f, ey - 76f))
            drawCircle(color = Color(0xFFFF1744), radius = 8f, center = Offset(ex - 85f, ey - 76f))
            drawCircle(color = outline, radius = 24f, center = Offset(ex - 85f, ey - 76f), style = Stroke(width = 3.2f))
        }
        EnemyKind.EMPRESS_BUNZILLA_BOSS -> {
            // Giant Cosmic Battle Bunny Ears, Royal Crown & Orbital Rings!
            drawRoundRect(color = Color(0xFFE040FB), topLeft = Offset(ex - 26f, ey - 192f), size = Size(16f, 62f), cornerRadius = CornerRadius(8f, 8f))
            drawRoundRect(color = Color(0xFFE040FB), topLeft = Offset(ex + 10f, ey - 192f), size = Size(16f, 62f), cornerRadius = CornerRadius(8f, 8f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 26f, ey - 192f), size = Size(16f, 62f), cornerRadius = CornerRadius(8f, 8f), style = Stroke(width = 3.8f))
            drawRoundRect(color = outline, topLeft = Offset(ex + 10f, ey - 192f), size = Size(16f, 62f), cornerRadius = CornerRadius(8f, 8f), style = Stroke(width = 3.8f))
            drawOval(color = Color(0xFF00E5FF), topLeft = Offset(ex - 95f, ey - 75f), size = Size(190f, 24f), style = Stroke(width = 3.6f))
        }
        else -> {
            // Don Foxone's Menacing Steamroller Chomp-Teeth Grill & Front Spiked Drum!
            drawRoundRect(color = Color(0xFF212121), topLeft = Offset(ex - 44f, ey - 86f), size = Size(88f, 24f), cornerRadius = CornerRadius(6f, 6f))
            for (t in 0..4) {
                drawRect(color = Color.White, topLeft = Offset(ex - 38f + t * 17f, ey - 84f), size = Size(12f, 18f))
                drawRect(color = outline, topLeft = Offset(ex - 38f + t * 17f, ey - 84f), size = Size(12f, 18f), style = Stroke(width = 2.2f))
            }
            // Don Foxone's Giant Red Gangster Fedora on top of cockpit
            drawRoundRect(color = Color(0xFFD50000), topLeft = Offset(ex - 28f, ey - 160f), size = Size(56f, 18f), cornerRadius = CornerRadius(6f, 6f))
            drawRect(color = Color.Black, topLeft = Offset(ex - 24f, ey - 148f), size = Size(48f, 5f))
            drawRoundRect(color = outline, topLeft = Offset(ex - 28f, ey - 160f), size = Size(56f, 18f), cornerRadius = CornerRadius(6f, 6f), style = Stroke(width = 3.2f))
        }
    }

    // Segmented Boss HP & Stun Telegraph Bar above boss
    val maxHp = enemy.bossMaxHp.coerceAtLeast(1)
    val barWidth = 112f
    val barX = ex - barWidth / 2f
    val barY = ey - 196f
    drawRoundRect(
        color = Color(0xCC111111),
        topLeft = Offset(barX - 4f, barY - 4f),
        size = Size(barWidth + 8f, 16f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    val segWidth = (barWidth - (maxHp - 1) * 3f) / maxHp
    for (hpIdx in 0 until maxHp) {
        val sx = barX + hpIdx * (segWidth + 3f)
        val segColor = when {
            hpIdx >= enemy.bossHp -> Color(0xFF37474F)
            isStunned -> Color(0xFFFFD600)
            enemy.bossHp == 1 -> Color(0xFFFF1744)
            else -> Color(0xFF00E676)
        }
        drawRoundRect(
            color = segColor,
            topLeft = Offset(sx, barY),
            size = Size(segWidth, 8f),
            cornerRadius = CornerRadius(3f, 3f)
        )
    }
}

// =========================================================================
// SPEED LINES, HOLO-GHOST, DRONE & COMIC ACTION BUBBLES
// =========================================================================

private fun DrawScope.drawComicSpeedLines(px: Float, py: Float, facingRight: Boolean, tick: Int) {
    val dir = if (facingRight) -1f else 1f
    for (i in 0..5) {
        val yOffset = -85f + i * 22f + (sin((tick + i * 5).toDouble()) * 4f).toFloat()
        val startX = px + dir * (42f + i * 15f)
        val endX = px + dir * (130f + i * 24f)
        drawLine(
            color = Color.White.copy(alpha = 0.88f),
            start = Offset(startX, py + yOffset),
            end = Offset(endX, py + yOffset),
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawHoloGhostBunny(gx: Float, gy: Float, tick: Int) {
    val ghostAlpha = 0.55f + sin(tick * 0.15f) * 0.2f
    drawCircle(color = Color(0xFF00E5FF).copy(alpha = ghostAlpha * 0.42f), radius = 36f, center = Offset(gx, gy - 34f))
    drawRoundRect(
        color = Color.White.copy(alpha = ghostAlpha),
        topLeft = Offset(gx - 20f, gy - 54f),
        size = Size(40f, 44f),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawCircle(color = Color(0xFFFFD700).copy(alpha = ghostAlpha), radius = 19f, center = Offset(gx, gy - 74f))
    drawRoundRect(color = Color.White.copy(alpha = ghostAlpha), topLeft = Offset(gx - 13f, gy - 114f), size = Size(10f, 36f), cornerRadius = CornerRadius(6f, 6f))
    drawRoundRect(color = Color.White.copy(alpha = ghostAlpha), topLeft = Offset(gx + 3f, gy - 114f), size = Size(10f, 36f), cornerRadius = CornerRadius(6f, 6f))
}

private fun DrawScope.drawSidekickDrone(dx: Float, dy: Float, tick: Int) {
    val outline = Color(0xFF111111)
    drawCircle(color = Color(0x3300E5FF), radius = 30f, center = Offset(dx, dy))
    drawRoundRect(
        color = Color(0xFF263238),
        topLeft = Offset(dx - 22f, dy - 13f),
        size = Size(44f, 26f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(dx - 22f, dy - 13f),
        size = Size(44f, 26f),
        cornerRadius = CornerRadius(12f, 12f),
        style = Stroke(width = 3.6f)
    )
    drawCircle(color = Color(0xFF00E5FF), radius = 8f, center = Offset(dx, dy))
    val rotor = (tick % 4) * 9f - 14f
    drawLine(
        color = Color.White,
        start = Offset(dx - 28f + rotor, dy - 19f),
        end = Offset(dx + 28f - rotor, dy - 19f),
        strokeWidth = 3.8f,
        cap = StrokeCap.Round
    )
}

/** Floating starburst action bubbles ("POW!", "WHAM!", "BOING!", "KAPOW!") with comic drop shadows */
private fun DrawScope.drawComicActionBubbleDetailed(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    life: Float
) {
    val outline = Color(0xFF111111)
    val bubbleWidth = (text.length * 10f + 36f).coerceAtLeast(68f)
    val bubbleHeight = 34f
    val alpha = life.coerceIn(0f, 1f)

    // Deep comic drop shadow with starburst rays
    val rayCount = 10
    val shadowPath = Path().apply {
        for (i in 0 until rayCount * 2) {
            val angle = Math.toRadians((i * (360f / (rayCount * 2))).toDouble())
            val r = if (i % 2 == 0) bubbleWidth * 0.55f else bubbleWidth * 0.40f
            val px = x + 4f + cos(angle).toFloat() * r
            val py = y - 14f + sin(angle).toFloat() * (r * 0.45f)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(shadowPath, color = Color.Black.copy(alpha = alpha * 0.5f))

    // Starburst comic bubble base
    val bubblePath = Path().apply {
        for (i in 0 until rayCount * 2) {
            val angle = Math.toRadians((i * (360f / (rayCount * 2))).toDouble())
            val r = if (i % 2 == 0) bubbleWidth * 0.55f else bubbleWidth * 0.40f
            val px = x + cos(angle).toFloat() * r
            val py = y - 18f + sin(angle).toFloat() * (r * 0.45f)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    val fillColor = if (color == Color.Unspecified) Color(0xFFFFEA00).copy(alpha = alpha) else color.copy(alpha = alpha)
    drawPath(bubblePath, color = fillColor)
    drawPath(bubblePath, color = outline.copy(alpha = alpha), style = Stroke(width = 3.6f))
}

/** Draws cartoon road-ahead arrow signposts along the platforms indicating the path forward */
private fun DrawScope.drawRoadAheadSignposts(goalX: Float, groundY: Float, tick: Int) {
    val outline = Color(0xFF111111)
    val signSpacing = 550f
    var sx = 480f
    var signIdx = 0
    while (sx < goalX - 120f) {
        val signY = groundY - 60f
        // Wooden signpost pole
        drawRect(color = Color(0xFF5D4037), topLeft = Offset(sx - 4f, signY), size = Size(8f, 60f))
        drawRect(color = outline, topLeft = Offset(sx - 4f, signY), size = Size(8f, 60f), style = Stroke(width = 2.2f))

        // Wooden Arrow signboard pointing right
        val signColor = if (signIdx % 2 == 0) Color(0xFFFFD54F) else Color(0xFFFF9100)
        val arrowPath = Path().apply {
            moveTo(sx - 35f, signY - 24f)
            lineTo(sx + 20f, signY - 24f)
            lineTo(sx + 36f, signY - 12f)
            lineTo(sx + 20f, signY)
            lineTo(sx - 35f, signY)
            close()
        }
        drawPath(arrowPath, color = signColor)
        drawPath(arrowPath, color = outline, style = Stroke(width = 2.8f))

        // Directional arrow symbol ➔
        drawLine(
            color = Color(0xFF111111),
            start = Offset(sx - 18f, signY - 12f),
            end = Offset(sx + 14f, signY - 12f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF111111),
            start = Offset(sx + 8f, signY - 19f),
            end = Offset(sx + 15f, signY - 12f),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF111111),
            start = Offset(sx + 8f, signY - 5f),
            end = Offset(sx + 15f, signY - 12f),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        sx += signSpacing
        signIdx++
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 450)
@Composable
private fun CartoonStageCanvasPreview() {
    val stage = GameCatalog.stages.first()
    val sound = remember { CartoonSoundEngine().apply { soundEnabled = false } }
    val engine = remember {
        CartoonStageEngine(
            stageConfig = stage,
            selectedHero = PlayableHero.BOBS_BUNNY,
            equippedBadgeId = "badge_coyote_plus",
            trailColor = Color.White,
            soundEngine = sound
        )
    }
    val shades = GameCatalog.swagItems.first { it.id == "head_classic_shades" }
    val hoodie = GameCatalog.swagItems.first { it.id == "hoodie_orange_drip" }
    val kicks = GameCatalog.swagItems.first { it.id == "kicks_high_tops" }

    CartoonStageCanvas(
        engine = engine,
        headgear = shades,
        hoodie = hoodie,
        kicks = kicks
    )
}
