package me.ethius.shared

enum class Side {
    server, client;

    companion object {
        lateinit var currentSide:Side

        val _client get() = currentSide == client
        val _server get() = currentSide == server
    }
}