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

// Describes how a material is handled in the cut-planning pipeline. Orthogonal
// to MaterialType (which describes substance). The sheet layout / BOM paths
// branch on kind, not on whether sheet dimensions happen to be set.
public enum MaterialKind {
    // Rectangular stock sold in sheets; handled by the guillotine packer.
    SHEET_GOOD,
    // Boards sold in linear lengths; packed as runout from longer stock.
    SOLID_LUMBER,
    // Stone, engineered stone, quartz, laminate. Typically project-specific;
    // BOM reports per-piece, layout does not attempt to pack.
    SLAB,
    // Fasteners, pulls, hinges, glides — counted, not cut.
    HARDWARE
}
