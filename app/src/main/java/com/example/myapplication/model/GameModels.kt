package com.example.myapplication.model

import androidx.compose.ui.graphics.Color

enum class PlayableHero(
    val id: String,
    val displayName: String,
    val title: String,
    val specialMoveLabel: String,
    val description: String,
    val bodyColor: Color,
    val earOrHelmetColor: Color,
    val accentColor: Color,
    val speedFactor: Float,
    val jumpFactor: Float
) {
    BOBS_BUNNY(
        id = "hero_bobs",
        displayName = "Bobs Bunny",
        title = "The Smooth Street Legend",
        specialMoveLabel = "TROLL DASH",
        description = "Balanced speed, Helicopter Ears glide & fourth-wall Troll Counters.",
        bodyColor = Color(0xFFF5F5F5),
        earOrHelmetColor = Color(0xFFFF80AB),
        accentColor = Color(0xFFFF6D00),
        speedFactor = 1.0f,
        jumpFactor = 1.0f
    ),
    BRICK_BADGER(
        id = "hero_brick_badger",
        displayName = "Brick the Badger",
        title = "Heavy-Hitting Burrow Bruiser",
        specialMoveLabel = "QUAKE SLAM",
        description = "Phase 2 Ally! Dives from mid-air with a Ground-Pound Shockwave that flattens every nearby enemy!",
        bodyColor = Color(0xFF455A64),
        earOrHelmetColor = Color(0xFFECEFF1),
        accentColor = Color(0xFFFFD600),
        speedFactor = 0.94f,
        jumpFactor = 1.05f
    ),
    ROXY_SPARKS(
        id = "hero_roxy_fox",
        displayName = "Roxy Sparks",
        title = "Redeemed Gadget Fox",
        specialMoveLabel = "GRAPPLE ZIP",
        description = "Phase 2 Rival-Turned-Ally! Fires a high-speed Holo-Grapple zip boost forward & upward across huge gaps!",
        bodyColor = Color(0xFFFF7043),
        earOrHelmetColor = Color(0xFF263238),
        accentColor = Color(0xFF00E5FF),
        speedFactor = 1.14f,
        jumpFactor = 1.08f
    ),
    GOLDEN_BOBS_CHAMPION(
        id = "hero_golden_bobs",
        displayName = "24K Golden Bobs",
        title = "Phase 3 Sovereign of the Golden World",
        specialMoveLabel = "RAINBOW BRIDGE",
        description = "Phase 3 Endgame Hero! Summons solid Rainbow Highway Bridges beneath his feet in mid-air & blasts foes with 24K stardust!",
        bodyColor = Color(0xFFFFD700),
        earOrHelmetColor = Color(0xFF00E5FF),
        accentColor = Color(0xFFFF4081),
        speedFactor = 1.22f,
        jumpFactor = 1.12f
    ),
    COSMIC_EMPRESS_BUNZILLA(
        id = "hero_empress_bunzilla",
        displayName = "Redeemed Bun-Zilla",
        title = "Phase 4 Cosmic Encore Ally",
        specialMoveLabel = "STARLIGHT NOVA",
        description = "Phase 4 Unlock! After her defeat, Bun-Zilla joins the squad with low-gravity Starlight Nova blasts & quadruple air-hops!",
        bodyColor = Color(0xFFE040FB),
        earOrHelmetColor = Color(0xFFFFD700),
        accentColor = Color(0xFF00E5FF),
        speedFactor = 1.26f,
        jumpFactor = 1.16f
    ),
    REFORMED_DON_FOXONE(
        id = "hero_don_foxone",
        displayName = "Don Foxone (VIP Ally)",
        title = "Phase 5 Reformed Syndicate Boss",
        specialMoveLabel = "STEAMROLLER BLITZ",
        description = "Phase 5 Unlock! Drives a mini-gold turbo steamroller that plows through hazards and mints bonus carrots on every bonk!",
        bodyColor = Color(0xFFD50000),
        earOrHelmetColor = Color(0xFFFFD700),
        accentColor = Color(0xFF00E676),
        speedFactor = 1.30f,
        jumpFactor = 1.10f
    )
}

enum class DailyWeatherMutator(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val carrotMultiplierBonus: Int,
    val gravityScale: Float,
    val speedScale: Float
) {
    CLEAR_SKIES(
        id = "mutator_clear",
        title = "Sunny Carrot Skies",
        emoji = "☀️",
        description = "Classic physics across all worlds.",
        carrotMultiplierBonus = 0,
        gravityScale = 1.0f,
        speedScale = 1.0f
    ),
    GOLDEN_STAR_RAIN(
        id = "mutator_gold_rain",
        title = "24K Starlight Festival",
        emoji = "🌠",
        description = "+1 Bonus Swag Multiplier & +15% floatier jumps!",
        carrotMultiplierBonus = 1,
        gravityScale = 0.86f,
        speedScale = 1.08f
    ),
    TURBO_ESPRESSO_RUSH(
        id = "mutator_turbo",
        title = "Toky-Oat Turbo Rush",
        emoji = "⚡",
        description = "+25% Hero & Enemy Speed + +1 Bonus Swag Multiplier!",
        carrotMultiplierBonus = 1,
        gravityScale = 1.0f,
        speedScale = 1.25f
    ),
    MOON_BOUNCE_GALAXY(
        id = "mutator_moon",
        title = "Cosmic Low-Gravity Party",
        emoji = "🪐",
        description = "Super moon-jump hang-time across every biome!",
        carrotMultiplierBonus = 1,
        gravityScale = 0.65f,
        speedScale = 1.05f
    ),
    LOONEY_INK_OVERDRIVE(
        id = "mutator_ink_overdrive",
        title = "Multiverse Ink Overdrive",
        emoji = "🎨",
        description = "Phase 5 Mutator! +2 Bonus Swag Multiplier, +18% Speed & bouncy cartoon gravity!",
        carrotMultiplierBonus = 2,
        gravityScale = 0.78f,
        speedScale = 1.18f
    )
}

