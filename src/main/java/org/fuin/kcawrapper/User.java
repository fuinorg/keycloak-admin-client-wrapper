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

import java.util.List;

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.resource.RealmResource;
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

    private final RealmResource realm;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the user belongs to.
     */
    public User(final RealmResource realm) {
        super();
        this.realm = realm;
    }

    /**
     * Creates a user.
     * 
     * @param name
     *            Username.
     * @param pw
     *            Password.
     * @param enable
     *            Enable the user or not.
     * 
     * @return New user.
     */
    public final UserResource create(final String name, final String pw, final boolean enable) {
        LOG.debug("Create user '{}'", name);

        final CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(pw);
        credential.setTemporary(false);

        final UserRepresentation user = new UserRepresentation();
        user.setUsername(name);
        user.setCredentials(asList(credential));
        user.setEnabled(enable);

        try (final Response response = realm.users().create(user)) {
            KcaUtils.ensureCreated("user " + name, response);
            final String id = KcaUtils.extractId(response);
            return realm.users().get(id);
        }

    }

    /**
     * Locates a user by it's name.
     * 
     * @param name
     *            Name of user to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    public final UserRepresentation find(final String name) {
        final List<UserRepresentation> users = realm.users().list();
        if (users == null) {
            return null;
        }
        for (final UserRepresentation user : users) {
            if (user.getUsername().equals(name)) {
                LOG.debug("Found user '{}'", name);
                return user;
            }
        }
        return null;
    }

    /**
     * Locates a user by it's name or creates it if it was not found.
     * 
     * @param name
     *            Username.
     * @param pw
     *            Password.
     * @param enable
     *            Enable a newly created user or not.
     * 
     * @return Resource of the realm.
     */
    public final UserResource findOrCreate(final String name, final String pw, final boolean enable) {
        UserRepresentation user = find(name);
        if (user == null) {
            return create(name, pw, enable);
        }
        return realm.users().get(user.getId());
    }

}
