package org.github.gestalt.config.kotlin.test.classes

import org.github.gestalt.config.annotations.Config

data class DBInfo(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = "password"
)

data class DBInfoRequired(
    var port: Int,
    var uri: String,
    var password: String
)

data class DBInfoNoDefault(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String
)

data class DBInfoNullable(
    var port: Int?,
    var uri: String?,
    var password: String?
)

data class DBInfoNoDefaultOptional(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String? = null
)

data class DBInfoAnnotation(
    @Config(path = "channel", defaultVal = "1234") var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = "password"
)

data class DBInfoAnnotationLong(
    @Config(path = "channel.port", defaultVal = "1234") var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = "password"
)
