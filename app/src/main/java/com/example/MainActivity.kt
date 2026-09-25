package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.data.database.WaseemDatabase
import com.example.data.repository.ClinicRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.WaseemMedicalTheme
import com.example.ui.viewmodel.ClinicViewModel
import com.example.ui.viewmodel.ClinicViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy {
        WaseemDatabase.getDatabase(this, lifecycleScope)
    }

    private val repository by lazy {
        ClinicRepository(database.clinicDao(), database)
    }

    private val clinicViewModel: ClinicViewModel by viewModels {
        ClinicViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaseemMedicalTheme {
                MainScreen(viewModel = clinicViewModel)
            }
        }
    }
}
