package com.example.myapplication.game

import com.example.myapplication.model.CollectibleCarrot
import com.example.myapplication.model.EnemyEntity
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.PlatformEntity
import com.example.myapplication.model.PlatformKind
import com.example.myapplication.model.PowerUpPickup
import com.example.myapplication.model.PowerUpType
import com.example.myapplication.model.StageConfig

data class BuiltStageData(
    val config: StageConfig,
    val platforms: MutableList<PlatformEntity>,
    val enemies: MutableList<EnemyEntity>,
    val carrots: MutableList<CollectibleCarrot>,
    val powerUps: MutableList<PowerUpPickup>,
    val goalX: Float
)

object LevelBuilder {
    const val GROUND_Y = 520f

    fun buildStage(config: StageConfig): BuiltStageData {
        val platforms = mutableListOf<PlatformEntity>()
        val enemies = mutableListOf<EnemyEntity>()
        val carrots = mutableListOf<CollectibleCarrot>()
        val powerUps = mutableListOf<PowerUpPickup>()

        var nextEnemyId = 1
        var nextCarrotId = 1
        var nextPowerUpId = 1

        // Custom Cartoon Studio Stage Builder (Phase 3)
        config.customStudioBlocks?.let { blocks ->
            platforms.add(PlatformEntity(0f, GROUND_Y, 550f, 160f, PlatformKind.SOLID))
            var cursorX = 580f
            blocks.forEachIndexed { idx, block ->
                val segWidth = 560f
                platforms.add(PlatformEntity(cursorX, GROUND_Y, segWidth, 160f, PlatformKind.SOLID))
                val elevatedY = 350f - (idx % 2) * 50f
                platforms.add(
                    PlatformEntity(
                        x = cursorX + 120f,
                        y = elevatedY,
                        width = 250f,
                        height = 28f,
                        kind = block.platformKind
                    )
                )
                for (c in 0..4) {
                    carrots.add(
                        CollectibleCarrot(
                            id = nextCarrotId++,
                            x = cursorX + 75f + c * 85f,
                            y = if (c in 1..3) elevatedY - 55f else GROUND_Y - 48f,
                            isGoldenShard = (c == 2 && idx % 2 == 1)
                        )
                    )
                }
                block.enemyKind?.let { kind ->
                    enemies.add(
                        EnemyEntity(
                            id = nextEnemyId++,
                            kind = kind,
                            x = cursorX + segWidth * 0.6f,
                            y = GROUND_Y,
                            vx = if (kind.isBoss) -190f else -145f,
                            minX = cursorX + 60f,
                            maxX = cursorX + segWidth - 60f,
                            bossHp = if (kind.isBoss) 3 else 1,
                            bossMaxHp = if (kind.isBoss) 3 else 1
                        )
                    )
                }
                block.powerUpType?.let { pType ->
                    powerUps.add(
                        PowerUpPickup(
                            id = nextPowerUpId++,
                            x = cursorX + 230f,
                            y = elevatedY - 62f,
                            type = pType
                        )
                    )
                }
                cursorX += segWidth + 130f
            }
            platforms.add(PlatformEntity(cursorX, GROUND_Y, 700f, 160f, PlatformKind.SOLID))
            return BuiltStageData(
                config = config,
                platforms = platforms,
                enemies = enemies,
                carrots = carrots,
                powerUps = powerUps,
                goalX = cursorX + 320f
            )
        }

        // Phase 4 Grand Boss Rush Gauntlet (4 Bosses in sequence!)
        if (config.isBossRush) {
            platforms.add(PlatformEntity(0f, GROUND_Y, config.lengthWorldUnits + 600f, 160f, PlatformKind.SOLID))
            val bossSequence = listOf(
                EnemyKind.DON_FOXONE_BOSS,
                EnemyKind.GREAT_FURTINI_BOSS,
                EnemyKind.WRENCH_BOSS,
                EnemyKind.GOLDEN_GUARDIAN_BOSS
            )
            bossSequence.forEachIndexed { idx, bossKind ->
                val zoneX = 1100f + idx * 1250f
                platforms.add(PlatformEntity(zoneX - 250f, 360f, 240f, 26f, PlatformKind.RAINBOW_BRIDGE))
                platforms.add(PlatformEntity(zoneX + 120f, 330f, 240f, 26f, PlatformKind.BOUNCY_AWNING))
                powerUps.add(
                    PowerUpPickup(
                        id = nextPowerUpId++,
                        x = zoneX - 130f,
                        y = 290f,
                        type = if (idx % 2 == 0) PowerUpType.RAINBOW_CARROT else PowerUpType.GOLDEN_BOBS
                    )
                )
                for (c in 0..5) {
                    carrots.add(
                        CollectibleCarrot(
                            id = nextCarrotId++,
                            x = zoneX - 300f + c * 110f,
                            y = if (c % 2 == 0) 275f else 460f,
                            isGoldenShard = (c == 3)
                        )
                    )
                }
                enemies.add(
                    EnemyEntity(
                        id = nextEnemyId++,
                        kind = bossKind,
                        x = zoneX + 180f,
                        y = GROUND_Y,
                        vx = -195f,
                        minX = zoneX - 320f,
                        maxX = zoneX + 520f,
                        bossHp = 3,
                        bossMaxHp = 3
                    )
                )
            }
            return BuiltStageData(
                config = config,
                platforms = platforms,
                enemies = enemies,
                carrots = carrots,
                powerUps = powerUps,
                goalX = config.lengthWorldUnits - 220f
            )
        }

        if (config.isBossStage) {
            val bossKind = config.bossEnemyKind ?: EnemyKind.DON_FOXONE_BOSS
            val bossHp = when (bossKind) {
                EnemyKind.OMEGA_ANIMATOR_BOSS -> 7
                EnemyKind.GOLDEN_GUARDIAN_BOSS -> 6
                EnemyKind.EMPRESS_BUNZILLA_BOSS -> 5
                EnemyKind.GREAT_FURTINI_BOSS, EnemyKind.WRENCH_BOSS -> 4
                else -> 3
            }

            platforms.add(PlatformEntity(0f, GROUND_Y, config.lengthWorldUnits + 600f, 160f, PlatformKind.SOLID))
            platforms.add(
                PlatformEntity(
                    460f,
                    380f,
                    230f,
                    28f,
                    when (bossKind) {
                        EnemyKind.GREAT_FURTINI_BOSS -> PlatformKind.ILLUSION_BOOKSHELF
                        EnemyKind.GOLDEN_GUARDIAN_BOSS, EnemyKind.OMEGA_ANIMATOR_BOSS -> PlatformKind.RAINBOW_BRIDGE
                        else -> PlatformKind.BOUNCY_AWNING
                    }
                )
            )
            platforms.add(
                PlatformEntity(
                    1080f,
                    330f,
                    250f,
                    28f,
                    if (bossKind == EnemyKind.EMPRESS_BUNZILLA_BOSS || bossKind == EnemyKind.GOLDEN_GUARDIAN_BOSS || bossKind == EnemyKind.OMEGA_ANIMATOR_BOSS) {
                        PlatformKind.RAINBOW_BRIDGE
                    } else {
                        PlatformKind.CABLE_CAR_RAIL
                    }
                )
            )
            platforms.add(PlatformEntity(1750f, 375f, 230f, 28f, PlatformKind.BOUNCY_AWNING))
            platforms.add(PlatformEntity(2350f, 355f, 250f, 28f, PlatformKind.UPDRAFT_VENT))

            for (i in 0 until 26) {
                val cx = 300f + i * 110f
                val cy = if (i % 4 == 0) 265f else 460f
                carrots.add(CollectibleCarrot(nextCarrotId++, cx, cy, isGoldenShard = (i == 10 || i == 22)))
            }
            powerUps.add(PowerUpPickup(nextPowerUpId++, 580f, 305f, PowerUpType.SPRING_KICKS))
            powerUps.add(
                PowerUpPickup(
                    nextPowerUpId++,
                    1200f,
                    255f,
                    if (bossKind == EnemyKind.GOLDEN_GUARDIAN_BOSS || bossKind == EnemyKind.OMEGA_ANIMATOR_BOSS) PowerUpType.RAINBOW_CARROT else PowerUpType.WASABI_CARROT
                )
            )
            powerUps.add(
                PowerUpPickup(
                    nextPowerUpId,
                    1860f,
                    300f,
                    if (bossKind == EnemyKind.EMPRESS_BUNZILLA_BOSS || bossKind == EnemyKind.GOLDEN_GUARDIAN_BOSS || bossKind == EnemyKind.OMEGA_ANIMATOR_BOSS) {
                        PowerUpType.GOLDEN_BOBS
                    } else {
                        PowerUpType.BONK_MALLET
                    }
                )
            )

            val guardKind = when (config.worldNumber) {
                5 -> EnemyKind.ILLUSION_PHANTOM
                8 -> EnemyKind.WRENCH_FOX
                9, 10, 11 -> EnemyKind.MECHA_FOX
                else -> EnemyKind.SLY_SCOUT
            }
            enemies.add(
                EnemyEntity(
                    id = nextEnemyId++,
                    kind = guardKind,
                    x = 850f,
                    y = GROUND_Y,
                    vx = -135f,
                    minX = 560f,
                    maxX = 1200f
                )
            )
            enemies.add(
                EnemyEntity(
                    id = nextEnemyId,
                    kind = bossKind,
                    x = 1980f,
                    y = GROUND_Y,
                    vx = if (bossKind == EnemyKind.OMEGA_ANIMATOR_BOSS) -245f else if (bossKind == EnemyKind.GOLDEN_GUARDIAN_BOSS) -230f else if (bossKind == EnemyKind.EMPRESS_BUNZILLA_BOSS) -215f else -185f,
                    minX = 900f,
                    maxX = (config.lengthWorldUnits - 450f).coerceAtLeast(2200f),
                    bossHp = bossHp,
                    bossMaxHp = bossHp
                )
            )

            return BuiltStageData(
                config = config,
                platforms = platforms,
                enemies = enemies,
                carrots = carrots,
                powerUps = powerUps,
                goalX = config.lengthWorldUnits - 220f
            )
        }

        // Standard / Phase 2 & Phase 3 World 10 Stages
        var cursorX = 0f
        var segmentIndex = 0
        while (cursorX < config.lengthWorldUnits) {
            val segWidth = when {
                cursorX < 600f -> 750f
                else -> 580f + (segmentIndex % 3) * 140f
            }
            val groundKind = when {
                (config.isGoldenRift || config.worldNumber >= 10) && segmentIndex % 2 == 1 -> PlatformKind.RAINBOW_BRIDGE
                config.worldNumber == 4 && segmentIndex % 3 == 2 -> PlatformKind.CRUMBLING_COLOS_STONE
                else -> PlatformKind.SOLID
            }
            platforms.add(
                PlatformEntity(
                    x = cursorX,
                    y = GROUND_Y,
                    width = segWidth,
                    height = 160f,
                    kind = groundKind
                )
            )

            if (cursorX > 350f && cursorX < config.lengthWorldUnits - 500f) {
                val elevatedKind = when {
                    config.worldNumber >= 10 -> when (segmentIndex % 3) {
                        0 -> PlatformKind.RAINBOW_BRIDGE
                        1 -> PlatformKind.CABLE_CAR_RAIL
                        else -> PlatformKind.RHYTHM_BEAT_BLOCK
                    }
                    config.isGoldenRift -> PlatformKind.RAINBOW_BRIDGE
                    config.worldNumber == 2 -> if (segmentIndex % 2 == 0) PlatformKind.CABLE_CAR_RAIL else PlatformKind.UPDRAFT_VENT
                    config.worldNumber == 3 -> if (segmentIndex % 2 == 0) PlatformKind.RHYTHM_BEAT_BLOCK else PlatformKind.BOUNCY_AWNING
                    config.worldNumber == 4 -> if (segmentIndex % 2 == 0) PlatformKind.CRUMBLING_COLOS_STONE else PlatformKind.BOUNCY_AWNING
                    config.worldNumber == 5 -> PlatformKind.ILLUSION_BOOKSHELF
                    config.worldNumber == 6 -> if (segmentIndex % 2 == 0) PlatformKind.CABLE_CAR_RAIL else PlatformKind.BOUNCY_AWNING
                    config.worldNumber == 7 -> PlatformKind.BOUNCY_AWNING
                    config.worldNumber == 8 -> if (segmentIndex % 2 == 0) PlatformKind.UPDRAFT_VENT else PlatformKind.CABLE_CAR_RAIL
                    segmentIndex % 4 == 0 -> PlatformKind.BOUNCY_AWNING
                    segmentIndex % 4 == 2 -> PlatformKind.UPDRAFT_VENT
                    else -> PlatformKind.SOLID
                }
                val elevatedY = 360f - (segmentIndex % 2) * 58f
                platforms.add(
                    PlatformEntity(
                        x = cursorX + 130f,
                        y = elevatedY,
                        width = 250f,
                        height = 28f,
                        kind = elevatedKind
                    )
                )

                if (segmentIndex % 3 == 1) {
                    platforms.add(
                        PlatformEntity(
                            x = cursorX + 430f,
                            y = GROUND_Y - 120f,
                            width = 44f,
                            height = 120f,
                            kind = PlatformKind.PAINTED_TUNNEL_WALL
                        )
                    )
                }

                for (c in 0..4) {
                    carrots.add(
                        CollectibleCarrot(
                            id = nextCarrotId++,
                            x = cursorX + 80f + c * 85f,
                            y = if (c in 1..3) elevatedY - 55f else GROUND_Y - 48f,
                            isGoldenShard = (segmentIndex == 2 && c == 2) || (segmentIndex == 5 && c == 2)
                        )
                    )
                }

                val enemyKind = when (config.worldNumber) {
                    1 -> when (config.id) {
                        1 -> EnemyKind.SLY_SCOUT
                        2 -> if (segmentIndex % 2 == 0) EnemyKind.STREET_SWEEPER else EnemyKind.SLY_SCOUT
                        3 -> EnemyKind.STREET_SWEEPER
                        else -> EnemyKind.WRENCH_FOX
                    }
                    2 -> if (segmentIndex % 2 == 0) EnemyKind.STREET_SWEEPER else EnemyKind.SLY_SCOUT
                    3 -> EnemyKind.MECHA_FOX
                    4 -> EnemyKind.GLADIATOR_FOX
                    5 -> EnemyKind.ILLUSION_PHANTOM
                    6 -> if (segmentIndex % 2 == 0) EnemyKind.GLADIATOR_FOX else EnemyKind.WRENCH_FOX
                    7 -> EnemyKind.STREET_SWEEPER
                    8 -> EnemyKind.WRENCH_FOX
                    10, 11 -> if (segmentIndex % 2 == 0) EnemyKind.ILLUSION_PHANTOM else EnemyKind.MECHA_FOX
                    else -> EnemyKind.MECHA_FOX
                }
                enemies.add(
                    EnemyEntity(
                        id = nextEnemyId++,
                        kind = enemyKind,
                        x = cursorX + segWidth * 0.65f,
                        y = GROUND_Y,
                        vx = when (enemyKind) {
                            EnemyKind.STREET_SWEEPER, EnemyKind.MECHA_FOX -> -175f
                            EnemyKind.GLADIATOR_FOX -> -150f
                            else -> -120f
                        },
                        minX = cursorX + 70f,
                        maxX = cursorX + segWidth - 70f
                    )
                )

                if (segmentIndex % 2 == 1) {
                    val powerType = when {
                        config.worldNumber >= 10 && segmentIndex == 1 -> PowerUpType.RAINBOW_CARROT
                        config.isGoldenRift && segmentIndex == 1 -> PowerUpType.GOLDEN_BOBS
                        segmentIndex % 5 == 1 -> PowerUpType.WASABI_CARROT
                        segmentIndex % 5 == 3 -> PowerUpType.SWAG_SHADES
                        segmentIndex % 3 == 0 -> PowerUpType.SPRING_KICKS
                        else -> PowerUpType.BONK_MALLET
                    }
                    powerUps.add(
                        PowerUpPickup(
                            id = nextPowerUpId++,
                            x = cursorX + 240f,
                            y = elevatedY - 62f,
                            type = powerType
                        )
                    )
                }
            }

            val gapSize = if (cursorX + segWidth >= config.lengthWorldUnits - 650f) 0f else 155f
            cursorX += segWidth + gapSize
            segmentIndex++
        }

        platforms.add(
            PlatformEntity(
                x = config.lengthWorldUnits - 450f,
                y = GROUND_Y,
                width = 800f,
                height = 160f,
                kind = PlatformKind.SOLID
            )
        )

        return BuiltStageData(
            config = config,
            platforms = platforms,
            enemies = enemies,
            carrots = carrots,
            powerUps = powerUps,
            goalX = config.lengthWorldUnits - 180f
        )
    }
}
