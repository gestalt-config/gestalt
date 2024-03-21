package org.github.gestalt.config.kotlin

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.github.gestalt.config.annotations.Config
import org.github.gestalt.config.builder.GestaltBuilder
import org.github.gestalt.config.decoder.ProxyDecoderMode
import org.github.gestalt.config.exceptions.GestaltException
import org.github.gestalt.config.source.MapConfigSourceBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*


data class DBInfoData(
    var port: Int,
    var uri: String,
    var password: String,
    var connections: Int
)

data class DBInfoDataDefault(
    var port: Int = 0,
    var uri: String = "mysql:URI",
    var password: String = "password",
    @Config(defaultVal = "200") var connections: Int
)

data class DBInfoDataNullable(
    var port: Int?,
    var uri: String?,
    var password: String?,
    var connections: Int?
)

class MissingValuesConsistencyKtTest {

    @Test
    @Throws(GestaltException::class)
    fun testRecordResultsForIgnoreErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"
        configs["db.connections"] = "100"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoData>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoData\n" +
            " - level: ERROR, message: Data Class: DBInfoData, can not be constructed. Missing required members [uri], on path: db"

    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordResultsForMissingFail() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"
        configs["db.connections"] = "100"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoData>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoData\n" +
            " - level: ERROR, message: Data Class: DBInfoData, can not be constructed. Missing required members [uri], on path: db"

    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordResultsForMissingOkNullFailMissingValuesAsErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"
        configs["db.connections"] = "100"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoData>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoData\n" +
        " - level: ERROR, message: Data Class: DBInfoData, can not be constructed. Missing required members [uri], on path: db"
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordResultsForMissingFailMissingValuesAsNotErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"
        configs["db.connections"] = "100"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoData>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoData\n" +
            " - level: ERROR, message: Data Class: DBInfoData, can not be constructed. Missing required members [uri], on path: db"
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordDefaultResultsForIgnoreErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build()

        gestalt.loadConfigs()

        val dbInfo = gestalt.getConfig<DBInfoDataDefault>("db")

