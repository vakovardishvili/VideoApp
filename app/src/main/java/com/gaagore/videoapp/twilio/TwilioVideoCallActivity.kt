package com.gaagore.videoapp.twilio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.gaagore.videoapp.R
import com.gaagore.videoapp.databinding.ActivityVideoCallBinding
import com.gaagore.videoapp.twilio.Consts.TWILIO_ACCESS_TOKEN
import com.gaagore.videoapp.utils.toDp
import com.twilio.video.*
import com.twilio.video.LocalAudioTrack
import tvi.webrtc.Camera2Enumerator
import com.twilio.video.RemoteParticipant

import com.twilio.video.RemoteVideoTrack
import com.twilio.video.RemoteVideoTrackPublication


class TwilioVideoCallActivity : AppCompatActivity() {
    private val TAG: String = "--------"
    private val RECORD_AUDIO_REQUEST_CODE = 0

    private lateinit var binding: ActivityVideoCallBinding

    private var localVideoTrack: LocalVideoTrack? = null
    private var localAudioTrack: LocalAudioTrack? = null

    private lateinit var room: Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkAndRequestPermissions()
        connectToRoom(intent.getStringExtra(EXTRA_ROOM_NAME)!!)
    }

    private fun setUpLocalmedia() {
        val enable = true
        localAudioTrack = LocalAudioTrack.create(this, enable)

        val camera2Enumerator = Camera2Enumerator(this);
        var frontCameraId: String? = null

        camera2Enumerator.deviceNames.forEach { cameraId ->
            if (camera2Enumerator.isFrontFacing(cameraId)) {
                frontCameraId = cameraId
            }
        }

        if (frontCameraId != null) {
            // Create the CameraCapturer with the front camera
            val cameraCapturer = Camera2Capturer(this, frontCameraId!!)

            // Create a video track
            localVideoTrack = LocalVideoTrack.create(this, enable, cameraCapturer)
        }
    }

    private fun connectToRoom(roomName: String) {
        val connectOptions = ConnectOptions.Builder(TWILIO_ACCESS_TOKEN)
            .roomName(roomName)
            .videoTracks(mutableListOf(localVideoTrack))
            .audioTracks(mutableListOf(localAudioTrack))
            .build()
        room = Video.connect(this, connectOptions, roomListener())
    }

    private fun roomListener(): Room.Listener = object : Room.Listener {
        override fun onConnected(room: Room) {
            // Render a local video track to preview your camera
            localVideoTrack?.addSink(binding.videoView)
            println(TAG + room.localParticipant?.identity + " local identity")
            println(TAG + room.remoteParticipants.size + " size")
            room.remoteParticipants.forEach { participant ->
                participant.setListener(remoteParticipantListener())
            }
        }

        override fun onConnectFailure(room: Room, twilioException: TwilioException) {
            Toast.makeText(this@TwilioVideoCallActivity, twilioException.explanation, Toast.LENGTH_LONG)
                .show()
            println(TAG + twilioException.message + " " + twilioException.explanation)
        }

        override fun onReconnecting(room: Room, twilioException: TwilioException) {
            println(TAG + 2)
        }

        override fun onReconnected(room: Room) {
            println(TAG + 3)
        }

        override fun onDisconnected(room: Room, twilioException: TwilioException?) {
            // Release the audio track to free native memory resources
            // Release the video track to free native memory resources
            localAudioTrack?.release()
            localVideoTrack?.release()
            println(TAG + twilioException?.explanation + " disconnected")
            Toast.makeText(this@TwilioVideoCallActivity, "disconnected", Toast.LENGTH_LONG).show()
        }

        override fun onParticipantConnected(room: Room, remoteParticipant: RemoteParticipant) {
            remoteParticipant.setListener(remoteParticipantListener())
            Toast.makeText(
                this@TwilioVideoCallActivity,
                remoteParticipant.identity + " connected",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onParticipantDisconnected(
            room: Room,
            remoteParticipant: RemoteParticipant
        ) {
            binding.videoContainer.forEach { view ->
                if (view.tag == remoteParticipant.identity) {
                    binding.videoContainer.removeView(view)
                }
            }
            Toast.makeText(
                this@TwilioVideoCallActivity,
                remoteParticipant.identity + " disconnected",
                Toast.LENGTH_LONG
            ).show()
        }

        override fun onRecordingStarted(room: Room) {

        }

        override fun onRecordingStopped(room: Room) {
            println(TAG + 5)
        }
    }

    private fun remoteParticipantListener(): RemoteParticipant.Listener {
        return object : RemoteParticipant.Listener {
            override fun onVideoTrackSubscribed(
                participant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack
            ) {
                addParticipantVideo(participant, remoteVideoTrack)
            }

            override fun onAudioTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
            }

            override fun onAudioTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
            }

            override fun onAudioTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
            ) {
            }

            override fun onAudioTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                twilioException: TwilioException
            ) {
            }

            override fun onAudioTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication,
                remoteAudioTrack: RemoteAudioTrack
            ) {
            }

            override fun onVideoTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
            }

            override fun onVideoTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
            }

            override fun onVideoTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                twilioException: TwilioException
            ) {
            }

            override fun onVideoTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication,
                remoteVideoTrack: RemoteVideoTrack
            ) {
            }

            override fun onDataTrackPublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
            ) {
            }

            override fun onDataTrackUnpublished(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication
            ) {
            }

            override fun onDataTrackSubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {
            }

            override fun onDataTrackSubscriptionFailed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                twilioException: TwilioException
            ) {
            }

            override fun onDataTrackUnsubscribed(
                remoteParticipant: RemoteParticipant,
                remoteDataTrackPublication: RemoteDataTrackPublication,
                remoteDataTrack: RemoteDataTrack
            ) {
            }

            override fun onAudioTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
            }

            override fun onAudioTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteAudioTrackPublication: RemoteAudioTrackPublication
            ) {
            }

            override fun onVideoTrackEnabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
            }

            override fun onVideoTrackDisabled(
                remoteParticipant: RemoteParticipant,
                remoteVideoTrackPublication: RemoteVideoTrackPublication
            ) {
            }
        }
    }

    private fun addParticipantVideo(
        participant: RemoteParticipant,
        remoteVideoTrack: RemoteVideoTrack?
    ) {
        lateinit var videoView: View
        remoteVideoTrack?.let {
            videoView = VideoView(this@TwilioVideoCallActivity).apply {
                layoutParams = ViewGroup.LayoutParams(150.toDp, 150.toDp)
                mirror = false
                tag = participant.identity
            }
            remoteVideoTrack.addSink(videoView as VideoView)
        } ?: kotlin.run {
            videoView = ImageView(this@TwilioVideoCallActivity).apply {
                setImageResource(R.drawable.common_google_signin_btn_icon_dark_normal)
                layoutParams = ViewGroup.LayoutParams(150.toDp, 150.toDp)
                tag = participant.identity
            }
        }
        binding.videoContainer.visibility = View.VISIBLE
        binding.videoContainer.addView(videoView)
    }

    companion object {
        const val EXTRA_ROOM_NAME = "EXTRA_ROOM_NAME"
        fun start(context: Context, roomName: String) {
            val intent = Intent(context, TwilioVideoCallActivity::class.java)
            intent.putExtra(EXTRA_ROOM_NAME, roomName)

            context.startActivity(intent)
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
                RECORD_AUDIO_REQUEST_CODE
            )
        } else setUpLocalmedia()
    }

    override fun onDestroy() {
        room.disconnect()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                setUpLocalmedia()
            }
        }
    }

}