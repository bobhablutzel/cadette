package com.jigger.model;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A parametric template that can be instantiated to create an assembly of parts.
 * Body lines are raw command strings with $variable references that are
 * evaluated at instantiation time.
 *
 * Parameters can have aliases: define "cab" params width(w), height(h), depth(d)
 * Both the full name and alias can be used at instantiation time.
 */
@Data
public class Template {
    private final String name;
    private final List<String> paramNames;          // canonical names in order
    private final Map<String, String> paramAliases;  // alias → canonical name
    private final List<String> bodyLines;
    private final boolean builtIn;

    /** Simple constructor — no aliases. */
    public Template(String name, List<String> paramNames, List<String> bodyLines) {
        this(name, paramNames, Map.of(), bodyLines, false);
    }

    /** Constructor with aliases. */
    public Template(String name, List<String> paramNames, Map<String, String> paramAliases,
                    List<String> bodyLines, boolean builtIn) {
        this.name = name;
        this.paramNames = List.copyOf(paramNames);
        this.paramAliases = Map.copyOf(paramAliases);
        this.bodyLines = List.copyOf(bodyLines);
        this.builtIn = builtIn;
    }

    /** Resolve a param name or alias to its canonical name. Returns null if not recognized. */
    public String resolveParam(String nameOrAlias) {
        String lower = nameOrAlias.toLowerCase();
        if (paramNames.contains(lower)) return lower;
        return paramAliases.get(lower);
    }
}
