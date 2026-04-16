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
