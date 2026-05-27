package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.MainAppNavigation
import com.example.ui.theme.GadgetValuerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Support modern safe status/navigation system bars drawing
        enableEdgeToEdge()

        // Initialize local SQLite Room database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = AppRepository(
            valuationDao = database.valuationDao(),
            comparisonDao = database.comparisonDao(),
            vendorDao = database.vendorDao()
        )

        // Instantiate core viewmodel coordinating all application sub-screens
        val viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, applicationContext)
        )[MainViewModel::class.java]

        setContent {
            GadgetValuerTheme {
                MainAppNavigation(viewModel)
            }
        }
    }
}
