package com.example.pcmtpielauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pcmtpielauncher.ui.theme.PCMTPieLauncherTheme
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PCMTPieLauncherTheme {
                PCMTLauncher()
            }
        }
    }
}

@Composable
fun PCMTLauncher() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101014)),
        contentAlignment = Alignment.Center
    ) {

        val items = listOf(
            "1", "2", "3", "4",
            "5", "6", "7", "8"
        )

        val radius = 135.dp

        items.forEachIndexed { index, label ->

            val angle =
                Math.toRadians((index * 45.0) - 90.0)

            val x =
                (cos(angle) * radius.value).dp

            val y =
                (sin(angle) * radius.value).dp

            PieItem(
                label = label,
                modifier = Modifier.offset(
                    x = x,
                    y = y
                )
            )
        }

        // Center button
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(
                    color = Color(0xFF25252D),
                    shape = CircleShape
                )
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "PCMT",
                color = Color.White,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
fun PieItem(
    label: String,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                color = Color(0xFF30303A),
                shape = CircleShape
            )
            .clickable { },
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}