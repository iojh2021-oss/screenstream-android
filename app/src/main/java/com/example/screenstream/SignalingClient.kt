package com.example.screenstream

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class SignalingClient(
    private val serverUrl: String,
    private val room: String,
    private val listener: Listener
) {
    interface Listener {
        fun onJoined() {}
        fun onPeerJoined() {}
        fun onOffer(sdp: JSONObject) {}
        fun onAnswer(sdp: JSONObject) {}
        fun onIceCandidate(candidate: JSONObject) {}
        fun onPeerLeft() {}
        fun onError(message: String) {}
    }

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect() {
        webSocket = client.newWebSocket(Request.Builder().url(serverUrl).build(), object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                send(JSONObject().apply { put("type", "join"); put("room", room); put("role", "phone") })
            }
            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val msg = JSONObject(text)
                    when (msg.optString("type")) {
                        "joined" -> listener.onJoined()
                        "peer-joined" -> listener.onPeerJoined()
                        "offer" -> msg.optJSONObject("sdp")?.let(listener::onOffer)
                        "answer" -> msg.optJSONObject("sdp")?.let(listener::onAnswer)
                        "ice-candidate" -> msg.optJSONObject("candidate")?.let(listener::onIceCandidate)
                        "peer-left" -> listener.onPeerLeft()
                        "error" -> listener.onError(msg.optString("message", "Signaling server error"))
                    }
                } catch (e: Exception) {
                    listener.onError("Invalid signaling message: " + (e.message ?: "unknown error"))
                }
            }
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                listener.onError(t.message ?: "WebSocket connection failed")
            }
        })
    }

    fun sendOffer(sdp: JSONObject) = send(JSONObject().apply { put("type", "offer"); put("sdp", sdp) })
    fun sendAnswer(sdp: JSONObject) = send(JSONObject().apply { put("type", "answer"); put("sdp", sdp) })
    fun sendIceCandidate(candidate: JSONObject) = send(JSONObject().apply { put("type", "ice-candidate"); put("candidate", candidate) })
    private fun send(json: JSONObject) { webSocket?.send(json.toString()) }
    fun close() { webSocket?.close(1000, "done"); webSocket = null }
}
