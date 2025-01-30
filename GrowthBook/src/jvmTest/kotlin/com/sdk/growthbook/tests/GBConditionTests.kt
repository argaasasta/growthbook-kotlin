package com.sdk.growthbook.tests

import com.sdk.growthbook.utils.GBCondition
import com.sdk.growthbook.evaluators.GBAttributeType
import com.sdk.growthbook.evaluators.GBConditionEvaluator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.intellij.lang.annotations.Language
import kotlin.test.*

class GBConditionTests {

    private lateinit var evalConditions: JsonArray

    @BeforeTest
    fun setUp() {
        evalConditions = GBTestHelper.getEvalConditionData()
    }

    @Test
    fun testConditions() {
        val failedScenarios: ArrayList<String> = ArrayList()
        val passedScenarios: ArrayList<String> = ArrayList()
        for (item in evalConditions) {
            if (item is JsonArray) {
                val evaluator = GBConditionEvaluator()
                val item2JsonObject = item[2] as? JsonObject
                val attributes = if (item2JsonObject == null) {
                    emptyMap<String, JsonElement>()
                } else {
                    HashMap(item2JsonObject)
                }

                val result: Boolean = if (item.size > 4) {
                    evaluator.evalCondition(attributes, item[1], item[4].jsonObject)
                } else {
                    evaluator.evalCondition(attributes, item[1], null)
                }

                val status =
                    item[0].toString() + "\nExpected Result - " + item[3] +
                        "\nActual result - " + result + "\n\n"

                if (item[3].toString() == result.toString()) {
                    passedScenarios.add(status)
                } else {
                    failedScenarios.add(status)
                }
            }
        }

        print("\nTOTAL TESTS - " + evalConditions.size)
        print("\nPassed TESTS - " + passedScenarios.size)
        print("\nFailed TESTS - " + failedScenarios.size)
        print("\n")
        print(failedScenarios)

        assertTrue(failedScenarios.size == 0)
    }

    @Test
    fun testInValidConditionObj() {
        val evaluator = GBConditionEvaluator()
        assertFalse(evaluator.evalCondition(JsonObject(HashMap()), JsonArray(ArrayList()), null))

        assertFalse(evaluator.isOperatorObject(JsonObject(HashMap())))

        assertEquals(evaluator.getType(null).toString(), GBAttributeType.GbUnknown.toString())

        assertTrue(evaluator.getPath(mapOf("value" to JsonPrimitive("test")), "key") is JsonNull)

        assertTrue(!evaluator.evalConditionValue(JsonObject(HashMap()), null, null))

        assertTrue(
            evaluator.evalOperatorCondition(
                "${"$"}lte",
                JsonPrimitive("abc"),
                JsonPrimitive("abc"),
                null
            )
        )

        assertTrue(
            evaluator.evalOperatorCondition(
                "${"$"}gte",
                JsonPrimitive("abc"),
                JsonPrimitive("abc"),
                null
            )
        )

        assertTrue(
            evaluator.evalOperatorCondition(
                "${"$"}vlt",
                JsonPrimitive("0.9.0"),
                JsonPrimitive("0.10.0"),
                null
            )
        )

    }

    @Test
    fun testConditionFailAttributeDoesNotExist() {
        val attributes = mapOf("country" to JsonPrimitive("IN"))

        @Language("json")
        val condition = """
            {"brand":"KZ"}
        """.trimIndent()

        assertEquals(
            false, GBConditionEvaluator().evalCondition(
                attributes,
                Json.decodeFromString(GBCondition.serializer(), condition),
                null
            )
        )
    }

    @Test
    fun testConditionDoesNotExistAttributeExist() {
        val attributes = mapOf("userId" to JsonPrimitive("1199"))

        @Language("json")
        val condition = """
            {
              "userId": {
                "${'$'}exists": false
              }
            }
        """.trimIndent()

        assertEquals(
            false, GBConditionEvaluator().evalCondition(
                attributes,
                Json.decodeFromString(GBCondition.serializer(), condition),
                null
            )
        )
    }

    @Test
    fun testConditionExistAttributeExist() {
        val attributes = mapOf("userId" to JsonPrimitive("1199"))

        @Language("json")
        val condition = """
            {
              "userId": {
                "${'$'}exists": true
              }
            }
        """.trimIndent()

        assertEquals(
            true, GBConditionEvaluator().evalCondition(
                attributes,
                Json.decodeFromString(GBCondition.serializer(), condition),
                null
            )
        )
    }

    @Test
    fun testConditionExistAttributeDoesNotExist() {
        val attributes = mapOf("user_id_not_exist" to JsonPrimitive("1199"))

        @Language("json")
        val condition = """
            {
              "userId": {
                "${'$'}exists": true
              }
            }
        """.trimIndent()

        assertEquals(
            false, GBConditionEvaluator().evalCondition(
                attributes,
                Json.decodeFromString(GBCondition.serializer(), condition),
                null
            )
        )
    }
}
