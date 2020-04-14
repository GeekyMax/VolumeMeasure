package com.geekymax.volumemeasure.entity;

import android.util.Log;

import com.geekymax.volumemeasure.util.MathUtil;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

public class BoxFace {
    private static final String TAG = "Geeky-BoxFace";
    private AnchorNode anchorNode;
    private Node node;
    private List<BoxVertex> vertices; // 1-2-4-3为一个面
    private Material material;

    private static final float faceThickness = 0.005f;

    public BoxFace(AnchorNode anchorNode, Material material) {
        this.anchorNode = anchorNode;
        this.material = material;
        this.vertices = new ArrayList<>();
    }

    public BoxFace(AnchorNode anchorNode, List<BoxVertex> vertices, Material material) {
        this.anchorNode = anchorNode;
        this.vertices = vertices;
        this.material = material;

    }

    public void update() {
        if (node == null) {
            node = new Node();
        }
        Vector3 firstPos = vertices.get(0).getPosition();
        Vector3 secondPos = vertices.get(1).getPosition();
        Vector3 thirdPos = vertices.get(2).getPosition();
        Vector3 v1 = Vector3.subtract(secondPos, firstPos);
        Vector3 v2 = Vector3.subtract(thirdPos, secondPos);
        Log.d(TAG, "update: V1:" + v1 + ";V2:" + v2);
        float length1 = v1.length();
        float length2 = v2.length();
        Vector3 normal = MathUtil.getNormal(firstPos, secondPos, thirdPos);

        Quaternion rotation = MathUtil.getRotationQuaternion(v1.normalized(), normal, v2.normalized());
        Log.d(TAG, "update: normal:" + normal + "; rotation: " + rotation);
        ModelRenderable cube = ShapeFactory.makeCube(new Vector3(length1, faceThickness, length2), Vector3.zero(), material);
        node.setRenderable(cube);
        node.setParent(anchorNode);
        node.setLocalPosition(Vector3.add(firstPos, thirdPos).scaled(0.5f));
        Log.d(TAG, "update: local rotation:" + node.getLocalRotation());
        node.setLocalRotation(rotation);
    }

    public void addVertex(BoxVertex v) {
        this.vertices.add(v);
    }

}
