package bw.alphadirect.pratdroid.ui

import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bw.alphadirect.pratdroid.R
import kotlinx.coroutines.delay
import java.util.Locale

private const val TOTAL_MS = 8000
private val SkyTop = Color(0xFFAEE0FF)
private val SkyBottom = Color(0xFF3FA3DC)
private val InkShadow = Color(0x66012033)

/**
 * Cinematic 8-second intro on a sky-blue field: portrait fades in, a handwritten
 * welcome, the white RAPTOR wordmark wipes in, then a claw is drawn beneath it.
 * Angry roar on launch + Australian-male voice. Tap anywhere to skip.
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val t = remember { Animatable(0f) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val roar = MediaPlayer.create(context, R.raw.raptor_roar)
        roar?.start()
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) ttsReady = true
        }
        ttsEngine = engine
        onDispose {
            roar?.release()
            engine.stop()
            engine.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(durationMillis = TOTAL_MS, easing = LinearEasing))
        onDone()
    }

    LaunchedEffect(ttsReady) {
        if (ttsReady) {
            ttsEngine?.let { e ->
                e.language = Locale("en", "AU")
                e.voices
                    ?.firstOrNull { it.locale.country == "AU" && it.name.contains("male", true) }
                    ?.let { e.voice = it }
                delay(2200)
                e.speak("Welcome to the C F O's Raptor", TextToSpeech.QUEUE_FLUSH, null, "welcome")
            }
        }
    }

    fun seg(start: Float, end: Float): Float =
        ((t.value - start) / (end - start)).coerceIn(0f, 1f)

    val photoP = seg(0.00f, 0.16f)
    val textP = seg(0.14f, 0.30f)
    val wordP = seg(0.30f, 0.66f)
    val clawP = seg(0.62f, 0.92f)

    val handwritten = FontFamily.Cursive
    val shadowStyle = TextStyle(shadow = Shadow(InkShadow, Offset(2f, 3f), 6f))

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(SkyTop, SkyBottom)))
            .pointerInput(Unit) { detectTapGestures(onTap = { onDone() }) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.splash_photo),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .scale(0.7f + 0.3f * photoP)
                    .alpha(photoP)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(20.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textP)
            ) {
                Text(
                    "Welcome to",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontFamily = handwritten,
                    style = shadowStyle
                )
                Text(
                    "AI CFO's Raptor",
                    color = Color.White,
                    fontSize = 38.sp,
                    fontFamily = handwritten,
                    fontWeight = FontWeight.Bold,
                    style = shadowStyle
                )
            }
            Spacer(Modifier.height(28.dp))
            RaptorWordmark(wordP)
            Spacer(Modifier.height(10.dp))
            RaptorClaw(clawP)
        }

        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.25f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(t.value)
                        .height(3.dp)
                        .background(Color.White)
                )
            }
        }
    }
}

/** White RAPTOR wordmark that wipes in left-to-right. */
@Composable
private fun RaptorWordmark(progress: Float) {
    Image(
        painter = painterResource(R.drawable.raptor_wordmark),
        contentDescription = "RAPTOR",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .width(236.dp)
            .height(82.dp)
            .drawWithContent {
                clipRect(right = size.width * progress) {
                    this@drawWithContent.drawContent()
                }
            }
    )
}

/** Three white claw-slashes drawn one after another, beneath the wordmark. */
@Composable
private fun RaptorClaw(progress: Float) {
    Canvas(modifier = Modifier.size(width = 150.dp, height = 60.dp)) {
        val w = size.width
        val h = size.height
        for (i in 0..2) {
            val local = ((progress * 3f) - i).coerceIn(0f, 1f)
            if (local <= 0f) continue
            val dx = i * w * 0.22f
            val path = Path().apply {
                moveTo(w * 0.20f + dx, h * 0.05f)
                cubicTo(
                    w * 0.30f + dx, h * 0.35f,
                    w * 0.30f + dx, h * 0.66f,
                    w * 0.18f + dx, h * 0.95f
                )
            }
            val pm = PathMeasure().apply { setPath(path, false) }
            val dst = Path()
            pm.getSegment(0f, pm.length * local, dst, true)
            drawPath(dst, color = Color.White.copy(alpha = 0.30f), style = Stroke(width = 16f, cap = StrokeCap.Round))
            drawPath(dst, color = Color.White, style = Stroke(width = 6f, cap = StrokeCap.Round))
        }
    }
}
