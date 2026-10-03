package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.app.Studio;

    /**
     * All side panels, created once and refreshed together on every change.
     */
public final class Panels {

    public final ResourcesPanel resources;
    public final StructurePanel structure;
    public final ItemsPanel items;
    public final CanvasPanel canvas;
    public final InspectorPanel inspector;
    public final VariablesPanel variables;
    public final ProblemsPanel problems;
    public final XmlPanel xml;
    public final AiPanel ai;

    private boolean refreshing;

    public Panels(Studio studio) {
        java.util.function.BooleanSupplier guard = () -> refreshing;
        resources = new ResourcesPanel(studio, guard);
        structure = new StructurePanel(studio, guard);
        items = new ItemsPanel(studio, guard);
        canvas = new CanvasPanel(studio);
        inspector = new InspectorPanel(studio);
        variables = new VariablesPanel(studio);
        problems = new ProblemsPanel(studio);
        xml = new XmlPanel(studio);
        ai = new AiPanel(studio);
        studio.session().addListener(this::refresh);
        refresh();
    }

    /**
     * True while panels are being rebuilt; programmatic selection must not echo back.
     */
    public boolean isRefreshing() {
        return refreshing;
    }

    /**
     * Refreshes every panel from the session. Cheap and idempotent.
     */
    public void refresh() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            resources.refresh();
            structure.refresh();
            items.refresh();
            canvas.refresh();
            inspector.refresh();
            variables.refresh();
            problems.validate();
            xml.refresh();
        } finally {
            refreshing = false;
        }
    }
}
