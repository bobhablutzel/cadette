package com.jigger.model;

import lombok.Getter;

@Getter
public enum JointType {
    BUTT("Butt joint", false),
    DADO("Dado", true),
    RABBET("Rabbet", true),
    POCKET_SCREW("Pocket screw", false);
    // Future: BISCUIT, DOWEL, DOVETAIL, MORTISE_TENON, BOX_JOINT

    private final String displayName;
    private final boolean affectsGeometry;

    JointType(String displayName, boolean affectsGeometry) {
        this.displayName = displayName;
        this.affectsGeometry = affectsGeometry;
    }

    public static JointType fromString(String text) {
        String lower = text.toLowerCase().replace('-', '_').replace(' ', '_');
        for (JointType jt : values()) {
            if (jt.name().toLowerCase().equals(lower)) return jt;
        }
        return null;
    }
}
