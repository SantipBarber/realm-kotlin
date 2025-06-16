package io.realm.kotlin

/**
 * Platform information and basic utilities
 */
public expect object PlatformInfo {
    public fun getPlatformName(): String
    public fun getPlatformVersion(): String
}