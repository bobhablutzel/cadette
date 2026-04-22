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
 * Source: https://github.com/bobhablutzel/cadette"
 */

package app.cadette;

import app.cadette.model.JointType;
import app.cadette.model.MaterialType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers material-type compatibility checks now attached to JointType,
 * both at the unit level and through the `join` command path.
 */
class JointCompatibilityTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    // ---- Unit-level: JointType.supports(MaterialType) ----

    @Test
    void pocketScrewDoesNotSupportHardboardOrStone() {
        assertFalse(JointType.POCKET_SCREW.supports(MaterialType.HARDBOARD),
                "hardboard is too soft for pocket screws");
        assertFalse(JointType.POCKET_SCREW.supports(MaterialType.STONE),
                "stone can't be pocket-screwed");
        assertFalse(JointType.POCKET_SCREW.supports(MaterialType.METAL),
                "aluminum bar can't be pocket-screwed");
    }

    @Test
    void pocketScrewSupportsPlywoodAndLumber() {
        assertTrue(JointType.POCKET_SCREW.supports(MaterialType.PLYWOOD));
        assertTrue(JointType.POCKET_SCREW.supports(MaterialType.HARDWOOD));
        assertTrue(JointType.POCKET_SCREW.supports(MaterialType.SOFTWOOD));
        assertTrue(JointType.POCKET_SCREW.supports(MaterialType.MDF));
    }

    @Test
    void dadoExcludesHardboardMetalAndStone() {
        assertFalse(JointType.DADO.supports(MaterialType.HARDBOARD),
                "hardboard is too thin for a dado");
        assertFalse(JointType.DADO.supports(MaterialType.METAL));
        assertFalse(JointType.DADO.supports(MaterialType.STONE));
    }

    @Test
    void rabbetAllowsHardboardButNotStoneOrMetal() {
        // Rabbets are shallow edge cuts — hardboard can take a gentle rabbet.
        assertTrue(JointType.RABBET.supports(MaterialType.HARDBOARD));
        assertFalse(JointType.RABBET.supports(MaterialType.METAL));
        assertFalse(JointType.RABBET.supports(MaterialType.STONE));
    }

    @Test
    void buttIsCompatibleWithEverything() {
        for (MaterialType t : MaterialType.values()) {
            assertTrue(JointType.BUTT.supports(t),
                    "butt joints should work in every MaterialType including " + t);
        }
    }

    // ---- Integration: the `join` command rejects incompatible pairs ----

    @Test
    void joinRejectsPocketScrewIntoHardboard() {
        exec("create part \"receiver\" material \"hardboard-5.5mm\" size 200, 300 at 0, 0, 0");
        exec("create part \"inserted\" material \"plywood-18mm\" size 200, 300 at 0, 0, 0");
        String result = exec("join \"receiver\" to \"inserted\" with pocket_screw");

        assertTrue(result.toLowerCase().contains("cannot"),
                "should reject pocket-screw-into-hardboard: " + result);
        assertTrue(result.contains("hardboard"),
                "error should name the offending material: " + result);
    }

    @Test
    void joinRejectsDadoIntoStone() {
        exec("create part \"counter\" material \"granite-3cm\" size 600, 2400 at 0, 0, 0");
        exec("create part \"side\" material \"plywood-18mm\" size 600, 900 at 0, 0, 0");
        String result = exec("join \"counter\" to \"side\" with dado");

        assertTrue(result.toLowerCase().contains("cannot"),
                "should reject dado into granite: " + result);
    }

    @Test
    void joinAcceptsCompatiblePair() {
        exec("create part \"a\" material \"plywood-18mm\" size 200, 300 at 0, 0, 0");
        exec("create part \"b\" material \"plywood-18mm\" size 200, 300 at 0, 0, 0");
        String result = exec("join \"a\" to \"b\" with dado depth 9");

        assertFalse(result.toLowerCase().contains("cannot"),
                "compatible pair should join without error: " + result);
    }

    @Test
    void joinRejectsEvenWhenOneSideIsIncompatible() {
        // Only ONE side being incompatible should still fail — the joint needs
        // both pieces to be workable. Here inserted is plywood (fine), but
        // receiving is hardboard which pocket-screw can't bite.
        exec("create part \"hb\" material \"hardboard-5.5mm\" size 200, 300 at 0, 0, 0");
        exec("create part \"pw\" material \"plywood-18mm\" size 200, 300 at 0, 0, 0");
        String bad = exec("join \"hb\" to \"pw\" with pocket_screw");
        assertTrue(bad.toLowerCase().contains("cannot"),
                "receiving-side hardboard should fail: " + bad);

        // And the other direction — inserted hardboard — also fails.
        // (Under the symmetric check; this is the less-obvious direction we
        // intentionally catch because pocket screws also need the inserted
        // part to be reasonable stock.)
        String bad2 = exec("join \"pw\" to \"hb\" with pocket_screw");
        assertTrue(bad2.toLowerCase().contains("cannot"),
                "inserted-side hardboard should also fail: " + bad2);
    }
}
