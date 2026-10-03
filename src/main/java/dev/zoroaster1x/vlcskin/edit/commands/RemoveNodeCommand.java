package dev.zoroaster1x.vlcskin.edit.commands;

import dev.zoroaster1x.vlcskin.edit.Command;
import java.util.List;

/**
 * Removes a node from a list, remembering its position.
 */
public final class RemoveNodeCommand<T> implements Command {

    private final List<T> target;
    private final T node;
    private final int index;
    private final String description;

    public RemoveNodeCommand(List<T> target, T node, String description) {
        this.target = target;
        this.node = node;
        this.index = target.indexOf(node);
        this.description = description;
    }

    @Override
    public void apply() {
        target.remove(node);
    }

    @Override
    public void undo() {
        target.add(Math.max(0, Math.min(index, target.size())), node);
    }

    @Override
    public String description() {
        return description;
    }
}
