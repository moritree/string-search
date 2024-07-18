package UI;

import Algorithm.BoyerMoore;
import Algorithm.KMP;
import Algorithm.SearchAlgorithm;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

sealed interface SearchDisplay permits KMPDisplay, BoyerMooreDisplay {
    Map<Field, Integer> fieldRow = Map.of(Field.TEXT, 0, Field.PATT, 1);
    enum Field{ TEXT, PATT }

    /**
     * @return the string search algorithm associated with this display
     */
    SearchAlgorithm alg();

    /**
     * @return display JPanel
     */
    JPanel panel();

    /**
     * @return the CharBox components corresponding to each field
     */
    Map<Field, List<? extends CharBox>> fieldComponents();

    /**
     * @return spacer CharBoxes for offsetting the pattern text
     */
    List<SpacerBox> spacers();

    /**
     * @return whether this display's associated algorithm is ready to run
     */
    default boolean ready() {
        return alg().ready();
    }

    /**
     * Update one of the display fields with new text
     * @param str text to replace original value with
     * @param field which field to write into
     */
    default void updateString(String str, Field field) {
        switch (field) {
            case TEXT -> alg().setText(str);
            case PATT -> alg().setPatt(str);
        }
        draw();
    }

    /**
     * Draw text and patt fields, with default coloring
     * <br>The interface default method applies to all types of <code>SearchDisplay</code>
     * and is called from concrete record <code>draw()</code> methods after any
     * implementation specific details are done.
     */
    default void draw() {
        // setup layout constraints
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(1, 1, 1, 1);
        c.fill = GridBagConstraints.BOTH;

        // add components for each field
        for (Field f : Field.values()) {
            c.gridy = fieldRow.get(f);  // which row is this field on?
            c.gridx = 0;                // each row starts from first column

            // For the pattern, offset as required
            if (f == Field.PATT) {
                for (SpacerBox spacer : spacers()) panel().remove(spacer.panel());
                spacers().clear();
                for (int i = 0; i < alg().pattOffset(); i++) {
                    spacers().add(new SpacerBox());
                    panel().add(spacers().getLast().panel(), c);
                    c.gridx++;
                }
            }

            // Add all letter components to panel
            for (CharBox letter : fieldComponents().get(f)) {
                panel().add(letter.panel(), c);
                c.gridx++;
            }
        }

        panel().revalidate();
        panel().repaint();
    }

    /**
     * Colour a character based on its match status at this point in the progress of the algorithm.
     * @param match information about the state of a character match
     * @param fieldComponents the CharBox components corresponding to each display text field
     * @param panel panel to repaint
     */
    static void colourMatchedChar(SearchAlgorithm.MatchInfo match,
                                  Map<Field, List<? extends CharBox>> fieldComponents, JPanel panel) {
        // colour depends on whether this char was a match or mismatch
        Color col = match.match() ? CharBox.MATCH_COL() : CharBox.MISMATCH_COL();

        // paint matched char in respective fields
        fieldComponents.get(Field.TEXT).get(match.textIndex()).setColor(col);
        fieldComponents.get(Field.PATT).get(match.pattIndex()).setColor(col);

        panel.repaint();
    }

    /**
     * Take one step in the algorithm, display updated panel
     */
    void step();
}

