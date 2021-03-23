package com.example.simplecleanarchitecture.core.lib.const

import java.util.regex.Pattern

object Patterns {

    val EMAIL_ADDRESS = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    val PASSWORD = Pattern.compile("^.{8,64}\$")
    val ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9 ]{0,200}\$")

}