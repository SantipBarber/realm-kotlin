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

/**
 * Modern Native Interface to realm-core.
 * 
 * This interface defines the low-level operations that need to be implemented
 * by each platform's native bridge to realm-core. This replaces the traditional
 * SWIG-generated bindings with a clean, modern interface.
 */
public expect object RealmCoreNative {
    
    // === Database Operations ===
    
    /**
     * Opens a realm database at the specified path.
     * Returns a native pointer to the database.
     */
    public fun nativeOpenDatabase(
        path: String,
        encryptionKey: ByteArray? = null,
        schemaVersion: Long = 0,
        shouldDeleteIfMigrationNeeded: Boolean = false
    ): Long
    
    /**
     * Closes a realm database.
     */
    public fun nativeCloseDatabase(databasePointer: Long)
    
    /**
     * Deletes a realm database at the specified path.
     */
    public fun nativeDeleteDatabase(path: String): Boolean
    
    /**
     * Compacts a realm database.
     */
    public fun nativeCompactDatabase(databasePointer: Long): Boolean
    
    // === Transaction Operations ===
    
    /**
     * Begins a write transaction.
     * Returns a native pointer to the transaction.
     */
    public fun nativeBeginWrite(databasePointer: Long): Long
    
    /**
     * Commits a write transaction.
     */
    public fun nativeCommitWrite(transactionPointer: Long): Boolean
    
    /**
     * Cancels a write transaction.
     */
    public fun nativeCancelWrite(transactionPointer: Long)
    
    // === Object Operations ===
    
    /**
     * Creates a new object in the realm.
     * Returns a native pointer to the object.
     */
    public fun nativeCreateObject(
        transactionPointer: Long,
        tableKey: Long,
        primaryKeyValue: Any? = null
    ): Long
    
    /**
     * Finds an object by primary key.
     * Returns a native pointer to the object, or 0 if not found.
     */
    public fun nativeFindObject(
        databasePointer: Long,
        tableKey: Long,
        primaryKeyValue: Any
    ): Long
    
    /**
     * Deletes an object from the realm.
     */
    public fun nativeDeleteObject(
        transactionPointer: Long,
        objectPointer: Long
    ): Boolean
    
    // === Property Operations ===
    
    /**
     * Gets a property value from an object.
     */
    public fun nativeGetProperty(
        objectPointer: Long,
        columnKey: Long
    ): Any?
    
    /**
     * Sets a property value on an object.
     */
    public fun nativeSetProperty(
        objectPointer: Long,
        columnKey: Long,
        value: Any?
    ): Boolean
    
    // === Schema Operations ===
    
    /**
     * Gets the schema version of a database.
     */
    public fun nativeGetSchemaVersion(databasePointer: Long): Long
    
    /**
     * Gets all table keys in the database.
     */
    public fun nativeGetTableKeys(databasePointer: Long): LongArray
    
    /**
     * Gets the name of a table by its key.
     */
    public fun nativeGetTableName(databasePointer: Long, tableKey: Long): String
    
    /**
     * Gets all column keys for a table.
     */
    public fun nativeGetColumnKeys(databasePointer: Long, tableKey: Long): LongArray
    
    /**
     * Gets the name of a column by its key.
     */
    public fun nativeGetColumnName(
        databasePointer: Long,
        tableKey: Long,
        columnKey: Long
    ): String
    
    /**
     * Gets the type of a column by its key.
     */
    public fun nativeGetColumnType(
        databasePointer: Long,
        tableKey: Long,
        columnKey: Long
    ): Int
    
    // === Query Operations ===
    
    /**
     * Creates a query for a table.
     * Returns a native pointer to the query.
     */
    public fun nativeCreateQuery(
        databasePointer: Long,
        tableKey: Long
    ): Long
    
    /**
     * Adds a filter to a query.
     */
    public fun nativeQueryFilter(
        queryPointer: Long,
        columnKey: Long,
        operator: Int,
        value: Any?
    ): Long
    
    /**
     * Executes a query and returns results.
     * Returns a native pointer to the results.
     */
    public fun nativeExecuteQuery(queryPointer: Long): Long
    
    /**
     * Gets the count of results.
     */
    public fun nativeGetResultsCount(resultsPointer: Long): Long
    
    /**
     * Gets an object from results by index.
     * Returns a native pointer to the object.
     */
    public fun nativeGetResultsObject(
        resultsPointer: Long,
        index: Long
    ): Long
    
    // === Memory Management ===
    
    /**
     * Releases a native handle/pointer.
     */
    public fun nativeReleaseHandle(pointer: Long)
    
    /**
     * Gets the last error message from realm-core.
     */
    public fun nativeGetLastError(): String?
    
    // === Change Notifications ===
    
    /**
     * Adds a change listener to the database.
     * Returns a native pointer to the listener.
     */
    public fun nativeAddChangeListener(
        databasePointer: Long,
        callback: (Long) -> Unit
    ): Long
    
    /**
     * Removes a change listener.
     */
    public fun nativeRemoveChangeListener(listenerPointer: Long)
}

/**
 * Native column type constants.
 * These correspond to realm-core's DataType enum.
 */
public object RealmCoreNativeTypes {
    public const val TYPE_INT: Int = 0
    public const val TYPE_BOOL: Int = 1
    public const val TYPE_STRING: Int = 2
    public const val TYPE_BINARY: Int = 4
    public const val TYPE_TIMESTAMP: Int = 8
    public const val TYPE_FLOAT: Int = 9
    public const val TYPE_DOUBLE: Int = 10
    public const val TYPE_DECIMAL: Int = 11
    public const val TYPE_LINK: Int = 12
    public const val TYPE_LINKLIST: Int = 13
    public const val TYPE_OBJECT_ID: Int = 15
    public const val TYPE_MIXED: Int = 16
    public const val TYPE_UUID: Int = 17
}

/**
 * Native query operator constants.
 * These correspond to realm-core's query operators.
 */
public object RealmCoreNativeOperators {
    public const val OP_EQUAL: Int = 0
    public const val OP_NOT_EQUAL: Int = 1
    public const val OP_GREATER: Int = 2
    public const val OP_GREATER_EQUAL: Int = 3
    public const val OP_LESS: Int = 4
    public const val OP_LESS_EQUAL: Int = 5
    public const val OP_BEGINS_WITH: Int = 6
    public const val OP_ENDS_WITH: Int = 7
    public const val OP_CONTAINS: Int = 8
    public const val OP_IS_NULL: Int = 9
    public const val OP_IS_NOT_NULL: Int = 10
}

/**
 * Exception thrown when realm-core operations fail.
 */
public class RealmCoreNativeException(
    message: String,
    public val nativeErrorCode: Int = 0,
    cause: Throwable? = null
) : Exception(message, cause)