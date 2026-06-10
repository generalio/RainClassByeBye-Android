package com.rainclass.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rainclass.app.navigation.AppNavHost
import com.rainclass.app.navigation.Home
import com.rainclass.app.navigation.Login
import com.rainclass.core.config.designsystem.theme.RainClassTheme
import com.rainclass.core.navigation3.RainRoute
import com.rainclass.core.network.cookie.PersistentCookieStore
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
  private val cookieStore: PersistentCookieStore by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val startDestination: RainRoute = if (cookieStore.hasCookies()) Home else Login

    setContent {
      RainClassTheme {
        AppNavHost(startDestination = startDestination)
      }
    }
  }
}
