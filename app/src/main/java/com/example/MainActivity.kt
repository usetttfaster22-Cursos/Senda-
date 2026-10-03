package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.SendaApp
import com.example.ui.theme.SendaTheme
import com.example.viewmodel.SendaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SendaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SendaTheme {
                SendaApp(viewModel = viewModel)
            }
        }
    }
}
