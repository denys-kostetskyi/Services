package com.denyskostetskyi.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast


class AudioVibrationService : Service() {
    private val audioManager by lazy { getSystemService(AudioManager::class.java) }
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private val binder = ServiceBinder()

    inner class ServiceBinder : Binder() {
        fun playSound(soundEffect: Int = AudioManager.FX_KEYPRESS_STANDARD) {
            this@AudioVibrationService.playSound(soundEffect)
        }

        fun vibrate(duration: Long = DEFAULT_VIBRATION_DURATION) {
            this@AudioVibrationService.vibrate(duration)
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun playSound(soundEffect: Int) {
        audioManager.playSoundEffect(soundEffect)
    }

    private fun vibrate(duration: Long) {
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect =
                    VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } else {
            Toast.makeText(
                this,
                getString(R.string.vibrator_is_not_available),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        private const val DEFAULT_VIBRATION_DURATION = 300L

        fun newIntent(context: Context) = Intent(context, AudioVibrationService::class.java)
    }
}
