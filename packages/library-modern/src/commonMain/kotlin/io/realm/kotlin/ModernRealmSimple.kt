/*
 * Copyright 2024 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.kotlin

/**
 * Modern Realm implementation - Simple version for compilation testing.
 */
public object ModernRealmSimple {
    
    /**
     * Get version information.
     */
    public fun getVersion(): String = "2.1.0-modern"
    
    /**
     * Check if the modern implementation is available.
     */
    public fun isAvailable(): Boolean = true
    
    /**
     * Get platform information.
     */
    public fun getPlatformInfo(): String {
        return "${PlatformInfo.getPlatformName()} ${PlatformInfo.getPlatformVersion()}"
    }
}

