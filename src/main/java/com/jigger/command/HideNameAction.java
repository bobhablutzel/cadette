package com.jigger.command;

import com.jigger.SceneManager;

public class HideNameAction implements UndoableAction {

    private final SceneManager scene;
    private final String objectName;
    private final boolean wasDisplayed;

    public HideNameAction(SceneManager scene, String objectName, boolean wasDisplayed) {
        this.scene = scene;
        this.objectName = objectName;
        this.wasDisplayed = wasDisplayed;
    }

    @Override
    public void undo() {
        if (wasDisplayed) {
            scene.displayName(objectName);
        } else {
            scene.hideName(objectName);
        }
    }

    @Override
    public void redo() {
        scene.hideName(objectName);
    }

    @Override
    public String description() {
        return "hide name \"" + objectName + "\"";
    }
}
