package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * The type of a node explains how to map a node to a domain object.
 *
 */
public interface NodeType extends HasDescription {
    /**
     * @return The type of the corresponding domain object
     */
    URI getDomainType();

    /**
     * Each child node must meet at least one of these constraints. A null
     * constraint is the case of having no children.
     * 
     * @return Constraints on child nodes.
     */
    List<NodeConstraint> getChildConstraints();

    /**
     * The parent node must meet at least one of these constraints. A null
     * constraint is the case of having no parent.
     * 
     * @return Constraints on parent node.
     */
    List<NodeConstraint> getParentConstraints();

    /**
     * @return Constraints on node properties.
     */
    List<PropertyConstraint> getPropertyConstraints();

    /**
     * @return Types of properties which may be inherited by descendants.
     */
    List<PropertyType> getInheritableProperties();

    /**
     * @return List of default property values the domain object must have on
     *         creation.
     */
    List<PropertyValue> getDefaultPropertyValues();

    /**
     * Some property values may be supplied by the system automatically. Such
     * types of property are assumed to have an identifier known to the profile.
     * This allows the profile to map system supplied properties to domain
     * properties. An example is the size of a file associated with a node.
     * 
     * TODO Just list such properties with enum?
     * 
     * @return Mapping of property types to system identifiers.
     */
    Map<PropertyType, URI> getSuppliedProperties();

    /**
     * @return Node must be associated with data.
     */
    boolean mustHaveData();
}
