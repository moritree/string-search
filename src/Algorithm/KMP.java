package Algorithm;

import org.jetbrains.annotations.NotNull;

/**
 * Knuth-Morris-Pratt string search algorithm.
 */
public class KMP implements SearchAlgorithm {
    private String text = "";
    private String patt = "";
    private int[] match;

    private int k = 0;  // start of current match in text
    private int i = 0;  // position of current character in patt
    private int pattOffset = 0;

    @Override
    public boolean ready() {
        return !(text.isEmpty() || patt.isEmpty()) && state() == State.IN_PROGRESS;
    }

    @Override
    public State state() {
        if ((text.isEmpty() || patt.isEmpty())
                || patt.length() > text.length()
                || k + patt.length() > text.length())
            return State.NO_MATCH;
        if (i == patt.length() && patt.charAt(i-1) == text.charAt(k+i - 1)) return State.MATCH_FOUND;
        return State.IN_PROGRESS;
    }

    @Override
    public @NotNull String text() {
        return text;
    }

    @Override
    public void setText(String s) {
        text = s;
        restart();
    }

    @Override
    public @NotNull String patt() {
        return patt;
    }

    @Override
    public void setPatt(String s) {
        patt = s;
        restart();
    }

    public void restart() {
        k = 0;
        i = 0;
        pattOffset = 0;

        if (!ready()) return;
        match = KMP.partialMatchTable(patt);
    }

    @Override
    public int pattOffset() {
        return pattOffset;
    }

    @Override
    public @NotNull MatchInfo step() {
        pattOffset = k;
        MatchInfo ret = new MatchInfo(k + i, i, false);
        if (k + i < text.length()) {
            // match
            if (charMatch()) {
                ret = new MatchInfo(k + i, i, true);
                i ++;
            }
            // mismatch, no self overlap
            else if (match[i] == -1) {
                k += i + 1;  // move forward
                i = 0;       // start again
            }
            // mismatch with self overlap
            else {
                k += i - match[i];  // match position jumps forward
                i = match[i];       // continue char comparisons from after prefix
            }
        }
        return ret;
    }

    private boolean charMatch() {
        return patt.charAt(i) == text.charAt(k+i);
    }

    /**
     * Calculate the KMP partial match table for the given pattern
     * @param patt pattern to generate the match table for
     * @return indexed KMP partial match table
     */
    public static int[] partialMatchTable(String patt) {
        final int[] match = new int[patt.length() + 1];
        match[0] = -1;

        if (patt.length() == 1) return match;

        int cnd,  // the index in pat of the next character of the current candidate substring
            pos;  // the current position we are computing in match[]

        for(cnd = 0, pos = 1; pos < patt.length(); pos ++, cnd++) {
            if (patt.charAt(pos) == patt.charAt(cnd)) match[pos] = match[cnd];
            else {
                match[pos] = cnd;
                // until we get back down to -1
                while (cnd >= 0 && patt.charAt(pos) != patt.charAt(cnd)) cnd = match[cnd];
            }
        }
        match[pos] = cnd;

        return match;
    }
}
