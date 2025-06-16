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

package io.realm.kotlin.internal

import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.modern.interop.RealmCoreInterop
import io.realm.kotlin.modern.interop.RealmResultsHandle
import io.realm.kotlin.modern.query.QueryBuilder
import io.realm.kotlin.modern.proxy.RealmObjectProxyFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.reflect.KClass

/**
 * Advanced implementation of RealmResults with full sort and limit support.
 * 
 * This version properly applies sorting and limiting to query results,
 * providing a complete implementation of the RealmResults interface.
 */
public class ModernRealmResultsAdvanced<T : BaseRealmObject>(
    private val clazz: KClass<T>,
    private val resultsHandle: RealmResultsHandle,
    private val queryBuilder: QueryBuilder<T>
) : RealmResults<T> {
    
    // Cache the processed results after applying sort and limit
    private val _processedResults: List<T> by lazy {
        loadAndProcessResults()
    }
    
    override val size: Int
        get() = _processedResults.size
    
    override fun contains(element: T): Boolean {
        return _processedResults.contains(element)
    }
    
    override fun containsAll(elements: Collection<T>): Boolean {
        return _processedResults.containsAll(elements)
    }
    
    override fun get(index: Int): T {
        return _processedResults[index]
    }
    
    override fun indexOf(element: T): Int {
        return _processedResults.indexOf(element)
    }
    
    override fun isEmpty(): Boolean = _processedResults.isEmpty()
    
    override fun iterator(): Iterator<T> {
        return _processedResults.iterator()
    }
    
    override fun lastIndexOf(element: T): Int {
        return _processedResults.lastIndexOf(element)
    }
    
    override fun listIterator(): ListIterator<T> {
        return _processedResults.listIterator()
    }
    
    override fun listIterator(index: Int): ListIterator<T> {
        return _processedResults.listIterator(index)
    }
    
    override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        return _processedResults.subList(fromIndex, toIndex)
    }
    
    override fun asFlow(): Flow<ResultsChange<T>> {
        return io.realm.kotlin.modern.notifications.RealmFlowFactory.createResultsFlow(
            clazz = clazz,
            resultsHandle = resultsHandle,
            resultsProvider = { this }
        )
    }
    
    /**
     * Loads all results from the handle and applies sorting and limiting.
     */
    private fun loadAndProcessResults(): List<T> {
        // First, load all raw results from the results handle
        val rawResults = loadRawResults()
        
        // Then apply sorting and limiting using the query builder
        val processedResults = queryBuilder.applySortAndLimit(rawResults)
        
        // Convert back to typed results
        @Suppress("UNCHECKED_CAST")
        return processedResults as List<T>
    }
    
    /**
     * Loads all raw results from the results handle.
     */
    private fun loadRawResults(): List<T> {
        val rawSize = RealmCoreInterop.getResultsSize(resultsHandle)
        val results = mutableListOf<T>()
        
        for (i in 0 until rawSize) {
            val objectHandle = RealmCoreInterop.getResultsObject(resultsHandle, i)
            if (objectHandle != null) {
                val proxy = RealmObjectProxyFactory.createProxy(clazz, objectHandle)
                results.add(proxy)
            }
        }
        
        return results
    }
}

/**
 * Factory for creating advanced results with proper sorting and limiting.
 */
public object ModernRealmResultsFactory {
    
    /**
     * Creates advanced results that support sorting and limiting.
     */
    public fun <T : BaseRealmObject> createAdvancedResults(
        clazz: KClass<T>,
        resultsHandle: RealmResultsHandle,
        queryBuilder: QueryBuilder<T>
    ): RealmResults<T> {
        return if (queryBuilder.getSortClauses().isNotEmpty() || queryBuilder.getLimit() != null) {
            // Use advanced results for queries with sorting or limiting
            ModernRealmResultsAdvanced(clazz, resultsHandle, queryBuilder)
        } else {
            // Use simple results for basic queries
            ModernRealmResultsImpl(clazz, resultsHandle)
        }
    }
}