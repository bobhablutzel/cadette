package com.jigger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CutListBomTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() { resetScene();
    }

    @Test
    void testCutListGroupedByMaterial() {
        exec("create base-cabinet K w 500 h 600 d 400");
        exec("join \"K/left-side\" to \"K/bottom\" with dado depth 9");
        exec("join \"K/right-side\" to \"K/bottom\" with dado depth 9");

        String cutlist = exec("show cutlist");
        System.out.println(cutlist);

        // Should have two material sections
        assertTrue(cutlist.contains("18mm Cabinet Plywood"), "Should list plywood");
        assertTrue(cutlist.contains("5.5mm Hardboard"), "Should list hardboard");

        // Should show parts
        assertTrue(cutlist.contains("K/left-side"), "Should list left side");
        assertTrue(cutlist.contains("K/back"), "Should list back");

        // Should show grain requirements
        assertTrue(cutlist.contains("grain: vertical"), "Should show grain for sides");

        // Should show machining operations from dados
        assertTrue(cutlist.contains("dado"), "Should list dado operations");
        assertTrue(cutlist.contains("9.0mm deep"), "Should show dado depth");

        // Should show total count
        assertTrue(cutlist.contains("Total: 5 parts"), "Should show total");
    }

    @Test
    void testBomWithSheetEstimate() {
        exec("create base-cabinet K w 500 h 600 d 400");

        String bom = exec("show bom");
        System.out.println(bom);

        // Should show material counts
        assertTrue(bom.contains("4 pc"), "Should show 4 plywood parts");
        assertTrue(bom.contains("1 pc"), "Should show 1 hardboard part");

        // Should estimate sheet count for sheet goods
        assertTrue(bom.contains("sheets"), "Should estimate sheet count");
    }

    @Test
    void testBomWithFasteners() {
        exec("create base-cabinet K w 500 h 600 d 400");
        exec("join \"K/left-side\" to \"K/top-stretcher\" with pocket screws 3 spacing 150");
        exec("join \"K/right-side\" to \"K/top-stretcher\" with pocket screws 3 spacing 150");

        String bom = exec("show bom");
        System.out.println(bom);

        assertTrue(bom.contains("Pocket screws"), "Should list pocket screws");
        assertTrue(bom.contains("6"), "Should total 6 screws (3+3)");
    }

    @Test
    void testCutListInDifferentUnits() {
        exec("set units inches");
        // Create a cabinet in inches
        exec("create base-cabinet K w 24 h 30 d 18");

        String cutlist = exec("show cutlist");
        System.out.println(cutlist);

        // Dimensions should be in inches
        assertTrue(cutlist.contains("in"), "Should show inches");
        // Should not have mm-scale numbers for the parts
        assertFalse(cutlist.contains("609"), "Should not show mm values");
    }

    @Test
    void testCutListWithJoineryOperations() {
        exec("create base-cabinet K w 500 h 600 d 400");
        exec("join \"K/left-side\" to \"K/bottom\" with dado depth 9");
        exec("join \"K/left-side\" to \"K/top-stretcher\" with pocket screws 2");

        String cutlist = exec("show cutlist");
        System.out.println(cutlist);

        // Left side should show both operations
        // Find the section for left-side
        assertTrue(cutlist.contains("dado 9.0mm deep for \"K/bottom\""),
                "Should show dado operation on left side");
        assertTrue(cutlist.contains("pocket screw hole"),
                "Should show pocket screw operation on left side");
    }
}
