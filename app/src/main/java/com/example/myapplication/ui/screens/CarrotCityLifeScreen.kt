package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PlayerProfileState
import com.example.myapplication.model.DailyWeatherMutator
import com.example.myapplication.model.GameCatalog
import com.example.myapplication.model.PlayableHero
import com.example.myapplication.model.StageConfig

@Composable
fun CarrotCityLifeScreen(
    profile: PlayerProfileState,
    onSelectHero: (PlayableHero) -> Unit,
    onSelectMutator: (DailyWeatherMutator) -> Unit,
    onClaimQuest: (questId: String, carrots: Int, shards: Int) -> Boolean,
    onWarpToStage: (StageConfig) -> Unit,
    onBack: () -> Unit
) {
    var bannerText by remember {
        mutableStateOf("Phase 5 Multiverse Hub: Pick from 6 Playable Heroes (including Reformed Don Foxone!), try the new Ink Overdrive mutator, or warp to World 11!")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF130E29))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🏙️ CARROT CITY LIFE, HEROES & HALL OF FAME",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "Hero: ${profile.selectedHero.displayName}  •  Weather: ${profile.activeMutator.emoji} ${profile.activeMutator.title}  •  Stars: ⭐ ${profile.totalStarsEarned}",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            OutlinedButton(onClick = onBack) {
                Text("World Map", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = bannerText,
            color = Color(0xFFFFCC80),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Seasonal Festival & Daily Weather Mutators (Phase 4)
            item {
                Text(
                    text = "🌦️ LIVE SEASONAL FESTIVAL & WEATHER MUTATORS (PHASE 4)",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DailyWeatherMutator.entries.forEach { mutator ->
                        FilterChip(
                            selected = profile.activeMutator == mutator,
                            onClick = {
                                onSelectMutator(mutator)
                                bannerText = "${mutator.emoji} Activated ${mutator.title}: ${mutator.description}"
                            },
                            label = {
                                Text("${mutator.emoji} ${mutator.title}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD700),
                                selectedLabelColor = Color.Black,
                                labelColor = Color.White
                            )
                        )
                    }
                }
            }

            // 2. Playable Heroes & Rivals Roster (All 5 Heroes!)
            item {
                Text(
                    text = "🦸 PLAYABLE HERO & ENCORE ALLY ROSTER (${PlayableHero.entries.size} HEROES)",
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            items(PlayableHero.entries) { hero ->
                val isSelected = profile.selectedHero == hero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF282052) else Color(0xFF1D173B)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(hero.bodyColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(hero.accentColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${hero.displayName} • ${hero.title}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = hero.description,
                                    color = Color(0xFFCFD8DC),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Special Action Button: ⚡ ${hero.specialMoveLabel}",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                onSelectHero(hero)
                                bannerText = "${hero.displayName} is now your active hero across all 10 Worlds!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) Color(0xFF00E676) else Color(0xFF00E5FF)
                            )
                        ) {
                            Text(
                                text = if (isSelected) "ACTIVE" else "SELECT",
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // 3. Secret Underground City Warp Tunnels (Including Boss Rush!)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🕳️ SECRET UNDERGROUND BURROW SHORTCUTS",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF23193E))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Instant Burrow Express Tunnels to Endgame & Boss Rush:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val crownStage = GameCatalog.stages.first { it.id == 17 }
                            val rushStage = GameCatalog.stages.first { it.isBossRush }
                            val bunzillaStage = GameCatalog.stages.first { it.worldNumber == 9 }
                            val omegaStage = GameCatalog.stages.first { it.bossEnemyKind == com.example.myapplication.model.EnemyKind.OMEGA_ANIMATOR_BOSS }

                            Button(
                                onClick = { onWarpToStage(bunzillaStage) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB388FF))
                            ) {
                                Text("🌌 W9 Finale", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onWarpToStage(crownStage) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                            ) {
                                Text("👑 24K Crown", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onWarpToStage(rushStage) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF1744))
                            ) {
                                Text("🔥 BOSS RUSH", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onWarpToStage(omegaStage) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
                            ) {
                                Text("🎨 W11 OMEGA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 4. Hall of Fame Franchise Trophies
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🏆 HALL OF FAME FRANCHISE TROPHIES",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            items(GameCatalog.franchiseTrophies, key = { it.id }) { trophy ->
                val unlocked = trophy.isUnlocked(
                    profile.totalCarrots,
                    profile.goldenShards,
                    profile.totalStarsEarned,
                    profile.bossStagesClearedCount,
                    profile.multiplayerWins
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (unlocked) Color(0xFF2E2412) else Color(0xFF1A1530)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = trophy.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = trophy.title,
                                    color = if (unlocked) Color(0xFFFFD700) else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = trophy.description,
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = if (unlocked) "UNLOCKED ✅" else "LOCKED 🔒",
                            color = if (unlocked) Color(0xFF00E676) else Color(0xFF78909C),
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 5. Carrot City NPC Side Quests
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💬 CARROT CITY PLAZA NPC SIDE QUESTS",
                    color = Color(0xFF00E5FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }

            items(GameCatalog.cityQuests, key = { it.id }) { quest ->
                val alreadyClaimed = profile.claimedQuestIds.contains(quest.id)
                val canClaim = quest.isCompletedCheck(
                    profile.totalCarrots,
                    profile.goldenShards,
                    profile.totalStarsEarned,
                    profile.bossStagesClearedCount
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D173B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${quest.npcName} • ${quest.npcRole}",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = quest.dialogue,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Goal: ${quest.requirementText}  •  Reward: +${quest.rewardCarrots} 🥕 & +${quest.rewardShards} 🌟",
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                if (canClaim && !alreadyClaimed) {
                                    val ok = onClaimQuest(quest.id, quest.rewardCarrots, quest.rewardShards)
                                    if (ok) {
                                        bannerText = "Claimed ${quest.npcName}'s reward: +${quest.rewardCarrots} 🥕 & +${quest.rewardShards} 🌟!"
                                    }
                                }
                            },
                            enabled = canClaim && !alreadyClaimed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    alreadyClaimed -> Color(0xFF455A64)
                                    canClaim -> Color(0xFF00E676)
                                    else -> Color(0xFF37474F)
                                }
                            )
                        ) {
                            Text(
                                text = when {
                                    alreadyClaimed -> "CLAIMED"
                                    canClaim -> "CLAIM!"
                                    else -> "IN PROGRESS"
                                },
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
