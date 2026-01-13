package com.callrecorder.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecordingsAdapter(
    private val recordings: List<RecordingFile>,
    private val onPlayClick: (RecordingFile) -> Unit
) : RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val tvFileDetails: TextView = view.findViewById(R.id.tvFileDetails)
        val tvFileSize: TextView = view.findViewById(R.id.tvFileSize)
        val btnPlay: ImageButton = view.findViewById(R.id.btnPlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recording, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recording = recordings[position]
        val file = recording.file
        
        // Parse filename: +94761234567_cellular_incoming_2025-01-13_14-30-45.mp3
        val parts = file.nameWithoutExtension.split("_")
        
        val phoneNumber = parts.getOrNull(0) ?: "Unknown"
        val callType = parts.getOrNull(1) ?: "Unknown"
        val direction = parts.getOrNull(2) ?: "Unknown"
        val date = parts.getOrNull(3) ?: ""
        val time = parts.getOrNull(4) ?: ""
        
        // Format display
        holder.tvFileName.text = phoneNumber
        holder.tvFileDetails.text = buildString {
            append(callType.capitalize())
            append(" • ")
            append(direction.capitalize())
            if (date.isNotEmpty()) {
                append("\n")
                append(formatDate(date, time))
            }
        }
        
        // File size
        val sizeKB = file.length() / 1024
        val sizeMB = sizeKB / 1024.0
        holder.tvFileSize.text = if (sizeMB >= 1.0) {
            String.format("%.1f MB", sizeMB)
        } else {
            "$sizeKB KB"
        }
        
        // Play button
        holder.btnPlay.setOnClickListener {
            onPlayClick(recording)
        }
    }

    override fun getItemCount() = recordings.size

    private fun formatDate(date: String, time: String): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val parsedDate = formatter.parse("${date}_${time}")
            val displayFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            parsedDate?.let { displayFormatter.format(it) } ?: "$date $time"
        } catch (e: Exception) {
            "$date $time"
        }
    }

    private fun String.capitalize(): String {
        return this.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }
}
