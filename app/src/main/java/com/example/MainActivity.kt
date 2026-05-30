package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Explicitly lock the screen orientation to landscape (16:9 widescreen format)
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    
    // Set system decorations to fully edge-to-edge immersive game mode
    window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_FULLSCREEN
        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    )
    
    enableEdgeToEdge()
    
    val viewModel = DeadShotViewModel()
    
    setContent {
      MyApplicationTheme(dynamicColor = false) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          // Render high-fidelity landscape interface
          DeadShotGameApp(viewModel = viewModel)
        }
      }
    }
  }
}
