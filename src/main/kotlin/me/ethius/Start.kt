package me.ethius

import me.ethius.client.Client
import me.ethius.server.Server

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        Client.main(Client.RunArgs(false))
    } else {
        when (args[0].lowercase()) {
            "-client" -> Client.main(Client.RunArgs(args.safeGet(1).equals("testing", true)))
            "-server" -> {
                Server.main(Server.RunArgs(args[1].lowercase(), args.safeGet(2).equals("testing", true)))
            }
            else -> Client.main(Client.RunArgs(false))
        }
    }
}

fun Array<String>.safeGet(index: Int): String {
    return if (index < this.size) this[index] else ""
}