package Algorithm;

import org.jetbrains.annotations.NotNull;

/**
 * A string search algorithm.
 */
public interface SearchAlgorithm {
    enum State { IN_PROGRESS, MATCH_FOUND, NO_MATCH }

    /**
     * @return if this algorithm is ready to run
     */
    boolean ready();

    /**
     * @return -1 if there is no match, 0 if in progress, 1 if match found
     */
    State state();

    @NotNull String text();
    @NotNull String patt();

    void setText(String s);
    void setPatt(String s);

    /**
     * @return number of spaces the pattern is offset from the left
     */
    int pattOffset();

    /**
     * Perform the next step for this search algorithm
     * @return <code>MatchInfo</code> object supplying information about the character match state
     */
    @NotNull MatchInfo step();

    /**
     * Object that encapsulates information about the state of a character match
     * @param textIndex the index of the source text at which the check was made
     * @param pattIndex the index of the pattern at which the check was made
     * @param match true if matched, false if mismatched
     */
    record MatchInfo(int textIndex, int pattIndex, boolean match) {  }
}
