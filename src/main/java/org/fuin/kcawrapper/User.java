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

import javax.ws.rs.core.Response;

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

    private final String id;

    private final UserResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the user belongs to.
     * @param id
     *            Unique user identifier.
     * @param resource
     *            Associated user resource.
     */
    public User(final Realm realm, final String id, final UserResource resource) {
        super();
        this.realm = realm;
        this.id = id;
        this.resource = resource;
    }

    /**
     * Returns the realm the user belongs to.
     * 
     * @return Realm.
     */
    public final Realm getRealm() {
        return realm;
    }

    /**
     * Returns the unique identifier of the user.
     * 
     * @return ID that is used for GET operations on the user resource.
     */
    public final String getId() {
        return id;
    }

    /**
     * Returns the user resource.
     * 
     * @return Associated user resource.
     */
    public final UserResource getResource() {
        return resource;
    }

    /**
     * Make user join the given groups.
     * 
     * @param names
     *            Group names.
     */
    public final void joinGroups(final Group...groups) {
        joinGroups(Arrays.asList(groups));
    }
    
    /**
     * Make user join the given groups.
     * 
     * @param names
     *            Group names.
     */
    public final void joinGroups(final List<Group> groups) {
        for (final Group group : groups) {
            resource.joinGroup(group.getId());
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
    public static User create(final Realm realm, final String name, final String pw, final boolean enable) {
        LOG.debug("Create user '{}'", name);

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
            return new User(realm, id, userResource);
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
     * @return Representation or {@literal null} if not found.
     */
    public static User find(final Realm realm, final String name) {
        final List<UserRepresentation> userReps = realm.getResource().users().list();
        if (userReps == null) {
            return null;
        }
        for (final UserRepresentation userRep : userReps) {
            if (userRep.getUsername().equals(name)) {
                LOG.debug("Found user '{}'", name);
                final UserResource userResource = realm.getResource().users().get(userRep.getId());
                return new User(realm, userRep.getId(), userResource);
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
     * @return Resource of the realm.
     */
    public static User findOrCreate(final Realm realm, final String name, final String pw, final boolean enable) {
        final User user = find(realm, name);
        if (user == null) {
            return create(realm, name, pw, enable);
        }
        return user;
    }

}
