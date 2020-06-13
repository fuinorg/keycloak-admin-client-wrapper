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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for realm related operations.
 */
public final class Realm {

    private static final Logger LOG = LoggerFactory.getLogger(Realm.class);

    private final Keycloak keycloak;

    private final String name;

    private final RealmResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param keycloak
     *            Keycloak instance to use.
     * @param name
     *            Unique realm name.
     * @param resource
     *            Associated realm resource.
     */
    private Realm(@NotNull final Keycloak keycloak, @NotEmpty final String name, @NotEmpty final RealmResource resource) {
        super();
        Validate.notNull(keycloak, "keycloak==null");
        Validate.notEmpty(name, "name==null or empty");
        Validate.notNull(resource, "resource==null");

        this.keycloak = keycloak;
        this.name = name;
        this.resource = resource;
    }

    /**
     * Returns the keycloak instance the realm belongs to.
     * 
     * @return Keycloak instance.
     */
    @NotNull
    public final Keycloak getKeycloak() {
        return keycloak;
    }

    /**
     * Returns the unique realm name.
     * 
     * @return Unique realm name.
     */
    @NotEmpty
    public final String getName() {
        return name;
    }

    /**
     * Returns the realm resource.
     * 
     * @return Associated realm resource.
     */
    @NotNull
    public final RealmResource getResource() {
        return resource;
    }

    /**
     * Removes the realm and all associated objects.
     */
    public final void remove() {
        resource.remove();
    }

    /**
     * Returns the defined roles.
     * 
     * @return All available roles.
     */
    @NotNull
    public final Roles getRoles() {
        return new Roles(resource.roles().list());
    }

    /**
     * Creates a realm.
     * 
     * @param keycloak
     *            Keycloak instance to use.
     * @param name
     *            Name of the realm to create.
     * @param enable
     *            Enable realm?
     * 
     * @return Resource of the created realm.
     */
    @NotNull
    public static Realm create(@NotNull final Keycloak keycloak, @NotEmpty final String name, final boolean enable) {
        Validate.notNull(keycloak, "keycloak==null");
        Validate.notEmpty(name, "name==null or empty");

        final RealmRepresentation realmRep = new RealmRepresentation();
        realmRep.setRealm(name);
        realmRep.setEnabled(enable);

        return create(keycloak, realmRep);
    }

    /**
     * Creates a realm.
     * 
     * @param keycloak
     *            Keycloak instance to use.
     * @param realmRep
     *            Realm configuration.
     * 
     * @return Resource of the created realm.
     */
    @NotNull
    public static Realm create(@NotNull final Keycloak keycloak, @NotNull final RealmRepresentation realmRep) {
        Validate.notNull(keycloak, "keycloak==null");
        Validate.notNull(realmRep, "realmRep==null");
        Validate.notEmpty(realmRep.getRealm(), "realmRep.realm==null or empty");

        final String name = realmRep.getRealm();
        LOG.debug("Create realm '{}'", name);
        keycloak.realms().create(realmRep);
        final RealmResource realmRes = keycloak.realm(name);
        return new Realm(keycloak, name, realmRes);

    }

    /**
     * Locates a realm by it's name.
     * 
     * @param keycloak
     *            Keycloak instance to use.
     * @param name
     *            Name of realm to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    @Nullable
    public static Realm find(@NotNull final Keycloak keycloak, @NotEmpty final String name) {
        Validate.notNull(keycloak, "keycloak==null");
        Validate.notEmpty(name, "name==null or empty");

        final List<RealmRepresentation> realms = keycloak.realms().findAll();
        if (realms == null) {
            return null;
        }
        for (final RealmRepresentation realm : realms) {
            if (realm.getRealm().equals(name)) {
                LOG.debug("Found realm '{}'", name);
                return new Realm(keycloak, name, keycloak.realm(name));
            }
        }
        return null;
    }

    /**
     * Locates a realm by it's name or creates it if it was not found.
     * 
     * @param keycloak
     *            Keycloak instance to use.
     * @param name
     *            Name of the realm to find.
     * @param enable
     *            Enable realm in case it has to be created?
     * 
     * @return Resource of the realm.
     */
    @NotNull
    public static Realm findOrCreate(@NotNull final Keycloak keycloak, @NotEmpty final String name, final boolean enable) {
        Validate.notNull(keycloak, "keycloak==null");
        Validate.notEmpty(name, "name==null or empty");

        final Realm realm = find(keycloak, name);
        if (realm == null) {
            return create(keycloak, name, enable);
        }
        return realm;
    }

    /**
     * Locates a realm by it's name or creates it if it was not found.
     * 
     * @param keycloak
     *            Keycloak instance to use.
     * @param realmRep
     *            Realm configuration in case the realm needs to be created.
     * 
     * @return Resource of the realm.
     */
    @NotNull
    public static Realm findOrCreate(@NotNull final Keycloak keycloak, @NotNull final RealmRepresentation realmRep) {
        Validate.notNull(keycloak, "keycloak==null");
        Validate.notNull(realmRep, "realmRep==null");
        Validate.notEmpty(realmRep.getRealm(), "realmRep.realm==null or empty");

        final Realm realm = find(keycloak, realmRep.getRealm());
        if (realm == null) {
            return create(keycloak, realmRep);
        }
        return realm;
    }

}
