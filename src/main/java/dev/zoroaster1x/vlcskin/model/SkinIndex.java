package dev.zoroaster1x.vlcskin.model;

import dev.zoroaster1x.vlcskin.model.item.Item;
import dev.zoroaster1x.vlcskin.model.resource.BitmapResource;
import dev.zoroaster1x.vlcskin.model.resource.FontResource;
import dev.zoroaster1x.vlcskin.model.resource.IniFileResource;
import dev.zoroaster1x.vlcskin.model.resource.Resource;
import dev.zoroaster1x.vlcskin.model.resource.SubBitmap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Lookup and traversal helpers over a theme. No mutation.
 */
public final class SkinIndex {

    /**
     * Either a whole bitmap or one of its sub bitmaps.
     */
    public record ImageRef(BitmapResource bitmap, SubBitmap sub) {

        public String id() {
            return sub != null ? sub.getId() : bitmap.getId();
        }

        public int x() {
            return sub != null ? sub.getX() : 0;
        }

        public int y() {
            return sub != null ? sub.getY() : 0;
        }

        public boolean isSub() {
            return sub != null;
        }
    }

    private final SkinTheme theme;
    private Map<String, String> constants;

    public SkinIndex(SkinTheme theme) {
        this.theme = theme;
    }

    public SkinTheme theme() {
        return theme;
    }

    public Resource findResource(String id) {
        if (id == null) {
            return null;
        }
        for (String candidate : candidates(id)) {
            Resource exact = findResourceExact(candidate);
            if (exact != null) {
                return exact;
            }
        }
        return null;
    }

    private Resource findResourceExact(String id) {
        for (Resource resource : theme.getResources()) {
            if (id.equals(resource.getId())) {
                return resource;
            }
        }
        return null;
    }

    /**
     * VLC accepts "id1;id2;id3" references and uses the first resource that
     * exists, which later Winamp2 derived themes rely on.
     */
    public ImageRef findImage(String id) {
        if (id == null) {
            return null;
        }
        for (String candidate : candidates(id)) {
            ImageRef exact = findImageExact(candidate);
            if (exact != null) {
                return exact;
            }
        }
        return null;
    }

    private ImageRef findImageExact(String id) {
        for (Resource resource : theme.getResources()) {
            if (resource instanceof BitmapResource bitmap) {
                if (id.equals(bitmap.getId())) {
                    return new ImageRef(bitmap, null);
                }
                for (SubBitmap sub : bitmap.getSubBitmaps()) {
                    if (id.equals(sub.getId())) {
                        return new ImageRef(bitmap, sub);
                    }
                }
            }
        }
        return null;
    }

    private static List<String> candidates(String id) {
        if (id.indexOf(';') < 0) {
            return List.of(id);
        }
        List<String> out = new ArrayList<>(3);
        for (String part : id.split(";")) {
            String trimmed = part.strip();
            if (!trimmed.isEmpty()) {
                out.add(trimmed);
            }
        }
        return out.isEmpty() ? List.of(id) : out;
    }

    /**
     * The value of a constant registered by an {@code IniFile} resource, keyed
     * as VLC keys it: the ini id, the section and the key, lowercased and dot
     * separated. Colors and other getConstant lookups resolve through here.
     */
    public String constant(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return constants().get(name.toLowerCase(Locale.ROOT));
    }

    private Map<String, String> constants() {
        Map<String, String> cached = constants;
        if (cached != null) {
            return cached;
        }
        Map<String, String> map = new LinkedHashMap<>();
        Path folder = theme.getSourcePath() == null || theme.getSourcePath().isBlank()
                ? null : Path.of(theme.getSourcePath()).getParent();
        if (folder != null) {
            for (Resource resource : theme.getResources()) {
                if (resource instanceof IniFileResource ini && ini.getFile() != null) {
                    Path file = folder.resolve(ini.getFile().replace('\\', '/')).normalize();
                    try {
                        parseIni(ini.getId(), file, map);
                    } catch (IOException ex) {
                        // A missing or unreadable ini leaves its constants absent.
                    }
                }
            }
        }
        constants = Map.copyOf(map);
        return constants;
    }

