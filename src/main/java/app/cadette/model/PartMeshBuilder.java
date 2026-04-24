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

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds a jME3 {@link Mesh} for a {@link Part}, respecting any rectangular
 * {@link Cutout.Rect}s attached to it — both through-cuts (full thickness)
 * and partial-depth pockets. The resulting mesh is centred at the origin with
 * dimensions {@code (cutWidthMm × cutHeightMm × thicknessMm)}, matching jME3's
 * {@code Box} primitive convention so downstream scene placement is unchanged.
 *
 * <h2>Algorithm (rect decomposition)</h2>
 *
 * <p>Every cutout edge (clipped to the part) contributes to a uniform grid
 * laid over the cut face. Each cell in the grid is classified by its
 * "top-solid Z" — the height to which the solid material extends above the
 * bottom face:
 *
 * <ul>
 *   <li>{@code +halfT} — fully solid (no cutout covers this cell).</li>
 *   <li>{@code +halfT − depth} — covered by a partial-depth pocket.</li>
 *   <li>{@code null} — covered by a through-cut; no material at all.</li>
 * </ul>
 *
 * <p>Where partial and through cuts overlap, through wins (removes all
 * material). Where two pockets of different depths overlap, the deeper one
 * wins (its floor is lower).
 *
 * <p>The mesh assembles in two passes:
 *
 * <ol>
 *   <li><em>Faces:</em> each non-through cell emits a top face at its
 *       top-solid Z (which is either the panel top or a pocket floor,
 *       always with {@code +Z} normal pointing up out of the material)
 *       and a bottom face at {@code −halfT} with {@code −Z} normal.</li>
 *   <li><em>Walls:</em> each unique edge in the grid is visited once. The
 *       two cells sharing that edge have top-solid Z values {@code a} and
 *       {@code b} (either may be {@code null} for through or off-grid).
 *       The wall bridges the range where one cell has solid material that
 *       the other lacks. If both have material to the same height, no wall.
 *       If one is {@code null}, the whole range of the other is exposed.
 *       Normal points toward the emptier side.</li>
 * </ol>
 *
 * <p>Non-{@link Cutout.Rect} variants (Circle, Polygon, Spline) are ignored
 * until proper triangulation lands in a later phase.
 */
public final class PartMeshBuilder {

    private PartMeshBuilder() {}

    public static Mesh build(Part part) {
        return build(part.getCutWidthMm(), part.getCutHeightMm(),
                part.getThicknessMm(), part.getCutouts());
    }

