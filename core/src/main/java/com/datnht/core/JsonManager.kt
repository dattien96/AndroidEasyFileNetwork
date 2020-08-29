package com.datnht.core

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonManager private constructor(){
    companion object {
        val INSTANCE = JsonManager()
    }

    val gson = Gson()

    fun <T> objectToJson(objectInput: T) = gson.toJson(objectInput)

    inline fun <reified T> jsonToObject(json: String) = gson.fromJson<T>(json, T::class.java)

    inline fun <reified T> listToJson(listInput: List<T>) : String {
        val listType =
            object : TypeToken<ArrayList<T>>() {}.type

        return gson.toJson(listInput, listType)
    }

    inline fun <reified T> jsonToList(json: String) : List<T> {
        val listType =
            object : TypeToken<ArrayList<T>>() {}.type

        return gson.fromJson(json, listType)
    }
}