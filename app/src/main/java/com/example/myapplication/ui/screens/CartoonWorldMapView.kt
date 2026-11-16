package com.example.myapplication.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PlayerProfileState
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.StageConfig
import kotlin.math.cos
import kotlin.math.sin

/**
 * CartoonWorldMapView - Interactive Super Mario World / Looney Tunes Overworld Map
 * Displays:
 * - The winding "Great Carrot Highway" through all 11 worlds across New Carrot City
 * - Handcrafted cartoon landmarks (City Gate, Cable Bridge, Cyber Towers, Colos-Spud,
 *   Onion Clock, Salsa Volcano, Carnival Tents, Crane Skyline, Cosmic Asteroid, 24K Golden Palace,
 *   and Animator's Inkwell Studio)
 * - Animated Bobs Bunny hopping from stage node to stage node along the road!
 * - Stage expedition info card with 1-tap "HOP IN & PLAY" launcher
 */
@Composable
fun CartoonWorldMapView(
    profile: PlayerProfileState,
    onSelectStage: (StageConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val stages = GameCatalog.stages
    var selectedStageId by remember { mutableIntStateOf(stages.first().id) }
    val selectedStage = stages.firstOrNull { it.id == selectedStageId } ?: stages.first()

    val mapScrollState = rememberScrollState()

    // Bobs Bunny hop animation along the road
    val bunnyHopAnim = remember { Animatable(0f) }
    LaunchedEffect(selectedStageId) {
        bunnyHopAnim.snapTo(0f)
        bunnyHopAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F0826), Color(0xFF1E103E), Color(0xFF080318))
                )
            )
    ) {
        // Top Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🗺️ THE ROAD TO THE GOLDEN WORLD",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
                Text(
                    text = "Follow the Great Carrot Highway • Tap any node to hop Bobs forward!",
                    color = Color(0xFF80DEEA),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF281F54))
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⭐ ${profile.totalStarsEarned} Stars",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
        }

        // Scrollable Cartoon Overworld Canvas (2400dp width highway panorama!)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .horizontalScroll(mapScrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(2600.dp)
                    .fillMaxSize()
            ) {
                val mapW = size.width
                val mapH = size.height
                val midY = mapH * 0.52f

                // 1. Overworld Biome Background Bands
                drawOverworldTerrainBands(mapW, mapH)

                // 2. The Great Carrot Highway Road
                drawWindingCarrotHighway(mapW, midY)

                // 3. World Landmarks along the Highway
                drawWorldLandmarks(mapW, midY)

                // 4. Stage Nodes along the Highway
                drawStageNodes(stages, profile, selectedStageId, mapW, midY)

                // 5. Animated Bobs Bunny on the Map
                val targetNodeX = getNodeX(selectedStage.id, stages.size, mapW)
                val targetNodeY = getNodeY(selectedStage.id, midY)
                val hopArc = sin(bunnyHopAnim.value * Math.PI.toFloat()) * 36f
                drawMapBobsBunny(targetNodeX, targetNodeY - 28f - hopArc)
            }

            // Clickable Hitboxes overlaid exactly on top of canvas stage nodes
            val stagesList = stages
            stagesList.forEach { stage ->
                val nodeXFraction = (stage.id - 1f) / (stagesList.size - 1f).coerceAtLeast(1f)
                val nx = 120f + nodeXFraction * 2360f
                val nyOffset = sin(stage.id * 0.95) * 85f

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(top = (240 + nyOffset).dp, start = (nx - 32f).dp)
                        .clip(CircleShape)
                        .clickable { selectedStageId = stage.id }
                )
            }
        }

        // Bottom Selected Stage Expedition Launcher Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF21C143B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${selectedStage.code} • ${selectedStage.title}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val stars = profile.stageStars[selectedStage.id] ?: 0
                        Text(
                            text = if (stars > 0) "⭐".repeat(stars) else "☆☆☆",
                            color = Color(0xFFFFD700),
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = selectedStage.subtitle,
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Par Time: ⏱️ ${selectedStage.parTimeSeconds.toInt()}s  •  Biome: ${selectedStage.biome.displayName}",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onSelectStage(selectedStage) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, Color(0xFF111111), RoundedCornerShape(14.dp))
                ) {
                    Text(
                        text = "HOP IN! 🥕",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// MAP CANVAS DRAWING ROUTINES
// =========================================================================

private fun getNodeX(stageId: Int, totalStages: Int, mapW: Float): Float {
    val fraction = (stageId - 1f) / (totalStages - 1f).coerceAtLeast(1f)
    return 120f + fraction * (mapW - 240f)
}

private fun getNodeY(stageId: Int, midY: Float): Float {
    return midY + sin(stageId * 0.95).toFloat() * 85f
}

/** Draws the 11-region atmospheric terrain gradients across the 2600px canvas */
private fun DrawScope.drawOverworldTerrainBands(mapW: Float, mapH: Float) {
    // Sky with stars and comic clouds
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0D0826), Color(0xFF1E1245), Color(0xFF2C1654))
        ),
        size = Size(mapW, mapH)
    )

    // Twinkling cartoon map stars
    for (i in 0..60) {
        val sx = (i * 44f) % mapW
        val sy = 20f + (i * 29) % (mapH * 0.35f).toInt()
        drawCircle(color = Color(0x88FFFFFF), radius = 2f, center = Offset(sx, sy))
    }

    // World Zone Ground Rolling Hills
    val groundPath = Path().apply {
        moveTo(0f, mapH * 0.65f)
        var x = 0f
        while (x <= mapW) {
            val hill = sin((x * 0.003f).toDouble()).toFloat() * 45f + cos((x * 0.007f).toDouble()).toFloat() * 25f
            lineTo(x, mapH * 0.65f + hill)
            x += 35f
        }
        lineTo(mapW, mapH)
        lineTo(0f, mapH)
        close()
    }

    drawPath(
        path = groundPath,
        brush = Brush.horizontalGradient(
            listOf(
                Color(0xFF2E7D32), // W1 Forest
                Color(0xFF37474F), // W2 San Francis-Kale
                Color(0xFF1A237E), // W3 Toky-Oat Cyber
                Color(0xFF8D6E63), // W4 Rome-Aine Colos
                Color(0xFF4A3B69), // W5 Lond-Onion
                Color(0xFFB71C1C), // W6 Vesuvi-Apple Volcano
                Color(0xFF00695C), // W7 Rio Jalapeño
                Color(0xFF37474F), // W8 Chicag-Artichoke
                Color(0xFF311B92), // W9 Cosmic Fortress
                Color(0xFFB8860B), // W10 The Golden World
                Color(0xFF880E4F)  // W11 Looney Multiverse
            )
        )
    )
    drawPath(groundPath, color = Color(0xFF111111), style = Stroke(width = 3.5f))
}

