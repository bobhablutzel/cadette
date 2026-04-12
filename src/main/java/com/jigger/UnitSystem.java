package com.jigger;

import com.jigger.model.MeasurementSystem;
import lombok.Getter;

/**
 * Length units with conversion to/from millimeters (the internal unit).
 */
@Getter
public enum UnitSystem {

    MILLIMETERS("mm",     1.0f),
    CENTIMETERS("cm",     10.0f),
    METERS("m",           1000.0f),
    INCHES("in",          25.4f),
    FEET("ft",            304.8f),
    YARDS("yd",           914.4f);

    private final String abbreviation;
    private final float mmPerUnit;  // how many mm in one of this unit

    UnitSystem(String abbreviation, float mmPerUnit) {
        this.abbreviation = abbreviation;
        this.mmPerUnit = mmPerUnit;
    }

    /** Convert a value in this unit to internal millimeters. */
    public float toMm(float value) {
        return value * mmPerUnit;
    }

    /** Convert an internal millimeter value to this unit. */
    public float fromMm(float mm) {
        return mm / mmPerUnit;
    }

    /** Look up a unit by keyword (name or abbreviation), case-insensitive. */
    public static UnitSystem fromString(String text) {
        String lower = text.toLowerCase();
        for (UnitSystem u : values()) {
            if (u.name().toLowerCase().equals(lower) || u.abbreviation.equals(lower)) {
                return u;
            }
        }
        return null;
    }

    /** Whether this unit is metric or imperial. */
    public MeasurementSystem getMeasurementSystem() {
        return switch (this) {
            case INCHES, FEET, YARDS -> MeasurementSystem.IMPERIAL;
            default -> MeasurementSystem.METRIC;
        };
    }

    /** Comma-separated list of all accepted names for help text. */
    public static String allNames() {
        StringBuilder sb = new StringBuilder();
        for (UnitSystem u : values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(u.name().toLowerCase()).append(" (").append(u.abbreviation).append(")");
        }
        return sb.toString();
    }
}
