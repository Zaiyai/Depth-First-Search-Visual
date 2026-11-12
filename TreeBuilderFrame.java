import javax.swing.*;
import java.awt.*;

public class TreeBuilderFrame extends JFrame {

    private static final Color DARK = new Color(21, 30, 48);
    private static final Color FIELD_BG = new Color(31, 43, 66);
    private static final Color BLUE = new Color(35, 119, 210);
    private static final Color GREEN = new Color(35, 180, 120);
    private static final Color GRAY = new Color(90, 105, 125);
    private static final Color LIGHT_GRAY = new Color(170, 185, 205);
    private static final Color WHITE = Color.WHITE;

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 30);
    private static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FIELD_FONT = new Font("SansSerif", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 13);

    private static final int CONTROL_HEIGHT = 40;
    private static final int STATUS_WIDTH = 300;

    private static final int GAP_SMALL = 6;
    private static final int GAP_MEDIUM = 10;
    private static final int GAP_LARGE = 15;
    private static final int GAP_XLARGE = 25;

    private boolean isBST;
    private JLabel treeTypeLabel;

    private JTextField rootInput;
    private JTextField leftInput;
    private JTextField rightInput;
    private JTextField startInput;
    private JTextField targetInput;

    private JComboBox<String> traversalOrder;

    private JLabel parentLabel;
    private JLabel statusLabel;

    private JButton setRootButton;
    private JButton yesButton;
    private JButton noButton;
    private JButton addButton;
    private JButton finishButton;

    private JPanel childPanel;
    private TreePanel treePanel;

    private TreeNode root;
    private TreeNode currentParent;

    private final DefaultListModel<TreeNode> parentQueue = new DefaultListModel<>();

    public TreeBuilderFrame() {
        chooseTreeType();

        setTitle("DFS Algorithm");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

        JLabel title = new JLabel("BUILD THE TREE!", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(WHITE);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(DARK);
        centerPanel.add(buildPreviewCard());
        centerPanel.add(buildInputCard());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
        setStartingState();
        addButtonActions();
        setVisible(true);
    }

    private void chooseTreeType() {
        String[] options = {"Binary Tree", "Binary Search Tree"};

        int choice = JOptionPane.showOptionDialog(
            null,
            "What kind of tree do you want to build?",
            "Choose Tree Type",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);

        if (choice == -1) {
            System.exit(0);
        }
        isBST = (choice == 1);
    }

    //design
    private JPanel buildPreviewCard() {
        treePanel = new TreePanel();

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(DARK);
        addCardBorder(card, "TREE PREVIEW");
        card.add(treePanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildInputCard() {
        rootInput = createTextField();
        leftInput = createTextField();
        rightInput = createTextField();
        startInput = createTextField();
        targetInput = createTextField();

        traversalOrder = createComboBox(new String[]{
            "Pre-order",
            "In-order",
            "Post-order"});

        setRootButton = createButton("SET ROOT", BLUE);
        yesButton = createButton("YES", GREEN);
        noButton = createButton("NO", GRAY);
        addButton = createButton("ADD CHILDREN", BLUE);
        finishButton = createButton("START DFS", GREEN);

        treeTypeLabel = createLabel("Tree Type: " + (isBST ? "Binary Search Tree" : "Binary Tree"));
        parentLabel = createLabel("Waiting for root...");

        statusLabel = createLabel("");
        statusLabel.setForeground(LIGHT_GRAY);
        setStatus("Enter the root node to begin.");

        JPanel choicePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        choicePanel.setBackground(DARK);
        choicePanel.add(yesButton);
        choicePanel.add(noButton);
        makeFullWidth(choicePanel);
        buildChildPanel();

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(DARK);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        inputPanel.add(treeTypeLabel);
        inputPanel.add(space(GAP_MEDIUM));

        //root
        inputPanel.add(createLabel("Enter Root Node:"));
        inputPanel.add(space(GAP_SMALL));
        inputPanel.add(rootInput);
        inputPanel.add(space(GAP_MEDIUM));
        inputPanel.add(setRootButton);

        //current parent
        inputPanel.add(space(GAP_XLARGE));
        inputPanel.add(parentLabel);
        inputPanel.add(space(GAP_MEDIUM));
        inputPanel.add(createLabel("Do you want to add children?"));
        inputPanel.add(space(GAP_SMALL));
        inputPanel.add(choicePanel);

        //children
        inputPanel.add(space(GAP_LARGE));
        inputPanel.add(childPanel);

        //starting node
        inputPanel.add(space(GAP_LARGE));
        inputPanel.add(createLabel("Starting Node (default is root):"));
        inputPanel.add(space(GAP_SMALL));
        inputPanel.add(startInput);

        //target node
        inputPanel.add(space(GAP_MEDIUM));
        inputPanel.add(createLabel("Target Node:"));
        inputPanel.add(space(GAP_SMALL));
        inputPanel.add(targetInput);

        //traversal order
        inputPanel.add(space(GAP_MEDIUM));
        inputPanel.add(createLabel("Choose the traversal order:"));
        inputPanel.add(space(GAP_SMALL));
        inputPanel.add(traversalOrder);

        inputPanel.add(space(GAP_LARGE));
        inputPanel.add(Box.createVerticalGlue());
        inputPanel.add(finishButton);
        inputPanel.add(space(GAP_MEDIUM));
        inputPanel.add(statusLabel);

        JScrollPane scrollPane = new JScrollPane(
            inputPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(DARK);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(DARK);
        addCardBorder(card, "TREE INPUT");
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void buildChildPanel() {
        childPanel = new JPanel();
        childPanel.setLayout(new BoxLayout(childPanel, BoxLayout.Y_AXIS));
        childPanel.setBackground(DARK);
        childPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        childPanel.add(createLabel("Left Child:"));
        childPanel.add(space(GAP_SMALL));
        childPanel.add(leftInput);

        childPanel.add(space(GAP_MEDIUM));
        childPanel.add(createLabel("Right Child:"));
        childPanel.add(space(GAP_SMALL));
        childPanel.add(rightInput);

        childPanel.add(space(GAP_MEDIUM));
        childPanel.add(addButton);
    }

    private void setStartingState() {
        childPanel.setVisible(false);
        yesButton.setEnabled(false);
        noButton.setEnabled(false);
        finishButton.setEnabled(false);
    }

    private void addCardBorder(JComponent component, String title) {
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GRAY),
                title,
                0, 0,
                SECTION_FONT,
                WHITE
            ),
            BorderFactory.createEmptyBorder(10, 18, 18, 18)
        ));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(WHITE);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(FIELD_FONT);
        field.setForeground(WHITE);
        field.setBackground(FIELD_BG);
        field.setCaretColor(WHITE);
        field.setDisabledTextColor(GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GRAY),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        makeFullWidth(field);
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(FIELD_FONT);
        combo.setForeground(DARK);
        combo.setBackground(WHITE);
        makeFullWidth(combo);
        return combo;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(enabled);
                setBackground(enabled ? color : color.darker().darker());
            }
        };
        button.setFont(BUTTON_FONT);
        button.setForeground(WHITE);
        button.setBackground(color);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        makeFullWidth(button);
        return button;
    }

    private void makeFullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setPreferredSize(new Dimension(0, CONTROL_HEIGHT));
        component.setMinimumSize(new Dimension(0, CONTROL_HEIGHT));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
    }

    private Component space(int height) {
        return Box.createVerticalStrut(height);
    }

    private void setStatus(String text) {
        statusLabel.setText(
            "<html><div style='width:" + STATUS_WIDTH + "px'>" + text + "</div></html>"
        );
    }

    //functionalities
    private void addButtonActions() {
        setRootButton.addActionListener(e -> setRoot());
        yesButton.addActionListener(e -> {
            showChildInputs(true);
            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            setStatus("Enter the children of " + currentParent.value);
        });

        noButton.addActionListener(e -> moveToNextParent());
        addButton.addActionListener(e -> addChildren());
        finishButton.addActionListener(e -> startDFS());
    }

    private void showChildInputs(boolean show) {
        childPanel.setVisible(show);
        revalidate();
        repaint();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    //logics here
    private void setRoot() {
        String rootText = rootInput.getText().trim();

        if (rootText.isEmpty()) {
            showError("Please enter a root node.");
            return;
        }
        if (!isNumber(rootText)) {
            showError("\"" + rootText + "\" is not a valid number.\nOnly numbers are allowed.");
            rootInput.requestFocus();
            rootInput.selectAll();
            return;
        }

        root = new TreeNode(rootText);
        treePanel.setRoot(root);
        parentQueue.addElement(root);

        rootInput.setEnabled(false);
        setRootButton.setEnabled(false);

        setStatus("Root node set successfully.");
        moveToNextParent();
    }

    private void moveToNextParent() {
        showChildInputs(false);

        if (parentQueue.isEmpty()) {
            currentParent = null;
            parentLabel.setText("All nodes processed!");

            yesButton.setEnabled(false);
            noButton.setEnabled(false);
            finishButton.setEnabled(true);

            setStatus("Tree building completed. Choose a traversal order and click START DFS.");
            treePanel.setCurrentNode(null);
            return;
        }

        currentParent = parentQueue.remove(0);
        parentLabel.setText("Current Parent: " + currentParent.value);
        treePanel.setCurrentNode(currentParent);

        yesButton.setEnabled(true);
        noButton.setEnabled(true);

        setStatus("Do you want to add children to " + currentParent.value + "?");
    }
    
    private void addChildren() {
        String left = leftInput.getText().trim();
        String right = rightInput.getText().trim();

        if (left.isEmpty() && right.isEmpty()) {
            showError("Please enter at least one child.");
            return;
        }

        if (!left.isEmpty() && !isNumber(left)) {
            showError("Left child \"" + left + "\" is not a valid number.\nOnly numbers are allowed.");
            leftInput.requestFocus();
            leftInput.selectAll();
            return;
        }
        if (!right.isEmpty() && !isNumber(right)) {
            showError("Right child \"" + right + "\" is not a valid number.\nOnly numbers are allowed.");
            rightInput.requestFocus();
            rightInput.selectAll();
            return;
        }

        // Binary Search Tree
        if (isBST) {
            String error = null;
            if (!left.isEmpty()) {
                error = bstError(left, true);
                if (error != null) {
                    leftInput.requestFocus();
                    leftInput.selectAll();
                }
            }
            if (error == null && !right.isEmpty()) {
                error = bstError(right, false);
                if (error != null) {
                    rightInput.requestFocus();
                    rightInput.selectAll();
                }
            }
            if (error != null) {
                showError(error);
                return;
            }
        }

        if (!left.isEmpty()) {
            currentParent.left = new TreeNode(left);
            parentQueue.addElement(currentParent.left);
        }

        if (!right.isEmpty()) {
            currentParent.right = new TreeNode(right);
            parentQueue.addElement(currentParent.right);
        }

        treePanel.repaint();
        leftInput.setText("");
        rightInput.setText("");
        moveToNextParent();
    }

    private void startDFS() {

        if (root == null) {
            showError("Tree is empty. Please create a tree first.");
            return;
        }

        String order = (String) traversalOrder.getSelectedItem();
        String start = startInput.getText().trim();
        String target = targetInput.getText().trim();

        TreeNode startNode = root;
        if (!start.isEmpty()) {
            if (!isNumber(start)) {
                showError("Starting node \"" + start + "\" is not a valid number.\nOnly numbers are allowed.");
                startInput.requestFocus();
                startInput.selectAll();
                return;
            }
            startNode = findNode(root, start);
            if (startNode == null) {
                showError("Starting node \"" + start + "\" is not in the tree.\n"
                        + "Enter a value that exists in the tree, or leave it blank to start at the root.");
                startInput.requestFocus();
                startInput.selectAll();
                return;
            }
        }

        if (!target.isEmpty()) {
            if (!isNumber(target)) {
                showError("Target \"" + target + "\" is not a valid number.\nOnly numbers are allowed.");
                targetInput.requestFocus();
                targetInput.selectAll();
                return;
            }
            if (!existsInTree(root, target)) {
                showError("Target \"" + target + "\" is not in the tree.\n"
                        + "Enter a value that exists in the tree, or leave it blank.");
                targetInput.requestFocus();
                targetInput.selectAll();
                return;
            }
        }

        DFSVisualizerFrame dfsFrame = new DFSVisualizerFrame(root, startNode, order, target);
        dfsFrame.setVisible(true);

        dispose();
    }

    private boolean isNumber(String text) {
        return text.matches("-?\\d+(\\.\\d+)?");
    }

    private String bstError(String text, boolean isLeft) {
        double value = Double.parseDouble(text);
        double parent = Double.parseDouble(currentParent.value);

        double[] range = new double[2];
        findBounds(root, currentParent, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, range);

        double low = isLeft ? range[0] : parent;
        double high = isLeft ? parent : range[1];

        if (value > low && value < high) {
            return null;
        }

        String message = (isLeft ? "Left" : "Right") + " child \"" + text
                + "\" is not allowed in a Binary Search Tree.\nIt must be ";

        boolean hasLow = low != Double.NEGATIVE_INFINITY;
        boolean hasHigh = high != Double.POSITIVE_INFINITY;

        if (hasLow && hasHigh) {
            message += "greater than " + show(low) + " and less than " + show(high) + ".";
        } else if (hasLow) {
            message += "greater than " + show(low) + ".";
        } else {
            message += "less than " + show(high) + ".";
        }
        return message;
    }

    private boolean findBounds(TreeNode node, TreeNode target, double low, double high, double[] out) {
        if (node == null) return false;

        if (node == target) {
            out[0] = low;
            out[1] = high;
            return true;
        }

        double v = Double.parseDouble(node.value);
        return findBounds(node.left, target, low, v, out)
            || findBounds(node.right, target, v, high, out);
    }

    private String show(double d) {
        return (d == Math.rint(d)) ? String.valueOf((long) d) : String.valueOf(d);
    }

    private boolean existsInTree(TreeNode node, String value) {
        if (node == null) return false;
        if (node.value.equals(value)) return true;
        return existsInTree(node.left, value) || existsInTree(node.right, value);
    }

    private TreeNode findNode(TreeNode node, String value) {
        if (node == null) return null;
        if (node.value.equals(value)) return node;
        TreeNode found = findNode(node.left, value);
        return (found != null) ? found : findNode(node.right, value);
    }
}

class TreeNode {

    String value;
    TreeNode left;
    TreeNode right;

    public TreeNode(String value) {
        this.value = value;
        this.left = null;
        this.right = null;
    }
}