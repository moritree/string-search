package UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StringSearchUI extends JFrame {
    private final Map<String, SearchDisplay> algorithmOptions =
            Map.of("Knuth-Morris-Pratt", new KMPDisplay(), "Boyer-Moore", new BoyerMooreDisplay());

    private ControlPanel control;
    private SearchDisplay display;
    private JPanel cardPanel;

    public StringSearchUI() {
        initialise();
    }

    private void initialise() {
        setTitle("String Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(480, 360));

        // generate components
        control = new ControlPanel(algorithmOptions.keySet().toArray(new String[0]));
        display = new KMPDisplay();

        cardPanel = new JPanel(new CardLayout());
        algorithmOptions.keySet().forEach(k -> {
            cardPanel.add(algorithmOptions.get(k).panel(), k);
        });
        display = algorithmOptions.get((String)control.algSelector.getSelectedItem());
        ((CardLayout)cardPanel.getLayout()).show(cardPanel, (String)control.algSelector.getSelectedItem());

        // add listeners
        control.text.addActionListener((e) -> updateString(control.text.getText(), SearchDisplay.Field.TEXT));
        control.patt.addActionListener((e) -> updateString(control.patt.getText(), SearchDisplay.Field.PATT));
        control.stepButton.addActionListener((e) -> step());
        control.algSelector.addActionListener((e) -> {
            // update current active display
            display = algorithmOptions.get((String)control.algSelector.getSelectedItem());

            // show in card
            ((CardLayout)cardPanel.getLayout()).show(cardPanel, (String)control.algSelector.getSelectedItem());

            updateString(control.text.getText(), SearchDisplay.Field.TEXT);
            updateString(control.patt.getText(), SearchDisplay.Field.PATT);
            display.panel().repaint();
        });

        // arrange everything
        setLayout(new BorderLayout());
        add(control, BorderLayout.NORTH);
        add(new JScrollPane(cardPanel,
                        JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                BorderLayout.CENTER);

        // show
        pack();
        setVisible(true);
    }

    private void updateString(String s, SearchDisplay.Field field) {
        display.updateString(s, field);
        control.setAlgorithmControlsEnabled(display.ready());

        revalidate();
    }

    private void step() {
        display.step();
        control.setAlgorithmControlsEnabled(display.ready());
    }

    public static void main(String[] args) {
        new StringSearchUI();
    }
}

/**
 * UI controls; allows end user to input their own source text string and pattern
 */
class ControlPanel extends JPanel {
    protected final JTextField text;
    protected final JTextField patt;
    protected final JButton stepButton;
    protected final JComboBox<String> algSelector;

    public ControlPanel(String[] algorithmOptions) {
        stepButton = new JButton("Step");
        algSelector = new JComboBox<>(algorithmOptions);

        // (source) text field and label
        JPanel textPanel = new JPanel(new BorderLayout());
        text = new JTextField();
        textPanel.setMinimumSize(new Dimension(100, 0));
        textPanel.add(text, BorderLayout.CENTER);
        textPanel.add(new JLabel("Text"), BorderLayout.NORTH);

        // (pattern) text field and label
        JPanel pattPanel = new JPanel(new BorderLayout());
        patt = new JTextField();
        pattPanel.setMinimumSize(new Dimension(100, 0));
        pattPanel.add(patt, BorderLayout.CENTER);
        pattPanel.add(new JLabel("Pattern"), BorderLayout.NORTH);

        // core panel: where text fields lie
        JPanel core = new JPanel();
        core.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        // algorithm control panel
        JPanel algorithmControl = new JPanel();
        algorithmControl.add(algSelector);
        algorithmControl.add(stepButton);

        // arrange components
        c.weightx = 2;
        core.add(textPanel, c);
        c.weightx = 1;
        core.add(pattPanel, c);

        setLayout(new BorderLayout());
        add(core, BorderLayout.CENTER);
        add(algorithmControl, BorderLayout.SOUTH);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        setAlgorithmControlsEnabled(false);
    }

    public void setAlgorithmControlsEnabled(Boolean b) {
        stepButton.setEnabled(b);
//        for (Component component : algorithmControl.getComponents()) component.setEnabled(b);
    }
}
