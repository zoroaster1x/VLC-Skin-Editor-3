package dev.zoroaster1x.vlcskin.edit;

import dev.zoroaster1x.vlcskin.format.ParseIssue;
import dev.zoroaster1x.vlcskin.format.SkinParser;
import dev.zoroaster1x.vlcskin.format.SkinWriter;
import dev.zoroaster1x.vlcskin.model.SkinIndex;
import dev.zoroaster1x.vlcskin.model.SkinLayout;
import dev.zoroaster1x.vlcskin.model.SkinTheme;
import dev.zoroaster1x.vlcskin.model.SkinWindow;
import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.render.ImageStore;
import dev.zoroaster1x.vlcskin.render.PreviewVariables;
import dev.zoroaster1x.vlcskin.render.RenderOptions;
import dev.zoroaster1x.vlcskin.render.SkinRenderer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * One open skin plus everything the editor needs around it: undo history,
 * selection, preview state and the renderer. All mutations should go through
 * {@link #apply(Command)} so undo and the dirty flag stay honest.
 */
public final class EditorSession {

    private SkinTheme theme;
    private Path file;
    private volatile boolean dirty;
    private List<ParseIssue> issues = List.of();
    private SkinIndex index;
    private ImageStore images;
    private SkinRenderer renderer;
    private final CommandStack history = new CommandStack();
    private final SelectionState selection = new SelectionState();
    private final PreviewVariables variables = new PreviewVariables();
    private final List<Runnable> listeners = new ArrayList<>();
    private volatile java.util.function.Consumer<Runnable> dispatcher = Runnable::run;
    private final java.util.concurrent.atomic.AtomicLong revision =
            new java.util.concurrent.atomic.AtomicLong();

    /**
     * One recorded change outside the tool-call machinery, for the notice an
     * AI client sees when the document moved under it.
     */
    public record Change(long revision, String origin, String description, long timeMillis) {
    }

    private final java.util.concurrent.atomic.AtomicLong changeRevision =
            new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.ConcurrentLinkedDeque<Change> journal =
            new java.util.concurrent.ConcurrentLinkedDeque<>();
    private volatile String changeOrigin = "user";

    /**
     * Who is making the changes. The MCP dispatcher sets a tool name while a
     * tool runs; anything else records as {@code user}.
     */
    public void setChangeOrigin(String origin) {
        this.changeOrigin = origin == null || origin.isBlank() ? "user" : origin;
    }

    public String changeOrigin() {
        return changeOrigin;
    }

    private void recordChange(String description) {
        long revision = changeRevision.incrementAndGet();
        journal.add(new Change(revision, changeOrigin,
                description == null || description.isBlank() ? "edit" : description,
                System.currentTimeMillis()));
        while (journal.size() > 200) {
            journal.pollFirst();
        }
    }

    /**
     * Changes recorded after the given revision, oldest first.
     */
    public List<Change> changesSince(long revision) {
        return journal.stream().filter(change -> change.revision() > revision).toList();
    }

    public long changeRevision() {
        return changeRevision.get();
    }

    private EditorSession(SkinTheme theme, Path file) {
        rebuild(theme, file);
        history.addListener(this::fireChanged);
    }

    /**
     * Bumped on every document change; the canvas cache keys on it.
     */
    public long revision() {
        return revision.get();
    }

    public static EditorSession empty() {
        return new EditorSession(new SkinTheme(), null);
    }

    public static EditorSession open(Path file) throws IOException {
        SkinParser.Result result = SkinParser.parse(file);
        EditorSession session = new EditorSession(result.theme(), file.toAbsolutePath());
        session.issues = result.issues();
        if (!result.theme().getWindows().isEmpty()) {
            var window = result.theme().getWindows().get(0);
            if (!window.getLayouts().isEmpty()) {
                session.selection.selectLayout(window.getId(), window.getLayouts().get(0).getId());
            } else {
                session.selection.selectWindow(window.getId());
            }
        }
        session.dirty = false;
        return session;
    }

    public static EditorSession of(SkinTheme theme, Path file) {
        EditorSession session = new EditorSession(theme, file);
        session.issues = List.of();
        session.dirty = false;
        session.selectFirstLayout();
        return session;
    }

    /**
     * Selects the first layout of the first window, or the window itself when
     * it has no layouts. Public because services replace the document in place.
     */
    public void selectFirstLayout() {
        for (SkinWindow window : theme.getWindows()) {
            if (!window.getLayouts().isEmpty()) {
                selection.selectLayout(window.getId(), window.getLayouts().get(0).getId());
                return;
            }
            selection.selectWindow(window.getId());
            return;
        }
        selection.selectWindow(null);
    }

    private void rebuild(SkinTheme theme, Path file) {
        this.theme = theme;
        this.file = file;
        this.index = new SkinIndex(theme);
        Path folder = file == null ? Path.of(".") : file.getParent();
        this.images = new ImageStore(folder == null ? Path.of(".") : folder);
        this.renderer = new SkinRenderer(index, images);
    }

    public SkinTheme theme() {
        return theme;
    }

    public SkinIndex index() {
        return index;
    }

    public ImageStore images() {
        return images;
    }

    public SkinRenderer renderer() {
        return renderer;
    }

    public SelectionState selection() {
        return selection;
    }

    public PreviewVariables variables() {
        return variables;
    }

    public CommandStack history() {
        return history;
    }

    public Path file() {
        return file;
    }

    public List<ParseIssue> issues() {
        return issues;
    }

    public void setIssues(List<ParseIssue> issues) {
        this.issues = issues;
        fireChanged();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void addListener(Runnable listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void fireChanged() {
        revision.incrementAndGet();
        List<Runnable> snapshot;
        synchronized (listeners) {
            snapshot = List.copyOf(listeners);
        }
        dispatcher.accept(() -> snapshot.forEach(Runnable::run));
    }

    /**
     * How change notifications reach listeners. The desktop app marshals them
     * onto the event thread; the default runs them on the calling thread.
     */
    public void setDispatcher(java.util.function.Consumer<Runnable> dispatcher) {
        this.dispatcher = dispatcher == null ? Runnable::run : dispatcher;
    }

    /**
     * Runs a command, marks the document dirty and notifies.
     */
    public void apply(Command command) {
        if (command instanceof ValueCommand value && value.isEmpty()) {
            return;
        }
        history.run(command);
        dirty = true;
        fireChanged();
    }

    /**
     * Marks the document dirty after a direct mutation outside a command.
     */
    public void touch() {
        dirty = true;
        fireChanged();
    }

    public void undo() {
        history.undo();
        dirty = true;
    }

    public void redo() {
        history.redo();
        dirty = true;
    }

    /**
     * One pixel nudges of the same item merge into a single undo step, the way
     * the original editor coalesced repeated arrow key presses.
     */
    public void nudge(Item item, int dx, int dy) {
        Object previous = history.current();
        if (previous instanceof NudgeCommand nudge && nudge.target() == item) {
            nudge.extend(dx, dy, item);
            return;
        }
        NudgeCommand command = new NudgeCommand(item, dx, dy);
        apply(command);
    }

    /**
     * An item move that can grow while the arrow keys are held.
     */
    private static final class NudgeCommand implements Command {

        private final Item item;
        private int dx;
        private int dy;

        NudgeCommand(Item item, int dx, int dy) {
            this.item = item;
            this.dx = dx;
            this.dy = dy;
        }

        Item target() {
            return item;
        }

        void extend(int extraX, int extraY, Item expected) {
            if (expected != item) {
                return;
            }
            dx += extraX;
            dy += extraY;
            item.setX(item.getX() + extraX);
            item.setY(item.getY() + extraY);
        }

        @Override
        public void apply() {
            item.setX(item.getX() + dx);
            item.setY(item.getY() + dy);
        }

        @Override
        public void undo() {
            item.setX(item.getX() - dx);
            item.setY(item.getY() - dy);
        }

        @Override
        public String description() {
            return "Move " + item.type().displayName();
        }
    }

    public String toXml() {
        return SkinWriter.toXml(theme);
    }

    public void save() throws IOException {
        if (file == null) {
            throw new IOException("No file chosen for this skin");
        }
        saveAs(file);
    }

    public void saveAs(Path target) throws IOException {
        Files.writeString(target, toXml(), StandardCharsets.UTF_8);
        if (!target.equals(file)) {
            rebuild(theme, target.toAbsolutePath());
        }
        dirty = false;
        fireChanged();
    }

    /**
     * Replaces the whole document, for new, open and XML apply.
     */
    public void replace(SkinTheme newTheme, Path newFile, List<ParseIssue> newIssues) {
        rebuild(newTheme, newFile);
        issues = newIssues == null ? List.of() : newIssues;
        selection.selectWindow(null);
        selection.clearItem();
        history.clear();
        dirty = false;
        fireChanged();
    }

    public RenderOptions renderOptions() {
        return RenderOptions.of(variables)
                .withSelection(selection.item(index));
    }

    /**
     * The layout currently selected, or the first layout of the theme.
     */
    public SkinLayout currentLayout() {
        SkinLayout layout = selection.layout(index);
        if (layout != null) {
            return layout;
        }
        for (var window : theme.getWindows()) {
            if (!window.getLayouts().isEmpty()) {
                return window.getLayouts().get(0);
            }
        }
        return null;
    }
}
