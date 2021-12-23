package com.gaagore.videoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gaagore.videoapp.amazon.AmazonVideoCallActivity
import com.gaagore.videoapp.databinding.ActivityMainBinding
import com.gaagore.videoapp.twilio.TwilioVideoCallActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //TwilioVideoCallActivity.start(this, "testroom")
        binding.joinRoomButton.setOnClickListener {
           // TwilioVideoCallActivity.start(this, binding.roomName.text.toString())
            AmazonVideoCallActivity.start(this, "testroom", "arshad.dentist")

        }
    }
}