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
 */
public class CutSheetPanel extends JPanel {

    private final SceneManager sceneManager;
    private final Supplier<UnitSystem> unitsSupplier;
    private List<SheetLayout> cachedLayouts = List.of();

    public CutSheetPanel(SceneManager sceneManager, Supplier<UnitSystem> unitsSupplier) {
        this.sceneManager = sceneManager;
        this.unitsSupplier = unitsSupplier;
        setBackground(CutSheetRenderer.BACKGROUND);
        sceneManager.addSceneChangeListener(() -> SwingUtilities.invokeLater(this::repaint));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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
}
