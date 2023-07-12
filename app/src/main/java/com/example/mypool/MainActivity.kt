package com.example.mypool

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class MainActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference
    private lateinit var phData: TextView
    private lateinit var turbidityData: TextView
    private lateinit var turbidityStatus: TextView
    private lateinit var phStatus: TextView
    private lateinit var turbidityProgressBar: ProgressBar
    private lateinit var phProgressBar: ProgressBar
    private lateinit var phCard: CardView
    private lateinit var turbidityCard: CardView
    private lateinit var feedingCard: CardView
    private lateinit var outputFuzzy: TextView

    private var phNetral: String = "Netral"
    private var phAsam: String = "Asam"
    private var phBasa: String = "Basa"

    private var turbidityNormal: String = "Normal"
    private var turbidityKeruh: String = "Keruh"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        phData = findViewById(R.id.ph_data)
        turbidityData = findViewById(R.id.turbidity_data)
        phStatus = findViewById(R.id.ph_status)
        turbidityStatus = findViewById(R.id.turbidity_status)
        turbidityProgressBar = findViewById(R.id.turbidity_progressBar)
        phProgressBar = findViewById(R.id.ph_progressBar)
        phCard = findViewById(R.id.card_ph)
        turbidityCard = findViewById(R.id.card_turbidity)
        feedingCard = findViewById(R.id.card_feeding)
        outputFuzzy = findViewById(R.id.notif)

        database = FirebaseDatabase.getInstance()
        dbRef = database.reference.child("Data")

//        dbRef.child("PH").get().addOnSuccessListener {
//            phData.text = it.value.toString()
//        }

        val past = System.currentTimeMillis()
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val dataPH = snapshot.child("PH").value.toString()
                    val dataTurbidity = snapshot.child("Turbidity").value.toString()
                    phData.text = dataPH
                    turbidityData.text = dataTurbidity + " NTU"

                    phProgressBar.progress = dataPH.toFloat().toInt()
                    if (dataPH.toFloat() <= 5.0) {
                        phStatus.text = phAsam
                        phStatus.setTextColor(Color.parseColor("#FEDB39"))
                    } else if (dataPH.toFloat() in 5.1..8.0) {
                        phStatus.text = phNetral
                        phStatus.setTextColor(Color.parseColor("#1CD6CE"))
                    } else if (dataPH.toFloat() in 8.1..14.0) {
                        phStatus.text = phBasa
                        phStatus.setTextColor(Color.parseColor("#D61C4E"))
                    }

                    turbidityProgressBar.progress = dataTurbidity.toFloat().toInt()
                    if (dataTurbidity.toFloat().toInt() in 0..50) {
                        turbidityStatus.text = turbidityNormal
                        turbidityStatus.setTextColor(Color.parseColor("#1CD6CE"))
                    } else if (dataTurbidity.toFloat().toInt() in 51..100) {
                        turbidityStatus.text = turbidityKeruh
                        turbidityStatus.setTextColor(Color.parseColor("#FEDB39"))
                    } else if (dataTurbidity.toFloat().toInt() > 100) {
                        turbidityStatus.text = "Danger"
                        turbidityStatus.setTextColor(Color.parseColor("#D61C4E"))
                    }
                    val future = System.currentTimeMillis()
                    val timer = future - past
                    val durationString: Duration = timer.milliseconds
                    Log.d(TAG, "$durationString = onDataChange: $timer")
                }

                if (phStatus.text == phAsam && turbidityStatus.text == turbidityNormal){
                    outputFuzzy.text = "Kadar PH Menurun, disarankan untuk menambahkan kapur agar PH netral"
                } else if (phStatus.text == phNetral && turbidityStatus.text == turbidityNormal){
                    outputFuzzy.text = "Kondisi air kolam masih aman"
                } else if (phStatus.text == phBasa && turbidityStatus.text == turbidityNormal){
                    outputFuzzy.text = "Kadar PH naik, disarankan untuk mengganti air kolam"
                } else if (phStatus.text == phAsam && turbidityStatus.text == turbidityKeruh){
                    outputFuzzy.text = "Kadar PH menurun dan Air kolam Keruh, disarankan untuk mengganti air kolam"
                } else if (phStatus.text == phNetral && turbidityStatus.text == turbidityKeruh){
                    outputFuzzy.text = "Kadar PH netral namun Air kolam Keruh, disarankan untuk mengganti air kolam"
                } else if (phStatus.text == phBasa && turbidityStatus.text == turbidityKeruh){
                    outputFuzzy.text = "Kadar PH naik dan Air kolam Keruh, disarankan untuk mengganti air kolam"
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

        phCard.setOnClickListener {
            val intentPh = Intent(this, PhActivity::class.java)
            startActivity(intentPh)
        }
        turbidityCard.setOnClickListener {
            val intentTurbidity = Intent(this, TurbidityActivity::class.java)
            startActivity(intentTurbidity)
        }
        feedingCard.setOnClickListener {
            val intentFeeding = Intent(this, FeedingActivity::class.java)
            startActivity(intentFeeding)
        }
    }
}