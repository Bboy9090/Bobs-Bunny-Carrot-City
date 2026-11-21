package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.data.GameProgressRepository
import com.example.myapplication.model.StageConfig
import com.example.myapplication.ui.screens.AsymmetricMultiplayerScreen
import com.example.myapplication.ui.screens.CarrotCityLifeScreen
import com.example.myapplication.ui.screens.CartoonStudioScreen
import com.example.myapplication.ui.screens.GamePlayScreen
import com.example.myapplication.ui.screens.HomeHubScreen
import com.example.myapplication.ui.screens.MasterRoadmapScreen
import com.example.myapplication.ui.screens.SwagShopScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

private sealed interface AppDestination {
    data object Hub : AppDestination
    data class StagePlay(val stage: StageConfig) : AppDestination
    data object CityLife : AppDestination
    data object CartoonStudio : AppDestination
    data object SwagShop : AppDestination
    data object Multiplayer : AppDestination
    data object MasterRoadmap : AppDestination
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = GameProgressRepository(applicationContext)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0E0921)
                ) {
                    BobsBunnyCarrotCityApp(repository = repository)
                }
            }
        }
    }
}

@Composable
private fun BobsBunnyCarrotCityApp(repository: GameProgressRepository) {
    val profile by repository.state.collectAsState()
    val soundEngine = remember { CartoonSoundEngine() }
    var destination by remember {
        mutableStateOf<AppDestination>(AppDestination.Hub)
    }

    DisposableEffect(Unit) {
        onDispose { soundEngine.release() }
    }

    BackHandler(enabled = destination !is AppDestination.Hub) {
        destination = AppDestination.Hub
    }

    when (val current = destination) {
        is AppDestination.Hub -> {
            HomeHubScreen(
                profile = profile,
                onSelectStage = { stage ->
                    Log.i("BobsBunnyDiorama", "StagePlay navigation requested: " + stage.code)
                    soundEngine.playJump()
                    destination = AppDestination.StagePlay(stage)
                },
                onOpenCityLife = {
                    soundEngine.playCarrotPickup(isGolden = true)
                    destination = AppDestination.CityLife
                },
                onOpenCartoonStudio = {
                    soundEngine.playVictoryFanfare()
                    destination = AppDestination.CartoonStudio
                },
                onOpenSwagShop = {
                    soundEngine.playCarrotPickup()
                    destination = AppDestination.SwagShop
                },
                onOpenMultiplayer = {
                    soundEngine.playBoing()
                    destination = AppDestination.Multiplayer
                },
                onOpenMasterRoadmap = {
                    soundEngine.playCarrotPickup(isGolden = true)
                    destination = AppDestination.MasterRoadmap
                }
            )
        }
        is AppDestination.StagePlay -> {
            GamePlayScreen(
                stageConfig = current.stage,
                profile = profile,
                soundEngine = soundEngine,
                onStageCompleted = { carrots, shards, stars, clearTimeSec ->
                    if (current.stage.id <= 20) {
                        repository.recordStageClear(
                            stageId = current.stage.id,
                            carrotsEarned = carrots,
                            shardsEarned = shards,
                            starsEarned = stars,
                            clearTimeSeconds = clearTimeSec
                        )
                    } else {
                        repository.addCarrotsAndShards(carrots = carrots, shards = shards)
                    }
                },
                onExitToMenu = {
                    destination = AppDestination.Hub
                }
            )
        }
        is AppDestination.CityLife -> {
            CarrotCityLifeScreen(
                profile = profile,
                onSelectHero = { hero ->
                    soundEngine.playVictoryFanfare()
                    repository.selectHero(hero)
                },
                onSelectMutator = { mutator ->
                    soundEngine.playCarrotPickup(isGolden = true)
                    repository.selectWeatherMutator(mutator)
                },
                onClaimQuest = { questId, carrots, shards ->
                    val ok = repository.claimCityQuest(questId, carrots, shards)
                    if (ok) soundEngine.playVictoryFanfare() else soundEngine.playBonk()
                    ok
                },
                onWarpToStage = { stage ->
                    soundEngine.playBoing()
                    destination = AppDestination.StagePlay(stage)
                },
                onBack = { destination = AppDestination.Hub }
            )
        }
        is AppDestination.CartoonStudio -> {
            CartoonStudioScreen(
                profile = profile,
                onSaveStudioCode = { code ->
                    soundEngine.playCarrotPickup(isGolden = true)
                    repository.saveCustomStudioCode(code)
                },
                onPlayCustomStage = { customStage ->
                    soundEngine.playVictoryFanfare()
                    destination = AppDestination.StagePlay(customStage)
                },
                onBack = { destination = AppDestination.Hub }
            )
        }
        is AppDestination.SwagShop -> {
            SwagShopScreen(
                profile = profile,
                onBuyOrEquip = { itemId, categoryName, cost ->
                    val ok = repository.purchaseOrEquipItem(itemId, categoryName, cost)
                    if (ok) soundEngine.playCarrotPickup(isGolden = true) else soundEngine.playBonk()
                    ok
                },
                onBack = { destination = AppDestination.Hub }
            )
        }
        is AppDestination.Multiplayer -> {
            AsymmetricMultiplayerScreen(
                soundEngine = soundEngine,
                onMatchReward = { carrots, won ->
                    repository.addCarrotsAndShards(carrots = carrots, shards = if (won) 1 else 0, mpWin = won)
                },
                onBack = { destination = AppDestination.Hub }
            )
        }
        is AppDestination.MasterRoadmap -> {
            MasterRoadmapScreen(
                profile = profile,
                onBack = { destination = AppDestination.Hub }
            )
        }
    }
}
