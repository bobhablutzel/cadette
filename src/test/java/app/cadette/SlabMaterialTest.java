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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slab materials (granite, quartz) must flow through the BOM / cut list /
 * layout code paths without crashing and without getting shoved into sheet
 * packing. These tests cover the scenario of a real kitchen project: granite
 * counter on top of plywood base cabinets.
 */
class SlabMaterialTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    @Test
    void graniteCounterAppearsInBomWithoutCrash() {
        exec("create part \"counter\" material \"granite-3cm\" size 600, 2400 at 0, 0, 0");
        exec("create part \"cabinet_side\" material \"plywood-18mm\" size 600, 900 at 0, 0, 0");

        String bom = exec("show bom");
        System.out.println(bom);

        // BOM must list both materials.
        assertTrue(bom.contains("Granite") || bom.contains("granite"),
                "BOM should mention granite: " + bom);
        assertTrue(bom.contains("Plywood") || bom.contains("plywood"),
                "BOM should mention plywood: " + bom);
    }

    @Test
    void slabMaterialIsSkippedBySheetLayout() {
        exec("create part \"counter\" material \"granite-3cm\" size 600, 2400 at 0, 0, 0");
        // Also add a real sheet good so the layout has something to emit.
        exec("create part \"side\" material \"plywood-18mm\" size 400, 600 at 0, 0, 0");

        String layout = exec("show layout");
        System.out.println(layout);

        // Plywood should pack normally.
        assertTrue(layout.contains("Plywood") || layout.contains("plywood"),
                "layout should include plywood: " + layout);
        // Granite should NOT appear in any sheet packing — it's a slab.
        assertFalse(layout.contains("Granite Slab"),
                "layout output should not try to pack granite as a sheet: " + layout);
    }

    @Test
    void slabOnlySceneReportsNoSheetGoods() {
        exec("create part \"counter\" material \"granite-3cm\" size 600, 2400 at 0, 0, 0");
        String layout = exec("show layout");
        System.out.println(layout);

        // No sheet goods → the "nothing to pack" message, not a crash.
        assertTrue(layout.contains("No sheet goods"),
                "slab-only scene should report no sheet goods: " + layout);
    }

    @Test
    void cutListIncludesSlabPart() {
        exec("create part \"counter\" material \"granite-3cm\" size 600, 2400 at 0, 0, 0");
        String cutlist = exec("show cutlist");
        System.out.println(cutlist);
        // Slabs show up in the cut list just like any other part; the layout
        // output is where they're excluded, not the cut list.
        assertTrue(cutlist.contains("counter"),
                "cut list should include slab parts: " + cutlist);
    }
}
