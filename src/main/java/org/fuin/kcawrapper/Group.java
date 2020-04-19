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

    private final String id;

    private final GroupResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the group belongs to.
     * @param id
     *            Unique group identifier.
     * @param resource
     *            Associated group resource.
     */
    private Group(final Realm realm, final String id, final GroupResource resource) {
        super();
        this.realm = realm;
        this.id = id;
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
    public final String getId() {
        return id;
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
            return new Group(realm, id, groupRes);
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
                return new Group(realm, groupRep.getId(), groupRes);
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
