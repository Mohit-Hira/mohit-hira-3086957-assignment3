package com.example.gpswaypoint

import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
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

        // Draw the compass circle
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
        // Apply rotation to the canvas
        withTransform({
            // Apply rotation to the canvas
            rotate(rotation, pivot = Offset(centerX, centerY))
        }) {
            val textPaint = Paint().apply {
                textAlign = Paint.Align.CENTER
                textSize = 40f
                typeface = Typeface.DEFAULT_BOLD
            }

            // Draw the compass directions
            // Drawing North
            textPaint.color = Color.Red.toArgb()
            drawContext.canvas.nativeCanvas.drawText("N", centerX, centerY - radius + 20, textPaint)

            // Drawing East
            textPaint.color = Color.Black.toArgb()
            drawContext.canvas.nativeCanvas.drawText("E", centerX + radius - 20, centerY, textPaint)

            // Drawing West
            drawContext.canvas.nativeCanvas.drawText("W", centerX - radius + 20, centerY, textPaint)

            // Drawing South
            drawContext.canvas.nativeCanvas.drawText("S", centerX, centerY + radius - 20, textPaint)
        }
        // Draw lines pointing towards each waypoint


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






