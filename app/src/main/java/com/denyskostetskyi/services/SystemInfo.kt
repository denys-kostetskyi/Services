package com.denyskostetskyi.services

data class SystemInfo(
    val batteryLevel: Int,
    val availableMemory: Double,
    val totalMemory: Double,
    val availableStorage: Double,
    val totalStorage: Double,
)
