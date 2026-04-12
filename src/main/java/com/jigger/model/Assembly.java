package com.jigger.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named collection of parts that form a single object (e.g., a cabinet).
 * For Phase 1, assemblies are thin wrappers. Individual parts can also exist standalone.
 */
@Data
public class Assembly {
    private final String name;
    private final List<Part> parts = new ArrayList<>();

    public void addPart(Part part) {
        parts.add(part);
    }

    public Part getPart(String partName) {
        return parts.stream()
                .filter(p -> p.getName().equals(partName))
                .findFirst()
                .orElse(null);
    }

    public boolean removePart(String partName) {
        return parts.removeIf(p -> p.getName().equals(partName));
    }

    public List<Part> getParts() {
        return Collections.unmodifiableList(parts);
    }
}
