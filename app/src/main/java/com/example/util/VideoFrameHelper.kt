package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoFrameHelper {
    suspend fun getClipFrames(
        context: Context,
        uriString: String?,
        clipDurationMs: Long,
        frameCount: Int = 8
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        if (uriString.isNullOrBlank()) return@withContext emptyList()
        val bitmaps = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            val uri = Uri.parse(uriString)
            retriever.setDataSource(context, uri)
            val durationUs = clipDurationMs * 1000L
            val intervalUs = if (frameCount > 1) durationUs / (frameCount - 1) else durationUs
            for (i in 0 until frameCount) {
                val timeUs = (i * intervalUs).coerceAtMost(durationUs)
                val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.getFrameAtTime(timeUs)
                if (frame != null) {
                    val scaled = Bitmap.createScaledBitmap(frame, 100, 130, true)
                    bitmaps.add(scaled)
                }
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
        bitmaps
    }
}
