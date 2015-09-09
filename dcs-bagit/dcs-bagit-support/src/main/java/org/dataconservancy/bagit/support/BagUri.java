/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.bagit.support;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URI scheme for addressing resources contained within a Bag.  The form of a Bag URI is:
 * {@code bag://<bag-name>/path/to/resource#optional-fragment}
 * <p>
 * The Bag URI {@link #BAG_SCHEME scheme} is equal to the string '{@code bag}'; resources inside the bag are unique
 * within the scope of a single bag. The {@code authority} component of a Bag URI is equal to the name of the Bag
 * serialization (as discussed in <a href="https://www.ietf.org/id/draft-kunze-bagit-11.txt">BagIt</a>
 * section 4), minus any file name extensions.  Query parameters are disallowed in Bag URIs, as they have no semantic
 * analog in the BagIt specification.
 * </p>
 *
 * @see <a href="https://www.ietf.org/id/draft-kunze-bagit-11.txt">BagIt Draft Specification version 0.97, expires December 25, 2015</a>
 * @see <a href="https://www.ietf.org/rfc/rfc2396.txt">RFC 2396: Uniform Resource Identifiers (URI): Generic Syntax</a>
 * @see <a href="http://dataconservancy.org/placeholder/for/bagit/profile">Data Conservancy BagIt Profile 1.0, section X</a>
 */
public class BagUri {

    /**
     * The value of the Bag URI {@code scheme} (RFC 2396 sec. 3)
     */
    public static final String BAG_SCHEME = "bag";

    /**
     * Characters that are reserved (i.e. illegal) for URI authority portion (RFC 2396 sec. 3.2)
     */
//    private static final char[] RESERVED_AUTHORITY_CHARACTERS = new char[] { ';', ':', '@', '?', '/' };

    private static final String ERR_NULL = "Argument '%s' must not be null or empty.";

    private static final String ERR_INVALID_SCHEME = "Invalid scheme '%s' for " + BagUri.class.getName() + ": scheme " +
            "must be equal to '" + BAG_SCHEME + "'";

    private static final String ERR_PARSE_URI = "Unable to parse URI string '%s': %s";

    private static final String ERR_CREATE_URI = "Unable to construct a URI with scheme '%s', authority '%s', path '%s', and fragment '%s': %s";

    /**
     * Internal representation of the BagUri as a java.net.URI.
     */
    private URI bagUri;

    /**
     * The authority string (must not be {@code null}).  It semantically aligns with, and should be equal to, the name
     * of the bag.  We keep this state for our own equals() and hashCode() implementation.
     */
    private String authority;

    /**
     * The path string (may be {@code null}).  We keep this state for our own equals() and hashCode() implementation.
     */
    private String path;

    /**
     * The fragment string (may be {@code null}).  We keep this state for our own equals() and hashCode()
     * implementation.
     */
    private String fragment;

    /**
     * Constructs a new Bag URI, which addresses a resource in a Bag named by {@code authority}.
     * <p>
     * Exemplars:<br/>
     * <ul>
     * <li>The {@code path} "data" with {@code authority} "mybag" would address the data directory inside
     * of a Bag named 'mybag': {@code bag://mybag/data}.</li>
     * <li>The {@code path} "bag-info.txt" would identify the Bag metadata file: {@code bag://mybag/bag-info.txt}.</li>
     * <li>The {@code path} "data/dataobject.rdf" with a {@code fragment} "#obj-3" would identify a resource
     * "{@code obj-3}" inside of the payload file {@code data/dataobject.rdf}:
     * {@code bag://mybag/data/dataobject#obj-3}.</li>
     * </ul>
     * </p>
     *
     * @param authority the authority portion of the URI, which is expected to be the Bag name.  Must not be
     *                  {@code null}.
     * @param path the path to the resource within the Bag
     * @param fragment an optional fragment identifier, useful for referencing individual resources within a file
     * @throws java.lang.IllegalArgumentException if any required parameters are {@code null} or invalid URI components.
     */
    public BagUri(String authority, String path, String fragment) {
        if (authority == null || authority.trim().length() == 0) {
            throw new IllegalArgumentException(String.format(ERR_NULL, "authority"));
        }
        try {
            bagUri = new URI(BAG_SCHEME, authority, path, null, fragment);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    String.format(ERR_CREATE_URI, BAG_SCHEME, authority, path, fragment, e.getMessage()), e);
        }

        this.authority = authority;
        this.path = path;
        this.fragment = fragment;
    }

    /**
     * TODO javadoc
     * @return
     */
    public String getAuthority() {
        return bagUri.getAuthority();
    }

    /**
     * TODO javadoc
     * @return
     */
    public String getFragment() {
        return bagUri.getFragment();
    }

    /**
     * TODO javadoc
     * @return
     */
    public String getPath() {
        return bagUri.getPath();
    }

    public URI asUri() {
        return bagUri;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Instances of this class are considered equal if their authority, path, and fragment components are equal.
     * </p>
     *
     * @param o the object to determine equivalence against.
     * @return {@code true} if the instances are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BagUri bagUri = (BagUri) o;

        if (authority != null ? !authority.equals(bagUri.authority) : bagUri.authority != null) return false;
        if (fragment != null ? !fragment.equals(bagUri.fragment) : bagUri.fragment != null) return false;
        if (path != null ? !path.equals(bagUri.path) : bagUri.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = authority != null ? authority.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        return result;
    }
}
