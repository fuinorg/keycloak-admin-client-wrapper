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

import javax.ws.rs.core.Response;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for client related operations.
 */
public final class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final RealmResource realm;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the client belongs to.
     */
    public Client(final RealmResource realm) {
        super();
        this.realm = realm;
    }

    /**
     * Creates a typical Open ID connect client with client secret and redirect URI.
     * 
     * @param clientId
     *            Client identifier.
     * @param secret
     *            Secret for the client.
     * @param uri
     *            Redirect URI.
     * 
     * @return New client.
     */
    public final ClientResource createOpenIdConnectWithSecret(final String clientId, final String secret, final String uri) {

        final ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setProtocol("openid-connect");
        client.setPublicClient(false);
        final List<String> redirectUris = new ArrayList<>();
        redirectUris.add(uri);
        client.setRedirectUris(redirectUris);
        client.setSecret(secret);
        client.setClientAuthenticatorType("client-secret");
        client.setStandardFlowEnabled(true);
        client.setDirectAccessGrantsEnabled(true);
        client.setServiceAccountsEnabled(true);

        return create(clientId, client);

    }

    /**
     * Creates a client based on a representation.
     * 
     * @param clientId
     *            Client identifier.
     * @param representation
     *            Representation to use.
     * 
     * @return New client.
     */
    public final ClientResource create(String clientId, ClientRepresentation representation) {

        try (final Response response = realm.clients().create(representation)) {
            KcaUtils.ensureCreated("client " + clientId, response);
            String id = KcaUtils.extractId(response);
            LOG.debug("Created client '{}'", clientId);
            return realm.clients().get(id);
        }

    }

    /**
     * Locates a user by it's client identifier.
     * 
     * @param clientId
     *            Client ID of user to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    public final ClientRepresentation find(final String clientId) {
        final List<ClientRepresentation> clients = realm.clients().findAll();
        if (clients == null) {
            return null;
        }
        for (final ClientRepresentation client : clients) {
            if (clientId.equals(client.getClientId())) {
                LOG.debug("Found client '{}'", clientId);
                return client;
            }
        }
        return null;
    }

    /**
     * Locates a user by it's client identifier or fails with a runtime exception if it does not exist.
     * 
     * @param clientId
     *            Client ID of user to find.
     * 
     * @return Representation.
     */
    public final ClientRepresentation findOrFail(final String clientId) {
        final ClientRepresentation client = find(clientId);
        if (client == null) {
            throw new RuntimeException("Client '" + clientId + "' should exist, but was not found");
        }
        LOG.debug("Found client '{}'", clientId);
        return client;
    }

    /**
     * Locates a user by it's client identifier or creates it if it does not exist yet.<br>
     * Creates a typical Open ID connect client with client secret and redirect.
     * 
     * @param clientId
     *            Client identifier.
     * @param secret
     *            Secret for the client.
     * @param uri
     *            Redirect URI.
     * 
     * @return Resource.
     */
    public final ClientResource findOrCreateOpenIdConnectWithSecret(final String clientId, final String secret, final String uri) {
        final ClientRepresentation cr = find(clientId);
        if (cr == null) {
            return createOpenIdConnectWithSecret(clientId, secret, uri);
        }
        return realm.clients().get(cr.getId());
    }

    /**
     * Locates a user by it's client identifier or creates it if it does not exist yet.
     * 
     * @param clientId
     *            Client ID of user to find.
     * @param representation
     *            Representation to use in case a new client needs to be created.
     * 
     * @return Resource.
     */
    public final ClientResource findOrCreate(final String clientId, final ClientRepresentation representation) {
        final ClientRepresentation cr = find(clientId);
        if (cr == null) {
            return create(clientId, representation);
        }
        return realm.clients().get(cr.getId());
    }

}