    /** Overload used by tests — no Part required, just raw dimensions + cutouts. */
    static Mesh build(float widthMm, float heightMm, float thicknessMm,
                      List<Cutout> cutouts) {
        // Split and clip cutouts. Anything outside the part or zero-area
        // after clipping is dropped silently.
        List<Cutout.Rect> throughRects = new ArrayList<>();
        List<Cutout.Rect> pocketRects = new ArrayList<>();
        for (Cutout c : cutouts) {
            if (!(c instanceof Cutout.Rect r)) continue;  // circle/polygon/spline: future
            Cutout.Rect clipped = clip(r, widthMm, heightMm);
            if (clipped == null) continue;
            if (clipped.depthMm() == null) throughRects.add(clipped);
            else pocketRects.add(clipped);
        }

        // Grid edges: every cutout edge counts, no matter through or partial.
        List<Cutout.Rect> allRects = new ArrayList<>(throughRects);
        allRects.addAll(pocketRects);
        float[] xs = distinctEdges(widthMm, allRects, true);
        float[] ys = distinctEdges(heightMm, allRects, false);
        int nx = xs.length - 1;
        int ny = ys.length - 1;

        float halfW = widthMm * 0.5f;
        float halfH = heightMm * 0.5f;
        float halfT = thicknessMm * 0.5f;

        // Per-cell top-solid Z. null = through (no material). halfT = solid.
        // halfT - depth = pocket with that depth.
        Float[][] topSolidZ = new Float[nx][ny];
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                float cx = (xs[i] + xs[i + 1]) * 0.5f;
                float cy = (ys[j] + ys[j + 1]) * 0.5f;
                if (pointInside(cx, cy, throughRects)) {
                    topSolidZ[i][j] = null;  // through — no material
                    continue;
                }
                // If multiple overlapping pockets, deepest wins (smallest topSolidZ).
                Float best = halfT;
                for (Cutout.Rect r : pocketRects) {
                    if (cx >= r.xMm() && cx <= r.xMm() + r.widthMm()
                            && cy >= r.yMm() && cy <= r.yMm() + r.heightMm()) {
                        float z = halfT - r.depthMm();
                        if (z < best) best = z;
                    }
                }
                topSolidZ[i][j] = best;
            }
        }

        MeshBuf buf = new MeshBuf();

        // Pass 1: faces. Top face at topSolidZ (for cells with any material),
        // bottom face at −halfT (same). Skip through cells entirely.
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                Float top = topSolidZ[i][j];
                if (top == null) continue;
                float x0 = xs[i] - halfW, x1 = xs[i + 1] - halfW;
                float y0 = ys[j] - halfH, y1 = ys[j + 1] - halfH;
                // Top (or pocket floor) — normal always +Z.
                buf.addQuad(
                        x0, y0, top,  x1, y0, top,  x1, y1, top,  x0, y1, top,
                        0, 0, 1);
                // Bottom — normal −Z.
                buf.addQuad(
                        x0, y1, -halfT, x1, y1, -halfT, x1, y0, -halfT, x0, y0, -halfT,
                        0, 0, -1);
            }
        }

        // Pass 2: walls. Iterate every unique edge once.
        // Vertical edges at xs[i], between cells (i-1, j) and (i, j).
        for (int i = 0; i <= nx; i++) {
            for (int j = 0; j < ny; j++) {
                Float a = topSolidZAt(topSolidZ, i - 1, j, nx, ny);
                Float b = topSolidZAt(topSolidZ, i, j, nx, ny);
                emitVerticalEdgeWall(buf, xs[i] - halfW,
                        ys[j] - halfH, ys[j + 1] - halfH,
                        a, b, halfT);
            }
        }
        // Horizontal edges at ys[j], between cells (i, j-1) and (i, j).
        for (int j = 0; j <= ny; j++) {
            for (int i = 0; i < nx; i++) {
                Float a = topSolidZAt(topSolidZ, i, j - 1, nx, ny);
                Float b = topSolidZAt(topSolidZ, i, j, nx, ny);
                emitHorizontalEdgeWall(buf, ys[j] - halfH,
                        xs[i] - halfW, xs[i + 1] - halfW,
                        a, b, halfT);
            }
        }

        return buf.toMesh();
    }

    // ---- Edge-wall emission ----

    /**
     * Wall at a vertical edge (fixed X, varying Y, full Z range as determined
     * by the cell types). {@code a} is the top-solid-Z of the cell to the −X
     * side, {@code b} of the +X side. Either may be {@code null} (through or
     * off-grid). Emits a wall over the Z range where exactly one side has
     * material that the other lacks; normal points toward the emptier side.
     */
    private static void emitVerticalEdgeWall(MeshBuf buf, float x,
                                             float y0, float y1,
                                             Float a, Float b, float halfT) {
        WallRange w = wallRange(a, b, halfT);
        if (w == null) return;
        // Vertical edge wall — rectangle in Y-Z plane at fixed X.
        // Winding chosen so the emitted normal matches w.normalSign along X.
        if (w.normalSign > 0) {
            // Normal +X: face points toward +X side (emptier = right).
            buf.addQuad(
                    x, y0, w.zLow,  x, y1, w.zLow,  x, y1, w.zHigh,  x, y0, w.zHigh,
                    1, 0, 0);
        } else {
            // Normal −X: face points toward −X side (emptier = left).
            buf.addQuad(
                    x, y0, w.zHigh, x, y1, w.zHigh, x, y1, w.zLow,  x, y0, w.zLow,
                    -1, 0, 0);
        }
    }

    /**
     * Wall at a horizontal edge (fixed Y, varying X). {@code a} is the
     * top-solid-Z of the cell to the −Y side, {@code b} of the +Y side.
     */
    private static void emitHorizontalEdgeWall(MeshBuf buf, float y,
                                               float x0, float x1,
                                               Float a, Float b, float halfT) {
        WallRange w = wallRange(a, b, halfT);
        if (w == null) return;
        if (w.normalSign > 0) {
            // Normal +Y: toward the +Y side (emptier).
            buf.addQuad(
                    x0, y, w.zHigh, x1, y, w.zHigh, x1, y, w.zLow,  x0, y, w.zLow,
                    0, 1, 0);
        } else {
            // Normal −Y.
            buf.addQuad(
                    x0, y, w.zLow,  x1, y, w.zLow,  x1, y, w.zHigh, x0, y, w.zHigh,
                    0, -1, 0);
        }
    }

    /**
     * Compute the Z range of a wall between two cells, plus which side the
     * normal points toward. Returns {@code null} if no wall is needed (both
     * solid to the same height, or neither solid).
     *
     * <p>{@code normalSign} is {@code +1} if the wall's normal points toward
     * cell {@code b} (the +axis side), {@code -1} if toward cell {@code a}.
     */
    private static WallRange wallRange(Float a, Float b, float halfT) {
        if (a == null && b == null) return null;
        if (a == null) {
            // b has material up to b; expose from the bottom to b's top.
            return new WallRange(-halfT, b, -1);  // normal toward a (emptier)
        }
        if (b == null) {
            return new WallRange(-halfT, a, +1);  // normal toward b (emptier)
        }
        if (a.equals(b)) return null;
        if (a < b) {
            // b has more material above a. Wall in the range [a, b], normal → a.
            return new WallRange(a, b, -1);
        }
        return new WallRange(b, a, +1);
    }

    private record WallRange(float zLow, float zHigh, int normalSign) {}

    // ---- Grid + classification helpers ----

    private static Float topSolidZAt(Float[][] topSolidZ, int i, int j, int nx, int ny) {
        if (i < 0 || j < 0 || i >= nx || j >= ny) return null;  // off-grid = no material
        return topSolidZ[i][j];
    }

    private static boolean pointInside(float x, float y, List<Cutout.Rect> rects) {
        for (Cutout.Rect r : rects) {
            if (x >= r.xMm() && x <= r.xMm() + r.widthMm()
                    && y >= r.yMm() && y <= r.yMm() + r.heightMm()) {
                return true;
            }
        }
        return false;
    }

    /** Clip a cutout rect to the part's bounds. Returns null if nothing remains. */
    private static Cutout.Rect clip(Cutout.Rect r, float widthMm, float heightMm) {
        float x0 = Math.max(0, r.xMm());
        float y0 = Math.max(0, r.yMm());
        float x1 = Math.min(widthMm, r.xMm() + r.widthMm());
        float y1 = Math.min(heightMm, r.yMm() + r.heightMm());
        if (x1 <= x0 || y1 <= y0) return null;
        return new Cutout.Rect(x0, y0, x1 - x0, y1 - y0, r.depthMm());
    }

    /** Sorted unique x- (or y-) coordinates from part edges and cutout edges. */
    private static float[] distinctEdges(float partSize, List<Cutout.Rect> rects, boolean xAxis) {
        Set<Float> coords = new TreeSet<>();
        coords.add(0f);
        coords.add(partSize);
        for (Cutout.Rect r : rects) {
            if (xAxis) {
                coords.add(r.xMm());
                coords.add(r.xMm() + r.widthMm());
            } else {
                coords.add(r.yMm());
                coords.add(r.yMm() + r.heightMm());
            }
        }
        float[] out = new float[coords.size()];
        int i = 0;
        for (float c : coords) out[i++] = c;
        return out;
    }

    /**
     * Accumulator for vertex / normal / index data. Each face uses distinct
     * vertices so jME3's lighting can apply a flat per-face normal — matches
     * how the built-in Box primitive handles its 24 vertices.
     */
    private static final class MeshBuf {
        private final List<Float> positions = new ArrayList<>();
        private final List<Float> normals = new ArrayList<>();
        private final List<Integer> indices = new ArrayList<>();

        void addQuad(float ax, float ay, float az,
                     float bx, float by, float bz,
                     float cx, float cy, float cz,
                     float dx, float dy, float dz,
                     float nx, float ny, float nz) {
            int base = positions.size() / 3;
            addVertex(ax, ay, az, nx, ny, nz);
            addVertex(bx, by, bz, nx, ny, nz);
            addVertex(cx, cy, cz, nx, ny, nz);
            addVertex(dx, dy, dz, nx, ny, nz);
            // Quad split: (a, b, c) + (a, c, d). CCW in the +normal direction.
            indices.add(base);     indices.add(base + 1); indices.add(base + 2);
            indices.add(base);     indices.add(base + 2); indices.add(base + 3);
        }

        private void addVertex(float x, float y, float z, float nx, float ny, float nz) {
            positions.add(x); positions.add(y); positions.add(z);
            normals.add(nx); normals.add(ny); normals.add(nz);
        }

        Mesh toMesh() {
            Mesh m = new Mesh();
            FloatBuffer posBuf = BufferUtils.createFloatBuffer(toFloatArray(positions));
            FloatBuffer normBuf = BufferUtils.createFloatBuffer(toFloatArray(normals));
            IntBuffer idxBuf = BufferUtils.createIntBuffer(toIntArray(indices));
            m.setBuffer(VertexBuffer.Type.Position, 3, posBuf);
            m.setBuffer(VertexBuffer.Type.Normal, 3, normBuf);
            m.setBuffer(VertexBuffer.Type.Index, 3, idxBuf);
            m.updateBound();
            return m;
        }

        private static float[] toFloatArray(List<Float> list) {
            float[] a = new float[list.size()];
            for (int i = 0; i < list.size(); i++) a[i] = list.get(i);
            return a;
        }

        private static int[] toIntArray(List<Integer> list) {
            int[] a = new int[list.size()];
            for (int i = 0; i < list.size(); i++) a[i] = list.get(i);
            return a;
        }
    }
}