record KMPDisplay(
            Map<Field, List<? extends CharBox>> fieldComponents, List<SpacerBox> spacers,
            JPanel panel, SearchAlgorithm alg)
        implements SearchDisplay {
    KMPDisplay() {
        this(new HashMap<>(){{
            put(Field.TEXT, new ArrayList<>());
            put(Field.PATT, new ArrayList<>());
        }}, new ArrayList<>(), new JPanel(), new KMP());

        panel.setLayout(new GridBagLayout());
    }

    public void draw() {
        // draw fields
        // Clear all text boxes
        for (Field f : fieldComponents().keySet()) {
            for (CharBox letter : fieldComponents().get(f)) panel().remove(letter.panel());
        }

        // construct text components
        fieldComponents().put(Field.TEXT, CharBox.singleBoxes(alg().text()));

        // construct pattern components
        if (alg().patt().isEmpty()) fieldComponents().put(Field.PATT, new ArrayList<>());
        else fieldComponents().put(Field.PATT, kmpMatchBoxes(alg().patt()));

        SearchDisplay.super.draw();
    }

    public void step() {
        // iterate algorithm, MatchInfo gives us information about the state of the character match
        SearchAlgorithm.MatchInfo match = alg.step();

        draw();

        Color col = CharBox.DEFAULT_COL();
        int startInd = 0;
        int endInd = 0;

        switch (alg.state()) {
            case IN_PROGRESS -> {
                SearchDisplay.colourMatchedChar(match, fieldComponents, panel);
                return;
            }
            case NO_MATCH -> { // no match exists in the text
                col = CharBox.MISMATCH_COL();
                endInd = alg.text().length();
            }
            case MATCH_FOUND -> { // match found!
                col = CharBox.MATCH_COL();
                startInd = match.textIndex() - alg.patt().length() + 1;
                endInd = startInd + alg.patt().length();
            }
        }

        for (CharBox c : Stream.concat(
                fieldComponents.get(Field.PATT).stream(),
                fieldComponents.get(Field.TEXT).subList(startInd, endInd).stream()).toList()) {
            c.setColor(col);
        }

        panel.repaint();  // update colour
    }

    static List<? extends CharBox> kmpMatchBoxes(String s) {
        int[] matchTable = KMP.partialMatchTable(s);
        return new ArrayList<>(){{
            for (int i = 0; i < s.length(); i ++) add(new CharSubscript(s.charAt(i), matchTable[i]));
        }};
    }
}

record BoyerMooreDisplay(
        Map<Field, List<? extends CharBox>> fieldComponents,
        List<SpacerBox> spacers,
        JPanel panel,
        BoyerMoore alg)
        implements SearchDisplay {
    BoyerMooreDisplay() {
        this(new HashMap<>(){{
            put(Field.TEXT, new ArrayList<>());
            put(Field.PATT, new ArrayList<>());
        }}, new ArrayList<>(), new JPanel(), new BoyerMoore());

        panel.setLayout(new GridBagLayout());
    }

    public void draw() {
        // draw fields
        // Clear all text boxes
        for (Field f : fieldComponents().keySet()) {
            for (CharBox letter : fieldComponents().get(f)) panel().remove(letter.panel());
        }

        // construct text components
        if (!alg.text().isEmpty()) {
            if (alg.patt().isEmpty()) {
                // if there is no pattern, so preprocessing can't have been done, just do chars
                fieldComponents().put(Field.TEXT, CharBox.singleBoxes(alg.text()));
            } else {
                fieldComponents().put(Field.TEXT,
                        boyerMooreTextBoxes(alg.text(), alg.badCharDict(), alg.patt().length()));
            }
        }

        // construct pattern components
        if (alg().patt().isEmpty()) fieldComponents().put(Field.PATT, new ArrayList<>());
        else fieldComponents().put(Field.PATT, boyerMoorePattBoxes(alg.patt(), alg.goodSuffixTable()));

        SearchDisplay.super.draw();
    }

    public void step() {
        // iterate algorithm, MatchInfo gives us information about the state of the character match
        SearchAlgorithm.MatchInfo match = alg.step();

        draw();

        Color col = CharBox.DEFAULT_COL();
        int startInd = 0;
        int endInd = 0;

        switch (alg.state()) {
            case IN_PROGRESS -> {
                SearchDisplay.colourMatchedChar(match, fieldComponents, panel);
                return;
            }
            case NO_MATCH -> { // no match exists in the text
                col = CharBox.MISMATCH_COL();
                endInd = alg.text().length();
            }
            case MATCH_FOUND -> { // match found!
                col = CharBox.MATCH_COL();
                startInd = match.textIndex();
                endInd = startInd + alg.patt().length();
            }
        }

        for (CharBox c : Stream.concat(
                fieldComponents.get(Field.PATT).stream(),
                fieldComponents.get(Field.TEXT).subList(startInd, endInd).stream()).toList()) {
            c.setColor(col);
        }

        panel.repaint();  // update colour
    }

    static List<? extends CharBox> boyerMooreTextBoxes(String s, Map<Character, Integer> badChar, int defVal) {
        return new ArrayList<>(){{
            for (int i = 0; i < s.length(); i ++)
                add(new CharSubscript(s.charAt(i), badChar.getOrDefault(s.charAt(i), defVal)));
        }};
    }

    static List<? extends CharBox> boyerMoorePattBoxes(String s, int[] goodSuffix) {
        return new ArrayList<>(){{
            for (int i = 0; i < s.length(); i ++)
                add(new CharSubscript(s.charAt(i), goodSuffix[i]));
        }};
    }
}