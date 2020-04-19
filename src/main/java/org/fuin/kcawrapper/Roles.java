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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * Provides additional functionalities for roles.
 */
public final class Roles implements Iterable<RoleRepresentation> {

    private final List<RoleRepresentation> list;

    /**
     * Creates an empty roles collection.
     */
    public Roles() {
        super();
        list = Collections.emptyList();
    }

    /**
     * Constructor with list.
     * 
     * @param roles
     *            Roles to wrap.
     */
    public Roles(final List<RoleRepresentation> roles) {
        super();
        if (roles == null) {
            this.list = new ArrayList<>();
        } else {
            this.list = new ArrayList<>(roles);
        }
    }

    /**
     * Returns a copy of the internal role represantations.
     * 
     * @return List of roles.
     */
    public List<RoleRepresentation> getList() {
        return new ArrayList<>(list);
    }

    /**
     * Locates a role by it's name.
     * 
     * @param name
     *            Role name to find.
     * 
     * @return Role or {@literal null} in case it was not found.
     */
    public final RoleRepresentation findByName(final String name) {
        for (final RoleRepresentation role : list) {
            if (name.contentEquals(role.getName())) {
                return role;
            }
        }
        return null;
    }

    /**
     * Tries to locate a role by it's name or fails with a runtime exception if it was not found.
     * 
     * @param name
     *            Role name to find.
     * 
     * @return Role.
     */
    public final RoleRepresentation findByNameOrFail(final String name) {
        final RoleRepresentation role = findByName(name);
        if (role == null) {
            throw new RuntimeException("Role '" + name + "' not found: " + asNames());
        }
        return role;
    }

    /**
     * Tries to locate multiple role by it's name or fails with a runtime exception if any of them was not found.
     * 
     * @param names
     *            Role names to find.
     * 
     * @return Roles.
     */
    public final Roles findByNamesOrFail(final String... names) {
        List<RoleRepresentation> result = new ArrayList<>();
        for (String name : names) {
            RoleRepresentation role = findByName(name);
            if (role == null) {
                throw new RuntimeException("Role '" + name + "' not found: " + asNames());
            }
            result.add(role);
        }
        return new Roles(result);
    }

    /**
     * Returns the role names.
     * 
     * @return Names.
     */
    public final List<String> asNames() {
        final List<String> names = new ArrayList<>();
        for (RoleRepresentation role : list) {
            names.add(role.getName());
        }
        return names;
    }

    /**
     * Compares the given expected role list with this one and returns a list of all roles that cannot be found.
     * 
     * @param expectedRoles
     *            Super set of this role list.
     * 
     * @return List of roles that were missing in this list.
     */
    public final Roles missing(final Roles expectedRoles) {

        if (list.isEmpty()) {
            return expectedRoles;
        }

        final List<RoleRepresentation> missing = new ArrayList<>();

        for (final RoleRepresentation expectedRole : expectedRoles) {
            if (findByName(expectedRole.getName()) == null) {
                missing.add(expectedRole);
            }
        }

        return new Roles(missing);

    }

    /**
     * Determines if the list of roles is empty or not.
     * 
     * @return If the list is empty {@literal true} else {@literal false}.
     */
    public final boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public final Iterator<RoleRepresentation> iterator() {
        return list.iterator();
    }

}
