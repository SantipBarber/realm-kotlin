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

import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.BaseRealmObject
import kotlinx.coroutines.flow.Flow

/**
 * Modern Interop Layer - Core interface for Realm database operations.
 * 
 * This replaces the traditional SWIG+cinterop approach with a clean, modern
 * Kotlin/Native FFI interface that directly communicates with realm-core.
 */
public expect object RealmCoreInterop {
    
    // Database Management
    public fun openDatabase(config: RealmConfiguration): RealmDatabaseHandle
    public fun closeDatabase(handle: RealmDatabaseHandle)
    public fun deleteDatabase(config: RealmConfiguration): Boolean
    public fun compactDatabase(config: RealmConfiguration): Boolean
    
    // Transaction Management
    public fun beginWrite(handle: RealmDatabaseHandle): RealmTransactionHandle
    public fun commitWrite(transactionHandle: RealmTransactionHandle): Boolean
    public fun cancelWrite(transactionHandle: RealmTransactionHandle)
    
    // Object Operations
    public fun createObject(
        transactionHandle: RealmTransactionHandle,
        className: String,
        primaryKey: Any? = null
    ): RealmObjectHandle
    
    public fun findObject(
        handle: RealmDatabaseHandle,
        className: String,
        primaryKey: Any
    ): RealmObjectHandle?
    
    public fun deleteObject(
        transactionHandle: RealmTransactionHandle,
        objectHandle: RealmObjectHandle
    ): Boolean
    
    // Property Operations
    public fun getProperty(objectHandle: RealmObjectHandle, propertyName: String): Any?
    public fun setProperty(
        objectHandle: RealmObjectHandle,
        propertyName: String,
        value: Any?
    ): Boolean
    
    // Query Operations
    public fun createQuery(
        handle: RealmDatabaseHandle,
        className: String
    ): RealmQueryHandle
    
    public fun addQueryFilter(
        queryHandle: RealmQueryHandle,
        property: String,
        operator: QueryOperator,
        value: Any?
    ): RealmQueryHandle
    
    public fun executeQuery(queryHandle: RealmQueryHandle): RealmResultsHandle
    public fun getQueryCount(queryHandle: RealmQueryHandle): Long
    
    // Results Operations
    public fun getResultsSize(resultsHandle: RealmResultsHandle): Long
    public fun getResultsObject(resultsHandle: RealmResultsHandle, index: Long): RealmObjectHandle?
    
    // Change Notifications
    public fun addChangeListener(
        handle: RealmDatabaseHandle,
        callback: (RealmChangeset) -> Unit
    ): RealmListenerHandle
    
    public fun addResultsListener(
        resultsHandle: RealmResultsHandle,
        callback: (changeType: io.realm.kotlin.modern.notifications.ChangeType, indices: IntArray?) -> Unit
    ): RealmListenerHandle
    
    public fun addObjectListener(
        objectHandle: RealmObjectHandle,
        callback: (changeType: io.realm.kotlin.modern.notifications.ChangeType, changedProperties: Array<String>?) -> Unit
    ): RealmListenerHandle
    
    public fun addDatabaseListener(
        databaseHandle: RealmDatabaseHandle,
        callback: (changeType: io.realm.kotlin.modern.notifications.ChangeType) -> Unit
    ): RealmListenerHandle
    
    public fun removeListener(listenerHandle: RealmListenerHandle)
    public fun removeChangeListener(listenerHandle: RealmListenerHandle)
    
    // Schema Operations
    public fun getSchemaVersion(handle: RealmDatabaseHandle): Long
    public fun getSchemaClasses(handle: RealmDatabaseHandle): List<String>
    public fun getClassProperties(handle: RealmDatabaseHandle, className: String): List<RealmPropertyInfo>
    
    // Memory Management
    public fun releaseHandle(handle: RealmHandle)
    public fun releaseAllHandles()
}

/**
 * Base interface for all Realm handles
 */
public interface RealmHandle {
    public val nativePointer: Long
    public val isValid: Boolean
}

/**
 * Handle to a Realm database instance
 */
public expect class RealmDatabaseHandle : RealmHandle {
    public override val nativePointer: Long
    public override val isValid: Boolean
}

/**
 * Handle to a write transaction
 */
public expect class RealmTransactionHandle : RealmHandle {
    public override val nativePointer: Long
    public override val isValid: Boolean
    public val databaseHandle: RealmDatabaseHandle
}

