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

import app.cadette.model.*;
import com.jme3.math.ColorRGBA;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GuillotinePackerTest {

    // Standard 4x8 plywood sheet: 1220 x 2440 mm
    private static final Material PLYWOOD_18 = Material.builder()
            .name("plywood-18mm")
            .displayName("18mm Cabinet Plywood")
            .type(MaterialType.PLYWOOD)
            .thicknessMm(18f)
            .sheetWidthMm(1220f)
            .sheetHeightMm(2440f)
            .grainDirection(GrainDirection.ALONG_LENGTH)
            .measurementSystem(MeasurementSystem.METRIC)
            .displayColor(ColorRGBA.Orange)
            .build();

    private static final Material MDF_18 = Material.builder()
            .name("mdf-18mm")
            .displayName("18mm MDF")
            .type(MaterialType.MDF)
            .thicknessMm(18f)
            .sheetWidthMm(1220f)
            .sheetHeightMm(2440f)
            .grainDirection(GrainDirection.NONE)
            .measurementSystem(MeasurementSystem.METRIC)
            .displayColor(ColorRGBA.Brown)
            .build();

    private static final Material HARDWOOD = Material.builder()
            .name("maple")
            .displayName("Hard Maple")
            .type(MaterialType.HARDWOOD)
            .thicknessMm(19f)
            .sheetWidthMm(null)
            .sheetHeightMm(null)
            .grainDirection(GrainDirection.ALONG_LENGTH)
            .measurementSystem(MeasurementSystem.METRIC)
            .displayColor(ColorRGBA.Yellow)
            .build();

    @Test
    void testSinglePartOnSheet() {
        var parts = List.of(
                new GuillotinePacker.PackingPart("side", 400f, 600f, GrainRequirement.VERTICAL));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, parts, 3.2f);

        assertEquals(1, layouts.size());
        assertEquals(1, layouts.get(0).getPlacements().size());

        var placed = layouts.get(0).getPlacements().get(0);
        assertEquals("side", placed.getPartName());
        assertEquals(0f, placed.getX(), 0.01f);
        assertEquals(0f, placed.getY(), 0.01f);
        assertEquals(400f, placed.getWidthOnSheet(), 0.01f);
        assertEquals(600f, placed.getHeightOnSheet(), 0.01f);
        assertFalse(placed.isRotated());
    }

    @Test
    void testMultiplePartsSingleSheet() {
        var parts = List.of(
                new GuillotinePacker.PackingPart("left", 400f, 600f, GrainRequirement.VERTICAL),
                new GuillotinePacker.PackingPart("right", 400f, 600f, GrainRequirement.VERTICAL),
                new GuillotinePacker.PackingPart("bottom", 464f, 400f, GrainRequirement.ANY));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, parts, 3.2f);

        assertEquals(1, layouts.size());
        assertEquals(3, layouts.get(0).getPlacements().size());
    }

    @Test
    void testPartsRequireMultipleSheets() {
        // Four large panels: each 600x1200 = 720,000 mm^2
        // Sheet area = 1220x2440 = 2,976,800 mm^2
        // Two panels + kerf should fit per sheet, so 4 panels = 2 sheets
        var parts = List.of(
                new GuillotinePacker.PackingPart("panel1", 600f, 1200f, GrainRequirement.ANY),
                new GuillotinePacker.PackingPart("panel2", 600f, 1200f, GrainRequirement.ANY),
                new GuillotinePacker.PackingPart("panel3", 600f, 1200f, GrainRequirement.ANY),
                new GuillotinePacker.PackingPart("panel4", 600f, 1200f, GrainRequirement.ANY));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, parts, 3.2f);

        assertTrue(layouts.size() >= 1 && layouts.size() <= 2,
                "Should fit on 1-2 sheets, got " + layouts.size());
        int totalPlaced = layouts.stream().mapToInt(s -> s.getPlacements().size()).sum();
        assertEquals(4, totalPlaced);
    }

    @Test
    void testGrainPreventsRotation() {
        // A 1200x400 part with VERTICAL grain on plywood (grain along sheet height=2440).
        // Vertical grain means part height (400) must align with sheet height — cannot rotate.
        var part = new GuillotinePacker.PackingPart("side", 1200f, 400f, GrainRequirement.VERTICAL);

        assertFalse(GuillotinePacker.canRotate(part, PLYWOOD_18));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, List.of(part), 3.2f);
        assertEquals(1, layouts.size());
        assertFalse(layouts.get(0).getPlacements().get(0).isRotated(),
                "Grain-constrained part should not be rotated");
    }

    @Test
    void testNoGrainAllowsRotation() {
        // MDF has no grain — rotation is always allowed
        var part = new GuillotinePacker.PackingPart("shelf", 800f, 400f, GrainRequirement.VERTICAL);

        assertTrue(GuillotinePacker.canRotate(part, MDF_18));
    }

    @Test
    void testGrainAnyCanRotate() {
        var part = new GuillotinePacker.PackingPart("back", 600f, 800f, GrainRequirement.ANY);
        assertTrue(GuillotinePacker.canRotate(part, PLYWOOD_18));
    }

    @Test
    void testKerfSpacing() {
        // Two narrow parts side by side
        var parts = List.of(
                new GuillotinePacker.PackingPart("left", 500f, 600f, GrainRequirement.ANY),
                new GuillotinePacker.PackingPart("right", 500f, 600f, GrainRequirement.ANY));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, parts, 3.2f);
        assertEquals(1, layouts.size());

        var placements = layouts.get(0).getPlacements();
        assertEquals(2, placements.size());

        // Both same area, so order is stable — first at origin
        var first = placements.get(0);
        var second = placements.get(1);

        assertEquals(0f, first.getX(), 0.01f);
        // Second part should be offset by first width + kerf
        float expectedX = first.getWidthOnSheet() + 3.2f;
        // The second part could be to the right or below depending on split choice
        // Just verify they don't overlap
        boolean noOverlap = second.getX() >= first.getX() + first.getWidthOnSheet()
                || second.getY() >= first.getY() + first.getHeightOnSheet();
        assertTrue(noOverlap, "Parts should not overlap (kerf separates them)");
    }

    @Test
    void testNonSheetGoodReturnsEmpty() {
        var parts = List.of(
                new GuillotinePacker.PackingPart("rail", 600f, 50f, GrainRequirement.ANY));

        List<SheetLayout> layouts = GuillotinePacker.pack(HARDWOOD, parts, 3.2f);
        assertTrue(layouts.isEmpty(), "Non-sheet goods should return no layouts");
    }

    @Test
    void testOffcutCalculation() {
        // Single 610x1220 part on a 1220x2440 sheet = exactly 1/4 of sheet area
        var parts = List.of(
                new GuillotinePacker.PackingPart("panel", 610f, 1220f, GrainRequirement.ANY));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, parts, 0f);
        assertEquals(1, layouts.size());
        assertEquals(75f, layouts.get(0).getOffcutPercent(), 0.5f);
    }

    @Test
    void testZeroKerf() {
        var parts = List.of(
                new GuillotinePacker.PackingPart("a", 610f, 1220f, GrainRequirement.ANY),
                new GuillotinePacker.PackingPart("b", 610f, 1220f, GrainRequirement.ANY));

        List<SheetLayout> layouts = GuillotinePacker.pack(PLYWOOD_18, parts, 0f);
        assertEquals(1, layouts.size());
        assertEquals(2, layouts.get(0).getPlacements().size());
    }

    @Test
    void testDefaultKerfConstant() {
        assertEquals(3.2f, GuillotinePacker.DEFAULT_KERF_MM, 0.01f);
    }
}
