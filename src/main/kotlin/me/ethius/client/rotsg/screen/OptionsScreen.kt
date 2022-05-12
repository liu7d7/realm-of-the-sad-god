package me.ethius.client.rotsg.screen

import me.ethius.shared.bool

class OptionsScreen:Screen() {
    override val shouldCloseOnEsc:bool
        get() = true
}