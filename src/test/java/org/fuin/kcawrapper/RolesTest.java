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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * Test for the {@link Roles} class.
 */
public class RolesTest {

    @Test
    public void testFindByName() {

        assertThat(roles().findByName("one").getName()).isEqualTo("one");
        assertThat(roles().findByName("four")).isNull();

    }

    @Test
    public void testFindByNameOrFail() {

        assertThat(roles().findByNameOrFail("one").getName()).isEqualTo("one");

        try {
            roles().findByNameOrFail("four");
            fail();
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Role 'four' not found: [one, two, three]");
        }

    }

    @Test
    public void testFindByNamesOrFail() {

        assertThat(roles().findByNamesOrFail("one", "two", "three").asNames()).hasSameElementsAs(roles().asNames());

        try {
            roles().findByNamesOrFail("one", "two", "three", "four");
            fail();
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Role 'four' not found: [one, two, three]");
        }

    }

    @Test
    public void testAsNames() {

        assertThat(roles().asNames()).hasSameElementsAs(Arrays.asList(new String[] { "one", "two", "three" }));

    }

    @Test
    public void testMissing() {

        assertThat(roles().missing(new Roles()).isEmpty()).isTrue();
        assertThat(roles().missing(roles("one")).isEmpty()).isTrue();
        assertThat(roles().missing(roles("one", "two")).isEmpty()).isTrue();
        assertThat(roles().missing(roles("one", "two", "three")).isEmpty()).isTrue();
        assertThat(roles().missing(roles("one", "two", "three", "four")).asNames()).hasSameElementsAs(roles("four").asNames());

    }

    private static Roles roles(final String... names) {
        final List<RoleRepresentation> results = new ArrayList<>();
        for (final String name : names) {
            final RoleRepresentation entry = new RoleRepresentation();
            entry.setName(name);
            results.add(entry);
        }
        return new Roles(results);
    }

    private static Roles roles() {
        return roles("one", "two", "three");
    }

}
