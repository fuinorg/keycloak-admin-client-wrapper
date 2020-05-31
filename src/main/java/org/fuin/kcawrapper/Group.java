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

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for group related operations.
 */
public final class Group {

    private static final Logger LOG = LoggerFactory.getLogger(Group.class);

    private final Realm realm;

    private final String uuid;

    private final String name;

    private final GroupResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the group belongs to.
     * @param uuid
     *            Unique group identifier.
     * @param name
     *            Unique group name.
     * @param resource
     *            Associated group resource.
     */
    private Group(final Realm realm, final String uuid, final String name, final GroupResource resource) {
        super();
        this.realm = realm;
        this.uuid = uuid;
        this.name = name;
        this.resource = resource;
    }

    /**
     * Returns the realm the group belongs to.
     * 
     * @return Realm.
     */
    public final Realm getRealm() {
        return realm;
    }

    /**
     * Returns the unique identifier of the group.
     * 
     * @return ID that is used for GET operations on the group resource.
     */
    public final String getUUID() {
        return uuid;
    }

    /**
     * Returns the unique name of the group.
     * 
     * @return Unique group name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the group resource.
     * 
     * @return Associated group resource.
     */
    public final GroupResource getResource() {
        return resource;
    }

    /**
     * Returns the realm roles that are assigned to the group.
     * 
     * @return All group realm roles.
     */
    public final Roles realmRoles() {
        return new Roles(resource.roles().realmLevel().listAll());
    }

    /**
     * Adds the given realm roles to the group.
     * 
     * @param roles
     *            Roles from the realm to add.
     */
    public final void addRealmRoles(final Roles roles) {
        resource.roles().realmLevel().add(roles.getList());
    }

    /**
     * Adds the given realm roles to the group if they are not already assigned. The function fails with a runtime exception if any of the
     * roles is not found within the realm.
     * 
     * @param roleNames
     *            Name of the roles from the realm to add to the group.
     */
    public final void addRealmRoles(final String... roleNames) {
        final Roles currentRealmRoles = realmRoles();
        final Roles expectedRoles = realm.getRoles().findByNamesOrFail(roleNames);
        final Roles missingRoles = currentRealmRoles.missing(expectedRoles);
        if (!missingRoles.isEmpty()) {
            addRealmRoles(missingRoles);
        }
    }

    /**
     * Returns the client roles that are assigned to the group.
     * 
     * @param client
     *            Client to return group roles for.
     * 
     * @return All group client roles.
     */
    public final Roles clientRoles(final Client client) {
        return new Roles(resource.roles().clientLevel(client.getUUID()).listAll());
    }

    /**
     * Adds the given client roles to the group.
     * 
     * @param client
     *            Client the roles are associated with.
     * @param roles
     *            Roles from the client to add.
     */
    public final void addClientRoles(final Client client, final Roles roles) {
        resource.roles().clientLevel(client.getUUID()).add(roles.getList());
    }

    /**
     * Adds the given client roles to the group if they are not already assigned. The function fails with a runtime exception if any of the
     * roles is not found within the client.
     * 
     * @param client
     *            Client the roles are associated with.
     * @param roleNames
     *            Name of the roles from the client to add to the group.
     */
    public final void addClientRoles(final Client client, final String... roleNames) {
        final Roles currentClientRoles = clientRoles(client);
        final Roles expectedRoles = client.getRoles().findByNamesOrFail(roleNames);
        final Roles missingRoles = currentClientRoles.missing(expectedRoles);
        if (!missingRoles.isEmpty()) {
            addClientRoles(client, missingRoles);
        }
    }

    /**
     * Creates a group.
     * 
     * @param realm
     *            Realm the group belongs to.
     * @param name
     *            Name.
     * 
     * @return New group.
     */
    public static Group create(final Realm realm, final String name) {
        LOG.debug("Create group '{}'", name);

        final GroupRepresentation groupRep = new GroupRepresentation();
        groupRep.setName(name);

        try (final Response response = realm.getResource().groups().add(groupRep)) {
            KcaUtils.ensureCreated("group " + name, response);
            final String id = KcaUtils.extractId(response);
            final GroupResource groupRes = realm.getResource().groups().group(id);
            return new Group(realm, id, name, groupRes);
        }

    }

    /**
     * Locates a group by it's name.
     * 
     * @param realm
     *            Realm the group belongs to.
     * @param name
     *            Name of group to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    public static Group find(final Realm realm, final String name) {
        final List<GroupRepresentation> groupReps = realm.getResource().groups().groups();
        if (groupReps == null) {
            return null;
        }
        for (final GroupRepresentation groupRep : groupReps) {
            if (groupRep.getName().equals(name)) {
                LOG.debug("Found group '{}'", name);
                final GroupResource groupRes = realm.getResource().groups().group(groupRep.getId());
                return new Group(realm, groupRep.getId(), name, groupRes);
            }
        }
        return null;
    }

    /**
     * Locates a group by it's name or creates it if it was not found.
     * 
     * @param realm
     *            Realm the group belongs to.
     * @param name
     *            Name.
     * 
     * @return Resource of the realm.
     */
    public static Group findOrCreate(final Realm realm, final String name) {
        final Group group = find(realm, name);
        if (group == null) {
            return create(realm, name);
        }
        return group;
    }

}
