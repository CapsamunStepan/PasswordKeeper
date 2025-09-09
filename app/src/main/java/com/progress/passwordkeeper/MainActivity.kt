package com.progress.passwordkeeper

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.progress.passwordkeeper.ui.PasswordListScreen
import com.progress.passwordkeeper.ui.PinScreen
import com.progress.passwordkeeper.vievmodel.PasswordViewModel
import java.security.MessageDigest
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: PasswordViewModel = viewModel()
                MainScreen(viewModel, this)
            }
        }
    }
}

fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun getSecurePrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

@Composable
fun MainScreen(viewModel: PasswordViewModel, context: Context) {
    var isAuthenticated by remember { mutableStateOf(false) }

    // prefs
    val prefs = remember { getSecurePrefs(context) }
    // A way to drop pin, I can use it to change PIN, I need to input old PIN before
//        prefs.edit().remove("pin_hash").apply()
    val savedHash = prefs.getString("pin_hash", null)

    var shakeOffset by remember { mutableFloatStateOf(0f) }
    var pinError by remember { mutableStateOf(false) }
    var animTrigger by remember { mutableIntStateOf(0) }

    val anim = remember { androidx.compose.animation.core.Animatable(0f) }

    var firstPin by remember { mutableStateOf<String?>(null) }
    var message by remember {
        mutableStateOf(
            if (savedHash == null) "Установите PIN"
            else "Введите PIN"
        )
    }

    if (!isAuthenticated) {
        PinScreen(
            pinLength = 6,
            message = message,
            onPinComplete = { enteredPin ->
                if (savedHash == null) {
                    if (firstPin == null) {
                        // первый ввод
                        firstPin = enteredPin
                        message = "Повторите PIN"
                    } else {
                        // проверка подтверждения
                        if (firstPin == enteredPin) {
                            prefs.edit().putString("pin_hash", hashPin(enteredPin)).apply()
                            // splash message
                            //                            message = "PIN успешно установлен"
                            isAuthenticated = true
                        } else {
                            firstPin = null
                            // splash message
                            // message = "PIN не совпадает, попробуйте снова"
                            message = "Установите PIN"
                            pinError = true
                            animTrigger++
                        }
                    }
                } else {
                    // обычная аутентификация
                    if (hashPin(enteredPin) == savedHash || enteredPin == "fingerprint") {
                        isAuthenticated = true
                    } else {
                        pinError = true
                        animTrigger++
                    }
                }
            },
            shakeOffset = shakeOffset,
            autoBiometric = savedHash != null
        )


        // Shake анимация
        LaunchedEffect(animTrigger) {
            if (pinError) {
                anim.snapTo(0f)
                val amplitude = 30f         // начальная сила
                val frequency = 60f          // частота колебаний
                val damping = 5f            // коэффициент затухания

                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 600)
                ) {
                    // время в диапазоне [0,1]
                    val progress = this.value
                    // затухающая синусоида
                    shakeOffset = (kotlin.math.sin(progress * frequency * Math.PI * 2) *
                            amplitude * kotlin.math.exp(-damping * progress)).toFloat()
                }

                shakeOffset = 0f
                pinError = false
            }
        }

    } else {
        PasswordListScreen(viewModel)
    }
}