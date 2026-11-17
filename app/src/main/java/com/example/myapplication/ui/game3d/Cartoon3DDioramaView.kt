package com.example.myapplication.ui.game3d

import android.app.ActivityManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.opengl.GLSurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.game.CartoonStageEngine

/**
 * W1-1 3D vertical slice. Show an intentional loading state until the first
 * complete 3D draw, and allow unsupported/invalid GLES3 to fall back to 2D.
 * The existing game engine and progression never change.
 */
@Composable
fun Cartoon3DDioramaView(
    engine: CartoonStageEngine,
    frames: DioramaFrameBridge,
    modifier: Modifier = Modifier,
    on3DUnavailable: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentUnavailable by rememberUpdatedState(on3DUnavailable)
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val gles3Supported = remember(context) {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        (manager?.deviceConfigurationInfo?.reqGlEsVersion ?: 0) >= 0x30000
    }

    if (!gles3Supported) {
        LaunchedEffect(engine) {
            currentUnavailable("OpenGL ES 3.0 is not supported on this device")
        }
        Box(modifier.fillMaxSize().background(Color(0xFF17243A)), contentAlignment = Alignment.Center) {
            Text("Switching to classic 2D mode…", color = Color.White)
        }
        return
    }

    var frameReady by remember(engine) { mutableStateOf(false) }
    val renderer = remember(engine) {
        CarrotCity3DRenderer(
            frames = frames,
            onFirstFrame = {
                mainHandler.post { frameReady = true }
            },
            onInitializationFailure = { cause ->
                Log.e("BobsBunnyDiorama", "3D initialization failed: $cause")
                mainHandler.post { currentUnavailable(cause) }
            }
        )
    }
    val glView = remember(engine) {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(3)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }

    DisposableEffect(lifecycleOwner, glView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glView.onResume()
                Lifecycle.Event.ON_PAUSE -> glView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // The composable can mount after ON_RESUME has already been dispatched.
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            glView.onResume()
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glView.onPause()
        }
    }

    Box(modifier.fillMaxSize()) {
        AndroidView(factory = { glView }, modifier = Modifier.fillMaxSize())
        if (!frameReady) {
            Column(
                modifier = Modifier.fillMaxSize().background(Color(0xFF17243A)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFFFFC928))
                Text(
                    text = "Preparing Carrot City's 3D world…",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}
