package com.jigger.command;

import com.jigger.SceneManager;
import com.jigger.model.Part;

public class CreatePartAction implements UndoableAction {

    private final SceneManager scene;
    private final Part part;

    public CreatePartAction(SceneManager scene, Part part) {
        this.scene = scene;
        this.part = part;
    }

    @Override
    public void undo() {
        scene.deleteObject(part.getName());
    }

    @Override
    public void redo() {
        scene.createPart(part);
    }

    @Override
    public String description() {
        return "create part \"" + part.getName() + "\" (" + part.getMaterial().getName() + ")";
    }
}
