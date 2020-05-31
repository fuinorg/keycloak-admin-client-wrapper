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

import java.util.List;

import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for role related operations.
 */
public final class Role {

    private static final Logger LOG = LoggerFactory.getLogger(Role.class);

    private final Realm realm;

    private final String uuid;

    private final String name;

    private final RoleResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the role belongs to.
     * @param uuid
     *            Unique role identifier.
     * @param name
     *            Unique role name.
     * @param resource
     *            Associated role resource.
     */
    private Role(final Realm realm, final String uuid, final String name, final RoleResource resource) {
        super();
        this.realm = realm;
        this.uuid = uuid;
        this.name = name;
        this.resource = resource;
    }

    /**
     * Returns the realm the role belongs to.
     * 
     * @return Realm.
     */
    public final Realm getRealm() {
        return realm;
    }

    /**
     * Returns the unique identifier of the role.
     * 
     * @return ID that is used for GET operations on the role resource.
     */
    public final String getUUID() {
        return uuid;
    }

    /**
     * Returns the role name.
     * 
     * @return Unique role name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the role resource.
     * 
     * @return Associated role resource.
     */
    public final RoleResource getResource() {
        return resource;
    }

    /**
     * Creates a role based on a representation.
     * 
     * @param realm
     *            Realm the role belongs to.
     * @param name
     *            Name of the role.
     * @param description
     *            Description of the purpose.
     * 
     * @return New role.
     */
    public static Role create(final Realm realm, final String name, final String description) {

        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName(name);
        roleRep.setDescription(description);

        realm.getResource().roles().create(roleRep);
        LOG.debug("Created role '{}'", name);

        roleRep = new Roles(realm.getRoles().getList()).findByNameOrFail(name);

        final RoleResource roleRes = realm.getResource().roles().get(name);
        return new Role(realm, roleRep.getId(), name, roleRes);

    }

    /**
     * Locates a user by it's role identifier.
     * 
     * @param realm
     *            Realm the role belongs to.
     * @param name
     *            Role ID of user to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    public static Role find(final Realm realm, final String name) {
        final List<RoleRepresentation> roles = realm.getResource().roles().list();
        if (roles == null) {
            return null;
        }
        final RoleRepresentation roleRep = new Roles(roles).findByName(name);
        if (roleRep != null) {
            LOG.debug("Found role '{}'", name);
            final RoleResource roleRes = realm.getResource().roles().get(roleRep.getId());
            return new Role(realm, roleRep.getId(), name, roleRes);
        }
        return null;
    }

    /**
     * Locates a role by it's name or fails with a runtime exception if it does not exist.
     * 
     * @param realm
     *            Realm the role belongs to.
     * @param name
     *            Name of role to find.
     * 
     * @return Representation.
     */
    public static Role findOrFail(final Realm realm, final String name) {
        final Role role = find(realm, name);
        if (role == null) {
            throw new RuntimeException("Role '" + name + "' should exist, but was not found");
        }
        LOG.debug("Found role '{}'", name);
        return role;
    }

    /**
     * Locates a role by it's name or creates it if it does not exist yet.
     * 
     * @param realm
     *            Realm the role belongs to.
     * @param name
     *            Name of role to find.
     * @param description
     *            Description of the purpose.
     * 
     * @return Role.
     */
    public static Role findOrCreate(final Realm realm, final String name, final String description) {
        final Role role = find(realm, name);
        if (role == null) {
            return create(realm, name, description);
        }
        return role;
    }

}