data class FranchiseTrophy(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: (carrots: Int, shards: Int, stars: Int, bosses: Int, mpWins: Int) -> Boolean
)

enum class BiomeTheme(
    val displayName: String,
    val worldNumber: Int,
    val skyTop: Color,
    val skyBottom: Color,
    val groundColor: Color,
    val groundTrimColor: Color,
    val accentColor: Color,
    val hasRisingSalsa: Boolean = false,
    val gravityMultiplier: Float = 1.0f
) {
    FOREST_OUTSKIRTS(
        displayName = "World 1 • Overgrown Outskirts",
        worldNumber = 1,
        skyTop = Color(0xFF1E90FF),
        skyBottom = Color(0xFF98FB98),
        groundColor = Color(0xFF4E342E),
        groundTrimColor = Color(0xFF4CAF50),
        accentColor = Color(0xFFFF9800)
    ),
    NEON_ROOFTOPS(
        displayName = "World 1 • Neon Rooftop Run",
        worldNumber = 1,
        skyTop = Color(0xFF140B36),
        skyBottom = Color(0xFF6A1B9A),
        groundColor = Color(0xFF263238),
        groundTrimColor = Color(0xFF00E5FF),
        accentColor = Color(0xFFFF4081)
    ),
    METRO_BURROWS(
        displayName = "World 1 • Metro Burrow Express",
        worldNumber = 1,
        skyTop = Color(0xFF0D1B2A),
        skyBottom = Color(0xFF1B263B),
        groundColor = Color(0xFF3E2723),
        groundTrimColor = Color(0xFFFFB300),
        accentColor = Color(0xFF00E676)
    ),
    SKY_HIGH_CRANES(
        displayName = "World 1 • Construction Sky-High",
        worldNumber = 1,
        skyTop = Color(0xFF0277BD),
        skyBottom = Color(0xFFFFCC80),
        groundColor = Color(0xFF5D4037),
        groundTrimColor = Color(0xFFFF5722),
        accentColor = Color(0xFFFFEB3B)
    ),
    GOLDEN_RIFT(
        displayName = "Secret • The Golden Rift",
        worldNumber = 10,
        skyTop = Color(0xFF3A1C71),
        skyBottom = Color(0xFFFFAF7B),
        groundColor = Color(0xFFB8860B),
        groundTrimColor = Color(0xFFFFD700),
        accentColor = Color(0xFF00FFFF),
        gravityMultiplier = 0.85f
    ),
    BOSS_ARENA(
        displayName = "World 1 Boss • Don Foxone's Arena",
        worldNumber = 1,
        skyTop = Color(0xFF21091E),
        skyBottom = Color(0xFF880E4F),
        groundColor = Color(0xFF212121),
        groundTrimColor = Color(0xFFFF1744),
        accentColor = Color(0xFFFFEA00)
    ),
    SAN_FRANCIS_KALE(
        displayName = "World 2 • San Francis-Kale",
        worldNumber = 2,
        skyTop = Color(0xFF37474F),
        skyBottom = Color(0xFFB0BEC5),
        groundColor = Color(0xFF2E7D32),
        groundTrimColor = Color(0xFFFF5252),
        accentColor = Color(0xFFFFD740)
    ),
    TOKY_OAT(
        displayName = "World 3 • Toky-Oat Cyber District",
        worldNumber = 3,
        skyTop = Color(0xFF090420),
        skyBottom = Color(0xFF4A148C),
        groundColor = Color(0xFF1A237E),
        groundTrimColor = Color(0xFF00E5FF),
        accentColor = Color(0xFFFF007F)
    ),
    ROME_AINE(
        displayName = "World 4 • Rome-Aine Colos-Spud",
        worldNumber = 4,
        skyTop = Color(0xFFE65100),
        skyBottom = Color(0xFFFFE082),
        groundColor = Color(0xFF8D6E63),
        groundTrimColor = Color(0xFFFFD54F),
        accentColor = Color(0xFF66BB6A)
    ),
    LOND_ONION(
        displayName = "World 5 • Lond-Onion Illusion Library",
        worldNumber = 5,
        skyTop = Color(0xFF1B142E),
        skyBottom = Color(0xFF4A3B69),
        groundColor = Color(0xFF3E2723),
        groundTrimColor = Color(0xFFB388FF),
        accentColor = Color(0xFF80CBC4)
    ),
    VESUVI_APPLE(
        displayName = "World 6 • Mount Vesuvi-Apple",
        worldNumber = 6,
        skyTop = Color(0xFF3E0000),
        skyBottom = Color(0xFFFF3D00),
        groundColor = Color(0xFF2D1414),
        groundTrimColor = Color(0xFFFF9100),
        accentColor = Color(0xFFFFEA00),
        hasRisingSalsa = true
    ),
    RIO_DE_JALAPENO(
        displayName = "World 7 • Rio de Jalapeño Carnival",
        worldNumber = 7,
        skyTop = Color(0xFF00695C),
        skyBottom = Color(0xFF64FFDA),
        groundColor = Color(0xFF33691E),
        groundTrimColor = Color(0xFFFFEB3B),
        accentColor = Color(0xFFFF4081)
    ),
    CHICAG_ARTICHOKE(
        displayName = "World 8 • Chicag-Artichoke Skyworks",
        worldNumber = 8,
        skyTop = Color(0xFF1C313A),
        skyBottom = Color(0xFF78909C),
        groundColor = Color(0xFF37474F),
        groundTrimColor = Color(0xFFFFAB00),
        accentColor = Color(0xFF00E676)
    ),
    COSMIC_HARE_FORTRESS(
        displayName = "World 9 • Cosmic Hare-Fortress",
        worldNumber = 9,
        skyTop = Color(0xFF050214),
        skyBottom = Color(0xFF311B92),
        groundColor = Color(0xFF212121),
        groundTrimColor = Color(0xFFE040FB),
        accentColor = Color(0xFF00E5FF),
        gravityMultiplier = 0.68f
    ),
    GOLDEN_WORLD_KINGDOM(
        displayName = "World 10 • The Golden World Kingdom",
        worldNumber = 10,
        skyTop = Color(0xFF2A1458),
        skyBottom = Color(0xFFFFD700),
        groundColor = Color(0xFF9A6F00),
        groundTrimColor = Color(0xFFFFF59D),
        accentColor = Color(0xFF00E5FF),
        gravityMultiplier = 0.80f
    ),
    LOONEY_MULTIVERSE(
        displayName = "World 11 • The Looney Multiverse & Ink Studio",
        worldNumber = 11,
        skyTop = Color(0xFF1F003B),
        skyBottom = Color(0xFFFF4081),
        groundColor = Color(0xFF212121),
        groundTrimColor = Color(0xFF00E676),
        accentColor = Color(0xFFFFEA00),
        gravityMultiplier = 0.76f
    )
}

