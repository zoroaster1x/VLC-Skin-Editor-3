package dev.zoroaster1x.vlcskin.model;

/**
 * Another skin file pulled into a theme. Include elements carry no id.
 */
public final class IncludeFile extends SkinNode {

    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String elementName() {
        return "Include";
    }
}
