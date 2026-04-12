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
