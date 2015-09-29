package org.dataconservancy.packaging.tool.model.dprofile;

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
     * @return The constrained node must be in exactly one of these
     *         relationships.
     */
    List<StructuralRelation> getStructuralRelations();
}
