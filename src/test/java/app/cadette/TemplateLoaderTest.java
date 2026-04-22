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

import app.cadette.model.Template;
import app.cadette.model.TemplateRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the filesystem template loader path — override warnings, name/path
 * mismatch detection, and resilience to one bad file among many.
 */
class TemplateLoaderTest extends HeadlessTestBase {

    @AfterEach
    void restoreStandardTemplates() {
        // Tests here override / inject templates by path. Reload from the
        // classpath so the shared TemplateRegistry singleton is back to its
        // pristine state before any downstream test runs. (Bundled only —
        // the developer's ~/.cadette/templates/ must not bleed into results.)
        executor.loadBundledTemplates();
    }

    private static Path writeTree(String... pathContentPairs) throws IOException {
        assertEquals(0, pathContentPairs.length % 2, "must pass path/content pairs");
        Path root = Files.createTempDirectory("cadette-templates-");
        root.toFile().deleteOnExit();
        for (int i = 0; i < pathContentPairs.length; i += 2) {
            Path f = root.resolve(pathContentPairs[i]);
            Files.createDirectories(f.getParent());
            Files.writeString(f, pathContentPairs[i + 1]);
            f.toFile().deleteOnExit();
        }
        return root;
    }

    private static String captureStderrWhile(Runnable body) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));
        try {
            body.run();
        } finally {
            System.setErr(originalErr);
        }
        return captured.toString();
    }

    @Test
    void filesystemTemplateLoadsAndRegistersUnderPathName() throws IOException {
        Path root = writeTree(
                "myco/widget.cds",
                """
                #! cadette
                define myco/widget params width
                  create part "w" size $width, 1 at 0, 0, 0
                end define
                """);

        executor.loadTemplatesFromDirectory(root);

        Template t = TemplateRegistry.instance().get("myco/widget");
        assertNotNull(t, "filesystem template should be loaded");
        assertEquals(1, t.getParamNames().size());
    }

    @Test
    void filesystemOverridesClasspathWithWarning() throws IOException {
        // base_cabinet is shipped in the classpath. Override it from disk.
        Path root = writeTree(
                "standard/cabinets/base_cabinet.cds",
                """
                #! cadette
                define standard/cabinets/base_cabinet params width, height, depth
                  create part "override-marker" size $width, 1 at 0, 0, 0
                end define
                """);

        String stderr = captureStderrWhile(() -> executor.loadTemplatesFromDirectory(root));
        assertTrue(stderr.contains("overrides classpath"),
                "expected override warning on stderr, got: " + stderr);

        Template t = TemplateRegistry.instance().get("standard/cabinets/base_cabinet");
        assertNotNull(t);
        assertTrue(t.getBodyLines().stream().anyMatch(l -> l.contains("override-marker")),
                "override body should win");
    }

    @Test
    void fileNameMismatchRejectsMisnamedTemplate() throws IOException {
        Path root = writeTree(
                "ns/expectedname.cds",
                """
                #! cadette
                define ns/wrongname params width
                  create part "p" size $width, 1 at 0, 0, 0
                end define
                """);

        String stderr = captureStderrWhile(() -> executor.loadTemplatesFromDirectory(root));
        assertTrue(stderr.contains("ns/expectedname") || stderr.contains("expectedname"),
                "expected mismatch warning mentioning the file, got: " + stderr);
        assertNull(TemplateRegistry.instance().get("ns/expectedname"),
                "the expected path name should not be registered");
        assertNull(TemplateRegistry.instance().get("ns/wrongname"),
                "the misnamed template must be rolled back — it would silently "
                        + "shadow other lookups of 'ns/wrongname'");
    }

    @Test
    void extraTemplatesInOneFileAreRejectedWithWarning() throws IOException {
        Path root = writeTree(
                "lib/main.cds",
                """
                #! cadette
                define lib/main params width
                  create part "p" size $width, 1 at 0, 0, 0
                end define
                define lib/bonus params width
                  create part "p" size $width, 1 at 0, 0, 0
                end define
                """);

        String stderr = captureStderrWhile(() -> executor.loadTemplatesFromDirectory(root));
        assertNotNull(TemplateRegistry.instance().get("lib/main"),
                "the template whose name matches the file should be kept");
        assertNull(TemplateRegistry.instance().get("lib/bonus"),
                "extra templates in the same file must be rejected — one file, one template");
        assertTrue(stderr.contains("lib/bonus") || stderr.contains("One template per file"),
                "expected a warning about the extra, got: " + stderr);
    }

    @Test
    void oneBadFileDoesNotBlockOthers() throws IOException {
        Path root = writeTree(
                "batch/good1.cds",
                """
                #! cadette
                define batch/good1 params width
                  create part "p" size $width, 1 at 0, 0, 0
                end define
                """,

                "batch/broken.cds",
                """
                #! cadette
                define batch/broken params width
                  this is not a valid command at all $$$
                end define
                """,

                "batch/good2.cds",
                """
                #! cadette
                define batch/good2 params width
                  create part "p" size $width, 1 at 0, 0, 0
                end define
                """);

        captureStderrWhile(() -> executor.loadTemplatesFromDirectory(root));

        assertNotNull(TemplateRegistry.instance().get("batch/good1"),
                "file before bad one should still load");
        assertNotNull(TemplateRegistry.instance().get("batch/good2"),
                "file after bad one should still load");
    }
}
