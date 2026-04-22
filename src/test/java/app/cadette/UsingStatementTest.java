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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the `using` statement: bare template names resolve via the
 * configured namespace list, and namespaces set inside a `run`-invoked
 * script do not leak back to the caller.
 */
class UsingStatementTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    @AfterEach
    void unregisterTestTemplates() {
        TemplateRegistry.instance().unregister("using_teamA/gadget99");
        TemplateRegistry.instance().unregister("using_teamB/gadget99");
        TemplateRegistry.instance().unregister("using_leak_a/relay77");
        TemplateRegistry.instance().unregister("using_leak_b/relay77");
        TemplateRegistry.instance().unregister("bare_override_widget");
        TemplateRegistry.instance().unregister("override_ns/bare_override_widget");
    }

    @Test
    void usingBeatsExactMatchForBareNames() {
        // Regression guard: the old resolver returned an exact bare match before
        // consulting `using`, which defeated the purpose of `using` when a local
        // bare-named template shadowed a namespaced one.
        exec("define bare_override_widget params width");
        exec("create part \"bare\" size $width, 1 at 0, 0, 0");
        exec("end define");

        exec("define override_ns/bare_override_widget params width");
        exec("create part \"namespaced\" size $width, 1 at 0, 0, 0");
        exec("end define");

        // Without `using`, exact bare match wins.
        exec("create bare_override_widget W1 width 1");
        assertNotNull(sceneManager.getObjectRecord("W1/bare"),
                "bare-named template should win exact-match when no using is set");

        // With `using`, the namespaced one must take priority over the bare match.
        exec("using override_ns");
        String result = exec("create bare_override_widget W2 width 1");
        assertTrue(result.contains("override_ns/bare_override_widget"),
                "create should report the resolved qualified name: " + result);
        assertNotNull(sceneManager.getObjectRecord("W2/namespaced"),
                "`using` must beat exact bare match — the whole point of `using`");
    }

    @Test
    void whichReportsResolvedNameAndSource() {
        String result = exec("which base_cabinet");
        assertTrue(result.contains("standard/cabinets/base_cabinet"),
                "which should report the fully-qualified name: " + result);
        assertTrue(result.contains("source:"), "which should include a source line: " + result);
        assertTrue(result.contains("base_cabinet.cds"),
                "source should mention the file: " + result);
    }

    @Test
    void whichRespectsUsingResolution() {
        exec("define override_ns/bare_override_widget params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        exec("using override_ns");
        String result = exec("which bare_override_widget");
        assertTrue(result.contains("override_ns/bare_override_widget"),
                "which should follow the `using` resolution path: " + result);
    }

    @Test
    void usingNoneClearsTheList() {
        // Seed two `using` namespaces.
        exec("define clr_ns_a/widget77 params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        exec("using clr_ns_a");
        // Confirm it took effect.
        String withUsing = exec("which widget77");
        assertTrue(withUsing.contains("clr_ns_a/widget77"),
                "using should resolve bare name via namespace: " + withUsing);

        // Clear.
        String clearResult = exec("using none");
        assertTrue(clearResult.toLowerCase().contains("cleared"),
                "using none should confirm it cleared: " + clearResult);

        // After clear, bare resolution should fall through to last-segment uniqueness
        // (clr_ns_a/widget77 is still the only template ending in widget77).
        String afterClear = exec("which widget77");
        assertTrue(afterClear.contains("clr_ns_a/widget77"),
                "uniqueness fallback should still find it: " + afterClear);

        // And most importantly — clearing removed the namespace from the using list,
        // so a newly-registered collision would no longer be disambiguated by it.
        exec("define clr_ns_b/widget77 params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        String ambig = exec("create widget77 X width 1");
        assertTrue(ambig.contains("ambiguous"),
                "after `using none`, new collisions should be ambiguous: " + ambig);

        TemplateRegistry.instance().unregister("clr_ns_a/widget77");
        TemplateRegistry.instance().unregister("clr_ns_b/widget77");
    }

    @Test
    void showTemplatesIncludesSourceColumn() {
        String result = exec("show templates");
        assertTrue(result.contains("source:"), "listing should include source metadata: " + result);
        assertTrue(result.contains("classpath:"),
                "bundled templates should be tagged as classpath: " + result);
    }

    @Test
    void usingPrefersNamespaceForAmbiguousBareName() {
        // Use a bare segment that doesn't collide with built-ins.
        exec("define using_teamA/gadget99 params width, height, depth");
        exec("create part \"p\" size $width, $height at 0, 0, 0");
        exec("end define");

        exec("define using_teamB/gadget99 params width, height, depth");
        exec("create part \"p\" size $width, $height at 0, 0, 0");
        exec("end define");

        // Without `using`, ambiguous.
        String ambiguous = exec("create gadget99 X width 10 height 10 depth 10");
        assertTrue(ambiguous.contains("ambiguous"), "expected ambiguity: " + ambiguous);

        // Pinning teamA via `using` disambiguates in favor of teamA.
        exec("using using_teamA");
        String ok = exec("create gadget99 Y width 10 height 10 depth 10");
        assertFalse(ok.contains("ambiguous"), "using should disambiguate: " + ok);
        assertNotNull(sceneManager.getObjectRecord("Y/p"));
    }

    @Test
    void usingInsideRunScriptDoesNotLeak() throws IOException {
        // Two namespaces both defining `relay77` (unique segment — no clash with built-ins).
        exec("define using_leak_a/relay77 params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        exec("define using_leak_b/relay77 params width");
        exec("create part \"p\" size $width, 1 at 0, 0, 0");
        exec("end define");

        // Script picks namespace a; after run, we should still see ambiguity on bare `relay77`.
        Path script = Files.createTempFile("cadette-using-leak-", ".cds");
        Files.write(script, List.of(
                "using using_leak_a",
                "create relay77 INSIDE width 1"
        ));
        script.toFile().deleteOnExit();

        String ran = exec("run " + script);
        assertFalse(ran.contains("ambiguous"), "script should succeed: " + ran);
        assertNotNull(sceneManager.getObjectRecord("INSIDE/p"));

        // After the run, the using list should be restored — bare name is ambiguous again.
        String after = exec("create relay77 OUTSIDE width 1");
        assertTrue(after.contains("ambiguous"),
                "using inside the script should not leak to caller: " + after);
    }
}
