package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PlayerProfileState
import com.example.myapplication.model.BiomeTheme
import com.example.myapplication.model.CustomStudioBlock
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.PlatformKind
import com.example.myapplication.model.PowerUpType
import com.example.myapplication.model.StageConfig

@Composable
fun CartoonStudioScreen(
    profile: PlayerProfileState,
    onSaveStudioCode: (String) -> Unit,
    onPlayCustomStage: (StageConfig) -> Unit,
    onBack: () -> Unit
) {
    var selectedBiome by remember { mutableStateOf(BiomeTheme.GOLDEN_WORLD_KINGDOM) }
    var statusText by remember {
        mutableStateOf("Phase 3 Cartoon Studio: Snap together 6 stage segments, place Fox Syndicate hazards & power-ups, and test your stage immediately!")
    }

    val segments = remember {
        mutableStateListOf(
            CustomStudioBlock(0, PlatformKind.RAINBOW_BRIDGE, EnemyKind.SLY_SCOUT, PowerUpType.RAINBOW_CARROT),
            CustomStudioBlock(1, PlatformKind.CABLE_CAR_RAIL, EnemyKind.STREET_SWEEPER, null),
            CustomStudioBlock(2, PlatformKind.BOUNCY_AWNING, EnemyKind.MECHA_FOX, PowerUpType.SPRING_KICKS),
            CustomStudioBlock(3, PlatformKind.RHYTHM_BEAT_BLOCK, EnemyKind.ILLUSION_PHANTOM, null),
            CustomStudioBlock(4, PlatformKind.UPDRAFT_VENT, EnemyKind.WRENCH_FOX, PowerUpType.GOLDEN_BOBS),
            CustomStudioBlock(5, PlatformKind.RAINBOW_BRIDGE, EnemyKind.GOLDEN_GUARDIAN_BOSS, PowerUpType.WASABI_CARROT)
        )
    }

    val studioPlatforms = remember {
        listOf(
            PlatformKind.RAINBOW_BRIDGE,
            PlatformKind.CABLE_CAR_RAIL,
            PlatformKind.BOUNCY_AWNING,
            PlatformKind.RHYTHM_BEAT_BLOCK,
            PlatformKind.UPDRAFT_VENT,
            PlatformKind.SOLID
        )
    }

    val studioEnemies = remember {
        listOf(
            null,
            EnemyKind.SLY_SCOUT,
            EnemyKind.STREET_SWEEPER,
            EnemyKind.MECHA_FOX,
            EnemyKind.GLADIATOR_FOX,
            EnemyKind.GOLDEN_GUARDIAN_BOSS
        )
    }

    val studioPowerUps = remember {
        listOf(
            null,
            PowerUpType.RAINBOW_CARROT,
            PowerUpType.GOLDEN_BOBS,
            PowerUpType.WASABI_CARROT,
            PowerUpType.SWAG_SHADES,
            PowerUpType.SPRING_KICKS
        )
    }

    fun generateShareCode(): String {
        val biomeCode = selectedBiome.name.take(3)
        val segHash = segments.joinToString("") {
            "${it.platformKind.ordinal}${it.enemyKind?.ordinal ?: 0}"
        }
        return "STUDIO-$biomeCode-$segHash"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF140D2B))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎬 CARTOON STUDIO LEVEL CREATOR (PHASE 3)",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )
                Text(
                    text = "Saved Share Code: ${profile.savedStudioCode}",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            OutlinedButton(onClick = onBack) {
                Text("World Hub", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = statusText,
            color = Color(0xFFFFCC80),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Biome Selector + Launch Custom Stage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    BiomeTheme.GOLDEN_WORLD_KINGDOM to "👑 Golden World",
                    BiomeTheme.NEON_ROOFTOPS to "🌆 Carrot City",
                    BiomeTheme.COSMIC_HARE_FORTRESS to "🌌 Cosmic"
                ).forEach { (biome, label) ->
                    FilterChip(
                        selected = selectedBiome == biome,
                        onClick = { selectedBiome = biome },
                        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD700),
                            selectedLabelColor = Color.Black,
                            labelColor = Color.White
                        )
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val code = generateShareCode()
                        onSaveStudioCode(code)
                        statusText = "Saved & validated Stage Code: $code! Ready to share with friends!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("💾 Save Code", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
                Button(
                    onClick = {
                        val code = generateShareCode()
                        onSaveStudioCode(code)
                        val customStage = StageConfig(
                            id = 99,
                            worldNumber = 10,
                            code = "STUDIO",
                            title = "Custom Cartoon Course ($code)",
                            subtitle = "Player-Created Phase 3 Stage in ${selectedBiome.displayName}",
                            biome = selectedBiome,
                            lengthWorldUnits = 4800f,
                            parTimeSeconds = 45f,
                            isGoldenRift = selectedBiome == BiomeTheme.GOLDEN_WORLD_KINGDOM,
                            recommendedHint = "Playing your custom Cartoon Studio creation! Test every platform and hazard!",
                            customStudioBlocks = segments.toList()
                        )
                        onPlayCustomStage(customStage)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("▶ PLAY STAGE", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 6 Editable Stage Segments
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(segments) { index, block ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F173D))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Segment #${index + 1}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Platform: ${block.platformKind.studioLabel}  •  Enemy: ${block.enemyKind?.title ?: "None"}  •  Item: ${block.powerUpType?.title ?: "None"}",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = {
                                    val nextIdx = (studioPlatforms.indexOf(block.platformKind) + 1) % studioPlatforms.size
                                    segments[index] = block.copy(platformKind = studioPlatforms[nextIdx])
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF651FFF))
                            ) {
                                Text("🧱 Platform", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    val nextIdx = (studioEnemies.indexOf(block.enemyKind) + 1) % studioEnemies.size
                                    segments[index] = block.copy(enemyKind = studioEnemies[nextIdx])
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                            ) {
                                Text("🦊 Enemy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    val nextIdx = (studioPowerUps.indexOf(block.powerUpType) + 1) % studioPowerUps.size
                                    segments[index] = block.copy(powerUpType = studioPowerUps[nextIdx])
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                            ) {
                                Text("⚡ PowerUp", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
