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

package io.realm.kotlin.modern.proxy

import io.realm.kotlin.types.BaseRealmObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate that intercepts property access and delegates to Realm storage.
 * 
 * This allows Realm objects to be written as normal Kotlin classes with delegated
 * properties that automatically sync with the native realm storage.
 */
public class RealmProperty<T> : ReadWriteProperty<BaseRealmObject, T> {
    
    override fun getValue(thisRef: BaseRealmObject, property: KProperty<*>): T {
        return if (thisRef is RealmObjectProxy) {
            @Suppress("UNCHECKED_CAST")
            thisRef.getRealmProperty(property.name) as T
        } else {
            throw IllegalStateException("RealmProperty can only be used with RealmObjectProxy instances")
        }
    }
    
    override fun setValue(thisRef: BaseRealmObject, property: KProperty<*>, value: T) {
        if (thisRef is RealmObjectProxy) {
            thisRef.setRealmProperty(property.name, value)
        } else {
            throw IllegalStateException("RealmProperty can only be used with RealmObjectProxy instances")
        }
    }
}

/**
 * Nullable property delegate for optional Realm properties.
 */
public class RealmNullableProperty<T> : ReadWriteProperty<BaseRealmObject, T?> {
    
    override fun getValue(thisRef: BaseRealmObject, property: KProperty<*>): T? {
        return if (thisRef is RealmObjectProxy) {
            @Suppress("UNCHECKED_CAST")
            thisRef.getRealmProperty(property.name) as? T
        } else {
            throw IllegalStateException("RealmNullableProperty can only be used with RealmObjectProxy instances")
        }
    }
    
    override fun setValue(thisRef: BaseRealmObject, property: KProperty<*>, value: T?) {
        if (thisRef is RealmObjectProxy) {
            thisRef.setRealmProperty(property.name, value)
        } else {
            throw IllegalStateException("RealmNullableProperty can only be used with RealmObjectProxy instances")
        }
    }
}

/**
 * List property delegate for Realm list properties.
 */
public class RealmListProperty<T> : ReadWriteProperty<BaseRealmObject, List<T>> {
    
    override fun getValue(thisRef: BaseRealmObject, property: KProperty<*>): List<T> {
        return if (thisRef is RealmObjectProxy) {
            @Suppress("UNCHECKED_CAST")
            thisRef.getRealmProperty(property.name) as? List<T> ?: emptyList()
        } else {
            throw IllegalStateException("RealmListProperty can only be used with RealmObjectProxy instances")
        }
    }
    
    override fun setValue(thisRef: BaseRealmObject, property: KProperty<*>, value: List<T>) {
        if (thisRef is RealmObjectProxy) {
            thisRef.setRealmProperty(property.name, value)
        } else {
            throw IllegalStateException("RealmListProperty can only be used with RealmObjectProxy instances")
        }
    }
}

/**
 * Convenience functions for creating property delegates.
 */
public fun <T> realmProperty(): RealmProperty<T> = RealmProperty()

public fun <T> realmNullableProperty(): RealmNullableProperty<T> = RealmNullableProperty()

public fun <T> realmListProperty(): RealmListProperty<T> = RealmListProperty()

/**
 * Type-safe property delegates for common Realm types.
 */
public fun realmString(): RealmNullableProperty<String> = RealmNullableProperty()
public fun realmInt(): RealmProperty<Int> = RealmProperty()
public fun realmLong(): RealmProperty<Long> = RealmProperty()
public fun realmFloat(): RealmProperty<Float> = RealmProperty()
public fun realmDouble(): RealmProperty<Double> = RealmProperty()
public fun realmBoolean(): RealmProperty<Boolean> = RealmProperty()

/**
 * Property interceptor that analyzes and instruments Realm object classes.
 */
public object PropertyInterceptorManager {
    
    private val instrumentedClasses = mutableSetOf<String>()
    
    /**
     * Instruments a Realm object class for property interception.
     */
    public fun instrumentClass(className: String) {
        if (instrumentedClasses.add(className)) {
            // In a full implementation, this would use bytecode manipulation
            // or compiler plugin integration to instrument the class
            // For now, we rely on manual use of property delegates
        }
    }
    
    /**
     * Checks if a class has been instrumented for Realm property access.
     */
    public fun isInstrumented(className: String): Boolean {
        return instrumentedClasses.contains(className)
    }
    
    /**
     * Creates a property descriptor from a property delegate.
     */
    internal fun createPropertyDescriptor(
        propertyName: String,
        propertyType: kotlin.reflect.KType,
        delegate: Any
    ): PropertyDescriptor {
        return PropertyDescriptor(
            name = propertyName,
            returnType = propertyType,
            isNullable = propertyType.isMarkedNullable
        )
    }
}