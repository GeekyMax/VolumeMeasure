package com.geekymax.volumemeasure.entity;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

public class BoxVertex {
    private Node parent;
    private Node node;
    private Material material;


    private Vector3 position;

    private List<BoxEdge> relativeEdges = new ArrayList<>();
    private List<BoxFace> relativeFaces = new ArrayList<>();

    public BoxVertex(Vector3 position, Node parent, Material material) {
        this.position = position;
        this.parent = parent;
        this.material = material;
    }

    public void update() {
        if (node == null) {
            node = new Node();
        }
        ModelRenderable renderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        renderable.setShadowCaster(false);
        renderable.setShadowReceiver(false);
        node.setRenderable(renderable);
        node.setParent(parent);
        node.setLocalPosition(position);
        relativeEdges.forEach(BoxEdge::update);
        relativeFaces.forEach(BoxFace::update);
    }


    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        this.relativeFaces.forEach(BoxFace::update);
        this.relativeEdges.forEach(BoxEdge::update);
    }

    public void addRelativeEdge(BoxEdge e) {
        this.relativeEdges.add(e);
    }

    public void addRelativeFace(BoxFace f) {
        this.relativeFaces.add(f);
    }

    public void clear() {
        if (this.node != null) {
            this.node.setParent(null);
            this.node.setEnabled(false);
        }
    }
}
