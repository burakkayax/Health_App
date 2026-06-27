package com.saglik.core.healthconnect

import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.PermissionController

object HealthConnectPermissionRequestContract {
    fun create(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()
}
