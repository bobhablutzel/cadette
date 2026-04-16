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
 * Source: https://github.com/bobhablutzel/jigger
 */

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
