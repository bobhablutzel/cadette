package com.jigger.command;

import com.jigger.SceneManager;
import com.jigger.model.Part;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import java.util.List;

/**
 * Records all objects that existed before a "delete all" so they can be restored.
 */
public class DeleteAllAction implements UndoableAction {

    private final SceneManager scene;
    private final List<ObjectSnapshot> snapshots;

    /** Snapshot of an object — includes the Part if it was a part-based object. */
    public record ObjectSnapshot(String name, String shapeType, Vector3f position,
                                 Vector3f size, ColorRGBA color, Part part) {
        /** Convenience constructor for primitives. */
        public ObjectSnapshot(String name, String shapeType, Vector3f position,
                              Vector3f size, ColorRGBA color) {
            this(name, shapeType, position, size, color, null);
        }
    }

    public DeleteAllAction(SceneManager scene, List<ObjectSnapshot> snapshots) {
        this.scene = scene;
        this.snapshots = snapshots;
    }

    @Override
    public void undo() {
        for (ObjectSnapshot s : snapshots) {
            if (s.part() != null) {
                scene.createPart(s.part());
            } else {
                scene.createObject(s.name(), s.shapeType(), s.position(), s.size(), s.color());
            }
        }
    }

    @Override
    public void redo() {
        scene.deleteAllObjects();
    }

    @Override
    public String description() {
        return "delete all (" + snapshots.size() + " objects)";
    }
}
