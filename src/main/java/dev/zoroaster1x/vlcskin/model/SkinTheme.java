package dev.zoroaster1x.vlcskin.model;

import dev.zoroaster1x.vlcskin.model.resource.Resource;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * The root of a skin: theme attributes, ThemeInfo, resources and windows.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public final class SkinTheme extends SkinNode {

    public static final String SUPPORTED_VERSION = "2.0";
    public static final int DEFAULT_MAGNET = 15;
    public static final int DEFAULT_ALPHA = 255;

    private String version = SUPPORTED_VERSION;
    private String tooltipfont = "defaultfont";
    private int magnet = DEFAULT_MAGNET;
    private int alpha = DEFAULT_ALPHA;
    private int movealpha = DEFAULT_ALPHA;
    private ThemeInfo themeInfo = new ThemeInfo();
    private final List<Resource> resources = new LinkedList<>();
    private final List<SkinWindow> windows = new LinkedList<>();
    private final List<IncludeFile> includes = new LinkedList<>();
    /**
     * Unknown top level XML kept verbatim so a save never destroys it.
     */
    private final List<String> unknownChildren = new LinkedList<>();
    /**
     * Absolute path of the file this theme was loaded from, when it has one.
     */
    private String sourcePath;
}
