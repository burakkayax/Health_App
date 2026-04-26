package com.burak.healthapp.data.export

import android.content.Context
import android.net.Uri
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface HealthDataExportFileWriter {
    suspend fun writeJson(uri: Uri, json: String)
}

class AndroidHealthDataExportFileWriter(
    context: Context,
) : HealthDataExportFileWriter {
    private val applicationContext = context.applicationContext

    override suspend fun writeJson(uri: Uri, json: String) {
        withContext(Dispatchers.IO) {
            val bytes = json.toByteArray(StandardCharsets.UTF_8)
            val outputStream = applicationContext.contentResolver.openOutputStream(uri)
                ?: error("Export destination could not be opened.")
            outputStream.use { stream ->
                stream.write(bytes)
                stream.flush()
            }
        }
    }
}
