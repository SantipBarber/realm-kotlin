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

package io.realm.kotlin.modern.example

import io.realm.kotlin.types.BaseRealmObject
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.modern.proxy.*

/**
 * Example Realm object using the modern property delegate system.
 * 
 * This demonstrates how to write Realm objects with the new approach:
 * - Clean Kotlin classes with delegated properties
 * - Automatic persistence through property interception
 * - Type-safe property access
 */
public open class Person : RealmObject {
    public var name: String? by realmString()
    public var age: Int by realmInt()
    public var email: String? by realmString()
    public var isActive: Boolean by realmBoolean()
    public var height: Float by realmFloat()
    public var weight: Double by realmDouble()
    public var friends: List<Person> by realmListProperty()
    
    override fun isManaged(): Boolean = this is RealmObjectProxy
    override fun isValid(): Boolean = this is RealmObjectProxy && (this as RealmObjectProxy).objectHandle.isValid
}

/**
 * Example with custom primary key and indexed properties.
 */
public open class User : RealmObject {
    public var id: Long by realmLong()  // Primary key would be configured via annotation
    public var username: String? by realmString()  // Indexed
    public var fullName: String? by realmString()
    public var profilePicture: ByteArray? by realmNullableProperty()
    public var createdAt: Long by realmLong()
    public var updatedAt: Long by realmLong()
    
    override fun isManaged(): Boolean = this is RealmObjectProxy
    override fun isValid(): Boolean = this is RealmObjectProxy && (this as RealmObjectProxy).objectHandle.isValid
}

/**
 * Example of an embedded object (nested within other objects).
 */
public open class Address : RealmObject {
    public var street: String? by realmString()
    public var city: String? by realmString()
    public var country: String? by realmString()
    public var zipCode: String? by realmString()
    public var coordinates: Coordinates? by realmNullableProperty()
    
    override fun isManaged(): Boolean = this is RealmObjectProxy
    override fun isValid(): Boolean = this is RealmObjectProxy && (this as RealmObjectProxy).objectHandle.isValid
}

/**
 * Example with geographic data.
 */
public open class Coordinates : RealmObject {
    public var latitude: Double by realmDouble()
    public var longitude: Double by realmDouble()
    public var altitude: Double by realmDouble()
    
    override fun isManaged(): Boolean = this is RealmObjectProxy
    override fun isValid(): Boolean = this is RealmObjectProxy && (this as RealmObjectProxy).objectHandle.isValid
}

/**
 * Example with relationships between objects.
 */
public open class Company : RealmObject {
    public var name: String? by realmString()
    public var employees: List<Person> by realmListProperty()
    public var headquarters: Address? by realmNullableProperty()
    public var revenue: Double by realmDouble()
    public var foundedYear: Int by realmInt()
    
    override fun isManaged(): Boolean = this is RealmObjectProxy
    override fun isValid(): Boolean = this is RealmObjectProxy && (this as RealmObjectProxy).objectHandle.isValid
}

/**
 * Interface-based Realm object (demonstrates dynamic proxy capability).
 */
public interface Product : BaseRealmObject {
    public val id: String?
    public val name: String?
    public val price: Double
    public val category: String?
    public val inStock: Boolean
    public val tags: List<String>
}

/**
 * Dynamic implementation example - shows how the proxy system
 * can work with interfaces or abstract classes.
 */
public abstract class Vehicle : RealmObject {
    public abstract val make: String?
    public abstract val model: String?
    public abstract val year: Int
    public abstract val color: String?
    
    // Computed property (not persisted)
    public val displayName: String
        get() = "$make $model ($year)"
}

/**
 * Concrete implementation of abstract Realm object.
 */
public class Car : Vehicle() {
    public override var make: String? by realmString()
    public override var model: String? by realmString()
    public override var year: Int by realmInt()
    public override var color: String? by realmString()
    
    // Car-specific properties
    public var doors: Int by realmInt()
    public var engine: String? by realmString()
    public var transmission: String? by realmString()
    
    override fun isManaged(): Boolean = this is RealmObjectProxy
    override fun isValid(): Boolean = this is RealmObjectProxy && (this as RealmObjectProxy).objectHandle.isValid
}

/**
 * Example showing how to use the modern Realm objects in practice.
 */
public class RealmObjectExamples {
    
    public fun createPersonExample(): Person {
        val person = Person()
        // These property assignments will automatically delegate to Realm storage
        // when the object is managed by a Realm transaction
        person.name = "John Doe"
        person.age = 30
        person.email = "john.doe@example.com"
        person.isActive = true
        person.height = 180.5f
        person.weight = 75.0
        return person
    }
    
    public fun createCompanyExample(): Company {
        val company = Company()
        company.name = "Tech Innovations Inc."
        company.revenue = 1_000_000.0
        company.foundedYear = 2020
        
        val headquarters = Address()
        headquarters.street = "123 Tech Street"
        headquarters.city = "San Francisco"
        headquarters.country = "USA"
        headquarters.zipCode = "94105"
        
        company.headquarters = headquarters
        return company
    }
}

/**
 * Example usage with the Modern Realm API.
 */
public suspend fun exampleUsage() {
    // This would be how developers use the modern Realm SDK
    val config = io.realm.kotlin.RealmConfiguration.Builder()
        .name("modern-example.realm")
        .build()
    
    val realm = io.realm.kotlin.Realm.open(config)
    
    // Write transaction using modern syntax
    realm.write {
        val person = Person()
        person.name = "Alice Smith"
        person.age = 28
        person.email = "alice@example.com"
        person.isActive = true
        
        // Properties are automatically persisted through the proxy system
        copyToRealm(person)
    }
    
    // Query using modern API
    val results = realm.query(Person::class, "age > 25").find()
    for (person in results) {
        println("Found: ${person.name}, age ${person.age}")
        // Property access automatically delegates to Realm storage
    }
    
    realm.close()
}