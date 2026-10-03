package io.github.zoroaster1x.vlcskin.edit.commands;

import io.github.zoroaster1x.vlcskin.edit.Command;
import java.util.List;

/**
 * Moves a node to another list or another position, which is what dragging an
 * item between containers or reordering it needs. Works when both lists are
 * the same object as well.
 */
public final class MoveNodeCommand<T> implements Command {

    private final List<T> from;
    private final List<T> to;
    private final T node;
    private final int fromIndex;
    private final int toIndex;
    private final String description;

    private int insertedAt = -1;

    public MoveNodeCommand(List<T> from, List<T> to, T node, int toIndex, String description) {
        this.from = from;
        this.to = to;
        this.node = node;
        this.fromIndex = from.indexOf(node);
        this.toIndex = toIndex;
        this.description = description;
    }

    @Override
    public void apply() {
        if (fromIndex < 0) {
            return;
        }
        int index = toIndex;
        if (from == to && toIndex > fromIndex) {
            index = toIndex - 1;
        }
        from.remove(node);
        insertedAt = Math.max(0, Math.min(index, to.size()));
        to.add(insertedAt, node);
    }

    @Override
    public void undo() {
        if (insertedAt < 0) {
            return;
        }
        to.remove(node);
        from.add(Math.max(0, Math.min(fromIndex, from.size())), node);
        insertedAt = -1;
    }

    @Override
    public String description() {
        return description;
    }
}
