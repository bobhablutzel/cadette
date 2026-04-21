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

package app.cadette;

import com.jme3.math.Vector3f;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for the `move` command. Supports both absolute placement
 * (x, y, z in current units) and relative placement (direction/reference/gap).
 * Returns the `move` command string to execute, or null if cancelled.
 */
public class MoveDialog {

    private static final String[] DIRECTIONS =
            { "left", "right", "in-front", "behind", "above", "below" };

    /**
     * Show the dialog modally.
     *
     * @param referenceCandidates scene names to offer for relative placement.
     *                            Should exclude the moving target. If empty, the
     *                            relative option is disabled.
     * @return the `move` command to execute, or null if cancelled.
     */
    public static String show(Component parent, String partName, UnitSystem units,
                              Vector3f currentPositionMm, List<String> referenceCandidates) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Move '" + partName + "'", Dialog.ModalityType.APPLICATION_MODAL);

        boolean hasReferences = !referenceCandidates.isEmpty();

        JRadioButton absRadio = new JRadioButton("To position", true);
        JRadioButton relRadio = new JRadioButton("Relative to another object");
        relRadio.setEnabled(hasReferences);
        ButtonGroup group = new ButtonGroup();
        group.add(absRadio);
        group.add(relRadio);

        String abbr = units.getAbbreviation();
        JTextField xField = new JTextField(formatValue(units.fromMm(currentPositionMm.x)), 8);
        JTextField yField = new JTextField(formatValue(units.fromMm(currentPositionMm.y)), 8);
        JTextField zField = new JTextField(formatValue(units.fromMm(currentPositionMm.z)), 8);

        JComboBox<String> dirCombo = new JComboBox<>(DIRECTIONS);
        JComboBox<String> refCombo = new JComboBox<>(referenceCandidates.toArray(String[]::new));
        refCombo.setEnabled(hasReferences);
        JTextField gapField = new JTextField(6);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 4;
        panel.add(absRadio, c);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; panel.add(new JLabel("X (" + abbr + "):"), c);
        c.gridx = 1; panel.add(xField, c);
        c.gridx = 2; panel.add(new JLabel("Y (" + abbr + "):"), c);
        c.gridx = 3; panel.add(yField, c);
        c.gridy = 2; c.gridx = 0; panel.add(new JLabel("Z (" + abbr + "):"), c);
        c.gridx = 1; panel.add(zField, c);

        c.gridy = 3; c.gridx = 0; c.gridwidth = 4;
        panel.add(relRadio, c);

        c.gridwidth = 1;
        c.gridy = 4; c.gridx = 0; panel.add(new JLabel("Direction:"), c);
        c.gridx = 1; panel.add(dirCombo, c);
        c.gridy = 5; c.gridx = 0; panel.add(new JLabel("Reference:"), c);
        c.gridx = 1; c.gridwidth = 3; c.fill = GridBagConstraints.HORIZONTAL; panel.add(refCombo, c);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridy = 6; c.gridx = 0; panel.add(new JLabel("Gap (" + abbr + "):"), c);
        c.gridx = 1; panel.add(gapField, c);

        // Enable only the fields belonging to the selected mode, so users don't
        // fill in one side and wonder why the other side got used.
        Runnable syncEnabled = () -> {
            boolean abs = absRadio.isSelected();
            xField.setEnabled(abs);
            yField.setEnabled(abs);
            zField.setEnabled(abs);
            dirCombo.setEnabled(!abs && hasReferences);
            refCombo.setEnabled(!abs && hasReferences);
            gapField.setEnabled(!abs && hasReferences);
        };
        absRadio.addActionListener(e -> syncEnabled.run());
        relRadio.addActionListener(e -> syncEnabled.run());
        syncEnabled.run();

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonBar.add(ok);
        buttonBar.add(cancel);

        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonBar, BorderLayout.SOUTH);

        String[] result = { null };
        ok.addActionListener(e -> {
            try {
                if (absRadio.isSelected()) {
                    float x = Float.parseFloat(xField.getText().trim());
                    float y = Float.parseFloat(yField.getText().trim());
                    float z = Float.parseFloat(zField.getText().trim());
                    result[0] = buildAbsoluteCommand(partName, x, y, z);
                } else {
                    String ref = (String) refCombo.getSelectedItem();
                    if (ref == null || ref.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "No reference selected.",
                                "Move", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String direction = (String) dirCombo.getSelectedItem();
                    String gapText = gapField.getText().trim();
                    Float gap = gapText.isEmpty() ? null : Float.parseFloat(gapText);
                    result[0] = buildRelativeCommand(partName, direction, ref, gap);
                }
                dialog.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.",
                        "Move", JOptionPane.WARNING_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(ok);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);  // blocks until disposed (modal)
        return result[0];
    }

    // ---- Command-string builders (package-private so they're unit-testable) ----

    static String buildAbsoluteCommand(String partName, float x, float y, float z) {
        return String.format("move \"%s\" to %s, %s, %s",
                partName, formatValue(x), formatValue(y), formatValue(z));
    }

    static String buildRelativeCommand(String partName, String direction, String reference, Float gapUnits) {
        StringBuilder sb = new StringBuilder();
        sb.append("move \"").append(partName).append("\" to ")
                .append(direction).append(" of \"").append(reference).append("\"");
        if (gapUnits != null) {
            sb.append(" gap ").append(formatValue(gapUnits));
        }
        return sb.toString();
    }

    private static String formatValue(float v) {
        // Whole numbers: no decimal point. Otherwise: up to 4 places, trimmed.
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.4f", v).replaceFirst("0+$", "").replaceFirst("\\.$", "");
    }
}
