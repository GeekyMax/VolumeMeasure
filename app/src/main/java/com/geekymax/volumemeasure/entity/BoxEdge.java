package com.geekymax.volumemeasure.entity;

import android.util.Log;

import com.geekymax.volumemeasure.manager.MaterialManager;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

public class BoxEdge {
    private static final String TAG = "Geeky-BoxEdge";
    private BoxVertex firstVertex;
    private BoxVertex secondVertex;
    private Node parent;
    private Node node;
    private Material material;

    public BoxEdge(BoxVertex firstVertex, BoxVertex secondVertex, Node parent, Material material) {
        this.firstVertex = firstVertex;
        this.secondVertex = secondVertex;
        this.parent = parent;
        this.material = material;
    }

    public void update() {
        if (node == null) {
            node = new Node();
            node.setOnTapListener((hitTestResult, motionEvent) -> {
                Log.d(TAG, "update: hitTestResult:" + hitTestResult + "; motionEvent:" + motionEvent);
                this.material = MaterialManager.getInstance().getMaterial(MaterialManager.MATERIAL_EDGE_SELECTED);
                update();
            });

        }
        Vector3 difference = Vector3.subtract(firstVertex.getPosition(), secondVertex.getPosition());

        Vector3 directionFromTopToBottom = difference.normalized();
        float length = difference.length();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        ModelRenderable line = ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, length), Vector3.zero(), material);
        line.setShadowCaster(false);
        line.setShadowReceiver(false);
        node.setRenderable(line);
        node.setParent(parent);
        node.setLocalPosition(Vector3.add(firstVertex.getPosition(), secondVertex.getPosition()).scaled(0.5f));
        node.setLocalRotation(rotationFromAToB);
    }

}
