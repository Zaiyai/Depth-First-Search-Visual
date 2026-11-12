import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    private final Color DARK = new Color(21, 30, 48);
    private final Color BLUE = new Color(35, 119, 210);
    private final Color GRAY = new Color(90, 105, 125);
    private final Color RED = new Color(220, 75, 75);
    private final Color WHITE = Color.WHITE;

    public Main() {
        showWelcomeFrame(); 
    }   

    public void showWelcomeFrame() {

        setTitle("DFS Algorithm");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK);

        JLabel title = new JLabel(
            "<html><center>" + "WELCOME TO<br>" +
            "<font color='#4DA8FF'>DEPTH-FIRST SEARCH</font><br>" + "<font color='#4DA8FF'>ALGORITHM VISUAL</font>" +
            "</center></html>", SwingConstants.CENTER);

        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(DARK);
        centerPanel.add(title, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 20, 25));

        buttonPanel.setBackground(DARK);

        JButton cancelButton = createButton("CANCEL", GRAY);

        cancelButton.addActionListener(e -> {
            System.exit(0);});

        buttonPanel.add(cancelButton);

        JButton proceedButton = createButton("PROCEED", BLUE);

        proceedButton.addActionListener(e -> {
            new TreeBuilderFrame().setVisible(true);
            dispose();});
            buttonPanel.add(proceedButton);


        JButton exitButton = createButton("EXIT", RED);
        exitButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(
                this, "Thank you for using Depth-First Search Algorithm Visual!");
            System.exit(0);});

        buttonPanel.add(exitButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
        setVisible(true);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(WHITE);
        button.setBackground(color);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(false);

        button.setPreferredSize(new Dimension(220, 45));
        button.setMaximumSize(new Dimension(250, 45));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        return button;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {new Main();});
    }
}
