package com.jigger.command;

/**
 * Represents a scene mutation that can be undone and redone.
 */
public interface UndoableAction {

    /** Reverse this action. */
    void undo();

    /** Re-apply this action after it was undone. */
    void redo();

    /** Human-readable description for feedback messages. */
    String description();
}
