package org.dataconservancy.packaging.tool.model.ipm;

import java.net.URI;
import java.util.List;

/**
 * A node in the PTG tree which the user views and manipulates. The tree is a
 * DAG. The tree must contain at least one node, the root node.
 * 
 * Each node is associated with a domain object, file data, and a type. The type
 * of the node constrains its position in the tree and explains how it is mapped
 * to a type of domain object.
 */
public interface Node {
    /**
     * @return Unique identifier of the node in the tree.
     */
    URI getIdentifier();

    /**
     * @return Parent node or null if node is root.
     */
    Node getParent();

    /**
     * @return Children of node.
     */
    List<Node> getChildren();

    /**
     * @return Identifier of domain object associated with node.
     */
    URI getDomainObject();

    /**
     * @return Location of data associated with Node.
     */
    URI getDataLocation();

    /**
     * @return Type of the node.
     */
    URI getNodeType();

    /**
     * @return Ignored status.
     */
    boolean isIgnored();
}
