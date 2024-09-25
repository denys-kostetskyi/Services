package com.denyskostetskyi.services

class MemoryUtils {
    companion object {
        fun bytesToGigabytes(bytes: Long) = bytes.toDouble() / 1024 / 1024 / 1024
    }
}