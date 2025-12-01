package com.tripandevent.sanbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tripandevent.sanbot.ui.navigation.SanbotNavGraph
import com.tripandevent.sanbot.ui.theme.SanbotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanbotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SanbotNavGraph()
                }
            }
        }
    }
}
