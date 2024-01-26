package edu.put.grooveglider.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import edu.put.grooveglider.music.Song
import java.io.BufferedReader
import java.io.InputStreamReader

class Utils {
    companion object {
        @Suppress("unused")
        fun songListFromJson(filename: String, context: Context): MutableList<Song> {
            val jsonFileString = context.assets?.open(filename)?.bufferedReader().use { it?.readText() }

            // Use gson to create a list of songs and pass them to adapter
            val gson = Gson()

            // Read every single entry from the list stored in json file
            val listType = object : TypeToken<List<Song>>() {}.type
            return gson.fromJson(jsonFileString, listType)
        }

        // Create a list of songs from a message (TCP)
        fun songListFromJsonMessage(json: String): MutableList<Song> {
            val gson = Gson()
            val jsonArray = gson.fromJson(json, JsonArray::class.java)
            val list = mutableListOf<Song>()
            for (jsonElement in jsonArray) {
                val jsonObject = jsonElement.asJsonObject
                val title = jsonObject.get("title").asString
                val artist = jsonObject.get("artist").asString
                val filename = jsonObject.get("s")?.asString ?: jsonObject.get("q")?.asString
                filename?.let { Song(title, artist, it) }?.let { list.add(it) }
            }
            return list
        }

        // Create a list of songs from a file
        @Suppress("unused")
        fun readPMCFromFile(context: Context, filename: String): ByteArray {
            val file = context.assets?.open(filename)
            val reader = BufferedReader(InputStreamReader(file))
            val numbers = reader.readText().split(",")
            val byteArray = ByteArray(numbers.size)

            for (i in numbers.indices) {
                var number = numbers[i].trim().toInt()
                if (number > 127) {
                    number -= 256
                }
                byteArray[i] = number.toByte()
            }

            return byteArray
        }
    }
}