package com.burak.healthapp.data.export

import android.content.Context
import android.net.Uri
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface HealthDataImportFileReader {
    suspend fun readText(uri: Uri): String
}

class AndroidHealthDataImportFileReader(
    context: Context,
) : HealthDataImportFileReader {
    private val applicationContext = context.applicationContext

    override suspend fun readText(uri: Uri): String {
        return withContext(Dispatchers.IO) {
            val inputStream = applicationContext.contentResolver.openInputStream(uri)
                ?: error("Import file could not be opened.")
            inputStream.use { stream ->
                stream.readBytes().toString(StandardCharsets.UTF_8)
            }
        }
    }
}
