package com.denyskostetskyi.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.denyskostetskyi.services.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var serviceBinder: AudioVibrationService.ServiceBinder? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceBinder = service as AudioVibrationService.ServiceBinder
            updateAudioVibrationControlsVisibility(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBinder = null
            updateAudioVibrationControlsVisibility(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
    }

    private fun updateAudioVibrationControlsVisibility(isVisible: Boolean) {
        with(binding) {
            buttonPlaySound.isVisible = isVisible
            buttonVibrate.isVisible = isVisible
        }
    }

    private fun initViews() {
        with(binding) {
            switchForegroundService.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startForegroundService()
                } else {
                    stopForegroundService()
                }
            }
            switchBackgroundService.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startBackgroundService()
                } else {
                    stopBackgroundService()
                }
            }
            switchBoundService.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    bindService()
                } else {
                    unbindService()
                }
            }
            buttonPlaySound.setOnClickListener {
                playSound()
            }
            buttonVibrate.setOnClickListener {
                vibrate()
            }
        }
    }

    private fun startForegroundService() {
        val intent = OverlayViewService.newIntent(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(this, OverlayViewService::class.java)
        stopService(intent)
    }

    private fun startBackgroundService() {
        val intent = SystemInfoService.newIntent(this, 5000)
        startService(intent)
    }

    private fun stopBackgroundService() {
        val intent = Intent(this, SystemInfoService::class.java)
        stopService(intent)
    }

    private fun bindService() {
        val intent = AudioVibrationService.newIntent(this)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        unbindService(serviceConnection)
        serviceBinder = null
        updateAudioVibrationControlsVisibility(false)
    }

    private fun playSound() {
        serviceBinder?.playSound()
    }

    private fun vibrate() {
        serviceBinder?.vibrate()
    }
}
