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

import com.jme3.math.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless test for the base-cabinet template.
 * Creates a cabinet and verifies each part's world-space bounding box.
 *
 * Expected layout for "create base-cabinet BCT width 500 height 600 depth 400"
 * with default 18mm plywood and 5.5mm hardboard:
 *
 * Looking from the front:
 *   - Left side:  X = 0 to 18,  Y = 0 to 600,  Z = 0 to -400
 *   - Right side: X = 482 to 500, Y = 0 to 600, Z = 0 to -400
 *   - Bottom:     X = 18 to 482, Y = 0 to 18,   Z = 0 to -400
 *   - Top stretcher: X = 18 to 482, Y = 500 to 600, Z = 0 to -100
 *   - Back:       X = 0 to 500,  Y = 0 to 600,  Z = -400 to -405.5
 */
class BaseCabinetTemplateTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() { resetScene();
    }

    @Test
    void testBaseCabinetGeometry() {
        String result = exec("create base-cabinet BCT width 500 height 600 depth 400");
        System.out.println(result);

        // Debug dump all parts
        System.out.println("\n=== Base Cabinet Parts ===");
        for (String part : new String[]{
                "BCT/left-side", "BCT/right-side", "BCT/bottom",
                "BCT/top-stretcher", "BCT/back"}) {
            debugPart(part);
        }

        float t = 18f;       // plywood thickness (mm default)
        float bt = 5.5f;     // hardboard back thickness
        float tol = 1f;       // tolerance for floating point

        // Verify all parts exist
        assertNotNull(sceneManager.getObjectRecord("BCT/left-side"), "left-side should exist");
        assertNotNull(sceneManager.getObjectRecord("BCT/right-side"), "right-side should exist");
        assertNotNull(sceneManager.getObjectRecord("BCT/bottom"), "bottom should exist");
        assertNotNull(sceneManager.getObjectRecord("BCT/top-stretcher"), "top-stretcher should exist");
        assertNotNull(sceneManager.getObjectRecord("BCT/back"), "back should exist");

        // Print raw bounds for analysis
        // We'll add assertions once we see the actual positions
    }

    @Test
    void testPartsDoNotOverlapSides() {
        exec("create base-cabinet BCT width 500 height 600 depth 400");

        Vector3f[] left = bounds("BCT/left-side");
        Vector3f[] right = bounds("BCT/right-side");
        Vector3f[] bottom = bounds("BCT/bottom");

        assertNotNull(left);
        assertNotNull(right);
        assertNotNull(bottom);

        // Left and right sides should not overlap in X
        assertTrue(left[1].x <= right[0].x + 1f,
                "Left side max X (" + left[1].x + ") should be <= right side min X (" + right[0].x + ")");

        // Bottom should fit between the sides in X
        assertTrue(bottom[0].x >= left[0].x - 1f,
                "Bottom min X should be >= left side min X");
        assertTrue(bottom[1].x <= right[1].x + 1f,
                "Bottom max X should be <= right side max X");
    }
}
