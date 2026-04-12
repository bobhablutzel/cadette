package com.jigger.command;

import com.jigger.SceneManager;
import com.jigger.model.Part;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class DeleteAction implements UndoableAction {

    private final SceneManager scene;
    private final String name;
    private final String shapeType;
    private final Vector3f position;
    private final Vector3f size;
    private final ColorRGBA color;
    private final Part part;  // non-null if this was a part, null for primitives

    /** Constructor for primitive objects. */
    public DeleteAction(SceneManager scene, String name, String shapeType,
                        Vector3f position, Vector3f size, ColorRGBA color) {
        this(scene, name, shapeType, position, size, color, null);
    }

    /** Constructor that captures part data for undo. */
    public DeleteAction(SceneManager scene, String name, String shapeType,
                        Vector3f position, Vector3f size, ColorRGBA color, Part part) {
        this.scene = scene;
        this.name = name;
        this.shapeType = shapeType;
        this.position = position;
        this.size = size;
        this.color = color;
        this.part = part;
    }

    @Override
    public void undo() {
        if (part != null) {
            scene.createPart(part);
        } else {
            scene.createObject(name, shapeType, position, size, color);
        }
    }

    @Override
    public void redo() {
        scene.deleteObject(name);
    }

    @Override
    public String description() {
        return "delete \"" + name + "\"";
    }
}
