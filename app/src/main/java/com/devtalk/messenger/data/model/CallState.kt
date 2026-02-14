package com.devtalk.messenger.data.model

data class CallSignal(
    val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val calleeId: String = "",
    val calleeName: String = "",
    val type: CallType = CallType.AUDIO,
    val status: CallStatus = CallStatus.RINGING,
    val offer: String = "",
    val answer: String = "",
    val iceCandidates: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "callerId" to callerId,
        "callerName" to callerName,
        "calleeId" to calleeId,
        "calleeName" to calleeName,
        "type" to type.name,
        "status" to status.name,
        "offer" to offer,
        "answer" to answer,
        "iceCandidates" to iceCandidates,
        "timestamp" to timestamp
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): CallSignal = CallSignal(
            id = map["id"] as? String ?: "",
            callerId = map["callerId"] as? String ?: "",
            callerName = map["callerName"] as? String ?: "",
            calleeId = map["calleeId"] as? String ?: "",
            calleeName = map["calleeName"] as? String ?: "",
            type = try {
                CallType.valueOf(map["type"] as? String ?: "AUDIO")
            } catch (_: Exception) {
                CallType.AUDIO
            },
            status = try {
                CallStatus.valueOf(map["status"] as? String ?: "RINGING")
            } catch (_: Exception) {
                CallStatus.RINGING
            },
            offer = map["offer"] as? String ?: "",
            answer = map["answer"] as? String ?: "",
            iceCandidates = (map["iceCandidates"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            timestamp = (map["timestamp"] as? Long) ?: System.currentTimeMillis()
        )
    }
}

enum class CallType { AUDIO, VIDEO }
enum class CallStatus { RINGING, ACCEPTED, REJECTED, ENDED, BUSY }
