package com.jigger.command;

import com.jigger.SceneManager;
import com.jme3.math.Vector3f;

public class RotateAction implements UndoableAction {

    private final SceneManager scene;
    private final String name;
    private final Vector3f oldDegrees;
    private final Vector3f newDegrees;

    public RotateAction(SceneManager scene, String name, Vector3f oldDegrees, Vector3f newDegrees) {
        this.scene = scene;
        this.name = name;
        this.oldDegrees = oldDegrees;
        this.newDegrees = newDegrees;
    }

    @Override
    public void undo() {
        scene.rotateObject(name, oldDegrees);
    }

    @Override
    public void redo() {
        scene.rotateObject(name, newDegrees);
    }

    @Override
    public String description() {
        return "rotate \"" + name + "\"";
    }
}
