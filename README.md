# keycloak-admin-client-wrapper
Small wrapper around the [Keycloak Admin Client](https://www.keycloak.org/keycloak-admin-client/index.html) that simplifies some tasks.

[![Build Status](https://jenkins.fuin.org/job/keycloak-admin-client-wrapper/badge/icon)](https://jenkins.fuin.org/job/keycloak-admin-client-wrapper/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.fuin/keycloak-admin-client-wrapper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.fuin/keycloak-admin-client-wrapper/)
[![LGPLv3 License](http://img.shields.io/badge/license-LGPLv3-blue.svg)](https://www.gnu.org/licenses/lgpl.html)
[![Java Development Kit 11](https://img.shields.io/badge/JDK-11-green.svg)](https://openjdk.java.net/projects/jdk/11/)

## Keycloak Version
Current Keycloak Version: 9.0.3

## Realm Example

```Java
// Find a realm by it's name (null if not found)
Realm foundRealm = Realm.find(keycloak, "test1");

// Create a realm and enable it
Realm createdRealm = Realm.create(keycloak, "test2", true);

// Find a realm or create it in case it was not found
Realm foundOrCreatedRealm = Realm.findOrCreate(keycloak, "test3", true);

// Create and remove a realm
Realm realm4 = Realm.create(keycloak, "test4", true);
realm4.remove();
```

## User Example

```Java
// Find a user by it's name (null if not found)
User found = User.find(realm, "one");

// Create a user with password and enable it
User created = User.create(realm, "two", "abc", true);

// Find a user or creates it in case it was not found
User foundOrCreated = User.findOrCreate(realm, "two", "abc", true);

// Join a group
user.joinGroups(group);

// Add some realm management client roles to the user
Client realmManagementClient = Client.findOrFail(realm, "realm-management");
user.addClientRoles(realmManagementClient, "impersonation", "manage-users", "view-users");

```

## Group Example

```Java
// Find a group by it's name (null if not found)
Group found = Group.find(realm, "one");

// Create a group
Group created = Group.create(realm, "two");

// Find a group or creates it in case it was not found
Group foundOrCreated = Group.findOrCreate(realm, "two");

// Add some realm management client roles to the admin group
Group adminGroup = Group.create(realm, "admins");
Client realmManagementClient = Client.findOrFail(realm, "realm-management");
adminGroup.addClientRoles(realmManagementClient, "impersonation", "manage-users", "view-users");

```

## Client Example

```Java
// Find a client or create it with a typical Open ID connect client with client secret and redirect URI 
Client clientA = Client.findOrCreateOpenIdConnectWithSecret(realm, "my-service-a", "abc", "http://localhost:8080/api");

// Find a client or create it based on a full specification
Client clientB = Client.findOrCreate(realm, "my-service-b", clientRepresentation);

// Find a client by it's name (null if not found)
Client clientC = Client.find(realm, "my-service-c");

// Find a client by it's name or fail with a runtime exception
Client clientD = Client.findOrFail(realm, "my-service-d");
```

## Roles Example

```Java
 // Find a role by it's name (null if not found)
RoleRepresentation one = new Roles(list).findByName("one");

// Find a role by it's name or fail with an exception if not found
RoleRepresentation unknown = new Roles(list).findByNameOrFail("unknown");

// Find multiple roles by their name and fail if any of them is not found
Roles foundRoles = new Roles(list).findByNamesOrFail("one", "two", "three");

// Return only role names as list
List<String> roleNames = new Roles(list).asNames();

// Determine which of the expected roles are missing in the current list
Roles currentRoles = new Roles(list1);
Roles expectedRoles = new Roles(list2);
Roles missingRoles = currentRoles.missing(expectedRoles);
```
