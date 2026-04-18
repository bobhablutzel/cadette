/*
 * Copyright 2026 Bob Hablutzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source: https://github.com/bobhablutzel/cadette
 */

package app.cadette;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

/**
 * Creates a visual X (red), Y (green), Z (blue) axis indicator at the origin.
 */
public class AxisDisplay {

    private static final float AXIS_LENGTH = 1000f;   // 1 meter in mm
    private static final float AXIS_THICKNESS = 3f;    // visible at workshop scale
    private static final float TIP_RADIUS = 10f;

    private AxisDisplay() {}

    public static Node create(AssetManager assetManager) {
        Node axisNode = new Node("axes");

        // X axis — red
        axisNode.attachChild(createAxisLine(assetManager, "X",
                new Vector3f(AXIS_LENGTH / 2, 0, 0), new Vector3f(AXIS_LENGTH / 2, AXIS_THICKNESS, AXIS_THICKNESS),
                ColorRGBA.Red));
        axisNode.attachChild(createTip(assetManager, "X_tip",
                new Vector3f(AXIS_LENGTH, 0, 0), ColorRGBA.Red));

        // Y axis — green
        axisNode.attachChild(createAxisLine(assetManager, "Y",
                new Vector3f(0, AXIS_LENGTH / 2, 0), new Vector3f(AXIS_THICKNESS, AXIS_LENGTH / 2, AXIS_THICKNESS),
                ColorRGBA.Green));
        axisNode.attachChild(createTip(assetManager, "Y_tip",
                new Vector3f(0, AXIS_LENGTH, 0), ColorRGBA.Green));

        // Z axis — blue
        axisNode.attachChild(createAxisLine(assetManager, "Z",
                new Vector3f(0, 0, AXIS_LENGTH / 2), new Vector3f(AXIS_THICKNESS, AXIS_THICKNESS, AXIS_LENGTH / 2),
                ColorRGBA.Blue));
        axisNode.attachChild(createTip(assetManager, "Z_tip",
                new Vector3f(0, 0, AXIS_LENGTH), ColorRGBA.Blue));

        return axisNode;
    }

    private static Geometry createAxisLine(AssetManager am, String name,
                                           Vector3f center, Vector3f halfExtents, ColorRGBA color) {
        Box box = new Box(halfExtents.x, halfExtents.y, halfExtents.z);
        Geometry geom = new Geometry("axis_" + name, box);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(center);
        return geom;
    }

    private static Geometry createTip(AssetManager am, String name,
                                      Vector3f position, ColorRGBA color) {
        Sphere sphere = new Sphere(16, 16, TIP_RADIUS);
        Geometry geom = new Geometry("tip_" + name, sphere);
        Material mat = new Material(am, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(position);
        return geom;
    }
}
