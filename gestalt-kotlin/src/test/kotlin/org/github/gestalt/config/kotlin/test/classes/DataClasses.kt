package org.github.gestalt.config.kotlin.test.classes

data class DBInfo(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = "password"
)

data class DBInfoNoDefault(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String
)

data class DBInfoNoDefaultOptional(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String? = null
)

