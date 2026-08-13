package com.example.screenstream
import android.app.Notification; import android.app.NotificationChannel; import android.app.NotificationManager; import android.app.Service; import android.content.Intent; import android.os.Build; import android.os.IBinder; import androidx.core.app.NotificationCompat
class ScreenCaptureService : Service() {
    companion object { const val EXTRA_RESULT_CODE = "result_code"; const val EXTRA_RESULT_DATA = "result_data"; const val EXTRA_SERVER_URL = "server_url"; const val EXTRA_ROOM_CODE = "room_code" }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int { startForeground(1, buildNotification()); return START_NOT_STICKY }
    private fun buildNotification(): Notification { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel("screenstream", "ScreenStream", NotificationManager.IMPORTANCE_LOW)) }; return NotificationCompat.Builder(this, "screenstream").setContentTitle("ScreenStream").setSmallIcon(android.R.drawable.ic_menu_view).setOngoing(true).build() }
}
