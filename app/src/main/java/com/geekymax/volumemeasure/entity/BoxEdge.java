package com.geekymax.volumemeasure.entity;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

public class BoxEdge {
    private BoxVertex firstVertex;
    private BoxVertex secondVertex;
    private AnchorNode anchorNode;
    private Node node;
    private Material material;
    private float length;
    private Quaternion rotationFromAToB;

    public BoxEdge(BoxVertex firstVertex, BoxVertex secondVertex, AnchorNode anchorNode, Material material) {
        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
        this.anchorNode = anchorNode;
        this.material = material;
    }

    public void update() {
        if (node == null) {
            node = new Node();
        }
        Vector3 difference = Vector3.subtract(firstVertex.getPosition(), secondVertex.getPosition());

        Vector3 directionFromTopToBottom = difference.normalized();
        length = difference.length();
        rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        ModelRenderable line = ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, length), Vector3.zero(), material);
        node.setRenderable(line);
        node.setParent(anchorNode);
        node.setLocalPosition(Vector3.add(firstVertex.getPosition(), secondVertex.getPosition()).scaled(0.5f));
        node.setLocalRotation(rotationFromAToB);
    }

}
