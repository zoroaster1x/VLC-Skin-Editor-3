package dev.zoroaster1x.vlcskin.format;

import java.util.ArrayList;
import java.util.List;

/**
 * Three-way line merge for canonical skin XML, so an AI session and the
 * desktop window can edit the same document without either side having to
 * reload blindly or overwrite the other.
 *
 * <p>The inputs are the merge base (what both sides last agreed on), our
 * current text and the file text. Regions only one side touched are taken from
 * that side; regions both sides changed to the same text are taken once;
 * regions both sides changed differently keep ours and are reported as
 * conflicts, with the other side's text, so a caller can show them and decide.
 *
 * <p>Skin XML writes as one element per line with stable indentation, so
 * line-oriented merging keeps edits localized. The merge is a diff3 walk over
 * LCS matches; no external tools.
 */
public final class XmlMerger {

    /**
     * The merge outcome: the combined text plus one description per conflict.
     */
    public record Merge(String text, List<String> conflicts) {

        public boolean clean() {
            return conflicts.isEmpty();
        }
    }

    private XmlMerger() {
    }

    /**
     * Merges ours and theirs over the base.
     */
    public static Merge merge(String base, String ours, String theirs) {
        List<String> baseLines = lines(base);
        List<String> ourLines = lines(ours);
        List<String> theirLines = lines(theirs);
        List<Edit> ourEdits = edits(baseLines, ourLines);
        List<Edit> theirEdits = edits(baseLines, theirLines);

        List<String> result = new ArrayList<>();
        List<String> conflicts = new ArrayList<>();
        int position = 0;
        while (position <= baseLines.size()) {
            List<String> ourInsert = insertionsAt(ourEdits, position);
            List<String> theirInsert = insertionsAt(theirEdits, position);
            if (!ourInsert.isEmpty() || !theirInsert.isEmpty()) {
                if (ourInsert.equals(theirInsert)) {
                    result.addAll(ourInsert);
                } else if (ourInsert.isEmpty()) {
                    result.addAll(theirInsert);
                } else if (theirInsert.isEmpty()) {
                    result.addAll(ourInsert);
                } else {
                    result.addAll(ourInsert);
                    conflicts.add(formatConflict(ourInsert, theirInsert));
                }
            }
            if (position == baseLines.size()) {
                break;
            }
            Edit ourEdit = covering(ourEdits, position);
            Edit theirEdit = covering(theirEdits, position);
            if (ourEdit == null && theirEdit == null) {
                result.add(baseLines.get(position));
                position++;
                continue;
            }
            int end = position;
            if (ourEdit != null) {
                end = Math.max(end, ourEdit.end());
            }
            if (theirEdit != null) {
                end = Math.max(end, theirEdit.end());
            }
            boolean grew = true;
            while (grew) {
                grew = false;
                for (Edit edit : ourEdits) {
                    if (edit.end() > edit.start() && edit.start() < end && edit.end() > end) {
                        end = edit.end();
                        grew = true;
                    }
                }
                for (Edit edit : theirEdits) {
                    if (edit.end() > edit.start() && edit.start() < end && edit.end() > end) {
                        end = edit.end();
                        grew = true;
                    }
                }
            }
            List<String> ourText = ourEdit == null ? List.of() : ourEdit.replacement();
            List<String> theirText = theirEdit == null ? List.of() : theirEdit.replacement();
            if (ourEdit != null && theirEdit != null) {
                if (ourText.equals(theirText)) {
                    result.addAll(ourText);
                } else {
                    result.addAll(ourText);
                    conflicts.add(formatConflict(ourText, theirText));
                }
            } else if (ourEdit != null) {
                result.addAll(ourText);
            } else {
                result.addAll(theirText);
            }
            position = end;
        }
        return new Merge(String.join("\n", result), conflicts);
    }

    /**
     * One change in a two-way diff: replace base lines [start, end) with text.
     */
    private record Edit(int start, int end, List<String> replacement) {
    }

    private static List<Edit> edits(List<String> base, List<String> other) {
        int[] match = matchPositions(base, other);
        List<Edit> edits = new ArrayList<>();
        int index = 0;
        int otherIndex = 0;
        while (index < base.size()) {
            if (match[index] == -1) {
                int start = index;
                while (index < base.size() && match[index] == -1) {
                    index++;
                }
                int next = index < base.size() ? match[index] : other.size();
                edits.add(new Edit(start, index,
                        new ArrayList<>(other.subList(otherIndex, next))));
                otherIndex = next;
            } else {
                if (match[index] > otherIndex) {
                    edits.add(new Edit(index, index,
                            new ArrayList<>(other.subList(otherIndex, match[index]))));
                }
                otherIndex = match[index] + 1;
                index++;
            }
        }
        if (otherIndex < other.size()) {
            edits.add(new Edit(base.size(), base.size(),
                    new ArrayList<>(other.subList(otherIndex, other.size()))));
        }
        return edits;
    }

    private static List<String> insertionsAt(List<Edit> edits, int position) {
        for (Edit edit : edits) {
            if (edit.start() == position && edit.end() == position) {
                return edit.replacement();
            }
        }
        return List.of();
    }

    private static Edit covering(List<Edit> edits, int position) {
        for (Edit edit : edits) {
            if (edit.start() <= position && position < edit.end()) {
                return edit;
            }
        }
        return null;
    }

    private static String formatConflict(List<String> ours, List<String> theirs) {
        return "ours: " + String.join(" | ", ours)
                + "\ntheirs: " + String.join(" | ", theirs);
    }

    /**
     * Difference summary between two canonical documents, for disk_diff.
     */
    public static List<String> diffLines(String ours, String theirs) {
        List<String> ourLines = lines(ours);
        List<String> theirLines = lines(theirs);
        int[] theirMatch = matchPositions(ourLines, theirLines);
        List<String> differences = new ArrayList<>();
        int ourIndex = 0;
        for (int index = 0; index < ourLines.size(); index++) {
            int match = theirMatch[index];
            if (match < ourIndex) {
                differences.add("- " + ourLines.get(index));
                continue;
            }
            for (int extra = ourIndex; extra < match; extra++) {
                differences.add("+ " + theirLines.get(extra));
            }
            ourIndex = match + 1;
        }
        for (int extra = ourIndex; extra < theirLines.size(); extra++) {
            differences.add("+ " + theirLines.get(extra));
        }
        return differences;
    }

    private static List<String> lines(String text) {
        return new ArrayList<>(java.util.Arrays.asList(
                (text == null ? "" : text).split("\n", -1)));
    }

    /**
     * For every line of a, the index of the matching line in b under an LCS,
     * or -1 when the line is not kept.
     */
    static int[] matchPositions(List<String> a, List<String> b) {
        int[][] lengths = new int[a.size() + 1][b.size() + 1];
        for (int i = a.size() - 1; i >= 0; i--) {
            for (int j = b.size() - 1; j >= 0; j--) {
                lengths[i][j] = a.get(i).equals(b.get(j))
                        ? lengths[i + 1][j + 1] + 1
                        : Math.max(lengths[i + 1][j], lengths[i][j + 1]);
            }
        }
        int[] matches = new int[a.size()];
        java.util.Arrays.fill(matches, -1);
        int i = 0;
        int j = 0;
        while (i < a.size() && j < b.size()) {
            if (a.get(i).equals(b.get(j))) {
                matches[i] = j;
                i++;
                j++;
            } else if (lengths[i + 1][j] >= lengths[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        return matches;
    }
}
