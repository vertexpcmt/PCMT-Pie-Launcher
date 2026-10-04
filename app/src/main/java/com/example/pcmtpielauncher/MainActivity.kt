package com.example.pcmtpielauncher

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.pcmtpielauncher.ui.theme.PCMTPieLauncherTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.atan2
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

data class LauncherApp(
    val label: String,
    val packageName: String,
    val icon: Drawable
)

@Composable
fun PCMTLauncher() {

    val context = LocalContext.current

    val apps = remember {
        getLaunchableApps(context).take(8)
    }

    // ---------------------------------------------------------
    // BLUE AUTOMATIC ANIMATION
    // ---------------------------------------------------------

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "pie_animation"
        )

    val blueRotation =
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 4500,
                    easing = LinearEasing
                )
            ),
            label = "blue_rotation"
        )

    // ---------------------------------------------------------
    // USER WHEEL ROTATION
    // ---------------------------------------------------------

    var wheelRotation by remember {
        mutableFloatStateOf(0f)
    }

    var previousPointerX by remember {
        mutableFloatStateOf(0f)
    }

    var previousPointerY by remember {
        mutableFloatStateOf(0f)
    }

    var angularVelocity by remember {
        mutableFloatStateOf(0f)
    }

    val scope = rememberCoroutineScope()

    var inertiaJob by remember {
        mutableStateOf<Job?>(null)
    }

    // ---------------------------------------------------------
    // MAIN SCREEN
    // ---------------------------------------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B12)),
        contentAlignment = Alignment.Center
    ) {

        // -----------------------------------------------------
        // OUTER BLUE RING
        // -----------------------------------------------------

        Canvas(
            modifier = Modifier.size(390.dp)
        ) {

            drawArc(
                color = Color(0xFF168CFF),
                startAngle = blueRotation.value,
                sweepAngle = 100f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            drawArc(
                color = Color(0xFF168CFF).copy(alpha = 0.25f),
                startAngle = blueRotation.value + 180f,
                sweepAngle = 75f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // -----------------------------------------------------
        // ROTATABLE APP WHEEL
        // -----------------------------------------------------

        Box(
            modifier = Modifier
                .size(330.dp)
                .pointerInput(Unit) {

                    detectDragGestures(

                        onDragStart = { position ->

                            // Stop previous inertia.
                            inertiaJob?.cancel()

                            angularVelocity = 0f

                            previousPointerX = position.x
                            previousPointerY = position.y
                        },

                        onDrag = { change, _ ->

                            val centerX =
                                size.width / 2f

                            val centerY =
                                size.height / 2f

                            // Current pointer position.
                            val currentX =
                                change.position.x - centerX

                            val currentY =
                                change.position.y - centerY

                            // Previous pointer position.
                            val previousX =
                                previousPointerX - centerX

                            val previousY =
                                previousPointerY - centerY

                            // Previous angle.
                            val previousAngle =
                                atan2(
                                    previousY,
                                    previousX
                                )

                            // Current angle.
                            val currentAngle =
                                atan2(
                                    currentY,
                                    currentX
                                )

                            var deltaAngle =
                                Math.toDegrees(
                                    (
                                            currentAngle -
                                                    previousAngle
                                            ).toDouble()
                                ).toFloat()

                            // Prevent jumps across -180 / +180.
                            if (deltaAngle > 180f) {
                                deltaAngle -= 360f
                            }

                            if (deltaAngle < -180f) {
                                deltaAngle += 360f
                            }

                            // Rotate complete wheel.
                            wheelRotation += deltaAngle

                            // Store movement speed.
                            angularVelocity =
                                deltaAngle * 60f

                            // Save current pointer.
                            previousPointerX =
                                change.position.x

                            previousPointerY =
                                change.position.y
                        },

                        onDragEnd = {

                            if (
                                kotlin.math.abs(
                                    angularVelocity
                                ) > 10f
                            ) {

                                inertiaJob =
                                    scope.launch {

                                        var velocity =
                                            angularVelocity

                                        while (
                                            kotlin.math.abs(
                                                velocity
                                            ) > 1f
                                        ) {

                                            wheelRotation +=
                                                velocity * 0.016f

                                            // Friction.
                                            velocity *= 0.93f

                                            delay(16)
                                        }
                                    }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {

            // -------------------------------------------------
            // APP POSITIONS
            // -------------------------------------------------

            val radius = 135.dp

            apps.forEachIndexed { index, app ->

                val angleDegrees =
                    (index * 45.0) -
                            90.0 +
                            wheelRotation

                val angle =
                    Math.toRadians(angleDegrees)

                val x =
                    (
                            cos(angle) *
                                    radius.value
                            ).dp

                val y =
                    (
                            sin(angle) *
                                    radius.value
                            ).dp

                PieAppItem(
                    app = app,
                    blueRotation =
                        blueRotation.value,
                    modifier =
                        Modifier.offset(
                            x = x,
                            y = y
                        )
                )
            }
        }

        // -----------------------------------------------------
        // CENTER PCMT BUTTON
        // -----------------------------------------------------

        Box(
            modifier = Modifier
                .size(110.dp)
                .background(
                    color = Color(0xFF111722),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {

            Canvas(
                modifier = Modifier.size(110.dp)
            ) {

                drawCircle(
                    color = Color(0xFF168CFF),
                    radius =
                        size.minDimension / 2f -
                                3.dp.toPx(),
                    style =
                        androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx()
                        )
                )
            }

            Text(
                text = "PCMT",
                color = Color.White
            )
        }
    }
}

@Composable
fun PieAppItem(
    app: LauncherApp,
    blueRotation: Float,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(78.dp)

            .background(Color(0xFF111722))
            .clickable {

                val launchIntent =
                    context.packageManager
                        .getLaunchIntentForPackage(
                            app.packageName
                        )

                launchIntent?.let {
                    context.startActivity(it)
                }
            },
        contentAlignment = Alignment.Center
    ) {

        // -----------------------------------------------------
        // BLUE APP RING
        // -----------------------------------------------------

        Canvas(
            modifier = Modifier.size(78.dp)
        ) {

            drawArc(
                color = Color(0xFF168CFF),
                startAngle = blueRotation,
                sweepAngle = 90f,
                useCenter = false,
                style =
                    androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
            )

            drawArc(
                color =
                    Color(0xFF168CFF)
                        .copy(alpha = 0.35f),
                startAngle =
                    blueRotation + 180f,
                sweepAngle = 55f,
                useCenter = false,
                style =
                    androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
            )
        }

        // -----------------------------------------------------
        // REAL APP ICON
        // -----------------------------------------------------

        AndroidView(
            factory = { ctx ->

                ImageView(ctx).apply {

                    scaleType =
                        ImageView.ScaleType.FIT_CENTER

                    setImageDrawable(app.icon)

                    setPadding(
                        10,
                        10,
                        10,
                        10
                    )

                    contentDescription =
                        app.label
                }
            },
            modifier = Modifier.size(62.dp)
        )
    }
}

// -------------------------------------------------------------
// FIND INSTALLED LAUNCHABLE APPS
// -------------------------------------------------------------

fun getLaunchableApps(
    context: Context
): List<LauncherApp> {

    val intent =
        Intent(Intent.ACTION_MAIN).apply {
            addCategory(
                Intent.CATEGORY_LAUNCHER
            )
        }

    val resolveInfos =
        context.packageManager
            .queryIntentActivities(
                intent,
                0
            )

    return resolveInfos
        .map {

            LauncherApp(
                label =
                    it.loadLabel(
                        context.packageManager
                    ).toString(),

                packageName =
                    it.activityInfo.packageName,

                icon =
                    it.loadIcon(
                        context.packageManager
                    )
            )
        }
        .distinctBy {
            it.packageName
        }
        .sortedBy {
            it.label.lowercase()
        }
}