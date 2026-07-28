package com.matejdro.micropebble.webservices.api

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/**
 * Represents a GitHub repository as a source for firmware files.
 */
@Immutable
@Serializable
data class GithubSource(
    val id: Uuid,
    val owner: String,
    val repo: String,
    val name: String,
    val enabled: Boolean = true,
) {
    val fullName: String
        get() = "$owner/$repo"
    
    val apiUrl: String
        get() = "https://api.github.com/repos/$owner/$repo"
    
    companion object {
        // Default GitHub sources for firmware
        val coredevicesPebbleOS = GithubSource(
            id = Uuid.parse("8a0d1a5f-4f2e-4f8a-9c11-3e4b8e6d7c2a"),
            owner = "coredevices",
            repo = "PebbleOS",
            name = "Core Devices PebbleOS",
            enabled = true
        )
        
        val pebbleFw = GithubSource(
            id = Uuid.parse("b2e3c4d5-7e8f-4a2b-9d3f-0c6e1f2a3b4c"),
            owner = "bmacphail",
            repo = "pebblefw",
            name = "Original Pebble Firmware",
            enabled = true
        )
        
        val defaultSources: List<GithubSource> = listOf(
            coredevicesPebbleOS,
            pebbleFw
        )
    }
}
