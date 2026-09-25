package com.example

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Captures fatal startup/runtime exceptions so the next launch can show the real cause. */
object CrashReporter {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching {
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(Date())
                File(appContext.filesDir, FILE_NAME).writeText(
                    "WASEEM MEDICAL PRO crash\n" +
                        "Time: $stamp\n" +
                        "Thread: ${thread.name}\n\n" +
                        throwable.stackTraceToString(),
                    Charsets.UTF_8
                )
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun readAndClear(context: Context): String? {
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        if (!file.exists()) return null
        return runCatching {
            val text = file.readText(Charsets.UTF_8)
            file.delete()
            text.take(12000)
        }.getOrNull()
    }
}
