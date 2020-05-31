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

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for the {@link Role} class.
 */
@Testcontainers
public class RoleIT extends BaseTest {

    private static final String REALM = "groupit";

    private static final String ROLE = "admin";

    private static final String DESCR = "Administrator";

    @Test
    public void testFindOrCreate() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);
            assertThat(Role.find(realm, ROLE)).isNull();

            // TEST
            final Role group = Role.findOrCreate(realm, ROLE, DESCR);

            // VERIFY
            assertThat(group).isNotNull();
            assertThat(Role.find(realm, ROLE)).isNotNull();

        }

    }

}
