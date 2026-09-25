package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.data.database.WaseemDatabase
import com.example.data.repository.ClinicRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.WaseemMedicalTheme
import com.example.ui.viewmodel.ClinicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReporter.install(this)
        enableEdgeToEdge()

        val previousCrash = CrashReporter.readAndClear(this)
        if (previousCrash != null) {
            showStartupError(
                "تم تسجيل تعطل سابق للتطبيق. هذه التفاصيل تساعد على تحديد السبب الحقيقي.",
                previousCrash
            )
            return
        }

        // عرض شاشة بسيطة أولًا؛ لا نُنشئ قاعدة البيانات أو الـViewModel داخل onCreate
        // حتى لا يحدث استثناء أثناء الإقلاع قبل أن يتم تركيب واجهة Compose.
        setContent {
            WaseemMedicalTheme {
                StartupLoadingScreen()
            }
        }

        lifecycleScope.launch {
            try {
                // فتح قاعدة البيانات والتحقق من الجداول وبيانات البداية خارج خيط الواجهة.
                // يتم تنفيذ التهيئة مرة واحدة فقط هنا لمنع سباق SQLite عند أول تشغيل.
                val database = withContext(Dispatchers.IO) {
                    WaseemDatabase.getDatabase(this@MainActivity).also {
                        WaseemDatabase.ensureEssentialData(it.clinicDao())
                    }
                }

                val repository = ClinicRepository(database.clinicDao(), database)
                val viewModel = ClinicViewModel(repository)

                // لا نعرض الشاشة الرئيسية إلا بعد نجاح تهيئة قاعدة البيانات كاملة.
                setContent {
                    WaseemMedicalTheme {
                        MainScreen(viewModel = viewModel)
                    }
                }
            } catch (error: Throwable) {
                showStartupError(
                    "تعذر تشغيل النظام أثناء التهيئة. أرسل تفاصيل الخطأ للمطور.",
                    error.stackTraceToString()
                )
            }
        }
    }

    private fun showStartupError(summary: String, details: String) {
        setContent {
            WaseemMedicalTheme {
                StartupErrorScreen(summary = summary, details = details)
            }
        }
    }
}

@Composable
private fun StartupLoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "نظام وسيم الطبي PRO",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        Text(
            text = "جاري تجهيز قاعدة البيانات وتشغيل النظام...",
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun StartupErrorScreen(summary: String, details: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "نظام وسيم الطبي PRO",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = summary,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = details,
            modifier = Modifier.padding(top = 16.dp),
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
        TextButton(onClick = {}) {
            Text("تم")
        }
    }
}
