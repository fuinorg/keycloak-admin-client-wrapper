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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
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
    private Client(@NotNull final Realm realm, @NotEmpty final String uuid, @NotEmpty final String clientId,
            @NotNull final ClientResource resource) {
        super();
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(uuid, "uuid==null or empty");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notNull(resource, "resource==null");

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
    @NotNull
    public final Realm getRealm() {
        return realm;
    }

    /**
     * Returns the unique identifier of the client.
     * 
     * @return ID that is used for GET operations on the client resource.
     */
    @NotEmpty
    public final String getUUID() {
        return uuid;
    }

    /**
     * Returns the client identifier.
     * 
     * @return Client ID.
     */
    @NotEmpty
    public final String getClientId() {
        return clientId;
    }

    /**
     * Returns the client resource.
     * 
     * @return Associated client resource.
     */
    @NotNull
    public final ClientResource getResource() {
        return resource;
    }

    /**
     * Returns the defined client roles.
     * 
     * @return All available client roles.
     */
    @NotNull
    public final Roles getRoles() {
        return new Roles(resource.roles().list());
    }

    /**
     * Returns the service account user.
     * 
     * @return User that represents the client.
     */
    @NotNull
    public final User getServiceAccountUser() {
        final UserRepresentation userRep = resource.getServiceAccountUser();
        final UserResource userResource = realm.getResource().users().get(userRep.getId());
        return new User(realm, userRep.getId(), userRep.getUsername(), userResource);
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
    @NotNull
    public static Client createOpenIdConnectWithSecret(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotEmpty final String secret, @NotEmpty final String uri, final boolean standardFlow, final boolean directAccessGrants) {

        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notEmpty(secret, "secret==null or empty");
        Validate.notEmpty(uri, "uri==null or empty");

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
    @NotNull
    public static Client createOpenIdConnectWithImplicit(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotEmpty final String uri) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notEmpty(uri, "uri==null or empty");

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
     * Creates a typical Open ID connect client with client Credentials Grant.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param clientSecret
     *            Client password.
     * 
     * @return New client.
     */
    @NotNull
    public static Client createOpenIdConnectWithClientCredentials(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotEmpty final String clientSecret) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notEmpty(clientSecret, "clientSecret==null or empty");

        final ClientRepresentation clientRep = new ClientRepresentation();
        clientRep.setClientId(clientId);
        clientRep.setProtocol("openid-connect");
        clientRep.setPublicClient(false);
        clientRep.setSecret(clientSecret);
        clientRep.setClientAuthenticatorType("client-secret");
        clientRep.setStandardFlowEnabled(false);
        clientRep.setDirectAccessGrantsEnabled(false);
        clientRep.setServiceAccountsEnabled(true);

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
    @NotNull
    public static Client create(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotNull final ClientRepresentation clientRep) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notNull(clientRep, "clientRep==null");

        try (final Response response = realm.getResource().clients().create(clientRep)) {
            KcaUtils.ensureCreated("client " + clientId, response);
            final String id = KcaUtils.extractId(response);
            LOG.debug("Created client '{}'", clientId);
            final ClientResource clientRes = realm.getResource().clients().get(id);
            return new Client(realm, id, clientId, clientRes);
        }

    }

    /**
     * Locates a client by it's client identifier.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            ID of client to find.
     * 
     * @return Client or {@literal null} if not found.
     */
    @Nullable
    public static Client find(@NotNull final Realm realm, @NotEmpty final String clientId) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");

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
     * Locates a client by it's client identifier or fails with a runtime exception if it does not exist.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            ID of client to find.
     * 
     * @return Client.
     */
    @NotNull
    public static Client findOrFail(@NotNull final Realm realm, @NotEmpty final String clientId) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");

        final Client client = find(realm, clientId);
        if (client == null) {
            throw new RuntimeException("Client '" + clientId + "' should exist, but was not found");
        }
        LOG.debug("Found client '{}'", clientId);
        return client;
    }

    /**
     * Locates a client by it's client identifier or creates it if it does not exist yet.<br>
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
     * @return Client.
     */
    @NotNull
    public static Client findOrCreateOpenIdConnectWithSecret(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotEmpty final String secret, @NotEmpty final String uri, final boolean standardFlow, final boolean directAccessGrants) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notEmpty(secret, "secret==null or empty");
        Validate.notEmpty(uri, "uri==null or empty");

        final Client client = find(realm, clientId);
        if (client == null) {
            return createOpenIdConnectWithSecret(realm, clientId, secret, uri, standardFlow, directAccessGrants);
        }
        return client;
    }

    /**
     * Locates a client by it's client identifier or creates it if it does not exist yet.<br>
     * Creates a typical Open ID connect client with implicit flow and redirect URI.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param uri
     *            Redirect URI.
     * 
     * @return Client.
     */
    @NotNull
    public static Client findOrCreateOpenIdConnectWithImplicit(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotEmpty final String uri) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notEmpty(uri, "uri==null or empty");

        final Client client = find(realm, clientId);
        if (client == null) {
            return createOpenIdConnectWithImplicit(realm, clientId, uri);
        }
        return client;
    }

    /**
     * Locates a client by it's identifier or creates it if it does not exist yet.<br>
     * Creates a typical Open ID connect client with Client Credentials Grant.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            Client identifier.
     * @param clientSecret
     *            Client password.
     * 
     * @return New client.
     */
    @NotNull
    public static Client findOrCreateOpenIdConnectWithClientCredentials(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotEmpty final String clientSecret) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notEmpty(clientSecret, "clientSecret==null or empty");

        final Client client = find(realm, clientId);
        if (client == null) {
            return createOpenIdConnectWithClientCredentials(realm, clientId, clientSecret);
        }
        return client;

    }

    /**
     * Locates a client by it's identifier or creates it if it does not exist yet.
     * 
     * @param realm
     *            Realm the client belongs to.
     * @param clientId
     *            ID of client to find.
     * @param representation
     *            Representation to use in case a new client needs to be created.
     * 
     * @return Client.
     */
    @NotNull
    public static Client findOrCreate(@NotNull final Realm realm, @NotEmpty final String clientId,
            @NotNull final ClientRepresentation representation) {
        Validate.notNull(realm, "realm==null");
        Validate.notEmpty(clientId, "clientId==null or empty");
        Validate.notNull(representation, "representation==null");

        final Client client = find(realm, clientId);
        if (client == null) {
            return create(realm, clientId, representation);
        }
        return client;
    }

}
