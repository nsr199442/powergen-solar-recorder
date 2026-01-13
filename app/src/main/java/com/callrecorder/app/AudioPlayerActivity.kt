package com.callrecorder.app

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.callrecorder.app.databinding.ActivityAudioPlayerBinding
import java.io.File
import java.util.concurrent.TimeUnit

class AudioPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding
    private var mediaPlayer: MediaPlayer? = null
    private var filePath: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        filePath = intent.getStringExtra("file_path")
        val fileName = intent.getStringExtra("file_name")

        binding.tvFileName.text = fileName ?: "Recording"

        setupPlayer()
        setupButtons()
    }

    private fun setupPlayer() {
        filePath?.let { path ->
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(path)
                    prepare()
                    
                    binding.seekBar.max = duration
                    binding.tvDuration.text = formatTime(duration.toLong())
                }
                
                updateSeekBar()
                
            } catch (e: Exception) {
                binding.tvFileName.text = "Error loading file"
            }
        }
    }

    private fun setupButtons() {
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }

        binding.btnStop.setOnClickListener {
            stopAudio()
        }

        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    binding.tvCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playAudio() {
        mediaPlayer?.start()
        isPlaying = true
        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause)
        updateSeekBar()
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
    }

    private fun stopAudio() {
        mediaPlayer?.stop()
        mediaPlayer?.prepare()
        isPlaying = false
        binding.btnPlayPause.setImageResource(android.R.drawable.ic_media_play)
        binding.seekBar.progress = 0
        binding.tvCurrentTime.text = "00:00"
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        binding.seekBar.progress = it.currentPosition
                        binding.tvCurrentTime.text = formatTime(it.currentPosition.toLong())
                        handler.postDelayed(this, 100)
                    }
                }
            }
        }, 100)
    }

    private fun formatTime(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}
