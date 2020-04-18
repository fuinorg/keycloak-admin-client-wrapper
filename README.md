# keycloak-admin-client-wrapper
Small wrapper around the [Keycloak Admin Client](https://www.keycloak.org/keycloak-admin-client/index.html) that simplifies some tasks.

[![Build Status](https://jenkins.fuin.org/job/keycloak-admin-client-wrapper/badge/icon)](https://jenkins.fuin.org/job/keycloak-admin-client-wrapper/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fuin/keycloak-admin-client-wrapper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fuin/keycloak-admin-client-wrapper/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 11](https://img.shields.io/badge/JDK-11-green.svg)](https://openjdk.java.net/projects/jdk/11/)

## Realm Example

```Java
// Create realm wrapper
Realm realm = new Realm(keycloak);

// Find a realm by it's name (null if not found)
RealmRepresentation foundRealm = realm.find("test1");

// Create a realm and enable it
RealmResource createdRealm = realm.create("test2", true);

// Find a realm or create it in case it was not found
RealmResource foundOrCreatedRealm = realm.findOrCreate("test3", true);
```

## User Example

```Java
// Create user wrapper
User user = new User(realmResource);
            
// Find a user by it's name (null if not found)
UserRepresentation found = user.find("one");

// Create a user with password and enable it
UserResource created = user.create("two", "abc", true);

// Find a user or create it in case it was not found
UserResource foundOrCreated = user.findOrCreate("two", "abc", true);
```

## Roles Example

```Java
// Find a role by it's name (null if not found)
RoleRepresentations one = new Roles(list).findByName("one");

// Find a role by it's name or fail with an exception if not found
RoleRepresentations one = new Roles(list).findByNameOrFail("unknown");

// Find multiple roles by their name and fail if any of them is not found
List<RoleRepresentations> foundRoles = new Roles(list).findByNamesOrFail("one", "two", "three");

// Return only role names as list
List<String> roleNames = new Roles(list).asNames();

// Determine which of the expected roles are missing in the current list
List<RoleRepresentations> missingRoles = new Roles(currentRoles).missing(expectedRoles);

```
