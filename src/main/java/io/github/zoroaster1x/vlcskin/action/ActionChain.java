package io.github.zoroaster1x.vlcskin.action;

import java.util.ArrayList;
import java.util.List;

/**
 * The semicolon separated action attribute of an item. Parsing and formatting
 * are lossless: unknown codes stay in the chain.
 */
public final class ActionChain {

    private final List<String> codes = new ArrayList<>();

    public static ActionChain parse(String attribute) {
        ActionChain chain = new ActionChain();
        if (attribute == null || attribute.isBlank()) {
            return chain;
        }
        for (String part : attribute.split(";")) {
            String code = part.trim();
            if (code.isEmpty() || "none".equals(code)) {
                continue;
            }
            chain.codes.add(code);
        }
        return chain;
    }

    public static String format(ActionChain chain) {
        return chain.isEmpty() ? "none" : String.join(";", chain.codes);
    }

    public static String format(List<String> codes) {
        List<String> clean = codes.stream().map(String::trim)
                .filter(code -> !code.isEmpty() && !"none".equals(code)).toList();
        return clean.isEmpty() ? "none" : String.join(";", clean);
    }

    public List<String> codes() {
        return codes;
    }

    public boolean isEmpty() {
        return codes.isEmpty();
    }

    public void add(String code) {
        codes.add(code.trim());
    }

    public void remove(int index) {
        if (index >= 0 && index < codes.size()) {
            codes.remove(index);
        }
    }

    public String toAttribute() {
        return format(this);
    }
}
