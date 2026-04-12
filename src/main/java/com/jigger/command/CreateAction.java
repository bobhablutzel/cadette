package com.jigger.command;

import com.jigger.SceneManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class CreateAction implements UndoableAction {

    private final SceneManager scene;
    private final String name;
    private final String shapeType;
    private final Vector3f position;
    private final Vector3f size;
    private final ColorRGBA color;

    public CreateAction(SceneManager scene, String name, String shapeType,
                        Vector3f position, Vector3f size, ColorRGBA color) {
        this.scene = scene;
        this.name = name;
        this.shapeType = shapeType;
        this.position = position;
        this.size = size;
        this.color = color;
    }

    @Override
    public void undo() {
        scene.deleteObject(name);
    }

    @Override
    public void redo() {
        scene.createObject(name, shapeType, position, size, color);
    }

    @Override
    public String description() {
        return "create " + shapeType + " \"" + name + "\"";
    }
}
