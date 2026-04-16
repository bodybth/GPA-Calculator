package com.bodybth.gpacalculator.util

import android.content.Context
import com.bodybth.gpacalculator.model.Semester
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataManager {
    private const val PREF_NAME = "gpa_calculator_data"
    private const val KEY_SEMESTERS = "semesters"
    private val gson = Gson()

    fun save(context: Context, semesters: List<Semester>) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SEMESTERS, gson.toJson(semesters))
            .apply()
    }

    fun load(context: Context): MutableList<Semester> {
        val json = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SEMESTERS, null) ?: return mutableListOf()
        return try {
            val type = object : TypeToken<MutableList<Semester>>() {}.type
            gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }
}
