package com.devtalk.messenger.webrtc

import android.content.Context
import com.devtalk.messenger.data.model.CallSignal
import com.devtalk.messenger.data.model.CallStatus
import com.devtalk.messenger.data.model.CallType
import com.devtalk.messenger.data.repository.FirebaseRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages WebRTC peer connections for audio/video calls.
 * Uses Firebase Realtime Database for signaling.
 */
@Singleton
class WebRtcManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseRepository: FirebaseRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // WebRTC components
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var eglBase: EglBase? = null

    // State
    private val _callState = MutableStateFlow<CallSignal?>(null)
    val callState: StateFlow<CallSignal?> = _callState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isCameraOff = MutableStateFlow(false)
    val isCameraOff: StateFlow<Boolean> = _isCameraOff.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _callDuration = MutableStateFlow(0L)
    val callDuration: StateFlow<Long> = _callDuration.asStateFlow()

    private var currentCallId: String? = null
    private var isInitiator = false

    fun initialize() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(false)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        eglBase = EglBase.create()

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase!!.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase!!.eglBaseContext,
                    true,
                    true
                )
            )
            .createPeerConnectionFactory()
    }

    /**
     * Start an outgoing call
     */
    suspend fun startCall(
        callerId: String,
        callerName: String,
        calleeId: String,
        calleeName: String,
        type: CallType
    ): String {
        val callId = firebaseRepository.generateCallId()
        currentCallId = callId
        isInitiator = true

        val call = CallSignal(
            id = callId,
            callerId = callerId,
            callerName = callerName,
            calleeId = calleeId,
            calleeName = calleeName,
            type = type,
            status = CallStatus.RINGING
        )

        _callState.value = call
        firebaseRepository.createCall(call)

        // Start foreground service
        CallService.start(context, type.name, calleeName)

        // Setup WebRTC
        setupPeerConnection(callId, true)

        // Create and send offer
        createOffer(callId)

        // Observe call state changes
        observeCallState(callId)

        return callId
    }

    /**
     * Accept an incoming call
     */
    suspend fun acceptCall(call: CallSignal) {
        currentCallId = call.id
        isInitiator = false
        _callState.value = call.copy(status = CallStatus.ACCEPTED)

        CallService.start(context, call.type.name, call.callerName)

        setupPeerConnection(call.id, false)

        // Create and send answer
        createAnswer(call.id, call.offer)

        observeCallState(call.id)
    }

    /**
     * Reject an incoming call
     */
    suspend fun rejectCall(callId: String) {
        firebaseRepository.updateCallStatus(callId, CallStatus.REJECTED)
        cleanup()
    }

    /**
     * End the current call
     */
    suspend fun endCall() {
        currentCallId?.let { callId ->
            firebaseRepository.updateCallStatus(callId, CallStatus.ENDED)
        }
        cleanup()
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        localAudioTrack?.setEnabled(!_isMuted.value)
    }

    fun toggleCamera() {
        _isCameraOff.value = !_isCameraOff.value
        localVideoTrack?.setEnabled(!_isCameraOff.value)
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
        // In a full implementation, toggle audio output route
    }

    private fun setupPeerConnection(callId: String, isInitiator: Boolean) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate?) {
                    candidate?.let {
                        scope.launch {
                            firebaseRepository.addIceCandidate(
                                callId,
                                "${it.sdpMid}|${it.sdpMLineIndex}|${it.sdp}",
                                isInitiator
                            )
                        }
                    }
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    when (state) {
                        PeerConnection.IceConnectionState.CONNECTED -> {
                            _callState.value = _callState.value?.copy(status = CallStatus.ACCEPTED)
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED,
                        PeerConnection.IceConnectionState.FAILED -> {
                            scope.launch { endCall() }
                        }
                        else -> {}
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
                override fun onAddStream(stream: MediaStream?) {}
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onDataChannel(channel: DataChannel?) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            }
        )

        // Add audio track
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio0", audioSource)
        localAudioTrack?.let {
            peerConnection?.addTrack(it)
        }

        // Add video track for video calls
        if (_callState.value?.type == CallType.VIDEO) {
            setupVideoTrack()
        }
    }

    private fun setupVideoTrack() {
        videoCapturer = createCameraCapturer()
        videoCapturer?.let { capturer ->
            val videoSource = peerConnectionFactory?.createVideoSource(capturer.isScreencast)
            capturer.initialize(
                SurfaceTextureHelper.create("CaptureThread", eglBase!!.eglBaseContext),
                context,
                videoSource?.capturerObserver
            )
            capturer.startCapture(1280, 720, 30)

            localVideoTrack = peerConnectionFactory?.createVideoTrack("video0", videoSource)
            localVideoTrack?.let {
                peerConnection?.addTrack(it)
            }
        }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                return enumerator.createCapturer(deviceName, null)
            }
        }
        return null
    }

    private fun createOffer(callId: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo",
                if (_callState.value?.type == CallType.VIDEO) "true" else "false"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection?.setLocalDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            scope.launch {
                                // Update Firebase with the offer
                                firebaseRepository.createCall(
                                    _callState.value!!.copy(offer = it.description)
                                )
                            }
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, it)
                }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun createAnswer(callId: String, offer: String) {
        // Set remote description from offer
        val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, offer)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                // Create answer
                val constraints = MediaConstraints().apply {
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo",
                        if (_callState.value?.type == CallType.VIDEO) "true" else "false"))
                }
                peerConnection?.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(sdp: SessionDescription?) {
                        sdp?.let {
                            peerConnection?.setLocalDescription(object : SdpObserver {
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onSetSuccess() {
                                    scope.launch {
                                        firebaseRepository.updateCallAnswer(callId, it.description)
                                    }
                                }
                                override fun onCreateFailure(p0: String?) {}
                                override fun onSetFailure(p0: String?) {}
                            }, it)
                        }
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(error: String?) {}
                    override fun onSetFailure(error: String?) {}
                }, constraints)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, remoteDesc)
    }

    private fun observeCallState(callId: String) {
        scope.launch {
            firebaseRepository.observeCall(callId).collect { call ->
                call?.let {
                    _callState.value = it
                    if (it.status == CallStatus.ENDED || it.status == CallStatus.REJECTED) {
                        cleanup()
                    }
                }
            }
        }
    }

    private fun cleanup() {
        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        videoCapturer = null
        localAudioTrack?.dispose()
        localAudioTrack = null
        localVideoTrack?.dispose()
        localVideoTrack = null
        peerConnection?.close()
        peerConnection = null
        currentCallId = null
        _callState.value = null
        _isMuted.value = false
        _isCameraOff.value = false
        _isSpeakerOn.value = false
        _callDuration.value = 0L

        CallService.stop(context)
    }

    fun release() {
        cleanup()
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        eglBase?.release()
        eglBase = null
    }
}
