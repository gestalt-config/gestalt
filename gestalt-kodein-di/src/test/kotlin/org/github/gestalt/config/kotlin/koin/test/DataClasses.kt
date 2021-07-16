package org.github.gestalt.config.kotlin.koin.test

class DBInfoPOJO(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = ""
)

data class DBInfo(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = "default"
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

class DBService1(private val dbInfo: DBInfo) {
    fun test(): Boolean = dbInfo.password.isNotBlank() && dbInfo.port != 0 && dbInfo.uri.isNotBlank()
    fun getPort(): Int = dbInfo.port
    fun getUri(): String = dbInfo.uri
    fun getPassword(): String = dbInfo.password
}

class DBService2(private val dbInfo: DBInfoPOJO) {
    fun test(): Boolean = dbInfo.password.isNotBlank() && dbInfo.port != 0 && dbInfo.uri.isNotBlank()
    fun getPort(): Int = dbInfo.port
    fun getUri(): String = dbInfo.uri
    fun getPassword(): String = dbInfo.password
}

class DBService3(private val dbInfo: DBInfoNoDefault) {
    fun test(): Boolean = dbInfo.password.isNotBlank() && dbInfo.port != 0 && dbInfo.uri.isNotBlank()
    fun getPort(): Int = dbInfo.port
    fun getUri(): String = dbInfo.uri
    fun getPassword(): String = dbInfo.password
}

class DBService4(private val dbInfo: DBInfoNoDefaultOptional) {
    fun test(): Boolean = !dbInfo.password.isNullOrBlank() && dbInfo.port != 0 && dbInfo.uri.isNotBlank()
    fun getPort(): Int = dbInfo.port
    fun getUri(): String = dbInfo.uri
    fun getPassword(): String = dbInfo.password ?: ""
}
