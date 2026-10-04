package com.example.pcmtpielauncher

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
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

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val windowInsetsController =
            WindowInsetsControllerCompat(window, window.decorView)

        windowInsetsController.hide(
            WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars()
        )

        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

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

    var showAllApps by remember {
        mutableStateOf(false)
    }

    BackHandler(
        enabled = showAllApps
    ) {
        showAllApps = false
    }

    if (showAllApps) {
        AllAppsScreen(
            onBack = {
                showAllApps = false
            }
        )
    } else {
        PCMTLauncherHome(
            onOpenAllApps = {
                showAllApps = true
            }
        )
    }
}

@Composable
fun PCMTLauncherHome(
    onOpenAllApps: () -> Unit
) {

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
        // COOL SCI-FI OUTER RING
        // -----------------------------------------------------

        Canvas(
            modifier = Modifier.size(390.dp)
        ) {

            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Soft outer circle
            drawCircle(
                color = Color(0xFF168CFF).copy(alpha = 0.10f),
                radius = size.minDimension / 2f - 4.dp.toPx(),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx()
                )
            )

            // Main rotating segmented arc
            drawArc(
                color = Color(0xFF168CFF),
                startAngle = blueRotation.value,
                sweepAngle = 125f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Opposite smaller arc
            drawArc(
                color = Color(0xFF168CFF).copy(alpha = 0.55f),
                startAngle = blueRotation.value + 180f,
                sweepAngle = 70f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Thin inner rotating arc
            drawArc(
                color = Color(0xFF42A5FF).copy(alpha = 0.75f),
                startAngle = -blueRotation.value * 0.65f + 35f,
                sweepAngle = 45f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Small fixed-style markers around the ring
            for (i in 0 until 12) {

                val angle =
                    Math.toRadians(
                        (i * 30.0) - 90.0
                    )

                val outerRadius =
                    size.minDimension / 2f - 13.dp.toPx()

                val innerRadius =
                    outerRadius - 8.dp.toPx()

                val x1 =
                    centerX +
                            cos(angle).toFloat() *
                            innerRadius

                val y1 =
                    centerY +
                            sin(angle).toFloat() *
                            innerRadius

                val x2 =
                    centerX +
                            cos(angle).toFloat() *
                            outerRadius

                val y2 =
                    centerY +
                            sin(angle).toFloat() *
                            outerRadius

                drawLine(
                    color =
                        Color(0xFF168CFF)
                            .copy(alpha = 0.35f),
                    start =
                        androidx.compose.ui.geometry.Offset(
                            x1,
                            y1
                        ),
                    end =
                        androidx.compose.ui.geometry.Offset(
                            x2,
                            y2
                        ),
                    strokeWidth = 2.dp.toPx()
                )
            }
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
                )
                .clickable {
                    onOpenAllApps()
                },
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
    val scope = rememberCoroutineScope()

    var selected by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1.0f,
        animationSpec = tween(160),
        label = "app_selection_scale"
    )

    Box(
        modifier = modifier
            .size(96.dp),
        contentAlignment = Alignment.Center
    ) {

        // -----------------------------------------------------
        // SELECTION / HOVER EFFECT
        // -----------------------------------------------------

        Canvas(
            modifier = Modifier
                .size(90.dp)
        ) {

            if (selected) {

                drawCircle(
                    color = Color(0xFF168CFF).copy(alpha = 0.16f),
                    radius = size.minDimension / 2f
                )

                drawCircle(
                    color = Color(0xFF42A5FF).copy(alpha = 0.85f),
                    radius = size.minDimension / 2f - 3.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx()
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .size((78f * scale).dp)
                .background(
                    color = Color(0xFF111722),
                    shape = CircleShape
                )
                .clickable {

                    selected = true

                    scope.launch {

                        delay(260)

                        val launchIntent =
                            context.packageManager
                                .getLaunchIntentForPackage(
                                    app.packageName
                                )

                        launchIntent?.let {
                            context.startActivity(it)
                        }

                        delay(300)

                        selected = false
                    }
                },
            contentAlignment = Alignment.Center
        ) {

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
                modifier = Modifier.size(
                    (62f * scale).dp
                )
            )
        }

        // -----------------------------------------------------
        // SELECTED APP NAME
        // -----------------------------------------------------

        if (selected) {

            Box(
                modifier = Modifier
                    .offset(y = 57.dp)
                    .background(
                        color = Color(0xFF111722).copy(alpha = 0.94f),
                        shape = CircleShape
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    )
            ) {

                Text(
                    text = app.label,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun AllAppsScreen(
    onBack: () -> Unit
) {

    val context = LocalContext.current

    val apps = remember {
        getLaunchableApps(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080B12))
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // -------------------------------------------------
            // HEADER
            // -------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = 18.dp
                    )
            ) {

                Text(
                    text = "PCMT  •  ALL APPS",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .clickable {
                            onBack()
                        }
                        .padding(4.dp)
                )
            }

            // -------------------------------------------------
            // APP GRID
            // -------------------------------------------------

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                items(
                    apps.chunked(4)
                ) { rowApps ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 10.dp,
                                vertical = 8.dp
                            )
                    ) {

                        rowApps.forEach { app ->

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
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
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .background(
                                                Color(0xFF111722),
                                                CircleShape
                                            ),
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        AndroidView(
                                            factory = { ctx ->

                                                ImageView(ctx).apply {

                                                    scaleType =
                                                        ImageView.ScaleType
                                                            .FIT_CENTER

                                                    setImageDrawable(
                                                        app.icon
                                                    )

                                                    setPadding(
                                                        8,
                                                        8,
                                                        8,
                                                        8
                                                    )
                                                }
                                            },
                                            modifier =
                                                Modifier.size(56.dp)
                                        )
                                    }

                                    Text(
                                        text = app.label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .padding(
                                                top = 6.dp
                                            )
                                    )
                                }
                            }
                        }

                        // Keep the final row aligned to four columns.
                        repeat(4 - rowApps.size) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
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