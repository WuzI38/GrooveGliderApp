package edu.put.grooveglider.internet

import android.content.Context
import android.net.wifi.WifiManager
import edu.put.grooveglider.music.MusicPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.util.Arrays

object UDPHandler {
    private lateinit var socket: DatagramSocket
    private lateinit var multicastSocket: MulticastSocket
    private lateinit var wifiLock: WifiManager.WifiLock
    private lateinit var wifiManager: WifiManager
    private lateinit var multiLock: WifiManager.MulticastLock
    private lateinit var group: InetAddress

    // Connect and ad to multicast group
    suspend fun connectMulticast(context: Context) = withContext(Dispatchers.IO) {
        try {
            // Log.d("Multicast", "Multicast connection ready")
            // Multicast lock
            wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "WifiLock")
            multiLock = wifiManager.createMulticastLock("MultiLock")
            multiLock.setReferenceCounted(true)

            group = InetAddress.getByName(Constants.MULTICAST_GROUP)
            multicastSocket = MulticastSocket(Constants.MULTICAST_PORT)
            multicastSocket.joinGroup(group)

            wifiLock.acquire()
            multiLock.acquire() // If release is not called (cannot be called in onPause and onDestroy) this throws an error
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Connect using UDP
    @Suppress("unused")
    suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            socket = DatagramSocket()
            socket.connect(InetSocketAddress(Constants.SERVER_ID, Constants.SERVER_PORT))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Receive multicast messages
    suspend fun receiveMulticast(ignoreUnordered: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val packet = DatagramPacket(ByteArray(Constants.PACKET_SIZE), Constants.PACKET_SIZE)

            var lastIndex = 0

            while (true) {
                multicastSocket.receive(packet)

                val receivedData = packet.data
                val musicData = Arrays.copyOfRange(receivedData, Constants.INDEX_BYTES, Constants.PACKET_SIZE)

                if(ignoreUnordered) {
                    val indexBytes = receivedData.copyOfRange(0, Constants.INDEX_BYTES)
                    val index = indexBytes.fold(0) { acc, b -> (acc shl 8) or (b.toInt() and 0xFF) }

                    if (index > lastIndex || (lastIndex != 0 && index == 0)) {
                        MusicPlayer.play(musicData)
                        lastIndex = index
                    }
                }
                else {
                    MusicPlayer.play(musicData)
                }
                // Log.d("MUSIC", receivedData.contentToString()) // print byte array
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Receive UDP messages
    @Suppress("unused")
    suspend fun receive() = withContext(Dispatchers.IO) {
        try {
            val packet = DatagramPacket(ByteArray(Constants.PACKET_SIZE), Constants.PACKET_SIZE)
            while (true) {
                socket.receive(packet)

                val receivedData = packet.data

                MusicPlayer.play(receivedData)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Request song (by title)
    @Suppress("unused")
    suspend fun requestSong(songName: String) = withContext(Dispatchers.IO) {
        try {
            val buffer = songName.toByteArray()
            val packet = DatagramPacket(buffer, buffer.size,
                InetAddress.getByName(Constants.SERVER_ID), Constants.SERVER_PORT)
            socket.send(packet)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Close UDP / Multicast connection
    fun close() {
        if(UDPHandler::multicastSocket.isInitialized) {
            multicastSocket.leaveGroup(group)
            multicastSocket.close()
            multiLock.release()
            wifiLock.release()
        }
        if (UDPHandler::socket.isInitialized) {
            socket.close()
        }
    }
}