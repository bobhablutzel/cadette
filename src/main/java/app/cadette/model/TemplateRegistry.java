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

import java.util.*;
import java.util.regex.Pattern;

/**
 * Registry of template definitions. Templates are loaded at application
 * startup from {@code src/main/resources/templates/} (classpath) and
 * {@code ~/.cadette/templates/} (filesystem), or registered interactively
 * via the {@code define} command.
 */
public class TemplateRegistry {

    /**
     * Template names are Java-identifier-like segments joined by forward slashes
     * (e.g. "base_cabinet", "standard/cabinets/base_cabinet"). Each segment must
     * start with a letter and contain only letters, digits, and underscores.
     */
    private static final Pattern VALID_NAME =
            Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*(?:/[a-zA-Z][a-zA-Z0-9_]*)*");

    private static final TemplateRegistry INSTANCE = new TemplateRegistry();

    private final Map<String, Template> templates = new LinkedHashMap<>();

    private TemplateRegistry() {}

    public static TemplateRegistry instance() {
        return INSTANCE;
    }

    public Template get(String name) {
        return templates.get(name.toLowerCase());
    }

    public Collection<Template> getAll() {
        return Collections.unmodifiableCollection(templates.values());
    }

    public void register(Template template) {
        String name = template.getName();
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Invalid template name '" + name + "': each slash-separated segment "
                    + "must start with a letter and contain only letters, digits, and underscores.");
        }
        templates.put(name.toLowerCase(), template);
    }

    /** Clear all registered templates. Used by tests for isolation. */
    public void clear() {
        templates.clear();
    }

    /** Remove a single template by name. Returns true if it was present. */
    public boolean unregister(String name) {
        return templates.remove(name.toLowerCase()) != null;
    }
}
