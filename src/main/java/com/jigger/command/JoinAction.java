package com.jigger.command;

import com.jigger.SceneManager;
import com.jigger.model.Joint;

public class JoinAction implements UndoableAction {

    private final SceneManager scene;
    private final Joint joint;

    public JoinAction(SceneManager scene, Joint joint) {
        this.scene = scene;
        this.joint = joint;
    }

    @Override
    public void undo() {
        scene.getJointRegistry().removeJoint(joint.getId());
    }

    @Override
    public void redo() {
        scene.getJointRegistry().addJoint(joint);
    }

    @Override
    public String description() {
        return "join \"" + joint.getReceivingPartName() + "\" to \""
                + joint.getInsertedPartName() + "\" with " + joint.getType().getDisplayName();
    }
}
