package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.abs
import kotlin.random.Random

object ImageStorageDelegate {

    private const val TAG = "ImageStorageDelegate"

    /**
     * Converts any image ByteArray to a WebP file at maximum quality/lossless compression
     * on a background thread (Dispatchers.IO).
     */
    suspend fun saveWebpCoverFromBytes(
        context: Context,
        bytes: ByteArray,
        identifier: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
            val file = File(LocalStorageManager.getImagesDir(context), "cover_${sanitizeFileName(identifier)}_${System.currentTimeMillis()}.webp")
            
            FileOutputStream(file).use { out ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
                }
            }
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save WebP cover from bytes", e)
            null
        }
    }

    /**
     * Converts a Uri selected by user (Custom Cover Art) to a WebP file on Dispatchers.IO
     * at maximum lossless compression to avoid UI freezing.
     */
    suspend fun saveCustomWebpCoverFromUri(
        context: Context,
        imageUri: Uri,
        trackId: Long
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            val file = File(LocalStorageManager.getImagesDir(context), "custom_cover_${trackId}_${System.currentTimeMillis()}.webp")
            FileOutputStream(file).use { out ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
                }
            }
            bitmap.recycle()
            Log.d(TAG, "Custom cover converted to WebP successfully: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing custom cover image to WebP", e)
            null
        }
    }

    /**
     * Generates a seed-based colorful gradient cover art with artistic geometric patterns
     * and song initial letters when no cover art is embedded in the audio file.
     * Saved as a compressed WebP file on Dispatchers.IO.
     */
    suspend fun generateSeedWebpCover(
        context: Context,
        title: String,
        artist: String,
        seedInput: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val seed = abs((title + artist + seedInput).hashCode())
            val random = Random(seed)

            val width = 512
            val height = 512
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Generate two modern vibrant colors from seed
            val hue1 = random.nextFloat() * 360f
            val hue2 = (hue1 + 120f + random.nextFloat() * 60f) % 360f
            val color1 = Color.HSVToColor(floatArrayOf(hue1, 0.75f, 0.85f))
            val color2 = Color.HSVToColor(floatArrayOf(hue2, 0.85f, 0.45f))

            // Background Gradient
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    color1, color2, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Decorative Geometric Patterns from seed
            val shapePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            for (i in 0..4) {
                val shapeHue = (hue1 + random.nextFloat() * 180f) % 360f
                shapePaint.color = Color.HSVToColor(100, floatArrayOf(shapeHue, 0.6f, 0.9f))
                val cx = random.nextFloat() * width
                val cy = random.nextFloat() * height
                val radius = (100 + random.nextInt(180)).toFloat()
                canvas.drawCircle(cx, cy, radius, shapePaint)
            }

            // Draw initial letter in center
            val initial = title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "🎵"
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                textSize = 180f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                setShadowLayer(16f, 4f, 8f, Color.argb(120, 0, 0, 0))
            }

            // Vertically center text
            val textY = (height / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            canvas.drawText(initial, width / 2f, textY, textPaint)

            // Save to WebP in images/
            val file = File(LocalStorageManager.getImagesDir(context), "seed_cover_${sanitizeFileName(title)}_${seed}.webp")
            FileOutputStream(file).use { out ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, out)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
                }
            }
            bitmap.recycle()
            Log.d(TAG, "Seed cover generated and saved to WebP: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate seed webp cover", e)
            null
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_]"), "_").take(30)
    }
}
