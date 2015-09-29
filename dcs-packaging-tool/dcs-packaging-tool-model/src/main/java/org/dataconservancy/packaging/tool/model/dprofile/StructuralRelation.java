package org.dataconservancy.packaging.tool.model.dprofile;

import java.net.URI;

public interface StructuralRelation {
    public URI getHasParentPredicate();
    
    public URI getHasChildPredicate();
}
