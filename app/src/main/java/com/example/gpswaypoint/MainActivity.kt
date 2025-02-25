package com.example.gpswaypoint

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class MainActivity : ComponentActivity(),SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var compassRotation = mutableStateOf(0f)
    private lateinit var locationManager: LocationManager
    private var waypoints = mutableListOf<Location>()
    private var selectedWaypoint = mutableStateOf<Location?>(null)
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            currentLocation.value = location
            checkProximityToCurrentWaypoint(location)
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    private val currentLocation = mutableStateOf<Location?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        setContent {
            var isTracking by remember { mutableStateOf(false) }
            var showClearDialog by remember { mutableStateOf(false) }
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(),color = MaterialTheme.colors.background) {
                    GPSApp(isTracking = isTracking,
                        showClearDialog = showClearDialog,
                        waypoints = waypoints,
                        currentLocation = currentLocation,
                        selectedWaypoint = selectedWaypoint,
                        onSaveWaypoint = {  currentLocation.value?.let { location ->
                            addWaypoint(location)
                        } },
                        onClearWaypoints = {  clearWaypoints()
                            showClearDialog = false
                            selectedWaypoint.value=null },
                        onStartTracking = {
                            isTracking = true
                            startTracking()
                        },
                        onStopTracking = {
                            isTracking = false
                            stopTracking()
                        },
                        onDialogSelect={ showClearDialog = true },
                        onDialogDeSelect={ showClearDialog = false },
                        compassRotation = compassRotation.value)
                }
            }
        }

    }
    private fun checkProximityToCurrentWaypoint(currentLocation: Location) {
        val currentSelectedIndex = waypoints.indexOf(selectedWaypoint.value)
        selectedWaypoint.value?.let { currentWaypoint ->
            if (currentLocation.distanceTo(currentWaypoint) < 10) {
                if (currentSelectedIndex > 0) {
                    selectedWaypoint.value = waypoints[currentSelectedIndex -1]
                }
            }
        }
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            compassRotation.value = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    private fun startTracking() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        setupLocationTracking()
    }
    private fun setupLocationTracking() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0f, locationListener)
    }
    private fun clearWaypoints() {
        waypoints.clear()
        saveWaypointsToFile()
    }
    private fun stopTracking() {

        if (this::locationManager.isInitialized) {
            locationManager.removeUpdates(locationListener)
        }
    }
    private fun addWaypoint(location: Location) {
        waypoints.add(location)
        saveWaypointsToFile()
    }
    private fun saveWaypointsToFile() {
        val waypointsJson = Gson().toJson(waypoints)
        val file = File(filesDir, "waypoints.json")
        file.writeText(waypointsJson)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupLocationTracking()
        }
    }
}
