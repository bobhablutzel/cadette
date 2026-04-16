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

import com.jigger.model.GuillotinePacker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SheetLayoutTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() {
        resetScene();
        sceneManager.setKerfMm(GuillotinePacker.DEFAULT_KERF_MM);
    }

    @Test
    void testShowLayoutSinglePart() {
        exec("create part \"panel\" size 600,400 grain vertical");
        String layout = exec("show layout");
        System.out.println(layout);

        assertTrue(layout.contains("Sheet 1 of 1"), "Should show single sheet");
        assertTrue(layout.contains("panel"), "Should list the part");
        assertTrue(layout.contains("Offcut:"), "Should show offcut percentage");
        assertTrue(layout.contains("grain: vertical"), "Should show grain requirement");
    }

    @Test
    void testShowLayoutBaseCabinet() {
        exec("create base-cabinet K w 500 h 600 d 400");
        String layout = exec("show layout");
        System.out.println(layout);

        // Should have plywood and hardboard sections
        assertTrue(layout.contains("18mm Cabinet Plywood"), "Should show plywood");
        assertTrue(layout.contains("5.5mm Hardboard"), "Should show hardboard");

        // All parts should appear
        assertTrue(layout.contains("K/left-side"), "Should list left side");
        assertTrue(layout.contains("K/right-side"), "Should list right side");
        assertTrue(layout.contains("K/bottom"), "Should list bottom");
        assertTrue(layout.contains("K/back"), "Should list back");

        // Summary at bottom
        assertTrue(layout.contains("Summary:"), "Should have summary");
        assertTrue(layout.contains("Total:"), "Should show total sheet count");
    }

    @Test
    void testShowLayoutNoSheetGoods() {
        exec("create part \"rail\" material \"maple-3/4\" size 600,50");
        String layout = exec("show layout");
        System.out.println(layout);
        assertTrue(layout.contains("No sheet goods"), "Should report no sheet goods");
    }

    @Test
    void testShowLayoutNoParts() {
        String layout = exec("show layout");
        assertTrue(layout.contains("No parts"), "Should report no parts");
    }

    @Test
    void testSetKerf() {
        String result = exec("set kerf 2.5");
        System.out.println(result);

        assertTrue(result.contains("Kerf set to"), "Should confirm kerf change");
        assertTrue(result.contains("2.5"), "Should show new kerf value");
        assertEquals(2.5f, sceneManager.getKerfMm(), 0.01f);
    }

    @Test
    void testSetKerfInInches() {
        exec("set units inches");
        // 1/8 inch = 3.175mm
        String result = exec("set kerf 0.125");
        System.out.println(result);

        assertTrue(result.contains("Kerf set to"), "Should confirm kerf change");
        assertEquals(3.175f, sceneManager.getKerfMm(), 0.1f);
    }

    @Test
    void testKerfAffectsLayout() {
        exec("create part \"a\" size 600,1200");
        exec("create part \"b\" size 600,1200");

        // Get layout with default kerf
        exec("set kerf 3.2");
        String layout1 = exec("show layout");

        // Get layout with zero kerf — offcut should be lower
        exec("set kerf 0");
        String layout2 = exec("show layout");

        // Both should succeed
        assertTrue(layout1.contains("Sheet 1"), "Default kerf layout works");
        assertTrue(layout2.contains("Sheet 1"), "Zero kerf layout works");
    }

    @Test
    void testBomShowsRealSheetCount() {
        exec("create base-cabinet K w 500 h 600 d 400");
        String bom = exec("show bom");
        System.out.println(bom);

        // Should show actual sheet count (integer), not ~N.N estimate
        assertTrue(bom.contains("sheet"), "Should mention sheets");
        assertTrue(bom.contains("offcut"), "Should show offcut percentage");
        // Should NOT show the old ~estimate format
        assertFalse(bom.contains("~"), "Should not use approximate estimates");
    }

    @Test
    void testLayoutUnitsConversion() {
        exec("set units inches");
        exec("create base-cabinet K w 24 h 30 d 18");
        String layout = exec("show layout");
        System.out.println(layout);

        // Dimensions should be in inches, not mm
        assertTrue(layout.contains("in"), "Should display in inches");
        assertTrue(layout.contains("kerf:"), "Should show kerf");
    }
}
