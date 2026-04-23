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
        return parts.values().stream()
                .map(part -> new CutListEntry(
                        part.getName(),
                        part.getMaterial(),
                        part.getCutWidthMm(),
                        part.getCutHeightMm(),
                        part.getThicknessMm(),
                        part.getGrainRequirement(),
                        operationsFor(part, joints)))
                .sorted(Comparator
                        .comparing((CutListEntry e) -> e.getMaterial().getName())
                        .thenComparing(CutListEntry::getPartName))
                .toList();
    }

    /** Machining operations applied to a part (only when it's the receiving side of a joint). */
    private static List<String> operationsFor(Part part, JointRegistry joints) {
        return joints.getJointsForPart(part.getName()).stream()
                .filter(j -> j.getReceivingPartName().equals(part.getName()))
                .map(CutListGenerator::describeOperation)
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<String> describeOperation(Joint j) {
        return switch (j.getType()) {
            case DADO -> Optional.of(String.format(
                    "dado %.1fmm deep for \"%s\"", j.getDepthMm(), j.getInsertedPartName()));
            case RABBET -> Optional.of(String.format(
                    "rabbet %.1fmm deep for \"%s\"", j.getDepthMm(), j.getInsertedPartName()));
            case POCKET_SCREW -> j.getScrewCount() > 0
                    ? Optional.of(String.format("%d pocket screw hole(s) for \"%s\"",
                            j.getScrewCount(), j.getInsertedPartName()))
                    : Optional.empty();
            default -> Optional.empty();  // butt — no machining
        };
    }

    /**
     * Generate the BOM using actual sheet layout results from the packer.
     */
    public static List<BomEntry> generateBom(Map<String, Part> parts, List<SheetLayout> layouts) {
        Map<String, List<Part>> byMaterial = parts.values().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMaterial().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<String, List<SheetLayout>> layoutsByMaterial = layouts.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getMaterial().getName(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return byMaterial.values().stream()
                .map(materialParts -> toBomEntry(materialParts,
                        layoutsByMaterial.get(materialParts.get(0).getMaterial().getName())))
                .toList();
    }

    private static BomEntry toBomEntry(List<Part> materialParts, List<SheetLayout> matLayouts) {
        Material mat = materialParts.get(0).getMaterial();
        float totalArea = (float) materialParts.stream()
                .mapToDouble(p -> p.getCutWidthMm() * p.getCutHeightMm())
                .sum();
        boolean haveLayouts = matLayouts != null && !matLayouts.isEmpty();
        Integer sheetCount = haveLayouts ? matLayouts.size() : null;
        Float offcutPercent = haveLayouts
                ? (float) matLayouts.stream()
                        .mapToDouble(SheetLayout::getOffcutPercent)
                        .average()
                        .orElse(0)
                : null;
        return new BomEntry(mat, materialParts.size(), totalArea, sheetCount, offcutPercent);
    }

    /**
     * Generate fastener summary from joints.
     */
    public static List<FastenerEntry> generateFasteners(JointRegistry joints) {
        int totalPocketScrews = joints.getAllJoints().stream()
                .filter(j -> j.getType() == JointType.POCKET_SCREW)
                .mapToInt(Joint::getScrewCount)
                .sum();
        return totalPocketScrews > 0
                ? List.of(new FastenerEntry("Pocket screws", totalPocketScrews))
                : List.of();
        // Future: biscuits, dowels, etc.
    }
}
