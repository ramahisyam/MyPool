package com.example.mypool

import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mypool.model.ScheduleData
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.text.SimpleDateFormat
import java.util.*

class FeedingActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRefFeed: DatabaseReference
    private lateinit var dbRefScheduling: DatabaseReference
    private lateinit var feedNow: Button
    private lateinit var feedSchedule: Button
    private lateinit var adapter: ScheduleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var list : List<ScheduleData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeding)
        feedNow = findViewById(R.id.feed)
        feedSchedule = findViewById(R.id.add_schedule)
        recyclerView = findViewById(R.id.rv_schedule)

        database = FirebaseDatabase.getInstance()
        dbRefFeed = database.reference.child("Feed")
        dbRefScheduling = database.reference.child("Schedule")
        val databaseRef = FirebaseDatabase.getInstance().getReference("Schedule")

        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            // Create a Calendar object and set the selected time
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

            // Get the Unix timestamp (in milliseconds) for the selected time
            val timestamp = calendar.timeInMillis

            // Store the timestamp in the database
            dbRefScheduling.push().setValue(timestamp)
        }
        val timePickerDialog = TimePickerDialog(this,
            timeSetListener, hour, minute, true)

        feedNow.setOnClickListener {
            dbRefFeed.child("feed_now").setValue(1)
        }

        feedSchedule.setOnClickListener {
            timePickerDialog.show()
        }

        adapter = ScheduleAdapter(emptyList())
        recyclerView.adapter = adapter
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                val dataList = snapshot.children.mapNotNull { it.getValue(ScheduleData::class.java) }
//                val dataList = snapshot.getValue<ScheduleData>()
//                val dataList = ArrayList<ScheduleData>()
//                for (dataSnapshot in snapshot.children) {
//                    val data = dataSnapshot.getValue(ScheduleData::class.java)
//                    dataList.add(data!!)
//                }
//                adapter = ScheduleAdapter(dataList)
                val dataList = mutableListOf<ScheduleData>()
                for (childSnapshot in snapshot.children) {
                    val scheduleTime = childSnapshot.getValue(Long::class.java)
                    val date = Date(scheduleTime!!)
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val formattedTime = format.format(date)
                    if (scheduleTime != null) {
                        dataList.add(ScheduleData(scheduleTime))
                    }
                }
                val adapter = ScheduleAdapter(dataList)
                recyclerView.layoutManager = LinearLayoutManager(this@FeedingActivity)
                recyclerView.adapter = adapter
//                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "loadPost:onCancelled", error.toException())
            }

        })
    }
}