package com.example.serbisyo_it342_g3.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Schedule
import java.text.SimpleDateFormat
import java.util.Locale

class ScheduleAdapter(
    private var schedules: MutableList<Schedule>,
    private val onDeleteClick: (Schedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {
    private val tag = "ScheduleAdapter"

    fun updateSchedules(newSchedules: List<Schedule>) {
        Log.d(tag, "Updating schedules: ${newSchedules.size} items")
        schedules.clear()
        schedules.addAll(newSchedules)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int = schedules.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvScheduleTime: TextView = itemView.findViewById(R.id.tvScheduleTime)
        private val tvScheduleStatus: TextView = itemView.findViewById(R.id.tvScheduleStatus)
        private val btnDeleteSchedule: ImageButton = itemView.findViewById(R.id.btnDeleteSchedule)

        fun bind(schedule: Schedule) {
            try {
                val formattedStartTime = formatTime(schedule.startTime)
                val formattedEndTime = formatTime(schedule.endTime)
                
                // Add day of week to display
                val dayOfWeek = formatDayOfWeek(schedule.dayOfWeek)
                tvScheduleTime.text = "$dayOfWeek: $formattedStartTime - $formattedEndTime"
                
                if (schedule.isAvailable) {
                    tvScheduleStatus.text = "(Available)"
                    tvScheduleStatus.setTextColor(itemView.context.getResources().getColor(android.R.color.holo_green_dark))
                } else {
                    tvScheduleStatus.text = "(Booked)"
                    tvScheduleStatus.setTextColor(itemView.context.getResources().getColor(android.R.color.holo_red_dark))
                }

                Log.d(tag, "Binding schedule: $dayOfWeek, $formattedStartTime - $formattedEndTime, Available: ${schedule.isAvailable}")
                
                btnDeleteSchedule.setOnClickListener {
                    onDeleteClick(schedule)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error binding schedule: ${e.message}", e)
                tvScheduleTime.text = "Error displaying schedule"
                tvScheduleStatus.text = ""
            }
        }
        
        private fun formatDayOfWeek(dayOfWeek: String): String {
            return when (dayOfWeek.uppercase()) {
                "MONDAY" -> "Monday"
                "TUESDAY" -> "Tuesday"
                "WEDNESDAY" -> "Wednesday"
                "THURSDAY" -> "Thursday"
                "FRIDAY" -> "Friday"
                "SATURDAY" -> "Saturday"
                "SUNDAY" -> "Sunday"
                else -> dayOfWeek
            }
        }
        
        private fun formatTime(timeString: String): String {
            try {
                if (timeString.isEmpty()) return "00:00"
                
                // Handle cases where format might be different
                val cleanTimeString = when {
                    timeString.contains("T") -> {
                        // Handle ISO format like "2023-10-10T08:00:00"
                        timeString.substringAfter("T").substringBefore(".")
                    }
                    timeString.length > 5 -> {
                        // Handle extended format "08:00:00"
                        timeString.substring(0, 5)
                    }
                    else -> timeString
                }
                
                // Parse the time string (e.g., "08:00") to a Date
                val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(cleanTimeString) ?: return timeString
                
                // Format to AM/PM format
                val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                return outputFormat.format(date)
            } catch (e: Exception) {
                Log.e(tag, "Error formatting time: $timeString - ${e.message}", e)
                return timeString // Return original if parsing fails
            }
        }
    }
} 