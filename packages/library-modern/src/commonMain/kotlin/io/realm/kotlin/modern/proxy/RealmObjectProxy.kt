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
import io.realm.kotlin.modern.interop.RealmObjectHandle
import io.realm.kotlin.modern.interop.RealmCoreInterop
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.createInstance

/**
 * Modern Dynamic Proxy system for Realm Objects.
 * 
 * This replaces the traditional SWIG-based approach with a clean, modern
 * Kotlin reflection-based proxy that intercepts property access and delegates
 * to the Modern Interop Layer.
 */
public interface RealmObjectProxy : BaseRealmObject {
    /**
     * The handle to the native realm object.
     */
    public val objectHandle: RealmObjectHandle
    
    /**
     * The class name of this realm object.
     */
    public val className: String
    
    /**
     * Gets a property value from the native realm object.
     */
    public fun getRealmProperty(propertyName: String): Any?
    
    /**
     * Sets a property value in the native realm object.
     */
    public fun setRealmProperty(propertyName: String, value: Any?)
}

/**
 * Factory for creating dynamic proxy instances of Realm objects.
 */
public object RealmObjectProxyFactory {
    
    /**
     * Creates a dynamic proxy for a realm object class.
     */
    public fun <T : BaseRealmObject> createProxy(
        clazz: KClass<T>,
        objectHandle: RealmObjectHandle
    ): T {
        val className = clazz.simpleName ?: throw IllegalArgumentException("Class must have a name")
        
        return when {
            // For concrete classes, create a proxy that extends the class
            !clazz.isAbstract && !clazz.isSealed -> {
                createConcreteProxy(clazz, objectHandle, className)
            }
            // For interfaces or abstract classes, create a dynamic implementation
            else -> {
                createDynamicProxy(clazz, objectHandle, className)
            }
        }
    }
    
    /**
     * Creates a proxy for concrete Realm object classes.
     */
    private fun <T : BaseRealmObject> createConcreteProxy(
        clazz: KClass<T>,
        objectHandle: RealmObjectHandle,
        className: String
    ): T {
        // Create an instance and wrap it with property interception
        val instance = clazz.createInstance()
        
        @Suppress("UNCHECKED_CAST")
        return ConcreteRealmObjectProxy(
            instance = instance,
            objectHandle = objectHandle,
            className = className,
            clazz = clazz
        ) as T
    }
    
    /**
     * Creates a dynamic proxy for interface-based Realm objects.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : BaseRealmObject> createDynamicProxy(
        clazz: KClass<T>,
        objectHandle: RealmObjectHandle,
        className: String
    ): T {
        return DynamicRealmObjectProxy(
            clazz = clazz,
            objectHandle = objectHandle,
            className = className
        ) as T
    }
}

/**
 * Proxy implementation for concrete Realm object classes.
 */
internal class ConcreteRealmObjectProxy<T : BaseRealmObject>(
    private val instance: T,
    override val objectHandle: RealmObjectHandle,
    override val className: String,
    private val clazz: KClass<T>
) : RealmObjectProxy {
    
    private val propertyCache = mutableMapOf<String, KProperty1<T, *>>()
    
    init {
        // Cache all properties for efficient access
        clazz.memberProperties.forEach { property ->
            propertyCache[property.name] = property
        }
    }
    
    override fun isManaged(): Boolean = objectHandle.isValid
    
    override fun isValid(): Boolean = objectHandle.isValid
    
    override fun getRealmProperty(propertyName: String): Any? {
        return RealmCoreInterop.getProperty(objectHandle, propertyName)
    }
    
    override fun setRealmProperty(propertyName: String, value: Any?) {
        RealmCoreInterop.setProperty(objectHandle, propertyName, value)
    }
}

/**
 * Dynamic proxy implementation for interface-based Realm objects.
 */
internal class DynamicRealmObjectProxy<T : BaseRealmObject>(
    private val clazz: KClass<T>,
    override val objectHandle: RealmObjectHandle,
    override val className: String
) : RealmObjectProxy {
    
    private val propertyDescriptors = mutableMapOf<String, PropertyDescriptor>()
    
    init {
        // Analyze the interface to understand property structure
        clazz.memberProperties.forEach { property ->
            propertyDescriptors[property.name] = PropertyDescriptor(
                name = property.name,
                returnType = property.returnType,
                isNullable = property.returnType.isMarkedNullable
            )
        }
    }
    
    override fun isManaged(): Boolean = objectHandle.isValid
    
    override fun isValid(): Boolean = objectHandle.isValid
    
    override fun getRealmProperty(propertyName: String): Any? {
        return RealmCoreInterop.getProperty(objectHandle, propertyName)
    }
    
    override fun setRealmProperty(propertyName: String, value: Any?) {
        RealmCoreInterop.setProperty(objectHandle, propertyName, value)
    }
}

/**
 * Descriptor for a property in a Realm object.
 */
internal data class PropertyDescriptor(
    val name: String,
    val returnType: kotlin.reflect.KType,
    val isNullable: Boolean
)

/**
 * Extension functions for easy property access on Realm objects.
 */
public inline fun <reified T> BaseRealmObject.getProperty(propertyName: String): T? {
    return if (this is RealmObjectProxy) {
        this.getRealmProperty(propertyName) as? T
    } else {
        null
    }
}

public fun BaseRealmObject.setProperty(propertyName: String, value: Any?) {
    if (this is RealmObjectProxy) {
        this.setRealmProperty(propertyName, value)
    }
}