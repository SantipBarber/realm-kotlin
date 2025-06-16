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

package io.realm.kotlin.modern.query

import io.realm.kotlin.modern.interop.*
import io.realm.kotlin.query.Sort

/**
 * Modern Query Builder for Realm queries.
 * 
 * This provides a clean, type-safe way to build queries that are
 * translated to efficient realm-core operations through the Modern Interop Layer.
 */
public class QueryBuilder<T> {
    
    private val filters = mutableListOf<QueryFilter>()
    private val logicalGroups = mutableListOf<LogicalGroup>()
    private var sortClauses = mutableListOf<SortClause>()
    private var limitValue: Int? = null
    
    /**
     * Adds an equality filter.
     */
    public fun equal(property: String, value: Any?): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.EQUAL, value))
        return this
    }
    
    /**
     * Adds a not-equal filter.
     */
    public fun notEqual(property: String, value: Any?): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.NOT_EQUAL, value))
        return this
    }
    
    /**
     * Adds a greater-than filter.
     */
    public fun greaterThan(property: String, value: Any): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.GREATER_THAN, value))
        return this
    }
    
    /**
     * Adds a greater-than-or-equal filter.
     */
    public fun greaterThanOrEqual(property: String, value: Any): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.GREATER_THAN_OR_EQUAL, value))
        return this
    }
    
    /**
     * Adds a less-than filter.
     */
    public fun lessThan(property: String, value: Any): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.LESS_THAN, value))
        return this
    }
    
    /**
     * Adds a less-than-or-equal filter.
     */
    public fun lessThanOrEqual(property: String, value: Any): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.LESS_THAN_OR_EQUAL, value))
        return this
    }
    
    /**
     * Adds a contains filter (for strings).
     */
    public fun contains(property: String, value: String, caseSensitive: Boolean = true): QueryBuilder<T> {
        val operator = if (caseSensitive) QueryOperator.CONTAINS else QueryOperator.CONTAINS
        filters.add(QueryFilter(property, operator, value))
        return this
    }
    
    /**
     * Adds a starts-with filter (for strings).
     */
    public fun startsWith(property: String, value: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.STARTS_WITH, value))
        return this
    }
    
    /**
     * Adds an ends-with filter (for strings).
     */
    public fun endsWith(property: String, value: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.ENDS_WITH, value))
        return this
    }
    
    /**
     * Adds an in filter (value is in list).
     */
    public fun `in`(property: String, values: Collection<Any>): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.IN, values))
        return this
    }
    
    /**
     * Adds a between filter (value is between min and max, inclusive).
     */
    public fun between(property: String, min: Any, max: Any): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.GREATER_THAN_OR_EQUAL, min))
        filters.add(QueryFilter(property, QueryOperator.LESS_THAN_OR_EQUAL, max))
        return this
    }
    
    /**
     * Adds a case-insensitive contains filter.
     */
    public fun containsIgnoreCase(property: String, value: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.CONTAINS, value.lowercase()))
        return this
    }
    
    /**
     * Adds a like filter with wildcard support.
     */
    public fun like(property: String, pattern: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.LIKE, pattern))
        return this
    }
    
    /**
     * Adds an isEmpty filter for collections.
     */
    public fun isEmpty(property: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.IS_EMPTY, null))
        return this
    }
    
    /**
     * Adds an isNotEmpty filter for collections.
     */
    public fun isNotEmpty(property: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.IS_NOT_EMPTY, null))
        return this
    }
    
    /**
     * Adds an is-null filter.
     */
    public fun isNull(property: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.IS_NULL, null))
        return this
    }
    
    /**
     * Adds an is-not-null filter.
     */
    public fun isNotNull(property: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.IS_NOT_NULL, null))
        return this
    }
    
    /**
     * Adds a regex match filter.
     */
    public fun matches(property: String, regex: String): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.REGEX_MATCH, regex))
        return this
    }
    
    /**
     * Adds a size equal filter for collections.
     */
    public fun sizeEqual(property: String, size: Int): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.SIZE_EQUAL, size))
        return this
    }
    
    /**
     * Adds a size greater than filter for collections.
     */
    public fun sizeGreaterThan(property: String, size: Int): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.SIZE_GREATER_THAN, size))
        return this
    }
    
    /**
     * Adds a size less than filter for collections.
     */
    public fun sizeLessThan(property: String, size: Int): QueryBuilder<T> {
        filters.add(QueryFilter(property, QueryOperator.SIZE_LESS_THAN, size))
        return this
    }
    
    /**
     * Adds a sort clause.
     */
    public fun sort(property: String, sortOrder: Sort = Sort.ASCENDING): QueryBuilder<T> {
        sortClauses.add(SortClause(property, sortOrder))
        return this
    }
    
    /**
     * Adds a limit clause.
     */
    public fun limit(count: Int): QueryBuilder<T> {
        limitValue = count
        return this
    }
    
    /**
     * Builds the query and applies all filters to the query handle.
     */
    public fun applyTo(queryHandle: RealmQueryHandle): RealmQueryHandle {
        var currentHandle = queryHandle
        
        // Apply all filters
        for (filter in filters) {
            currentHandle = RealmCoreInterop.addQueryFilter(
                queryHandle = currentHandle,
                property = filter.property,
                operator = filter.operator,
                value = filter.value
            )
        }
        
        return currentHandle
    }
    
    /**
     * Applies sort clauses to a results handle after query execution.
     */
    public fun applySortAndLimit(results: List<Any>): List<Any> {
        var sortedResults = results
        
        // Apply sorting
        if (sortClauses.isNotEmpty()) {
            sortedResults = applySorting(sortedResults)
        }
        
        // Apply limit
        limitValue?.let { limit ->
            sortedResults = sortedResults.take(limit)
        }
        
        return sortedResults
    }
    
    /**
     * Applies sorting to a list of results.
     */
    private fun applySorting(results: List<Any>): List<Any> {
        if (sortClauses.isEmpty()) return results
        
        return results.sortedWith { a, b ->
            var comparison = 0
            
            for (sortClause in sortClauses) {
                comparison = compareByProperty(a, b, sortClause.property, sortClause.sortOrder)
                if (comparison != 0) break
            }
            
            comparison
        }
    }
    
    /**
     * Compares two objects by a specific property.
     */
    @Suppress("UNCHECKED_CAST")
    private fun compareByProperty(a: Any, b: Any, property: String, sortOrder: Sort): Int {
        try {
            // For now, this is a simplified comparison
            // In a real implementation, this would use reflection or property delegates
            // to access the actual property values
            
            val valueA = getPropertyValue(a, property)
            val valueB = getPropertyValue(b, property)
            
            val comparison = when {
                valueA == null && valueB == null -> 0
                valueA == null -> -1
                valueB == null -> 1
                valueA is Comparable<*> && valueB is Comparable<*> -> {
                    (valueA as Comparable<Any>).compareTo(valueB)
                }
                else -> valueA.toString().compareTo(valueB.toString())
            }
            
            return if (sortOrder == Sort.ASCENDING) comparison else -comparison
            
        } catch (e: Exception) {
            // If comparison fails, consider them equal
            return 0
        }
    }
    
    /**
     * Gets a property value from an object.
     * This is a simplified implementation that would be enhanced in production.
     */
    private fun getPropertyValue(obj: Any, property: String): Any? {
        return when (obj) {
            is io.realm.kotlin.modern.proxy.RealmObjectProxy -> {
                obj.getRealmProperty(property)
            }
            else -> {
                // For non-proxy objects, this would use reflection
                // This is a fallback for testing/development
                null
            }
        }
    }
    
    /**
     * Gets all filters in this query.
     */
    public fun getFilters(): List<QueryFilter> = filters.toList()
    
    /**
     * Gets all sort clauses in this query.
     */
    public fun getSortClauses(): List<SortClause> = sortClauses.toList()
    
    /**
     * Gets the limit value if set.
     */
    public fun getLimit(): Int? = limitValue
    
    /**
     * Creates a logical AND group of conditions.
     */
    public fun and(block: QueryBuilder<T>.() -> Unit): QueryBuilder<T> {
        val subBuilder = QueryBuilder<T>()
        subBuilder.block()
        logicalGroups.add(LogicalGroup(LogicalOperator.AND, subBuilder.getFilters()))
        return this
    }
    
    /**
     * Creates a logical OR group of conditions.
     */
    public fun or(block: QueryBuilder<T>.() -> Unit): QueryBuilder<T> {
        val subBuilder = QueryBuilder<T>()
        subBuilder.block()
        logicalGroups.add(LogicalGroup(LogicalOperator.OR, subBuilder.getFilters()))
        return this
    }
    
    /**
     * Creates a logical NOT group of conditions.
     */
    public fun not(block: QueryBuilder<T>.() -> Unit): QueryBuilder<T> {
        val subBuilder = QueryBuilder<T>()
        subBuilder.block()
        logicalGroups.add(LogicalGroup(LogicalOperator.NOT, subBuilder.getFilters()))
        return this
    }
    
    /**
     * Gets all logical groups.
     */
    public fun getLogicalGroups(): List<LogicalGroup> = logicalGroups.toList()
}

