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

import com.jme3.math.Vector3f;
import lombok.Builder;
import lombok.Data;

/**
 * A single cut piece of material.
 *
 * A part has two user-specified cut dimensions (width and height on the face)
 * plus a thickness that comes from the material. This matches how woodworkers
 * think: "cut me a piece of 3/4 ply, 23" wide by 34" tall."
 *
 * The 3D representation maps to: X = cutWidth, Y = cutHeight, Z = thickness.
 */
@Data
@Builder(toBuilder = true)
public class Part {
    private final String name;
    private final Material material;
    private final float cutWidthMm;
    private final float cutHeightMm;
    private final Vector3f position;    // offset from origin (or assembly origin)
    private final GrainRequirement grainRequirement;

    /** Material thickness in mm. */
    public float getThicknessMm() {
        return material.getThicknessMm();
    }

    /**
     * Full 3D size vector: (width, height, thickness).
     * This is the full dimension, not half-extents — SceneManager handles the halving.
     */
    public Vector3f toSizeVector() {
        return new Vector3f(cutWidthMm, cutHeightMm, getThicknessMm());
    }
}
