package com.saglik.core.model

data class HealthConnectPermissionStatus(
    val requiredPermissions: Set<String>,
    val grantedPermissions: Set<String>,
) {
    val missingPermissions: Set<String> = requiredPermissions - grantedPermissions
    val allRequiredGranted: Boolean = missingPermissions.isEmpty()

    companion object {
        fun from(
            requiredPermissions: Set<String>,
            grantedPermissions: Set<String>,
        ): HealthConnectPermissionStatus =
            HealthConnectPermissionStatus(
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions,
            )
    }
}