        dbInfo.uri shouldBe "mysql:URI"
        dbInfo.password shouldBe "test"
        dbInfo.port shouldBe 3306
        dbInfo.connections shouldBe 200
    }


    @Test
    @Throws(GestaltException::class)
    fun testRecordDefaultResultsForMissingFail() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoDataDefault>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoDataDefault\n" +
            " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on path: db.uri, with node: " +
            "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoDataDefault\n" +
            " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on path: db.connections, " +
            "with node: MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoDataDefault"

    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordDefaultResultsForMissingOkNullFailMissingValuesAsErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build()

        gestalt.loadConfigs()

        val dbInfo = gestalt.getConfig<DBInfoDataDefault>("db")

        dbInfo.uri shouldBe "mysql:URI"
        dbInfo.password shouldBe "test"
        dbInfo.port shouldBe 3306
        dbInfo.connections shouldBe 200
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordDefaultResultsForMissingFailMissingValuesAsNotErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoDataDefault>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoDataDefault\n" +
            " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on path: db.uri, with node: " +
            "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoDataDefault\n" +
            " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on path: db.connections, " +
            "with node: MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, with class: DBInfoDataDefault"
    }


    @Test
    @Throws(GestaltException::class)
    fun testRecordNullableResultsForIgnoreErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build()

        gestalt.loadConfigs()

        val dbInfo = gestalt.getConfig<DBInfoDataNullable>("db")

        dbInfo.uri shouldBe null
        dbInfo.password shouldBe "test"
        dbInfo.port shouldBe 3306
        dbInfo.connections shouldBe null
    }


    @Test
    @Throws(GestaltException::class)
    fun testRecordNullableResultsForMissingFail() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoDataNullable>("db") }

        ex.message shouldContain "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoDataNullable"
        ex.message shouldContain "level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on " +
            "path: db.uri, with node: MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, " +
            "with class: DBInfoDataNullable"
        ex.message shouldContain "level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on " +
            "path: db.connections, with node: MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}}, " +
            "with class: DBInfoDataNullable"


    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordNullableResultsForMissingOkNullFailMissingValuesAsErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .build()

        gestalt.loadConfigs()

        val dbInfo = gestalt.getConfig<DBInfoDataNullable>("db")

        dbInfo.uri shouldBe null
        dbInfo.password shouldBe "test"
        dbInfo.port shouldBe 3306
        dbInfo.connections shouldBe null
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordNullableResultsForMissingFailMissingValuesAsNotErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"
        configs["db.connections"] = "100"

        val builder = GestaltBuilder()
        val gestalt = builder
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .build()

        gestalt.loadConfigs()

        val ex = Assertions.assertThrows(GestaltException::class.java) { gestalt.getConfig<DBInfoDataNullable>("db") }

        ex.message shouldBe "Failed getting config path: db, for class: org.github.gestalt.config.kotlin.DBInfoDataNullable\n" +
            " - level: MISSING_OPTIONAL_VALUE, message: Missing Optional Value while decoding DataClass on path: db.uri, with node: " +
            "MapNode{password=LeafNode{value='*****'}, port=LeafNode{value='3306'}, connections=LeafNode{value='100'}}, " +
            "with class: DBInfoDataNullable"
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordOptionalForMissingOkNullFail() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val gestalt = GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build()

        gestalt.loadConfigs()

        val dbInfo: DBInfoData? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo)

        val dbInfo2: DBInfoDataDefault? = gestalt.getConfig("db")
        Assertions.assertNotNull(dbInfo2)

        dbInfo2!!.uri shouldBe "mysql:URI"
        dbInfo2.password shouldBe "test"
        dbInfo2.port shouldBe 3306
        dbInfo2.connections shouldBe 200

        val dbInfo3: DBInfoDataNullable? = gestalt.getConfig("db")
        Assertions.assertNotNull(dbInfo3)
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordOptionalForMissingFail() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val gestalt = GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build()

        gestalt.loadConfigs()

        val dbInfo: DBInfoDataDefault? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo)

        val dbInfo2: DBInfoDataNullable? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo2)

        val dbInfo3: DBInfoData? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo3)
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordOptionalForMissingOkNullFailMissingValuesAsErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val gestalt = GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(true)
            .setTreatMissingDiscretionaryValuesAsErrors(false)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build()

        gestalt.loadConfigs()

        val dbInfo: DBInfoDataDefault? = gestalt.getConfig("db")
        Assertions.assertNotNull(dbInfo)

        dbInfo!!.uri shouldBe "mysql:URI"
        dbInfo.password shouldBe "test"
        dbInfo.port shouldBe 3306

        val dbInfo2: DBInfoDataNullable? = gestalt.getConfig("db")
        Assertions.assertNotNull(dbInfo2)

        val dbInfo3: DBInfoData? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo3)
    }

    @Test
    @Throws(GestaltException::class)
    fun testRecordOptionalsForMissingFailMissingValuesAsNotErrors() {
        val configs: MutableMap<String, String> = HashMap()
        configs["db.password"] = "test"
        configs["db.port"] = "3306"

        val gestalt = GestaltBuilder()
            .addSource(MapConfigSourceBuilder.builder().setCustomConfig(configs).build())
            .setTreatMissingValuesAsErrors(false)
            .setTreatMissingDiscretionaryValuesAsErrors(true)
            .setProxyDecoderMode(ProxyDecoderMode.PASSTHROUGH)
            .build()

        gestalt.loadConfigs()

        val dbInfo: DBInfoData? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo)

        val dbInfo2: DBInfoDataDefault? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo2)

        val dbInfo3: DBInfoDataNullable? = gestalt.getConfig("db")
        Assertions.assertNull(dbInfo3)

    }
}
