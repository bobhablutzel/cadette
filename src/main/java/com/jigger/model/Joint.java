package com.jigger.model;

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
