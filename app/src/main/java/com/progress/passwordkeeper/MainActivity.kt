package com.progress.passwordkeeper

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.progress.passwordkeeper.ui.PasswordListScreen
import com.progress.passwordkeeper.ui.PinScreen
import com.progress.passwordkeeper.vievmodel.PasswordViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: PasswordViewModel = viewModel()
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: PasswordViewModel) {
    var isAuthenticated by remember { mutableStateOf(false) }
    val correctPin = "12345"

    var shakeOffset by remember { mutableFloatStateOf(0f) }
    var pinError by remember { mutableStateOf(false) }
    var animTrigger by remember { mutableIntStateOf(0) } // триггер для анимации

    val anim = remember { androidx.compose.animation.core.Animatable(0f) }

    if (!isAuthenticated) {
        PinScreen(
            pinLength = correctPin.length,
            onPinComplete = { enteredPin ->
                if (enteredPin == correctPin || enteredPin == "fingerprint") {
                    isAuthenticated = true
                } else {
                    pinError = true
                    animTrigger++  // запускаем shake
                }
            },
            shakeOffset = shakeOffset
        )

        // Shake анимация
        LaunchedEffect(animTrigger) {
            if (pinError) {
                anim.snapTo(0f)
                val amplitude = 12f
                val frequency = 10f
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
                ) {
                    // 'this.value' — текущее значение анимации
                    shakeOffset = (kotlin.math.sin(this.value * frequency * Math.PI * 2) * amplitude).toFloat()
                }
                shakeOffset = 0f
                pinError = false
            }
        }
    } else {
        PasswordListScreen(viewModel)
    }
}

