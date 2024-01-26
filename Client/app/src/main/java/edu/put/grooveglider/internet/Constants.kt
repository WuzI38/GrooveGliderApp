package edu.put.grooveglider.internet

object Constants {
    const val SERVER_ID = "192.168.100.113" // Server ip
    const val SERVER_PORT = 8888 // Server port

    const val MULTICAST_GROUP = "239.0.0.1" // Multicast group
    const val MULTICAST_PORT = 6000 // Multicast port

    const val TIMEOUT = 5000 // Time between connection attempts

    const val PACKET_SIZE = 422 // Bytes of received data (without index)
    const val INDEX_BYTES = 2 // Package index
    const val IGNORE_UNORDERED = false // Must be false if INDEX_BYTES <= 0

    const val RECEIVE_BUFFER_SIZE = 8192 // Buffer used for receiving messages from the server
    const val FILE_BUFFER_SIZE = 4096 // Buffer used for sending files
}