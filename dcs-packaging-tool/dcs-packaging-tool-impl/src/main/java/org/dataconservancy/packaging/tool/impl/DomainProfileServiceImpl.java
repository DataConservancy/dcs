package org.dataconservancy.packaging.tool.impl;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dataconservancy.packaging.tool.api.DomainProfileObjectStore;
import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.model.dprofile.DomainProfile;
import org.dataconservancy.packaging.tool.model.dprofile.NodeConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.NodeTransform;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyConstraint;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValue;
import org.dataconservancy.packaging.tool.model.ipm.Node;

public class DomainProfileServiceImpl implements DomainProfileService {
    private final DomainProfile profile;
    private final DomainProfileObjectStore objstore;
    private final Map<URI, NodeType> nodetypemap;

    public DomainProfileServiceImpl(DomainProfile profile, DomainProfileObjectStore objstore) {
        this.profile = profile;
        this.objstore = objstore;
        this.nodetypemap = new HashMap<>();

        for (NodeType nt : profile.getNodeTypes()) {
            nodetypemap.put(nt.getIdentifier(), nt);
        }
    }

    @Override
    public DomainProfile getDomainProfile() {
        return profile;
    }

    @Override
    public void addProperty(Node node, PropertyValue value) {
        objstore.addProperty(node.getDomainObject(), value);
    }

    @Override
    public void removeProperty(Node node, PropertyValue value) {
        objstore.removeProperty(node.getDomainObject(), value);
    }

    @Override
    public void removeProperty(Node node, PropertyType type) {
        objstore.removeProperty(node.getDomainObject(), type);
    }

    @Override
    public List<PropertyValue> getProperties(Node node) {
        return objstore.getProperties(node.getDomainObject());
    }

    @Override
    public List<PropertyValue> getProperties(Node node, PropertyType type) {
        return objstore.getProperties(node.getDomainObject(), type);
    }

    @Override
    public boolean validateProperties(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<PropertyConstraint> getPropertyConstraints(Node node) {
        return nodetypemap.get(node.getNodeType()).getPropertyConstraints();
    }

    @Override
    public String formatPropertyValue(PropertyValue value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PropertyValue parsePropertyValue(PropertyType type, String value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void transformNode(Node node, NodeTransform trans) {
        // TODO Auto-generated method stub
    }

    @Override
    public List<NodeTransform> getNodeTransforms(Node node) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean validateTree(Node root, boolean check_properties) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean meets_constraint(Node subject, Node object, NodeConstraint object_constraint) {
        if (object_constraint == null) {
            return true;
        }

        NodeType nodeType = object_constraint.getNodeType();

        URI nodeType2 = object.getNodeType();

        return false;
    }

    private boolean is_valid_type(Node node, NodeType type) {
        for (NodeConstraint c : type.getParentConstraints()) {
            if (meets_constraint(node, node.getParent(), c)) {
                return true;
            }
        }

        return false;
    }

    // TODO This method deals with borem type rules to influence what assignement happens?

    @Override
    public boolean assignNodeTypes(Node node) {
        Node parent = node.getParent();

        next: for (NodeType nt : profile.getNodeTypes()) {
            if (is_valid_type(node, nt)) {
                node.setNodeType(nt.getIdentifier());

                for (Node child : node.getChildren()) {
                    if (!assignNodeTypes(child)) {
                        continue next;
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public Node createTreeFromFileSystem(Path path) {
        return create_tree(null, path);
    }

    private Node create_tree(Node parent, Path path) {
        Node node = null;

        if (parent != null) {
            parent.addChild(node);
        }

        node.setIdentifier(URI.create("uuid:" + UUID.randomUUID().toString()));
        node.setDataLocation(path.toUri());

        // TODO Gather file metadata here and associate to data location?

        if (Files.isRegularFile(path)) {

        } else if (Files.isDirectory(path)) {
            try {
                Files.list(path).forEach(child_path -> create_tree(node, child_path));
            } catch (IOException e) {
                // TODO
                throw new RuntimeException(e);
            }
        } else {
            // TODO
        }

        return node;
    }

    @Override
    public void ignoreNode(Node node, boolean status) {
        if (node.isIgnored() == status) {
            return;
        }

        if (node.isIgnored()) {
            node.setIgnored(false);

            // Unignore ancestors
            for (Node n = node; n != null && n.isIgnored(); n = n.getParent()) {
                n.setIgnored(false);
            }

            // Unignore descendants
            for (Node child : node.getChildren()) {
                ignoreNode(child, false);
            }
        } else {
            node.setIgnored(true);

            // Ignore descendants
            for (Node child : node.getChildren()) {
                ignoreNode(child, true);
            }
        }
    }
}
