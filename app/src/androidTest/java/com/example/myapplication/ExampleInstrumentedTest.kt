package com.example.myapplication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.audio.CartoonSoundEngine
import com.example.myapplication.data.GameProgressRepository
import com.example.myapplication.model.PlayableHero
import com.example.myapplication.ui.game3d.CarrotCity3DRenderer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapplication", appContext.packageName)
    }

    @Test
    fun launchAndNavigation_hubToCityLifeSwagAndBackWorks() {
        composeRule.onNodeWithText("BOBS BUNNY: CARROT CITY", substring = true).assertIsDisplayed()

        composeRule.onNodeWithText("City & Trophies", substring = true).performClick()
        composeRule.onNodeWithText("CARROT CITY LIFE, HEROES & HALL OF FAME", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("World Map").performClick()

        composeRule.onNodeWithText("Swag", substring = true).performClick()
        composeRule.onNodeWithText("SWAG SHOP", substring = true, ignoreCase = true).assertIsDisplayed()
        composeRule.onNodeWithText("Back to Hub", substring = true).performClick()

        composeRule.onNodeWithText("BOBS BUNNY: CARROT CITY", substring = true).assertIsDisplayed()
    }

    @Test
    fun stageRosterAndGameplay_launchW1BossAndTouchControlsRespond() {
        composeRule.onNodeWithText("STAGE ROSTER", substring = true).performClick()
        composeRule.onNodeWithTag("world_filter_tabs")
            .performScrollToNode(hasText("W1: Carrot City", substring = true))
        composeRule.onNodeWithText("W1: Carrot City", substring = true).performClick()
        composeRule.onNodeWithTag("stage_roster_list")
            .performScrollToNode(hasText("W1-BOSS", substring = true))
        composeRule.onNodeWithText("W1-BOSS", substring = true).assertIsDisplayed()

        composeRule.onAllNodesWithText("BOSS FIGHT").onFirst().performClick()

        // Pause autoAdvance while GamePlayScreen runs its 60 FPS frame loop
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(200)

        composeRule.onNodeWithText("W1-BOSS", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("TROLL DASH").assertIsDisplayed()
        composeRule.onNodeWithText("HOP / EARS").assertIsDisplayed()
        composeRule.onNodeWithText("BURROW").assertIsDisplayed()

        // Exercise touch controls (Right, Troll Dash, Hop)
        composeRule.onNodeWithText("RIGHT").performTouchInput { down(center); advanceEventTime(150); up() }
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText("TROLL DASH").performClick()
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithText("HOP / EARS").performClick()
        composeRule.mainClock.advanceTimeBy(100)

        composeRule.onNodeWithText("Pause").performClick()
        composeRule.mainClock.autoAdvance = true
        composeRule.onNodeWithText("World Hub", substring = true).performClick()
        composeRule.onNodeWithText("BOBS BUNNY: CARROT CITY", substring = true).assertIsDisplayed()
    }

    @Test
    fun w1One_dioramaTogglePreservesStageAndClassic2dFallback() {
        composeRule.onNodeWithText("STAGE ROSTER", substring = true).performClick()
        composeRule.onNodeWithTag("world_filter_tabs")
            .performScrollToNode(hasText("W1: Carrot City", substring = true))
        composeRule.onNodeWithText("W1: Carrot City", substring = true).performClick()
        composeRule.onNodeWithTag("stage_roster_list")
            .performScrollToNode(hasText("W1-1", substring = true))
        composeRule.onNodeWithText("W1-1", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("PLAY").onFirst().performClick()

        // Frame progression remains controlled while the native GL surface
        // and Compose button states are switched repeatedly.
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(250)
        composeRule.onNodeWithText("3D").assertIsDisplayed()
        composeRule.onNodeWithText("3D").performClick()
        composeRule.mainClock.advanceTimeBy(80)
        composeRule.onNodeWithText("2D").assertIsDisplayed()
        composeRule.onNodeWithText("2D").performClick()
        composeRule.mainClock.advanceTimeBy(80)
        composeRule.onNodeWithText("3D").assertIsDisplayed()
        composeRule.onNodeWithText("W1-1", substring = true).assertIsDisplayed()
        composeRule.mainClock.autoAdvance = true
    }

    @Test
    fun w1One_visualCaptureWaitsForFirst3dDraw() {
        // Only a completed GLES3 render increments this signal. Unlike
        // Compose semantics polling, it never waits for a 60-FPS UI to idle.
        val completedBefore = CarrotCity3DRenderer.completedFirstFrames.get()
        composeRule.onNodeWithText("STAGE ROSTER", substring = true).performClick()
        composeRule.onNodeWithTag("world_filter_tabs")
            .performScrollToNode(hasText("W1: Carrot City", substring = true))
        composeRule.onNodeWithText("W1: Carrot City", substring = true).performClick()
        composeRule.onNodeWithTag("stage_roster_list")
            .performScrollToNode(hasText("W1-1", substring = true))
        // Target the intended stage, never the first generic PLAY button. A
        // different stage launch would produce a false-negative GLES receipt.
        composeRule.onNodeWithTag("stage_play_W1-1").performClick()
        // Commit the destination change through one deterministic Compose frame,
        // without Espresso waiting for the intentionally continuous 60-FPS loop.
        // OpenGL has its own render thread and still must independently produce
        // the first-frame receipt; advancing Compose does not fake GLES output.
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(250)
        // Do NOT request Compose idleness after launching the continuous
        // 60-FPS game loop: Espresso can time out on normal recompositions.
        // The focused run may start at zero, but the subsequent full test
        // suite can legitimately have completed earlier GLSurfaceView draws.
        // Only an increment beyond our pre-navigation baseline proves THIS
        // launch rendered a fresh scene; do not assert the global count is zero.
        // No Compose semantics calls below this point; use the GL callback.
        // The other W1-1 mode-toggle test already checks the 3D UI label.

        val deadline = android.os.SystemClock.elapsedRealtime() + 45_000L
        while (CarrotCity3DRenderer.completedFirstFrames.get() <= completedBefore &&
            android.os.SystemClock.elapsedRealtime() < deadline) {
            Thread.sleep(100)
        }
        val completedAfter = CarrotCity3DRenderer.completedFirstFrames.get()
        val rendered3D = completedAfter > completedBefore
        // A GLES3 draw can finish while Compose's loading overlay is waiting
        // for its next frame. Drive the Compose clock only after the GL receipt
        // so its callback dismisses the loader before capturing the screenshot.
        // This never increments or manufactures a native render receipt.
        if (rendered3D) composeRule.mainClock.advanceTimeBy(250)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
        val reportedGles = manager?.deviceConfigurationInfo?.reqGlEsVersion ?: 0
        val activeActivity = composeRule.activity.javaClass.simpleName

        // Preserve screenshot AND capability evidence even if the 3D gate
        // fails, so a hosted emulator limitation is not silently misdiagnosed.
        Thread.sleep(800)
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val saved = bitmap?.let { image ->
            context.openFileOutput("bobs_w1_1_visual_qa.png", android.content.Context.MODE_PRIVATE).use {
                image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
            }
        } ?: false
        val colors = mutableSetOf<Int>()
        val sceneColors = mutableSetOf<Int>()
        if (bitmap != null) {
            for (gy in 1..10) {
                for (gx in 1..16) {
                    val color = bitmap.getPixel(gx * (bitmap.width - 1) / 17, gy * (bitmap.height - 1) / 11)
                    colors.add(color and 0x00F0F0F0)
                    // Isolate the central scene so colorful HUD elements do not
                    // falsely certify a loading screen or blank 3D viewport.
                    val sx = ((0.20f + 0.60f * gx / 17f) * (bitmap.width - 1)).toInt()
                    val sy = ((0.35f + 0.37f * gy / 11f) * (bitmap.height - 1)).toInt()
                    sceneColors.add(bitmap.getPixel(sx, sy) and 0x00F0F0F0)
                }
            }
            bitmap.recycle()
        }
        val diagnostics = "sdk=" + android.os.Build.VERSION.SDK_INT +
            "\nreportedGles=0x" + reportedGles.toString(16) +
            "\nfirstFrameBefore=" + completedBefore +
            "\nfirstFrameAfter=" + completedAfter +
            "\n3dCompleted=" + rendered3D +
            "\nscreenshotSaved=" + saved +
            "\nsampledColorClasses=" + colors.size + "\n"
            "\nsceneColorClasses=" + sceneColors.size + "\n"
        context.openFileOutput("bobs_w1_1_render_qa.txt", android.content.Context.MODE_PRIVATE)
            .bufferedWriter().use { it.write(diagnostics) }
        android.util.Log.i("BobsBunnyDiorama", "W1-1 hosted QA: " +
            diagnostics.replace("\n", " | "))

        // Gradle uninstalls instrumented applications on the hosted runner,
        // so private app files cannot be fetched afterward via adb run-as.
        // Shell screencap writes an actual device screenshot into shared
        // Downloads which remains available for GitHub artifact upload.
        val screenshotFd = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand("screencap -p /sdcard/Download/bobs_w1_1_visual_qa.png")
        screenshotFd?.use { fd ->
            android.os.ParcelFileDescriptor.AutoCloseInputStream(fd).use { stream ->
                stream.readBytes() // wait for the shell command to complete
            }
        }

        assertTrue("GLES3 did not complete first W1-1 frame: " + diagnostics, rendered3D)
        assertTrue("W1-1 3D screenshot unavailable: " + diagnostics, saved)
        assertTrue("W1-1 screenshot appears nearly uniform: " + diagnostics, colors.size >= 8)
        assertTrue("W1-1 playfield still blank or obscured by its loader: " + diagnostics,
            sceneColors.size >= 6)
    }

    @Test
    fun savePersistence_repositoryPersistsProgressAcrossReloads() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repo1 = GameProgressRepository(context)
        val initialCarrots = repo1.state.value.totalCarrots

        repo1.recordStageClear(
            stageId = 1,
            carrotsEarned = 25,
            shardsEarned = 1,
            starsEarned = 3,
            clearTimeSeconds = 28.5f
        )
        repo1.selectHero(PlayableHero.BOBS_BUNNY)

        val repo2 = GameProgressRepository(context)
        assertEquals(initialCarrots + 25, repo2.state.value.totalCarrots)
        assertEquals(3, repo2.state.value.stageStars[1])
        assertEquals(PlayableHero.BOBS_BUNNY, repo2.state.value.selectedHero)
    }

    @Test
    fun audioLifecycle_soundEnginePlaysAndReleasesCleanly() {
        val engine = CartoonSoundEngine()
        engine.playJump()
        engine.playCarrotPickup(isGolden = true)
        engine.playBoing()
        engine.playBonk()
        engine.playVictoryFanfare()
        engine.release()
        assertTrue(engine.soundEnabled)
    }

    @Test
    fun backgroundResume_activitySurvivesCreatedAndResumedTransitions() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.onNodeWithText("BOBS BUNNY: CARROT CITY", substring = true).assertIsDisplayed()
    }
}
