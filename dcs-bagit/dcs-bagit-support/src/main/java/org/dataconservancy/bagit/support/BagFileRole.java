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

/**
 * Roles of tag files seen in a Bag.  Roles are defined independently of concrete files because: 1) a single role may
 * be served by multiple files; 2) names of files that fulfill these roles may change.
 * <p>
 * While changing the name of the {@code bagit.txt} file is hard to imagine, it is reasonable that additional files may
 * fulfill a particular role as the BagIt specification evolves.  If the name of {@code bagit.txt} does change, it is
 * likely that the role of a bag declaration will continue to be needed, even if it is not longer fulfilled by
 * {@code bagit.txt}.
 * </p>
 * <p>
 * BagIt requires that there be a bag declaration and a pay load manifest.  These roles are enumerated in this class as
 * {@link #BAG_DECL} and {@link #PAYLOAD_MANIFEST}, respectively.  Other roles such as a tag manifest, payload
 * directory, and fetch file are enumerated in this class.  A payload manifest role may be fulfilled by two different
 * files, a {@code manifest-sha1.txt} file containing SHA checksums, and a {@code manifest-md5.txt} file containing MD5
 * checksums.  In the future, implementations may use SHA-256 or other algorithms.  Regardless of the name of future
 * files, their role will be enumerated in this class.
 * </p>
 * <p>
 * The documentation for each role includes example file names from the specification, and are informative.  These are
 * meant to be examples in aiding the comprehension of what the role represents; they are not normative.
 * </p>
 */
public enum BagFileRole {

    /**
     * The bag payload (e.g. {@code data/}) directory.
     */
    PAYLOAD_DIRECTORY,

    /**
     * Bag payload itself (e.g. content in the {@code data/} directory.
     */
    PAYLOAD_CONTENT,

    /**
     * Tag file corresponding to the {@code bagit.txt} file, at the base of the bag.
     */
    BAG_DECL,

    /**
     * Tag file corresponding to the {@code bag-info.txt} file, at the base of the bag.
     */
    BAG_INFO,

    /**
     * Tag file(s) corresponding to the payload {@code manifest-&lt;algorithm&gt;.txt} file, at the base of the bag.
     */
    PAYLOAD_MANIFEST,

    /**
     * Tag file(s) corresponding to the {@code tagmanifest-&lt;algorithm&gt;.txt} file, at the base of the bag.
     */
    TAG_MANIFEST,

    /**
     * Tag file corresponding to the {@code fetch.txt} file, at the base of the bag.
     */
    FETCH,

    /**
     * Tag files corresponding to additional tag files, not covered by the BagIt specification.
     */
    OTHER_TAG
}
