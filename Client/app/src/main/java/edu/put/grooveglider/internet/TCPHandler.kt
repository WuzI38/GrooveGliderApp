package edu.put.grooveglider.internet

import android.content.Context
import android.net.Uri
import android.util.Log
import edu.put.grooveglider.ui.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

object TCPHandler {
    private lateinit var socket: Socket

    private var isUploading = false

    // Connect using TCP
    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        var isConnected = false
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(Constants.SERVER_ID, Constants.SERVER_PORT), Constants.TIMEOUT)
            isConnected = TCPHandler::socket.isInitialized
        } catch (e: IOException) {
            e.printStackTrace()
        }
        isConnected
    }

    // Send message (TCP)
    suspend fun send(message: String) = withContext(Dispatchers.IO) {
        if (TCPHandler::socket.isInitialized && !isUploading) {
            try {
                val out = PrintWriter(socket.getOutputStream(), true)
                out.print(message)
                out.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Send MP3 file (TCP)
    suspend fun sendFile(context: Context, uri: Uri) = withContext(Dispatchers.IO) {
        if (TCPHandler::socket.isInitialized && !isUploading) {
            try {
                isUploading = true
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val outputStream = socket.getOutputStream()
                    val buffer = ByteArray(Constants.FILE_BUFFER_SIZE)
                    var bytesRead: Int

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.flush()
                }
                isUploading = false
            } catch (e: IOException) {
                isUploading = false
                e.printStackTrace()
            }
        }
    }

    // Handle received JSON (including joined messages)
    suspend fun receive(viewModel: SharedViewModel) = withContext(Dispatchers.IO) {
        try {
            if (TCPHandler::socket.isInitialized) {
                val reader = InputStreamReader(socket.getInputStream())
                val buffer = CharArray(Constants.RECEIVE_BUFFER_SIZE)
                var charsRead: Int

                fun processJsonArrays(jsonArrays: List<String>) {
                    val msg = jsonArrays[0] + "]"
                    val msg2 = "[" + jsonArrays[1]
                    if (msg2.last() == ']') {
                        viewModel.jsonDataServer.postValue(msg)
                        viewModel.jsonDataQueue.postValue(msg2)
                    } else {
                        val jsonArrays2 = msg2.split("]")
                        val msg3 = jsonArrays2[0] + "]"
                        val msg4 = jsonArrays2[1].toIntOrNull()
                        if (msg4 != null) {
                            viewModel.jsonDataServer.postValue(msg)
                            viewModel.jsonDataQueue.postValue(msg3)
                            viewModel.currentSong.postValue(msg4)
                        }
                    }
                }

                while (reader.read(buffer).also { charsRead = it } != -1) {
                    val message = String(buffer, 0, charsRead)
                    Log.d("JSON", message)
                    when {
                        message.contains("][") -> processJsonArrays(message.split("]["))
                        message.contains("{\"q\"") -> viewModel.jsonDataQueue.postValue(message)
                        message.contains("{\"s\"") -> viewModel.jsonDataServer.postValue(message)
                        message.contains("[") -> viewModel.jsonDataQueue.postValue(message)
                        else -> message.toIntOrNull()?.let { viewModel.currentSong.postValue(it) }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun close() {
        if (TCPHandler::socket.isInitialized) {
            socket.close()
        }
    }
}