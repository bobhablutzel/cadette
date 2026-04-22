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
 * Verifies that a template body can instantiate another template (sub-template
 * composition). This is the substrate for building "families" — e.g. a
 * craftsman/base_cabinet composing a craftsman/face_frame and craftsman/door.
 *
 * Template references are resolved at runtime (during instantiation), not at
 * define time — so a missing sub-template is only detected when someone calls
 * `create outer`. A post-load validation pass is a reasonable backlog item.
 */
class RecursiveTemplateTest extends HeadlessTestBase {

    @BeforeEach
    void clean() {
        resetScene();
    }

    @AfterEach
    void unregisterTestTemplates() {
        TemplateRegistry.instance().unregister("rt_nested/inner");
        TemplateRegistry.instance().unregister("rt_nested/outer");
        TemplateRegistry.instance().unregister("rt_nested/missing_dep");
    }

    @Test
    void outerTemplateInstantiatesInnerTemplate() {
        // Inner: a one-part "panel" template.
        exec("define rt_nested/inner params width, height");
        exec("create part \"panel\" size $width, $height at 0, 0, 0");
        exec("end define");

        // Outer: creates its own "frame" part, then instantiates the inner template.
        exec("define rt_nested/outer params width, height");
        exec("create part \"frame\" size $width, $height at 0, 0, 0");
        exec("create rt_nested/inner door1 width $width height $height");
        exec("end define");

        String result = exec("create rt_nested/outer OC width 500 height 600");
        System.out.println("recursive result: " + result);

        // Outer's direct part, with the outer prefix.
        assertNotNull(sceneManager.getObjectRecord("OC/frame"),
                "outer template's own part should exist");

        // Inner template's part, prefixed through both levels (outer/inner/part).
        assertNotNull(sceneManager.getObjectRecord("OC/door1/panel"),
                "nested part should use the chained prefix 'OC/door1/panel'");

        // The inner instantiation should have registered a nested assembly too.
        assertNotNull(sceneManager.getAssembly("OC/door1"),
                "nested assembly should be registered under the chained name");
    }

    @Test
    void runtimeErrorWhenSubTemplateMissing() {
        // Define outer referencing a template that does not exist.
        exec("define rt_nested/missing_dep params width");
        exec("create part \"stub\" size $width, 1 at 0, 0, 0");
        exec("create rt_nested/does_not_exist sub1 width $width");
        exec("end define");

        // Defining should succeed — the reference is not checked until instantiation.
        assertNotNull(TemplateRegistry.instance().get("rt_nested/missing_dep"),
                "template with an unresolved sub-reference should still register");

        // Instantiating surfaces the missing dep as part of the body output.
        String result = exec("create rt_nested/missing_dep M width 50");
        assertTrue(result.contains("not found"),
                "instantiation should surface the missing sub-template: " + result);
    }
}
