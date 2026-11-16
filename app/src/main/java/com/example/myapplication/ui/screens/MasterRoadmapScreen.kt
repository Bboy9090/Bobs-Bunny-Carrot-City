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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PlayerProfileState

private data class RoadmapSection(
    val phaseBadge: String,
    val title: String,
    val objective: String,
    val accentColor: Color,
    val bullets: List<String>
)

private val allPhasesSequential = listOf(
    RoadmapSection(
        phaseBadge = "PHASE 1 • COMPLETED & LIVE ✅",
        title = "Phase 1 — The Playable Foundation (New Carrot City)",
        objective = "Delivered a polished, installable Android game featuring New Carrot City, responsive cartoon physics, original characters, and the 6-stage opening chapter.",
        accentColor = Color(0xFF00E676),
        bullets = listOf(
            "1. Gameplay & Technical Foundation: 60 FPS Compose Canvas cartoon engine, 'Don't Look Down' Coyote Time, Helicopter Ears, and Burrow Blitz.",
            "2. Playable Slapstick Health States: Full Swag (Shades On), Scuffed (Catch flying shades within 3s!), and Pancake Squash state.",
            "3. Streamlined Core Power-Ups: Golden Wasabi Carrot, Swag Shades, Spring-Loaded Kicks, Bonk-O-Matic Mallet + 24K Golden Bobs.",
            "4. World 1 (New Carrot City): 6 handcrafted stages including Overgrown Outskirts, Neon Rooftops, Metro Burrows, Sky-High Cranes, Secret Golden Rift, and Don Foxone's Steamroller Juicer Boss.",
            "5. Fair Economy & Wonder Badges: 100% cosmetic Swag Shop separated from tactical Wonder Badges."
        )
    ),
    RoadmapSection(
        phaseBadge = "PHASE 2 • COMPLETED & LIVE ✅",
        title = "Phase 2 — The Great Carrot Adventure (Worlds 2–9 & Allies)",
        objective = "Expanded Bobs Bunny into a complete 9-World platform adventure with biome mechanics, 4 major bosses, playable allies, and the Carrot City Life hub.",
        accentColor = Color(0xFF00E5FF),
        bullets = listOf(
            "1. Worlds 2–4 Live: San Francis-Kale (Cable Car Rail Grinds), Toky-Oat (Rhythm Beat Blocks & Mecha-Foxes), Rome-Aine (Colos-Spud Heist Chase & Gladiator Foxes).",
            "2. Worlds 5–7 Live: Lond-Onion (Shifting bookshelves & teleporting boss The Great Furtini), Mount Vesuvi-Apple (Rising Molten Salsa waves), Rio de Jalapeño (Carnival bounce runs).",
            "3. Worlds 8–9 Live: Chicag-Artichoke (Chief Wrench Fox's Crane-O-Tron Boss) & Cosmic Hare-Fortress (Low-gravity showdown with 5-HP Empress Bun-Zilla).",
            "4. Playable Friends & Rivals Live: Switch freely between Bobs Bunny (Troll Dash), Brick the Badger (Mid-air Quake Slam), and Roxy Sparks (Holo-Grapple Zip).",
            "5. Living City & Secret Burrows Live: Complete NPC side quests, use Secret Underground Burrow warp shortcuts, and beat Stage Par Times."
        )
    ),
    RoadmapSection(
        phaseBadge = "PHASE 3 • COMPLETED & LIVE ✅",
        title = "Phase 3 — The Golden World & Console Franchise Expansion",
        objective = "Unlocked World 10 (The Golden World), 24K Golden Bobs, Cartoon Studio Level Creator, Couch+Phone Cross-Play Party Arena, and Console Gamepad + Haptic Rumble!",
        accentColor = Color(0xFFFFD700),
        bullets = listOf(
            "1. World 10 (The Golden World) Live: 3 endgame stages (W10-1 Golden Forest & Rainbow Highway, W10-2 Palace of the Ancient Carrot, and W10-CROWN The 24-Karat Crown Run vs. the 6-HP Encore Guardian).",
            "2. 24K Golden Bobs & Rainbow Carrot Live: Summon solid Rainbow Bridges beneath your feet in mid-air + equip the new '24K Rainbow Stride' Wonder Badge.",
            "3. Cartoon Studio Level Creator Live: Snap together custom 6-segment courses, pick biomes/platforms/enemies/power-ups, save shareable Studio Codes, and playtest immediately!",
            "4. Couch + Phone Asymmetric Cross-Play & Console Polish Live: Simultaneous TV Runner + Mobile Trapper party mode, hardware Gamepad/Keyboard bindings, and HD Haptic Rumble."
        )
    ),
    RoadmapSection(
        phaseBadge = "PHASE 4 • COMPLETED & PLAYABLE NOW ✅",
        title = "Phase 4 — Grand Encore: Boss Rush Pantheon, Cosmic Empress & Live Weather Mutators",
        objective = "Completed the ultimate franchise encore with Stage 18 (Grand Boss Rush Pantheon), 5th playable character Cosmic Empress Bun-Zilla, Live Weather Mutators, and the Hall of Fame!",
        accentColor = Color(0xFFE040FB),
        bullets = listOf(
            "1. Grand Boss Rush Pantheon (Stage 18: W10-RUSH) Live: Face Don Foxone, The Great Furtini, Chief Wrench Fox, and the 24K Encore Guardian back-to-back in a single marathon stage!",
            "2. 5th Playable Hero — Cosmic Empress Bun-Zilla Live: Reformed World 9 boss joins your squad with Quadruple Jumps and the screen-clearing 'STARLIGHT NOVA' shockwave.",
            "3. Live Seasonal Festival & Weather Mutators Live: Toggle Clear Skies, 24K Golden Star Rain (2x Carrots), Turbo Espresso Rush (1.2x Speed & 1.5x Carrots), or Moon-Bounce Galaxy (0.68x Gravity).",
            "4. Hall of Fame Franchise Trophies Live: Track 6 milestone trophies across New Carrot City, Boss Rush, Golden Shards, and Mastery Stars."
        )
    ),
    RoadmapSection(
        phaseBadge = "PHASE 5 • COMPLETED & PLAYABLE NOW ✅",
        title = "Phase 5 — The Looney Multiverse: World 11, Holo-Ghost Rivals & P2 Co-Op Sidekick",
        objective = "Broke the fourth wall into the Animator's Ink Studio with World 11, a 6th hero, a speedrun ghost rival, drop-in co-op support, and the 7-HP Omega Foxtron finale!",
        accentColor = Color(0xFFFF4081),
        bullets = listOf(
            "1. World 11 (The Looney Multiverse) Live: W11-1 Inkwell Studio & Mirror Dimension + W11-OMEGA The Animator's Pencil vs. the 7-HP teleporting Omega Foxtron.",
            "2. 6th Playable Hero — Reformed Don Foxone Live: Triple-jump VIP ally whose 'STEAMROLLER BLITZ' plows through foes with brief invulnerability and mints +5 bonus carrots per hit.",
            "3. Holo-Ghost Speedrun Rival Live: A translucent ghost bunny runs every stage at Par Time pace — outrun it to the goal for a +25 Carrot bonus, with a live lead/behind HUD tracker.",
            "4. P2 Co-Op Sidekick Drone Live: Toggle with the 🤖 button, P key, or gamepad Y. The drone hovers beside you, auto-zaps nearby foxes with pies, and extends your carrot magnet.",
            "5. Multiverse Ink Overdrive Mutator, 'Master of the Looney Multiverse' trophy & Inkwell the Toon Director NPC quest Live."
        )
    )
)

@Composable
fun MasterRoadmapScreen(
    profile: PlayerProfileState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF100C20))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🗺️ COMPLETE MASTER ROADMAP (PHASES 1–5 ALL LIVE!)",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text(
                    text = "All 11 Worlds, 20 Campaign Stages, 6 Heroes, Holo-Ghost Rivals & P2 Co-Op Unlocked!",
                    color = Color(0xFFB0BEC5),
                    fontSize = 12.sp
                )
            }
            OutlinedButton(onClick = onBack) {
                Text("Back to Hub", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF231B42))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🏆 Hub Centerpiece: ${profile.goldenStatueTierName}",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Golden Shards: 🌟 ${profile.goldenShards} / 10  •  Mastery Stars: ⭐ ${profile.totalStarsEarned}  •  Studio Code: ${profile.savedStudioCode}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { profile.goldenWorldProgressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = Color(0xFFFFD700),
                    trackColor = Color(0x44FFFFFF)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allPhasesSequential) { section ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1530))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = section.phaseBadge,
                            color = section.accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = section.title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = section.objective,
                            color = Color(0xFFFFCC80),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        section.bullets.forEach { item ->
                            Text(
                                text = "• $item",
                                color = Color(0xFFE0E0E0),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
