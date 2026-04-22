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
import java.util.stream.Collectors;

/**
 * Groups parts by material and runs the guillotine packer on each group.
 * Non-sheet goods (hardwood, metal) are excluded from layout.
 */
public class SheetLayoutGenerator {

    /**
     * Generate sheet layouts for all parts, grouped by material.
     *
     * @param parts  all parts in the scene
     * @param kerfMm saw blade kerf width in mm
     * @return list of sheet layouts across all materials
     */
    public static List<SheetLayout> generateLayouts(Map<String, Part> parts, float kerfMm) {
        // Group parts by material name
        Map<String, List<Part>> byMaterial = parts.values().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMaterial().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<SheetLayout> allLayouts = new ArrayList<>();

        for (var entry : byMaterial.entrySet()) {
            List<Part> materialParts = entry.getValue();
            Material mat = materialParts.get(0).getMaterial();

            // Only sheet goods go through the guillotine packer. Solid lumber
            // is packed elsewhere (or not at all yet); slab and hardware are
            // counted per-piece in the BOM.
            if (mat.getKind() != MaterialKind.SHEET_GOOD) {
                continue;
            }

            // Convert to packing parts
            List<GuillotinePacker.PackingPart> packingParts = materialParts.stream()
                    .map(p -> new GuillotinePacker.PackingPart(
                            p.getName(),
                            p.getCutWidthMm(),
                            p.getCutHeightMm(),
                            p.getGrainRequirement()))
                    .toList();

            allLayouts.addAll(GuillotinePacker.pack(mat, packingParts, kerfMm));
        }

        return allLayouts;
    }

    public static List<SheetLayout> generateLayouts(Map<String, Part> parts) {
        return generateLayouts(parts, GuillotinePacker.DEFAULT_KERF_MM);
    }
}
