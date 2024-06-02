package Algorithm;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BoyerMoore implements SearchAlgorithm {
    private String text = "";
    private String patt = "";

    private Map<Character, Integer> badCharDict;  // Bad character rule
    private int[] goodSuffixTable;                // Good suffix rule

    private int i = 0;  // i is the current index from text where comparison begins
    private int j = 0;  // index of character from pattern

    private int pattOffset = 0;
    private MatchInfo lastMatch = null;

    @Override
    public boolean ready() {
        return !(text.isEmpty() || patt.isEmpty()) && state() == State.IN_PROGRESS;
    }

    @Override
    public State state() {
        if ((text.isEmpty() || patt.isEmpty())
                || patt.length() > text.length()
                || i >= text.length())
            return State.NO_MATCH;
        if (lastMatch.match() && lastMatch.pattIndex() == 0) return State.MATCH_FOUND;
        return State.IN_PROGRESS;
    }

    @Override
    public @NotNull String text() {
        return text;
    }

    @Override
    public @NotNull String patt() {
        return patt;
    }

    @Override
    public void setText(String s) {
        text = s;
        restart();
    }

    @Override
    public void setPatt(String s) {
        patt = s;
        badCharDict = badChar(patt);
        goodSuffixTable = goodSuffix(patt);
        restart();
    }

    @Override
    public @NotNull MatchInfo step() {
        MatchInfo match = new MatchInfo(i, j, false);

        if (i < text.length()) {
            // move left through pattern step by step matching characters
            if (patt.charAt(j) == text.charAt(i)) {
                match = new MatchInfo(i, j, true);
                i--;
                j--;
            } else {
                // at position of mismatch, make the biggest jump based on the two rules
                i += Math.max(goodSuffixTable[patt.length() - j - 1],
                        badCharDict.getOrDefault(text.charAt(i), patt.length()));
                // for a bad character that isn't found in the pattern: jump the whole pattern length

                j = patt.length() - 1;
            }
        }
        lastMatch = match;
        return match;
    }

    @Override
    public int pattOffset() {
        if (state() == State.MATCH_FOUND) return lastMatch.textIndex();

        return lastMatch.textIndex() - lastMatch.pattIndex();
    }

    private void restart() {
        pattOffset = 0;
        lastMatch = new MatchInfo(0, 0, false);

        if (patt.isEmpty()) return;
        i = patt.length() - 1;
        j = patt.length() - 1;
    }

    public Map<Character, Integer> badCharDict() {
        return Collections.unmodifiableMap(badCharDict);
    }

    public int[] goodSuffixTable() {
        return goodSuffixTable.clone();
    }

    /**
     * Generates a map representing the jump lengths according to the bad character rule
     * @param s string to generate the bad character dictionary for
     * @return map from character to jump length
     */
    public static Map<Character, Integer> badChar(String s) {
        return new HashMap<>() {{
            for (int i = 0; i < s.length(); i ++)
                // jump so that a bad match (with a character that's in the pattern)
                // will line up the mismatch with that character in the pattern
                put(s.charAt(i), Math.max(1, s.length() - i - 1));
        }};
    }

    /**
     * Generates a table representing the jump lengths according to the good suffix rule
     * @param s
     * @return
     */
    public static int[] goodSuffix(String s) {
        int[] offsetTable = new int[s.length()];
        int last = s.length();
        for (int i = s.length(); i > 0; --i) {
            if (isPrefix(s, i)) {
                last = i;
            }
            offsetTable[s.length() - i] = last - i + s.length();
        }
        for (int i = 0; i < s.length() - 1; ++i) {
            int suffixLength = suffixLength(s, i);
            offsetTable[suffixLength] = s.length() - 1 - i + suffixLength;
        }
        return offsetTable;
    }

    /**
     * @param s
     * @param p
     * @return whether the suffix of the pattern starting from p is a prefix of the pattern
     */
    private static boolean isPrefix(String s, int p) {
        for (int i = p, j = 0; i < s.length(); ++i, ++j) {
            if (s.charAt(i) != s.charAt(j)) return false;
        }
        return true;
    }

    /**
     * @param s
     * @param p
     * @return length of suffix that ends at p
     */
    private static int suffixLength(String s, int p) {
        int len = 0;
        // check from right to left, matching chars
        for (int i = p, j = s.length() - 1; i >= 0 && s.charAt(i) == s.charAt(j); --i, --j) len ++;
        return len;
    }
}
