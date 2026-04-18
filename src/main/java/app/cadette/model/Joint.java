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

import lombok.Builder;
import lombok.Data;

/**
 * A joint between two parts. The receiving part hosts the joint (e.g., the dado groove).
 * The inserted part sits in/against it.
 *
 * For dado/rabbet: depthMm is how deep the groove is cut into the receiving part.
 * For pocket_screw: screwCount and screwSpacingMm describe the fasteners.
 * For butt: no additional parameters.
 */
@Data
@Builder
public class Joint {
    private final String receivingPartName;
    private final String insertedPartName;
    private final JointType type;
    @Builder.Default private final float depthMm = 0;
    @Builder.Default private final int screwCount = 0;
    @Builder.Default private final float screwSpacingMm = 0;

    public String getId() {
        return receivingPartName + "->" + insertedPartName + ":" + type.name();
    }
}
