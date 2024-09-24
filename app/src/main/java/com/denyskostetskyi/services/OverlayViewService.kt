package com.denyskostetskyi.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat

class OverlayViewService : Service() {
    private val windowManager by lazy { getSystemService(WindowManager::class.java) }
    private lateinit var floatingView: View

    override fun onCreate() {
        super.onCreate()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!::floatingView.isInitialized) {
            initFloatingView()
        }
        intent?.action?.let {
            when (it) {
                ACTION_SHOW_VIEW -> floatingView.visibility = View.VISIBLE
                ACTION_HIDE_VIEW -> floatingView.visibility = View.GONE
            }
        }
        return START_STICKY
    }

    private fun initFloatingView() {
        val inflater = getSystemService(LayoutInflater::class.java)
        floatingView = inflater.inflate(R.layout.floating_view, null)
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(floatingView, params)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) windowManager.removeView(floatingView)
    }

    private fun startForeground() {
        createNotificationChannel()
        val notification = createNotification()
        val foregroundServiceType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        ServiceCompat.startForeground(
            this,
            SERVICE_ID,
            notification,
            foregroundServiceType
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.foreground_service_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val actionShowView = NotificationCompat.Action.Builder(
            null,
            getString(R.string.show_view),
            createPendingIntent(ACTION_SHOW_VIEW, RC_SHOW_VIEW)
        ).build()
        val actionHideView = NotificationCompat.Action.Builder(
            null,
            getString(R.string.hide_view),
            createPendingIntent(ACTION_HIDE_VIEW, RC_HIDE_VIEW)
        ).build()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.foreground_service))
            .setContentText(getString(R.string.tap_to_show_hide_the_view))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(actionShowView)
            .addAction(actionHideView)
            .build()
    }

    private fun createPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, OverlayViewService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val SERVICE_ID = 1
        private const val ACTION_SHOW_VIEW = "SHOW_VIEW"
        private const val ACTION_HIDE_VIEW = "HIDE_VIEW"
        private const val RC_SHOW_VIEW = 1
        private const val RC_HIDE_VIEW = 2

        fun newIntent(context: Context) = Intent(context, OverlayViewService::class.java)
    }
}
