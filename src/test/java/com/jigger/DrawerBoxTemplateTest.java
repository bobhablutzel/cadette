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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrawerBoxTemplateTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() { resetScene();
    }

    @Test
    void testDrawerBoxGeometry() {
        String result = exec("create drawer-box D width 500 height 200 depth 400");
        System.out.println(result);

        System.out.println("\n=== Drawer Box Parts ===");
        for (String part : new String[]{
                "D/left-side", "D/right-side", "D/front",
                "D/back", "D/bottom"}) {
            debugPart(part);
        }

        // Sides should span full depth in Z
        // Back should be at the far end (Z ≈ -depth), not in the middle
        var back = bounds("D/back");
        assertNotNull(back, "back should exist");

        // The back's Z min should be near -depth (far end of the box)
        float depth = 400f;
        assertTrue(back[0].z < -depth + 20f,
                "Back min Z (" + back[0].z + ") should be near -" + depth);
    }
}
