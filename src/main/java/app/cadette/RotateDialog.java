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

/**
 * Modal dialog for the `rotate` command. Prompts for X/Y/Z rotation in degrees.
 * Returns the `rotate` command string, or null if cancelled.
 */
public class RotateDialog {

    public static String show(Component parent, String partName, Vector3f currentRotationDegrees) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Rotate '" + partName + "'", Dialog.ModalityType.APPLICATION_MODAL);

        JTextField xField = new JTextField(formatValue(currentRotationDegrees.x), 8);
        JTextField yField = new JTextField(formatValue(currentRotationDegrees.y), 8);
        JTextField zField = new JTextField(formatValue(currentRotationDegrees.z), 8);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 4;
        panel.add(new JLabel("Rotation (degrees)"), c);

        c.gridwidth = 1;
        c.gridy = 1; c.gridx = 0; panel.add(new JLabel("X:"), c);
        c.gridx = 1; panel.add(xField, c);
        c.gridx = 2; panel.add(new JLabel("Y:"), c);
        c.gridx = 3; panel.add(yField, c);
        c.gridy = 2; c.gridx = 0; panel.add(new JLabel("Z:"), c);
        c.gridx = 1; panel.add(zField, c);

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
                float x = Float.parseFloat(xField.getText().trim());
                float y = Float.parseFloat(yField.getText().trim());
                float z = Float.parseFloat(zField.getText().trim());
                result[0] = buildCommand(partName, x, y, z);
                dialog.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.",
                        "Rotate", JOptionPane.WARNING_MESSAGE);
            }
        });
        cancel.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(ok);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return result[0];
    }

    static String buildCommand(String partName, float x, float y, float z) {
        return String.format("rotate \"%s\" %s, %s, %s",
                partName, formatValue(x), formatValue(y), formatValue(z));
    }

    private static String formatValue(float v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.4f", v).replaceFirst("0+$", "").replaceFirst("\\.$", "");
    }
}
