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

package com.jigger;

import com.jigger.model.SheetLayout;
import com.jigger.model.SheetLayoutGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Swing panel that displays cut sheet layouts using Java2D.
 * Checks the SceneManager's dirty flag and lazy-recomputes layouts on paint.
 * Delegates all rendering to {@link CutSheetRenderer}.
 * Implements Scrollable for vertical scrolling when content exceeds the viewport.
 */
public class CutSheetPanel extends JPanel implements javax.swing.Scrollable {

    private final SceneManager sceneManager;
    private final Supplier<UnitSystem> unitsSupplier;
    private List<SheetLayout> cachedLayouts = List.of();

    public CutSheetPanel(SceneManager sceneManager, Supplier<UnitSystem> unitsSupplier) {
        this.sceneManager = sceneManager;
        this.unitsSupplier = unitsSupplier;
        setBackground(CutSheetRenderer.BACKGROUND);
        sceneManager.addSceneChangeListener(() -> SwingUtilities.invokeLater(this::refreshLayouts));
    }

    private void refreshLayouts() {
        if (sceneManager.isCutSheetDirty()) {
            cachedLayouts = SheetLayoutGenerator.generateLayouts(
                    sceneManager.getAllParts(), sceneManager.getKerfMm());
            sceneManager.clearCutSheetDirty();
            revalidate();
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // In case paint is called before the listener (e.g. tab switch)
        if (sceneManager.isCutSheetDirty()) {
            cachedLayouts = SheetLayoutGenerator.generateLayouts(
                    sceneManager.getAllParts(), sceneManager.getKerfMm());
            sceneManager.clearCutSheetDirty();
        }

        Graphics2D g2 = (Graphics2D) g.create();
        CutSheetRenderer.render(g2, getWidth(), getHeight(), cachedLayouts,
                unitsSupplier.get(), false);
        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getParent() != null ? getParent().getWidth() : 800;
        int contentHeight = CutSheetRenderer.computeTotalHeight(width, cachedLayouts);
        // Ensure we're at least as tall as the viewport so content fills the space
        int viewportHeight = getParent() != null ? getParent().getHeight() : contentHeight;
        return new Dimension(width, Math.max(contentHeight, viewportHeight));
    }

    // -- Scrollable: fill viewport width, scroll vertically only when needed --

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
        return 40;
    }

    @Override
    public int getScrollableBlockIncrement(java.awt.Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height - 40 : visibleRect.width - 40;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;  // fill the viewport width — no horizontal scrollbar
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        // Track viewport height when content fits (fill the space);
        // stop tracking when content overflows (enable scrolling)
        if (getParent() instanceof javax.swing.JViewport viewport) {
            int contentHeight = CutSheetRenderer.computeTotalHeight(viewport.getWidth(), cachedLayouts);
            return contentHeight <= viewport.getHeight();
        }
        return true;
    }
}
