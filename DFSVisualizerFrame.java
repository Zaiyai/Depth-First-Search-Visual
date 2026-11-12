import javax.swing.*;
import java.awt.*;

public class DFSVisualizerFrame extends JFrame {

    private final Color DARK = new Color(21, 30, 48);
    private final Color BLUE = new Color(35, 119, 210);
    private final Color GREEN = new Color(35, 180, 120);
    private final Color YELLOW = new Color(255, 193, 7);
    private final Color GRAY = new Color(110, 125, 145);
    private final Color RED = new Color(220, 75, 75);
    private final Color WHITE = Color.WHITE;

    private JLabel currentNodeLabel;
    private JLabel traversalLabel;
    private TreePanel treePanel;

    private JButton playButton;
    private JButton resetButton;
    private JButton backButton;
    private JButton exitButton;

    private final String order;
    private final String target;
    private boolean targetFound = false;

    private final DefaultListModel<TreeNode> visitOrder = new DefaultListModel<>();

    private int stepIndex = 0;

    public DFSVisualizerFrame(TreeNode root, TreeNode startNode, String order, String target) {

        this.order = order;
        this.target = target.trim();

        buildVisitOrder(startNode);

        setTitle("DFS Algorithm");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(DARK);

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(15, 20, 10, 20)
        );

        JLabel title = new JLabel(
                "DEPTH-FIRST SEARCH VISUALIZATION",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 23));
        title.setForeground(WHITE);

        mainPanel.add(title, BorderLayout.NORTH);

        treePanel = new TreePanel();
        treePanel.setRoot(root);
        treePanel.setPreferredSize(new Dimension(850, 350));

        mainPanel.add(treePanel, BorderLayout.CENTER);

        mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);

        add(mainPanel);

        addButtonActions();
        resetTraversal();
    }

    //design
    private JPanel buildBottomPanel() {

        JPanel bottomPanel = new JPanel();

        bottomPanel.setBackground(DARK);

        bottomPanel.setLayout(
                new BoxLayout(bottomPanel, BoxLayout.Y_AXIS)
        );

        currentNodeLabel = createLabel("", 15);
        traversalLabel = createLabel("", 14);

        bottomPanel.add(currentNodeLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(traversalLabel);
        bottomPanel.add(Box.createVerticalStrut(5));

        JPanel legendPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 12, 3)
        );

        legendPanel.setBackground(DARK);

        legendPanel.add(createLegend("Not visited", BLUE));
        legendPanel.add(createLegend("Processing", YELLOW));
        legendPanel.add(createLegend("Visited", GRAY));

        if (!target.isEmpty()) {
            legendPanel.add(createLegend("Target found", GREEN));
        }

        bottomPanel.add(legendPanel);

        playButton = createButton("PLAY", GREEN);
        resetButton = createButton("RESET", GRAY);
        backButton = createButton("BACK", GRAY);
        exitButton = createButton("EXIT", RED);

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 5)
        );

        buttonPanel.setBackground(DARK);
        buttonPanel.add(playButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backButton);
        buttonPanel.add(exitButton);
        bottomPanel.add(buttonPanel);

        return bottomPanel;
    }

    private JLabel createLabel(String text, int size) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Arial", Font.PLAIN, size)
        );

        label.setForeground(WHITE);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        return label;
    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        button.setForeground(WHITE);
        button.setBackground(color);

        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);

        button.setPreferredSize(
                new Dimension(110, 35)
        );

        return button;
    }

    private JLabel createLegend(String text, Color color) {

        JLabel label = new JLabel("● " + text);

        label.setFont(
                new Font("Arial", Font.BOLD, 11)
        );

        label.setForeground(color);

        return label;
    }

    //functionalities
    private void addButtonActions() {

        playButton.addActionListener(e -> nextStep());
        resetButton.addActionListener(e -> resetTraversal());
        backButton.addActionListener(e -> {

            new TreeBuilderFrame().setVisible(true);
            dispose();

        });

        exitButton.addActionListener(e -> {
            dispose();

            SwingUtilities.invokeLater(() -> {
                new Main();
            });

        });
    }

    //logics here
    private void buildVisitOrder(TreeNode node) {

        if (node == null) {
            return;
        }

        if (order.equals("Pre-order")) {

            visitOrder.addElement(node);

            buildVisitOrder(node.left);
            buildVisitOrder(node.right);

        } else if (order.equals("In-order")) {

            buildVisitOrder(node.left);
            visitOrder.addElement(node);
            buildVisitOrder(node.right);

        } else {

            buildVisitOrder(node.left);
            buildVisitOrder(node.right);
            visitOrder.addElement(node);
        }
    }

    private void nextStep() {

        if (targetFound) {
            return;
        }

        if (stepIndex > 0) {
            treePanel.markVisited(
                    visitOrder.get(stepIndex - 1)
            );
        }

        if (stepIndex >= visitOrder.size()) {
            treePanel.setCurrentNode(null);

            if (target.isEmpty()) {
                currentNodeLabel.setForeground(GREEN);
                currentNodeLabel.setText("Traversal complete!");

            } else {
                currentNodeLabel.setForeground(RED);
                currentNodeLabel.setText(
                        "Target \"" + target + "\" was not found."
                );
            }

            return;
        }

        TreeNode node = visitOrder.get(stepIndex);

        stepIndex++;

        treePanel.setCurrentNode(node);

        currentNodeLabel.setForeground(YELLOW);

        currentNodeLabel.setText(
                "Currently Processing: " + node.value
        );

        updateTraversalLabel();

        if (!target.isEmpty() && node.value.equals(target)) {

            targetFound = true;

            treePanel.markFound(node);

            currentNodeLabel.setForeground(GREEN);

            currentNodeLabel.setText(
                    "Target \"" + target +
                    "\" found at step " + stepIndex + "!"
            );
        }
    }

    private void resetTraversal() {

        stepIndex = 0;
        targetFound = false;

        treePanel.reset();

        currentNodeLabel.setForeground(WHITE);

        currentNodeLabel.setText(
                "Currently Processing: None"
        );

        updateTraversalLabel();
    }

    private void updateTraversalLabel() {

        String text = "";

        for (int i = 0; i < stepIndex; i++) {

            if (i > 0) {
                text += " → ";
            }

            text += visitOrder.get(i).value;
        }

        traversalLabel.setText(
                order + " Traversal: " + text
        );
    }
}