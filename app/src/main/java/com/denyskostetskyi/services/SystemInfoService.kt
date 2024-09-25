package com.denyskostetskyi.services

import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.StatFs
import android.util.Log
import androidx.annotation.IntRange

class SystemInfoService : Service() {
    private val batteryManager by lazy { getSystemService(BatteryManager::class.java) }
    private val activityManager by lazy { getSystemService(ActivityManager::class.java) }

    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        handlerThread = HandlerThread(THREAD_NAME).apply {
            start()
            handler = Handler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sleepDuration = intent?.getLongExtra(KEY_SLEEP_DURATION, DEFAULT_SLEEP_DURATION)
            ?: DEFAULT_SLEEP_DURATION
        handler?.post(object : Runnable {
            override fun run() {
                val systemInfo = getSystemInfo()
                Log.d(TAG, systemInfo.toString())
                handler?.postDelayed(this, sleepDuration)
            }
        })
        return START_NOT_STICKY
    }

    private fun getSystemInfo(): SystemInfo {
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val memoryInfo = MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val availableMemory = MemoryUtils.bytesToGigabytes(memoryInfo.availMem)
        val totalMemory = MemoryUtils.bytesToGigabytes(memoryInfo.totalMem)
        val storageStat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = storageStat.availableBlocksLong * storageStat.blockSizeLong
        val availableStorage = MemoryUtils.bytesToGigabytes(availableBytes)
        val totalBytes = storageStat.blockCountLong * storageStat.blockSizeLong
        val totalStorage = MemoryUtils.bytesToGigabytes(totalBytes)
        return SystemInfo(
            batteryLevel = capacity,
            availableMemory = availableMemory,
            totalMemory = totalMemory,
            availableStorage = availableStorage,
            totalStorage = totalStorage,
        )
    }

    override fun onDestroy() {
        handler?.removeCallbacksAndMessages(null)
        handlerThread?.quitSafely()
        handlerThread = null
        handler = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "SystemInfoService"
        private const val THREAD_NAME = "SystemInfoServiceThread"
        private const val DEFAULT_SLEEP_DURATION = 10_000L
        private const val KEY_SLEEP_DURATION = "sleep_duration"

        fun newIntent(
            context: Context,
            @IntRange(from = 1L) sleepDuration: Long = DEFAULT_SLEEP_DURATION
        ) = Intent(context, SystemInfoService::class.java).apply {
            putExtra(KEY_SLEEP_DURATION, sleepDuration)
        }
    }
}
