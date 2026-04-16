/*
 * Copyright 2026 Bob Hablutzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source: https://github.com/bobhablutzel/jigger
 */

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
        scene.markCutSheetDirty();
    }

    @Override
    public void redo() {
        scene.getJointRegistry().addJoint(joint);
        scene.markCutSheetDirty();
    }

    @Override
    public String description() {
        return "join \"" + joint.getReceivingPartName() + "\" to \""
                + joint.getInsertedPartName() + "\" with " + joint.getType().getDisplayName();
    }
}
