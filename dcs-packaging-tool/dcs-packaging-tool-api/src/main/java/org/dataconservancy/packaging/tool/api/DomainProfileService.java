package org.dataconservancy.packaging.tool.api;

import java.nio.file.Path;
import java.util.List;

import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Service to perform operations on a tree of domain objects using a domain
 * profile.
 */
public interface DomainProfileService {
    /**
     * @return Domain profile used by service.
     */
    DomainProfile getDomainProfile();

    /**
     * Add a property to a node.
     * 
     * @param node
     * @param value
     */
    void addProperty(Node node, PropertyValue value);

    /**
     * Remove a particular property from a node.
     * 
     * @param node
     * @param value
     */
    void removeProperty(Node node, PropertyValue value);

    /**
     * Remove all properties of a given type from a node.
     * 
     * @param node
     * @param type
     */
    void removeProperty(Node node, PropertyType type);

    /**
     * @param node
     * @return All properties of a node.
     */
    List<PropertyValue> getProperties(Node node);

    /**
     * @param node
     * @param type
     * @return All properties of a node of a certain type.
     */
    List<PropertyValue> getProperties(Node node, PropertyType type);

    /**
     * Check that all the properties on a node satisfy constraints for that node
     * type and each property value satisfies the constraints for its property
     * type.
     * 
     * @param node
     * @return valid or invalid
     */
    boolean validateProperties(Node node);

    /**
     * Check that a property value satisfies the constraints of its property
     * type.
     * 
     * @param value
     * @return valid or invalid
     */
    boolean validatePropertyValue(PropertyValue value);

    /**
     * @return Property types which can be added to the node.
     */
    List<PropertyType> listAvailablePropertyTypes();

    /**
     * Format property value as a string according to its type and hint.
     * 
     * @param value
     * @return Formatted property value
     */
    String formatPropertyValue(PropertyValue value);

    /**
     * Attempt to parse a string into a property value according to its type and
     * hint.
     * 
     * @param value
     * @return value on success and null on failure
     */
    PropertyValue parsePropertyValue(String value);

    /**
     * Transform a node.
     * 
     * @param node
     * @param trans
     */
    void transformNode(Node node, NodeTransform trans);

    /**
     * @param node
     * @return All node transforms able to be performed on the node.
     */
    List<NodeTransform> getNodeTransforms(Node node);

    /**
     * Check if a tree satisfies all its node and optionally property
     * constraints.
     * 
     * @param root
     * @param check_properties
     * @return valid or invalid
     */
    boolean validateTree(Node root, boolean check_properties);

    /**
     * Attempt to assign node types to a tree such that it is valid with respect
     * to node types.
     * 
     * @param root
     * @return success or failure
     */
    boolean assignNodeTypes(Node root);

    /**
     * Create a valid tree from the file system.
     * 
     * @param path
     * @return root of tree
     */
    Node createTreeFromFileSystem(Path path);

    /**
     * Change the ignored status of a node. This may cause the types of other
     * nodes to change.
     * 
     * If a node is marked as ignored, all descendants are also marked as
     * ignored.
     * 
     * If a node is marked as not ignored, all descendants and ancestors are
     * also marked as not ignored.
     * 
     * @param node
     * @param status
     */
    void ignoreNode(Node node, boolean status);
}
