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

import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates cut lists and BOMs from parts and joints.
 *
 * Effective dimensions account for joinery:
 * - A part inserted into a dado is wider by the dado depth on each side it's dadoed into
 *   (the dado allows it to sit deeper, so the cut size stays the same — but the
 *    effective overlap changes). Actually, for a standard dado: the cut dimension
 *    of the inserted part doesn't change, but the position shifts. The receiving
 *    part gets a groove cut into it (a machining operation).
 *
 * For the cut list, what matters is:
 * - The cut dimensions of each part (unchanged by joinery)
 * - The machining operations on each part (dados, rabbets to be cut)
 * - The fasteners needed (pocket screws, etc.)
 */
public class CutListGenerator {

    @Data
    public static class CutListEntry {
        private final String partName;
        private final Material material;
        private final float cutWidthMm;
        private final float cutHeightMm;
        private final float thicknessMm;
        private final GrainRequirement grainRequirement;
        private final List<String> operations;  // machining operations (e.g., "dado 9mm deep")
    }

    @Data
    public static class BomEntry {
        private final Material material;
        private final int partCount;
        private final float totalAreaMm2;  // total area of all parts in this material
        private final Integer sheetCount;  // null if not a sheet good
        private final Float offcutPercent;  // null if not a sheet good
    }

    @Data
    public static class FastenerEntry {
        private final String type;
        private final int count;
    }

    /**
     * Generate the cut list from all parts in the scene.
     * Groups by material, includes machining operations from joints.
     */
    public static List<CutListEntry> generateCutList(
            Map<String, Part> parts, JointRegistry joints) {

        List<CutListEntry> entries = new ArrayList<>();

        for (Part part : parts.values()) {
            // Collect machining operations for this part (as the receiving part)
            List<String> operations = new ArrayList<>();
            for (Joint j : joints.getJointsForPart(part.getName())) {
                if (j.getReceivingPartName().equals(part.getName())) {
                    switch (j.getType()) {
                        case DADO -> operations.add(String.format(
                                "dado %.1fmm deep for \"%s\"", j.getDepthMm(), j.getInsertedPartName()));
                        case RABBET -> operations.add(String.format(
                                "rabbet %.1fmm deep for \"%s\"", j.getDepthMm(), j.getInsertedPartName()));
                        case POCKET_SCREW -> {
                            if (j.getScrewCount() > 0) {
                                operations.add(String.format(
                                        "%d pocket screw hole(s) for \"%s\"", j.getScrewCount(), j.getInsertedPartName()));
                            }
                        }
                        default -> {} // butt — no operation
                    }
                }
            }

            entries.add(new CutListEntry(
                    part.getName(),
                    part.getMaterial(),
                    part.getCutWidthMm(),
                    part.getCutHeightMm(),
                    part.getThicknessMm(),
                    part.getGrainRequirement(),
                    operations));
        }

        // Sort: group by material name, then by part name
        entries.sort(Comparator
                .comparing((CutListEntry e) -> e.getMaterial().getName())
                .thenComparing(CutListEntry::getPartName));

        return entries;
    }

    /**
     * Generate the BOM using actual sheet layout results from the packer.
     */
    public static List<BomEntry> generateBom(Map<String, Part> parts, List<SheetLayout> layouts) {
        // Group parts by material
        Map<String, List<Part>> byMaterial = parts.values().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMaterial().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // Index layouts by material name
        Map<String, List<SheetLayout>> layoutsByMaterial = new LinkedHashMap<>();
        for (SheetLayout layout : layouts) {
            layoutsByMaterial.computeIfAbsent(layout.getMaterial().getName(), k -> new ArrayList<>())
                    .add(layout);
        }

        List<BomEntry> entries = new ArrayList<>();
        for (var entry : byMaterial.entrySet()) {
            List<Part> materialParts = entry.getValue();
            Material mat = materialParts.get(0).getMaterial();

            float totalArea = 0;
            for (Part p : materialParts) {
                totalArea += p.getCutWidthMm() * p.getCutHeightMm();
            }

            List<SheetLayout> matLayouts = layoutsByMaterial.get(mat.getName());
            Integer sheetCount = null;
            Float offcutPercent = null;
            if (matLayouts != null && !matLayouts.isEmpty()) {
                sheetCount = matLayouts.size();
                float totalOffcut = 0;
                for (SheetLayout sl : matLayouts) {
                    totalOffcut += sl.getOffcutPercent();
                }
                offcutPercent = totalOffcut / matLayouts.size();
            }

            entries.add(new BomEntry(mat, materialParts.size(), totalArea, sheetCount, offcutPercent));
        }

        return entries;
    }

    /**
     * Generate fastener summary from joints.
     */
    public static List<FastenerEntry> generateFasteners(JointRegistry joints) {
        List<FastenerEntry> entries = new ArrayList<>();

        int totalPocketScrews = 0;
        for (Joint j : joints.getAllJoints()) {
            if (j.getType() == JointType.POCKET_SCREW && j.getScrewCount() > 0) {
                totalPocketScrews += j.getScrewCount();
            }
        }
        if (totalPocketScrews > 0) {
            entries.add(new FastenerEntry("Pocket screws", totalPocketScrews));
        }

        // Future: biscuits, dowels, etc.

        return entries;
    }
}
