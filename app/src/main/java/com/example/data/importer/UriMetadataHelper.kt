package com.example.data.importer

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object UriMetadataHelper {

    fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment
    }

    fun getFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    return cursor.getLong(sizeIndex)
                }
            }
        }
        return 0L
    }

    fun cleanFileName(fileName: String): String {
        val nameWithoutExt = fileName.substringBeforeLast(".")
        return nameWithoutExt.replace("_", " ").replace("-", " ").trim()
    }

    fun isAudioFile(fileName: String, context: Context, uri: Uri): Boolean {
        val lower = fileName.lowercase()
        val validExts = listOf(".mp3", ".m4a", ".aac", ".flac", ".wav", ".ogg", ".opus", ".3gp")
        if (validExts.any { lower.endsWith(it) }) return true

        val mime = context.contentResolver.getType(uri)
        return mime != null && (mime.startsWith("audio/") || mime == "application/ogg")
    }

    fun getFolderNameFromUri(uri: Uri): String? {
        val path = uri.path ?: return null
        val segments = path.split("/")
        return if (segments.size > 2) segments[segments.size - 2] else null
    }
}
