package com.evelolvetech.service.streaming

data class LiveKitConfig(
    val apiKey: String = System.getenv("LIVEKIT_API_KEY") ?: "devkey",
    val apiSecret: String = System.getenv("LIVEKIT_API_SECRET") ?: "aGVsbG93b3JsZGhlbGxvd29ybGRoZWxsb3dvcmxk",
    val serverUrl: String = System.getenv("LIVEKIT_URL") ?: "ws://localhost:7880",
    val httpUrl: String = System.getenv("LIVEKIT_HTTP_URL") ?: "http://localhost:7880",
    val tokenExpirySeconds: Long = 6 * 60 * 60,
    val roomPrefix: String = "momcare_"
)
