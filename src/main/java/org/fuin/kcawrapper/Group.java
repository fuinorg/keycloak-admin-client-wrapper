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
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for group related operations.
 */
public final class Group {

    private static final Logger LOG = LoggerFactory.getLogger(Group.class);

    private final RealmResource realm;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the group belongs to.
     */
    public Group(final RealmResource realm) {
        super();
        this.realm = realm;
    }

    /**
     * Creates a group.
     * 
     * @param name
     *            Name.
     * 
     * @return New group.
     */
    public final GroupResource create(final String name) {
        LOG.debug("Create group '{}'", name);

        final GroupRepresentation group = new GroupRepresentation();
        group.setName(name);

        try (final Response response = realm.groups().add(group)) {
            KcaUtils.ensureCreated("group " + name, response);
            final String id = KcaUtils.extractId(response);
            return realm.groups().group(id);
        }

    }

    /**
     * Locates a group by it's name.
     * 
     * @param name
     *            Name of group to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    public final GroupRepresentation find(final String name) {
        final List<GroupRepresentation> groups = realm.groups().groups();
        if (groups == null) {
            return null;
        }
        for (final GroupRepresentation group : groups) {
            if (group.getName().equals(name)) {
                LOG.debug("Found group '{}'", name);
                return group;
            }
        }
        return null;
    }

    /**
     * Locates a group by it's name or creates it if it was not found.
     * 
     * @param name
     *            Name.
     * 
     * @return Resource of the realm.
     */
    public final GroupResource findOrCreate(final String name) {
        GroupRepresentation group = find(name);
        if (group == null) {
            return create(name);
        }
        return realm.groups().group(group.getId());
    }

}