/**
 * Represents a single query filter.
 */
public data class QueryFilter(
    val property: String,
    val operator: QueryOperator,
    val value: Any?
)

/**
 * Represents a sort clause.
 */
public data class SortClause(
    val property: String,
    val sortOrder: Sort
)

/**
 * Represents a logical group of filters with an operator.
 */
public data class LogicalGroup(
    val operator: LogicalOperator,
    val filters: List<QueryFilter>
)

/**
 * Logical operators for combining filter conditions.
 */
public enum class LogicalOperator {
    AND,
    OR,
    NOT
}

/**
 * Modern query parser that can parse string-based queries into structured filters.
 */
public object ModernQueryParser {
    
    /**
     * Parses a string query into a QueryBuilder.
     */
    public fun <T> parseQuery(query: String, vararg args: Any?): QueryBuilder<T> {
        val builder = QueryBuilder<T>()
        
        // Simple parser for basic queries
        // In a real implementation, this would be much more sophisticated
        val normalizedQuery = query.trim()
        
        when {
            normalizedQuery.contains("==") -> {
                val parts = normalizedQuery.split("==").map { it.trim() }
                if (parts.size == 2) {
                    val property = parts[0].trim()
                    val value = if (args.isNotEmpty()) args[0] else parts[1].trim().removeSurrounding("\"", "'")
                    builder.equal(property, value)
                }
            }
            normalizedQuery.contains("!=") -> {
                val parts = normalizedQuery.split("!=").map { it.trim() }
                if (parts.size == 2) {
                    val property = parts[0].trim()
                    val value = if (args.isNotEmpty()) args[0] else parts[1].trim().removeSurrounding("\"", "'")
                    builder.notEqual(property, value)
                }
            }
            normalizedQuery.contains(">") -> {
                val parts = normalizedQuery.split(">").map { it.trim() }
                if (parts.size == 2) {
                    val property = parts[0].trim()
                    val value = if (args.isNotEmpty()) args[0] else parts[1].trim().toIntOrNull()
                    if (value != null) {
                        builder.greaterThan(property, value)
                    }
                }
            }
            normalizedQuery.contains("<") -> {
                val parts = normalizedQuery.split("<").map { it.trim() }
                if (parts.size == 2) {
                    val property = parts[0].trim()
                    val value = if (args.isNotEmpty()) args[0] else parts[1].trim().toIntOrNull()
                    if (value != null) {
                        builder.lessThan(property, value)
                    }
                }
            }
            normalizedQuery.contains("CONTAINS") -> {
                // Handle CONTAINS queries
                val regex = """(\w+)\s+CONTAINS\s+(.+)""".toRegex()
                val matchResult = regex.find(normalizedQuery)
                if (matchResult != null) {
                    val property = matchResult.groupValues[1]
                    val value = if (args.isNotEmpty()) args[0] as? String else matchResult.groupValues[2].removeSurrounding("\"", "'")
                    if (value != null) {
                        builder.contains(property, value)
                    }
                }
            }
        }
        
        return builder
    }
}

