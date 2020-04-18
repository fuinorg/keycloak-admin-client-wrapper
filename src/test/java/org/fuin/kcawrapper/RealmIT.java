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
import org.keycloak.admin.client.resource.RealmResource;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for the {@link Realm} class.
 */
@Testcontainers
public class RealmIT extends BaseTest {

    @Test
    public void testFindOrCreate() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm testee = new Realm(keycloak);
            assertThat(testee.find("test")).isNull();

            // TEST
            final RealmResource realmResource = testee.findOrCreate("test", true);

            // VERIFY
            assertThat(realmResource).isNotNull();
            assertThat(testee.find("test")).isNotNull();

        }

    }

}
