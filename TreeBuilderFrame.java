import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeBuilderFrame extends JFrame {

    private static final Color DARK = new Color(122, 52, 82);
    private static final Color BG = new Color(255, 228, 236);
    private static final Color FIELD_BG = new Color(255, 240, 245);
    private static final Color FIELD_BORDER = new Color(240, 190, 208);
    private static final Color BLUE = new Color(236, 120, 160);
    private static final Color GREEN = new Color(86, 160, 110);
    private static final Color RED = new Color(200, 70, 90);
    private static final Color GRAY = new Color(190, 150, 170);
    private static final Color AMBER = new Color(214, 84, 130);
    private static final Color TEAL = new Color(200, 90, 130);
    private static final Color WHITE = Color.WHITE;

    private static final Font TITLE_FONT = new Font("Segoe Script", Font.BOLD, 32);
    private static final Font SECTION_FONT = new Font("Georgia", Font.BOLD, 15);
    private static final Font LABEL_FONT = new Font("Georgia", Font.BOLD | Font.ITALIC, 15);
    private static final Font FIELD_FONT = new Font("Arial", Font.PLAIN, 16);
    private static final Font BUTTON_FONT = new Font("Georgia", Font.BOLD, 14);
    private static final Font TREE_TYPE_FONT = new Font("Georgia", Font.BOLD | Font.ITALIC, 18);

    //sizes and spacing
    private static final int CONTROL_HEIGHT = 40;
    private static final int CONTROL_RADIUS = 12;
    private static final int BUTTON_RADIUS = 24;
    private static final int BOX_RADIUS = 24;

    private static final int GAP_SMALL = 6;
    private static final int GAP_MEDIUM = 10;
    private static final int GAP_LARGE = 15;

    //steps of the flow
    private static final int STEP_ROOT = 0;
    private static final int STEP_QUESTION = 1;
    private static final int STEP_CHILDREN = 2;
    private static final int STEP_FINISH = 3;

    //tree type choices
    private boolean isBST;
    private JLabel treeTypeLabel;

    //input fields
    private JTextField rootInput;
    private JTextField leftInput;
    private JTextField rightInput;
    private JTextField startInput;
    private JTextField targetInput;

    private JComboBox<String> traversalOrder;

    //shows which node is being processed
    private JLabel parentLabel;

    private JLabel questionLabel;

    //buttons
    private JButton setRootButton;
    private JButton yesButton;
    private JButton noButton;
    private JButton addButton;
    private JButton cancelChildButton;
    private JButton finishButton;

    //one panel per step
    private JPanel rootSection;
    private JPanel questionSection;
    private JPanel childPanel;
    private JPanel finishSection;

    private TreePanel treePanel;

    //tree data
    private TreeNode root;
    private TreeNode currentParent;

    private final DefaultListModel<TreeNode> parentQueue = new DefaultListModel<>();

    private boolean ready;

    public TreeBuilderFrame(boolean isBST) {
        this.isBST = isBST;
        ready = true;
        buildFrame();
        showStep(STEP_ROOT);
        addButtonActions();
        setVisible(true);
    }

    public TreeBuilderFrame(TreeNode root, boolean isBST, String start, String target, String order) {
        this.isBST = isBST;
        ready = true;
        buildFrame();
        addButtonActions();

        this.root = root;
        treePanel.setRoot(root);
        parentQueue.addElement(root);

        rootInput.setText(root.value);
        rootInput.setEnabled(false);
        setRootButton.setEnabled(false);

        startInput.setText(start);
        targetInput.setText(target);
        traversalOrder.setSelectedItem(order);

        moveToNextParent();
        setVisible(true);
    }

    private void buildFrame() {

        //window settings
        setTitle("DFS Algorithm");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

        JLabel title = new JLabel("Build the Tree!", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(DARK);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(BG);
        centerPanel.add(buildPreviewCard());
        centerPanel.add(buildInputCard());
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    public boolean isReady() {
        return ready;
    }

    //design
    private JPanel buildPreviewCard() {
        treePanel = new TreePanel();

        treeTypeLabel = createLabel("Tree Type: " + (isBST ? "Binary Search Tree" : "Binary Tree"));
        treeTypeLabel.setFont(TREE_TYPE_FONT);
        treeTypeLabel.setForeground(TEAL);
        treeTypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        treeTypeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, GAP_MEDIUM, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BLUE, BOX_RADIUS),
            BorderFactory.createEmptyBorder(10, 18, 18, 18)
        ));
        card.add(treeTypeLabel, BorderLayout.NORTH);
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

        //button colors
        setRootButton = createButton("SET ROOT", BLUE);
        yesButton = createButton("YES", GREEN);
        noButton = createButton("NO", GRAY);
        addButton = createButton("ADD CHILDREN", BLUE);
        cancelChildButton = createButton("CANCEL", RED);
        finishButton = createButton("VISUALIZE DFS", GREEN);

        parentLabel = createLabel("Waiting for root...");
        parentLabel.setForeground(AMBER);

        buildRootSection();
        buildQuestionSection();
        buildChildPanel();
        buildFinishSection();

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(WHITE);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        //root
        inputPanel.add(rootSection);

        //current parent
        inputPanel.add(parentLabel);
        inputPanel.add(questionSection);

        //children
        inputPanel.add(childPanel);

        //start node, target, traversal, start dfs
        inputPanel.add(finishSection);

        //keeps the controls centered & scrolls if too tall
        JPanel centerWrapper = new CenteredScrollPanel();
        centerWrapper.setLayout(new GridBagLayout());
        centerWrapper.setBackground(WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(inputPanel, gbc);

        JScrollPane scrollPane = new JScrollPane(
            centerWrapper,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(WHITE);
        scrollPane.getViewport().setBackground(WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel card = new RoundedPanel(new BorderLayout(), WHITE, BOX_RADIUS);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    //empty vertical panel for one step
    private JPanel createSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(WHITE);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    private void buildRootSection() {
        rootSection = createSection();
        rootSection.add(createLabel("Enter Root Node:"));
        rootSection.add(space(GAP_SMALL));
        rootSection.add(rootInput);
        rootSection.add(space(GAP_MEDIUM));
        rootSection.add(setRootButton);
    }

    private void buildQuestionSection() {
        JPanel choicePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        choicePanel.setBackground(WHITE);
        choicePanel.add(yesButton);
        choicePanel.add(noButton);
        makeFullWidth(choicePanel);

        questionLabel = createLabel("Do you want to add children?");

        questionSection = createSection();
        questionSection.add(space(GAP_MEDIUM));
        questionSection.add(questionLabel);
        questionSection.add(space(GAP_SMALL));
        questionSection.add(choicePanel);
    }

    private void buildChildPanel() {
        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setBackground(WHITE);
        buttonRow.add(addButton);
        buttonRow.add(cancelChildButton);
        makeFullWidth(buttonRow);

        childPanel = createSection();
        childPanel.add(space(GAP_MEDIUM));
        childPanel.add(createLabel("Left Child:"));
        childPanel.add(space(GAP_SMALL));
        childPanel.add(leftInput);

        childPanel.add(space(GAP_MEDIUM));
        childPanel.add(createLabel("Right Child:"));
        childPanel.add(space(GAP_SMALL));
        childPanel.add(rightInput);

        childPanel.add(space(GAP_MEDIUM));
        childPanel.add(buttonRow);
    }

    private void buildFinishSection() {
        finishSection = createSection();

        finishSection.add(createLabel("Starting Node:"));
        finishSection.add(space(GAP_SMALL));
        finishSection.add(startInput);

        finishSection.add(space(GAP_MEDIUM));
        finishSection.add(createLabel("Target Node:"));
        finishSection.add(space(GAP_SMALL));
        finishSection.add(targetInput);

        finishSection.add(space(GAP_MEDIUM));
        finishSection.add(createLabel("Choose the traversal order:"));
        finishSection.add(space(GAP_SMALL));
        finishSection.add(traversalOrder);

        finishSection.add(space(GAP_LARGE));
        finishSection.add(finishButton);
    }

    //shows only the controls of the given step
    private void showStep(int step) {
        rootSection.setVisible(step == STEP_ROOT);
        parentLabel.setVisible(step == STEP_QUESTION || step == STEP_CHILDREN);
        questionSection.setVisible(step == STEP_QUESTION);
        childPanel.setVisible(step == STEP_CHILDREN);
        finishSection.setVisible(step == STEP_FINISH);

        revalidate();
        repaint();
    }

    //titled border for a card
    private void addCardBorder(JComponent component, String title) {
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GRAY),
                title,
                0, 0,
                SECTION_FONT,
                DARK
            ),
            BorderFactory.createEmptyBorder(10, 18, 18, 18)
        ));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField createTextField() {
        JTextField field = new RoundedTextField(FIELD_BORDER, BLUE, CONTROL_RADIUS);
        field.setFont(FIELD_FONT);
        field.setForeground(DARK);
        field.setBackground(FIELD_BG);
        field.setCaretColor(DARK);
        field.setDisabledTextColor(GRAY);
        field.setHorizontalAlignment(JTextField.CENTER);
        makeFullWidth(field);
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> combo = new RoundedComboBox(items, FIELD_BORDER, BLUE, CONTROL_RADIUS);
        combo.setFont(FIELD_FONT);
        combo.setForeground(DARK);
        combo.setBackground(FIELD_BG);
        makeFullWidth(combo);
        return combo;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new RoundedButton(text, BUTTON_RADIUS);

        button.setFont(BUTTON_FONT);
        button.setForeground(WHITE);
        button.setBackground(color);
        makeFullWidth(button);
        return button;
    }

    //stretches a control across the width at a fixed height
    private void makeFullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setPreferredSize(new Dimension(0, CONTROL_HEIGHT));
        component.setMinimumSize(new Dimension(0, CONTROL_HEIGHT));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTROL_HEIGHT));
    }

    //vertical gap
    private Component space(int height) {
        return Box.createVerticalStrut(height);
    }

    //functionalities
    private void addButtonActions() {

        //save the root then ask about children
        setRootButton.addActionListener(e -> setRoot());

        //show the child inputs
        yesButton.addActionListener(e -> {
            fillChildInputs();
            showStep(STEP_CHILDREN);
        });

        //skip to the next node
        noButton.addActionListener(e -> {
            enqueueChildren();
            moveToNextParent();
        });

        //validate and add children
        addButton.addActionListener(e -> addChildren());

        //clears the input and go back to the question
        cancelChildButton.addActionListener(e -> {
            leftInput.setText("");
            rightInput.setText("");
            showStep(STEP_QUESTION);
        });

         //open the visualizer
        finishButton.addActionListener(e -> startDFS());
    }

    //error popup
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    //logics here
    // validates and saves the root
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
        startInput.setText(rootText);
        treePanel.setRoot(root);
        parentQueue.addElement(root);

        rootInput.setEnabled(false);
        setRootButton.setEnabled(false);

        moveToNextParent();
    }

    //picks the next node to ask about or finishes
    private void moveToNextParent() {

        //if no nodes left go to the final step
        if (parentQueue.isEmpty()) {
            currentParent = null;
            parentLabel.setText("All nodes processed!");

            showStep(STEP_FINISH);

            treePanel.setCurrentNode(null);
            return;
        }

        currentParent = parentQueue.remove(0);
        parentLabel.setText("Current Parent: " + currentParent.value);
        treePanel.setCurrentNode(currentParent);

        boolean hasChildren = currentParent.left != null || currentParent.right != null;
        questionLabel.setText(hasChildren
                ? "Do you want to change the children?"
                : "Do you want to add children?");

        showStep(STEP_QUESTION);
    }

    private void fillChildInputs() {
        leftInput.setText(currentParent.left != null ? currentParent.left.value : "");
        rightInput.setText(currentParent.right != null ? currentParent.right.value : "");
    }

    private void enqueueChildren() {
        if (currentParent.left != null) {
            parentQueue.addElement(currentParent.left);
        }
        if (currentParent.right != null) {
            parentQueue.addElement(currentParent.right);
        }
    }

    //validates the inputs and attaches the children
    private void addChildren() {
        String left = leftInput.getText().trim();
        String right = rightInput.getText().trim();

        boolean hasChildren = currentParent.left != null || currentParent.right != null;
        if (left.isEmpty() && right.isEmpty() && !hasChildren) {
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

        //binary search tree rules
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

        if (left.isEmpty()) {
            currentParent.left = null;
        } else if (currentParent.left == null || !currentParent.left.value.equals(left)) {
            currentParent.left = new TreeNode(left);
        }

        if (right.isEmpty()) {
            currentParent.right = null;
        } else if (currentParent.right == null || !currentParent.right.value.equals(right)) {
            currentParent.right = new TreeNode(right);
        }

        enqueueChildren();

        treePanel.repaint();
        leftInput.setText("");
        rightInput.setText("");
        moveToNextParent();
    }

    //validates the DFS settings
    private void startDFS() {

        if (root == null) {
            showError("Tree is empty. Please create a tree first.");
            return;
        }

        String order = (String) traversalOrder.getSelectedItem();
        String start = startInput.getText().trim();
        String target = targetInput.getText().trim();

        //start at the root unless another node is given
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

        //target is optional but must exist in the tree
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

        DFSVisualizerFrame dfsFrame = new DFSVisualizerFrame(root, startNode, order, target, isBST);
        dfsFrame.setVisible(true);

        dispose();
    }

    //true for integers and decimals with an optional minus
    private boolean isNumber(String text) {
        return text.matches("-?\\d+(\\.\\d+)?");
    }

    //returns an error message if the value breaks the BST rule 
    private String bstError(String text, boolean isLeft) {
        double value = Double.parseDouble(text);
        double parent = Double.parseDouble(currentParent.value);

        //allowed range for this parent's subtree
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

    //finds the lowest and highest values allowed under the target node
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

    //prints whole numbers without .0
    private String show(double d) {
        return (d == Math.rint(d)) ? String.valueOf((long) d) : String.valueOf(d);
    }

    //true if the value is in the tree
    private boolean existsInTree(TreeNode node, String value) {
        if (node == null) return false;
        if (node.value.equals(value)) return true;
        return existsInTree(node.left, value) || existsInTree(node.right, value);
    }

    //returns the node with this value
    private TreeNode findNode(TreeNode node, String value) {
        if (node == null) return null;
        if (node.value.equals(value)) return node;
        TreeNode found = findNode(node.left, value);
        return (found != null) ? found : findNode(node.right, value);
    }


    //screen for adding and editing a general graph before running DFS
    public static class GraphBuilderFrame extends JFrame {

        private static final Color DARK = new Color(122, 52, 82);
        private static final Color BG = new Color(255, 228, 236);
        private static final Color FIELD_BG = new Color(255, 240, 245);
        private static final Color FIELD_BORDER = new Color(240, 190, 208);
        private static final Color BLUE = new Color(236, 120, 160);
        private static final Color GREEN = new Color(86, 160, 110);
        private static final Color RED = new Color(200, 70, 90);
        private static final Color GRAY = new Color(190, 150, 170);
        private static final Color AMBER = new Color(214, 84, 130);
        private static final Color TEAL = new Color(200, 90, 130);
        private static final Color WHITE = Color.WHITE;

        private static final Font TITLE_FONT = new Font("Segoe Script", Font.BOLD, 32);
        private static final Font TYPE_FONT = new Font("Georgia", Font.BOLD | Font.ITALIC, 18);
        private static final Font STATUS_FONT = new Font("Georgia", Font.ITALIC, 14);
        private static final Font LABEL_FONT = new Font("Georgia", Font.BOLD | Font.ITALIC, 15);
        private static final Font FIELD_FONT = new Font("Arial", Font.PLAIN, 16);
        private static final Font BUTTON_FONT = new Font("Georgia", Font.BOLD, 14);

        private static final int CONTROL_HEIGHT = 40;
        private static final int CONTROL_RADIUS = 12;
        private static final int BUTTON_RADIUS = 24;
        private static final int BOX_RADIUS = 24;
        private static final int GAP_SMALL = 6;
        private static final int GAP_MEDIUM = 10;
        private static final int GAP_LARGE = 15;

        private static final String NONE = "<None>";
        private static final String UNDIRECTED = "Undirected Graph";
        private static final String DIRECTED = "Directed Graph";

        //graph data and state
        private Graph graph;
        private boolean directed;
        private boolean ready;

        //preview
        private TreePanel.GraphPanel graphPanel;
        private JLabel typeLabel;
        private JLabel statusLabel;

        //inputs
        private JTextField vertexInput;
        private JComboBox<String> startCombo;
        private JComboBox<String> targetCombo;

        private final DefaultListModel<String> vertexModel = new DefaultListModel<>();
        private final DefaultListModel<Graph.Edge> edgeModel = new DefaultListModel<>();
        private JList<String> vertexList;
        private JList<Graph.Edge> edgeList;
        private JPanel vertexSection;
        private JPanel edgeSection;
        private JPanel dfsSettingsSection;

        //buttons
        private JButton addVertexButton;
        private JButton renameButton;
        private JButton deleteVertexButton;
        private JButton finishVerticesButton;
        private JButton removeEdgeButton;
        private JButton visualizeButton;

        public GraphBuilderFrame(boolean directed) {
            this.directed = directed;
            graph = new Graph(directed);
            ready = true;

            buildFrame();
            addButtonActions();
            refreshAll();
            setVertexEntryMode(true);
            setVisible(true);
        }

        //used when returning from the visualizer so the graph can be edited again
        public GraphBuilderFrame(Graph graph, boolean directed, String start, String target) {
            this.graph = graph;
            this.directed = directed;
            this.ready = true;

            buildFrame();
            addButtonActions();

            startCombo.setSelectedItem(start);
            targetCombo.setSelectedItem(target == null || target.isEmpty() ? NONE : target);

            refreshAll();
            setVertexEntryMode(false);
            setVisible(true);
        }

        public boolean isReady() {
            return ready;
        }

        //design
        private void buildFrame() {
            setTitle("DFS Algorithm");
            setSize(1100, 650);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(true);

            JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
            mainPanel.setBackground(BG);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 25, 25));

            JLabel title = new JLabel("Build the Graph!", SwingConstants.CENTER);
            title.setFont(TITLE_FONT);
            title.setForeground(DARK);
            mainPanel.add(title, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            centerPanel.setBackground(BG);
            centerPanel.add(buildPreviewCard());
            centerPanel.add(buildInputCard());
            mainPanel.add(centerPanel, BorderLayout.CENTER);

            add(mainPanel);
        }

        private JPanel buildPreviewCard() {
            graphPanel = new TreePanel.GraphPanel();

            typeLabel = createLabel("Graph Type: " + typeName());
            typeLabel.setFont(TYPE_FONT);
            typeLabel.setForeground(TEAL);
            typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            statusLabel = createLabel("");
            statusLabel.setFont(STATUS_FONT);
            statusLabel.setForeground(DARK);
            statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JPanel heading = new JPanel();
            heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
            heading.setBackground(BG);
            heading.add(typeLabel);
            heading.add(Box.createVerticalStrut(GAP_SMALL));
            heading.add(statusLabel);
            heading.add(Box.createVerticalStrut(GAP_MEDIUM));

            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(BG);
            card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BLUE, BOX_RADIUS),
                BorderFactory.createEmptyBorder(10, 18, 18, 18)
            ));
            card.add(heading, BorderLayout.NORTH);
            card.add(graphPanel, BorderLayout.CENTER);

            return card;
        }

        private JPanel buildInputCard() {
            vertexInput = createTextField();
            startCombo = createComboBox(new String[0]);
            targetCombo = createComboBox(new String[]{NONE});

            addVertexButton = createButton("ADD VERTEX", BLUE);
            renameButton = createButton("RENAME", AMBER);
            deleteVertexButton = createButton("DELETE VERTEX", RED);
            finishVerticesButton = createButton("DONE ADDING VERTICES", GREEN);
            removeEdgeButton = createButton("REMOVE EDGE", RED);
            visualizeButton = createButton("VISUALIZE DFS", GREEN);

            vertexList = new JList<>(vertexModel);
            styleList(vertexList);

            edgeList = new JList<>(edgeModel);
            styleList(edgeList);
            edgeList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                              boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Graph.Edge) {
                        Graph.Edge edge = (Graph.Edge) value;
                        setText(edge.label(directed));
                    }
                    return this;
                }
            });

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
            inputPanel.setBackground(WHITE);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

            inputPanel.add(buildVertexSection());
            inputPanel.add(buildEdgeSection());
            inputPanel.add(buildDfsSection());

            JPanel centerWrapper = new CenteredScrollPanel();
            centerWrapper.setLayout(new GridBagLayout());
            centerWrapper.setBackground(WHITE);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            centerWrapper.add(inputPanel, gbc);

            JScrollPane scrollPane = new JScrollPane(
                centerWrapper,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setBackground(WHITE);
            scrollPane.getViewport().setBackground(WHITE);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            JPanel card = new RoundedPanel(new BorderLayout(), WHITE, BOX_RADIUS);
            card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
            card.add(scrollPane, BorderLayout.CENTER);

            return card;
        }

        private JPanel buildVertexSection() {
            JPanel section = createSection();
            section.add(space(GAP_MEDIUM));
            section.add(createLabel("Vertex Name:"));
            section.add(space(GAP_SMALL));
            section.add(vertexInput);

            JPanel addRow = new JPanel(new GridLayout(1, 2, 10, 0));
            addRow.setBackground(WHITE);
            addRow.add(addVertexButton);
            addRow.add(renameButton);
            makeFullWidth(addRow);
            section.add(space(GAP_SMALL));
            section.add(addRow);

            section.add(space(GAP_MEDIUM));
            section.add(createLabel("Vertices (select one to rename or delete):"));
            section.add(space(GAP_SMALL));
            section.add(wrapList(vertexList));

            section.add(space(GAP_SMALL));
            section.add(deleteVertexButton);
            section.add(space(GAP_MEDIUM));
            section.add(finishVerticesButton);
            vertexSection = section;
            return section;
        }

        private JPanel buildEdgeSection() {
            JPanel section = createSection();
            section.add(space(GAP_MEDIUM));
            section.add(createLabel("Drag from one vertex to another to add a connection."));
            section.add(space(GAP_MEDIUM));
            section.add(createLabel("Edges:"));
            section.add(space(GAP_SMALL));
            section.add(wrapList(edgeList));

            section.add(space(GAP_SMALL));
            section.add(removeEdgeButton);
            edgeSection = section;
            return section;
        }

        private JPanel buildDfsSection() {
            JPanel section = createSection();
            dfsSettingsSection = createSection();
            dfsSettingsSection.add(space(GAP_MEDIUM));
            dfsSettingsSection.add(createLabel("Starting Vertex:"));
            dfsSettingsSection.add(space(GAP_SMALL));
            dfsSettingsSection.add(startCombo);

            dfsSettingsSection.add(space(GAP_MEDIUM));
            dfsSettingsSection.add(createLabel("Target Vertex (optional):"));
            dfsSettingsSection.add(space(GAP_SMALL));
            dfsSettingsSection.add(targetCombo);
            dfsSettingsSection.add(space(GAP_LARGE));
            dfsSettingsSection.add(visualizeButton);
            section.add(dfsSettingsSection);

            return section;
        }

        private JPanel createSection() {
            JPanel section = new JPanel();
            section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
            section.setBackground(WHITE);
            section.setAlignmentX(Component.LEFT_ALIGNMENT);
            return section;
        }

        private JLabel createLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(LABEL_FONT);
            label.setForeground(DARK);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            return label;
        }

        private JTextField createTextField() {
            JTextField field = new RoundedTextField(FIELD_BORDER, BLUE, CONTROL_RADIUS);
            field.setFont(FIELD_FONT);
            field.setForeground(DARK);
            field.setBackground(FIELD_BG);
            field.setCaretColor(DARK);
            field.setDisabledTextColor(GRAY);
            field.setHorizontalAlignment(JTextField.CENTER);
            makeFullWidth(field);
            return field;
        }

        private JComboBox<String> createComboBox(String[] items) {
            JComboBox<String> combo = new RoundedComboBox(items, FIELD_BORDER, BLUE, CONTROL_RADIUS);
            combo.setFont(FIELD_FONT);
            combo.setForeground(DARK);
            combo.setBackground(FIELD_BG);
            makeFullWidth(combo);
            return combo;
        }

        private JButton createButton(String text, Color color) {
            JButton button = new RoundedButton(text, BUTTON_RADIUS);
            button.setFont(BUTTON_FONT);
            button.setForeground(WHITE);
            button.setBackground(color);
            makeFullWidth(button);
            return button;
        }

        private void styleList(JList<?> list) {
            list.setFont(FIELD_FONT);
            list.setForeground(DARK);
            list.setBackground(FIELD_BG);
            list.setSelectionBackground(BLUE);
            list.setSelectionForeground(WHITE);
            list.setFixedCellHeight(26);
            list.setVisibleRowCount(4);
        }

        private JScrollPane wrapList(JList<?> list) {
            JScrollPane pane = new JScrollPane(list);
            pane.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
            pane.setBackground(WHITE);
            pane.getViewport().setBackground(FIELD_BG);
            makeFullWidthHeight(pane, 110);
            return pane;
        }

        private void makeFullWidth(JComponent component) {
            makeFullWidthHeight(component, CONTROL_HEIGHT);
        }

        private void makeFullWidthHeight(JComponent component, int height) {
            component.setAlignmentX(Component.LEFT_ALIGNMENT);
            component.setPreferredSize(new Dimension(0, height));
            component.setMinimumSize(new Dimension(0, height));
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        }

        private Component space(int height) {
            return Box.createVerticalStrut(height);
        }

        //functionalities
        private void addButtonActions() {
            addVertexButton.addActionListener(e -> addVertex());
            renameButton.addActionListener(e -> renameVertex());
            deleteVertexButton.addActionListener(e -> deleteVertex());
            finishVerticesButton.addActionListener(e -> finishAddingVertices());
            removeEdgeButton.addActionListener(e -> removeEdge());
            visualizeButton.addActionListener(e -> visualizeDFS());
            graphPanel.setEdgeCreationHandler(this::addConnection);
        }

        private void finishAddingVertices() {
            if (graph.vertexCount() == 0) {
                showError("Please add at least one vertex before continuing.");
                return;
            }
            setVertexEntryMode(false);
        }

        private void setVertexEntryMode(boolean enabled) {
            vertexSection.setVisible(enabled);
            edgeSection.setVisible(!enabled);
            dfsSettingsSection.setVisible(!enabled);
            graphPanel.setEdgeCreationEnabled(!enabled);
            revalidate();
            repaint();
        }

        private void showError(String message) {
            JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }

        private String typeName() {
            return directed ? DIRECTED : UNDIRECTED;
        }

        private String readVertexInput() {
            String value = vertexInput.getText().trim();
            if (value.isEmpty()) {
                showError("Please enter a vertex name.");
                return null;
            }
            if (value.equals(NONE)) {
                showError("\"" + NONE + "\" is reserved for the empty target choice.\nPlease use another name.");
                return null;
            }
            return value;
        }

        private void addVertex() {
            String value = readVertexInput();
            if (value == null) {
                return;
            }
            if (!graph.addVertex(value)) {
                showError("A vertex named \"" + value + "\" already exists.");
                return;
            }

            vertexInput.setText("");
            refreshAll();
            vertexList.setSelectedValue(value, true);
            graphPanel.highlightVertex(value);
        }

        private void renameVertex() {
            String selected = vertexList.getSelectedValue();
            if (selected == null) {
                showError("Please select a vertex to rename.");
                return;
            }

            String value = readVertexInput();
            if (value == null) {
                return;
            }
            if (value.equals(selected)) {
                vertexInput.setText("");
                return;
            }
            if (!graph.renameVertex(selected, value)) {
                showError("A vertex named \"" + value + "\" already exists.");
                return;
            }

            vertexInput.setText("");
            refreshAll();
            vertexList.setSelectedValue(value, true);
            graphPanel.highlightVertex(value);
        }

        private void deleteVertex() {
            String selected = vertexList.getSelectedValue();
            if (selected == null) {
                showError("Please select a vertex to delete.");
                return;
            }

            graph.removeVertex(selected);
            refreshAll();
            graphPanel.highlightVertex(null);
        }

        private void addConnection(String from, String to) {
            if (!graph.addEdge(from, to)) {
                showError("That connection already exists in this graph.");
                return;
            }

            refreshAll();
            edgeList.setSelectedValue(graph.getEdges().get(graph.edgeCount() - 1), true);
            graphPanel.setTraversalEdge(null, null);
        }

        private void removeEdge() {
            Graph.Edge selected = edgeList.getSelectedValue();
            if (selected == null) {
                showError("Please select an edge to remove.");
                return;
            }

            graph.removeEdge(selected.from, selected.to);
            refreshAll();
            graphPanel.setTraversalEdge(null, null);
        }

        private void visualizeDFS() {
            if (graph.vertexCount() == 0) {
                showError("The graph is empty. Please add at least one vertex first.");
                return;
            }

            String start = (String) startCombo.getSelectedItem();
            if (start == null || !graph.hasVertex(start)) {
                showError("Please choose a starting vertex.");
                return;
            }

            String selectedTarget = (String) targetCombo.getSelectedItem();
            String target = (selectedTarget == null || selectedTarget.equals(NONE)) ? "" : selectedTarget;
            if (!target.isEmpty() && !graph.hasVertex(target)) {
                showError("Target \"" + target + "\" is not in the graph.");
                return;
            }

            DFSVisualizerFrame.GraphDFSVisualizerFrame dfsFrame =
                new DFSVisualizerFrame.GraphDFSVisualizerFrame(graph, directed, start, target);
            dfsFrame.setVisible(true);

            dispose();
        }

        //keeps every list and combo in sync with the graph
        private void refreshAll() {
            vertexModel.clear();
            for (String value : graph.getVertices()) {
                vertexModel.addElement(value);
            }

            edgeModel.clear();
            for (Graph.Edge edge : graph.getEdges()) {
                edgeModel.addElement(edge);
            }

            fillCombo(startCombo, false);
            fillCombo(targetCombo, true);

            typeLabel.setText("Graph Type: " + typeName());
            statusLabel.setText("Vertices: " + graph.vertexCount() + "    |    Edges: " + graph.edgeCount());
            graphPanel.setGraph(graph);

            revalidate();
            repaint();
        }

        private void fillCombo(JComboBox<String> combo, boolean includeNone) {
            String previous = (String) combo.getSelectedItem();

            combo.removeAllItems();
            if (includeNone) {
                combo.addItem(NONE);
            }
            for (String value : graph.getVertices()) {
                combo.addItem(value);
            }

            if (includeNone) {
                boolean keep = previous != null
                    && (previous.equals(NONE) || graph.hasVertex(previous));
                combo.setSelectedItem(keep ? previous : NONE);
            } else if (!graph.getVertices().isEmpty()) {
                boolean keep = previous != null && graph.hasVertex(previous);
                combo.setSelectedItem(keep ? previous : graph.getVertices().get(0));
            }
        }


        //a general graph that can be either directed or undirected
        static class Graph {

            //one connection between two vertices (a loop is allowed)
            class Edge {

                String from;
                String to;

                Edge(String from, String to) {
                    this.from = from;
                    this.to = to;
                }

                boolean isLoop() {
                    return from.equals(to);
                }

                boolean connects(String a, String b) {
                    if (from.equals(a) && to.equals(b)) {
                        return true;
                    }
                    return !directed && from.equals(b) && to.equals(a);
                }

                String label(boolean isDirected) {
                    return from + (isDirected ? " -> " : " -- ") + to;
                }
            }

            private final List<String> vertices = new ArrayList<>();
            private final List<Edge> edges = new ArrayList<>();
            private boolean directed;

            Graph(boolean directed) {
                this.directed = directed;
            }

            boolean isDirected() {
                return directed;
            }

            //switching to undirected removes duplicate reversed connections
            void setDirected(boolean directed) {
                if (this.directed == directed) {
                    return;
                }
                this.directed = directed;

                if (!directed) {
                    for (int i = 0; i < edges.size(); i++) {
                        Edge edge = edges.get(i);
                        for (int j = i + 1; j < edges.size(); j++) {
                            Edge other = edges.get(j);
                            if (edge.from.equals(other.to) && edge.to.equals(other.from)) {
                                edges.remove(j);
                                j--;
                            }
                        }
                    }
                }
            }

            List<String> getVertices() {
                return Collections.unmodifiableList(vertices);
            }

            List<Edge> getEdges() {
                return Collections.unmodifiableList(edges);
            }

            int vertexCount() {
                return vertices.size();
            }

            int edgeCount() {
                return edges.size();
            }

            boolean hasVertex(String value) {
                return vertices.contains(value);
            }

            boolean addVertex(String value) {
                if (value == null || value.trim().isEmpty() || hasVertex(value)) {
                    return false;
                }
                vertices.add(value);
                return true;
            }

            //renames a vertex and updates every edge that touches it
            boolean renameVertex(String oldValue, String newValue) {
                if (oldValue == null || newValue == null || newValue.trim().isEmpty()
                        || !hasVertex(oldValue) || hasVertex(newValue)) {
                    return false;
                }

                vertices.set(vertices.indexOf(oldValue), newValue);
                for (Edge edge : edges) {
                    if (edge.from.equals(oldValue)) {
                        edge.from = newValue;
                    }
                    if (edge.to.equals(oldValue)) {
                        edge.to = newValue;
                    }
                }
                return true;
            }

            //removing a vertex also removes all of its connections
            boolean removeVertex(String value) {
                if (!vertices.remove(value)) {
                    return false;
                }
                edges.removeIf(edge -> edge.from.equals(value) || edge.to.equals(value));
                return true;
            }

            boolean hasEdge(String from, String to) {
                if (!hasVertex(from) || !hasVertex(to)) {
                    return false;
                }
                for (Edge edge : edges) {
                    if (edge.connects(from, to)) {
                        return true;
                    }
                }
                return false;
            }

            boolean addEdge(String from, String to) {
                if (hasEdge(from, to)) {
                    return false;
                }
                edges.add(new Edge(from, to));
                return true;
            }

            boolean removeEdge(String from, String to) {
                for (Edge edge : edges) {
                    if (edge.connects(from, to)) {
                        edges.remove(edge);
                        return true;
                    }
                }
                return false;
            }

            //neighbors are returned in the order the edges were added
            //so that a DFS always visits them consistently
            List<String> neighbors(String value) {
                List<String> found = new ArrayList<>();
                for (Edge edge : edges) {
                    if (edge.from.equals(value) && !found.contains(edge.to)) {
                        found.add(edge.to);
                    }
                    if (!directed && edge.to.equals(value) && !found.contains(edge.from)) {
                        found.add(edge.from);
                    }
                }
                return found;
            }

            void clear() {
                vertices.clear();
                edges.clear();
            }
        }
    }
}

