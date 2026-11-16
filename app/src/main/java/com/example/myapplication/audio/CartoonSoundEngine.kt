package com.example.myapplication.audio

import android.media.AudioManager
import android.media.ToneGenerator

class CartoonSoundEngine {
    private var toneGenerator: ToneGenerator? = null
    var soundEnabled: Boolean = true

    private fun ensureGenerator(): ToneGenerator? {
        if (!soundEnabled) return null
        if (toneGenerator == null) {
            runCatching {
                toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 65)
            }
        }
        return toneGenerator
    }

    fun playJump() {
        runCatching {
            ensureGenerator()?.startTone(ToneGenerator.TONE_PROP_BEEP, 55)
        }
    }

    fun playCarrotPickup(isGolden: Boolean = false) {
        runCatching {
            val tone = if (isGolden) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP2
            ensureGenerator()?.startTone(tone, if (isGolden) 140 else 45)
        }
    }

    fun playBoing() {
        runCatching {
            ensureGenerator()?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 95)
        }
    }

    fun playBonk() {
        runCatching {
            ensureGenerator()?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
        }
    }

    fun playVictoryFanfare() {
        runCatching {
            ensureGenerator()?.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 240)
        }
    }

    fun release() {
        runCatching {
            toneGenerator?.release()
            toneGenerator = null
        }
    }
}
