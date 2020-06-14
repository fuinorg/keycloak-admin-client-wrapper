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

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for the {@link Realm} class.
 */
@Testcontainers
public class RealmIT extends BaseTest {

    private static final String REALM = "realmit";

    @Test
    public void testFindOrCreate() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            assertThat(Realm.find(keycloak, REALM)).isNull();

            // TEST
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);

            // VERIFY
            assertThat(realm).isNotNull();
            assertThat(Realm.find(keycloak, REALM)).isNotNull();

        }

    }

    @Test
    public void testFindOrFail() {

        try (final Keycloak keycloak = master()) {

            try {
                Realm.findOrFail(keycloak, "non-existing-realm");
                fail();
            } catch (final RuntimeException ex) {
                assertThat(ex.getMessage()).isEqualTo("Realm 'non-existing-realm' should exist, but was not found");
            }

        }

    }

}
