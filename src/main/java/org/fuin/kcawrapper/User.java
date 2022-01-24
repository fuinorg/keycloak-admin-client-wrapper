/**
 * Copyright (C) 2020 Michael Schnell. All rights reserved. 
 * http://www.fuin.org/
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see http://www.gnu.org/licenses/.
 */
package org.fuin.kcawrapper;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for user related operations.
 */
public final class User {

    private static final Logger LOG = LoggerFactory.getLogger(User.class);

    private final Realm realm;

    private final String uuid;

    private final String name;

    private final UserResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the user belongs to.
     * @param uuid
     *            Unique user identifier.
     * @param name
     *            Unique name of the user.
     * @param resource
     *            Associated user resource.
     */
    User(@NotNull final Realm realm, @NotEmpty final String uuid, @NotEmpty final String name, @NotNull final UserResource resource) {
        super();
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(uuid, "uuid==null or empty");
        Validate.notEmpty(name, "name==null or empty");
        Validate.notNull(resource, "resource==null");

        this.realm = realm;
        this.uuid = uuid;
        this.name = name;
        this.resource = resource;
    }

    /**
     * Returns the realm the user belongs to.
     * 
     * @return Realm.
     */
    @NotNull
    public final Realm getRealm() {
        return realm;
    }

    /**
     * Returns the unique identifier of the user.
     * 
     * @return ID that is used for GET operations on the user resource.
     */
    @NotEmpty
    public final String getUUID() {
        return uuid;
    }

    /**
     * Returns the unique name of the user.
     * 
     * @return Unique user name.
     */
    @NotEmpty
    public final String getName() {
        return name;
    }

    /**
     * Returns the user resource.
     * 
     * @return Associated user resource.
     */
    @NotNull
    public final UserResource getResource() {
        return resource;
    }

    /**
     * Make user join the given groups.
     * 
     * @param groups
     *            Group names.
     */
    public final void joinGroups(@NotNull final Group... groups) {
        Validate.notEmpty(groups, "groups==null or empty");
        Validate.noNullElements(groups, "null elements are not allowed for groups");

        joinGroups(Arrays.asList(groups));
    }

    /**
     * Returns the realm roles that are assigned to the user.
     * 
     * @return All user realm roles.
     */
    @NotNull
    public final Roles realmRoles() {
        return new Roles(resource.roles().realmLevel().listAll());
    }

    /**
     * Adds the given realm roles to the user.
     * 
     * @param roles
     *            Roles from the realm to add.
     */
    public final void addRealmRoles(@NotNull final Roles roles) {
        Validate.notNull(roles, "roles==null");

        resource.roles().realmLevel().add(roles.getList());
    }

    /**
     * Adds the given realm roles to the user if they are not already assigned. The function fails with a runtime exception if any of the
     * roles is not found within the realm.
     * 
     * @param roleNames
     *            Name of the roles from the realm to add to the user. A name cannot be null or empty.
     */
    public final void addRealmRoles(@NotNull final String... roleNames) {
        Validate.notEmpty(roleNames, "roleNames==null or empty");
        Validate.noNullElements(roleNames, "null elements are not allowed for roleNames");

        final Roles currentRealmRoles = realmRoles();
        final Roles expectedRoles = realm.getRoles().findByNamesOrFail(roleNames);
        final Roles missingRoles = currentRealmRoles.missing(expectedRoles);
        if (!missingRoles.isEmpty()) {
            addRealmRoles(missingRoles);
        }
    }

    /**
     * Returns the client roles that are assigned to the user.
     * 
     * @param client
     *            Client to return user roles for.
     * 
     * @return All user client roles.
     */
    @NotNull
    public final Roles clientRoles(@NotNull final Client client) {
        Validate.notNull(client, "client== null");

        return new Roles(resource.roles().clientLevel(client.getUUID()).listAll());
    }

    /**
     * Adds the given client roles to the user.
     * 
     * @param client
     *            Client the roles are associated with.
     * @param roles
     *            Roles from the client to add.
     */
    public final void addClientRoles(@NotNull final Client client, @NotNull final Roles roles) {
        Validate.notNull(client, "client==null");
        Validate.notNull(roles, "roles==null");

        resource.roles().clientLevel(client.getUUID()).add(roles.getList());
    }

