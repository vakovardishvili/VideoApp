package com.gaagore.videoapp.amazon

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amazonaws.services.chime.sdk.meetings.audiovideo.*
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.DefaultVideoRenderView
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoTileObserver
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.VideoTileState
import com.amazonaws.services.chime.sdk.meetings.session.*
import com.gaagore.videoapp.databinding.ActivityAmazonVideoCallBinding
import android.widget.LinearLayout
import androidx.core.view.forEach
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.DefaultCameraCaptureSource
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.capture.DefaultSurfaceTextureCaptureSourceFactory
import com.amazonaws.services.chime.sdk.meetings.audiovideo.video.gl.DefaultEglCoreFactory
import com.amazonaws.services.chime.sdk.meetings.realtime.RealtimeObserver
import com.amazonaws.services.chime.sdk.meetings.utils.logger.ConsoleLogger
import com.amazonaws.services.chime.sdk.meetings.utils.logger.LogLevel

class AmazonVideoCallActivity : AppCompatActivity(), AudioVideoObserver, RealtimeObserver,
    VideoTileObserver {
    private lateinit var binding: ActivityAmazonVideoCallBinding
    private lateinit var meetingSession: MeetingSession
    private lateinit var audioVideo: AudioVideoFacade

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmazonVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getSession()
    }


    private fun getSession() {
        val meeting = getMeeting()
        val attendee = getAttendee()
        val configuration = MeetingSessionConfiguration(
            CreateMeetingResponse(meeting),
            CreateAttendeeResponse(attendee)
        )

        val eglCoreFactory = DefaultEglCoreFactory()
        val surfaceTextureCaptureSourceFactory =
            DefaultSurfaceTextureCaptureSourceFactory(ConsoleLogger(), eglCoreFactory)
        val cameraCaptureSource = DefaultCameraCaptureSource(
            applicationContext,
            ConsoleLogger(),
            surfaceTextureCaptureSourceFactory
        )

        meetingSession =
            DefaultMeetingSession(
                configuration,
                ConsoleLogger().apply { setLogLevel(LogLevel.VERBOSE) },
                applicationContext,
                eglCoreFactory
            )

        audioVideo = meetingSession.audioVideo
        audioVideo.addAudioVideoObserver(this@AmazonVideoCallActivity)
        audioVideo.addRealtimeObserver(this@AmazonVideoCallActivity)
        audioVideo.addVideoTileObserver(this@AmazonVideoCallActivity)
        cameraCaptureSource.start()
        audioVideo.start()
        //audioVideo.startLocalVideo(cameraCaptureSource)
        audioVideo.startLocalVideo()
        audioVideo.startRemoteVideo()

    }

    private fun getAttendee() = Attendee(
        "cd32f3fc-20e2-2c4c-7550-27c7596d3083",
        "MTL-5c8ff070-6bef-4fb5-a6f8-385bf9dbf6b1",
        "Y2QzMmYzZmMtMjBlMi0yYzRjLTc1NTAtMjdjNzU5NmQzMDgzOmU0NTQzN2Y4LWFmNWQtNDYyNy05Y2U4LTQ2YmIzNzE0MDBiMA"
    )


    private fun getMeeting() = Meeting(
        "MET-dff45299-3ad2-4452-8cde-837b41424602",
        MediaPlacement(
            "wss://haxrp.m3.ue1.app.chime.aws:443/calls/74f71aad-7d53-412e-a554-47bb30be0706",
            "6ac3274e8f08373ab00b56c0ab3df36f.k.m2.ue1.app.chime.aws:3478",
            "wss://signal.m2.ue1.app.chime.aws/control/e5044ee4-69bf-404b-809f-6b6bda100706",
            "",
            ""
        ),
        "us-east-1",
        "c068e22d-9a45-4e48-8c2e-c8da13480706"
    )

    private fun addVideo(tileState: VideoTileState) {
        val videoSurface = DefaultVideoRenderView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            tag = tileState.attendeeId
        }
        binding.root.addView(videoSurface)
        audioVideo.bindVideoView(videoSurface, tileState.tileId)
    }

    private fun removeVideo(tileState: VideoTileState) {
        binding.root.forEach {
            if (it.tag == tileState.attendeeId) {
                binding.root.removeView(it)
            }
        }
    }

    companion object {
        private const val TAG = "AmazonVideoCallActivity"

        fun start(context: Context) {
            val intent = Intent(context, AmazonVideoCallActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onVideoTileAdded(tileState: VideoTileState) {
        Log.d(TAG, tileState.attendeeId + " onVideoTileAdded")
        addVideo(tileState)
    }

    override fun onVideoTileRemoved(tileState: VideoTileState) {
        Log.d(TAG, tileState.attendeeId + " onVideoTileRemoved")
        removeVideo(tileState)
    }

    override fun onAudioSessionCancelledReconnect() {
        Log.d("---------------", " onAudioSessionCancelledReconnect")
    }

    override fun onAudioSessionDropped() {
        Log.d("---------------", " onAudioSessionDropped")
    }

    override fun onAudioSessionStarted(reconnecting: Boolean) {
        Log.d("---------------", " onAudioSessionStarted")
    }

    override fun onAudioSessionStartedConnecting(reconnecting: Boolean) {
        Log.d("---------------", " onAudioSessionStartedConnecting")
    }

    override fun onAudioSessionStopped(sessionStatus: MeetingSessionStatus) {
        Log.d("---------------", sessionStatus.statusCode.toString())
    }

    override fun onConnectionBecamePoor() {
        Log.d("---------------", "onConnectionBecamePoor")
    }

    override fun onConnectionRecovered() {
        Log.d("---------------", "onConnectionRecovered")
    }

    override fun onVideoSessionStarted(sessionStatus: MeetingSessionStatus) {
        Log.d("---------------", " onVideoSessionStarted")
    }

    override fun onVideoSessionStartedConnecting() {
        Log.d("---------------", "onVideoSessionStartedConnecting ")
    }

    override fun onVideoSessionStopped(sessionStatus: MeetingSessionStatus) {
        Log.d("---------------", " onVideoSessionStopped")
    }

    override fun onVideoTilePaused(tileState: VideoTileState) {
        Log.d("---------------", " onVideoTilePaused")
    }

    override fun onVideoTileResumed(tileState: VideoTileState) {
        Log.d("---------------", " onVideoTileResumed")
    }

    override fun onVideoTileSizeChanged(tileState: VideoTileState) {
        Log.d("---------------", " onVideoTileSizeChanged")
    }

    override fun onAttendeesDropped(attendeeInfo: Array<AttendeeInfo>) {
        Log.d("---------------", "onAttendeesDropped")
    }

    override fun onAttendeesJoined(attendeeInfo: Array<AttendeeInfo>) {
        Log.d("---------------", attendeeInfo[0].attendeeId + " onAttendeesJoined")
    }

    override fun onAttendeesLeft(attendeeInfo: Array<AttendeeInfo>) {
        Log.d("---------------", "onAttendeesLeft")
    }

    override fun onAttendeesMuted(attendeeInfo: Array<AttendeeInfo>) {
        Log.d("---------------", "onAttendeesMuted")
    }

    override fun onAttendeesUnmuted(attendeeInfo: Array<AttendeeInfo>) {
        Log.d("---------------", "onAttendeesUnmuted")
    }

    override fun onSignalStrengthChanged(signalUpdates: Array<SignalUpdate>) {
        //Log.d("---------------", "onSignalStrengthChanged")
    }

    override fun onVolumeChanged(volumeUpdates: Array<VolumeUpdate>) {
        //  Log.d("---------------", "onVolumeChanged")
    }

}
