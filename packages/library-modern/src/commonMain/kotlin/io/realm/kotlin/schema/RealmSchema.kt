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

package io.realm.kotlin.schema

import kotlin.reflect.KClass

/**
 * Represents the schema of a Realm, containing all class definitions.
 */
public interface RealmSchema : Set<RealmClass> {
    
    /**
     * Gets the RealmClass for the given class name.
     */
    public operator fun get(className: String): RealmClass?
    
    /**
     * Gets the RealmClass for the given class.
     */
    public operator fun get(clazz: KClass<*>): RealmClass?
}

/**
 * Represents a class in the Realm schema.
 */
public interface RealmClass {
    
    /**
     * The name of the class.
     */
    public val name: String
    
    /**
     * The kind of class (standard, embedded, etc.).
     */
    public val kind: RealmClassKind
    
    /**
     * The properties of this class.
     */
    public val properties: Set<RealmProperty>
    
    /**
     * Gets a property by name.
     */
    public operator fun get(propertyName: String): RealmProperty?
}

/**
 * Represents a property in a Realm class.
 */
public interface RealmProperty {
    
    /**
     * The name of the property.
     */
    public val name: String
    
    /**
     * The type of the property.
     */
    public val type: RealmPropertyType
    
    /**
     * Whether the property is nullable.
     */
    public val isNullable: Boolean
    
    /**
     * Whether the property is a primary key.
     */
    public val isPrimaryKey: Boolean
    
    /**
     * Whether the property is indexed.
     */
    public val isIndexed: Boolean
}

/**
 * Enum representing the kind of Realm class.
 */
public enum class RealmClassKind {
    STANDARD,
    EMBEDDED
}

/**
 * Enum representing the type of a Realm property.
 */
public enum class RealmPropertyType {
    INT,
    LONG,
    FLOAT,
    DOUBLE,
    BOOL,
    STRING,
    BINARY,
    BYTE_ARRAY,
    INSTANT,
    OBJECT,
    LIST,
    SET,
    MAP
}