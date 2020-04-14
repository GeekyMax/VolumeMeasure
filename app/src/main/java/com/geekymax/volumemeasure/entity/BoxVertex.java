package com.geekymax.volumemeasure.entity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;

public class BoxVertex {
    private AnchorNode anchorNode;
    private Node node;

    private Vector3 position;

    private Renderable renderable;

    public boolean x;
    public boolean y;
    public boolean z;

    public BoxVertex(Vector3 position, AnchorNode anchorNode, Renderable renderable) {
        this.position = position;
        this.anchorNode = anchorNode;
        this.renderable = renderable;
    }

    public void update() {
        if (node == null) {
            node = new Node();
            node.setRenderable(renderable);
        }
        node.setParent(anchorNode);
        node.setLocalPosition(position);
    }

    public void setPoint(boolean x, boolean y, boolean z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }
}
