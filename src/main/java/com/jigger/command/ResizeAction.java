package com.jigger.command;

import com.jigger.SceneManager;
import com.jme3.math.Vector3f;

public class ResizeAction implements UndoableAction {

    private final SceneManager scene;
    private final String name;
    private final Vector3f oldSize;
    private final Vector3f newSize;

    public ResizeAction(SceneManager scene, String name, Vector3f oldSize, Vector3f newSize) {
        this.scene = scene;
        this.name = name;
        this.oldSize = oldSize;
        this.newSize = newSize;
    }

    @Override
    public void undo() {
        scene.resizeObject(name, oldSize);
    }

    @Override
    public void redo() {
        scene.resizeObject(name, newSize);
    }

    @Override
    public String description() {
        return "resize \"" + name + "\"";
    }
}