enum class PowerUpType(
    val title: String,
    val subtitle: String,
    val durationSeconds: Float,
    val color: Color,
    val emoji: String
) {
    WASABI_CARROT(
        title = "Golden Wasabi Carrot",
        subtitle = "Fire dash + smash straight through foxes!",
        durationSeconds = 6.5f,
        color = Color(0xFFFF3D00),
        emoji = "🌶️"
    ),
    SWAG_SHADES(
        title = "Swag Shades",
        subtitle = "Bullet-time slow-mo + auto Troll Counter!",
        durationSeconds = 7.0f,
        color = Color(0xFF00E5FF),
        emoji = "🕶️"
    ),
    SPRING_KICKS(
        title = "Spring-Loaded Kicks",
        subtitle = "Triple-jump & super skyward bounces!",
        durationSeconds = 8.5f,
        color = Color(0xFF76FF03),
        emoji = "👟"
    ),
    BONK_MALLET(
        title = "Bonk-O-Matic Mallet",
        subtitle = "Drops cartoon anvils & flattens foes into accordions!",
        durationSeconds = 7.5f,
        color = Color(0xFFFFD600),
        emoji = "🔨"
    ),
    GOLDEN_BOBS(
        title = "24-Karat Golden Aura",
        subtitle = "Invincible Golden World aura + 3x carrot magnet!",
        durationSeconds = 8.5f,
        color = Color(0xFFFFD700),
        emoji = "✨"
    ),
    RAINBOW_CARROT(
        title = "Rainbow Highway Carrot",
        subtitle = "Phase 3 Power-Up! Spawns Rainbow Bridges in mid-air & 3x Swag!",
        durationSeconds = 9.0f,
        color = Color(0xFFE040FB),
        emoji = "🌈"
    )
}

enum class CartoonHealthState(val displayName: String, val hitsRemaining: Int) {
    FULL_SWAG("Full Swag (Shades On!)", 2),
    SCUFFED("Scuffed! Catch Your Shades!", 1),
    PANCAKE("Squashed Pancake!", 1)
}

enum class SwagCategory(val label: String) {
    HEADGEAR("Headgear"),
    HOODIE("Hoodie"),
    KICKS("Kicks"),
    TRAIL("Swag Trail"),
    BADGE("Wonder Badge")
}

data class SwagItem(
    val id: String,
    val name: String,
    val category: SwagCategory,
    val costCarrots: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String,
    val badgePerkDescription: String = ""
)

data class CustomStudioBlock(
    val segmentIndex: Int,
    val platformKind: PlatformKind,
    val enemyKind: EnemyKind?,
    val powerUpType: PowerUpType?
)

data class StageConfig(
    val id: Int,
    val worldNumber: Int,
    val code: String,
    val title: String,
    val subtitle: String,
    val biome: BiomeTheme,
    val lengthWorldUnits: Float,
    val parTimeSeconds: Float = 45f,
    val isBossStage: Boolean = false,
    val isGoldenRift: Boolean = false,
    val isHeistStage: Boolean = false,
    val isBossRush: Boolean = false,
    val bossEnemyKind: EnemyKind? = null,
    val recommendedHint: String,
    val customStudioBlocks: List<CustomStudioBlock>? = null
)