/** The Great Carrot Highway: winding cobblestone road curving through all 11 worlds */
private fun DrawScope.drawWindingCarrotHighway(mapW: Float, midY: Float) {
    val outline = Color(0xFF111111)

    // Road body curve
    val roadPath = Path().apply {
        var x = 60f
        moveTo(x, midY + sin(1f * 0.95).toFloat() * 85f)
        while (x <= mapW - 60f) {
            val stageIndex = (x / mapW) * 20f
            val ry = midY + sin((stageIndex * 0.95).toDouble()).toFloat() * 85f
            lineTo(x, ry)
            x += 20f
        }
    }

    // Wide road base (dirt/asphalt)
    drawPath(roadPath, color = Color(0xFFE65100), style = Stroke(width = 38f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(roadPath, color = Color(0xFFFFD54F), style = Stroke(width = 30f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(roadPath, color = outline, style = Stroke(width = 42f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(roadPath, color = Color(0xFFFFD54F), style = Stroke(width = 30f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Dashed center line
    var dx = 70f
    while (dx < mapW - 70f) {
        val sIdx = (dx / mapW) * 20f
        val dy = midY + sin((sIdx * 0.95).toDouble()).toFloat() * 85f
        drawLine(
            color = Color(0xFFFF6D00),
            start = Offset(dx, dy),
            end = Offset(dx + 16f, dy),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        dx += 34f
    }
}

/** Draws cartoon landmarks along the highway (City Gate, Castle, Volcano, Inkwell...) */
private fun DrawScope.drawWorldLandmarks(mapW: Float, midY: Float) {
    val outline = Color(0xFF111111)

    // W1 Landmark (x=220): Carrot City Gate & Windmill
    val w1X = 220f
    val w1Y = midY - 95f
    drawRoundRect(color = Color(0xFF4E342E), topLeft = Offset(w1X - 25f, w1Y), size = Size(50f, 65f), cornerRadius = CornerRadius(8f, 8f))
    drawRoundRect(color = outline, topLeft = Offset(w1X - 25f, w1Y), size = Size(50f, 65f), cornerRadius = CornerRadius(8f, 8f), style = Stroke(width = 3f))
    drawCircle(color = Color(0xFFFF9800), radius = 16f, center = Offset(w1X, w1Y - 18f))
    drawCircle(color = outline, radius = 16f, center = Offset(w1X, w1Y - 18f), style = Stroke(width = 3f))

    // W2 Landmark (x=460): San Francis-Kale Golden Cable Bridge
    val w2X = 460f
    val w2Y = midY - 100f
    drawRect(color = Color(0xFFFF3D00), topLeft = Offset(w2X - 24f, w2Y - 30f), size = Size(48f, 80f))
    drawRect(color = outline, topLeft = Offset(w2X - 24f, w2Y - 30f), size = Size(48f, 80f), style = Stroke(width = 3f))
    // Suspension cables
    drawLine(color = Color(0xFFFFD54F), start = Offset(w2X - 35f, w2Y + 30f), end = Offset(w2X, w2Y - 25f), strokeWidth = 2.5f)
    drawLine(color = Color(0xFFFFD54F), start = Offset(w2X + 35f, w2Y + 30f), end = Offset(w2X, w2Y - 25f), strokeWidth = 2.5f)

    // W3 Landmark (x=700): Toky-Oat Cyber Neon Pagoda
    val w3X = 700f
    val w3Y = midY - 110f
    drawRoundRect(color = Color(0xFF1A237E), topLeft = Offset(w3X - 22f, w3Y), size = Size(44f, 65f), cornerRadius = CornerRadius(6f, 6f))
    drawRoundRect(color = Color(0xFF00E5FF), topLeft = Offset(w3X - 30f, w3Y - 10f), size = Size(60f, 14f), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = outline, topLeft = Offset(w3X - 30f, w3Y - 10f), size = Size(60f, 14f), cornerRadius = CornerRadius(4f, 4f), style = Stroke(width = 2.8f))
    drawCircle(color = Color(0xFFFF007F), radius = 6f, center = Offset(w3X, w3Y - 18f))

    // W4 Landmark (x=940): Rome-Aine Colos-Spud Stone Arches
    val w4X = 940f
    val w4Y = midY - 90f
    drawRoundRect(color = Color(0xFF8D6E63), topLeft = Offset(w4X - 28f, w4Y), size = Size(56f, 55f), cornerRadius = CornerRadius(8f, 8f))
    for (a in 0..2) {
        drawRoundRect(color = Color(0xFF3E2723), topLeft = Offset(w4X - 20f + a * 15f, w4Y + 18f), size = Size(10f, 26f), cornerRadius = CornerRadius(5f, 5f))
    }
    drawRoundRect(color = outline, topLeft = Offset(w4X - 28f, w4Y), size = Size(56f, 55f), cornerRadius = CornerRadius(8f, 8f), style = Stroke(width = 3f))

    // W5 Landmark (x=1180): Lond-Onion Big Ben Clock Tower
    val w5X = 1180f
    val w5Y = midY - 115f
    drawRect(color = Color(0xFF5D4037), topLeft = Offset(w5X - 16f, w5Y), size = Size(32f, 75f))
    drawCircle(color = Color(0xFFFFD54F), radius = 10f, center = Offset(w5X, w5Y + 18f))
    drawCircle(color = outline, radius = 10f, center = Offset(w5X, w5Y + 18f), style = Stroke(width = 2f))
    drawRect(color = outline, topLeft = Offset(w5X - 16f, w5Y), size = Size(32f, 75f), style = Stroke(width = 3f))

    // W6 Landmark (x=1420): Mount Vesuvi-Apple Smoking Volcano
    val w6X = 1420f
    val w6Y = midY - 105f
    val volcanoPath = Path().apply {
        moveTo(w6X - 45f, w6Y + 70f)
        lineTo(w6X - 16f, w6Y)
        lineTo(w6X + 16f, w6Y)
        lineTo(w6X + 45f, w6Y + 70f)
        close()
    }
    drawPath(volcanoPath, color = Color(0xFFD50000))
    drawPath(volcanoPath, color = outline, style = Stroke(width = 3.5f))
    // Bubbling salsa lava crest
    drawCircle(color = Color(0xFFFFEA00), radius = 14f, center = Offset(w6X, w6Y))
    drawCircle(color = outline, radius = 14f, center = Offset(w6X, w6Y), style = Stroke(width = 2.5f))

    // W7 Landmark (x=1660): Rio Jalapeño Carnival Big-Top Tent
    val w7X = 1660f
    val w7Y = midY - 100f
    val tentPath = Path().apply {
        moveTo(w7X - 34f, w7Y + 60f)
        lineTo(w7X, w7Y)
        lineTo(w7X + 34f, w7Y + 60f)
        close()
    }
    drawPath(tentPath, color = Color(0xFFFF1744))
    drawPath(tentPath, color = outline, style = Stroke(width = 3f))
    drawLine(color = Color(0xFFFFD600), start = Offset(w7X - 12f, w7Y + 60f), end = Offset(w7X, w7Y), strokeWidth = 5f)
    drawLine(color = Color(0xFFFFD600), start = Offset(w7X + 12f, w7Y + 60f), end = Offset(w7X, w7Y), strokeWidth = 5f)

    // W8 Landmark (x=1900): Chicag-Artichoke Art-Deco High-Rise
    val w8X = 1900f
    val w8Y = midY - 120f
    drawRect(color = Color(0xFF37474F), topLeft = Offset(w8X - 20f, w8Y), size = Size(40f, 85f))
    drawRect(color = Color(0xFF00E5FF), topLeft = Offset(w8X - 14f, w8Y + 12f), size = Size(28f, 10f))
    drawRect(color = Color(0xFF00E5FF), topLeft = Offset(w8X - 14f, w8Y + 32f), size = Size(28f, 10f))
    drawRect(color = outline, topLeft = Offset(w8X - 20f, w8Y), size = Size(40f, 85f), style = Stroke(width = 3f))

    // W9 Landmark (x=2120): Cosmic Fortress & Saturn Rings
    val w9X = 2120f
    val w9Y = midY - 110f
    drawCircle(color = Color(0xFF7C4DFF), radius = 22f, center = Offset(w9X, w9Y + 20f))
    drawOval(color = Color(0xFFFFD700), topLeft = Offset(w9X - 35f, w9Y + 14f), size = Size(70f, 14f), style = Stroke(width = 3.5f))
    drawCircle(color = outline, radius = 22f, center = Offset(w9X, w9Y + 20f), style = Stroke(width = 3f))

    // W10 Landmark (x=2340): The 24-Karat Golden Castle & Spire
    val w10X = 2340f
    val w10Y = midY - 120f
    drawRoundRect(color = Color(0xFFFFD700), topLeft = Offset(w10X - 35f, w10Y), size = Size(70f, 85f), cornerRadius = CornerRadius(10f, 10f))
    drawRoundRect(color = outline, topLeft = Offset(w10X - 35f, w10Y), size = Size(70f, 85f), cornerRadius = CornerRadius(10f, 10f), style = Stroke(width = 3.5f))
    drawCircle(color = Color(0xFF00E5FF), radius = 10f, center = Offset(w10X, w10Y - 15f))
    drawCircle(color = outline, radius = 10f, center = Offset(w10X, w10Y - 15f), style = Stroke(width = 2.5f))

    // W11 Landmark (x=2520): The Animator's Giant Spilled Inkwell & Quill
    val w11X = 2520f
    val w11Y = midY - 110f
    drawRoundRect(color = Color(0xFF212121), topLeft = Offset(w11X - 28f, w11Y), size = Size(56f, 65f), cornerRadius = CornerRadius(12f, 12f))
    drawRoundRect(color = Color(0xFF00E676), topLeft = Offset(w11X - 22f, w11Y + 12f), size = Size(44f, 12f), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = outline, topLeft = Offset(w11X - 28f, w11Y), size = Size(56f, 65f), cornerRadius = CornerRadius(12f, 12f), style = Stroke(width = 3.5f))
    // Giant white feather quill
    drawLine(color = Color.White, start = Offset(w11X + 10f, w11Y + 10f), end = Offset(w11X + 32f, w11Y - 35f), strokeWidth = 5f, cap = StrokeCap.Round)
    drawLine(color = outline, start = Offset(w11X + 10f, w11Y + 10f), end = Offset(w11X + 32f, w11Y - 35f), strokeWidth = 1.8f, cap = StrokeCap.Round)
}

/** Draws circular stage nodes along the road with stars, boss badges and clear states */
private fun DrawScope.drawStageNodes(
    stages: List<StageConfig>,
    profile: PlayerProfileState,
    selectedId: Int,
    mapW: Float,
    midY: Float
) {
    val outline = Color(0xFF111111)

    stages.forEach { stage ->
        val x = getNodeX(stage.id, stages.size, mapW)
        val y = getNodeY(stage.id, midY)
        val isSelected = stage.id == selectedId
        val stars = profile.stageStars[stage.id] ?: 0
        val isBoss = stage.isBossStage || stage.isBossRush

        val nodeColor = when {
            stage.isBossRush -> Color(0xFFFF4081)
            stage.worldNumber == 11 -> Color(0xFFE040FB)
            stage.worldNumber == 10 || stage.isGoldenRift -> Color(0xFFFFD700)
            isBoss -> Color(0xFFFF1744)
            stage.isHeistStage -> Color(0xFF00E676)
            else -> Color(0xFF00E5FF)
        }

        // Selected halo pulse
        if (isSelected) {
            drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 34f, center = Offset(x, y))
            drawCircle(color = Color(0xFFFFD700), radius = 30f, center = Offset(x, y), style = Stroke(width = 4f))
        }

        // Node circle
        val r = if (isBoss) 24f else 20f
        drawCircle(color = nodeColor, radius = r, center = Offset(x, y))
        drawCircle(color = outline, radius = r, center = Offset(x, y), style = Stroke(width = 3.5f))

        // White gloss highlight
        drawCircle(color = Color.White.copy(alpha = 0.75f), radius = r * 0.45f, center = Offset(x - 5f, y - 5f))

        // Boss skull / Crown marker
        if (isBoss) {
            drawCircle(color = Color(0xFF111111), radius = 6f, center = Offset(x, y))
        }

        // Stars earned badge below node
        if (stars > 0) {
            drawRoundRect(
                color = Color(0xCC111111),
                topLeft = Offset(x - 18f, y + r + 4f),
                size = Size(36f, 14f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            for (st in 0 until stars) {
                drawCircle(color = Color(0xFFFFD700), radius = 3f, center = Offset(x - 9f + st * 9f, y + r + 11f))
            }
        }
    }
}

/** Draws Bobs Bunny hopping on the selected world map node with high-fidelity cartoon flair */
private fun DrawScope.drawMapBobsBunny(x: Float, y: Float) {
    val outline = Color(0xFF111111)

    // Contact drop shadow on the road
    drawOval(
        color = Color(0x77000000),
        topLeft = Offset(x - 24f, y + 24f),
        size = Size(48f, 14f)
    )

    // Fluffy 3-lobe Cottontail at back
    drawCircle(color = Color.White, radius = 7.5f, center = Offset(x - 19f, y - 4f))
    drawCircle(color = Color.White, radius = 5.5f, center = Offset(x - 22f, y - 7f))
    drawCircle(color = outline, radius = 7.5f, center = Offset(x - 19f, y - 4f), style = Stroke(width = 2.2f))

    // Feet / High-Top Sneakers with white rubber toe-caps and soles
    drawRoundRect(color = Color(0xFFFF1744), topLeft = Offset(x - 18f, y + 14f), size = Size(16f, 12f), cornerRadius = CornerRadius(5f, 5f))
    drawRect(color = Color.White, topLeft = Offset(x - 18f, y + 22f), size = Size(16f, 4f))
    drawCircle(color = Color.White, radius = 2.2f, center = Offset(x - 13f, y + 18f))
    drawRoundRect(color = outline, topLeft = Offset(x - 18f, y + 14f), size = Size(16f, 12f), cornerRadius = CornerRadius(5f, 5f), style = Stroke(width = 2.4f))

    drawRoundRect(color = Color(0xFFFF1744), topLeft = Offset(x + 2f, y + 14f), size = Size(16f, 12f), cornerRadius = CornerRadius(5f, 5f))
    drawRect(color = Color.White, topLeft = Offset(x + 2f, y + 22f), size = Size(16f, 4f))
    drawCircle(color = Color.White, radius = 2.2f, center = Offset(x + 7f, y + 18f))
    drawRoundRect(color = outline, topLeft = Offset(x + 2f, y + 14f), size = Size(16f, 12f), cornerRadius = CornerRadius(5f, 5f), style = Stroke(width = 2.4f))

    // White Cartoon 4-Fingered Gloves with pintuck darts
    for (gx in listOf(x - 19f, x + 19f)) {
        drawCircle(color = Color.White, radius = 7.5f, center = Offset(gx, y - 2f))
        drawCircle(color = outline, radius = 7.5f, center = Offset(gx, y - 2f), style = Stroke(width = 2.2f))
        drawLine(color = outline, start = Offset(gx - 2f, y - 5f), end = Offset(gx - 2f, y), strokeWidth = 1.2f)
        drawLine(color = outline, start = Offset(gx + 2f, y - 5f), end = Offset(gx + 2f, y), strokeWidth = 1.2f)
    }

    // Torso / Streetwear Orange Hoodie with Cream Belly & Carrot Emblem
    drawRoundRect(color = Color(0xFFFF6D00), topLeft = Offset(x - 16f, y - 14f), size = Size(32f, 30f), cornerRadius = CornerRadius(10f, 10f))
    drawOval(color = Color(0xFFFFF9C4), topLeft = Offset(x - 10f, y - 9f), size = Size(20f, 20f))
    drawCircle(color = Color(0xFFFF6D00), radius = 2.5f, center = Offset(x, y + 1f))
    drawRoundRect(color = outline, topLeft = Offset(x - 16f, y - 14f), size = Size(32f, 30f), cornerRadius = CornerRadius(10f, 10f), style = Stroke(width = 2.8f))

    // Head with cheek fluff ruffs
    drawCircle(color = Color.White, radius = 6f, center = Offset(x - 17f, y - 24f))
    drawCircle(color = Color.White, radius = 6f, center = Offset(x + 17f, y - 24f))
    drawCircle(color = Color.White, radius = 18f, center = Offset(x, y - 28f))
    drawCircle(color = outline, radius = 18f, center = Offset(x, y - 28f), style = Stroke(width = 2.8f))

    // Sculpted W-Muzzle & Rosy Cheeks
    drawOval(color = Color.White, topLeft = Offset(x - 15f, y - 27f), size = Size(16f, 12f))
    drawOval(color = Color.White, topLeft = Offset(x - 1f, y - 27f), size = Size(16f, 12f))
    drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 4f, center = Offset(x - 9f, y - 22f))
    drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.6f), radius = 4f, center = Offset(x + 9f, y - 22f))
    // Pink nose
    drawCircle(color = Color(0xFFFF4081), radius = 2.8f, center = Offset(x, y - 25f))

    // Articulated Cubic-Bezier Bunny Ears with Bent Tips!
    val leftEar = Path().apply {
        moveTo(x - 10f, y - 44f)
        cubicTo(x - 16f, y - 58f, x - 14f, y - 72f, x - 6f, y - 74f)
        cubicTo(x - 2f, y - 70f, x - 3f, y - 56f, x - 4f, y - 44f)
        close()
    }
    drawPath(leftEar, color = Color.White)
    drawRoundRect(color = Color(0xFFFF80AB), topLeft = Offset(x - 10f, y - 66f), size = Size(4.5f, 18f), cornerRadius = CornerRadius(2f, 2f))
    drawPath(leftEar, color = outline, style = Stroke(width = 2.5f, join = StrokeJoin.Round))

    val rightEar = Path().apply {
        moveTo(x + 4f, y - 44f)
        cubicTo(x + 3f, y - 56f, x + 3f, y - 70f, x + 11f, y - 73f)
        cubicTo(x + 18f, y - 68f, x + 16f, y - 56f, x + 10f, y - 44f)
        close()
    }
    drawPath(rightEar, color = Color.White)
    drawRoundRect(color = Color(0xFFFF80AB), topLeft = Offset(x + 5.5f, y - 66f), size = Size(4.5f, 18f), cornerRadius = CornerRadius(2f, 2f))
    drawPath(rightEar, color = outline, style = Stroke(width = 2.5f, join = StrokeJoin.Round))

    // Cool Swag Shades with Diagonal Glint
    drawRoundRect(color = Color(0xFF111111), topLeft = Offset(x - 14f, y - 34f), size = Size(28f, 10f), cornerRadius = CornerRadius(4f, 4f))
    drawRoundRect(color = outline, topLeft = Offset(x - 14f, y - 34f), size = Size(28f, 10f), cornerRadius = CornerRadius(4f, 4f), style = Stroke(width = 2f))
    drawLine(color = Color.White, start = Offset(x - 9f, y - 26f), end = Offset(x - 5f, y - 32f), strokeWidth = 2f, cap = StrokeCap.Round)

    // Buck Teeth Smile & Carrot Toothpick
    drawRect(color = Color.White, topLeft = Offset(x - 4f, y - 20f), size = Size(8f, 5f))
    drawRect(color = outline, topLeft = Offset(x - 4f, y - 20f), size = Size(8f, 5f), style = Stroke(width = 1.2f))
    drawLine(
        color = Color(0xFFFF9100),
        start = Offset(x + 3f, y - 18f),
        end = Offset(x + 15f, y - 21f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    drawCircle(color = Color(0xFF00E676), radius = 2.8f, center = Offset(x + 16f, y - 21f))

    // Cartoon Thought/Speech Bubble floating above Bobs!
    val bubbleX = x
    val bubbleY = y - 88f
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(bubbleX - 38f, bubbleY - 14f),
        size = Size(76f, 24f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = outline,
        topLeft = Offset(bubbleX - 38f, bubbleY - 14f),
        size = Size(76f, 24f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 2.2f)
    )
    // Little pointer triangle
    val ptrPath = Path().apply {
        moveTo(bubbleX - 4f, bubbleY + 10f)
        lineTo(bubbleX, bubbleY + 16f)
        lineTo(bubbleX + 4f, bubbleY + 10f)
        close()
    }
    drawPath(ptrPath, color = Color.White)
    drawPath(ptrPath, color = outline, style = Stroke(width = 1.8f))
}

@Preview(showBackground = true, widthDp = 800, heightDp = 450)
@Composable
private fun CartoonWorldMapViewPreview() {
    CartoonWorldMapView(
        profile = PlayerProfileState(),
        onSelectStage = {}
    )
}
