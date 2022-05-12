package me.ethius.client.network

import me.ethius.shared.int
import me.ethius.shared.string
import java.net.Socket

class ServerView(addr:string, port:int) {

    val socket = Socket(addr, port)
    val to = socket.outputStream.writer().buffered()
    val from = socket.inputStream.reader().buffered()
    var closed = false
        private set

    fun close() {
        socket.close()
        closed = true
    }

    init {
        socket.tcpNoDelay = true
    }

}