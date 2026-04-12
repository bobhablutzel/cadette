package com.jigger.command;

import com.jigger.SceneManager;
import com.jigger.model.Part;

import java.util.List;

/**
 * Undoable action for template instantiation.
 * Undo removes all parts created by the template. Redo re-creates them.
 */
public class CreateTemplateAction implements UndoableAction {

    private final SceneManager scene;
    private final String assemblyName;
    private final String templateName;
    private final List<Part> createdParts;

    public CreateTemplateAction(SceneManager scene, String assemblyName,
                                String templateName, List<Part> createdParts) {
        this.scene = scene;
        this.assemblyName = assemblyName;
        this.templateName = templateName;
        this.createdParts = List.copyOf(createdParts);
    }

    @Override
    public void undo() {
        for (Part part : createdParts.reversed()) {
            scene.deleteObject(part.getName());
        }
        scene.removeAssembly(assemblyName);
    }

    @Override
    public void redo() {
        for (Part part : createdParts) {
            scene.createPart(part);
        }
        scene.registerAssembly(new com.jigger.model.Assembly(assemblyName));
        for (Part part : createdParts) {
            scene.getAssembly(assemblyName).addPart(part);
        }
    }

    @Override
    public String description() {
        return "create " + templateName + " \"" + assemblyName + "\" (" + createdParts.size() + " parts)";
    }
}
