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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the command-string builders in MoveDialog and RotateDialog.
 * The Swing dialogs themselves aren't tested (they need a real display).
 */
class MoveRotateDialogTest {

    @Test
    void moveAbsoluteEmitsExpectedCommand() {
        assertEquals("move \"K\" to 100, 200, 0",
                MoveDialog.buildAbsoluteCommand("K", 100, 200, 0));
    }

    @Test
    void moveAbsoluteFormatsFractionalValues() {
        assertEquals("move \"K\" to 1.5, 2.25, 3",
                MoveDialog.buildAbsoluteCommand("K", 1.5f, 2.25f, 3));
    }

    @Test
    void moveRelativeWithoutGap() {
        assertEquals("move \"b\" to right of \"a\"",
                MoveDialog.buildRelativeCommand("b", "right", "a", null));
    }

    @Test
    void moveRelativeWithGap() {
        assertEquals("move \"b\" to above of \"a\" gap 25",
                MoveDialog.buildRelativeCommand("b", "above", "a", 25f));
    }

    @Test
    void moveRelativeWithFractionalGap() {
        assertEquals("move \"b\" to left of \"a\" gap 12.5",
                MoveDialog.buildRelativeCommand("b", "left", "a", 12.5f));
    }

    @Test
    void rotateEmitsExpectedCommand() {
        assertEquals("rotate \"foo\" 0, 90, 0",
                RotateDialog.buildCommand("foo", 0, 90, 0));
    }

    @Test
    void rotateWithNegativeValues() {
        assertEquals("rotate \"foo\" -45, 90, -22.5",
                RotateDialog.buildCommand("foo", -45, 90, -22.5f));
    }

    /**
     * Roundtrip: the generated commands must be valid grammar that the executor
     * can parse and run. Uses a headless executor so we exercise the real parser.
     */
    @org.junit.jupiter.api.Nested
    class Roundtrip extends HeadlessTestBase {
        @org.junit.jupiter.api.BeforeEach
        void setup() { resetScene(); }

        @Test
        void absoluteMoveCommandParses() {
            exec("create box foo size 1");
            String cmd = MoveDialog.buildAbsoluteCommand("foo", 100, 200, 0);
            String result = exec(cmd);
            assertTrue(result.contains("Moved"), "absolute move should succeed: " + result);
        }

        @Test
        void relativeMoveCommandParses() {
            exec("create box a size 1 at 0,0,0");
            exec("create box b size 1 at 0,0,0");
            String cmd = MoveDialog.buildRelativeCommand("b", "right", "a", null);
            String result = exec(cmd);
            assertTrue(result.contains("Moved"), "relative move should succeed: " + result);
        }

        @Test
        void relativeMoveWithGapParses() {
            exec("create box a size 1 at 0,0,0");
            exec("create box b size 1 at 0,0,0");
            String cmd = MoveDialog.buildRelativeCommand("b", "above", "a", 25f);
            String result = exec(cmd);
            assertTrue(result.contains("Moved"), "relative move with gap should succeed: " + result);
        }

        @Test
        void rotateCommandParses() {
            exec("create box foo size 1");
            String cmd = RotateDialog.buildCommand("foo", 0, 90, 0);
            String result = exec(cmd);
            assertTrue(result.contains("Rotated"), "rotate should succeed: " + result);
        }
    }
}
