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

import app.cadette.model.Material;
import app.cadette.model.MaterialCatalog;
import app.cadette.model.MaterialKind;
import app.cadette.model.MaterialType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locks in the material classification: which substances map to which kinds,
 * and that every bundled material has a kind set (i.e. no null-kind leaks).
 */
class MaterialKindTest {

    @Test
    void everyBundledMaterialHasAKind() {
        for (Material m : MaterialCatalog.instance().getAll()) {
            assertNotNull(m.getKind(),
                    "Material '" + m.getName() + "' is missing a MaterialKind");
        }
    }

    @Test
    void sheetGoodsAreClassifiedCorrectly() {
        assertEquals(MaterialKind.SHEET_GOOD,
                MaterialCatalog.instance().get("plywood-3/4").getKind());
        assertEquals(MaterialKind.SHEET_GOOD,
                MaterialCatalog.instance().get("hardboard-5.5mm").getKind());
        assertEquals(MaterialKind.SHEET_GOOD,
                MaterialCatalog.instance().get("mdf-18mm").getKind());
    }

    @Test
    void solidLumberIsClassifiedCorrectly() {
        assertEquals(MaterialKind.SOLID_LUMBER,
                MaterialCatalog.instance().get("oak-3/4").getKind());
        assertEquals(MaterialKind.SOLID_LUMBER,
                MaterialCatalog.instance().get("pine-3/4").getKind());
        // Metal bar is sold by length, not by sheet — fits SOLID_LUMBER handling.
        assertEquals(MaterialKind.SOLID_LUMBER,
                MaterialCatalog.instance().get("aluminum-1/8").getKind());
    }

    @Test
    void slabsAreClassifiedCorrectly() {
        Material granite = MaterialCatalog.instance().get("granite-3cm");
        assertNotNull(granite, "granite-3cm should be in the catalog");
        assertEquals(MaterialKind.SLAB, granite.getKind());
        assertEquals(MaterialType.STONE, granite.getType());
        // Slabs don't have bundled sheet dimensions — they're project-specific.
        assertNull(granite.getSheetWidthMm(),
                "slab materials should not have sheet dimensions");
    }
}
