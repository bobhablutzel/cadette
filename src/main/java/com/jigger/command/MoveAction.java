package com.jigger.command;

import com.jigger.SceneManager;
import com.jme3.math.Vector3f;

public class MoveAction implements UndoableAction {

    private final SceneManager scene;
    private final String name;
    private final Vector3f oldPosition;
    private final Vector3f newPosition;

    public MoveAction(SceneManager scene, String name, Vector3f oldPosition, Vector3f newPosition) {
        this.scene = scene;
        this.name = name;
        this.oldPosition = oldPosition;
        this.newPosition = newPosition;
    }

    @Override
    public void undo() {
        scene.moveObject(name, oldPosition);
    }

    @Override
    public void redo() {
        scene.moveObject(name, newPosition);
    }

    @Override
    public String description() {
        return "move \"" + name + "\"";
    }
}
