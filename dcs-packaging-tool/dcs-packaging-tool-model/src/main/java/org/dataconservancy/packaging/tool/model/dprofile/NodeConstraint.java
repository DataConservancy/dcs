package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;
import java.util.List;

/**
 * A node constraint represents requirements on a node.
 */
public interface NodeConstraint {
    /**
     * @return The type the constrained node must be
     */
    NodeType getNodeType();

    /**
     * @return The constrained node must be pointed to by exactly one of these
     *         domain predicates
     */
    List<URI> getDomainPredicates();
}
