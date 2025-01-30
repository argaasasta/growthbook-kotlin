package com.sdk.growthbook.integration

import org.junit.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import org.intellij.lang.annotations.Language
import com.sdk.growthbook.utils.Resource
import com.sdk.growthbook.tests.MockNetworkClient

class IntegrationTests {

    @Test
    fun verifyIsInTheListRule() {
        // User attributes for targeting and assigning users to experiment variations
        val attrs = HashMap<String, Any>()
        attrs["appBuildNumber"] = 3432

        @Language("json")
        val json = """
{
  "status": 200,
  "features": {
    "user576-feature": {
      "defaultValue": false,
      "rules": [
        {
          "condition": {
            "appBuildNumber": {
              "${'$'}in": [
                3432,
                3431
              ]
            }
          },
          "force": true
        }
      ]
    }
  }
}
        """.trimMargin()

        val sdkInstance = buildSDK(json, attrs)

        assertTrue(
            sdkInstance.isOn("user576-feature")
        )
    }

    @Test
    fun `gBExperimentResult name should not be null`() {
        @Language("json")
        val json = """
{
  "status": 200,
  "features": {
    "post-appointment-all-video-appointments-button": {
      "defaultValue": false,
      "rules": [{
        "coverage": 1,
        "hashAttribute": "id",
        "bucketVersion": 1,
        "seed": "56bc09c2-53b7-457e-9980-c531c4431c59",
        "hashVersion": 2,
        "variations": [false, true],
        "weights": [0, 1],
        "key": "aoc-post-appointment-button-type",
        "meta": [{
          "key": "0",
          "name": "Control"
        }, {
          "key": "1",
          "name": "Button filled"
        }],
        "phase": "1",
        "name": "AoC post appointment button type"
      }]
    }
  },
  "dateUpdated": "2024-03-11T16:40:55.214Z"
}
        """.trimMargin()

        val growthBookSdk = buildSDK(
            json = json,
            attributes = mapOf("id" to "someId"),
            trackingCallback = { _, gbExperimentResult ->
                val variationName = gbExperimentResult.name
                assertTrue(
                    (variationName == "Button filled") || (variationName == "Control")
                )
            }
        )

        growthBookSdk.feature<Boolean>("post-appointment-all-video-appointments-button")
    }

    @Test
    fun `autoRefreshFeatures() method usage example`() {
        val growthBookSdk = buildSDK("")

        growthBookSdk
            .autoRefreshFeatures()
            .launchIn(TestScope())
    }

    @Test
    fun `autoRefreshFeatures() method triggers consumeSSEConnection()`() {
        var wasMethodCalled = false

        val networkDispatcher = object: MockNetworkClient("", null) {
            override fun consumeSSEConnection(url: String): Flow<Resource<String>> {
                wasMethodCalled = true
                return super.consumeSSEConnection(url)
            }
        }

        val growthBookSdk = buildSDK(
            json = "",
            attributes = mapOf("id" to "someId"),
            networkDispatcher = networkDispatcher,
        )

        growthBookSdk.autoRefreshFeatures()

        assertTrue(wasMethodCalled)
    }
}
