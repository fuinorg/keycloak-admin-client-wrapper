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
import java.util.List;

import org.keycloak.representations.idm.RoleRepresentation;

/**
 * Provides additional functionalities for roles. 
 */
public final class Roles {

    private final List<RoleRepresentation> list;
    
    public Roles(final List<RoleRepresentation> roles) {
        super();
        if (roles == null) {
            this.list = new ArrayList<>();
        } else {
            this.list = new ArrayList<>(roles);
        }
    }
    
    public final RoleRepresentation findByName(final String name) {
        for (final RoleRepresentation role : list) {
            if (name.contentEquals(role.getName())) {
                return role;
            }
        }
        return null;
    }
    
    public final RoleRepresentation findByNameOrFail(final String name) {
        final RoleRepresentation role = findByName(name);
        if (role == null) {
            throw new RuntimeException("Role '" + name + "' not found: " + asNames());
        }
        return role;
    }
    
    public final List<RoleRepresentation> findByNamesOrFail(final String... names) {
        List<RoleRepresentation> result = new ArrayList<>();
        for (String name : names) {
            RoleRepresentation role = findByName(name);
            if (role == null) {
                throw new RuntimeException("Role '" + name + "' not found: " + asNames());
            }
            result.add(role);
        }
        return result;
    }
    
    public final List<String> asNames() {
       final  List<String> names = new ArrayList<>();
        for (RoleRepresentation role : list) {
            names.add(role.getName());
        }
        return names;
    }
    
    public final List<RoleRepresentation> missing(final List<RoleRepresentation> expectedRoles) {

        if (list.isEmpty()) {
            return expectedRoles;
        }

        final List<RoleRepresentation> missing = new ArrayList<>();
        
        for (final RoleRepresentation expectedRole : expectedRoles) {
            if (findByName(expectedRole.getName()) == null) {
                missing.add(expectedRole);
            }
        }
        
        return missing;
        
    }
    
}
