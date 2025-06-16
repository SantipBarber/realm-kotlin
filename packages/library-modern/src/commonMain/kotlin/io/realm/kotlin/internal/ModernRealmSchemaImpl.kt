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

import io.realm.kotlin.schema.RealmClass
import io.realm.kotlin.schema.RealmSchema
import io.realm.kotlin.modern.interop.RealmCoreInterop
import io.realm.kotlin.modern.interop.RealmDatabaseHandle
import kotlin.reflect.KClass

/**
 * Modern implementation of RealmSchema using Modern Interop Layer.
 */
public class ModernRealmSchemaImpl(
    private val databaseHandle: RealmDatabaseHandle
) : RealmSchema {

    private val _classes: Set<RealmClass> by lazy {
        val classNames = RealmCoreInterop.getSchemaClasses(databaseHandle)
        classNames.map { className ->
            ModernRealmClassImpl(className, databaseHandle)
        }.toSet()
    }

    override val size: Int
        get() = _classes.size

    override fun contains(element: RealmClass): Boolean {
        return _classes.contains(element)
    }

    override fun containsAll(elements: Collection<RealmClass>): Boolean {
        return _classes.containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return _classes.isEmpty()
    }

    override fun iterator(): Iterator<RealmClass> {
        return _classes.iterator()
    }

    override fun get(className: String): RealmClass? {
        return _classes.find { it.name == className }
    }

    override fun get(clazz: KClass<*>): RealmClass? {
        return _classes.find { it.name == clazz.simpleName }
    }
}

/**
 * Modern implementation of RealmClass using Modern Interop Layer.
 */
public class ModernRealmClassImpl(
    override val name: String,
    private val databaseHandle: RealmDatabaseHandle
) : RealmClass {

    override val kind: io.realm.kotlin.schema.RealmClassKind = io.realm.kotlin.schema.RealmClassKind.STANDARD

    override val properties: Set<io.realm.kotlin.schema.RealmProperty> by lazy {
        RealmCoreInterop.getClassProperties(databaseHandle, name).map { propInfo ->
            ModernRealmPropertyImpl(propInfo)
        }.toSet()
    }

    override fun get(propertyName: String): io.realm.kotlin.schema.RealmProperty? {
        return properties.find { it.name == propertyName }
    }
}

/**
 * Modern implementation of RealmProperty using Modern Interop Layer.
 */
public class ModernRealmPropertyImpl(
    private val propertyInfo: io.realm.kotlin.modern.interop.RealmPropertyInfo
) : io.realm.kotlin.schema.RealmProperty {

    override val name: String = propertyInfo.name

    override val isNullable: Boolean = propertyInfo.isOptional

    override val isPrimaryKey: Boolean = propertyInfo.isPrimaryKey

    override val isIndexed: Boolean = propertyInfo.isIndexed

    override val type: io.realm.kotlin.schema.RealmPropertyType = when (propertyInfo.type) {
        io.realm.kotlin.modern.interop.RealmPropertyType.BOOLEAN -> io.realm.kotlin.schema.RealmPropertyType.BOOL
        io.realm.kotlin.modern.interop.RealmPropertyType.INT -> io.realm.kotlin.schema.RealmPropertyType.INT
        io.realm.kotlin.modern.interop.RealmPropertyType.LONG -> io.realm.kotlin.schema.RealmPropertyType.LONG
        io.realm.kotlin.modern.interop.RealmPropertyType.FLOAT -> io.realm.kotlin.schema.RealmPropertyType.FLOAT
        io.realm.kotlin.modern.interop.RealmPropertyType.DOUBLE -> io.realm.kotlin.schema.RealmPropertyType.DOUBLE
        io.realm.kotlin.modern.interop.RealmPropertyType.STRING -> io.realm.kotlin.schema.RealmPropertyType.STRING
        io.realm.kotlin.modern.interop.RealmPropertyType.BINARY -> io.realm.kotlin.schema.RealmPropertyType.BYTE_ARRAY
        io.realm.kotlin.modern.interop.RealmPropertyType.DATE -> io.realm.kotlin.schema.RealmPropertyType.INSTANT
        io.realm.kotlin.modern.interop.RealmPropertyType.OBJECT -> io.realm.kotlin.schema.RealmPropertyType.OBJECT
        io.realm.kotlin.modern.interop.RealmPropertyType.LIST -> io.realm.kotlin.schema.RealmPropertyType.LIST
        io.realm.kotlin.modern.interop.RealmPropertyType.SET -> io.realm.kotlin.schema.RealmPropertyType.SET
        io.realm.kotlin.modern.interop.RealmPropertyType.DICTIONARY -> io.realm.kotlin.schema.RealmPropertyType.MAP
    }
}