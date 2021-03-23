package com.example.simplecleanarchitecture

import com.github.terrakok.cicerone.Command
import com.github.terrakok.cicerone.Router

class MainRouter : Router() {

    fun execute(vararg commands: Command) = executeCommands(*commands)

}