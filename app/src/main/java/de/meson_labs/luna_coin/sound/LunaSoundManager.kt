package de.meson_labs.luna_coin.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import de.meson_labs.luna_coin.R
import kotlinx.coroutines.delay

object LunaSoundManager {

    private var initialized = false

    private lateinit var soundPool: SoundPool

    private var tabClickSound = 0
    private var gameSuccessSound = 0
    private var wheelTickSound = 0

    fun init(
        context: Context
    ) {
        if (initialized) {
            return
        }

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        tabClickSound = soundPool.load(
            context,
            R.raw.tab_click,
            1
        )

        gameSuccessSound = soundPool.load(
            context,
            R.raw.game_success,
            1
        )

        wheelTickSound = soundPool.load(
            context,
            R.raw.wheel_tick,
            1
        )

        initialized = true
    }

    fun playTabClick() {
        if (!initialized) return

        play(
            soundId = tabClickSound,
            volume = 0.35f
        )
    }

    fun playGameSuccess() {
        if (!initialized) return

        play(
            soundId = gameSuccessSound,
            volume = 0.8f
        )
    }

    fun playWheelTick(
        volume: Float = 0.65f
    ) {
        if (!initialized) return

        play(
            soundId = wheelTickSound,
            volume = volume
        )
    }

    suspend fun playSlowingWheelTicks(
        ticks: Int = 24,
        startDelayMs: Long = 35L,
        endDelayMs: Long = 260L
    ) {
        if (!initialized) return
        if (ticks <= 0) return

        repeat(ticks) { index ->

            val progress =
                index.toFloat() /
                        (ticks - 1).coerceAtLeast(1)

            val delayMs =
                (
                        startDelayMs +
                                (
                                        endDelayMs -
                                                startDelayMs
                                        ) * progress
                        ).toLong()

            playWheelTick(
                volume = 0.75f - progress * 0.35f
            )

            delay(delayMs)
        }
    }

    private fun play(
        soundId: Int,
        volume: Float = 1f
    ) {
        if (!initialized) return

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
    }
}