enum class PlatformKind(val studioLabel: String) {
    SOLID("Solid Turf"),
    BOUNCY_AWNING("Bouncy Awning"),
    UPDRAFT_VENT("Updraft Vent"),
    PAINTED_TUNNEL_WALL("Painted Tunnel"),
    RAINBOW_BRIDGE("Rainbow Bridge"),
    CABLE_CAR_RAIL("Cable Car Rail"),
    RHYTHM_BEAT_BLOCK("Rhythm Beat Block"),
    CRUMBLING_COLOS_STONE("Colos-Spud Stone"),
    ILLUSION_BOOKSHELF("Illusion Shelf")
}

data class PlatformEntity(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val kind: PlatformKind = PlatformKind.SOLID
)

enum class EnemyKind(val title: String, val isBoss: Boolean = false) {
    SLY_SCOUT("Sly Scout Fox"),
    STREET_SWEEPER("Roller Sweeper Fox"),
    WRENCH_FOX("Wrench Trap Fox"),
    MECHA_FOX("Cyber Mecha-Fox"),
    GLADIATOR_FOX("Colos-Spud Gladiator Fox"),
    ILLUSION_PHANTOM("Furtini Mirror Fox"),
    DON_FOXONE_BOSS("Don Foxone's Steamroller Juicer", isBoss = true),
    GREAT_FURTINI_BOSS("The Great Furtini (Illusionist Boss)", isBoss = true),
    WRENCH_BOSS("Chief Wrench Fox's Crane-O-Tron", isBoss = true),
    EMPRESS_BUNZILLA_BOSS("Empress Bun-Zilla (Cosmic Ruler)", isBoss = true),
    GOLDEN_GUARDIAN_BOSS("24K Encore Guardian & Golden Foxone", isBoss = true),
    OMEGA_ANIMATOR_BOSS("Omega Foxtron & The Animator's Eraser", isBoss = true)
}

data class EnemyEntity(
    val id: Int,
    val kind: EnemyKind,
    var x: Float,
    var y: Float,
    var vx: Float,
    val minX: Float,
    val maxX: Float,
    var isFlattenedAccordion: Boolean = false,
    var flattenedTimer: Float = 0f,
    var isLaunchedSkyward: Boolean = false,
    var vy: Float = 0f,
    var bossHp: Int = 3,
    var bossMaxHp: Int = 3,
    var bossStunTimer: Float = 0f,
    var teleportTimer: Float = 0f
)

data class CollectibleCarrot(
    val id: Int,
    var x: Float,
    var y: Float,
    val isGoldenShard: Boolean = false,
    var collected: Boolean = false
)

data class PowerUpPickup(
    val id: Int,
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    var collected: Boolean = false
)

data class DroppedShades(
    var active: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var timer: Float = 0f
)

data class FloatingCartoonText(
    val id: Long,
    val text: String,
    var x: Float,
    var y: Float,
    val color: Color,
    var lifeRemaining: Float = 1.1f
)

data class ParticleDot(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float = 1f
)

data class CityNpcQuest(
    val id: String,
    val npcName: String,
    val npcRole: String,
    val dialogue: String,
    val requirementText: String,
    val rewardCarrots: Int,
    val rewardShards: Int,
    val isCompletedCheck: (totalCarrots: Int, goldenShards: Int, starsCount: Int, bossClears: Int) -> Boolean
)

