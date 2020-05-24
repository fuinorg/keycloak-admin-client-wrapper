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
import org.keycloak.representations.idm.ClientRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for client related operations.
 */
public final class Client {

    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final Realm realm;

    private final String uuid;

    private final String clientId;

    private final ClientResource resource;

    /**
     * Constructor with mandatory parameters.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param uuid
     *            Unique client identifier.
     * @param clientId
     *            Unique client identifier.
     * @param resource
     *            Associated client resource.
     */
    private Client(final Realm realm, final String uuid, final String clientId, final ClientResource resource) {
        super();
        this.realm = realm;
        this.uuid = uuid;
        this.clientId = clientId;
        this.resource = resource;
    }

    /**
     * Returns the realm the client belongs to.
     * 
     * @return Realm.
     */
    public final Realm getRealm() {
        return realm;
    }

    /**
     * Returns the unique identifier of the client.
     * 
     * @return ID that is used for GET operations on the client resource.
     */
    public final String getUUID() {
        return uuid;
    }

    /**
     * Returns the client identifier.
     * 
     * @return Client ID.
     */
    public final String getClientId() {
        return clientId;
    }

    /**
     * Returns the client resource.
     * 
     * @return Associated client resource.
     */
    public final ClientResource getResource() {
        return resource;
    }

    /**
     * Returns the defined client roles.
     * 
     * @return All available client roles.
     */
    public final Roles getRoles() {
        return new Roles(resource.roles().list());
    }

    /**
     * Creates a typical Open ID connect client with client secret and redirect URI.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param secret
     *            Secret for the client.
     * @param uri
     *            Redirect URI.
     * @param standardFlow
     *            Enables the "Standard Flow" (Authorization Code Flow). This is recommend you use to authenticate and authorize with
     *            browser-based applications. Can be ussed for example by the Swagger UI.
     * @param uri
     *            Enables the "Direct Access Grants" (Resource Owner Password Credentials Grant) flow. This means "ServiceId/ServicePW" plus
     *            "UserName/UserPw" are used for authentication.
     * 
     * @return New client.
     */
    public static Client createOpenIdConnectWithSecret(final Realm realm, final String clientId, final String secret, final String uri,
            final boolean standardFlow, final boolean directAccessGrants) {

        final ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setClientId(clientId);
        clientRep.setProtocol("openid-connect");
        clientRep.setPublicClient(false);
        final List<String> redirectUris = new ArrayList<>();
        redirectUris.add(uri);
        clientRep.setRedirectUris(redirectUris);
        clientRep.setSecret(secret);
        clientRep.setClientAuthenticatorType("client-secret");
        clientRep.setStandardFlowEnabled(standardFlow);
        clientRep.setDirectAccessGrantsEnabled(directAccessGrants);

        return create(realm, clientId, clientRep);

    }

    /**
     * Creates a typical Open ID connect client with implicit flow and redirect URI.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param uri
     *            Redirect URI.
     * 
     * @return New client.
     */
    public static Client createOpenIdConnectWithImplicit(final Realm realm, final String clientId, final String uri) {

        final ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setClientId(clientId);
        clientRep.setProtocol("openid-connect");
        clientRep.setPublicClient(true);
        final List<String> redirectUris = new ArrayList<>();
        redirectUris.add(uri);
        clientRep.setRedirectUris(redirectUris);
        clientRep.setStandardFlowEnabled(false);
        clientRep.setImplicitFlowEnabled(true);
        clientRep.setDirectAccessGrantsEnabled(false);

        return create(realm, clientId, clientRep);

    }

    /**
     * Creates a client based on a representation.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param clientRep
     *            Representation to use.
     * 
     * @return New client.
     */
    public static Client create(final Realm realm, final String clientId, final ClientRepresentation clientRep) {

        try (final Response response = realm.getResource().clients().create(clientRep)) {
            KcaUtils.ensureCreated("client " + clientId, response);
            final String id = KcaUtils.extractId(response);
            LOG.debug("Created client '{}'", clientId);
            final ClientResource clientRes = realm.getResource().clients().get(id);
            return new Client(realm, id, clientId, clientRes);
        }

    }

    /**
     * Locates a user by it's client identifier.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client ID of user to find.
     * 
     * @return Representation or {@literal null} if not found.
     */
    public static Client find(final Realm realm, final String clientId) {
        final List<ClientRepresentation> clients = realm.getResource().clients().findAll();
        if (clients == null) {
            return null;
        }
        for (final ClientRepresentation clientRep : clients) {
            if (clientId.equals(clientRep.getClientId())) {
                LOG.debug("Found client '{}'", clientId);
                final ClientResource clientRes = realm.getResource().clients().get(clientRep.getId());
                return new Client(realm, clientRep.getId(), clientId, clientRes);
            }
        }
        return null;
    }

    /**
     * Locates a user by it's client identifier or fails with a runtime exception if it does not exist.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client ID of user to find.
     * 
     * @return Representation.
     */
    public static Client findOrFail(final Realm realm, final String clientId) {
        final Client client = find(realm, clientId);
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
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param secret
     *            Secret for the client.
     * @param uri
     *            Redirect URI.
     * @param standardFlow
     *            Enables the "Standard Flow" (Authorization Code Flow). This is recommend you use to authenticate and authorize with
     *            browser-based applications. Can be used for example for the Swagger UI.
     * @param uri
     *            Enables the "Direct Access Grants" (Resource Owner Password Credentials Grant) flow. This means "ServiceId/ServicePW" plus
     *            "UserName/UserPw" are used for authentication.
     * 
     * @return Resource.
     */
    public static Client findOrCreateOpenIdConnectWithSecret(final Realm realm, final String clientId, final String secret,
            final String uri, final boolean standardFlow, final boolean directAccessGrants) {
        final Client client = find(realm, clientId);
        if (client == null) {
            return createOpenIdConnectWithSecret(realm, clientId, secret, uri, standardFlow, directAccessGrants);
        }
        return client;
    }

    /**
     * Locates a user by it's client identifier or creates it if it does not exist yet.<br>
     * Creates a typical Open ID connect client with implicit flow and redirect URI.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param uri
     *            Redirect URI.
     * 
     * @return Resource.
     */
    public static Client findOrCreateOpenIdConnectWithImplicit(final Realm realm, final String clientId, final String uri) {
        final Client client = find(realm, clientId);
        if (client == null) {
            return createOpenIdConnectWithImplicit(realm, clientId, uri);
        }
        return client;
    }

    /**
     * Locates a user by it's client identifier or creates it if it does not exist yet.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client ID of user to find.
     * @param representation
     *            Representation to use in case a new client needs to be created.
     * 
     * @return Client.
     */
    public static Client findOrCreate(final Realm realm, final String clientId, final ClientRepresentation representation) {
        final Client client = find(realm, clientId);
        if (client == null) {
            return create(realm, clientId, representation);
        }
        return client;
    }

}
