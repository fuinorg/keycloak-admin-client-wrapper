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

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Duration;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Base class to allow starting Keycloak Docker image only once for all tests.
 */
@SuppressWarnings("rawtypes")
public abstract class BaseTest {

    static final String ADMIN_USER = "admin";

    static final String ADMIN_PW = "abc";

    static final GenericContainer KEYCLOAK_CONTAINER;

    static {
        KEYCLOAK_CONTAINER = new GenericContainer("jboss/keycloak:16.1.0").withExposedPorts(8080).withEnv("KEYCLOAK_USER", ADMIN_USER)
                .withEnv("KEYCLOAK_PASSWORD", ADMIN_PW).waitingFor(Wait.forHttp("/auth").withStartupTimeout(Duration.of(2L, MINUTES)));
        KEYCLOAK_CONTAINER.start();
    }

    /**
     * Returns the Keycloak host URL.
     * 
     * @return Base URL (without "/auth").
     */
    static String getHostUrl() {
        return "http://" + KEYCLOAK_CONTAINER.getContainerIpAddress() + ":" + KEYCLOAK_CONTAINER.getMappedPort(8080);
    }

    /**
     * Returns a client admin to the Keycloak master realm.
     * 
     * @return New client admin.
     */
    static Keycloak master() {
        return KeycloakBuilder.builder().serverUrl(getHostUrl() + "/auth/").realm("master").username(ADMIN_USER).password(ADMIN_PW)
                .clientId("admin-cli")
                .resteasyClient(((ResteasyClientBuilder) ResteasyClientBuilder.newBuilder()).connectionPoolSize(20).build()).build();
    }

}