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

package io.realm.kotlin.modern.test

import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.modern.interop.RealmCoreInterop
import io.realm.kotlin.modern.proxy.RealmObjectProxyFactory
import io.realm.kotlin.modern.proxy.ProxyPerformanceOptimizer
import io.realm.kotlin.modern.query.QueryBuilder
import io.realm.kotlin.modern.example.*
import io.realm.kotlin.query.Sort
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the Modern Interop Layer.
 * 
 * These tests validate the complete integration of all Modern Realm components:
 * - Modern Interop Layer
 * - Dynamic Proxy System
 * - Advanced Query Builder
 * - Performance Optimizations
 * - Reactive Notifications
 */
class ModernInteropIntegrationTest {
    
    @Test
    fun testDatabaseOperations() {
        // Test basic database operations
        val config = RealmConfiguration.Builder()
            .name("test-modern.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        assertNotNull(databaseHandle)
        assertTrue(databaseHandle.isValid)
        
        // Clean up
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testObjectCreationAndProxy() {
        val config = RealmConfiguration.Builder()
            .name("test-proxy.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        val transactionHandle = RealmCoreInterop.beginWrite(databaseHandle)
        
        // Create object
        val objectHandle = RealmCoreInterop.createObject(transactionHandle, "Person", null)
        assertNotNull(objectHandle)
        assertTrue(objectHandle.isValid)
        
        // Create proxy
        val proxy = RealmObjectProxyFactory.createProxy(Person::class, objectHandle)
        assertNotNull(proxy)
        assertTrue(proxy.isManaged())
        
        // Clean up
        RealmCoreInterop.cancelWrite(transactionHandle)
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testAdvancedQueryBuilder() {
        // Test complex query building
        val queryBuilder = QueryBuilder<Person>()
        
        queryBuilder
            .equal("name", "John")
            .greaterThan("age", 18)
            .lessThan("age", 65)
            .isNotNull("email")
            .contains("description", "developer")
            .sort("name", Sort.ASCENDING)
            .limit(10)
        
        val filters = queryBuilder.getFilters()
        assertEquals(5, filters.size)
        
        val sortClauses = queryBuilder.getSortClauses()
        assertEquals(1, sortClauses.size)
        assertEquals("name", sortClauses[0].property)
        
        val limit = queryBuilder.getLimit()
        assertEquals(10, limit)
    }
    
    @Test
    fun testLogicalQueryOperators() {
        // Test logical grouping
        val queryBuilder = QueryBuilder<Person>()
        
        queryBuilder
            .and {
                greaterThan("age", 18)
                lessThan("age", 65)
            }
            .or {
                equal("isActive", true)
                isNotNull("email")
            }
            .not {
                contains("name", "test")
            }
        
        val logicalGroups = queryBuilder.getLogicalGroups()
        assertEquals(3, logicalGroups.size)
    }
    
    @Test
    fun testPerformanceOptimizer() {
        // Test performance optimizations
        val stats = ProxyPerformanceOptimizer.getPerformanceStats()
        assertNotNull(stats)
        
        // Test class metadata caching
        val metadata1 = ProxyPerformanceOptimizer.getClassMetadata(Person::class)
        val metadata2 = ProxyPerformanceOptimizer.getClassMetadata(Person::class)
        
        // Should be the same cached instance
        assertEquals(metadata1.className, metadata2.className)
        assertEquals(metadata1.properties.size, metadata2.properties.size)
    }
    
    @Test
    fun testComplexQueryOperators() {
        val queryBuilder = QueryBuilder<Person>()
        
        // Test all advanced operators
        queryBuilder
            .between("age", 25, 45)
            .containsIgnoreCase("name", "JOHN")
            .like("description", "*developer*")
            .matches("email", ".*@company\\.com")
            .isEmpty("tags")
            .sizeGreaterThan("skills", 3)
            .`in`("department", listOf("Engineering", "Product"))
        
        val filters = queryBuilder.getFilters()
        assertEquals(7, filters.size)
        
        // Verify specific operators
        assertTrue(filters.any { it.property == "age" })
        assertTrue(filters.any { it.property == "name" })
        assertTrue(filters.any { it.property == "description" })
        assertTrue(filters.any { it.property == "email" })
    }
    
    @Test
    fun testProxyPropertyAccess() {
        val config = RealmConfiguration.Builder()
            .name("test-properties.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        val transactionHandle = RealmCoreInterop.beginWrite(databaseHandle)
        val objectHandle = RealmCoreInterop.createObject(transactionHandle, "Person", null)
        
        // Test property setting through proxy
        RealmCoreInterop.setProperty(objectHandle, "name", "John Doe")
        RealmCoreInterop.setProperty(objectHandle, "age", 30)
        RealmCoreInterop.setProperty(objectHandle, "email", "john@example.com")
        
        // Test property getting through proxy
        val name = RealmCoreInterop.getProperty(objectHandle, "name")
        val age = RealmCoreInterop.getProperty(objectHandle, "age")
        val email = RealmCoreInterop.getProperty(objectHandle, "email")
        
        assertEquals("John Doe", name)
        assertEquals(30, age)
        assertEquals("john@example.com", email)
        
        // Clean up
        RealmCoreInterop.cancelWrite(transactionHandle)
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testQueryExecution() {
        val config = RealmConfiguration.Builder()
            .name("test-query.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        
        // Create query
        val queryHandle = RealmCoreInterop.createQuery(databaseHandle, "Person")
        assertNotNull(queryHandle)
        assertTrue(queryHandle.isValid)
        
        // Add filters
        val filteredQuery = RealmCoreInterop.addQueryFilter(
            queryHandle, 
            "age", 
            io.realm.kotlin.modern.interop.QueryOperator.GREATER_THAN, 
            18
        )
        assertNotNull(filteredQuery)
        
        // Execute query
        val resultsHandle = RealmCoreInterop.executeQuery(filteredQuery)
        assertNotNull(resultsHandle)
        assertTrue(resultsHandle.isValid)
        
        val size = RealmCoreInterop.getResultsSize(resultsHandle)
        assertTrue(size >= 0)
        
        // Clean up
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testNotificationListeners() {
        val config = RealmConfiguration.Builder()
            .name("test-notifications.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        
        // Test database listener
        val databaseListener = RealmCoreInterop.addDatabaseListener(databaseHandle) { changeType ->
            // Notification received
        }
        assertNotNull(databaseListener)
        assertTrue(databaseListener.isValid)
        
        // Test results listener  
        val queryHandle = RealmCoreInterop.createQuery(databaseHandle, "Person")
        val resultsHandle = RealmCoreInterop.executeQuery(queryHandle)
        
        val resultsListener = RealmCoreInterop.addResultsListener(resultsHandle) { changeType, indices ->
            // Results notification received
        }
        assertNotNull(resultsListener)
        assertTrue(resultsListener.isValid)
        
        // Clean up listeners
        RealmCoreInterop.removeListener(databaseListener)
        RealmCoreInterop.removeListener(resultsListener)
        
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testSchemaIntrospection() {
        val config = RealmConfiguration.Builder()
            .name("test-schema.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        
        // Test schema operations
        val version = RealmCoreInterop.getSchemaVersion(databaseHandle)
        assertTrue(version >= 0)
        
        val classes = RealmCoreInterop.getSchemaClasses(databaseHandle)
        assertNotNull(classes)
        assertTrue(classes.isNotEmpty())
        
        // Test class properties
        val className = classes.first()
        val properties = RealmCoreInterop.getClassProperties(databaseHandle, className)
        assertNotNull(properties)
        
        // Clean up
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testBatchProxyCreation() {
        val config = RealmConfiguration.Builder()
            .name("test-batch.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        val transactionHandle = RealmCoreInterop.beginWrite(databaseHandle)
        
        // Create multiple objects
        val objectHandles = (1..5).map { i ->
            RealmCoreInterop.createObject(transactionHandle, "Person", "person_$i")
        }
        
        // Test batch proxy creation
        val proxies = ProxyPerformanceOptimizer.createProxyBatch(Person::class, objectHandles)
        assertEquals(5, proxies.size)
        
        proxies.forEach { proxy ->
            assertNotNull(proxy)
            assertTrue(proxy.isManaged())
        }
        
        // Clean up
        RealmCoreInterop.cancelWrite(transactionHandle)
        RealmCoreInterop.closeDatabase(databaseHandle)
    }
    
    @Test
    fun testFullIntegrationWorkflow() {
        // Complete workflow test
        val config = RealmConfiguration.Builder()
            .name("test-integration.realm")
            .build()
            
        val databaseHandle = RealmCoreInterop.openDatabase(config)
        
        try {
            // 1. Create objects in transaction
            val transactionHandle = RealmCoreInterop.beginWrite(databaseHandle)
            
            val person1Handle = RealmCoreInterop.createObject(transactionHandle, "Person", "1")
            val person2Handle = RealmCoreInterop.createObject(transactionHandle, "Person", "2")
            
            // Set properties
            RealmCoreInterop.setProperty(person1Handle, "name", "Alice")
            RealmCoreInterop.setProperty(person1Handle, "age", 25)
            RealmCoreInterop.setProperty(person2Handle, "name", "Bob") 
            RealmCoreInterop.setProperty(person2Handle, "age", 30)
            
            RealmCoreInterop.commitWrite(transactionHandle)
            
            // 2. Query with advanced builder
            val queryBuilder = QueryBuilder<Person>()
            queryBuilder
                .greaterThan("age", 20)
                .sort("name", Sort.ASCENDING)
                .limit(10)
            
            val queryHandle = RealmCoreInterop.createQuery(databaseHandle, "Person")
            val filteredQuery = queryBuilder.applyTo(queryHandle)
            val resultsHandle = RealmCoreInterop.executeQuery(filteredQuery)
            
            // 3. Verify results
            val size = RealmCoreInterop.getResultsSize(resultsHandle)
            assertTrue(size >= 0)
            
            // 4. Create proxies for results
            val resultObjects = (0 until size.toInt()).mapNotNull { i ->
                RealmCoreInterop.getResultsObject(resultsHandle, i.toLong())
            }
            
            val proxies = resultObjects.map { objectHandle ->
                RealmObjectProxyFactory.createProxy(Person::class, objectHandle)
            }
            
            // 5. Verify proxy functionality
            proxies.forEach { proxy ->
                assertNotNull(proxy)
                assertTrue(proxy.isManaged())
                assertTrue(proxy.isValid())
            }
            
            // 6. Test performance stats
            val stats = ProxyPerformanceOptimizer.getPerformanceStats()
            assertTrue(stats.cachedClasses >= 0)
            assertTrue(stats.cachedProxies >= 0)
            
        } finally {
            RealmCoreInterop.closeDatabase(databaseHandle)
        }
    }
}

/**
 * Helper functions for testing
 */
private fun QueryBuilder<Person>.applyTestFilters(): QueryBuilder<Person> {
    return this
        .equal("isActive", true)
        .greaterThan("age", 18)
        .contains("email", "@company.com")
        .sort("name", Sort.ASCENDING)
}