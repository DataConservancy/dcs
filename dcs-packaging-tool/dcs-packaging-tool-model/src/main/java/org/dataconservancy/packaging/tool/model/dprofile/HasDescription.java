package org.dataconservancy.packaging.tool.model.dprofile;

/**
 * A human readable label and description.
 */
public interface HasDescription {
    /**
     * @return Short label for something.
     */
    public String getLabel();

    /**
     * @return Description of something.
     */
    public String getDescription();
}
