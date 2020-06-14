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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Test for the {@link User} class.
 */
@Testcontainers
public class UserIT extends BaseTest {

    private static final String REALM = "userit";

    private static final String USER = "jane";

    @Test
    public void testFindOrCreate() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);
            assertThat(User.find(realm, USER)).isNull();

            // TEST
            final User user = User.findOrCreate(realm, USER, "abc", true);

            // VERIFY
            assertThat(user).isNotNull();
            assertThat(User.find(realm, USER)).isNotNull();

        }

    }

    @Test
    public void testFindOrFail() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);

            // TEST & VERIFY
            try {
                User.findOrFail(realm, "non-existing-user");
                fail();
            } catch (final RuntimeException ex) {
                assertThat(ex.getMessage()).isEqualTo("User 'non-existing-user' should exist, but was not found");
            }

        }

    }

    @Test
    public void testJoinGroups() {

        try (final Keycloak keycloak = master()) {

            // PREPARE
            final String groupName = "administrators";
            final Realm realm = Realm.findOrCreate(keycloak, REALM, true);
            final User user = User.findOrCreate(realm, "john", "123", true);
            final Group group = Group.findOrCreate(realm, groupName);

            // TEST
            user.joinGroups(group);

            // VERIFY
            final List<GroupRepresentation> groupReps = user.getResource().groups();
            assertThat(groupReps).hasSize(1);
            final GroupRepresentation groupRep = groupReps.get(0);
            assertThat(groupRep.getId()).isEqualTo(group.getUUID());
            assertThat(groupRep.getName()).isEqualTo(groupName);

        }

    }

}
