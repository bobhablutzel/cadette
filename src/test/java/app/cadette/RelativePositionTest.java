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
 * Source: https://github.com/bobhablutzel/cadette
 */

package app.cadette;

import app.cadette.model.Assembly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for relative positioning (to left/right/etc. of) and align command.
 */
class RelativePositionTest extends HeadlessTestBase {

    @BeforeEach
    void setup() {
        resetScene();
    }

    @Test
    void moveToRightOfAssembly() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 600 height 900 depth 400");

        String result = exec("move b to right of a");
        assertFalse(result.contains("error"), "Should succeed: " + result);

        // b's AABB min X should be at a's AABB max X
        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[1].x, bBBox[0].x, 1f, "b should be immediately right of a");
    }

    @Test
    void moveToLeftOfAssembly() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 600 height 900 depth 400");

        exec("move b to left of a");

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[0].x, bBBox[1].x, 1f, "b's right edge should meet a's left edge");
    }

    @Test
    void moveWithGap() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 600 height 900 depth 400");

        exec("move b to right of a gap 50");

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[1].x + 50f, bBBox[0].x, 1f, "Should have 50mm gap");
    }

    @Test
    void createWithRelativePosition() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        String result = exec("create base_cabinet \"b\" width 600 height 900 depth 400 to right of a");
        assertFalse(result.contains("error"), "Should succeed: " + result);

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[1].x, bBBox[0].x, 1f, "b should be right of a");
    }

    @Test
    void createWithRelativePositionAndGap() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 600 height 900 depth 400 to right of a gap 25");

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[1].x + 25f, bBBox[0].x, 1f, "Should have 25mm gap");
    }

    @Test
    void moveAboveAssembly() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create wall_cabinet \"wc\" width 600 height 400 depth 300");

        String result = exec("move wc to above a");
        assertFalse(result.contains("error"), "Should succeed: " + result);

        var aBBox = getAssemblyAABB("a");
        var wBBox = getAssemblyAABB("wc");
        assertEquals(aBBox[1].y, wBBox[0].y, 1f, "wc's bottom should meet a's top");
    }

    @Test
    void alignFrontOfMultipleAssemblies() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 500 height 900 depth 300");
        exec("create base_cabinet \"c\" width 400 height 900 depth 350");

        exec("move b to right of a");
        exec("move c to right of b");

        String result = exec("align front of b,c with a");
        assertTrue(result.contains("Aligned"), "Should confirm alignment");

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        var cBBox = getAssemblyAABB("c");
        assertEquals(aBBox[1].z, bBBox[1].z, 1f, "b's front should match a's front");
        assertEquals(aBBox[1].z, cBBox[1].z, 1f, "c's front should match a's front");
    }

    @Test
    void alignBackOfAssembly() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 500 height 900 depth 300");
        exec("move b to right of a");

        exec("align back of b with a");

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[0].z, bBBox[0].z, 1f, "b's back should match a's back");
    }

    @Test
    void alignTopOfAssembly() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 600 height 700 depth 400");
        exec("move b to right of a");

        exec("align top of b with a");

        var aBBox = getAssemblyAABB("a");
        var bBBox = getAssemblyAABB("b");
        assertEquals(aBBox[1].y, bBBox[1].y, 1f, "b's top should match a's top");
    }

    @Test
    void moveRelativeUndoable() {
        exec("create base_cabinet \"a\" width 600 height 900 depth 400");
        exec("create base_cabinet \"b\" width 600 height 900 depth 400");

        var origBBox = getAssemblyAABB("b");
        exec("move b to right of a");
        exec("undo");

        var restoredBBox = getAssemblyAABB("b");
        assertEquals(origBBox[0].x, restoredBBox[0].x, 1f, "Should restore original position");
    }

    /** Get rotation-aware AABB for an assembly. */
    private com.jme3.math.Vector3f[] getAssemblyAABB(String name) {
        Assembly assembly = sceneManager.getAssembly(name);
        assertNotNull(assembly, "Assembly " + name + " should exist");
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for (var part : assembly.getParts()) {
            var aabb = sceneManager.computeObjectAABB(part.getName());
            if (aabb != null) {
                minX = Math.min(minX, aabb[0].x);
                minY = Math.min(minY, aabb[0].y);
                minZ = Math.min(minZ, aabb[0].z);
                maxX = Math.max(maxX, aabb[1].x);
                maxY = Math.max(maxY, aabb[1].y);
                maxZ = Math.max(maxZ, aabb[1].z);
            }
        }
        return new com.jme3.math.Vector3f[]{
                new com.jme3.math.Vector3f(minX, minY, minZ),
                new com.jme3.math.Vector3f(maxX, maxY, maxZ)
        };
    }
}
