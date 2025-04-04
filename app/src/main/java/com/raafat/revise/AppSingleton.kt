package com.raafat.revise

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raafat.revise.AyaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Singleton class that handles database and repository access
 * This replaces the dependency injection framework
 */
object AppSingleton {
    private var database: AppDatabase? = null
    private var ayaDao: AyaDao? = null

    @Volatile
    private var initialized = false

    fun initialize(applicationContext: Context) {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    database = Room.databaseBuilder(
                        applicationContext,
                        AppDatabase::class.java,
                        applicationContext.getString(R.string.revise_database)
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    ayaDao = database?.ayaDao()
                    initialized = true
                }
            }
        }
    }

    fun getAyaDao(): AyaDao {
        checkInitialized()
        return ayaDao!!
    }

    fun loadAyaList(context: Context): Flow<List<Aya>> = flow {
        checkInitialized()
        val ayaList = ayaDao?.getAyaList() ?: emptyList()

        if (ayaList.isEmpty()) {
            val json = loadJsonFromAssets(context, "uthmanic_hafs.json")
            val parsedAyaList = parseJson(json)
            ayaDao?.insertAyaList(parsedAyaList)
            emit(parsedAyaList)
        } else {
            emit(ayaList)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun loadJsonFromAssets(context: Context, fileName: String): String = withContext(Dispatchers.IO) {
        val inputStream = context.assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return@withContext bufferedReader.use { it.readText() }
    }

    private fun parseJson(json: String): List<Aya> {
        val gson = Gson()
        val ayaListType = object : TypeToken<List<Aya>>() {}.type
        return gson.fromJson(json, ayaListType)
    }

    private fun checkInitialized() {
        check(initialized) { "AppSingleton must be initialized before use" }
    }
}