    /**
     * Adds the given client roles to the user if they are not already assigned. The function fails with a runtime exception if any of the
     * roles is not found within the client.
     * 
     * @param client
     *            Client the roles are associated with.
     * @param roleNames
     *            Name of the roles from the client to add to the user. The name cannot be null or empty.
     */
    public final void addClientRoles(@NotNull final Client client, @NotNull final String... roleNames) {
        Validate.notNull(client, "client==null");
        Validate.notEmpty(roleNames, "roleNames==null or empty");
        Validate.noNullElements(roleNames, "null elements are not allowed for roleNames");

        final Roles currentClientRoles = clientRoles(client);
        final Roles expectedRoles = client.getRoles().findByNamesOrFail(roleNames);
        final Roles missingRoles = currentClientRoles.missing(expectedRoles);
        if (!missingRoles.isEmpty()) {
            addClientRoles(client, missingRoles);
        }
    }

    /**
     * Make user join the given groups.
     * 
     * @param groups
     *            Groups.
     */
    public final void joinGroups(@NotEmpty final List<Group> groups) {
        Validate.notEmpty(groups, "groups== null or groups.size==0");
        Validate.noNullElements(groups, "null elements are not allowed for groups");

        for (final Group group : groups) {
            LOG.debug("[{}] User '{}' joined group '{}'", realm.getName(), name, group.getName());
            resource.joinGroup(group.getUUID());
        }
    }

    /**
     * Creates a user.
     * 
     * @param realm
     *            Realm the user belongs to.
     * @param name
     *            Username.
     * @param pw
     *            Password.
     * @param enable
     *            Enable the user or not.
     * 
     * @return New user.
     */
    @NotNull
    public static User create(@NotNull final Realm realm, @NotEmpty final String name, @NotEmpty final String pw, final boolean enable) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(name, "name==null or empty");
        Validate.notEmpty(pw, "pw==null or empty");

        LOG.debug("[{}] Create user '{}'", realm.getName(), name);

        final CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(pw);
        credential.setTemporary(false);

        final UserRepresentation user = new UserRepresentation();
        user.setUsername(name);
        user.setCredentials(asList(credential));
        user.setEnabled(enable);

        try (final Response response = realm.getResource().users().create(user)) {
            KcaUtils.ensureCreated("user " + name, response);
            final String id = KcaUtils.extractId(response);
            final UserResource userResource = realm.getResource().users().get(id);
            return new User(realm, id, name, userResource);
        }

    }

    /**
     * Locates a user by it's name.
     * 
     * @param realm
     *            Realm the user belongs to.
     * @param name
     *            Name of user to find.
     * 
     * @return User or {@literal null} if not found.
     */
    @Nullable
    public static User find(@NotNull final Realm realm, @NotNull final String name) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(name, "name==null or empty");

        final List<UserRepresentation> userReps = realm.getResource().users().list();
        if (userReps == null) {
            return null;
        }
        for (final UserRepresentation userRep : userReps) {
            if (userRep.getUsername().equals(name)) {
                LOG.debug("[{}] Found user '{}'", realm.getName(), name);
                final UserResource userResource = realm.getResource().users().get(userRep.getId());
                return new User(realm, userRep.getId(), name, userResource);
            }
        }
        return null;
    }

    /**
     * Locates a user by it's name or creates it if it was not found.
     * 
     * @param realm
     *            Realm the user belongs to.
     * @param name
     *            Username.
     * @param pw
     *            Password.
     * @param enable
     *            Enable a newly created user or not.
     * 
     * @return User.
     */
    @NotNull
    public static User findOrCreate(@NotNull final Realm realm, @NotNull final String name, @NotNull final String pw,
            final boolean enable) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(name, "name==null or empty");

        final User user = find(realm, name);
        if (user == null) {
            return create(realm, name, pw, enable);
        }
        return user;
    }

    /**
     * Locates a user by it's name or fails with a runtime exception if it does not exist.
     * 
     * @param realm
     *            Realm the user belongs to.
     * @param name
     *            Name of the user to find.
     * 
     * @return User.
     */
    @NotNull
    public static User findOrFail(@NotNull final Realm realm, @NotNull final String name) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(name, "name==null or empty");

        final User user = find(realm, name);
        if (user == null) {
            throw new RuntimeException("User '" + name + "' should exist, but was not found");
        }
        return user;
    }

}
