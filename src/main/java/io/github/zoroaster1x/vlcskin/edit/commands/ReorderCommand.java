package io.github.zoroaster1x.vlcskin.edit.commands;

import io.github.zoroaster1x.vlcskin.edit.Command;
import java.util.List;

/**
 * Moves a node inside its list, used for move up and move down.
 */
public final class ReorderCommand<T> implements Command {

    private final List<T> target;
    private final int from;
    private final int to;
    private final String description;

    public ReorderCommand(List<T> target, int from, int to, String description) {
        this.target = target;
        this.from = from;
        this.to = to;
        this.description = description;
    }

    @Override
    public void apply() {
        if (from < 0 || from >= target.size() || to < 0 || to >= target.size()) {
            return;
        }
        target.add(to, target.remove(from));
    }

    @Override
    public void undo() {
        if (from < 0 || from >= target.size() || to < 0 || to >= target.size()) {
            return;
        }
        target.add(from, target.remove(to));
    }

    @Override
    public String description() {
        return description;
    }
}
