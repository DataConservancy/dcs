package org.dataconservancy.packaging.tool.model.fsdata;

import java.net.URI;
import java.util.List;

/**
 * Metadata about a file.
 */
public interface FileInfo {
    /**
     * @return Some known checksum of the bytestream or null if directory.
     */
    String getChecksum();

    /**
     * @return Name of the file.
     */
    String getName();

    /**
     * @return List of formats for the file.
     */
    List<URI> getFormats();

    /**
     * @return Size of the bytestream or -1 if directory.
     */
    long getSize();

    /**
     * @return Whether or not file has a bytestream.
     */
    boolean hasByteStream();

    /**
     * @return Last modification date as in File.lastModified().
     */
    long getLastModified();
}
