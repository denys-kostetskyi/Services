package com.denyskostetskyi.services

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denyskostetskyi.services.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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
        }
    }

    private fun startForegroundService() {
        val intent = OverlayViewService.newIntent(this)
        startService(intent)
    }

    private fun stopForegroundService() {
        val intent = OverlayViewService.newIntent(this)
        stopService(intent)
    }

    private fun startBackgroundService() {

    }

    private fun stopBackgroundService() {

    }

    private fun bindService() {

    }

    private fun unbindService() {

    }
}
