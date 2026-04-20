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
 * Verify that the base cabinet template works correctly across different unit systems.
 * The top stretcher should always cap at 100mm regardless of display units.
 */
class BaseCabinetUnitsTest extends HeadlessTestBase {

    @BeforeEach
    void clearScene() { resetScene();
    }

    @Test
    void testStretcherInCm_deepCabinet() {
        exec("set units cm");
        // 30cm deep cabinet — stretcher should cap at 10cm (= 100mm)
        exec("create base_cabinet K width 50 height 60 depth 30");

        System.out.println("\n=== Base Cabinet (cm, depth 30) ===");
        debugPart("K/top-stretcher");

        var bounds = bounds("K/top-stretcher");
        assertNotNull(bounds, "stretcher should exist");
        // Stretcher depth in mm: should be 100mm = 10cm, not 30cm
        float stretcherDepthMm = Math.abs(bounds[1].z - bounds[0].z);
        assertEquals(100f, stretcherDepthMm, 2f,
                "Stretcher depth should be 100mm (10cm), not full cabinet depth");
    }

    @Test
    void testStretcherInCm_shallowCabinet() {
        exec("set units cm");
        // 8cm deep cabinet — stretcher should use full depth (80mm < 100mm)
        exec("create base_cabinet K width 50 height 60 depth 8");

        System.out.println("\n=== Base Cabinet (cm, depth 8) ===");
        debugPart("K/top-stretcher");

        var bounds = bounds("K/top-stretcher");
        assertNotNull(bounds, "stretcher should exist");
        float stretcherDepthMm = Math.abs(bounds[1].z - bounds[0].z);
        assertEquals(80f, stretcherDepthMm, 2f,
                "Stretcher depth should be 80mm (8cm) for shallow cabinet");
    }

    @Test
    void testStretcherInInches() {
        exec("set units inches");
        // 16" deep cabinet — stretcher should cap at ~3.94" (100mm)
        exec("create base_cabinet K width 20 height 24 depth 16");

        System.out.println("\n=== Base Cabinet (inches, depth 16) ===");
        debugPart("K/top-stretcher");

        var bounds = bounds("K/top-stretcher");
        assertNotNull(bounds, "stretcher should exist");
        float stretcherDepthMm = Math.abs(bounds[1].z - bounds[0].z);
        assertEquals(100f, stretcherDepthMm, 2f,
                "Stretcher depth should be ~100mm for deep cabinet in inches");
    }
}
