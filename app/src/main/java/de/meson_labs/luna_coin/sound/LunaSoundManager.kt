package de.meson_labs.luna_coin.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import de.meson_labs.luna_coin.R

object LunaSoundManager {

    private var initialized = false

    private lateinit var soundPool: SoundPool

    private var gameSuccessSound = 0
    private var wheelTickSound = 0

    private var gameSuccessLoaded = false
    private var wheelTickLoaded = false

    fun init(
        context: Context
    ) {
        if (initialized) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                when (sampleId) {
                    gameSuccessSound -> gameSuccessLoaded = true
                    wheelTickSound -> wheelTickLoaded = true
                }
            }
        }

        val appContext = context.applicationContext

        gameSuccessSound = soundPool.load(
            appContext,
            R.raw.game_success,
            1
        )

        wheelTickSound = soundPool.load(
            appContext,
            R.raw.wheel_tick,
            1
        )

        initialized = true
    }

    fun playGameSuccess() {
        if (!initialized || !gameSuccessLoaded) return

        play(
            soundId = gameSuccessSound,
            volume = 0.85f
        )
    }

    fun playWheelTick(
        volume: Float = 0.65f
    ) {
        if (!initialized || !wheelTickLoaded) return

        play(
            soundId = wheelTickSound,
            volume = volume
        )
    }

    private fun play(
        soundId: Int,
        volume: Float = 1f
    ) {
        soundPool.play(
            soundId,
            volume,
            volume,
            1,
            0,
            1f
        )
    }

    fun release() {
        if (!initialized) return

        soundPool.release()

        initialized = false

        gameSuccessSound = 0
        wheelTickSound = 0

        gameSuccessLoaded = false
        wheelTickLoaded = false
    }
}