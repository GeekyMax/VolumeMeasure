package com.geekymax.volumemeasure.manager;

import android.content.Context;

import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * AR材质管理器
 */
public class MaterialManager {
    private static final String TAG = "Geeky-MaterialManager";
    private static MaterialManager instance;
    private Context context;

    public static final String MATERIAL_VERTEX_DEFAULT = "vertex";
    public static final String MATERIAL_FACE_DEFAULT = "face";
    public static final String MATERIAL_EDGE_DEFAULT = "edge";
    public static final String MATERIAL_FACE_SELECTED = "face_selected";
    public static final String MATERIAL_EDGE_SELECTED = "edge_selected";
    private Map<String, Material> materialMap;

    private MaterialManager(Context context) {
        this.context = context;
        materialMap = new HashMap<>();
        initMaterial();
    }

    private void initMaterial() {
        MaterialFactory.makeTransparentWithColor(context, new Color(1f, 1f, 1f, 0.1f)).thenAccept(material -> {
            materialMap.put(MATERIAL_FACE_DEFAULT, material);
        });
        MaterialFactory.makeTransparentWithColor(context, new Color(1f, 0.3f, 0.3f, 0.1f)).thenAccept(material -> {
            materialMap.put(MATERIAL_FACE_SELECTED, material);
        });

        MaterialFactory.makeOpaqueWithColor(context, new Color(0.3f, 0.6f, 0.3f)).thenAccept(material -> {
            materialMap.put(MATERIAL_EDGE_DEFAULT, material);
        });
        MaterialFactory.makeOpaqueWithColor(context, new Color(0.1f, 0.9f, 0.1f)).thenAccept(material -> {
            materialMap.put(MATERIAL_EDGE_SELECTED, material);
        });
        MaterialFactory.makeOpaqueWithColor(context, new Color(0.3f, 0.3f, 0.6f)).thenAccept(material -> {
            materialMap.put(MATERIAL_VERTEX_DEFAULT, material);
        });
    }

    public static MaterialManager getInstance(Context context) {
        if (instance == null) {
            instance = new MaterialManager(context);
        }
        return instance;
    }

    public static MaterialManager getInstance() {
        return instance;
    }

    public Material getMaterial(String id) {
        return materialMap.get(id);
    }
}
