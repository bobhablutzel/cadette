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

import app.cadette.model.TemplateRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises slash-qualified template references in `create`, `define`, and
 * `show template`, plus bare-name uniqueness resolution.
 */
class QualifiedTemplateTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    @AfterEach
    void unregisterTestTemplates() {
        // Names used by tests below — kept out of the standard registry so they
        // don't taint downstream tests (TemplateRegistry is a process-wide singleton).
        TemplateRegistry.instance().unregister("qt_ambig_teamA/widget42");
        TemplateRegistry.instance().unregister("qt_ambig_teamB/widget42");
        TemplateRegistry.instance().unregister("team/widget");
    }

    @Test
    void createWithFullyQualifiedName() {
        String result = exec("create standard/cabinets/base_cabinet K1 width 500 height 600 depth 400");
        assertTrue(result.contains("standard/cabinets/base_cabinet"),
                "should confirm creation under qualified name: " + result);
        assertNotNull(sceneManager.getObjectRecord("K1/left-side"));
    }

    @Test
    void bareNameResolvesByUniqueLastSegment() {
        // base_cabinet is unique under standard/cabinets — bare name should still work.
        String result = exec("create base_cabinet K2 width 500 height 600 depth 400");
        assertTrue(result.toLowerCase().contains("base_cabinet"),
                "bare name should resolve: " + result);
        assertNotNull(sceneManager.getObjectRecord("K2/left-side"));
    }

    @Test
    void ambiguousBareNameReportsCandidates() {
        // Use a bare segment that isn't already in the registry — we want to
        // isolate the ambiguity to these two templates without affecting
        // built-ins (or other tests that use `base_cabinet`).
        exec("define qt_ambig_teamA/widget42 params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        exec("define qt_ambig_teamB/widget42 params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        String result = exec("create widget42 C width 1");
        assertTrue(result.contains("ambiguous"), "should flag ambiguity: " + result);
        assertTrue(result.contains("qt_ambig_teamA/widget42"),
                "should list first candidate: " + result);
        assertTrue(result.contains("qt_ambig_teamB/widget42"),
                "should list second candidate: " + result);
    }

    @Test
    void showTemplateAcceptsQualifiedName() {
        String result = exec("show template standard/cabinets/base_cabinet");
        assertFalse(result.contains("not found"), "qualified show should find it: " + result);
        assertTrue(result.contains("base_cabinet"), "should render the template: " + result);
    }

    @Test
    void unknownQualifiedNameDoesNotFallBackToBareLookup() {
        // Even though standard/cabinets/base_cabinet exists, the explicit wrong
        // namespace should error — qualified lookups are exact-match only.
        String result = exec("create nosuch/base_cabinet K width 500 height 600 depth 400");
        assertTrue(result.contains("not found"), "qualified miss should not bare-fallback: " + result);
    }

    @Test
    void definingQualifiedTemplateRegistersUnderFullName() {
        exec("define team/widget params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        assertNotNull(TemplateRegistry.instance().get("team/widget"));
        assertNull(TemplateRegistry.instance().get("widget"),
                "segment alone shouldn't be a registry key");
    }
}