object GameCatalog {
    val stages = listOf(
        // WORLD 1: NEW CARROT CITY
        StageConfig(
            id = 1,
            worldNumber = 1,
            code = "W1-1",
            title = "Overgrown Outskirts",
            subtitle = "Bounce off forest mushrooms & outsmart Sly Scout Foxes",
            biome = BiomeTheme.FOREST_OUTSKIRTS,
            lengthWorldUnits = 4200f,
            parTimeSeconds = 38f,
            recommendedHint = "Hold JUMP in mid-air to spin Bobs's Helicopter Ears! Run off ledges to test 'Don't Look Down' Coyote Time."
        ),
        StageConfig(
            id = 2,
            worldNumber = 1,
            code = "W1-2",
            title = "Neon Rooftop Run",
            subtitle = "Spring across glowing awnings & dodge Roller-Skating Foxes",
            biome = BiomeTheme.NEON_ROOFTOPS,
            lengthWorldUnits = 4600f,
            parTimeSeconds = 42f,
            recommendedHint = "Use Painted Tunnel walls or your Hero Special when your Swag Meter is charged!"
        ),
        StageConfig(
            id = 3,
            worldNumber = 1,
            code = "W1-3",
            title = "Metro Burrow Express",
            subtitle = "Dive underground with Burrow Blitz & launch enemies skyward",
            biome = BiomeTheme.METRO_BURROWS,
            lengthWorldUnits = 4900f,
            parTimeSeconds = 45f,
            recommendedHint = "Hold BURROW to tunnel under hazards—pop out directly underneath a fox to launch them!"
        ),
        StageConfig(
            id = 4,
            worldNumber = 1,
            code = "W1-4",
            title = "Construction Sky-High",
            subtitle = "Ride high-altitude updrafts & smash Wrench Fox contraptions",
            biome = BiomeTheme.SKY_HIGH_CRANES,
            lengthWorldUnits = 5200f,
            parTimeSeconds = 48f,
            recommendedHint = "Spread your Helicopter Ears inside wind vents to soar up to the Golden Carrot Shard route!"
        ),
        StageConfig(
            id = 5,
            worldNumber = 1,
            code = "W1-RIFT",
            title = "The Secret Golden Rift",
            subtitle = "A playable glimpse into World 10: The Golden World!",
            biome = BiomeTheme.GOLDEN_RIFT,
            lengthWorldUnits = 4800f,
            parTimeSeconds = 42f,
            isGoldenRift = true,
            recommendedHint = "Grab the 24-Karat Golden power-up to magnetize every carrot and walk on Rainbow Bridges!"
        ),
        StageConfig(
            id = 6,
            worldNumber = 1,
            code = "W1-BOSS",
            title = "Don Foxone's Steamroller Juicer",
            subtitle = "Foil the Fox Syndicate boss in a 3-phase slapstick showdown!",
            biome = BiomeTheme.BOSS_ARENA,
            lengthWorldUnits = 3200f,
            parTimeSeconds = 36f,
            isBossStage = true,
            bossEnemyKind = EnemyKind.DON_FOXONE_BOSS,
            recommendedHint = "Burrow underneath Don Foxone's steamroller or bounce on his cockpit when he stalls out!"
        ),

        // PHASE 2: WORLDS 2 TO 9
        StageConfig(
            id = 7,
            worldNumber = 2,
            code = "W2-1",
            title = "San Francis-Kale Bay Bridge",
            subtitle = "Grind spark-flying cable car rails & ride coastal wind updrafts!",
            biome = BiomeTheme.SAN_FRANCIS_KALE,
            lengthWorldUnits = 5100f,
            parTimeSeconds = 42f,
            recommendedHint = "Land on golden Cable Car Rails to grind automatically at high speed with bonus Swag!"
        ),
        StageConfig(
            id = 8,
            worldNumber = 3,
            code = "W3-1",
            title = "Toky-Oat Neon Beat",
            subtitle = "Hop across rhythm-synced neon blocks & short-circuit Cyber Mecha-Foxes!",
            biome = BiomeTheme.TOKY_OAT,
            lengthWorldUnits = 5300f,
            parTimeSeconds = 44f,
            recommendedHint = "Cyan Rhythm Blocks pulse to the beat—time your jumps or use Roxy Sparks's Grapple Zip!"
        ),
        StageConfig(
            id = 9,
            worldNumber = 4,
            code = "W4-1",
            title = "Rome-Aine Colos-Spud",
            subtitle = "Charge through crumbling marble columns & Gladiator Foxes!",
            biome = BiomeTheme.ROME_AINE,
            lengthWorldUnits = 5200f,
            parTimeSeconds = 45f,
            isHeistStage = true,
            recommendedHint = "HEIST CHASE MODE: The more carrots you carry, the faster Gladiator Foxes patrol!"
        ),
        StageConfig(
            id = 10,
            worldNumber = 5,
            code = "W5-BOSS",
            title = "Lond-Onion & The Great Furtini",
            subtitle = "Navigate shifting library shelves & defeat illusionist boss The Great Furtini!",
            biome = BiomeTheme.LOND_ONION,
            lengthWorldUnits = 3600f,
            parTimeSeconds = 40f,
            isBossStage = true,
            bossEnemyKind = EnemyKind.GREAT_FURTINI_BOSS,
            recommendedHint = "The Great Furtini teleports after every hit! Use Brick the Badger's Quake Slam or stomp his top hat!"
        ),
        StageConfig(
            id = 11,
            worldNumber = 6,
            code = "W6-1",
            title = "Mount Vesuvi-Apple Eruption",
            subtitle = "Stay on high platforms above bubbling Molten Salsa & minecart tracks!",
            biome = BiomeTheme.VESUVI_APPLE,
            lengthWorldUnits = 5400f,
            parTimeSeconds = 44f,
            recommendedHint = "Molten Salsa rises and falls rhythmically at the bottom—stay on the upper cable rails and awnings!"
        ),
        StageConfig(
            id = 12,
            worldNumber = 7,
            code = "W7-1",
            title = "Rio de Jalapeño Carnival",
            subtitle = "Bounce across tropical carnival floats, waterfalls & samba awnings!",
            biome = BiomeTheme.RIO_DE_JALAPENO,
            lengthWorldUnits = 5500f,
            parTimeSeconds = 43f,
            recommendedHint = "Chain Bouncy Awning jumps together without touching the ground for 3x Swag Overdrive!"
        ),
        StageConfig(
            id = 13,
            worldNumber = 8,
            code = "W8-BOSS",
            title = "Chicag-Artichoke & Chief Wrench Fox",
            subtitle = "Brave high-wind steel girders & smash Chief Wrench Fox's Crane-O-Tron!",
            biome = BiomeTheme.CHICAG_ARTICHOKE,
            lengthWorldUnits = 3800f,
            parTimeSeconds = 42f,
            isBossStage = true,
            bossEnemyKind = EnemyKind.WRENCH_BOSS,
            recommendedHint = "Use updraft vents to get above Chief Wrench Fox's armored Crane-O-Tron and drop anvils on him!"
        ),
        StageConfig(
            id = 14,
            worldNumber = 9,
            code = "W9-FINAL",
            title = "Cosmic Hare-Fortress: Empress Bun-Zilla",
            subtitle = "Low-gravity space platforming & the grand campaign finale for the Golden Compass!",
            biome = BiomeTheme.COSMIC_HARE_FORTRESS,
            lengthWorldUnits = 4200f,
            parTimeSeconds = 50f,
            isBossStage = true,
            bossEnemyKind = EnemyKind.EMPRESS_BUNZILLA_BOSS,
            recommendedHint = "Low Cosmic Gravity lets you soar! Defeat Empress Bun-Zilla (5 HP) to claim the Golden Carrot Compass!"
        ),

        // PHASE 3 & 4: WORLD 10 — THE GOLDEN WORLD & BOSS RUSH GAUNTLET
        StageConfig(
            id = 15,
            worldNumber = 10,
            code = "W10-1",
            title = "Golden Forest & Rainbow Highway",
            subtitle = "Soar across shimmering Rainbow Bridges & 24K Cable Rails in the Golden World!",
            biome = BiomeTheme.GOLDEN_WORLD_KINGDOM,
            lengthWorldUnits = 5600f,
            parTimeSeconds = 42f,
            isGoldenRift = true,
            recommendedHint = "Select 24K Golden Bobs or grab the Rainbow Carrot to summon Rainbow Bridges beneath your feet in mid-air!"
        ),
        StageConfig(
            id = 16,
            worldNumber = 10,
            code = "W10-2",
            title = "Palace of the Ancient Carrot",
            subtitle = "Uncover the Golden World's hidden truth across rhythm-synced starlight vaults!",
            biome = BiomeTheme.GOLDEN_WORLD_KINGDOM,
            lengthWorldUnits = 5900f,
            parTimeSeconds = 45f,
            isGoldenRift = true,
            isHeistStage = true,
            recommendedHint = "Ancient Truth: The First Golden Carrot planted all 9 Veggie Cosmos worlds! Guard your stash to the vault!"
        ),
        StageConfig(
            id = 17,
            worldNumber = 10,
            code = "W10-CROWN",
            title = "The 24-Karat Crown Run (Encore Boss)",
            subtitle = "Ultimate Phase 3 Finale against the 6-HP 24K Encore Guardian & Golden Foxone!",
            biome = BiomeTheme.GOLDEN_WORLD_KINGDOM,
            lengthWorldUnits = 4500f,
            parTimeSeconds = 52f,
            isBossStage = true,
            isGoldenRift = true,
            bossEnemyKind = EnemyKind.GOLDEN_GUARDIAN_BOSS,
            recommendedHint = "The 24K Encore Guardian teleports AND charges! Use Rainbow Bridges, Quake Slams, or Burrow Uppercuts!"
        ),
        StageConfig(
            id = 18,
            worldNumber = 10,
            code = "W10-RUSH",
            title = "Grand Boss Rush Pantheons",
            subtitle = "Phase 4 Master Gauntlet! Face Don Foxone, Great Furtini, Chief Wrench & Encore Guardian in one epic run!",
            biome = BiomeTheme.GOLDEN_WORLD_KINGDOM,
            lengthWorldUnits = 6400f,
            parTimeSeconds = 75f,
            isBossStage = true,
            isGoldenRift = true,
            isBossRush = true,
            bossEnemyKind = EnemyKind.GOLDEN_GUARDIAN_BOSS,
            recommendedHint = "BOSS RUSH GAUNTLET: Defeat all 4 Syndicate Bosses along the Golden Highway or use Rainbow Bridges to style over them!"
        ),

        // PHASE 5: WORLD 11 — THE LOONEY MULTIVERSE & ANIMATOR'S INK STUDIO
        StageConfig(
            id = 19,
            worldNumber = 11,
            code = "W11-1",
            title = "Inkwell Studio & Mirror Dimension",
            subtitle = "Phase 5 Multiverse Run! Outrace the Holo-Ghost Bunny across Rainbow Rails & Painted Tunnels!",
            biome = BiomeTheme.LOONEY_MULTIVERSE,
            lengthWorldUnits = 5800f,
            parTimeSeconds = 43f,
            isGoldenRift = true,
            recommendedHint = "Beat the translucent Holo-Ghost Bunny (it runs at Par Time pace) to earn a +25 Carrot Speedrun Bonus! Toggle the P2 Sidekick Drone anytime!"
        ),
        StageConfig(
            id = 20,
            worldNumber = 11,
            code = "W11-OMEGA",
            title = "The Animator's Pencil & Omega Foxtron",
            subtitle = "Phase 5 Ultimate Showdown against the 7-HP Omega Foxtron & Giant Eraser!",
            biome = BiomeTheme.LOONEY_MULTIVERSE,
            lengthWorldUnits = 4800f,
            parTimeSeconds = 55f,
            isBossStage = true,
            isGoldenRift = true,
            bossEnemyKind = EnemyKind.OMEGA_ANIMATOR_BOSS,
            recommendedHint = "Use Don Foxone's Steamroller Blitz, Bun-Zilla's Starlight Nova, or P2 Sidekick Pie Zaps to topple the 7-HP Omega Foxtron!"
        )
    )

