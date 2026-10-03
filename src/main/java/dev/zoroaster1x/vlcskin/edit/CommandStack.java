package dev.zoroaster1x.vlcskin.edit;

import java.util.ArrayList;
import java.util.List;

/**
 * Undo and redo with a bounded depth.
 */
public final class CommandStack {

    private static final int DEFAULT_LIMIT = 50;

    private final List<Command> commands = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final int limit;
    private int cursor;

    public CommandStack() {
        this(DEFAULT_LIMIT);
    }

    public CommandStack(int limit) {
        this.limit = limit;
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    public boolean canUndo() {
        return cursor > 0;
    }

    public boolean canRedo() {
        return cursor < commands.size();
    }

    /**
     * The command at the cursor, the most recent applied one, or null.
     */
    public Command current() {
        return cursor > 0 ? commands.get(cursor - 1) : null;
    }

    public String undoDescription() {
        return cursor > 0 ? commands.get(cursor - 1).description() : null;
    }

    public String redoDescription() {
        return cursor < commands.size() ? commands.get(cursor).description() : null;
    }

    public void run(Command command) {
        command.apply();
        while (commands.size() > cursor) {
            commands.removeLast();
        }
        commands.add(command);
        cursor++;
        if (commands.size() > limit) {
            commands.removeFirst();
            cursor--;
        }
        fire();
    }

    public void undo() {
        if (!canUndo()) {
            return;
        }
        Command command = commands.get(--cursor);
        command.undo();
        fire();
    }

    public void redo() {
        if (!canRedo()) {
            return;
        }
        Command command = commands.get(cursor++);
        command.apply();
        fire();
    }

    public void clear() {
        commands.clear();
        cursor = 0;
        fire();
    }

    private void fire() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }
}
