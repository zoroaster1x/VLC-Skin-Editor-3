package dev.zoroaster1x.vlcskin.edit;

/**
 * One undoable change.
 */
public interface Command {

    /**
     * Applies the change.
     */
    void apply();

    /**
     * Reverts the change.
     */
    void undo();

    String description();
}
