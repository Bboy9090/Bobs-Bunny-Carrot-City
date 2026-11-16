package com.example.myapplication.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.data.PlayerProfileState
import com.example.myapplication.game.CartoonStageEngine
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.ui.game3d.Cartoon3DDioramaView
import com.example.myapplication.model.StageConfig
import com.example.myapplication.ui.game.CartoonStageCanvas

@Composable
fun GamePlayScreen(
    stageConfig: StageConfig,
    profile: PlayerProfileState,
    soundEngine: CartoonSoundEngine,
    onStageCompleted: (carrotsEarned: Int, shardsEarned: Int, starsEarned: Int, clearTimeSeconds: Float) -> Unit,
    onExitToMenu: () -> Unit
) {
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }
    val activeHero = profile.selectedHero
    val activeMutator = profile.activeMutator

    val headgear = remember(profile.equippedHeadgearId) {
        GameCatalog.swagItems.firstOrNull { it.id == profile.equippedHeadgearId }
            ?: GameCatalog.swagItems.first { it.id == "head_classic_shades" }
    }
    val hoodie = remember(profile.equippedHoodieId) {
        GameCatalog.swagItems.firstOrNull { it.id == profile.equippedHoodieId }
            ?: GameCatalog.swagItems.first { it.id == "hoodie_orange_drip" }
    }
    val kicks = remember(profile.equippedKicksId) {
        GameCatalog.swagItems.firstOrNull { it.id == profile.equippedKicksId }
            ?: GameCatalog.swagItems.first { it.id == "kicks_high_tops" }
    }
    val trail = remember(profile.equippedTrailId) {
        GameCatalog.swagItems.firstOrNull { it.id == profile.equippedTrailId }
            ?: GameCatalog.swagItems.first { it.id == "trail_stardust" }
    }
    val badge = remember(profile.equippedBadgeId) {
        GameCatalog.swagItems.firstOrNull { it.id == profile.equippedBadgeId }
            ?: GameCatalog.swagItems.first { it.id == "badge_coyote_plus" }
    }

    var attemptKey by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var use3DDiorama by remember(stageConfig.id) { mutableStateOf(stageConfig.code == "W1-1") }
    var rewardCommitted by remember(stageConfig.id, attemptKey) { mutableStateOf(false) }

    val engine = remember(stageConfig, attemptKey, activeHero.id, activeMutator.id) {
        CartoonStageEngine(
            stageConfig = stageConfig,
            selectedHero = activeHero,
            weatherMutator = activeMutator,
            equippedBadgeId = badge.id,
            trailColor = trail.primaryColor,
            soundEngine = soundEngine
        )
    }

    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    // 60 FPS Game Loop
    LaunchedEffect(engine, isPaused) {
        var lastFrameNanos = withFrameNanos { it }
        while (!isPaused && !engine.isStageCleared && !engine.isStageFailed) {
            val now = withFrameNanos { it }
            val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.04f)
            lastFrameNanos = now
            engine.stepFrame(dt)
        }
    }

    // Commit rewards once when cleared + HD Haptic Rumble
    LaunchedEffect(engine.isStageCleared) {
        if (engine.isStageCleared && !rewardCommitted) {
            rewardCommitted = true
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onStageCompleted(
                engine.collectedCarrots,
                engine.collectedGoldenShards,
                engine.calculateStarsEarned(),
                engine.stageTimeSeconds
            )
        }
    }

    val progressFraction = (engine.playerX / engine.stageData.goalX).coerceIn(0f, 1f)
    val bossEntity = engine.stageData.enemies.firstOrNull { it.kind.isBoss && it.bossHp > 0 }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val isDown = event.type == KeyEventType.KeyDown
                when (event.key) {
                    Key.DirectionLeft, Key.A -> {
                        engine.inputLeft = isDown
                        true
                    }
                    Key.DirectionRight, Key.D -> {
                        engine.inputRight = isDown
                        true
                    }
                    Key.DirectionDown, Key.S, Key.ButtonR2 -> {
                        engine.inputBurrowHeld = isDown
                        true
                    }
                    Key.Spacebar, Key.DirectionUp, Key.W, Key.ButtonA -> {
                        engine.inputJumpHeld = isDown
                        if (isDown) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            engine.triggerJump()
                        }
                        true
                    }
                    Key.Enter, Key.X, Key.ButtonX, Key.ButtonB -> {
                        if (isDown) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            engine.triggerTrollDashOrAction()
                        }
                        true
                    }
                    Key.P, Key.ButtonY -> {
                        if (isDown) engine.toggleSidekickDrone()
                        true
                    }
                    else -> false
                }
            }
    ) {
        // 1. Live 3D Diorama (W1-1 Vertical Slice) or 2.5D Cartoon Stage Canvas
        if (use3DDiorama) {
            Cartoon3DDioramaView(engine = engine)
        } else {
            CartoonStageCanvas(
                engine = engine,
                headgear = headgear,
                hoodie = hoodie,
                kicks = kicks
            )
        }

        // 2. Top HUD Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xE6212121), Color(0xCC0D0D18))
                            )
                        )
                        .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "${stageConfig.code} • ${stageConfig.title} (${activeHero.displayName})",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${engine.healthState.displayName} • ${activeMutator.emoji} ${activeMutator.title} • ⏱️ ${"%.1f".format(engine.stageTimeSeconds)}s",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xE6212121), Color(0xCC0D0D18))
                            )
                        )
                        .border(2.5.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🥕 ${engine.collectedCarrots}",
                        color = Color(0xFFFF9100),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "🌟 ${engine.collectedGoldenShards}",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${engine.swagMultiplier}x SWAG",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { use3DDiorama = !use3DDiorama },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (use3DDiorama) Color(0xFF00E5FF) else Color(0xFF455A64)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = if (use3DDiorama) "3D" else "2D",
                            fontWeight = FontWeight.Black,
                            color = if (use3DDiorama) Color.Black else Color.White
                        )
                    }
                    Button(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.5.dp, Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Text(if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stage Progress Bar + Swag Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "World ${stageConfig.worldNumber} Progress (${(progressFraction * 100).toInt()}%) • ${badge.badgePerkDescription}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0x55FFFFFF)
                    )
                }
                Column(modifier = Modifier.width(120.dp)) {
                    Text(
                        text = "Swag Overdrive",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { engine.swagMeter },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00E5FF),
                        trackColor = Color(0x55FFFFFF)
                    )
                }
            }

            engine.activePowerUp?.let { power ->
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(power.color.copy(alpha = 0.9f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${power.emoji} ${power.title} (${"%.1f".format(engine.powerUpRemainingSeconds)}s)",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }

            // Phase 5: Holo-Ghost Speedrun Rival tracker
            Spacer(modifier = Modifier.height(4.dp))
            val ghostLead = engine.playerX - engine.ghostRivalX
            Text(
                text = if (ghostLead >= 0f) {
                    "👻 Holo-Ghost: You lead by ${ghostLead.toInt()}u — keep it for +25 🥕!"
                } else {
                    "👻 Holo-Ghost ahead by ${(-ghostLead).toInt()}u — catch it before Par Time!"
                },
                color = if (ghostLead >= 0f) Color(0xFF00E676) else Color(0xFFFF80AB),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            if (bossEntity != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xDD880E4F))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "BOSS: ${bossEntity.kind.title} (${bossEntity.bossHp}/${bossEntity.bossMaxHp} HP)",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    LinearProgressIndicator(
                        progress = { bossEntity.bossHp.toFloat() / bossEntity.bossMaxHp.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFFEA00),
                        trackColor = Color(0x55000000)
                    )
                }
            }

            if (engine.quipTimer > 0f) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFFFDE7), Color(0xFFFFF9C4))
                            )
                        )
                        .border(3.dp, Color(0xFFFF6D00), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "💬 ${activeHero.displayName}: \"${engine.bobsQuip}\"",
                        color = Color(0xFF1B0000),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.5.sp
                    )
                }
            }

            engine.floatingTexts.lastOrNull()?.let { callout ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = callout.text,
                    color = callout.color,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }

        // 3. Bottom Two-Thumb Arcade Controls + HD Rumble Haptics
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ArcadeHoldButton(
                    label = "◀",
                    subtitle = "LEFT",
                    color = Color(0xCC263238),
                    onPressChanged = { engine.inputLeft = it }
                )
                ArcadeHoldButton(
                    label = "🕳️",
                    subtitle = "BURROW",
                    color = Color(0xCC795548),
                    onPressChanged = {
                        if (it) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        engine.inputBurrowHeld = it
                    }
                )
                ArcadeHoldButton(
                    label = "▶",
                    subtitle = "RIGHT",
                    color = Color(0xCC263238),
                    onPressChanged = { engine.inputRight = it }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ArcadeHoldButton(
                    label = "🤖",
                    subtitle = if (engine.sidekickDroneEnabled) "P2 ON" else "P2 OFF",
                    color = Color(0xCC00838F),
                    onPressChanged = { pressed ->
                        if (pressed) {
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            engine.toggleSidekickDrone()
                        }
                    }
                )
                ArcadeHoldButton(
                    label = "🌈",
                    subtitle = activeHero.specialMoveLabel,
                    color = Color(0xCCFF4081),
                    onPressChanged = { pressed ->
                        if (pressed) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            engine.triggerTrollDashOrAction()
                        }
                    }
                )
                ArcadeHoldButton(
                    label = "🥕⬆",
                    subtitle = "HOP / EARS",
                    color = Color(0xCC00E676),
                    onPressChanged = { pressed ->
                        engine.inputJumpHeld = pressed
                        if (pressed) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            engine.triggerJump()
                        }
                    }
                )
            }
        }

        // 4. Pause / Stage Cleared / Stage Failed Modal Overlay (Looney Tunes Bullseye Iris!)
        if (isPaused || engine.isStageCleared || engine.isStageFailed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000)),
                contentAlignment = Alignment.Center
            ) {
                // Classic Looney Tunes Concentric Bullseye Iris Rings
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val maxR = size.width.coerceAtLeast(size.height) * 0.75f
                    val ringColors = listOf(
                        Color(0xFF880E4F),
                        Color(0xFFC2185B),
                        Color(0xFFD50000),
                        Color(0xFFFF1744),
                        Color(0xFFFF5252),
                        Color(0xFFFF8A80)
                    )
                    for (i in ringColors.indices.reversed()) {
                        val radius = maxR * ((i + 1f) / ringColors.size.toFloat())
                        drawCircle(
                            color = ringColors[i].copy(alpha = 0.35f),
                            radius = radius,
                            center = centerOffset
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .padding(16.dp)
                        .border(3.5.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF21E1B2E))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val title = when {
                            engine.isStageCleared && stageConfig.isBossRush -> "🏆 GRAND BOSS RUSH PANTHEON CONQUERED!"
                            engine.isStageCleared && stageConfig.worldNumber == 11 -> "🎨 LOONEY MULTIVERSE CONQUERED! THAT'S ALL, FOXES!"
                            engine.isStageCleared && stageConfig.worldNumber == 10 -> "👑 24-KARAT GOLDEN WORLD CLEARED!"
                            engine.isStageCleared -> "🎉 WORLD ${stageConfig.worldNumber} CLEARED! THAT'S ALL, FOXES!"
                            engine.isStageFailed -> "🪗 OOPS! OUT OF SWAG!"
                            else -> "⏸️ PAUSED IN ${stageConfig.title.uppercase()}"
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stageConfig.recommendedHint,
                            color = Color(0xFFB0BEC5),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Carrots: 🥕 ${engine.collectedCarrots}  •  Shards: 🌟 ${engine.collectedGoldenShards}  •  Time: ⏱️ ${"%.1f".format(engine.stageTimeSeconds)}s",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (engine.isStageCleared) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val stars = engine.calculateStarsEarned()
                            val beatPar = engine.stageTimeSeconds <= stageConfig.parTimeSeconds
                            Text(
                                text = "Mastery: " + "⭐".repeat(stars) + if (beatPar) " (⚡ BEAT PAR & OUTRAN THE HOLO-GHOST!)" else "",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (isPaused && !engine.isStageCleared && !engine.isStageFailed) {
                                Button(
                                    onClick = { isPaused = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                    modifier = Modifier.border(2.dp, Color(0xFF111111), RoundedCornerShape(12.dp))
                                ) {
                                    Text("Keep Hopping", color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                            Button(
                                onClick = {
                                    isPaused = false
                                    attemptKey++
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                                modifier = Modifier.border(2.dp, Color(0xFF111111), RoundedCornerShape(12.dp))
                            ) {
                                Text("Replay Stage", color = Color.Black, fontWeight = FontWeight.Black)
                            }
                            OutlinedButton(
                                onClick = onExitToMenu,
                                modifier = Modifier.border(2.dp, Color.White, RoundedCornerShape(12.dp))
                            ) {
                                Text("World Hub", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArcadeHoldButton(
    label: String,
    subtitle: String,
    color: Color,
    onPressChanged: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFF8F00))
                    } else {
                        listOf(color.copy(alpha = 0.95f), color, color.copy(alpha = 0.75f))
                    }
                )
            )
            .border(3.5.dp, if (isPressed) Color(0xFF111111) else Color(0xEEFFFFFF), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChanged(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChanged(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Comic Button Top Glint Arc
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(width = 38.dp, height = 10.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (isPressed) 0.5f else 0.32f))
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                color = if (isPressed) Color.Black else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = subtitle,
                color = if (isPressed) Color.Black else Color.White.copy(alpha = 0.92f),
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
