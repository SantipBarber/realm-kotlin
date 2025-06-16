/*
 * Copyright 2024 Realm Inc. & SantipBarber
 * 
 * Modern Interop Layer Implementation
 * Designed and developed by SantipBarber - December 2024
 * 
 * This Modern Interop Layer represents a complete architectural rewrite
 * that eliminates SWIG dependencies and introduces advanced reactive programming.
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

package io.realm.kotlin.modern.interop

import io.realm.kotlin.modern.platform.PlatformUtils

/**
 * Android implementation of RealmCoreNative using JNI.
 * 
 * This provides direct access to realm-core C++ library through
 * optimized JNI bindings, replacing the traditional SWIG approach.
 */
public actual object RealmCoreNative {
    
    private var isNativeLibraryLoaded = false
    private var nativeAvailable = false
    
    init {
        loadNativeLibrary()
    }
    
    /**
     * Loads the native realm-core library.
     */
    private fun loadNativeLibrary() {
        try {
            System.loadLibrary("realm-core-modern")
            isNativeLibraryLoaded = true
            nativeAvailable = nativeInitialize()
            
            if (nativeAvailable) {
                PlatformUtils.logPlatformError("Successfully loaded realm-core native library", null)
            } else {
                PlatformUtils.logPlatformError("Realm-core native library loaded but initialization failed", null)
            }
        } catch (e: UnsatisfiedLinkError) {
            PlatformUtils.logPlatformError("Failed to load realm-core native library", e)
            isNativeLibraryLoaded = false
            nativeAvailable = false
        } catch (e: Exception) {
            PlatformUtils.logPlatformError("Unexpected error loading realm-core native library", e)
            isNativeLibraryLoaded = false
            nativeAvailable = false
        }
    }
    
    /**
     * Checks if the native library is available.
     */
    public fun isNativeAvailable(): Boolean = nativeAvailable
    
    // === Native method declarations ===
    
    /**
     * Initializes the native realm-core library.
     */
    private external fun nativeInitialize(): Boolean
    
    // Database Operations
    public actual external fun nativeOpenDatabase(
        path: String,
        encryptionKey: ByteArray?,
        schemaVersion: Long,
        shouldDeleteIfMigrationNeeded: Boolean
    ): Long
    
    public actual external fun nativeCloseDatabase(databasePointer: Long)
    
    public actual external fun nativeDeleteDatabase(path: String): Boolean
    
    public actual external fun nativeCompactDatabase(databasePointer: Long): Boolean
    
    // Transaction Operations
    public actual external fun nativeBeginWrite(databasePointer: Long): Long
    
    public actual external fun nativeCommitWrite(transactionPointer: Long): Boolean
    
    public actual external fun nativeCancelWrite(transactionPointer: Long)
    
    // Object Operations
    public actual external fun nativeCreateObject(
        transactionPointer: Long,
        tableKey: Long,
        primaryKeyValue: Any?
    ): Long
    
    public actual external fun nativeFindObject(
        databasePointer: Long,
        tableKey: Long,
        primaryKeyValue: Any
    ): Long
    
    public actual external fun nativeDeleteObject(
        transactionPointer: Long,
        objectPointer: Long
    ): Boolean
    
    // Property Operations
    public actual external fun nativeGetProperty(
        objectPointer: Long,
        columnKey: Long
    ): Any?
    
    public actual external fun nativeSetProperty(
        objectPointer: Long,
        columnKey: Long,
        value: Any?
    ): Boolean
    
    // Schema Operations
    public actual external fun nativeGetSchemaVersion(databasePointer: Long): Long
    
    public actual external fun nativeGetTableKeys(databasePointer: Long): LongArray
    
    public actual external fun nativeGetTableName(databasePointer: Long, tableKey: Long): String
    
    public actual external fun nativeGetColumnKeys(databasePointer: Long, tableKey: Long): LongArray
    
    public actual external fun nativeGetColumnName(
        databasePointer: Long,
        tableKey: Long,
        columnKey: Long
    ): String
    
    public actual external fun nativeGetColumnType(
        databasePointer: Long,
        tableKey: Long,
        columnKey: Long
    ): Int
    
    // Query Operations
    public actual external fun nativeCreateQuery(
        databasePointer: Long,
        tableKey: Long
    ): Long
    
    public actual external fun nativeQueryFilter(
        queryPointer: Long,
        columnKey: Long,
        operator: Int,
        value: Any?
    ): Long
    
    public actual external fun nativeExecuteQuery(queryPointer: Long): Long
    
    public actual external fun nativeGetResultsCount(resultsPointer: Long): Long
    
    public actual external fun nativeGetResultsObject(
        resultsPointer: Long,
        index: Long
    ): Long
    
    // Memory Management
    public actual external fun nativeReleaseHandle(pointer: Long)
    
    public actual external fun nativeGetLastError(): String?
    
    // Change Notifications
    public actual external fun nativeAddChangeListener(
        databasePointer: Long,
        callback: (Long) -> Unit
    ): Long
    
    public actual external fun nativeRemoveChangeListener(listenerPointer: Long)
    
    // === Fallback implementations for when native library is not available ===
    
    /**
     * Provides fallback implementations when native library is not available.
     * These are simplified versions for testing/development.
     */
    private object FallbackImplementation {
        
        private val handleCounter = java.util.concurrent.atomic.AtomicLong(10000L)
        private val databases = mutableMapOf<Long, String>()
        private val errorMessage = ThreadLocal<String?>()
        
        fun openDatabase(
            path: String,
            encryptionKey: ByteArray?,
            schemaVersion: Long,
            shouldDeleteIfMigrationNeeded: Boolean
        ): Long {
            val handle = handleCounter.incrementAndGet()
            databases[handle] = path
            return handle
        }
        
        fun closeDatabase(databasePointer: Long) {
            databases.remove(databasePointer)
        }
        
        fun deleteDatabase(path: String): Boolean {
            return try {
                java.io.File(path).delete()
            } catch (e: Exception) {
                false
            }
        }
        
        fun getLastError(): String? = errorMessage.get()
        
        fun setError(message: String) {
            errorMessage.set(message)
        }
    }
    
    // === Public API implementations that handle fallback ===
    
    /**
     * Opens a database, using native implementation if available, fallback otherwise.
     */
    public fun openDatabaseSafe(
        path: String,
        encryptionKey: ByteArray? = null,
        schemaVersion: Long = 0,
        shouldDeleteIfMigrationNeeded: Boolean = false
    ): Long {
        return if (nativeAvailable) {
            nativeOpenDatabase(path, encryptionKey, schemaVersion, shouldDeleteIfMigrationNeeded)
        } else {
            FallbackImplementation.openDatabase(path, encryptionKey, schemaVersion, shouldDeleteIfMigrationNeeded)
        }
    }
    
    /**
     * Closes a database safely.
     */
    public fun closeDatabaseSafe(databasePointer: Long) {
        if (nativeAvailable) {
            nativeCloseDatabase(databasePointer)
        } else {
            FallbackImplementation.closeDatabase(databasePointer)
        }
    }
    
    /**
     * Gets the last error safely.
     */
    public fun getLastErrorSafe(): String? {
        return if (nativeAvailable) {
            nativeGetLastError()
        } else {
            FallbackImplementation.getLastError()
        }
    }
}