    val franchiseTrophies = listOf(
        FranchiseTrophy(
            id = "trophy_first_hop",
            title = "Welcome to New Carrot City",
            description = "Earn your first Stage Mastery Star.",
            icon = "🥕",
            isUnlocked = { _, _, stars, _, _ -> stars >= 1 }
        ),
        FranchiseTrophy(
            id = "trophy_carrot_tycoon",
            title = "24-Karat Street Tycoon",
            description = "Hold 300+ Carrots in your stash.",
            icon = "💰",
            isUnlocked = { carrots, _, _, _, _ -> carrots >= 300 }
        ),
        FranchiseTrophy(
            id = "trophy_golden_monument",
            title = "Golden World Architect",
            description = "Collect 6+ Golden Carrot Shards for the Hub Monument.",
            icon = "🌟",
            isUnlocked = { _, shards, _, _, _ -> shards >= 6 }
        ),
        FranchiseTrophy(
            id = "trophy_boss_slayer",
            title = "Syndicate Buster",
            description = "Foil 3+ Bosses across the Veggie Cosmos.",
            icon = "🔨",
            isUnlocked = { _, _, _, bosses, _ -> bosses >= 3 }
        ),
        FranchiseTrophy(
            id = "trophy_party_champ",
            title = "Fox Hunt Party Legend",
            description = "Win a 1v3 or Couch+Phone Asymmetric Party Match.",
            icon = "🦊",
            isUnlocked = { _, _, _, _, mpWins -> mpWins >= 1 }
        ),
        FranchiseTrophy(
            id = "trophy_crown_sovereign",
            title = "Sovereign of the Golden World",
            description = "Earn 15+ Total Mastery Stars across all 10 Worlds.",
            icon = "👑",
            isUnlocked = { _, _, stars, _, _ -> stars >= 15 }
        ),
        FranchiseTrophy(
            id = "trophy_multiverse_animator",
            title = "Master of the Looney Multiverse",
            description = "Phase 5 Trophy: Foil 5+ Boss Stages across the 11 Worlds.",
            icon = "🎨",
            isUnlocked = { _, _, _, bosses, _ -> bosses >= 5 }
        )
    )

