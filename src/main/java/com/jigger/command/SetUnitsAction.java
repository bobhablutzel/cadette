package com.jigger.command;

import com.jigger.UnitSystem;

import java.util.function.Consumer;

public class SetUnitsAction implements UndoableAction {

    private final Consumer<UnitSystem> setter;
    private final UnitSystem oldUnits;
    private final UnitSystem newUnits;

    public SetUnitsAction(Consumer<UnitSystem> setter, UnitSystem oldUnits, UnitSystem newUnits) {
        this.setter = setter;
        this.oldUnits = oldUnits;
        this.newUnits = newUnits;
    }

    @Override
    public void undo() {
        setter.accept(oldUnits);
    }

    @Override
    public void redo() {
        setter.accept(newUnits);
    }

    @Override
    public String description() {
        return "set units " + newUnits.name().toLowerCase();
    }
}
