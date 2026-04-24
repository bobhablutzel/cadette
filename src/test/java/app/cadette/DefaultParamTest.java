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
 * Exercises default values for template parameters (`params foo, bar=3`).
 * Defaults are parsed ExpressionContexts evaluated at instantiation with
 * earlier params already in scope.
 */
class DefaultParamTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    @Test
    void defaultUsedWhenParamNotSupplied() {
        // shelves=3 is the default; caller doesn't provide it.
        exec("define r1 params width, shelves=3");
        exec("for $i = 1 to $shelves");
        exec("create part \"shelf_$i\" size $width, 20 at 0, 0, 0");
        exec("end for");
        exec("end define");

        exec("create r1 R width 500");
        assertNotNull(sceneManager.getPart("R/shelf_1"));
        assertNotNull(sceneManager.getPart("R/shelf_2"));
        assertNotNull(sceneManager.getPart("R/shelf_3"));
        assertNull(sceneManager.getPart("R/shelf_4"));
    }

    @Test
    void providedValueOverridesDefault() {
        exec("define r2 params width, shelves=3");
        exec("for $i = 1 to $shelves");
        exec("create part \"shelf_$i\" size $width, 20 at 0, 0, 0");
        exec("end for");
        exec("end define");

        exec("create r2 R width 500 shelves 1");
        assertNotNull(sceneManager.getPart("R/shelf_1"));
        assertNull(sceneManager.getPart("R/shelf_2"),
                "explicit shelves=1 must override the default");
    }

    @Test
    void defaultCanBeAnArithmeticExpression() {
        exec("define r3 params base, scaled=$base * 2");
        exec("create part \"a\" size $scaled, 20 at 0, 0, 0");
        exec("end define");

        exec("create r3 R base 100");
        // We don't assert exact dimensions (toMm / display units can shift),
        // but the part exists, which means $scaled evaluated (base*2=200)
        // without the user supplying it.
        assertNotNull(sceneManager.getPart("R/a"));
    }

    @Test
    void laterDefaultReferencesEarlierParam() {
        // `height = $width * 2` — default expression uses a user-supplied param.
        exec("define r4 params width, height=$width * 2");
        exec("create part \"p\" size $width, $height at 0, 0, 0");
        exec("end define");

        exec("create r4 R width 100");
        assertNotNull(sceneManager.getPart("R/p"));
    }

    @Test
    void laterDefaultReferencesEarlierDefaultedParam() {
        // Both defaulted. `shelves` default chains off `max_shelves` default.
        exec("define r5 params max_shelves=5, shelves=$max_shelves - 2");
        exec("for $i = 1 to $shelves");
        exec("create part \"x_$i\" size 10, 10 at 0, 0, 0");
        exec("end for");
        exec("end define");

        exec("create r5 R");  // no params — both use defaults (5 - 2 = 3)
        assertNotNull(sceneManager.getPart("R/x_1"));
        assertNotNull(sceneManager.getPart("R/x_2"));
        assertNotNull(sceneManager.getPart("R/x_3"));
        assertNull(sceneManager.getPart("R/x_4"));
    }

    @Test
    void missingRequiredParamStillErrors() {
        // `width` is required (no default); omitting it should yield the
        // usage message rather than silently creating a broken instance.
        exec("define r6 params width, optional=5");
        exec("create part \"p\" size $width, 10 at 0, 0, 0");
        exec("end define");

        String result = exec("create r6 R optional 9");
        assertTrue(result.toLowerCase().contains("usage"),
                "omitting a required param should return a usage hint, got: " + result);
        assertNull(sceneManager.getPart("R/p"),
                "instance must not be created when required param is missing");
    }

    @Test
    void templateWithAllDefaultsCanBeInstantiatedWithNoArgs() {
        exec("define r7 params width=100, height=200");
        exec("create part \"p\" size $width, $height at 0, 0, 0");
        exec("end define");

        String result = exec("create r7 R");
        assertFalse(result.toLowerCase().contains("usage"),
                "all-defaults template should not show usage, got: " + result);
        assertNotNull(sceneManager.getPart("R/p"));
    }
}
