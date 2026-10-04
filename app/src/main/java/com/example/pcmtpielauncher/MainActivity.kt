package com.example.pcmtpielauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pcmtpielauncher.ui.theme.PCMTPieLauncherTheme

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

        // Center PCMT button
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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Pie slots
        PieItem("1", Modifier.align(Alignment.TopCenter))
        PieItem("2", Modifier.align(Alignment.CenterEnd))
        PieItem("3", Modifier.align(Alignment.BottomCenter))
        PieItem("4", Modifier.align(Alignment.CenterStart))

        PieItem("5", Modifier.align(Alignment.TopEnd))
        PieItem("6", Modifier.align(Alignment.BottomEnd))
        PieItem("7", Modifier.align(Alignment.BottomStart))
        PieItem("8", Modifier.align(Alignment.TopStart))
    }
}

@Composable
fun PieItem(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(62.dp)
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
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}