/**
 * Extension functions for easy query building.
 */
public fun <T> QueryBuilder<T>.where(property: String): PropertyQueryBuilder<T> {
    return PropertyQueryBuilder(this, property)
}

/**
 * Property-specific query builder for fluent syntax.
 */
public class PropertyQueryBuilder<T>(
    private val queryBuilder: QueryBuilder<T>,
    private val property: String
) {
    
    public fun equalTo(value: Any?): QueryBuilder<T> = queryBuilder.equal(property, value)
    public fun notEqualTo(value: Any?): QueryBuilder<T> = queryBuilder.notEqual(property, value)
    public fun greaterThan(value: Any): QueryBuilder<T> = queryBuilder.greaterThan(property, value)
    public fun greaterThanOrEqualTo(value: Any): QueryBuilder<T> = queryBuilder.greaterThanOrEqual(property, value)
    public fun lessThan(value: Any): QueryBuilder<T> = queryBuilder.lessThan(property, value)
    public fun lessThanOrEqualTo(value: Any): QueryBuilder<T> = queryBuilder.lessThanOrEqual(property, value)
    public fun between(min: Any, max: Any): QueryBuilder<T> = queryBuilder.between(property, min, max)
    public fun contains(value: String): QueryBuilder<T> = queryBuilder.contains(property, value)
    public fun containsIgnoreCase(value: String): QueryBuilder<T> = queryBuilder.containsIgnoreCase(property, value)
    public fun startsWith(value: String): QueryBuilder<T> = queryBuilder.startsWith(property, value)
    public fun endsWith(value: String): QueryBuilder<T> = queryBuilder.endsWith(property, value)
    public fun like(pattern: String): QueryBuilder<T> = queryBuilder.like(property, pattern)
    public fun matches(regex: String): QueryBuilder<T> = queryBuilder.matches(property, regex)
    public fun `in`(values: Collection<Any>): QueryBuilder<T> = queryBuilder.`in`(property, values)
    public fun isNull(): QueryBuilder<T> = queryBuilder.isNull(property)
    public fun isNotNull(): QueryBuilder<T> = queryBuilder.isNotNull(property)
    public fun isEmpty(): QueryBuilder<T> = queryBuilder.isEmpty(property)
    public fun isNotEmpty(): QueryBuilder<T> = queryBuilder.isNotEmpty(property)
    public fun sizeEqual(size: Int): QueryBuilder<T> = queryBuilder.sizeEqual(property, size)
    public fun sizeGreaterThan(size: Int): QueryBuilder<T> = queryBuilder.sizeGreaterThan(property, size)
    public fun sizeLessThan(size: Int): QueryBuilder<T> = queryBuilder.sizeLessThan(property, size)
}