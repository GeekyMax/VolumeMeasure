package com.geekymax.volumemeasure.entity;

import android.util.Log;

import com.geekymax.volumemeasure.manager.BoxManager;
import com.geekymax.volumemeasure.manager.MaterialManager;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

public class BoxEdge {
    private static final String TAG = "Geeky-BoxEdge";
    private List<BoxVertex> vertices = new ArrayList<>();
    private Node parent;
    private Node node;
    private Material material;

    private int i;
    private int j;
    // 顶面四条棱关联四个侧面
    private BoxFace relativeSideFace;

    public BoxEdge(Node parent, Material material) {
        this.parent = parent;
        this.material = material;
    }

    public void update() {
        if (node == null) {
            node = new Node();
            node.setOnTapListener((hitTestResult, motionEvent) -> {
                Log.d(TAG, "onTap: i=" + i + ";j=" + j);
                if (relativeSideFace != null) {
                    BoxManager.getInstance().setSelectedFace(relativeSideFace);
                }
            });

        }
        Vector3 difference = Vector3.subtract(vertices.get(0).getPosition(), vertices.get(1).getPosition());

        Vector3 directionFromTopToBottom = difference.normalized();
        float length = difference.length();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
        ModelRenderable line = ShapeFactory.makeCube(new Vector3(0.01f, 0.01f, length), Vector3.zero(), material);
        line.setShadowCaster(false);
        line.setShadowReceiver(false);
        node.setRenderable(line);
        node.setParent(parent);
        node.setLocalPosition(Vector3.add(vertices.get(0).getPosition(), vertices.get(1).getPosition()).scaled(0.5f));
        node.setLocalRotation(rotationFromAToB);
    }

    public void addVertex(BoxVertex v) {
        v.addRelativeEdge(this);
        this.vertices.add(v);
    }

    public void setRelativeSideFace(BoxFace relativeSideFace) {
        Log.d(TAG, "setRelativeSideFace: ");
        this.relativeSideFace = relativeSideFace;
    }

    public void set(int i, int j) {
        this.i = i;
        this.j = j;
    }
}
