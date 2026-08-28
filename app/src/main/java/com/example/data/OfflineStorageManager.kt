package com.example.data

import android.content.Context
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

object OfflineStorageManager {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(StudyChapter::class.java)

    private fun getFile(context: Context, chapterId: String): File {
        return File(context.filesDir, "chapter_$chapterId.json")
    }

    fun downloadChapter(context: Context, chapter: StudyChapter) {
        try {
            val json = adapter.toJson(chapter)
            getFile(context, chapter.id).writeText(json)
            Log.d("OfflineStorage", "Downloaded chapter ${chapter.id} to offline storage.")
        } catch (e: Exception) {
            Log.e("OfflineStorage", "Error downloading chapter ${chapter.id}", e)
        }
    }

    fun removeChapter(context: Context, chapterId: String) {
        val file = getFile(context, chapterId)
        if (file.exists()) {
            file.delete()
            Log.d("OfflineStorage", "Removed chapter $chapterId from offline storage.")
        }
    }

    fun isChapterDownloaded(context: Context, chapterId: String): Boolean {
        return getFile(context, chapterId).exists()
    }

    fun getDownloadedChapter(context: Context, chapterId: String): StudyChapter? {
        val file = getFile(context, chapterId)
        if (!file.exists()) return null
        return try {
            val json = file.readText()
            adapter.fromJson(json)
        } catch (e: Exception) {
            Log.e("OfflineStorage", "Error reading downloaded chapter $chapterId", e)
            null
        }
    }
}