    val cityQuests = listOf(
        CityNpcQuest(
            id = "quest_dj_turnip",
            npcName = "DJ Turnip-Beat",
            npcRole = "Carrot City Radio Host",
            dialogue = "\"Yo Bobs! Bring me proof you've been styling on the Fox Syndicate and I'll fund your sneaker collection!\"",
            requirementText = "Collect at least 150 Total Carrots",
            rewardCarrots = 75,
            rewardShards = 1,
            isCompletedCheck = { carrots, _, _, _ -> carrots >= 150 }
        ),
        CityNpcQuest(
            id = "quest_mayor_marrow",
            npcName = "Mayor Marrow",
            npcRole = "New Carrot City Plaza",
            dialogue = "\"Our Golden Bobs Monument needs Golden Carrot Shards! Recover 3 Shards from across the worlds!\"",
            requirementText = "Hold at least 3 Golden Carrot Shards",
            rewardCarrots = 120,
            rewardShards = 2,
            isCompletedCheck = { _, shards, _, _ -> shards >= 3 }
        ),
        CityNpcQuest(
            id = "quest_mole_mechanic",
            npcName = "Diggs the Mole",
            npcRole = "Secret Underground Burrow Chief",
            dialogue = "\"Show me 8 total Mastery Stars across the Veggie Cosmos and I'll open the Secret Golden Vault!\"",
            requirementText = "Earn 8+ Total Stage Stars",
            rewardCarrots = 160,
            rewardShards = 2,
            isCompletedCheck = { _, _, stars, _ -> stars >= 8 }
        ),
        CityNpcQuest(
            id = "quest_cosmic_champion",
            npcName = "Professor Parsnip",
            npcRole = "Golden Compass Historian",
            dialogue = "\"Defeat at least 2 Syndicate Bosses across the 9 Worlds to calibrate the Golden World Portal!\"",
            requirementText = "Clear 2+ Boss Stages",
            rewardCarrots = 250,
            rewardShards = 3,
            isCompletedCheck = { _, _, _, bossClears -> bossClears >= 2 }
        ),
        CityNpcQuest(
            id = "quest_24k_sovereign",
            npcName = "Elder Carotene",
            npcRole = "Keeper of World 10 (The Golden World)",
            dialogue = "\"Conquer 3+ Bosses across the Veggie Cosmos & Golden World to earn the eternal 24-Karat Tribute!\"",
            requirementText = "Clear 3+ Boss Stages",
            rewardCarrots = 400,
            rewardShards = 4,
            isCompletedCheck = { _, _, _, bossClears -> bossClears >= 3 }
        ),
        CityNpcQuest(
            id = "quest_inkwell_director",
            npcName = "Inkwell the Toon Director",
            npcRole = "World 11 Multiverse Studio",
            dialogue = "\"Earn 12+ Mastery Stars and I'll fund your Multiverse Championship tour with 500 Carrots!\"",
            requirementText = "Earn 12+ Total Stage Stars",
            rewardCarrots = 500,
            rewardShards = 4,
            isCompletedCheck = { _, _, stars, _ -> stars >= 12 }
        )
    )

