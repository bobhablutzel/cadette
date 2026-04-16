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

class TemplatePlacementTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() { resetScene();
    }

    @Test
    void testPlacementAtOrigin() {
        exec("create base-cabinet K w 500 h 600 d 400");

        System.out.println("\n=== At origin ===");
        debugPart("K/left-side");

        var left = bounds("K/left-side");
        assertNotNull(left);
        // Left side should start at X=0
        assertEquals(0f, left[0].x, 1f, "Left side min X at origin");
    }

    @Test
    void testPlacementOffset() {
        exec("create base-cabinet K w 500 h 600 d 400 at 1000,0,0");

        System.out.println("\n=== At 1000,0,0 ===");
        debugPart("K/left-side");
        debugPart("K/right-side");
        debugPart("K/bottom");

        var left = bounds("K/left-side");
        assertNotNull(left);
        // Left side should start at X=1000 (shifted by 1000)
        assertEquals(1000f, left[0].x, 1f, "Left side min X with offset");

        var right = bounds("K/right-side");
        assertNotNull(right);
        // Right side should be at X=1000+482=1482
        assertEquals(1482f, right[0].x, 1f, "Right side min X with offset");
    }

    @Test
    void testPlacementWithAtAlias() {
        exec("create base-cabinet K w 500 h 600 d 400 @ 500,200,0");

        var left = bounds("K/left-side");
        assertNotNull(left);
        assertEquals(500f, left[0].x, 1f, "Left side min X with @ alias");
        assertEquals(200f, left[0].y, 1f, "Left side min Y with @ alias");
    }

    @Test
    void testPlacementInCm() {
        exec("set units cm");
        exec("create base-cabinet K w 50 h 60 d 40 at 100,0,0");

        System.out.println("\n=== At 100cm ===");
        debugPart("K/left-side");

        var left = bounds("K/left-side");
        assertNotNull(left);
        // 100cm = 1000mm
        assertEquals(1000f, left[0].x, 1f, "Left side min X at 100cm");
    }

    @Test
    void testTwoCabinetsSideBySide() {
        exec("create base-cabinet K1 w 500 h 600 d 400");
        exec("create base-cabinet K2 w 500 h 600 d 400 at 500,0,0");

        var k1Right = bounds("K1/right-side");
        var k2Left = bounds("K2/left-side");
        assertNotNull(k1Right);
        assertNotNull(k2Left);

        System.out.println("\n=== Side by side ===");
        debugPart("K1/right-side");
        debugPart("K2/left-side");

        // K1's right side max X should be near K2's left side min X
        assertEquals(k1Right[1].x, k2Left[0].x, 2f,
                "K1 right edge should meet K2 left edge");
    }
}