//one node of the tree
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

//keeps the content centered and enables scrolling when it exceeds the available height
class CenteredScrollPanel extends JPanel implements Scrollable {

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return getParent() instanceof JViewport
            && getParent().getHeight() > getPreferredSize().height;
    }
}

class RoundedPanel extends JPanel {

    private final int radius;

    public RoundedPanel(LayoutManager layout, Color background, int radius) {
        super(layout);
        this.radius = radius;
        setBackground(background);
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
    }
}

class RoundedTextField extends JTextField {

    private final Color borderColor;
    private final Color focusColor;
    private final int radius;

    public RoundedTextField(Color borderColor, Color focusColor, int radius) {
        this.borderColor = borderColor;
        this.focusColor = focusColor;
        this.radius = radius;

        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.setColor(hasFocus() ? focusColor : borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}

class RoundedButton extends JButton {

    private final int radius;

    public RoundedButton(String text, int radius) {
        super(text);
        this.radius = radius;
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color base = getBackground();
        Color fill = base;
        Color text = getForeground();

        if (!isEnabled()) {
            fill = blend(base, Color.WHITE, 0.6f);
            text = blend(base, Color.WHITE, 0.2f);
        } else if (getModel().isPressed()) {
            fill = base.darker();
        } else if (getModel().isRollover()) {
            fill = blend(base, Color.WHITE, 0.15f);
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.setColor(text);
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    private static Color blend(Color from, Color to, float amount) {
        int red = Math.round(from.getRed() + (to.getRed() - from.getRed()) * amount);
        int green = Math.round(from.getGreen() + (to.getGreen() - from.getGreen()) * amount);
        int blue = Math.round(from.getBlue() + (to.getBlue() - from.getBlue()) * amount);
        return new Color(red, green, blue);
    }
}

class RoundedComboBox extends JComboBox<String> {

    private final Color borderColor;
    private final Color focusColor;
    private final int radius;

    public RoundedComboBox(String[] items, Color borderColor, Color focusColor, int radius) {
        super(items);
        this.borderColor = borderColor;
        this.focusColor = focusColor;
        this.radius = radius;

        setUI(new FlatComboBoxUI(focusColor, borderColor));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.setColor(hasFocus() ? focusColor : borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}

class FlatComboBoxUI extends BasicComboBoxUI {

    private final Color arrowColor;
    private final Color popupBorderColor;

    public FlatComboBoxUI(Color arrowColor, Color popupBorderColor) {
        this.arrowColor = arrowColor;
        this.popupBorderColor = popupBorderColor;
    }

    @Override
    protected JButton createArrowButton() {
        JButton button = new JButton() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(arrowColor);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.fillPolygon(new int[]{cx - 5, cx + 5, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3);
                g2.dispose();
            }
        };
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusable(false);
        return button;
    }

    @Override
    protected ComboPopup createPopup() {
        BasicComboPopup popup = (BasicComboPopup) super.createPopup();
        popup.setBorder(BorderFactory.createLineBorder(popupBorderColor));
        return popup;
    }

    @Override
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
    }

    @Override
    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
        ListCellRenderer<Object> renderer = comboBox.getRenderer();
        Component c = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
        c.setFont(comboBox.getFont());
        c.setForeground(comboBox.getForeground());
        c.setBackground(comboBox.getBackground());
        currentValuePane.paintComponent(g, c, comboBox, bounds.x, bounds.y, bounds.width, bounds.height, false);
    }
}

class RoundedBorder extends AbstractBorder {

    private final Color color;
    private final int radius;

    public RoundedBorder(Color color, int radius) {
        this.color = color;
        this.radius = radius;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(1, 1, 1, 1);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(1, 1, 1, 1);
        return insets;
    }
}