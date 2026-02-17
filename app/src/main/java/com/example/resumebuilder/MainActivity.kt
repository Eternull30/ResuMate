package com.example.resumebuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.resumebuilder.ui.navigation.NavGraph
import com.example.resumebuilder.ui.theme.ResumeBuilderTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.isSystemInDarkTheme


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val darkTheme = isSystemInDarkTheme()
            val window = (this as ComponentActivity).window
            window.setBackgroundDrawableResource(
                if (darkTheme) R.color.window_background_dark
                else R.color.window_background_light
            )

            ResumeBuilderTheme(
                darkTheme = darkTheme
            ) {
                NavGraph()
            }
        }


    }
}


