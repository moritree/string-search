package UI;

import Algorithm.BoyerMoore;
import Algorithm.KMP;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A square panel that displays a single character from a string.
 */
public interface CharBox {
    static Color DEFAULT_COL() {
        return Color.LIGHT_GRAY;
    }
    static Color MISMATCH_COL() {
        return Color.RED;
    }
    static Color MATCH_COL() {
        return Color.GREEN;
    }
    JPanel panel();

    default void setColor(Color c) {
        panel().setBackground(c);
    }

    static List<? extends CharBox> singleBoxes(String str) {
        return new ArrayList<>(){{
            if (str != null) addAll(str.chars().boxed().map(c -> new SingleChar((char)(int)c)).toList());
        }};
    }

    static List<? extends CharBox> boyerMooreCharBoxes(String s) {
        int[] goodSuffix = BoyerMoore.goodSuffix(s);
        Map<Character, Integer> badChar = BoyerMoore.badChar(s);

        return new ArrayList<>(){{
            for (int i = 0; i < s.length(); i ++)
                add(new CharDoubleSub(s.charAt(i), goodSuffix[i], badChar.get(s.charAt(i))));
        }};
    }
}

/**
 * A simple square panel with a one character label inside it
 */
record SingleChar(char c, JPanel panel, int boxSize) implements CharBox {
    public SingleChar(char c) {
        this(c, new JPanel(), 60);
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(boxSize, boxSize));

        panel.setBorder(new LineBorder(Color.DARK_GRAY, 2));
        panel.setBackground(CharBox.DEFAULT_COL());

        JLabel  label = new JLabel(c + "");
        label.setForeground(Color.DARK_GRAY);
        label.setFont(new Font("Verdana", Font.PLAIN, (int) (boxSize * 0.6)));
        label.setHorizontalAlignment(JLabel.HORIZONTAL);
        label.setVerticalAlignment(JLabel.CENTER);

        panel.add(label, BorderLayout.CENTER);
    }
}

/**
 * A simple square panel with one character label, and a small number label underneath it.
 */
record CharSubscript(char c, JPanel panel, int boxSize, JPanel inside) implements CharBox {
    public CharSubscript(char c, int sub) {
        this(c, new JPanel(), 60, new JPanel(new BorderLayout()));
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(boxSize, boxSize));

        panel.setBorder(new LineBorder(Color.DARK_GRAY, 2));

        JLabel charLabel = new JLabel(c + "");
        charLabel.setForeground(Color.DARK_GRAY);
        charLabel.setFont(new Font("Verdana", Font.PLAIN, (int) (boxSize * 0.5)));
        charLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
        charLabel.setVerticalAlignment(JLabel.CENTER);

        JLabel matchLabel = new JLabel(sub + "");
        matchLabel.setForeground(Color.DARK_GRAY);
        matchLabel.setFont(new Font("Verdana", Font.PLAIN, (int) (boxSize * 0.2)));
        matchLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
        matchLabel.setVerticalAlignment(JLabel.CENTER);

        inside.add(charLabel, BorderLayout.CENTER);
        inside.add(matchLabel, BorderLayout.SOUTH);
        panel.add(inside);

        setColor(CharBox.DEFAULT_COL());
    }

    @Override
    public void setColor(Color c) {
        inside.setBackground(c);
    }
}

/**
 * A simple square panel with one character label, and a small number label underneath it.
 */
record CharDoubleSub(char c, JPanel panel, int boxSize, JPanel inside, JPanel south) implements CharBox {
    public CharDoubleSub(char c, int sub1, int sub2) {
        this(c, new JPanel(), 60, new JPanel(new BorderLayout()), new JPanel(new BorderLayout()));
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(boxSize, boxSize));

        panel.setBorder(new LineBorder(Color.DARK_GRAY, 2));

        JLabel charLabel = new JLabel(c + "");
        charLabel.setForeground(Color.DARK_GRAY);
        charLabel.setFont(new Font("Verdana", Font.PLAIN, (int) (boxSize * 0.5)));
        charLabel.setHorizontalAlignment(JLabel.HORIZONTAL);
        charLabel.setVerticalAlignment(JLabel.CENTER);

        // Bottom panel, with subscript numbers
        JLabel sub1Label = new JLabel(sub1 + "");
        sub1Label.setForeground(Color.DARK_GRAY);
        sub1Label.setFont(new Font("Verdana", Font.PLAIN, (int) (boxSize * 0.2)));
        sub1Label.setHorizontalAlignment(JLabel.LEFT);
        sub1Label.setVerticalAlignment(JLabel.CENTER);

        JLabel sub2Label = new JLabel(sub2 + "");
        sub2Label.setForeground(Color.DARK_GRAY);
        sub2Label.setFont(new Font("Verdana", Font.PLAIN, (int) (boxSize * 0.2)));
        sub2Label.setHorizontalAlignment(JLabel.RIGHT);
        sub2Label.setVerticalAlignment(JLabel.CENTER);

        south.add(sub1Label, BorderLayout.WEST);
        south.add(sub2Label, BorderLayout.EAST);

        // Arrange overall box
        inside.add(charLabel, BorderLayout.CENTER);
        inside.add(south, BorderLayout.SOUTH);
        panel.add(inside);

        setColor(CharBox.DEFAULT_COL());
    }

    @Override
    public void setColor(Color c) {
        inside.setBackground(c);
        south.setBackground(c);
    }
}

/**
 * Invisible spacer box
 * @param panel
 * @param boxSize
 */
record SpacerBox(JPanel panel, int boxSize) implements CharBox {
    public SpacerBox() {
        this(new JPanel(), 60);
        panel.setPreferredSize(new Dimension(boxSize, boxSize));
    }

    @Override
    public void setColor(Color c) { }
}