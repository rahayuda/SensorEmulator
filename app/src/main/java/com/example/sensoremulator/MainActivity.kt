package com.example.sensoremulator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var textViewX: TextView
    private lateinit var textViewY: TextView
    private lateinit var textViewZ: TextView
    private lateinit var textViewLocation: TextView

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewX = findViewById(R.id.textViewX)
        textViewY = findViewById(R.id.textViewY)
        textViewZ = findViewById(R.id.textViewZ)
        textViewLocation = findViewById(R.id.textViewLocation)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance().getReference("SensorData")

        // Inisialisasi Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Cek izin lokasi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        getLocationUpdates() // Mendapatkan pembaruan lokasi
    }

    private fun getLocationUpdates() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    textViewLocation.text = "Location: Lat: $latitude, Lon: $longitude"

                    // Metode Push
                    //val locationData = mapOf("latitude" to latitude, "longitude" to longitude)
                    //database.child("location").push().setValue(locationData)

                    // Metode Value
                    val locationData = mapOf("latitude" to latitude, "longitude" to longitude)
                    database.child("location").setValue(locationData)
                }
            }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            textViewX.text = "X: ${it.values[0]}"
            textViewY.text = "Y: ${it.values[1]}"
            textViewZ.text = "Z: ${it.values[2]}"

            // Metode Push
            //val sensorData = mapOf("x" to it.values[0], "y" to it.values[1], "z" to it.values[2])
            //database.child("sensor").push().setValue(sensorData)

            // Metode Value
            val sensorData = mapOf("x" to it.values[0], "y" to it.values[1], "z" to it.values[2])
            database.child("sensor").setValue(sensorData)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        getLocationUpdates() // Mendapatkan pembaruan lokasi saat resume
    }
}
