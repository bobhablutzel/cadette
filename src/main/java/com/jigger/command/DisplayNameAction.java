package com.jigger.command;

import com.jigger.SceneManager;

public class DisplayNameAction implements UndoableAction {

    private final SceneManager scene;
    private final String objectName;
    private final boolean wasDisplayed;

    public DisplayNameAction(SceneManager scene, String objectName, boolean wasDisplayed) {
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
        scene.displayName(objectName);
    }

    @Override
    public String description() {
        return "display name \"" + objectName + "\"";
    }
}
