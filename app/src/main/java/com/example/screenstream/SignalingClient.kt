package com.example.screenstream
import okhttp3.OkHttpClient; import okhttp3.Request; import okhttp3.WebSocket; import okhttp3.WebSocketListener; import org.json.JSONObject
class SignalingClient(val serverUrl: String, val room: String, val listener: Listener) {
    interface Listener { fun onJoined() {}; fun onPeerJoined() {}; fun onAnswer(sdp: JSONObject) {}; fun onIceCandidate(c: JSONObject) {}; fun onError(m: String) {} }
    private val client = OkHttpClient(); private var webSocket: WebSocket? = null
    fun connect() { webSocket = client.newWebSocket(Request.Builder().url(serverUrl).build(), object : WebSocketListener() { override fun onOpen(ws: WebSocket, response: okhttp3.Response) { send(JSONObject().apply { put("type", "join"); put("room", room); put("role", "phone") }) }; override fun onMessage(ws: WebSocket, text: String) { val msg = JSONObject(text); when(msg.optString("type")) { "joined" -> listener.onJoined(); "peer-joined" -> listener.onPeerJoined() } }; override fun onFailure(ws: WebSocket, t: Throwable, r: okhttp3.Response?) { listener.onError(t.message ?: "") } }) }
    fun sendOffer(sdp: JSONObject) { send(JSONObject().apply { put("type", "offer"); put("sdp", sdp) }) }
    private fun send(json: JSONObject) { webSocket?.send(json.toString()) }
    fun close() { webSocket?.close(1000, "done") }
}
