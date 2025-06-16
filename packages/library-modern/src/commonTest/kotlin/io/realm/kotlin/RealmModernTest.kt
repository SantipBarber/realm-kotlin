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

import io.realm.kotlin.types.RealmObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Basic tests for the Modern Realm implementation.
 */
class RealmModernTest {

    @Test
    fun testRealmConfigurationBuilder() {
        val config = RealmConfiguration.Builder()
            .name("test.realm")
            .schemaVersion(1L)
            .build()

        assertEquals("test.realm", config.name)
        assertEquals(1L, config.schemaVersion)
        assertNotNull(config.schema)
    }

    @Test
    fun testRealmOpen() {
        val config = RealmConfiguration.Builder()
            .name("test-open.realm")
            .build()

        val realm = Realm.open(config)
        assertNotNull(realm)
        assertEquals(config, realm.configuration)
        assertTrue(realm.schema.isEmpty())
        realm.close()
    }

    @Test
    fun testRealmQuery() {
        val config = RealmConfiguration.Builder()
            .name("test-query.realm")
            .build()

        val realm = Realm.open(config)
        val query = realm.query(TestRealmObject::class)
        assertNotNull(query)
        
        val results = query.find()
        assertNotNull(results)
        assertTrue(results.isEmpty())
        
        realm.close()
    }

    @Test
    fun testRealmWrite() {
        val config = RealmConfiguration.Builder()
            .name("test-write.realm")
            .build()

        val realm = Realm.open(config)
        
        // Test write blocking
        val result = realm.writeBlocking {
            // This should not throw since we're not actually creating objects yet
            this.schema.size
        }
        
        assertEquals(0, result)
        realm.close()
    }

    @Test
    fun testRealmCompanionFunctions() {
        val config = RealmConfiguration.Builder()
            .name("test-companion.realm")
            .build()

        // Test compact
        val compactResult = Realm.compactRealm(config)
        assertTrue(compactResult)

        // Test delete (should not throw)
        Realm.deleteRealm(config)
    }
}

/**
 * Test realm object for testing purposes.
 */
class TestRealmObject : RealmObject {
    override fun isManaged(): Boolean = false
    override fun isValid(): Boolean = true
}