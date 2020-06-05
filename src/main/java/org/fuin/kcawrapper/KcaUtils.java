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

import java.net.URI;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Utility methods for the package.
 */
public final class KcaUtils {

    private KcaUtils() {
        throw new UnsupportedOperationException("It is not allowed to create an instance of this utiliyt class");
    }

    /**
     * Extracts the identifier from the response location.
     * 
     * @param typeAndName
     *            Type and name used in case of error messages.
     * @param response
     *            Response to extract the last part of the path from.
     * 
     * @return Unique identifier assigned by the l
     */
    @Nullable
    public static String extractId(@NotNull final Response response) {
        Validate.notNull(response, "response==null");

        final URI location = response.getLocation();
        if (location == null) {
            return null;
        }
        final String path = location.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Verifies if the create was successful and throws a runtime exception if not.
     * 
     * @param typeAndName
     *            Type and name used in case of an error message.
     * @param response
     *            Response to check for status 201.
     */
    public static void ensureCreated(@NotEmpty final String typeAndName, @NotNull final Response response) {
        Validate.notEmpty(typeAndName, "typeAndName==null or empty");
        Validate.notNull(response, "response==null");

        if (!response.getStatusInfo().equals(Response.Status.CREATED)) {
            final Response.StatusType statusInfo = response.getStatusInfo();
            throw new RuntimeException(
                    "Creating " + typeAndName + " failed with: #" + statusInfo.getStatusCode() + " / " + statusInfo.getReasonPhrase());
        }
    }

}
