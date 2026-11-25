import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private final Color BG = new Color(255, 228, 236);
    private final Color TEXT = new Color(122, 52, 82);
    private final Color PINK = new Color(236, 120, 160);
    private final Color RIBBON = new Color(200, 70, 90);
    private final Color WHITE = Color.WHITE;

    public Main() {
        showWelcomeFrame();
    }

    public void showWelcomeFrame() {

        //window settings
        setTitle("DFS Algorithm");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG);

        //welcome title
        JLabel title = new JLabel(
            "<html><center>" + "Welcome to <br>" +
            "<font color='#E0709B'>Depth-First Search</font><br>" +
            "<font color='#E0709B'>Algorithm Visual</font>" +
            "</center></html>", SwingConstants.CENTER);

        title.setFont(new Font("Segoe Script", Font.BOLD, 38));
        title.setForeground(TEXT);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG);
        centerPanel.add(title, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        //buttons at the bottom
        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 20, 50));

        buttonPanel.setBackground(BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 60, 0));

        JButton treeButton = createButton("BUILD TREE", PINK);
        treeButton.addActionListener(e -> {
            TreeBuilderFrame builder = new TreeBuilderFrame();
            if (builder.isReady()) {
                dispose();
            }
        });

        buttonPanel.add(treeButton);

        JButton graphButton = createButton("BUILD GRAPH", PINK);
        graphButton.addActionListener(e -> {
            TreeBuilderFrame.GraphBuilderFrame builder = new TreeBuilderFrame.GraphBuilderFrame();
            if (builder.isReady()) {
                dispose();
            }
        });

        buttonPanel.add(graphButton);
        JButton exitButton = createButton("EXIT", RIBBON);

        exitButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this,
                "Thank you for using Depth-First Search Algorithm Visual! ♡"
            );
            System.exit(0);
        });

        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);
    }

    //creates a flat & colored button
    private JButton createButton(String text, Color color) {
        JButton button = new RoundedButton(text, 24);

        button.setFont(new Font("Georgia", Font.BOLD, 15));
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setRolloverEnabled(false);

        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(250, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    //starts the program
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main();
        });
    }
}