    private static void parseIni(String iniId, Path file, Map<String, String> map) throws IOException {
        if (!Files.isRegularFile(file)) {
            return;
        }
        String section = "";
        for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith(";") || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1);
                continue;
            }
            int equals = line.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String key = line.substring(0, equals).strip();
            String value = line.substring(equals + 1).strip();
            map.put((iniId + "." + section + "." + key).toLowerCase(Locale.ROOT), value);
        }
    }

    public FontResource findFont(String id) {
        Resource resource = findResource(id);
        return resource instanceof FontResource font ? font : null;
    }

    public SkinWindow findWindow(String id) {
        if (id == null) {
            return null;
        }
        for (SkinWindow window : theme.getWindows()) {
            if (id.equals(window.getId())) {
                return window;
            }
        }
        return null;
    }

    public SkinLayout findLayout(SkinWindow window, String id) {
        if (window == null || id == null) {
            return null;
        }
        for (SkinLayout layout : window.getLayouts()) {
            if (id.equals(layout.getId())) {
                return layout;
            }
        }
        return null;
    }

    public SkinLayout findAnyLayout(String id) {
        for (SkinWindow window : theme.getWindows()) {
            SkinLayout layout = findLayout(window, id);
            if (layout != null) {
                return layout;
            }
        }
        return null;
    }

    public SkinWindow windowOf(SkinLayout layout) {
        for (SkinWindow window : theme.getWindows()) {
            if (window.getLayouts().contains(layout)) {
                return window;
            }
        }
        return null;
    }

    /**
     * The layout that ultimately contains an item, or null.
     */
    public SkinLayout layoutOf(String id) {
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                if (containsItem(layout.getItems(), id)) {
                    return layout;
                }
            }
        }
        return null;
    }

    private boolean containsItem(java.util.List<Item> items, String id) {
        for (Item item : items) {
            if (id.equals(item.getId())) {
                return true;
            }
            if (containsItem(item.children(), id)) {
                return true;
            }
        }
        return false;
    }

    public Item findItem(String id) {
        if (id == null) {
            return null;
        }
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                Item found = findItem(layout, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private Item findItem(SkinLayout layout, String id) {
        for (Item item : layout.getItems()) {
            Item found = findItem(item, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private Item findItem(Item root, String id) {
        if (id.equals(root.getId())) {
            return root;
        }
        for (Item child : root.children()) {
            Item found = findItem(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * The mutable list that directly contains the item, or null at layout root.
     */
    public List<Item> parentListOf(String id) {
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                List<Item> found = containing(layout.getItems(), id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private List<Item> containing(List<Item> items, String id) {
        for (Item item : items) {
            if (id.equals(item.getId())) {
                return items;
            }
            List<Item> child = containing(item.children(), id);
            if (child != null) {
                return child;
            }
        }
        return null;
    }

    public Item parentItemOf(String id) {
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                Item found = parentIn(layout.getItems(), id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private Item parentIn(List<Item> items, String id) {
        for (Item item : items) {
            for (Item child : item.children()) {
                if (id.equals(child.getId())) {
                    return item;
                }
            }
            Item found = parentIn(item.children(), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public void forEachItem(Consumer<Item> consumer) {
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                for (Item item : layout.getItems()) {
                    walk(item, consumer);
                }
            }
        }
    }

    private void walk(Item item, Consumer<Item> consumer) {
        consumer.accept(item);
        for (Item child : item.children()) {
            walk(child, consumer);
        }
    }

    public List<Item> allItems() {
        List<Item> result = new ArrayList<>();
        forEachItem(result::add);
        return result;
    }

    public boolean isResourceUsed(String id) {
        for (SkinWindow window : theme.getWindows()) {
            for (SkinLayout layout : window.getLayouts()) {
                for (Item item : layout.getItems()) {
                    if (item.usesResource(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Set<String> allIds() {
        Set<String> ids = new HashSet<>();
        for (Resource resource : theme.getResources()) {
            ids.add(resource.getId());
            if (resource instanceof BitmapResource bitmap) {
                for (SubBitmap sub : bitmap.getSubBitmaps()) {
                    ids.add(sub.getId());
                }
            }
        }
        for (SkinWindow window : theme.getWindows()) {
            ids.add(window.getId());
            for (SkinLayout layout : window.getLayouts()) {
                ids.add(layout.getId());
            }
        }
        forEachItem(item -> ids.add(item.getId()));
        ids.remove(null);
        return ids;
    }

    public boolean idExists(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        if (findResource(id) != null) {
            return true;
        }
        for (SkinWindow window : theme.getWindows()) {
            if (id.equals(window.getId())) {
                return true;
            }
            for (SkinLayout layout : window.getLayouts()) {
                if (id.equals(layout.getId())) {
                    return true;
                }
            }
        }
        return allItems().stream().anyMatch(item -> id.equals(item.getId()));
    }

    /**
     * Produces a unique default id such as "Slider #4".
     */
    public String uniqueUnnamed(String typeName) {
        int counter = 1;
        String candidate;
        do {
            candidate = typeName + " #" + counter;
            counter++;
        } while (idExists(candidate));
        return candidate;
    }

    /**
     * Produces a unique copy id from a pattern where %oldid% is the source id.
     */
    public String uniqueCopy(String pattern, String oldId) {
        String base = pattern == null || pattern.isEmpty() ? oldId + "_copy" : pattern;
        base = base.replace("%oldid%", oldId).replace("%", "").replace("\"", "");
        if (base.isBlank()) {
            base = oldId + "_copy";
        }
        if (!idExists(base)) {
            return base;
        }
        int counter = 2;
        while (idExists(base + "_" + counter)) {
            counter++;
        }
        return base + "_" + counter;
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
