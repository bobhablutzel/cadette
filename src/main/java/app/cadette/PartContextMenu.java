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

import app.cadette.command.CommandExecutor;
import app.cadette.model.Assembly;
import com.jme3.math.Vector3f;
import lombok.RequiredArgsConstructor;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Right-click context menu for a part or assembly: Info / Move… / Rotate… / Delete.
 * Each action builds a CADette command string and runs it through the executor,
 * so the command line stays the system of record.
 *
 * The menu's effective target is resolved from the current selection: if the
 * clicked part belongs to a selected assembly, the menu operates on the
 * assembly; otherwise on the clicked part directly. Selection itself isn't
 * modified by right-click.
 */
@RequiredArgsConstructor
public class PartContextMenu {

    private final SceneManager scene;
    private final SelectionManager selection;
    private final CommandExecutor executor;
    private final CommandPanel commandPanel;

    /** Show the menu at the given screen coordinates. */
    public void showAt(Component source, int x, int y, String clickedPart) {
        String target = resolveTarget(clickedPart);
        String label = scene.getAssembly(target) != null ? "Assembly: " : "Part: ";
        JPopupMenu menu = new JPopupMenu(label + target);
        menu.add(item("Info", () -> runCommand("show info \"" + target + "\"")));
        menu.add(item("Move…", () -> openMoveDialog(source, target)));
        menu.add(item("Rotate…", () -> openRotateDialog(source, target)));
        menu.addSeparator();
        menu.add(item("Delete", () -> runCommand("delete \"" + target + "\"")));
        menu.show(source, x, y);
    }

    /**
     * If the clicked part is covered by the current selection, prefer the
     * selected entity (an assembly wins over its parts).
     */
    private String resolveTarget(String clickedPart) {
        int slash = clickedPart.indexOf('/');
        if (slash > 0) {
            String assemblyName = clickedPart.substring(0, slash);
            if (scene.getAssembly(assemblyName) != null && selection.isSelected(assemblyName)) {
                return assemblyName;
            }
        }
        return clickedPart;
    }

    private void openMoveDialog(Component source, String target) {
        SceneManager.ObjectRecord rec = scene.getObjectRecord(target);
        Vector3f posMm = rec != null ? rec.position() : Vector3f.ZERO;
        List<String> references = referenceCandidatesExcluding(target);
        String cmd = MoveDialog.show(source, target, executor.getUnits(), posMm, references);
        if (cmd != null) runCommand(cmd);
    }

    private void openRotateDialog(Component source, String target) {
        Vector3f rotation = scene.getRotation(target);
        if (rotation == null) rotation = Vector3f.ZERO;
        String cmd = RotateDialog.show(source, target, rotation);
        if (cmd != null) runCommand(cmd);
    }

    /** Run a command through the executor and echo the command + result to the command panel. */
    private void runCommand(String cmd) {
        commandPanel.appendOutput("> " + cmd + "\n");
        String result = executor.execute(cmd);
        commandPanel.appendOutput(result + "\n");
    }

    /**
     * Reference candidates for relative placement: all nameable scene entities
     * minus the target's family. If target is an assembly, its constituent
     * parts are also excluded; if target is a part inside an assembly, the
     * owning assembly and sibling parts are excluded too.
     */
    private List<String> referenceCandidatesExcluding(String target) {
        Set<String> excluded = new HashSet<>();
        excluded.add(target);

        Assembly targetAssembly = scene.getAssembly(target);
        if (targetAssembly != null) {
            targetAssembly.getParts().forEach(p -> excluded.add(p.getName()));
        } else {
            int slash = target.indexOf('/');
            if (slash > 0) {
                String owning = target.substring(0, slash);
                Assembly owningAssembly = scene.getAssembly(owning);
                if (owningAssembly != null) {
                    excluded.add(owning);
                    owningAssembly.getParts().forEach(p -> excluded.add(p.getName()));
                }
            }
        }

        return Stream.concat(
                scene.getObjectRecords().keySet().stream(),
                scene.getAllAssemblies().keySet().stream())
                .filter(n -> !excluded.contains(n))
                .distinct()
                .sorted()
                .toList();
    }

    private static JMenuItem item(String label, Runnable action) {
        JMenuItem mi = new JMenuItem(label);
        mi.addActionListener(e -> action.run());
        return mi;
    }
}
