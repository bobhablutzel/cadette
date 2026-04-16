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

package com.jigger;

import com.jigger.model.Joint;
import com.jigger.model.JointType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JoineryTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() { resetScene();
    }

    @Test
    void testDadoJoint() {
        exec("create base-cabinet K w 500 h 600 d 400");
        String result = exec("join \"K/left-side\" to \"K/bottom\" with dado depth 9");
        System.out.println(result);

        assertTrue(result.contains("Dado"), "Should report dado joint");
        assertTrue(result.contains("depth"), "Should report depth");

        // Verify joint is registered
        var joints = sceneManager.getJointRegistry().getJointsForPart("K/left-side");
        assertFalse(joints.isEmpty(), "Should have joints for left-side");

        Joint dado = joints.get(0);
        assertEquals(JointType.DADO, dado.getType());
        assertEquals(9f, dado.getDepthMm(), 0.1f);
        assertEquals("K/left-side", dado.getReceivingPartName());
        assertEquals("K/bottom", dado.getInsertedPartName());
    }

    @Test
    void testPocketScrewJoint() {
        exec("create base-cabinet K w 500 h 600 d 400");
        String result = exec("join \"K/left-side\" to \"K/top-stretcher\" with pocket screws 3 spacing 150");
        System.out.println(result);

        assertTrue(result.contains("Pocket screw"), "Should report pocket screw");

        var joints = sceneManager.getJointRegistry().getJointsForPart("K/top-stretcher");
        assertFalse(joints.isEmpty());

        Joint ps = joints.get(0);
        assertEquals(JointType.POCKET_SCREW, ps.getType());
        assertEquals(3, ps.getScrewCount());
        assertEquals(150f, ps.getScrewSpacingMm(), 0.1f);
    }

    @Test
    void testButtJoint() {
        exec("create base-cabinet K w 500 h 600 d 400");
        String result = exec("join \"K/left-side\" to \"K/back\" with butt");
        System.out.println(result);
        assertTrue(result.contains("Butt joint"));
    }

    @Test
    void testShowJoints() {
        exec("create base-cabinet K w 500 h 600 d 400");
        exec("join \"K/left-side\" to \"K/bottom\" with dado depth 9");
        exec("join \"K/right-side\" to \"K/bottom\" with dado depth 9");
        exec("join \"K/left-side\" to \"K/top-stretcher\" with pocket screws 2");

        String result = exec("show joints");
        System.out.println(result);

        assertTrue(result.contains("Dado"), "Should list dado joints");
        assertTrue(result.contains("Pocket screw"), "Should list pocket screw");
        assertTrue(result.contains("2x Dado"), "Summary should show 2 dados");
    }

    @Test
    void testUndoJoin() {
        // Use individual parts to avoid template's built-in joints
        exec("create part \"side\" size 400,600 at 0,0,0");
        exec("create part \"shelf\" size 300,400 at 0,0,0");

        exec("join \"side\" to \"shelf\" with dado depth 9");
        assertFalse(sceneManager.getJointRegistry().getAllJoints().isEmpty(),
                "Should have a joint after join command");

        exec("undo");
        assertTrue(sceneManager.getJointRegistry().getAllJoints().isEmpty(),
                "Undo should remove the joint");

        exec("redo");
        assertFalse(sceneManager.getJointRegistry().getAllJoints().isEmpty(),
                "Redo should restore the joint");
    }

    @Test
    void testShowInfoWithJoints() {
        exec("create base-cabinet K w 500 h 600 d 400");
        exec("join \"K/left-side\" to \"K/bottom\" with dado depth 9");

        String info = exec("show info \"K/left-side\"");
        System.out.println(info);

        assertTrue(info.contains("Joints:"), "Info should show joints section");
        assertTrue(info.contains("receives"), "Left side receives the bottom");
    }

    @Test
    void testDadoDefaultDepth() {
        exec("create base-cabinet K w 500 h 600 d 400");
        // No depth specified — should default to half the receiving material thickness
        String result = exec("join \"K/left-side\" to \"K/bottom\" with dado");
        System.out.println(result);

        var joints = sceneManager.getJointRegistry().getJointsForPart("K/left-side");
        Joint dado = joints.get(0);
        // Default plywood is 18mm, half = 9mm
        assertEquals(9f, dado.getDepthMm(), 0.1f, "Default dado depth should be half material thickness");
    }

    @Test
    void testDadoDepthCapped() {
        exec("create base-cabinet K w 500 h 600 d 400");
        // Depth exceeds material thickness (18mm) — should be capped
        String result = exec("join \"K/left-side\" to \"K/bottom\" with dado depth 25");
        System.out.println(result);

        assertTrue(result.contains("Warning"), "Should warn about capping");

        var joints = sceneManager.getJointRegistry().getJointsForPart("K/left-side");
        Joint dado = joints.get(0);
        assertEquals(18f, dado.getDepthMm(), 0.1f, "Depth should be capped at material thickness");
    }

    @Test
    void testThinMaterialWarning() {
        exec("create base-cabinet K w 500 h 600 d 400");
        // Back is 5.5mm hardboard — too thin to receive a dado
        String result = exec("join \"K/back\" to \"K/bottom\" with dado");
        System.out.println(result);

        assertTrue(result.contains("Warning"), "Should warn about thin material");
        assertTrue(result.contains("reversing"), "Should suggest reversing direction");
    }

    @Test
    void testJoinInTemplate() {
        // Joints should work inside template definitions
        exec("define \"joined-box\" params width, height, depth");
        exec("create part \"left\" size $depth, $height at 0,0,0 grain vertical");
        exec("rotate \"left\" 0,90,0");
        exec("create part \"bottom\" size $width, $depth at 0,0,0");
        exec("rotate \"bottom\" -90,0,0");
        exec("join \"left\" to \"bottom\" with dado depth 9");
        exec("end define");

        String result = exec("create joined-box JB w 500 h 300 d 400");
        System.out.println(result);

        // Joints should exist with prefixed names
        var joints = sceneManager.getJointRegistry().getJointsForPart("JB/left");
        assertFalse(joints.isEmpty(), "Template joints should be created with prefixed names");
        assertEquals("JB/left", joints.get(0).getReceivingPartName());
        assertEquals("JB/bottom", joints.get(0).getInsertedPartName());
    }
}
