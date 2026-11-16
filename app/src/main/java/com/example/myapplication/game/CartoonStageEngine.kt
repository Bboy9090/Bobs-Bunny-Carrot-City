package com.example.myapplication.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.model.CartoonHealthState
import com.example.myapplication.model.DailyWeatherMutator
import com.example.myapplication.model.DroppedShades
import com.example.myapplication.model.EnemyEntity
import com.example.myapplication.model.EnemyKind
import com.example.myapplication.model.FloatingCartoonText
import com.example.myapplication.model.ParticleDot
import com.example.myapplication.model.PlatformEntity
import com.example.myapplication.model.PlatformKind
import com.example.myapplication.model.PlayableHero
import com.example.myapplication.model.PowerUpType
import com.example.myapplication.model.StageConfig
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class CartoonStageEngine(
    val stageConfig: StageConfig,
    val selectedHero: PlayableHero = PlayableHero.BOBS_BUNNY,
    val weatherMutator: DailyWeatherMutator = DailyWeatherMutator.CLEAR_SKIES,
    private val equippedBadgeId: String,
    private val trailColor: Color,
    private val soundEngine: CartoonSoundEngine
) {
    val stageData: BuiltStageData = LevelBuilder.buildStage(stageConfig)

    // Player physics state
    var playerX by mutableFloatStateOf(160f)
    var playerY by mutableFloatStateOf(LevelBuilder.GROUND_Y)
    var playerVx by mutableFloatStateOf(0f)
    var playerVy by mutableFloatStateOf(0f)
    var facingRight by mutableStateOf(true)

    // Phase 5: Holo-Ghost Speedrun Rival & P2 Co-Op Sidekick Drone
    var ghostRivalX by mutableFloatStateOf(160f)
    var ghostRivalY by mutableFloatStateOf(LevelBuilder.GROUND_Y - 40f)
    var beatHoloGhostBonusAwarded by mutableStateOf(false)
    var sidekickDroneEnabled by mutableStateOf(true)
    var sidekickDroneX by mutableFloatStateOf(120f)
    var sidekickDroneY by mutableFloatStateOf(LevelBuilder.GROUND_Y - 120f)
    private var sidekickZapCooldown = 0f

    // Cartoon movement & slapstick states
    var isGrounded by mutableStateOf(true)
    var jumpCount by mutableIntStateOf(0)
    var isHelicopterGliding by mutableStateOf(false)
    var earSpinAngle by mutableFloatStateOf(0f)
    var isBurrowing by mutableStateOf(false)
    var isRailGrinding by mutableStateOf(false)
    var isQuakeSlamming by mutableStateOf(false)
    var coyoteHangTimer by mutableFloatStateOf(0f)
    var isShowingDontLookDown by mutableStateOf(false)
    var trollDashTimer by mutableFloatStateOf(0f)
    var invulnerableTimer by mutableFloatStateOf(0f)
    var pancakeTimer by mutableFloatStateOf(0f)

    // Cartoon Screen Shake & Squash/Stretch Physics (Hardened Polish)
    var screenShakeTimer by mutableFloatStateOf(0f)
    var screenShakeIntensity by mutableFloatStateOf(0f)
    var squashStretchX by mutableFloatStateOf(1f)
    var squashStretchY by mutableFloatStateOf(1f)

    fun triggerScreenShake(duration: Float = 0.25f, intensity: Float = 10f) {
        screenShakeTimer = duration
        screenShakeIntensity = intensity
    }

    // World 3 Rhythm Beat pulse & World 6 Molten Salsa level
    var rhythmPhaseSolid by mutableStateOf(true)
    var moltenSalsaY by mutableFloatStateOf(LevelBuilder.GROUND_Y + 140f)

    // Health, Swag, Score & Power-Up
    var healthState by mutableStateOf(CartoonHealthState.FULL_SWAG)
    val droppedShades = DroppedShades()
    var swagMeter by mutableFloatStateOf(0.25f) // 0f..1f
    var swagMultiplier by mutableIntStateOf(1)
    var collectedCarrots by mutableIntStateOf(0)
    var collectedGoldenShards by mutableIntStateOf(0)
    var activePowerUp by mutableStateOf<PowerUpType?>(null)
    var powerUpRemainingSeconds by mutableFloatStateOf(0f)

    // Fourth-wall quip bubble
    var bobsQuip by mutableStateOf(
        when (selectedHero) {
            PlayableHero.BOBS_BUNNY -> "Ain't I a smooth stinker? Next stop: ${stageConfig.title}! 🥕"
            PlayableHero.BRICK_BADGER -> "Brick the Badger here! Time to QUAKE SLAM some Foxes! 💥"
            PlayableHero.ROXY_SPARKS -> "Roxy Sparks online! Holo-Grapple calibrated for ${stageConfig.title}! ⚡"
            PlayableHero.GOLDEN_BOBS_CHAMPION -> "24-Karat Golden Bobs reporting! Let's pave the Rainbow Highway! 🌈👑"
            PlayableHero.COSMIC_EMPRESS_BUNZILLA -> "Empress Bun-Zilla on your side now! Behold the Starlight Nova! 🌌👑"
            PlayableHero.REFORMED_DON_FOXONE -> "Don Foxone Jr. reporting! My Golden Steamroller works for Bobs now! 🚜👑"
        }
    )
    var quipTimer by mutableFloatStateOf(4.0f)

    // Stage lifecycle & Panoramic Camera Lookahead
    var cameraX by mutableFloatStateOf(0f)
    var viewportWorldWidth by mutableFloatStateOf(1050f)
    var stageTimeSeconds by mutableFloatStateOf(0f)
    var isStageCleared by mutableStateOf(false)
    var isStageFailed by mutableStateOf(false)
    var frameTick by mutableIntStateOf(0)

    // Input flags
    var inputLeft by mutableStateOf(false)
    var inputRight by mutableStateOf(false)
    var inputJumpHeld by mutableStateOf(false)
    var inputBurrowHeld by mutableStateOf(false)

    val floatingTexts = mutableStateListOf<FloatingCartoonText>()
    val particles = mutableStateListOf<ParticleDot>()
    private var nextTextId = 1L

    private val baseCoyoteDuration: Float
        get() = if (equippedBadgeId == "badge_coyote_plus") 0.68f else 0.45f

    private val burrowSpeedMultiplier: Float
        get() = if (equippedBadgeId == "badge_turbo_burrow") 1.45f else 1.18f

    private val railBoostMultiplier: Float
        get() = if (equippedBadgeId == "badge_rail_spark") 1.32f else 1.0f

    private val passiveMagnetRadius: Float
        get() = when {
            activePowerUp == PowerUpType.GOLDEN_BOBS || activePowerUp == PowerUpType.RAINBOW_CARROT -> 310f
            selectedHero == PlayableHero.GOLDEN_BOBS_CHAMPION ||
                selectedHero == PlayableHero.COSMIC_EMPRESS_BUNZILLA ||
                selectedHero == PlayableHero.REFORMED_DON_FOXONE -> 235f
            sidekickDroneEnabled -> 195f
            equippedBadgeId == "badge_carrot_magnet" -> 165f
            else -> 58f
        }

    fun toggleSidekickDrone() {
        sidekickDroneEnabled = !sidekickDroneEnabled
        val stateLabel = if (sidekickDroneEnabled) "P2 SIDEKICK DRONE ONLINE! 🤖" else "P2 SIDEKICK PAUSED"
        spawnFloatingText(stateLabel, playerX, playerY - 85f, Color(0xFF00E5FF))
    }

    fun triggerJump() {
        if (isStageCleared || isStageFailed) return
        val maxJumps = when {
            selectedHero == PlayableHero.COSMIC_EMPRESS_BUNZILLA -> 4
            activePowerUp == PowerUpType.SPRING_KICKS ||
                selectedHero == PlayableHero.GOLDEN_BOBS_CHAMPION ||
                selectedHero == PlayableHero.REFORMED_DON_FOXONE -> 3
            else -> 2
        }

        if (isBurrowing) {
            isBurrowing = false
            playerVy = (if (equippedBadgeId == "badge_turbo_burrow") -860f else -760f) * selectedHero.jumpFactor
            isGrounded = false
            jumpCount = 1
            soundEngine.playBoing()
            spawnBurstOutAttack()
            spawnFloatingText("BURROW POP! 💥", playerX, playerY - 70f, Color(0xFFFFD600))
            return
        }

        if (isGrounded || coyoteHangTimer > 0f || jumpCount < maxJumps) {
            val wasCoyoteRescue = !isGrounded && coyoteHangTimer > 0f
            isGrounded = false
            isRailGrinding = false
            coyoteHangTimer = 0f
            isShowingDontLookDown = false
            jumpCount++

            val baseForce = when {
                activePowerUp == PowerUpType.SPRING_KICKS -> -840f
                jumpCount == 1 -> -715f
                else -> -655f
            }
            playerVy = baseForce * selectedHero.jumpFactor
            soundEngine.playJump()

            if (jumpCount >= 2 && (equippedBadgeId == "badge_24k_rainbow_stride" || activePowerUp == PowerUpType.RAINBOW_CARROT)) {
                spawnRainbowBridgeBeneathPlayer()
            }

            if (wasCoyoteRescue) {
                addSwag(0.18f, "LOONEY SAVE! 👀", Color(0xFF00E5FF))
                setQuip("Whew! Almost looked down there, folks!")
            } else if (jumpCount >= 2) {
                addSwag(0.06f, "AIR HOP x$jumpCount!", Color(0xFFFFEA00))
            }
            spawnDustParticles(playerX, playerY, 6, trailColor)
        }
    }

    fun triggerTrollDashOrAction() {
        if (isStageCleared || isStageFailed) return
        val dir = if (facingRight) 1f else -1f

        if (activePowerUp == PowerUpType.RAINBOW_CARROT) {
            spawnRainbowBridgeBeneathPlayer()
        }

        when (selectedHero) {
            PlayableHero.BRICK_BADGER -> {
                if (!isGrounded) {
                    isQuakeSlamming = true
                    playerVy = 1150f
                    trollDashTimer = 0.45f
                    soundEngine.playBoing()
                    spawnFloatingText("QUAKE DIVE! 🦡💥", playerX, playerY - 60f, Color(0xFFFFD600))
                } else {
                    trollDashTimer = 0.35f
                    playerVx = dir * 560f
                    triggerBadgerShockwave()
                }
            }
            PlayableHero.ROXY_SPARKS -> {
                trollDashTimer = 0.44f
                isGrounded = false
                playerVx = dir * 740f
                playerVy = -380f
                soundEngine.playBoing()
                addSwag(0.14f, "HOLO-GRAPPLE ZIP! ⚡", Color(0xFF00E5FF))
                setQuip("Grapple locked! Catch my sparks! ⚡")
                spawnDustParticles(playerX, playerY - 30f, 12, Color(0xFF00E5FF))
                flattenNearbyEnemies(190f)
            }
            PlayableHero.GOLDEN_BOBS_CHAMPION -> {
                trollDashTimer = 0.46f
                playerVx = dir * 720f
                spawnRainbowBridgeBeneathPlayer()
                soundEngine.playVictoryFanfare()
                flattenNearbyEnemies(230f)
                addSwag(0.26f, "24K RAINBOW HIGHWAY! 🌈👑", Color(0xFFFFD700))
                setQuip("Golden World royalty coming through! 🌈✨")
                spawnDustParticles(playerX, playerY - 30f, 16, Color(0xFFFFD700))
            }
            PlayableHero.COSMIC_EMPRESS_BUNZILLA -> {
                trollDashTimer = 0.50f
                playerVx = dir * 760f
                playerVy = -320f
                isGrounded = false
                triggerScreenShake(0.42f, 16f)
                squashStretchX = 0.85f
                squashStretchY = 1.25f
                spawnRainbowBridgeBeneathPlayer()
                soundEngine.playVictoryFanfare()
                flattenNearbyEnemies(310f)
                addSwag(0.30f, "STARLIGHT NOVA! 🌌👑", Color(0xFFE040FB))
                setQuip("Cosmic Starlight Nova! Bow to the Empress & Bobs! 🌌")
                spawnDustParticles(playerX, playerY - 40f, 20, Color(0xFFE040FB))
            }
            PlayableHero.REFORMED_DON_FOXONE -> {
                // Phase 5 Signature Move: Golden Steamroller Blitz + Bonus Carrot Minting!
                trollDashTimer = 0.58f
                invulnerableTimer = 0.85f
                playerVx = dir * 820f
                triggerScreenShake(0.35f, 13f)
                squashStretchX = 1.25f
                squashStretchY = 0.85f
                soundEngine.playBoing()
                val hit = flattenNearbyEnemies(320f)
                if (hit) {
                    collectedCarrots += 5 * swagMultiplier
                }
                addSwag(0.32f, "STEAMROLLER BLITZ! 🚜💥", Color(0xFFFFD700))
                setQuip("Outta the way! Don Foxone's Golden Steamroller is on Bobs's team now! 🚜")
                spawnDustParticles(playerX, playerY - 25f, 22, Color(0xFFFFD700))
            }
            PlayableHero.BOBS_BUNNY -> {
                trollDashTimer = 0.38f
                playerVx = dir * 640f
                squashStretchX = 1.2f
                squashStretchY = 0.88f
                soundEngine.playBoing()
                val flattenedAny = flattenNearbyEnemies(175f)
                if (flattenedAny) {
                    triggerScreenShake(0.24f, 8f)
                    addSwag(0.25f, "TROLL COUNTER! 🎭", Color(0xFFFF4081))
                    setQuip("Hold this anvil for me, Foxy! 🔨")
                } else {
                    addSwag(0.08f, "SWAG DASH! ✨", Color(0xFF00E5FF))
                }
                spawnDustParticles(playerX, playerY - 30f, 10, Color(0xFFFFD700))
            }
        }
    }

    private fun spawnRainbowBridgeBeneathPlayer() {
        val bridgeY = (playerY + 6f).coerceIn(220f, LevelBuilder.GROUND_Y - 10f)
        stageData.platforms.add(
            PlatformEntity(
                x = playerX - 60f,
                y = bridgeY,
                width = 240f,
                height = 20f,
                kind = PlatformKind.RAINBOW_BRIDGE
            )
        )
        spawnFloatingText("RAINBOW BRIDGE! 🌈", playerX, bridgeY - 45f, Color(0xFFE040FB))
    }

    private fun triggerBadgerShockwave() {
        isQuakeSlamming = false
        soundEngine.playBoing()
        triggerScreenShake(0.38f, 15f)
        squashStretchX = 1.35f
        squashStretchY = 0.65f
        spawnDustParticles(playerX, playerY, 18, Color(0xFFFFD600))
        val hit = flattenNearbyEnemies(260f)
        if (hit) {
            addSwag(0.28f, "BADGER QUAKE SLAM! 💥", Color(0xFFFFD600))
            setQuip("Nobody stands still when Brick hits the floor! 🦡")
        } else {
            addSwag(0.10f, "SEISMIC THUMP! 💥", Color(0xFFFF9100))
        }
    }

    private fun flattenNearbyEnemies(radiusX: Float): Boolean {
        var flattenedAny = false
        for (enemy in stageData.enemies) {
            if (!enemy.isFlattenedAccordion && !enemy.isLaunchedSkyward &&
                abs(enemy.x - playerX) < radiusX && abs(enemy.y - playerY) < 130f
            ) {
                if (enemy.kind.isBoss) {
                    damageBoss(enemy, "${selectedHero.specialMoveLabel} HIT! 💥")
                } else {
                    enemy.isFlattenedAccordion = true
                    enemy.flattenedTimer = 4.2f
                    collectedCarrots += 3 * swagMultiplier
                }
                flattenedAny = true
            }
        }
        return flattenedAny
    }

    fun stepFrame(rawDt: Float) {
        if (isStageCleared || isStageFailed) return
        val dt = rawDt.coerceIn(0.001f, 0.04f)
        val enemyTimeScale = (if (activePowerUp == PowerUpType.SWAG_SHADES) 0.42f else 1.0f) * weatherMutator.speedScale

        stageTimeSeconds += dt
        frameTick++

        // Update Holo-Ghost Speedrun Rival along Par Time trajectory
        val ghostSpeed = (stageData.goalX - 160f) / stageConfig.parTimeSeconds.coerceAtLeast(10f)
        ghostRivalX = (160f + stageTimeSeconds * ghostSpeed).coerceAtMost(stageData.goalX)
        ghostRivalY = LevelBuilder.GROUND_Y - 38f - abs(sin(stageTimeSeconds * 4.2f)) * 95f

        // Update P2 Co-Op Sidekick Drone
        if (sidekickDroneEnabled) {
            val targetDroneX = playerX - (if (facingRight) 65f else -65f)
            val targetDroneY = playerY - 115f + sin(stageTimeSeconds * 5f) * 14f
            sidekickDroneX += (targetDroneX - sidekickDroneX) * (dt * 7.5f).coerceIn(0f, 1f)
            sidekickDroneY += (targetDroneY - sidekickDroneY) * (dt * 7.5f).coerceIn(0f, 1f)
            sidekickZapCooldown = (sidekickZapCooldown - dt).coerceAtLeast(0f)
            if (sidekickZapCooldown <= 0f) {
                val targetEnemy = stageData.enemies.firstOrNull {
                    !it.isFlattenedAccordion && !it.isLaunchedSkyward &&
                        !it.kind.isBoss && abs(it.x - sidekickDroneX) < 150f
                }
                if (targetEnemy != null) {
                    targetEnemy.isFlattenedAccordion = true
                    targetEnemy.flattenedTimer = 3.5f
                    sidekickZapCooldown = 3.2f
                    collectedCarrots += 2 * swagMultiplier
                    spawnFloatingText("P2 PIE ZAP! 🥧", targetEnemy.x, targetEnemy.y - 60f, Color(0xFF00E5FF))
                }
            }
        }

        rhythmPhaseSolid = ((stageTimeSeconds / 1.4f).toInt() % 2 == 0) || equippedBadgeId == "badge_rail_spark"

        if (stageConfig.biome.hasRisingSalsa) {
            val wave = sin(stageTimeSeconds * 1.7f)
            moltenSalsaY = LevelBuilder.GROUND_Y + 25f - (wave * 42f)
        }

        if (invulnerableTimer > 0f) invulnerableTimer = (invulnerableTimer - dt).coerceAtLeast(0f)
        if (trollDashTimer > 0f) trollDashTimer = (trollDashTimer - dt).coerceAtLeast(0f)
        if (screenShakeTimer > 0f) screenShakeTimer = (screenShakeTimer - dt).coerceAtLeast(0f)

        // Dynamic cartoon squash & stretch decay
        val targetSquashX: Float
        val targetSquashY: Float
        when {
            healthState == CartoonHealthState.PANCAKE -> {
                targetSquashX = 1.62f
                targetSquashY = 0.42f
            }
            !isGrounded && playerVy < -200f -> {
                targetSquashX = 0.86f
                targetSquashY = 1.20f
            }
            !isGrounded && playerVy > 400f -> {
                targetSquashX = 0.88f
                targetSquashY = 1.16f
            }
            isGrounded && abs(playerVx) > 420f -> {
                targetSquashX = 1.15f
                targetSquashY = 0.88f
            }
            else -> {
                targetSquashX = 1.0f
                targetSquashY = 1.0f
            }
        }
        squashStretchX += (targetSquashX - squashStretchX) * (dt * 12f).coerceIn(0f, 1f)
        squashStretchY += (targetSquashY - squashStretchY) * (dt * 12f).coerceIn(0f, 1f)

        if (pancakeTimer > 0f) {
            pancakeTimer = (pancakeTimer - dt).coerceAtLeast(0f)
            if (pancakeTimer == 0f && healthState == CartoonHealthState.PANCAKE) {
                healthState = CartoonHealthState.SCUFFED
            }
        }
        if (quipTimer > 0f) quipTimer = (quipTimer - dt).coerceAtLeast(0f)

        if (powerUpRemainingSeconds > 0f) {
            powerUpRemainingSeconds = (powerUpRemainingSeconds - dt).coerceAtLeast(0f)
            if (powerUpRemainingSeconds == 0f) {
                activePowerUp = null
            }
        }

        val baseSwagMult = when {
            activePowerUp == PowerUpType.RAINBOW_CARROT || swagMeter >= 0.85f -> 3
            swagMeter >= 0.45f -> 2
            else -> 1
        }
        swagMultiplier = baseSwagMult + weatherMutator.carrotMultiplierBonus
        swagMeter = (swagMeter - dt * 0.024f).coerceIn(0f, 1f)

        val moveDir = when {
            inputRight && !inputLeft -> 1f
            inputLeft && !inputRight -> -1f
            else -> 0f
        }
        if (moveDir != 0f) {
            facingRight = moveDir > 0f
        }

        val baseSpeed = (when {
            isRailGrinding -> 540f * railBoostMultiplier
            trollDashTimer > 0f -> 640f
            activePowerUp == PowerUpType.WASABI_CARROT -> 550f
            activePowerUp == PowerUpType.GOLDEN_BOBS || activePowerUp == PowerUpType.RAINBOW_CARROT -> 525f
            isBurrowing -> 355f * burrowSpeedMultiplier
            healthState == CartoonHealthState.PANCAKE -> 230f
            else -> 365f * selectedHero.speedFactor
        }) * weatherMutator.speedScale

        if (trollDashTimer <= 0f) {
            playerVx = if (isRailGrinding && moveDir == 0f) {
                (if (facingRight) 1f else -1f) * baseSpeed
            } else {
                moveDir * baseSpeed
            }
        }

        if (inputBurrowHeld && isGrounded) {
            if (!isBurrowing) {
                isBurrowing = true
                isRailGrinding = false
                spawnDustParticles(playerX, playerY, 8, Color(0xFF795548))
            }
        } else if (!inputBurrowHeld && isBurrowing) {
            isBurrowing = false
            spawnBurstOutAttack()
        }

        val prevY = playerY
        playerX = (playerX + playerVx * dt).coerceAtLeast(40f)

        val supportingPlatform = findSupportingPlatform(playerX, playerY, prevY)

        if (isGrounded && supportingPlatform == null) {
            isGrounded = false
            isRailGrinding = false
            coyoteHangTimer = baseCoyoteDuration
            isShowingDontLookDown = true
            playerVy = 0f
            spawnFloatingText("DON'T LOOK DOWN! 👀", playerX, playerY - 82f, Color(0xFFFFEA00))
        }

        val worldGravityScale = stageConfig.biome.gravityMultiplier * weatherMutator.gravityScale

        if (!isGrounded) {
            if (coyoteHangTimer > 0f && playerVy >= 0f) {
                coyoteHangTimer = (coyoteHangTimer - dt).coerceAtLeast(0f)
                playerVy = 0f
                earSpinAngle += dt * 720f
                if (coyoteHangTimer == 0f) {
                    isShowingDontLookDown = false
                }
            } else {
                isShowingDontLookDown = false
                isHelicopterGliding = inputJumpHeld && playerVy > 0f && !isQuakeSlamming
                val gravity = (if (isHelicopterGliding) 410f else 1650f) * worldGravityScale
                val maxFallSpeed = if (isHelicopterGliding) 140f else 1050f
                playerVy = (playerVy + gravity * dt).coerceAtMost(maxFallSpeed)
                if (isHelicopterGliding) {
                    earSpinAngle += dt * 1080f
                }
            }
        } else {
            isHelicopterGliding = false
        }

        playerY += playerVy * dt

        val landedPlatform = findSupportingPlatform(playerX, playerY, prevY)
        if (landedPlatform != null && playerVy >= 0f) {
            playerY = landedPlatform.y
            if (isQuakeSlamming) {
                triggerBadgerShockwave()
            }
            when (landedPlatform.kind) {
                PlatformKind.BOUNCY_AWNING -> {
                    playerVy = -860f * selectedHero.jumpFactor
                    isGrounded = false
                    isRailGrinding = false
                    jumpCount = 1
                    triggerScreenShake(0.18f, 7f)
                    squashStretchX = 1.35f
                    squashStretchY = 0.65f
                    soundEngine.playBoing()
                    addSwag(0.12f, "AWNING BOUNCE! 🎪", Color(0xFFFF4081))
                    spawnDustParticles(playerX, playerY, 8, Color(0xFF00E5FF))
                }
                PlatformKind.UPDRAFT_VENT -> {
                    isRailGrinding = false
                    if (inputJumpHeld) {
                        playerVy = -640f
                        isGrounded = false
                        isHelicopterGliding = true
                        addSwag(0.08f, "UPDRAFT SOAR! 🌪️", Color(0xFF76FF03))
                    } else {
                        playerVy = 0f
                        isGrounded = true
                        jumpCount = 0
                    }
                }
                PlatformKind.CABLE_CAR_RAIL -> {
                    playerVy = 0f
                    isGrounded = true
                    jumpCount = 0
                    if (!isRailGrinding) {
                        isRailGrinding = true
                        addSwag(0.14f, "CABLE RAIL GRIND! 🚋✨", Color(0xFFFFD740))
                    }
                    if (frameTick % 4 == 0) {
                        spawnDustParticles(playerX, playerY, 3, Color(0xFFFFEA00))
                    }
                }
                else -> {
                    if (!isGrounded && playerVy > 420f) {
                        squashStretchX = 1.30f
                        squashStretchY = 0.70f
                        triggerScreenShake(0.12f, 4.5f)
                        spawnDustParticles(playerX, playerY, 6, Color(0xFFEEEEEE))
                    }
                    playerVy = 0f
                    isGrounded = true
                    isRailGrinding = false
                    jumpCount = 0
                }
            }
        }

        if (stageConfig.biome.hasRisingSalsa && playerY >= moltenSalsaY && invulnerableTimer <= 0f && !isBurrowing) {
            playerVy = -780f
            isGrounded = false
            invulnerableTimer = 1.2f
            soundEngine.playBoing()
            addSwag(0.05f, "HOT SALSA HOP! 🌶️🔥", Color(0xFFFF3D00))
            setQuip("YOWZA! That salsa's extra spicy! 🌶️")
        }

        for (plat in stageData.platforms) {
            if (plat.kind == PlatformKind.PAINTED_TUNNEL_WALL) {
                val closeX = abs(playerX - (plat.x + plat.width / 2f)) < 42f
                val closeY = playerY > plat.y && playerY <= plat.y + plat.height + 15f
                if (closeX && closeY) {
                    if (trollDashTimer > 0f || isBurrowing || swagMeter >= 0.45f || activePowerUp != null) {
                        collectedCarrots += 5 * swagMultiplier
                        addSwag(0.2f, "PAINTED TUNNEL TRICK! 🎨+5", Color(0xFFFFD700))
                        setQuip("Painted that tunnel myself! Beep-beep! 🎨")
                        playerX += if (facingRight) 65f else -65f
                    }
                }
            }
        }

        if (playerY > LevelBuilder.GROUND_Y + 240f) {
            if (healthState == CartoonHealthState.FULL_SWAG) {
                healthState = CartoonHealthState.SCUFFED
                playerY = LevelBuilder.GROUND_Y - 120f
                playerVy = -720f
                playerX = (playerX - 90f).coerceAtLeast(120f)
                invulnerableTimer = 2.0f
                soundEngine.playBoing()
                spawnFloatingText("CARTOON SPRING RESCUE!", playerX, playerY, Color(0xFFFF9100))
            } else {
                isStageFailed = true
                soundEngine.playBonk()
            }
        }

        for (carrot in stageData.carrots) {
            if (!carrot.collected) {
                val dist = hypot(carrot.x - playerX, (carrot.y - (playerY - 36f)))
                if (dist < passiveMagnetRadius) {
                    carrot.x += (playerX - carrot.x) * (dt * 9.5f)
                    carrot.y += ((playerY - 36f) - carrot.y) * (dt * 9.5f)
                }
                if (dist < 46f) {
                    carrot.collected = true
                    if (carrot.isGoldenShard) {
                        collectedGoldenShards += 1
                        collectedCarrots += 10 * swagMultiplier
                        soundEngine.playCarrotPickup(isGolden = true)
                        addSwag(0.25f, "GOLDEN SHARD! 🌟", Color(0xFFFFD700))
                        setQuip("The Golden World shines brighter! ✨")
                    } else {
                        collectedCarrots += 1 * swagMultiplier
                        soundEngine.playCarrotPickup(isGolden = false)
                        addSwag(0.02f, null, Color.Unspecified)
                    }
                }
            }
        }

        for (pickup in stageData.powerUps) {
            if (!pickup.collected) {
                val dist = hypot(pickup.x - playerX, pickup.y - (playerY - 36f))
                if (dist < 52f) {
                    pickup.collected = true
                    activePowerUp = pickup.type
                    powerUpRemainingSeconds = pickup.type.durationSeconds
                    healthState = CartoonHealthState.FULL_SWAG
                    soundEngine.playVictoryFanfare()
                    addSwag(0.3f, "${pickup.type.emoji} ${pickup.type.title.uppercase()}!", pickup.type.color)
                    setQuip(pickup.type.subtitle)
                }
            }
        }

        if (droppedShades.active) {
            droppedShades.timer -= dt
            droppedShades.x += droppedShades.vx * dt
            droppedShades.y += droppedShades.vy * dt
            droppedShades.vy += 900f * dt
            if (droppedShades.y > LevelBuilder.GROUND_Y - 18f) {
                droppedShades.y = LevelBuilder.GROUND_Y - 18f
                droppedShades.vy = -droppedShades.vy * 0.55f
            }
            if (hypot(droppedShades.x - playerX, droppedShades.y - (playerY - 30f)) < 54f && invulnerableTimer < 1.1f) {
                droppedShades.active = false
                healthState = CartoonHealthState.FULL_SWAG
                soundEngine.playCarrotPickup(isGolden = true)
                addSwag(0.18f, "SHADES RECOVERED! 🕶️", Color(0xFF00E5FF))
                setQuip("Nobody touches the frames! 🕶️")
            } else if (droppedShades.timer <= 0f) {
                droppedShades.active = false
            }
        }

        val heistSpeedBoost = if (stageConfig.isHeistStage) {
            1.0f + (collectedCarrots.coerceAtMost(40) * 0.01f)
        } else {
            1.0f
        }

        updateEnemies(dt * enemyTimeScale * heistSpeedBoost)

        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.alpha -= dt * 1.6f
            if (p.alpha <= 0f) pIter.remove()
        }
        if (abs(playerVx) > 220f && frameTick % 4 == 0) {
            particles.add(
                ParticleDot(
                    x = playerX - (if (facingRight) 20f else -20f),
                    y = playerY - 10f,
                    vx = -playerVx * 0.15f,
                    vy = Random.nextFloat() * -40f,
                    color = activePowerUp?.color ?: trailColor,
                    size = 9f
                )
            )
        }

        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val item = tIter.next()
            item.y -= 48f * dt
            item.lifeRemaining -= dt
            if (item.lifeRemaining <= 0f) tIter.remove()
        }

        // Wide panoramic forward lookahead camera tracking:
        // situates player ~26% from left edge with dynamic forward lead in movement direction
        val lookahead = if (facingRight) 160f else -80f
        val targetCam = (playerX - (viewportWorldWidth * 0.26f) + lookahead).coerceAtLeast(0f)
        cameraX += (targetCam - cameraX) * (dt * 6.5f).coerceIn(0f, 1f)

        val bossAlive = stageData.enemies.any { it.kind.isBoss && it.bossHp > 0 }
        if (playerX >= stageData.goalX && (!stageConfig.isBossStage || !bossAlive)) {
            if (stageTimeSeconds <= stageConfig.parTimeSeconds && !beatHoloGhostBonusAwarded) {
                beatHoloGhostBonusAwarded = true
                collectedCarrots += 25
                spawnFloatingText("OUTRAN HOLO-GHOST! +25 🥕", playerX, playerY - 95f, Color(0xFFFFD700))
            }
            isStageCleared = true
            soundEngine.playVictoryFanfare()
            setQuip(
                if (stageConfig.worldNumber >= 10) {
                    "24-Karat Multiverse Crown claimed! You outran the Holo-Ghost! 👑🥕"
                } else {
                    "World ${stageConfig.worldNumber} stage cleared! Onward to the Golden World! 🥕👑"
                }
            )
        }
    }

    private fun updateEnemies(dt: Float) {
        for (enemy in stageData.enemies) {
            if (enemy.isLaunchedSkyward) {
                enemy.x += enemy.vx * dt
                enemy.y += enemy.vy * dt
                enemy.vy += 1200f * dt
                continue
            }
            if (enemy.isFlattenedAccordion) {
                enemy.flattenedTimer -= dt
                if (enemy.flattenedTimer <= 0f) {
                    enemy.isFlattenedAccordion = false
                }
                continue
            }

            if (enemy.bossStunTimer > 0f) {
                enemy.bossStunTimer = (enemy.bossStunTimer - dt).coerceAtLeast(0f)
            } else {
                enemy.x += enemy.vx * dt
                if (enemy.x <= enemy.minX) {
                    enemy.x = enemy.minX
                    enemy.vx = abs(enemy.vx)
                } else if (enemy.x >= enemy.maxX) {
                    enemy.x = enemy.maxX
                    enemy.vx = -abs(enemy.vx)
                }

                if (enemy.kind == EnemyKind.GREAT_FURTINI_BOSS ||
                    enemy.kind == EnemyKind.GOLDEN_GUARDIAN_BOSS ||
                    enemy.kind == EnemyKind.OMEGA_ANIMATOR_BOSS ||
                    enemy.kind == EnemyKind.ILLUSION_PHANTOM
                ) {
                    enemy.teleportTimer += dt
                    if (enemy.teleportTimer >= 3.0f) {
                        enemy.teleportTimer = 0f
                        spawnDustParticles(enemy.x, enemy.y - 40f, 10, Color(0xFFFFD700))
                    }
                }
            }

            for (plat in stageData.platforms) {
                if (plat.kind == PlatformKind.PAINTED_TUNNEL_WALL &&
                    abs(enemy.x - (plat.x + plat.width / 2f)) < 34f
                ) {
                    enemy.isFlattenedAccordion = true
                    enemy.flattenedTimer = 3.8f
                    enemy.vx = -enemy.vx
                    spawnFloatingText("WALL BONK! 🪗", enemy.x, enemy.y - 60f, Color(0xFFFFEA00))
                }
            }

            val hitRadiusX = if (enemy.kind.isBoss) 82f else 44f
            val hitRadiusY = if (enemy.kind.isBoss) 88f else 54f
            val dx = abs(playerX - enemy.x)
            val dy = abs(playerY - enemy.y)

            if (dx < hitRadiusX && dy < hitRadiusY) {
                val isStompingFromAbove = !isGrounded && playerVy > 0f && (playerY < enemy.y - 18f)
                val isInvinciblePower = activePowerUp == PowerUpType.WASABI_CARROT ||
                    activePowerUp == PowerUpType.GOLDEN_BOBS ||
                    activePowerUp == PowerUpType.RAINBOW_CARROT ||
                    activePowerUp == PowerUpType.BONK_MALLET ||
                    trollDashTimer > 0f ||
                    isQuakeSlamming

                if (isBurrowing) {
                    if (enemy.kind.isBoss) {
                        damageBoss(enemy, "UNDERGROUND FLIP! 💥")
                        isBurrowing = false
                        playerVy = -720f
                        isGrounded = false
                    } else {
                        enemy.isLaunchedSkyward = true
                        enemy.vy = -820f
                        enemy.vx = if (facingRight) 220f else -220f
                        collectedCarrots += 3 * swagMultiplier
                        soundEngine.playBoing()
                        addSwag(0.18f, "BURROW LAUNCH! 🚀", Color(0xFF00E676))
                    }
                } else if (isStompingFromAbove || isInvinciblePower) {
                    if (enemy.kind.isBoss) {
                        damageBoss(enemy, "BOSS STOMP! 🥾")
                        playerVy = -760f
                        isGrounded = false
                    } else {
                        enemy.isFlattenedAccordion = true
                        enemy.flattenedTimer = 4.5f
                        playerVy = -640f
                        isGrounded = false
                        collectedCarrots += 3 * swagMultiplier
                        soundEngine.playBoing()
                        addSwag(0.16f, "ACCORDION SQUASH! 🪗", Color(0xFFFFD600))
                    }
                } else if (activePowerUp == PowerUpType.SWAG_SHADES) {
                    enemy.isFlattenedAccordion = true
                    enemy.flattenedTimer = 4.0f
                    addSwag(0.2f, "SHADES AUTO-COUNTER! 🕶️", Color(0xFF00E5FF))
                } else if (invulnerableTimer <= 0f) {
                    onPlayerHitByEnemy(enemy)
                }
            }
        }
    }

    private fun damageBoss(boss: EnemyEntity, label: String) {
        if (boss.bossStunTimer > 0f) return
        boss.bossHp = (boss.bossHp - 1).coerceAtLeast(0)
        boss.bossStunTimer = 2.1f
        boss.vx = -boss.vx * 1.18f
        triggerScreenShake(0.34f, 14f)
        squashStretchX = 1.30f
        squashStretchY = 0.70f
        soundEngine.playBoing()
        addSwag(0.3f, "$label (${boss.bossHp} HP Left!)", Color(0xFFFF1744))
        spawnDustParticles(boss.x, boss.y - 40f, 16, Color(0xFFFFD600))

        if ((boss.kind == EnemyKind.GREAT_FURTINI_BOSS ||
                boss.kind == EnemyKind.GOLDEN_GUARDIAN_BOSS ||
                boss.kind == EnemyKind.OMEGA_ANIMATOR_BOSS) && boss.bossHp > 0
        ) {
            val mid = (boss.minX + boss.maxX) * 0.5f
            boss.x = if (boss.x < mid) boss.maxX - 180f else boss.minX + 180f
            spawnFloatingText("ENCORE WARP! ✨", boss.x, boss.y - 100f, Color(0xFFFFD700))
        }

        if (boss.bossHp <= 0) {
            triggerScreenShake(0.48f, 18f)
            boss.isLaunchedSkyward = true
            boss.vy = -950f
            collectedCarrots += 75
            collectedGoldenShards += 2
            val remainingBosses = stageData.enemies.count { it.kind.isBoss && it.bossHp > 0 }
            if (remainingBosses == 0) {
                setQuip("${boss.kind.title} is officially FOIL-ED! 🥕👑")
                isStageCleared = true
                soundEngine.playVictoryFanfare()
            } else {
                setQuip("${boss.kind.title} down! $remainingBosses Boss(es) left in the Gauntlet! 🔥")
            }
        } else {
            val phaseNum = (boss.bossMaxHp - boss.bossHp + 1).coerceAtLeast(2)
            spawnFloatingText("PHASE $phaseNum! FASTER! ⚡", boss.x, boss.y - 135f, Color(0xFFFF9100))
            setQuip("Phase $phaseNum! Keep the pressure on ${boss.kind.title}! 😂")
        }
    }

    private fun onPlayerHitByEnemy(enemy: EnemyEntity) {
        invulnerableTimer = 1.65f
        triggerScreenShake(0.40f, 16f)
        squashStretchX = 1.45f
        squashStretchY = 0.55f
        soundEngine.playBonk()
        playerVy = -460f
        playerVx = if (playerX < enemy.x) -260f else 260f
        isGrounded = false
        isRailGrinding = false

        when (healthState) {
            CartoonHealthState.FULL_SWAG -> {
                if (enemy.kind == EnemyKind.WRENCH_FOX || enemy.kind.isBoss) {
                    healthState = CartoonHealthState.PANCAKE
                    pancakeTimer = 3.5f
                    spawnFloatingText("PANCAKE SQUASH! 🥞", playerX, playerY - 70f, Color(0xFFFF9100))
                    setQuip("Okay, I'm flat... but still looking fresh! 🥞")
                } else {
                    healthState = CartoonHealthState.SCUFFED
                    spawnFloatingText("MY SHADES! CATCH 'EM! 🕶️", playerX, playerY - 70f, Color(0xFFFF4081))
                    setQuip("Hey! Watch the vintage frames! Grab 'em!")
                }
                droppedShades.active = true
                droppedShades.x = playerX + (if (facingRight) 65f else -65f)
                droppedShades.y = playerY - 60f
                droppedShades.vx = if (facingRight) 110f else -110f
                droppedShades.vy = -420f
                droppedShades.timer = 3.8f
            }
            CartoonHealthState.SCUFFED, CartoonHealthState.PANCAKE -> {
                collectedCarrots = (collectedCarrots - 10).coerceAtLeast(0)
                healthState = CartoonHealthState.FULL_SWAG
                playerX = (playerX - 180f).coerceAtLeast(120f)
                playerY = LevelBuilder.GROUND_Y - 80f
                playerVy = -500f
                spawnFloatingText("BURROW CHECKPOINT SAVE! (-10 🥕)", playerX, playerY - 70f, Color(0xFFFF5252))
                setQuip("Good thing I dug a backup burrow right here! 🕳️")
            }
        }
    }

    private fun spawnBurstOutAttack() {
        spawnDustParticles(playerX, playerY, 12, Color(0xFF8D6E63))
        for (enemy in stageData.enemies) {
            if (!enemy.isLaunchedSkyward && abs(enemy.x - playerX) < 130f && abs(enemy.y - playerY) < 90f) {
                if (enemy.kind.isBoss) {
                    damageBoss(enemy, "BURROW UPPERCUT! 💥")
                } else {
                    enemy.isLaunchedSkyward = true
                    enemy.vy = -880f
                    enemy.vx = if (facingRight) 200f else -200f
                    collectedCarrots += 4 * swagMultiplier
                    addSwag(0.22f, "UPPERCUT LAUNCH! 🚀", Color(0xFF76FF03))
                }
            }
        }
    }

    private fun findSupportingPlatform(px: Float, py: Float, prevY: Float): PlatformEntity? =
        stageData.platforms.firstOrNull { plat ->
            val isBeatInactive = plat.kind == PlatformKind.RHYTHM_BEAT_BLOCK && !rhythmPhaseSolid
            plat.kind != PlatformKind.PAINTED_TUNNEL_WALL &&
                !isBeatInactive &&
                px >= plat.x - 18f &&
                px <= plat.x + plat.width + 18f &&
                py >= plat.y - 12f &&
                prevY <= plat.y + 28f
        }

    private fun addSwag(amount: Float, popupText: String?, color: Color) {
        swagMeter = (swagMeter + amount).coerceIn(0f, 1f)
        if (popupText != null) {
            spawnFloatingText(popupText, playerX, playerY - 75f, color)
        }
    }

    private fun setQuip(text: String) {
        bobsQuip = text
        quipTimer = 3.6f
    }

    private fun spawnFloatingText(text: String, x: Float, y: Float, color: Color) {
        floatingTexts.add(FloatingCartoonText(nextTextId++, text, x, y, color))
    }

    private fun spawnDustParticles(x: Float, y: Float, count: Int, color: Color) {
        repeat(count) {
            particles.add(
                ParticleDot(
                    x = x + Random.nextFloat() * 24f - 12f,
                    y = y - Random.nextFloat() * 16f,
                    vx = Random.nextFloat() * 180f - 90f,
                    vy = Random.nextFloat() * -160f - 20f,
                    color = color,
                    size = Random.nextFloat() * 8f + 5f
                )
            )
        }
    }

    fun calculateStarsEarned(): Int {
        val beatParTime = stageTimeSeconds <= stageConfig.parTimeSeconds
        return when {
            collectedCarrots >= 30 && (collectedGoldenShards >= 1 || beatParTime) -> 3
            collectedCarrots >= 16 || beatParTime -> 2
            else -> 1
        }
    }
}
