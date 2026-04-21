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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Script shebang identifier: `#! cadette` marks a .cds file as a CADette
 * script. The line is a comment to the lexer, so it's a no-op at execution
 * time; runScript uses it only as a file-identification hint.
 */
class ShebangTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    private Path write(String... lines) throws IOException {
        Path f = Files.createTempFile("cadette-shebang-", ".cds");
        Files.write(f, List.of(lines));
        f.toFile().deleteOnExit();
        return f;
    }

    @Test
    void cadetteShebangProducesNoWarning() throws IOException {
        Path script = write(
                "#! cadette",
                "create box shebang_ok size 1");
        String result = exec("run " + script);
        assertFalse(result.contains("Warning:"), "no warning expected: " + result);
        assertNotNull(sceneManager.getObjectRecord("shebang_ok"));
    }

    @Test
    void missingShebangIsAllowedSilently() throws IOException {
        Path script = write("create box no_shebang size 1");
        String result = exec("run " + script);
        assertFalse(result.contains("Warning:"), "no warning expected: " + result);
        assertNotNull(sceneManager.getObjectRecord("no_shebang"));
    }

    @Test
    void foreignShebangWarnsButStillRuns() throws IOException {
        Path script = write(
                "#!/bin/bash",
                "create box foreign size 1");
        String result = exec("run " + script);
        assertTrue(result.contains("Warning:"), "should warn about foreign shebang: " + result);
        assertTrue(result.contains("cadette"), "warning should reference cadette: " + result);
        assertNotNull(sceneManager.getObjectRecord("foreign"),
                "script should still run despite foreign shebang");
    }

    @Test
    void shebangWithPathAndCadetteWordIsAccepted() throws IOException {
        // A richer shebang variant that embeds "cadette" as part of a path
        Path script = write(
                "#!/usr/local/bin/cadette",
                "create box rich_shebang size 1");
        String result = exec("run " + script);
        assertFalse(result.contains("Warning:"), "no warning expected: " + result);
        assertNotNull(sceneManager.getObjectRecord("rich_shebang"));
    }
}
