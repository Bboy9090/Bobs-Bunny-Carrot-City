package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PlayerProfileState
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.StageConfig

@Composable
fun HomeHubScreen(
    profile: PlayerProfileState,
    onSelectStage: (StageConfig) -> Unit,
    onOpenCityLife: () -> Unit,
    onOpenCartoonStudio: () -> Unit,
    onOpenSwagShop: () -> Unit,
    onOpenMultiplayer: () -> Unit,
    onOpenMasterRoadmap: () -> Unit
) {
    // True = 2.5D Overworld Cartoon Road Map (Default), False = Stage Grid Roster
    var isWorldMapView by remember { mutableStateOf(true) }

    // 0 = All 11 Worlds (20 Stages), 1..11 = Specific World filter
    var selectedWorldFilter by remember { mutableIntStateOf(0) }

    val filteredStages = remember(selectedWorldFilter) {
        if (selectedWorldFilter == 0) {
            GameCatalog.stages
        } else {
            GameCatalog.stages.filter { it.worldNumber == selectedWorldFilter }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A103C), Color(0xFF0E0921))
                )
            )
            .systemBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Top Banner: Title + Economy Stash
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎨 BOBS BUNNY: CARROT CITY — LOONEY MULTIVERSE",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )
                Text(
                    text = "Hero: ${profile.selectedHero.displayName} (${profile.selectedHero.specialMoveLabel}) • Mutator: ${profile.activeMutator.title}",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF281F54))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🥕 ${profile.totalCarrots}",
                    color = Color(0xFFFF9100),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "🌟 ${profile.goldenShards}/10",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "⭐ ${profile.totalStarsEarned}",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick-Access Mode Buttons (Heroes & City, Cartoon Studio, Swag Shop, 1v3 Hunt, Roadmap)
        val compactBtnPad = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onOpenCityLife,
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = compactBtnPad,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
            ) {
                Text("🦸 City & Trophies", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
            Button(
                onClick = onOpenCartoonStudio,
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = compactBtnPad,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB))
            ) {
                Text("🎬 Studio", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
            Button(
                onClick = onOpenSwagShop,
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = compactBtnPad,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
            ) {
                Text("👟 Swag", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
            Button(
                onClick = onOpenMultiplayer,
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = compactBtnPad,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
            ) {
                Text("🦊 1v3 Party", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
            Button(
                onClick = onOpenMasterRoadmap,
                modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                contentPadding = compactBtnPad,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("🗺️ Plan", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Compact Landscape Row: Golden Statue Progress + View Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF251D4A))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ ${profile.goldenStatueTierName}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.5.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "👑 ${profile.bossStagesClearedCount}/7",
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { profile.goldenWorldProgressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFFFFD700),
                        trackColor = Color(0x33FFFFFF)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1.05f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF221848))
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { isWorldMapView = true },
                    modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                    contentPadding = compactBtnPad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isWorldMapView) Color(0xFFFFD700) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "🗺️ CARTOON WORLD MAP",
                        color = if (isWorldMapView) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = { isWorldMapView = false },
                    modifier = Modifier.weight(1f).heightIn(min = 34.dp),
                    contentPadding = compactBtnPad,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isWorldMapView) Color(0xFF00E5FF) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "📋 STAGE ROSTER",
                        color = if (!isWorldMapView) Color.Black else Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isWorldMapView) {
            // Interactive 11-World Cartoon Overworld Map with Bobs Bunny on the Road!
            CartoonWorldMapView(
                profile = profile,
                onSelectStage = onSelectStage,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        } else {
            // Interactive 10-World Map Filter Bar (Including World 10: The Golden World & Boss Rush!)
            LazyRow(
                modifier = Modifier.fillMaxWidth().testTag("world_filter_tabs"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            val worldTabs = listOf(
                0 to "🌌 All 11 Worlds (20 Stages)",
                11 to "🎨 W11: LOONEY MULTIVERSE",
                10 to "👑 W10: GOLDEN WORLD + BOSS RUSH",
                1 to "W1: Carrot City",
                2 to "W2: San Francis-Kale",
                3 to "W3: Toky-Oat",
                4 to "W4: Rome-Aine",
                5 to "W5: Lond-Onion",
                6 to "W6: Vesuvi-Apple",
                7 to "W7: Rio Jalapeño",
                8 to "W8: Chicag-Artichoke",
                9 to "W9: Cosmic Fortress"
            )
            items(worldTabs, key = { it.first }) { (worldNum, label) ->
                FilterChip(
                    selected = selectedWorldFilter == worldNum,
                    onClick = { selectedWorldFilter = worldNum },
                    label = {
                        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (worldNum == 11) Color(0xFFFF4081) else if (worldNum == 10) Color(0xFFFFD700) else Color(0xFF00E5FF),
                        selectedLabelColor = Color.Black,
                        labelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stage Selection List across Worlds 1-10
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("stage_roster_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStages, key = { it.id }) { stage ->
                val stars = profile.stageStars[stage.id] ?: 0
                val bestTime = profile.bestStageTimesSec[stage.id]
                val cardBorderColor = when {
                    stage.isBossRush -> Color(0xFFFF4081)
                    stage.worldNumber == 11 -> Color(0xFFE040FB)
                    stage.worldNumber == 10 || stage.isGoldenRift -> Color(0xFFFFD700)
                    stage.isBossStage -> Color(0xFFFF1744)
                    stage.isHeistStage -> Color(0xFF00E676)
                    else -> Color(0xFF5C6BC0)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.5.dp, cardBorderColor, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            stage.isBossRush -> Color(0xFF4A1038)
                            stage.worldNumber == 11 -> Color(0xFF2A0F3A)
                            stage.worldNumber == 10 -> Color(0xFF3E2B08)
                            stage.isGoldenRift -> Color(0xFF3B280C)
                            stage.isBossStage -> Color(0xFF3D122C)
                            stage.isHeistStage -> Color(0xFF1A332A)
                            else -> Color(0xFF1F193D)
                        }
                    )
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
                                    text = "${stage.code} • ${stage.title}",
                                    color = if (stage.isBossRush) Color(0xFFFF80AB) else if (stage.isGoldenRift || stage.worldNumber == 10) Color(0xFFFFD700) else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (stars > 0) "⭐".repeat(stars) else "☆☆☆",
                                    color = Color(0xFFFFD700),
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stage.subtitle,
                                color = Color(0xFFCFD8DC),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = buildString {
                                    append("Par Time: ⏱️ ${stage.parTimeSeconds.toInt()}s")
                                    if (bestTime != null) {
                                        append("  •  Best: ${"%.1f".format(bestTime)}s")
                                    }
                                    if (stage.isBossRush) {
                                        append("  •  🏆 4-BOSS PANTHEON GAUNTLET")
                                    } else if (stage.isHeistStage) {
                                        append("  •  🚨 HEIST CHASE")
                                    } else if (stage.worldNumber == 11) {
                                        append("  •  🎨 MULTIVERSE + 👻 HOLO-GHOST")
                                    } else if (stage.worldNumber == 10) {
                                        append("  •  👑 24K ENDGAME")
                                    }
                                },
                                color = Color(0xFF80DEEA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { onSelectStage(stage) },
                            modifier = Modifier.testTag("stage_play_${stage.code}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    stage.isBossRush -> Color(0xFFE040FB)
                                    stage.worldNumber == 10 -> Color(0xFFFFD700)
                                    stage.isGoldenRift -> Color(0xFFFFD700)
                                    stage.isBossStage -> Color(0xFFFF1744)
                                    else -> Color(0xFF00E676)
                                }
                            )
                        ) {
                            Text(
                                text = when {
                                    stage.isBossRush -> "BOSS RUSH"
                                    stage.isBossStage -> "BOSS FIGHT"
                                    else -> "PLAY"
                                },
                                color = if ((stage.isBossStage || stage.isBossRush) && stage.worldNumber != 10) Color.White else if (stage.isBossRush) Color.White else Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 500)
@Composable
private fun HomeHubScreenPreview() {
    HomeHubScreen(
        profile = PlayerProfileState(),
        onSelectStage = {},
        onOpenCityLife = {},
        onOpenCartoonStudio = {},
        onOpenSwagShop = {},
        onOpenMultiplayer = {},
        onOpenMasterRoadmap = {}
    )
}
