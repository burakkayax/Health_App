package com.saglik.core.healthconnect

import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient

object HealthConnectIntents {
    const val PROVIDER_PACKAGE_NAME = "com.google.android.apps.healthdata"

    fun settingsIntent(): Intent =
        Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)

    fun installOrUpdateIntent(): Intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$PROVIDER_PACKAGE_NAME"),
        ).setPackage("com.android.vending")

    fun installOrUpdateWebIntent(): Intent =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$PROVIDER_PACKAGE_NAME"),
        )
}
