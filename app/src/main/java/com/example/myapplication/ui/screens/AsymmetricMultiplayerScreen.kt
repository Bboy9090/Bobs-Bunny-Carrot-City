package com.example.myapplication.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.audio.CartoonSoundEngine
import kotlin.math.abs
import kotlin.random.Random

private data class StreetTrap(
    val lane: Int, // 0, 1, 2
    var xProgress: Float, // 0f..1f across the street
    val kindName: String,
    val color: Color
)

@Composable
fun AsymmetricMultiplayerScreen(
    soundEngine: CartoonSoundEngine,
    onMatchReward: (carrots: Int, won: Boolean) -> Unit,
    onBack: () -> Unit
) {
    // 0 = Bobs Runner (1v3), 1 = Fox Syndicate Trappers, 2 = Couch + Phone Cross-Play Room
    var modeIndex by remember { mutableIntStateOf(0) }
    val playingAsBobs = modeIndex == 0
    val roomCode = remember { "TV-${Random.nextInt(1000, 9999)}" }

    var bobsLane by remember { mutableIntStateOf(1) }
    var bobsCarrots by remember { mutableIntStateOf(0) }
    var foxCatches by remember { mutableIntStateOf(0) }
    var timeRemaining by remember { mutableFloatStateOf(45f) }
    var decoyTimer by remember { mutableFloatStateOf(0f) }
    var matchEnded by remember { mutableStateOf(false) }
    var statusBanner by remember {
        mutableStateOf("Phase 3 Party Arena! Play as Bobs Runner, Fox Syndicate, or Couch + Phone Cross-Play!")
    }

    val activeTraps = remember { mutableStateListOf<StreetTrap>() }
    val laneCarrotX = remember { mutableStateListOf(0.45f, 0.72f, 0.58f) }

    fun resetMatch(newMode: Int) {
        modeIndex = newMode
        bobsLane = 1
        bobsCarrots = 0
        foxCatches = 0
        timeRemaining = 45f
        decoyTimer = 0f
        matchEnded = false
        activeTraps.clear()
        statusBanner = when (newMode) {
            0 -> "Role: BOBS BUNNY (Runner) • Hop lanes & deploy Holo-Decoys to dodge Fox traps!"
            1 -> "Role: FOX SYNDICATE TRAPPER (1v3) • Drop Anvils, Nets & Grease in Bobs's lane!"
            else -> "📺📱 COUCH + PHONE LINK ($roomCode): TV Player runs as Bobs while Mobile Trappers tap lanes!"
        }
    }

    LaunchedEffect(modeIndex, matchEnded) {
        var lastNanos = withFrameNanos { it }
        var aiSpawnTimer = 0f
        var aiBobsMoveTimer = 0f

        while (!matchEnded) {
            val now = withFrameNanos { it }
            val dt = ((now - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
            lastNanos = now

            timeRemaining = (timeRemaining - dt).coerceAtLeast(0f)
            if (decoyTimer > 0f) decoyTimer = (decoyTimer - dt).coerceAtLeast(0f)

            if (modeIndex == 0) {
                aiSpawnTimer += dt
                if (aiSpawnTimer >= 1.15f) {
                    aiSpawnTimer = 0f
                    val lane = Random.nextInt(0, 3)
                    val trapNames = listOf("Bonk Anvil 🔨", "Butterfly Net 🕸️", "Salsa Slick 🌶️")
                    val trapColors = listOf(Color(0xFFFFD600), Color(0xFF00E5FF), Color(0xFFFF1744))
                    val idx = Random.nextInt(0, 3)
                    activeTraps.add(StreetTrap(lane, 1.0f, trapNames[idx], trapColors[idx]))
                }
            } else if (modeIndex == 1) {
                aiBobsMoveTimer += dt
                if (aiBobsMoveTimer >= 0.65f) {
                    aiBobsMoveTimer = 0f
                    val dangerInCurrentLane = activeTraps.any { it.lane == bobsLane && it.xProgress in 0.18f..0.48f }
                    if (dangerInCurrentLane) {
                        bobsLane = (bobsLane + Random.nextInt(1, 3)) % 3
                    } else if (Random.nextBoolean()) {
                        bobsLane = Random.nextInt(0, 3)
                    }
                }
            }

            // Move carrots & check Bobs collection
            for (lane in 0..2) {
                laneCarrotX[lane] = laneCarrotX[lane] - dt * 0.34f
                if (laneCarrotX[lane] <= 0.22f && bobsLane == lane) {
                    bobsCarrots += 2
                    soundEngine.playCarrotPickup()
                    laneCarrotX[lane] = 0.95f
                } else if (laneCarrotX[lane] < 0.05f) {
                    laneCarrotX[lane] = 0.95f
                }
            }

            // Move traps & check collisions
            val iter = activeTraps.iterator()
            while (iter.hasNext()) {
                val trap = iter.next()
                trap.xProgress -= dt * 0.44f
                if (abs(trap.xProgress - 0.2f) < 0.045f && trap.lane == bobsLane) {
                    if (decoyTimer > 0f) {
                        statusBanner = "🎭 HOLO-BUNNY DECOY FOOLED THE FOX TRAP!"
                        soundEngine.playBoing()
                    } else {
                        foxCatches++
                        soundEngine.playBonk()
                        statusBanner = "🪗 BONK! ${trap.kindName} caught Bobs! ($foxCatches/4 Catches)"
                    }
                    iter.remove()
                } else if (trap.xProgress <= 0f) {
                    iter.remove()
                }
            }

            // Check win/loss conditions
            if (bobsCarrots >= 25 || foxCatches >= 4 || timeRemaining <= 0f) {
                matchEnded = true
                val playerWon = if (playingAsBobs || modeIndex == 2) {
                    bobsCarrots >= 25 && foxCatches < 4
                } else {
                    foxCatches >= 4 || (timeRemaining <= 0f && bobsCarrots < 25)
                }
                if (playerWon) soundEngine.playVictoryFanfare() else soundEngine.playBonk()
                onMatchReward(if (playerWon) 50 else 20, playerWon)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF120D26))
            .systemBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header & Role Switcher
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🦊 FOX HUNT vs. HARE STUNT (1v3 & COUCH-TO-PHONE)",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
                OutlinedButton(onClick = onBack) {
                    Text("Hub", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusBanner,
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { resetMatch(0) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modeIndex == 0) Color(0xFF00E676) else Color(0xFF263238)
                    )
                ) {
                    Text(
                        "🐰 Bobs Runner",
                        color = if (modeIndex == 0) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { resetMatch(1) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modeIndex == 1) Color(0xFFFF4081) else Color(0xFF263238)
                    )
                ) {
                    Text(
                        "🦊 Fox Trapper",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { resetMatch(2) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (modeIndex == 2) Color(0xFFFFD700) else Color(0xFF263238)
                    )
                ) {
                    Text(
                        "📺📱 Couch+Phone ($roomCode)",
                        color = if (modeIndex == 2) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // 3-Lane Asymmetric Street Arena Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1938))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val laneHeight = size.height / 3f
                for (lane in 0..2) {
                    val topY = lane * laneHeight
                    drawRect(
                        color = if (lane % 2 == 0) Color(0xFF251F47) else Color(0xFF1B1635),
                        topLeft = Offset(0f, topY),
                        size = Size(size.width, laneHeight)
                    )
                    if (lane > 0) {
                        drawLine(
                            color = Color(0x5500E5FF),
                            start = Offset(0f, topY),
                            end = Offset(size.width, topY),
                            strokeWidth = 4f
                        )
                    }

                    val cx = laneCarrotX[lane] * size.width
                    val cy = topY + laneHeight * 0.5f
                    drawCircle(
                        color = Color(0xFFFF9100),
                        radius = 18f,
                        center = Offset(cx, cy)
                    )
                }

                // Draw Bobs Runner on left side
                val bobsY = bobsLane * laneHeight + laneHeight * 0.5f
                val bobsX = size.width * 0.2f
                if (decoyTimer > 0f) {
                    drawCircle(
                        color = Color(0x6600E5FF),
                        radius = 44f,
                        center = Offset(bobsX, bobsY)
                    )
                }
                drawRoundRect(
                    color = Color(0xFFFF6D00),
                    topLeft = Offset(bobsX - 24f, bobsY - 24f),
                    size = Size(48f, 48f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(bobsX - 14f, bobsY - 54f),
                    size = Size(10f, 30f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(bobsX + 4f, bobsY - 54f),
                    size = Size(10f, 30f),
                    cornerRadius = CornerRadius(5f, 5f)
                )

                // Draw Fox Street Traps
                for (trap in activeTraps) {
                    val tx = trap.xProgress * size.width
                    val ty = trap.lane * laneHeight + laneHeight * 0.5f
                    drawRoundRect(
                        color = trap.color,
                        topLeft = Offset(tx - 24f, ty - 24f),
                        size = Size(48f, 48f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "🥕 Bobs Stash: $bobsCarrots / 25",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "⏱️ ${timeRemaining.toInt()}s",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "🦊 Fox Catches: $foxCatches / 4",
                    color = Color(0xFFFF4081),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Bottom Controls for Active Role (Including Couch+Phone Split Controls!)
        if (matchEnded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 MATCH COMPLETE! +Carrots & Golden Progress Awarded!",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Black
                    )
                    Button(
                        onClick = { resetMatch(modeIndex) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("Rematch", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (modeIndex == 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        bobsLane = (bobsLane - 1).coerceAtLeast(0)
                        soundEngine.playJump()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("⬆ Hop Up Lane", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        decoyTimer = 2.4f
                        soundEngine.playBoing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                ) {
                    Text("🎭 Holo-Decoy", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        bobsLane = (bobsLane + 1).coerceAtMost(2)
                        soundEngine.playJump()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("⬇ Burrow Down Lane", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        } else if (modeIndex == 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        activeTraps.add(StreetTrap(0, 0.95f, "Anvil Drop 🔨", Color(0xFFFFD600)))
                        soundEngine.playBoing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                ) {
                    Text("🔨 Trap Top Lane", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        activeTraps.add(StreetTrap(1, 0.95f, "Net Cage 🕸️", Color(0xFF00E5FF)))
                        soundEngine.playBoing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("🕸️ Trap Mid Lane", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        activeTraps.add(StreetTrap(2, 0.95f, "Salsa Slick 🌶️", Color(0xFFFF4081)))
                        soundEngine.playBoing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                ) {
                    Text("🌶️ Trap Bot Lane", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Mode 2: Couch + Phone simultaneous 2-sided controls!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { bobsLane = (bobsLane - 1).coerceAtLeast(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("📺 Bobs ⬆", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { bobsLane = (bobsLane + 1).coerceAtMost(2) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("📺 Bobs ⬇", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { activeTraps.add(StreetTrap(0, 0.95f, "Phone Anvil 🔨", Color(0xFFFFD600))) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600))
                    ) {
                        Text("📱 Trap 1", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { activeTraps.add(StreetTrap(1, 0.95f, "Phone Net 🕸️", Color(0xFF00E5FF))) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("📱 Trap 2", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { activeTraps.add(StreetTrap(2, 0.95f, "Phone Salsa 🌶️", Color(0xFFFF4081))) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                    ) {
                        Text("📱 Trap 3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
