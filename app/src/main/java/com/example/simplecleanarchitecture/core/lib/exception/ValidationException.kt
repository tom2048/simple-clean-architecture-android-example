package com.example.simplecleanarchitecture.core.lib.exception

class ValidationException(
    val validationMessages: List<Pair<String, String>>,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)