/**
 * Handle to a Realm object instance
 */
public expect class RealmObjectHandle : RealmHandle {
    public override val nativePointer: Long
    public override val isValid: Boolean
    public val className: String
}

/**
 * Handle to a query instance
 */
public expect class RealmQueryHandle : RealmHandle {
    public override val nativePointer: Long
    public override val isValid: Boolean
    public val className: String
}

/**
 * Handle to query results
 */
public expect class RealmResultsHandle : RealmHandle {
    public override val nativePointer: Long
    public override val isValid: Boolean
    public val size: Long
}

/**
 * Handle to a change notification listener
 */
public expect class RealmListenerHandle : RealmHandle {
    public override val nativePointer: Long
    public override val isValid: Boolean
}

/**
 * Query operators for filtering
 */
public enum class QueryOperator {
    EQUAL,
    NOT_EQUAL,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    CONTAINS,
    STARTS_WITH,
    ENDS_WITH,
    IN,
    IS_NULL,
    IS_NOT_NULL,
    LIKE,
    IS_EMPTY,
    IS_NOT_EMPTY,
    REGEX_MATCH,
    SIZE_EQUAL,
    SIZE_GREATER_THAN,
    SIZE_LESS_THAN
}

/**
 * Information about a property in a Realm class
 */
public data class RealmPropertyInfo(
    val name: String,
    val type: RealmPropertyType,
    val isOptional: Boolean,
    val isPrimaryKey: Boolean,
    val isIndexed: Boolean
)

/**
 * Realm property types
 */
public enum class RealmPropertyType {
    BOOLEAN,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    STRING,
    BINARY,
    DATE,
    OBJECT,
    LIST,
    SET,
    DICTIONARY
}

/**
 * Changeset information for database notifications
 */
public data class RealmChangeset(
    val deletions: List<Long>,
    val insertions: List<Long>,
    val modifications: List<Long>
)

/**
 * Modern Realm exception for interop errors
 */
public class RealmInteropException(
    message: String,
    cause: Throwable? = null,
    public val errorCode: Int = 0
) : Exception(message, cause)

/**
 * High-level Modern Realm Manager that coordinates all operations
 */
public object ModernRealmManager {
    
    private val openDatabases = mutableMapOf<String, RealmDatabaseHandle>()
    private val activeTransactions = mutableMapOf<RealmDatabaseHandle, RealmTransactionHandle>()
    
    public fun openRealm(config: RealmConfiguration): RealmDatabaseHandle {
        val key = config.name ?: config.path
        return openDatabases.getOrPut(key) {
            RealmCoreInterop.openDatabase(config)
        }
    }
    
    public fun closeRealm(config: RealmConfiguration) {
        val key = config.name ?: config.path
        openDatabases[key]?.let { handle ->
            // Cancel any active transactions
            activeTransactions[handle]?.let { transaction ->
                RealmCoreInterop.cancelWrite(transaction)
                activeTransactions.remove(handle)
            }
            
            RealmCoreInterop.closeDatabase(handle)
            openDatabases.remove(key)
        }
    }
    
    public fun <R> executeWrite(
        handle: RealmDatabaseHandle,
        block: (RealmTransactionHandle) -> R
    ): R {
        if (activeTransactions.containsKey(handle)) {
            throw RealmInteropException("Transaction already active for this database")
        }
        
        val transaction = RealmCoreInterop.beginWrite(handle)
        activeTransactions[handle] = transaction
        
        return try {
            val result = block(transaction)
            if (RealmCoreInterop.commitWrite(transaction)) {
                result
            } else {
                throw RealmInteropException("Failed to commit transaction")
            }
        } catch (e: Exception) {
            RealmCoreInterop.cancelWrite(transaction)
            throw e
        } finally {
            activeTransactions.remove(handle)
        }
    }
    
    public fun isInTransaction(handle: RealmDatabaseHandle): Boolean {
        return activeTransactions.containsKey(handle)
    }
    
    public fun cleanup() {
        // Cancel all active transactions
        activeTransactions.values.forEach { transaction ->
            RealmCoreInterop.cancelWrite(transaction)
        }
        activeTransactions.clear()
        
        // Close all open databases
        openDatabases.values.forEach { handle ->
            RealmCoreInterop.closeDatabase(handle)
        }
        openDatabases.clear()
        
        // Release all handles
        RealmCoreInterop.releaseAllHandles()
    }
}