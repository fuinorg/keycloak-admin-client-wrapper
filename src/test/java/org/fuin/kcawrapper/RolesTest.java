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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RoleRepresentation;

/**
 * Test for the {@link Roles} class.
 */
public class RolesTest {

    private static List<RoleRepresentation> list;

    private static RoleRepresentation one;

    private static RoleRepresentation two;

    private static RoleRepresentation three;

    @BeforeAll
    public static void setup() {
        list = new ArrayList<>();
        one = new RoleRepresentation();
        one.setName("one");
        list.add(one);
        two = new RoleRepresentation();
        two.setName("two");
        list.add(two);
        three = new RoleRepresentation();
        three.setName("three");
        list.add(three);
    }

    @Test
    public void testFindByName() {

        assertThat(new Roles(list).findByName("one")).isEqualTo(one);
        assertThat(new Roles(list).findByName("four")).isNull();

    }

    @Test
    public void testFindByNameOrFail() {

        assertThat(new Roles(list).findByNameOrFail("one")).isEqualTo(one);

        try {
            new Roles(list).findByNameOrFail("four");
            fail();
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Role 'four' not found: [one, two, three]");
        }

    }

    @Test
    public void testFindByNamesOrFail() {

        assertThat(new Roles(list).findByNamesOrFail("one", "two", "three")).hasSameElementsAs(list);

        try {
            new Roles(list).findByNamesOrFail("one", "two", "three", "four");
            fail();
        } catch (final RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Role 'four' not found: [one, two, three]");
        }

    }

    @Test
    public void testAsNames() {

        assertThat(new Roles(list).asNames()).hasSameElementsAs(Arrays.asList(new String[] { "one", "two", "three" }));

    }

    @Test
    public void testMissing() {

        assertThat(new Roles(list).missing(Collections.emptyList())).isEmpty();
        assertThat(new Roles(list).missing(create("one"))).isEmpty();
        assertThat(new Roles(list).missing(create("one", "two"))).isEmpty();
        assertThat(new Roles(list).missing(create("one", "two", "three"))).isEmpty();
        assertThat(names(new Roles(list).missing(create("one", "two", "three", "four")))).hasSameElementsAs(names(create("four")));

    }
    
    private static List<String> names(List<RoleRepresentation> list) {
        return new Roles(list).asNames();
    }

    private static List<RoleRepresentation> create(final String... names) {
        final List<RoleRepresentation> results = new ArrayList<>();
        for (final String name : names) {
            final RoleRepresentation entry = new RoleRepresentation();
            entry.setName(name);
            results.add(entry);
        }
        return results;
    }

}
