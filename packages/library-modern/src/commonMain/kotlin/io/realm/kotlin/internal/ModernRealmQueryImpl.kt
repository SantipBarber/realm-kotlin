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
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.RealmScalarQuery
import io.realm.kotlin.query.RealmSingleQuery
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.modern.interop.RealmCoreInterop
import io.realm.kotlin.modern.interop.RealmDatabaseHandle
import io.realm.kotlin.modern.interop.RealmQueryHandle
import io.realm.kotlin.modern.query.QueryBuilder
import io.realm.kotlin.modern.query.ModernQueryParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.reflect.KClass

/**
 * Modern implementation of the RealmQuery interface using Modern Interop Layer.
 */
public class ModernRealmQueryImpl<T : BaseRealmObject>(
    private val clazz: KClass<T>,
    private val databaseHandle: RealmDatabaseHandle,
    private val query: String = "",
    private val args: List<Any?> = emptyList()
) : RealmQuery<T> {

    private var queryHandle: RealmQueryHandle = RealmCoreInterop.createQuery(
        databaseHandle, 
        clazz.simpleName ?: "RealmObject"
    )
    
    private val queryBuilder: QueryBuilder<T> = if (query.isNotEmpty()) {
        ModernQueryParser.parseQuery(query, *args.toTypedArray())
    } else {
        QueryBuilder()
    }

    override fun find(): RealmResults<T> {
        // Apply all filters from the query builder
        queryHandle = queryBuilder.applyTo(queryHandle)
        val resultsHandle = RealmCoreInterop.executeQuery(queryHandle)
        
        // Use advanced results factory to determine the appropriate implementation
        return ModernRealmResultsFactory.createAdvancedResults(clazz, resultsHandle, queryBuilder)
    }

    override fun first(): RealmSingleQuery<T> {
        return ModernRealmSingleQueryImpl(clazz, queryHandle, queryBuilder)
    }

    override fun limit(limit: Int): RealmQuery<T> {
        queryBuilder.limit(limit)
        return this
    }

    override fun sort(property: String, sortOrder: Sort): RealmQuery<T> {
        queryBuilder.sort(property, sortOrder)
        return this
    }

    override fun count(): RealmScalarQuery<Long> {
        queryHandle = queryBuilder.applyTo(queryHandle)
        val count = RealmCoreInterop.getQueryCount(queryHandle)
        return ModernRealmScalarQueryImpl(count)
    }

    override fun asFlow(): Flow<ResultsChange<T>> {
        // Execute query and get results handle
        queryHandle = queryBuilder.applyTo(queryHandle)
        val resultsHandle = RealmCoreInterop.executeQuery(queryHandle)
        
        // Create reactive flow for the results
        return io.realm.kotlin.modern.notifications.RealmFlowFactory.createResultsFlow(
            clazz = clazz,
            resultsHandle = resultsHandle,
            resultsProvider = { 
                ModernRealmResultsFactory.createAdvancedResults(clazz, resultsHandle, queryBuilder)
            }
        )
    }
    
    // Extension methods for fluent query building
    public fun equal(property: String, value: Any?): RealmQuery<T> {
        queryBuilder.equal(property, value)
        return this
    }
    
    public fun notEqual(property: String, value: Any?): RealmQuery<T> {
        queryBuilder.notEqual(property, value)
        return this
    }
    
    public fun greaterThan(property: String, value: Any): RealmQuery<T> {
        queryBuilder.greaterThan(property, value)
        return this
    }
    
    public fun lessThan(property: String, value: Any): RealmQuery<T> {
        queryBuilder.lessThan(property, value)
        return this
    }
    
    public fun contains(property: String, value: String): RealmQuery<T> {
        queryBuilder.contains(property, value)
        return this
    }
    
    public fun isNull(property: String): RealmQuery<T> {
        queryBuilder.isNull(property)
        return this
    }
    
    public fun isNotNull(property: String): RealmQuery<T> {
        queryBuilder.isNotNull(property)
        return this
    }
}

/**
 * Modern implementation of RealmSingleQuery.
 */
public class ModernRealmSingleQueryImpl<T : BaseRealmObject>(
    private val clazz: KClass<T>,
    private var queryHandle: RealmQueryHandle,
    private val queryBuilder: QueryBuilder<T> = QueryBuilder()
) : RealmSingleQuery<T> {
    override fun find(): T? {
        // Apply all filters from the query builder
        queryHandle = queryBuilder.applyTo(queryHandle)
        val resultsHandle = RealmCoreInterop.executeQuery(queryHandle)
        val objectHandle = RealmCoreInterop.getResultsObject(resultsHandle, 0)
        
        return objectHandle?.let {
            // Create proper proxy object using the modern system
            io.realm.kotlin.modern.proxy.RealmObjectProxyFactory.createProxy(clazz, it)
        }
    }
}

/**
 * Modern implementation of RealmScalarQuery.
 */
public class ModernRealmScalarQueryImpl<T>(private val value: T) : RealmScalarQuery<T> {
    override fun find(): T {
        return value
    }
}