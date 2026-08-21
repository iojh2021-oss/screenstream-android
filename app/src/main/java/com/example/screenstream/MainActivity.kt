package com.example.screenstream

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {
    companion object {
        private const val DEFAULT_SERVER = "wss://YOUR-SIGNALING-SERVER"
        private const val PREFS = "screenstream"
        private const val KEY_SERVER = "server"
        private const val KEY_ROOM = "room"
    }

    private lateinit var serverUrlInput: EditText
    private lateinit var roomCodeInput: EditText
    private lateinit var statusText: TextView

    private val screenCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val server = serverUrlInput.text.toString().trim()
            val room = roomCodeInput.text.toString().trim()
            getPreferences(MODE_PRIVATE).edit().putString(KEY_SERVER, server).putString(KEY_ROOM, room).apply()
            val intent = Intent(this, ScreenCaptureService::class.java).apply {
                putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, result.resultCode)
                putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, result.data)
                putExtra(ScreenCaptureService.EXTRA_SERVER_URL, server)
                putExtra(ScreenCaptureService.EXTRA_ROOM_CODE, room)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
            statusText.text = "Connecting to room $room…"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        applyConfigurationIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyConfigurationIntent(intent)
    }

    private fun buildUi() {
        val prefs = getPreferences(MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 40)
        }
        val title = TextView(this).apply {
            text = "ScreenStream Sender"
            textSize = 28f
        }
        val hint = TextView(this).apply {
            text = "Use a setup link from ScreenStream Viewer, or enter the server and room manually."
            textSize = 14f
        }
        serverUrlInput = EditText(this).apply {
            hint = "Signaling server (wss://…)"
            setSingleLine(true)
            setText(prefs.getString(KEY_SERVER, ""))
        }
        roomCodeInput = EditText(this).apply {
            hint = "Room code"
            setSingleLine(true)
            setText(prefs.getString(KEY_ROOM, ""))
        }
        val startButton = Button(this).apply {
            text = "Start screen sharing"
            setOnClickListener { startCapture() }
        }
        statusText = TextView(this).apply { text = "Ready"; textSize = 14f }
        root.addView(title)
        root.addView(hint)
        root.addView(serverUrlInput)
        root.addView(roomCodeInput)
        root.addView(startButton)
        root.addView(statusText)
        setContentView(root)
    }

    private fun applyConfigurationIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "screenstream" || uri.host != "connect") return
        uri.getQueryParameter("server")?.trim()?.takeIf { it.isNotBlank() }?.let { serverUrlInput.setText(it) }
        uri.getQueryParameter("room")?.trim()?.takeIf { it.isNotBlank() }?.let { roomCodeInput.setText(it) }
        statusText.text = "Setup received — ready to start"
    }

    private fun startCapture() {
        val server = serverUrlInput.text.toString().trim()
        val room = roomCodeInput.text.toString().trim()
        if (!server.startsWith("ws://") && !server.startsWith("wss://")) {
            statusText.text = "Server must start with ws:// or wss://"
            return
        }
        if (room.isBlank()) {
            roomCodeInput.setText(UUID.randomUUID().toString().replace("-", "").take(8).uppercase())
        }
        val pm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(pm.createScreenCaptureIntent())
    }
}
