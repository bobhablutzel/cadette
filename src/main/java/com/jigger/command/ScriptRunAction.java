package com.jigger.command;

import java.util.List;

/**
 * Composite undoable action for a script run.
 * Undo reverses all actions in reverse order. Redo replays them in order.
 */
public class ScriptRunAction implements UndoableAction {

    private final String scriptName;
    private final List<UndoableAction> actions;

    public ScriptRunAction(String scriptName, List<UndoableAction> actions) {
        this.scriptName = scriptName;
        this.actions = List.copyOf(actions);
    }

    @Override
    public void undo() {
        for (int i = actions.size() - 1; i >= 0; i--) {
            actions.get(i).undo();
        }
    }

    @Override
    public void redo() {
        for (UndoableAction action : actions) {
            action.redo();
        }
    }

    @Override
    public String description() {
        return "run " + scriptName + " (" + actions.size() + " actions)";
    }
}
