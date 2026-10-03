package dev.zoroaster1x.vlcskin.edit.commands;

import dev.zoroaster1x.vlcskin.edit.Command;
import java.util.List;

/**
 * Adds a node to a list at a position.
 */
public final class AddNodeCommand<T> implements Command {

    private final List<T> target;
    private final T node;
    private final int index;
    private final String description;

    public AddNodeCommand(List<T> target, T node, int index, String description) {
        this.target = target;
        this.node = node;
        this.index = Math.max(0, Math.min(index, target.size()));
        this.description = description;
    }

    @Override
    public void apply() {
        target.add(Math.min(index, target.size()), node);
    }

    @Override
    public void undo() {
        target.remove(node);
    }

    @Override
    public String description() {
        return description;
    }
}
