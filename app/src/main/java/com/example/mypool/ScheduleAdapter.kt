package com.example.mypool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mypool.model.ScheduleData
import java.text.SimpleDateFormat
import java.util.*

class ScheduleAdapter(private val dataList: List<ScheduleData>) :
    RecyclerView.Adapter<ScheduleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_schedule_list, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
//        holder.bind(dataList[position])
        val scheduleData = dataList[position]
        val scheduleTime = scheduleData.schedule

        val date = Date(scheduleTime!!)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = format.format(date)
        holder.scheduleTv.text = formattedTime
    }

    override fun getItemCount() = dataList.size
}

class ScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val scheduleTv: TextView = itemView.findViewById(R.id.schedule_data)
    fun bind(data: ScheduleData) {
//        scheduleTv.text = data.schedule.toString()
    }
}