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
 * Test for the {@link Client} class.
 */
@Testcontainers
public class ClientIT extends BaseTest {

    private static final String REALM = "clientit";

    private static final String CLIENT = "my-service";

    @Test
    public void testFindOrCreateOpenIdConnectWithSecret() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);
            assertThat(Client.find(realm, CLIENT)).isNull();

            // TEST
            final Client client = Client.findOrCreateOpenIdConnectWithSecret(realm, CLIENT, "abc", "http://localhost:8080/api", true, true);

            // VERIFY
            assertThat(client).isNotNull();
            assertThat(Client.find(realm, CLIENT)).isNotNull();
            assertThat(Client.findOrFail(realm, CLIENT)).isNotNull();

        }

    }

    @Test
    public void testFindOrFail() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);

            // TEST & VERIFY
            try {
                Client.findOrFail(realm, "other-service");
                fail();
            } catch (final RuntimeException ex) {
                assertThat(ex.getMessage()).isEqualTo("Client 'other-service' should exist, but was not found");
            }

        }

    }

    @Test
    public void testGetServiceAccountUser() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);
            final Client client = Client.findOrCreateOpenIdConnectWithClientCredentials(realm, "testGetServiceAccountUser", "abc");

            // TEST
            final User user = client.getServiceAccountUser();

            // VERIFY
            assertThat(user).isNotNull();

        }

    }

}
