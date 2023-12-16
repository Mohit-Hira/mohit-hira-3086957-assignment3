package com.example.gpswaypoint

import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material3.*
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.log
import java.lang.Math.sin
import java.lang.Math.tan
@Composable
fun CompassCanvas(waypoints: List<Location>, currentLocation: Location?, rotation: Float
                  , selectedWaypoint: Location?) {
    Canvas(modifier = Modifier.aspectRatio(1f)) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color.White,
            radius = radius-2,
            center = Offset(centerX, centerY)
        )
        val textPaint = Paint().apply {
            textAlign = Paint.Align.CENTER
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }
        withTransform({
            rotate(rotation, pivot = Offset(centerX, centerY))
        }) {

            textPaint.color = Color.Red.toArgb()
            drawContext.canvas.nativeCanvas.drawText("N", centerX, centerY - radius + 20, textPaint)
            textPaint.color = Color.Black.toArgb()
            drawContext.canvas.nativeCanvas.drawText("E", centerX + radius - 20, centerY, textPaint)
            drawContext.canvas.nativeCanvas.drawText("W", centerX - radius + 20, centerY, textPaint)
            drawContext.canvas.nativeCanvas.drawText("S", centerX, centerY + radius - 20, textPaint)
        }
        waypoints.forEach { waypoint ->
            if (currentLocation != null) {
                val angle = bearingTo(currentLocation, waypoint)
                val adjustedAngle = Math.toRadians((angle - rotation).toDouble())
                val lineEndX = (centerX + radius * cos(adjustedAngle)).toFloat()
                val lineEndY = (centerY + radius * sin(adjustedAngle)).toFloat()
                val arrowLength = radius
                val arrowHeadSize = 40f
                if (waypoint == selectedWaypoint) {
                    drawLine(
                        color = if (waypoint == selectedWaypoint) Color.Green else Color.Blue,
                        start = Offset(centerX, centerY),
                        end = Offset(lineEndX, lineEndY),
                        strokeWidth = 4f
                    )
                }

                val endX = (centerX + arrowLength * cos(adjustedAngle)).toFloat()
                val endY = (centerY + arrowLength * sin(adjustedAngle)).toFloat()

                val arrowPath = Path().apply {
                    moveTo(endX, endY)
                    lineTo((endX - arrowHeadSize * cos(adjustedAngle - Math.PI / 6)).toFloat(),
                        (endY - arrowHeadSize * sin(adjustedAngle - Math.PI / 6)).toFloat()
                    )
                    lineTo((endX - arrowHeadSize * cos(adjustedAngle + Math.PI / 6)).toFloat(),
                        (endY - arrowHeadSize * sin(adjustedAngle + Math.PI / 6)).toFloat()
                    )
                    close()
                }
                if (waypoint == selectedWaypoint) {
                    drawPath(
                        arrowPath,
                        color = if (waypoint == selectedWaypoint) Color.Green else Color.Blue
                    )
                }
            }
            waypoints.forEach { waypoint ->
                currentLocation?.let { currentLoc ->
                    val distance = currentLoc.distanceTo(waypoint)
                    if (distance <= 500) {
                        val scale = distance / 500f
                        val angle = bearingTo(currentLoc, waypoint)
                        val adjustedAngle = Math.toRadians((angle - rotation).toDouble())
                        val waypointX = centerX + radius * scale * cos(adjustedAngle).toFloat()
                        val waypointY = centerY + radius * scale * sin(adjustedAngle).toFloat()
                        drawCircle(
                            color = if (waypoint == selectedWaypoint) Color.Magenta else Color.Cyan,
                            radius = if (waypoint == selectedWaypoint) 10f else 8f, // Larger circle for selected waypoint
                            center = Offset(waypointX, waypointY)
                        )
                    }
                }
            }
        }

    }
}
@Composable
fun GPSApp(isTracking: Boolean,
           showClearDialog: Boolean,
           waypoints: List<Location>,
           currentLocation: State<Location?>,
           selectedWaypoint: MutableState<Location?>,
           onSaveWaypoint: () -> Unit,
           onClearWaypoints: () -> Unit,
           onStartTracking: () -> Unit,
           onStopTracking: () -> Unit,
           onDialogSelect: () -> Unit,
           onDialogDeSelect: () -> Unit,
           compassRotation: Float) {

    Column( verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,) {
        Spacer(modifier = Modifier.width(10.dp))
        CompassCanvas(waypoints, currentLocation.value, compassRotation,selectedWaypoint.value)

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        )
        {
            TrackingButton(
                isTracking = isTracking,
                onStartTracking = onStartTracking,
                onStopTracking = onStopTracking,

                )
            Spacer(modifier = Modifier.width(10.dp))
            if (isTracking) {
                SaveWaypointButton (onSaveWaypoint)

            }
        }

        Spacer(modifier = Modifier.width(10.dp))
        if (isTracking) {
            Spacer(modifier = Modifier.height(5.dp))
            currentLocation.value?.let { location ->
                Text("Current Location: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}"
                    , fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Left)
            }
        }
        if (waypoints.isNotEmpty()) {
            Button(
                modifier = Modifier.size(width = 180.dp, height = 50.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    containerColor = Color.DarkGray,
                ), onClick = onDialogSelect
            ) {
                Text(
                    color = Color.White, fontSize = 14.sp,
                    text = "Clear Waypoints"
                )
            }
        }

        if (showClearDialog) {
            AlertDialog(
                onDismissRequest = onDialogDeSelect,
                title = { Text("Clear Waypoints") },
                text = { Text("Are you sure you want to clear all waypoints?") },
                confirmButton = {
                    Button(modifier = Modifier.size(width = 120.dp, height = 50.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Color.DarkGray,
                        ),onClick =
                        onClearWaypoints
                    ) {
                        Text(color = Color.White,fontSize = 14.sp,
                            text="Confirm")
                    }
                },
                dismissButton = {
                    Button(modifier = Modifier.size(width = 120.dp, height = 50.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            containerColor = Color.DarkGray,
                        ),onClick = onDialogDeSelect) {
                        Text(color = Color.White,fontSize = 14.sp,
                            text="Cancel")
                    }
                }
            )
        }
        selectedWaypoint.value?.let { waypoint ->
            val distance = currentLocation.value?.distanceTo(waypoint) ?: 0f
            Text("Distance to Waypoint: ${String.format("%.2f", distance)}m", fontSize = 12.sp,
                color = Color.Gray,fontWeight=FontWeight.Bold,
                textAlign = TextAlign.Left)
        }
        WaypointSelector(waypoints, selectedWaypoint)
    }
}
@Composable
fun WaypointSelector(   waypoints: List<Location>,selectedWaypoint: MutableState<Location?>) {
    if (waypoints.isNotEmpty()) {
        Text("Select Waypoint",fontSize = 12.sp,
            color = Color.Blue,
            textAlign = TextAlign.Left)

        LazyColumn {

            itemsIndexed(waypoints) { index, waypoint ->
                WaypointItem(waypoint, index,selectedWaypoint)
            }
        }
    }
}
@Composable
fun WaypointItem(waypoint: Location, index: Int,selectedWaypoint: MutableState<Location?>) {
    val isSelected = waypoint == selectedWaypoint.value

    Card(
        backgroundColor = if (isSelected) Color.Yellow else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { selectedWaypoint.value = waypoint }
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Text("Waypoint ${index + 1}",fontSize = 16.sp,
                fontWeight = FontWeight.Bold,color = Color.Gray,
                textAlign = TextAlign.Left)
            Text("Latitude: ${waypoint.latitude}", fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Left)
            Text("Longitude: ${waypoint.longitude}", fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Left)
        }
    }
}
@Composable
fun TrackingButton(isTracking: Boolean, onStartTracking: () -> Unit, onStopTracking: () -> Unit) {
    Button( modifier = Modifier.size(width = 180.dp, height = 50.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color.DarkGray,
        ),onClick = {
            if (isTracking) onStopTracking() else onStartTracking()
        }) {
        Text(color = Color.White, fontSize = 14.sp,
            text=if (isTracking) "Stop Tracking" else "Start Tracking")
    }
}
@Composable
fun SaveWaypointButton(onSaveWaypoint: () -> Unit) {
    Button( modifier = Modifier.size(width = 180.dp, height = 50.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color.DarkGray,
        ),onClick = onSaveWaypoint) {
        Text(color = Color.White,fontSize = 14.sp,
            text="Save Waypoint")
    }
}
fun bearingTo(start: Location, end: Location): Float {
    val startLat = Math.toRadians(start.latitude)
    val startLong = Math.toRadians(start.longitude)
    val endLat = Math.toRadians(end.latitude)
    val endLong = Math.toRadians(end.longitude)
    val dLong = endLong - startLong
    val dPhi = log(tan(endLat / 2.0 + Math.PI / 4.0) / tan(startLat / 2.0 + Math.PI / 4.0))
    return (Math.toDegrees(atan2(dLong, dPhi)).toFloat() + 360.0f) % 360.0f
}