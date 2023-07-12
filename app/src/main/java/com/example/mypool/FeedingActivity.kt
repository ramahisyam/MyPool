package com.example.mypool

import android.app.*
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class FeedingActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRefFeed: DatabaseReference
    private lateinit var dbRefScheduling: DatabaseReference
    private lateinit var dbRefData: DatabaseReference
    private lateinit var feedNow: Button
    private lateinit var feedSchedule: Button
    private lateinit var adapter: ScheduleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    private var jumlah_ikan: String = ""
    private var few: String = "Few"
    private var moderate: String = "Moderate"
    private var many: String = "Many"

    private var phAir: String = ""
    private var phNetral: String = "Netral"
    private var phAsam: String = "Asam"
    private var phBasa: String = "Basa"

    private lateinit var turbidity: String
    private var turbidityClear: String = "Normal"
    private var turbidityCloudly: String = "Keruh"

    private var jumlah_pakan: String = ""
    private var low: String = "Low"
    private var medium: String = "Medium"
    private var high: String = "High"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scheduler = AndroidAlarmScheduler(this)
        var alarmItem: ScheduleData? = null
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
            setAlarm(timestamp)
        }
        createNotificationChannel()
        val timePickerDialog = TimePickerDialog(this,
            timeSetListener, hour, minute, true)

        feedNow.setOnClickListener {
            val startTime = System.currentTimeMillis()
//            dbRefFeed.child("feed_now").setValue(1)
            fuzzy()
            val endTime = System.currentTimeMillis()
            val timer = endTime - startTime
            val durationString: Duration = timer.milliseconds

            Log.d(TAG, "onDataFeeding: $timer")
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

    private fun createNotificationChannel() {
        val name : CharSequence = "reminderChannel"
        val description = "Channel for Alarm Manager"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("foxandroid", name, importance)
        channel.description = description
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun setAlarm(timeInMillis: Long) {
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)

        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun fuzzy() {
        database = FirebaseDatabase.getInstance()
        dbRefData = database.reference.child("Data")

        dbRefData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val dataJumlah = snapshot.child("jumlah_ikan").value.toString()
                    val dataPH = snapshot.child("PH").value.toString()
                    val dataTurbidity = snapshot.child("Turbidity").value.toString()

                    if (dataJumlah.toInt() in 1..7) {
                        jumlah_ikan = few
                    } else if (dataJumlah.toInt() in 8..15) {
                        jumlah_ikan = moderate
                    } else if (dataJumlah.toInt() in 16..25) {
                        jumlah_ikan = many
                    }

                    if (dataPH.toFloat() <= 5.0) {
                        phAir = phAsam
                    } else if (dataPH.toFloat() in 5.1..8.0) {
                        phAir = phNetral
                    } else if (dataPH.toFloat() in 8.1..14.0) {
                        phAir = phBasa
                    }


                    turbidity = if (dataTurbidity.toFloat().toInt() in 0..50) {
                        turbidityClear
                    } else {
                        turbidityCloudly
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("TAG", "onCancelled: $error")
            }

        })

        if (jumlah_ikan == few && phAir == phAsam && turbidity == turbidityClear){
            //1
            jumlah_pakan = low
        } else if (jumlah_ikan == few && phAir == phAsam && turbidity == turbidityCloudly){
            //2
            jumlah_pakan = low
        } else if (jumlah_ikan == few && phAir == phNetral && turbidity == turbidityClear) {
            //3
            jumlah_pakan = medium
        } else if (jumlah_ikan == few && phAir == phNetral && turbidity == turbidityCloudly) {
            //4
            jumlah_pakan = medium
        } else if (jumlah_ikan == few && phAir == phBasa && turbidity == turbidityClear) {
            //5
            jumlah_pakan = high
        } else if (jumlah_ikan == few && phAir == phBasa && turbidity == turbidityCloudly) {
            //6
            jumlah_pakan = medium
        } else if (jumlah_ikan == moderate && phAir == phAsam && turbidity == turbidityClear) {
            //7
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phAsam && turbidity == turbidityCloudly) {
            //8
            jumlah_pakan = medium
        } else if (jumlah_ikan == moderate && phAir == phNetral && turbidity == turbidityClear) {
            //9
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phNetral && turbidity == turbidityCloudly) {
            //10
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phBasa && turbidity == turbidityClear) {
            //11
            jumlah_pakan = high
        } else if (jumlah_ikan == moderate && phAir == phBasa && turbidity == turbidityCloudly) {
            //12
            jumlah_pakan = medium
        } else if (jumlah_ikan == many && phAir == phAsam && turbidity == turbidityClear) {
            //13
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phAsam && turbidity == turbidityCloudly) {
            //14
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phNetral && turbidity == turbidityClear) {
            //15
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phNetral && turbidity == turbidityCloudly) {
            //16
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phBasa && turbidity == turbidityClear) {
            //17
            jumlah_pakan = high
        } else if (jumlah_ikan == many && phAir == phBasa && turbidity == turbidityCloudly) {
            //18
            jumlah_pakan = medium
        }

        when (jumlah_pakan) {
            low -> {
//                looping(1)
                database = FirebaseDatabase.getInstance()
                dbRefFeed = database.reference.child("Feed")
                dbRefFeed.child("feed_now").setValue(1)
            }
            medium -> {
//                looping(3)
                database = FirebaseDatabase.getInstance()
                dbRefFeed = database.reference.child("Feed")
                dbRefFeed.child("feed_now").setValue(2)
            }
            high -> {
//                looping(5)
                database = FirebaseDatabase.getInstance()
                dbRefFeed = database.reference.child("Feed")
                dbRefFeed.child("feed_now").setValue(3)
            }
        }

    }

    private fun looping(j: Int) {
        database = FirebaseDatabase.getInstance()
        dbRefFeed = database.reference.child("Feed")
        val delayMillis = 8000L // Delay of 1 second

        CoroutineScope(Dispatchers.Main).launch {
            for (i in 0 until j) {
                dbRefFeed.child("feed_now").setValue(1)
                delay(i * delayMillis)

                // Code to be executed after the delay
                // Place your loop logic here
                println("Iteration: $i")
            }
        }
    }
}