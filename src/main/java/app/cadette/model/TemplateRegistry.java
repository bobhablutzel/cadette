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

package app.cadette.model;

import java.util.*;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Registry of template definitions. Pre-loaded with common woodworking templates.
 */
public class TemplateRegistry {

    /** Template names: Java-identifier-like (start with a letter, then letters/digits/underscores). */
    private static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");

    private static final TemplateRegistry INSTANCE = new TemplateRegistry();

    private final Map<String, Template> templates = new LinkedHashMap<>();

    private TemplateRegistry() {
        loadBuiltins();
    }

    public static TemplateRegistry instance() {
        return INSTANCE;
    }

    public Template get(String name) {
        return templates.get(name.toLowerCase());
    }

    public Collection<Template> getAll() {
        return Collections.unmodifiableCollection(templates.values());
    }

    public void register(Template template) {
        String name = template.getName();
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid template name '" + name + "': must start with a letter and "
                    + "contain only letters, digits, and underscores.");
        }
        templates.put(name.toLowerCase(), template);
    }

    private void loadBuiltins() {
        // Notes on orientation:
        //   Parts are created as (X=cutWidth, Y=cutHeight, Z=thickness).
        //   - Side panels: rotate 0,90,0 to face inward (extends into -Z)
        //   - Horizontal panels (bottom/top): rotate -90,0,0 to lay flat (extends into -Z)
        //   - Back panels: not rotated, sits flat against the back
        //   $thickness = default material thickness (implicit)
        //   $back_thickness = hardboard thickness (implicit)
        //   Dado depth defaults to half the receiving material when not specified.

        Map<String, String> whdAliases = Map.of("w", "width", "h", "height", "d", "depth");

        // -- Base Cabinet --
        register(new Template("base_cabinet",
                List.of("width", "height", "depth"), whdAliases,
                List.of(
                    "# Sides",
                    "create part \"left-side\" size $depth, $height at 0, 0, 0 grain vertical",
                    "rotate \"left-side\" 0, 90, 0",
                    "create part \"right-side\" size $depth, $height at $width - $thickness, 0, 0 grain vertical",
                    "rotate \"right-side\" 0, 90, 0",
                    "# Bottom",
                    "create part \"bottom\" size $width - 2 * $thickness, $depth at $thickness, 0, 0",
                    "rotate \"bottom\" -90, 0, 0",
                    "# Top stretcher (capped at 100mm depth)",
                    "create part \"top-stretcher\" size $width - 2 * $thickness, min($depth, 100 * $mm) at $thickness, $height - $thickness, 0",
                    "rotate \"top-stretcher\" -90, 0, 0",
                    "# Back panel",
                    "create part \"back\" material \"hardboard-5.5mm\" size $width, $height at 0, 0, -$depth",
                    "# Joinery",
                    "join \"left-side\" to \"bottom\" with dado",
                    "join \"right-side\" to \"bottom\" with dado",
                    "join \"left-side\" to \"top-stretcher\" with pocket screws 3",
                    "join \"right-side\" to \"top-stretcher\" with pocket screws 3",
                    "join \"left-side\" to \"back\" with rabbet",
                    "join \"right-side\" to \"back\" with rabbet"
                ), true));

        // -- Wall Cabinet --
        register(new Template("wall_cabinet",
                List.of("width", "height", "depth"), whdAliases,
                List.of(
                    "create part \"left-side\" size $depth, $height at 0, 0, 0 grain vertical",
                    "rotate \"left-side\" 0, 90, 0",
                    "create part \"right-side\" size $depth, $height at $width - $thickness, 0, 0 grain vertical",
                    "rotate \"right-side\" 0, 90, 0",
                    "create part \"top\" size $width - 2 * $thickness, $depth at $thickness, $height - $thickness, 0",
                    "rotate \"top\" -90, 0, 0",
                    "create part \"bottom\" size $width - 2 * $thickness, $depth at $thickness, 0, 0",
                    "rotate \"bottom\" -90, 0, 0",
                    "create part \"back\" material \"hardboard-5.5mm\" size $width, $height at 0, 0, -$depth",
                    "# Joinery",
                    "join \"left-side\" to \"top\" with dado",
                    "join \"right-side\" to \"top\" with dado",
                    "join \"left-side\" to \"bottom\" with dado",
                    "join \"right-side\" to \"bottom\" with dado",
                    "join \"left-side\" to \"back\" with rabbet",
                    "join \"right-side\" to \"back\" with rabbet"
                ), true));

        // -- Drawer Box --
        register(new Template("drawer_box",
                List.of("width", "height", "depth"), whdAliases,
                List.of(
                    "create part \"left-side\" size $depth, $height at 0, 0, 0 grain vertical",
                    "rotate \"left-side\" 0, 90, 0",
                    "create part \"right-side\" size $depth, $height at $width - $thickness, 0, 0 grain vertical",
                    "rotate \"right-side\" 0, 90, 0",
                    "create part \"front\" size $width - 2 * $thickness, $height at $thickness, 0, -$thickness grain vertical",
                    "create part \"back\" size $width - 2 * $thickness, $height at $thickness, 0, -$depth grain vertical",
                    "create part \"bottom\" material \"plywood-1/4\" size $width, $depth at 0, 0, 0",
                    "rotate \"bottom\" -90, 0, 0",
                    "# Joinery",
                    "join \"left-side\" to \"front\" with dado",
                    "join \"right-side\" to \"front\" with dado",
                    "join \"left-side\" to \"back\" with dado",
                    "join \"right-side\" to \"back\" with dado",
                    "join \"left-side\" to \"bottom\" with rabbet",
                    "join \"right-side\" to \"bottom\" with rabbet"
                ), true));

        // -- Shelf Unit --
        register(new Template("shelf_unit",
                List.of("width", "height", "depth", "shelves"),
                Map.of("w", "width", "h", "height", "d", "depth", "s", "shelves"),
                List.of(
                    "create part \"left-side\" size $depth, $height at 0, 0, 0 grain vertical",
                    "rotate \"left-side\" 0, 90, 0",
                    "create part \"right-side\" size $depth, $height at $width - $thickness, 0, 0 grain vertical",
                    "rotate \"right-side\" 0, 90, 0",
                    "create part \"top\" size $width - 2 * $thickness, $depth at $thickness, $height - $thickness, 0",
                    "rotate \"top\" -90, 0, 0",
                    "create part \"bottom\" size $width - 2 * $thickness, $depth at $thickness, 0, 0",
                    "rotate \"bottom\" -90, 0, 0",
                    "create part \"back\" material \"hardboard-5.5mm\" size $width, $height at 0, 0, -$depth",
                    "# Joinery",
                    "join \"left-side\" to \"top\" with dado",
                    "join \"right-side\" to \"top\" with dado",
                    "join \"left-side\" to \"bottom\" with dado",
                    "join \"right-side\" to \"bottom\" with dado",
                    "join \"left-side\" to \"back\" with rabbet",
                    "join \"right-side\" to \"back\" with rabbet"
                ), true));

        // -- Crosscut Sled --
        register(new Template("crosscut_sled",
                List.of("width", "length", "fence_height"),
                Map.of("w", "width", "l", "length", "fh", "fence_height"),
                List.of(
                    "create part \"base\" material \"mdf-3/4\" size $width, $length at 0, 0, 0",
                    "rotate \"base\" -90, 0, 0",
                    "create part \"front-fence\" size $width, $fence_height at 0, 0, 0",
                    "rotate \"front-fence\" 90, 0, 0",
                    "create part \"rear-fence\" size $width, $fence_height at 0, 0, -$length",
                    "rotate \"rear-fence\" 90, 0, 0",
                    "# Joinery",
                    "join \"base\" to \"front-fence\" with butt",
                    "join \"base\" to \"rear-fence\" with butt"
                ), true));
    }
}
