package me.ethius

import me.ethius.client.Client
import me.ethius.server.Server

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        Client.main(args)
    } else {
        when (args[0].lowercase()) {
            "-client" -> Client.main(args)
            "-server" -> Server.main(Server.RunArgs(args[1].lowercase()))
            else -> Client.main(args)
        }

    }
}