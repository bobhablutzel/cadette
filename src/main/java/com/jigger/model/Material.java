package com.jigger.model;

import com.jme3.math.ColorRGBA;
import lombok.Builder;
import lombok.Data;

/**
 * A material that parts can be made from.
 * Thickness is always stored in mm. Sheet dimensions are null for non-sheet goods.
 */
@Data
@Builder
public class Material {
    private final String name;          // slug, e.g. "plywood-3/4"
    private final String displayName;   // human-readable, e.g. "3/4\" Cabinet Plywood"
    private final MaterialType type;
    private final float thicknessMm;
    private final Float sheetWidthMm;   // null for non-sheet goods
    private final Float sheetHeightMm;  // null for non-sheet goods
    private final GrainDirection grainDirection;
    private final MeasurementSystem measurementSystem;
    private final ColorRGBA displayColor;
}
