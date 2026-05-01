package com.burak.healthapp.data.export

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import com.burak.healthapp.domain.export.HealthDataImportException
import com.burak.healthapp.domain.export.HealthDataImportLimits
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.export.validateImportFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

interface HealthDataImportFileReader {
    suspend fun readText(uri: Uri): String
}

class AndroidHealthDataImportFileReader(
    context: Context,
    private val maxImportBytes: Long = HealthDataImportLimits.MAX_IMPORT_FILE_BYTES,
) : HealthDataImportFileReader {
    private val applicationContext = context.applicationContext

    override suspend fun readText(uri: Uri): String = withContext(Dispatchers.IO) {
        rejectIfKnownSizeTooLarge(uri, maxImportBytes)
        val inputStream = applicationContext.contentResolver.openInputStream(uri)
            ?: error("Import file could not be opened.")
        inputStream.use { stream ->
            stream.readBytesBounded(maxImportBytes).toString(StandardCharsets.UTF_8)
        }
    }

    private fun rejectIfKnownSizeTooLarge(uri: Uri, maxBytes: Long) {
        applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            val length = descriptor.length
            if (length != AssetFileDescriptor.UNKNOWN_LENGTH) {
                validateImportFileSize(length, maxBytes)?.let { error ->
                    throw HealthDataImportException(error)
                }
            }
        }
    }
}

private fun java.io.InputStream.readBytesBounded(maxBytes: Long): ByteArray {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    val output = ByteArrayOutputStream()
    var totalBytes = 0L
    while (true) {
        val read = read(buffer)
        if (read == -1) break
        totalBytes += read
        if (totalBytes > maxBytes) {
            throw HealthDataImportException(ImportValidationError.FileTooLarge(maxBytes))
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
