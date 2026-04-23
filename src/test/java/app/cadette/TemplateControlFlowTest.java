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
 * Exercises the new if/for block constructs in template bodies. These are
 * first-class grammar productions — the template body is parsed once at
 * define time and walked per instantiation, with loop variables scoped via
 * the executor's scope stack.
 */
class TemplateControlFlowTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    // ---- `if` ----

    @Test
    void ifBlockIncludesPartWhenConditionTrue() {
        exec("define maybe_box params toe_kick");
        exec("create part \"side\" size 100, 200 at 0, 0, 0");
        exec("if $toe_kick then");
        exec("create part \"toe\" size 100, 50 at 0, 0, 0");
        exec("end if");
        exec("end define");

        exec("create maybe_box B toe_kick 1");
        assertNotNull(sceneManager.getPart("B/side"));
        assertNotNull(sceneManager.getPart("B/toe"),
                "toe part should be created when toe_kick is truthy");
    }

    @Test
    void ifBlockSkipsPartWhenConditionFalse() {
        exec("define maybe_box2 params toe_kick");
        exec("create part \"side\" size 100, 200 at 0, 0, 0");
        exec("if $toe_kick then");
        exec("create part \"toe\" size 100, 50 at 0, 0, 0");
        exec("end if");
        exec("end define");

        exec("create maybe_box2 B toe_kick 0");
        assertNotNull(sceneManager.getPart("B/side"));
        assertNull(sceneManager.getPart("B/toe"),
                "toe part should NOT be created when toe_kick is 0");
    }

    @Test
    void ifElseTakesElseBranchWhenFalse() {
        exec("define either_box params pick_a");
        exec("if $pick_a then");
        exec("create part \"a\" size 100, 100 at 0, 0, 0");
        exec("else");
        exec("create part \"b\" size 200, 200 at 0, 0, 0");
        exec("end if");
        exec("end define");

        exec("create either_box E pick_a 0");
        assertNull(sceneManager.getPart("E/a"));
        assertNotNull(sceneManager.getPart("E/b"));
    }

    @Test
    void ifConditionCanUseComparisonOperators() {
        exec("define cmp params w");
        exec("if $w > 100 then");
        exec("create part \"wide\" size $w, 50 at 0, 0, 0");
        exec("end if");
        exec("end define");

        exec("create cmp W w 50");
        assertNull(sceneManager.getPart("W/wide"),
                "wide part should only appear when w > 100");

        exec("delete W");
        exec("create cmp W w 500");
        assertNotNull(sceneManager.getPart("W/wide"));
    }

    // ---- `for` ----

    @Test
    void forLoopCreatesMultipleParts() {
        exec("define shelves params count");
        exec("for $i = 1 to $count");
        exec("create part \"shelf_$i\" size 500, 20 at 0, 0, 0");
        exec("end for");
        exec("end define");

        exec("create shelves S count 3");
        assertNotNull(sceneManager.getPart("S/shelf_1"));
        assertNotNull(sceneManager.getPart("S/shelf_2"));
        assertNotNull(sceneManager.getPart("S/shelf_3"));
        assertNull(sceneManager.getPart("S/shelf_4"),
                "loop runs 1..count inclusive — no shelf_4 with count=3");
    }

    @Test
    void forLoopVarUsableInExpressions() {
        // Each shelf offset by 200 on Y-axis. `$i` resolves inside `size` and `at`.
        exec("define stack params count");
        exec("for $i = 1 to $count");
        exec("create part \"tier_$i\" size 400, 20 at 0, $i * 100, 0");
        exec("end for");
        exec("end define");

        exec("create stack S count 2");
        // We don't assert exact positions (placement normalization shifts everything),
        // but both parts must exist — proves the loop ran and $i-dependent expressions
        // parsed and evaluated.
        assertNotNull(sceneManager.getPart("S/tier_1"));
        assertNotNull(sceneManager.getPart("S/tier_2"));
    }

    @Test
    void forLoopWithZeroIterationsCreatesNothing() {
        exec("define empty_when_zero params n");
        exec("for $i = 1 to $n");
        exec("create part \"thing_$i\" size 100, 100 at 0, 0, 0");
        exec("end for");
        exec("end define");

        exec("create empty_when_zero E n 0");
        assertNull(sceneManager.getPart("E/thing_1"),
                "n=0 means the loop body should run zero times");
    }

    @Test
    void forLoopFromGreaterThanToIsZeroIterations() {
        // No implicit reverse — `for i = 5 to 3` just runs zero times.
        exec("define reverse_test params from, to");
        exec("for $i = $from to $to");
        exec("create part \"x_$i\" size 100, 100 at 0, 0, 0");
        exec("end for");
        exec("end define");

        exec("create reverse_test R from 5 to 3");
        assertNull(sceneManager.getPart("R/x_3"));
        assertNull(sceneManager.getPart("R/x_4"));
        assertNull(sceneManager.getPart("R/x_5"));
    }

    @Test
    void nestedIfInsideFor() {
        // For loop body contains an if block. Values pass the filter only
        // above the threshold — proves the if evaluates per iteration with
        // the current loop-var binding.
        exec("define threshold_filter params n, threshold");
        exec("for $i = 1 to $n");
        exec("if $i >= $threshold then");
        exec("create part \"pass_$i\" size 100, 100 at 0, 0, 0");
        exec("end if");
        exec("end for");
        exec("end define");

        exec("create threshold_filter T n 4 threshold 3");
        assertNull(sceneManager.getPart("T/pass_1"));
        assertNull(sceneManager.getPart("T/pass_2"));
        assertNotNull(sceneManager.getPart("T/pass_3"));
        assertNotNull(sceneManager.getPart("T/pass_4"));
    }

    @Test
    void nestedForLoopsStackIndependentScopes() {
        // Both $i and $j in scope inside the inner body.
        exec("define grid params rows, cols");
        exec("for $i = 1 to $rows");
        exec("for $j = 1 to $cols");
        exec("create part \"cell_${i}_${j}\" size 50, 50 at 0, 0, 0");
        exec("end for");
        exec("end for");
        exec("end define");

        exec("create grid G rows 2 cols 2");
        assertNotNull(sceneManager.getPart("G/cell_1_1"));
        assertNotNull(sceneManager.getPart("G/cell_1_2"));
        assertNotNull(sceneManager.getPart("G/cell_2_1"));
        assertNotNull(sceneManager.getPart("G/cell_2_2"));
    }
}