    val swagItems = listOf(
        // Headgear
        SwagItem(
            id = "head_classic_shades",
            name = "OG Carrot City Shades",
            category = SwagCategory.HEADGEAR,
            costCarrots = 0,
            primaryColor = Color(0xFF111111),
            secondaryColor = Color(0xFF00E5FF),
            description = "Bobs's signature dark frames. Effortlessly cool."
        ),
        SwagItem(
            id = "head_snapback_red",
            name = "Street King Snapback",
            category = SwagCategory.HEADGEAR,
            costCarrots = 60,
            primaryColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFFD600),
            description = "Backward crimson cap tilted for maximum aerodynamics."
        ),
        SwagItem(
            id = "head_cyber_visor",
            name = "Toky-Oat Holo Visor",
            category = SwagCategory.HEADGEAR,
            costCarrots = 130,
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFFF007F),
            description = "Phase 2 Neon Visor from World 3: Toky-Oat."
        ),
        SwagItem(
            id = "head_24k_crown",
            name = "24-Karat Golden Crown",
            category = SwagCategory.HEADGEAR,
            costCarrots = 180,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFF00E5FF),
            description = "Forged from Golden World stardust. Pure royalty."
        ),
        // Hoodies
        SwagItem(
            id = "hoodie_orange_drip",
            name = "Carrot Juice Hoodie",
            category = SwagCategory.HOODIE,
            costCarrots = 0,
            primaryColor = Color(0xFFFF6D00),
            secondaryColor = Color(0xFFFFFFFF),
            description = "Fresh oversized streetwear hoodie in bright carrot orange."
        ),
        SwagItem(
            id = "hoodie_neon_cyber",
            name = "Toky-Oat Neon Jacket",
            category = SwagCategory.HOODIE,
            costCarrots = 85,
            primaryColor = Color(0xFF651FFF),
            secondaryColor = Color(0xFF00E5FF),
            description = "Reflective synthwave jacket inspired by World 3."
        ),
        SwagItem(
            id = "hoodie_carnival_cape",
            name = "Rio Jalapeño Fiesta Vest",
            category = SwagCategory.HOODIE,
            costCarrots = 145,
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFFFFEB3B),
            description = "Vibrant festival streetwear from World 7."
        ),
        SwagItem(
            id = "hoodie_golden_velour",
            name = "Golden World Tracksuit",
            category = SwagCategory.HOODIE,
            costCarrots = 220,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFF212121),
            description = "Shimmers with 24-Karat threads as Bobs blurs past foes."
        ),
        // Kicks
        SwagItem(
            id = "kicks_high_tops",
            name = "Air Hop High-Tops",
            category = SwagCategory.KICKS,
            costCarrots = 0,
            primaryColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFFFFFF),
            description = "Classic red-and-white high-tops built for rooftop hops."
        ),
        SwagItem(
            id = "kicks_emerald_springs",
            name = "Neon Bounce Runners",
            category = SwagCategory.KICKS,
            costCarrots = 90,
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF111111),
            description = "Glow-in-the-dark soles that light up Metro Burrows."
        ),
        SwagItem(
            id = "kicks_gold_wings",
            name = "Golden Hermes Kicks",
            category = SwagCategory.KICKS,
            costCarrots = 200,
            primaryColor = Color(0xFFFFEA00),
            secondaryColor = Color(0xFFFF6D00),
            description = "Winged sneakers crafted for the Road to the Golden World."
        ),
        // Trails
        SwagItem(
            id = "trail_stardust",
            name = "Cartoon Smoke Puffs",
            category = SwagCategory.TRAIL,
            costCarrots = 0,
            primaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFFFFCC80),
            description = "Classic Looney dust clouds on every takeoff."
        ),
        SwagItem(
            id = "trail_neon_flames",
            name = "Wasabi Cyber Sparks",
            category = SwagCategory.TRAIL,
            costCarrots = 110,
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFFF4081),
            description = "Electric cyan and magenta sparks trailing behind Bobs."
        ),
        SwagItem(
            id = "trail_rainbow_gold",
            name = "Golden Rainbow Ribbon",
            category = SwagCategory.TRAIL,
            costCarrots = 240,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFF76FF03),
            description = "Leaves a glittering Golden World trail in mid-air."
        ),
        // Wonder Badges
        SwagItem(
            id = "badge_coyote_plus",
            name = "Badge: Looney Hang-Time",
            category = SwagCategory.BADGE,
            costCarrots = 0,
            primaryColor = Color(0xFFFF9100),
            secondaryColor = Color(0xFFFFFFFF),
            description = "Extends 'Don't Look Down' mid-air hang time before falling.",
            badgePerkDescription = "+40% Cartoon Coyote Time"
        ),
        SwagItem(
            id = "badge_carrot_magnet",
            name = "Badge: Carrot Magnet",
            category = SwagCategory.BADGE,
            costCarrots = 100,
            primaryColor = Color(0xFFFF1744),
            secondaryColor = Color(0xFFFFD600),
            description = "Automatically pulls nearby carrots toward your hero.",
            badgePerkDescription = "Passive Carrot Magnetism"
        ),
        SwagItem(
            id = "badge_turbo_burrow",
            name = "Badge: Turbo Burrow Drill",
            category = SwagCategory.BADGE,
            costCarrots = 140,
            primaryColor = Color(0xFF00E676),
            secondaryColor = Color(0xFF263238),
            description = "Increases underground Burrow Blitz speed and launch height.",
            badgePerkDescription = "+35% Burrow Speed & Launch"
        ),
        SwagItem(
            id = "badge_rail_spark",
            name = "Badge: Cable-Car Grind Master",
            category = SwagCategory.BADGE,
            costCarrots = 150,
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFFFFD700),
            description = "Phase 2 Badge: Boosts speed on Cable Car Rails & Rhythm Blocks.",
            badgePerkDescription = "+30% Rail Grind & Beat Boost"
        ),
        SwagItem(
            id = "badge_24k_rainbow_stride",
            name = "Badge: 24K Rainbow Stride",
            category = SwagCategory.BADGE,
            costCarrots = 210,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFE040FB),
            description = "Phase 3 Endgame Badge: Double-jumps automatically weave a temporary Rainbow Bridge!",
            badgePerkDescription = "Auto-Rainbow Bridge on Air Hop"
        )
    )
}
