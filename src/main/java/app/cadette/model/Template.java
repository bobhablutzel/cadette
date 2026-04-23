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

import app.cadette.command.CadetteCommandParser;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A parametric template that can be instantiated to create an assembly of parts.
 *
 * The template stores its body both as raw lines (for {@code show template}
 * and human inspection — comments survive here) and as a parsed tree (for
 * instantiation and validation). The tree is parsed once at define time and
 * walked at every instantiation.
 *
 * Parameters can have aliases: define "cab" params width(w), height(h), depth(d)
 * Both the full name and alias can be used at instantiation time.
 */
@Data
public class Template {
    private final String name;
    private final List<String> paramNames;          // canonical names in order
    private final Map<String, String> paramAliases;  // alias → canonical name
    /** Raw body lines as the user wrote them — preserves comments and source formatting. */
    private final List<String> bodyLines;
    /**
     * Parsed body. Walked by the visitor at every instantiation. Null only for
     * legacy test Templates that bypass the loader and have no parse tree.
     */
    private final CadetteCommandParser.TemplateBodyContext parsedBody;
    // Human-readable pointer to where this template came from, for `show templates`
    // and `which`. Conventions: "classpath:<path>" for bundled, the absolute path
    // for filesystem, "interactive" for REPL defines, null if unknown.
    private final String source;

    // Hand-coded: convenience ctor that delegates to the full form with
    // no aliases, no parsed body (legacy tests), and no source tag.
    public Template(String name, List<String> paramNames, List<String> bodyLines) {
        this(name, paramNames, Map.of(), bodyLines, null, null);
    }

    // Hand-coded: no-parsedBody 4-arg overload kept so existing tests that
    // construct templates directly don't need to thread a parse tree through.
    public Template(String name, List<String> paramNames, Map<String, String> paramAliases,
                    List<String> bodyLines) {
        this(name, paramNames, paramAliases, bodyLines, null, null);
    }

    // Hand-coded: defensive List.copyOf / Map.copyOf so template bodies and
    // param metadata are insulated from post-construction mutation.
    // @RequiredArgsConstructor / @AllArgsConstructor would store the caller's
    // references directly.
    public Template(String name, List<String> paramNames, Map<String, String> paramAliases,
                    List<String> bodyLines,
                    CadetteCommandParser.TemplateBodyContext parsedBody,
                    String source) {
        this.name = name;
        this.paramNames = List.copyOf(paramNames);
        this.paramAliases = Map.copyOf(paramAliases);
        this.bodyLines = List.copyOf(bodyLines);
        this.parsedBody = parsedBody;
        this.source = source;
    }

    /** Resolve a param name or alias to its canonical name. Returns null if not recognized. */
    public String resolveParam(String nameOrAlias) {
        String lower = nameOrAlias.toLowerCase();
        if (paramNames.contains(lower)) return lower;
        return paramAliases.get(lower);
    }

    /** Templates under the reserved "standard/" namespace ship with the app. */
    public boolean isStandard() {
        return name.startsWith("standard/");
    }
}
