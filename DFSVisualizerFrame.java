import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class DFSVisualizerFrame extends JFrame {

    private final Color DARK = new Color(255, 228, 236);
    private final Color TEXT = new Color(122, 52, 82);
    private final Color BLUE = new Color(236, 120, 160);
    private final Color GREEN = new Color(86, 160, 110);
    private final Color YELLOW = new Color(222, 150, 40);
    private final Color GRAY = new Color(190, 150, 170);
    private final Color RED = new Color(200, 70, 90);
    private final Color TEAL = new Color(200, 90, 130);
    private final Color WHITE = Color.WHITE;

    //labels, tree and buttons on the screen
    private JLabel currentNodeLabel;
    private JLabel traversalLabel;
    private TreePanel treePanel;

    private JButton playButton;
    private JButton resetButton;
    private JButton backButton;
    private JButton exitButton;

    //traversal settings and result
    private final String order;
    private final String target;
    private boolean targetFound = false;

    private final TreeNode root;
    private final TreeNode startNode;
    private final boolean isBST;

    //nodes in the order they will be visited
    private final DefaultListModel<TreeNode> visitOrder = new DefaultListModel<>();

    //number of steps played so far
    private int stepIndex = 0;

    private static class StackCellRenderer extends JPanel implements ListCellRenderer<String> {
        private final JLabel label = new JLabel();
        private final Map<String, Color> colors = new HashMap<>();
        private final Random random = new Random();
        private final Color textColor;
        private Color pillColor;

        StackCellRenderer(Color textColor) {
            super(new BorderLayout());
            this.textColor = textColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

            label.setFont(new Font("Georgia", Font.PLAIN, 14));
            label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 8));
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list,
                                                      String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            label.setText((index == 0 ? "TOP  |  " : "") + value);
            label.setForeground(textColor);
            pillColor = colors.get(value);
            if (pillColor == null) {
                pillColor = randomPastel();
                colors.put(value, pillColor);
            }
            return this;
        }

        private Color randomPastel() {
            float hue = random.nextFloat();
            float saturation = 0.25f + random.nextFloat() * 0.2f;
            return Color.getHSBColor(hue, saturation, 0.98f);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(pillColor);
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, getHeight(), getHeight());
            g2.dispose();
        }
    }

    public DFSVisualizerFrame(TreeNode root, TreeNode startNode, String order, String target, boolean isBST) {

        this.root = root;
        this.startNode = startNode;
        this.isBST = isBST;
        this.order = order;
        this.target = target.trim();

        buildVisitOrder(startNode);

        //window settings
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

        //screen title
        JLabel title = new JLabel(
                "Depth-First Search Visualization",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Segoe Script", Font.BOLD, 24));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel treeTypeLabel = createLabel(
                "Tree Type: " + (isBST ? "Binary Search Tree" : "Binary Tree"),
                18
        );
        treeTypeLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 18));
        treeTypeLabel.setForeground(TEAL);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(DARK);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(20));
        titlePanel.add(treeTypeLabel);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        //setting the tree in the middle
        treePanel = new TreePanel();
        treePanel.setRoot(root);
        treePanel.setStartNode(startNode);
        treePanel.setTargetValue(this.target);
        treePanel.setPreferredSize(new Dimension(850, 350));

        mainPanel.add(treePanel, BorderLayout.CENTER);

        //status, legend and buttons at the bottom
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

        //current node and traversal path
        currentNodeLabel = createLabel("", 15);
        traversalLabel = createLabel("", 14);

        bottomPanel.add(currentNodeLabel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(traversalLabel);
        bottomPanel.add(Box.createVerticalStrut(5));

        //color legend
        JPanel legendPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 12, 3)
        );

        legendPanel.setBackground(DARK);

        legendPanel.add(createLegend("Not visited", BLUE));
        legendPanel.add(createLegend("Processing", YELLOW));
        legendPanel.add(createLegend("Visited", GRAY));

        //only show the target color when a target was given
        if (!target.isEmpty()) {
            legendPanel.add(createLegend("Target found", GREEN));
        }

        bottomPanel.add(legendPanel);

        //control buttons
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

    //centered label
    private JLabel createLabel(String text, int size) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font("Georgia", Font.PLAIN, size + 1)
        );

        label.setForeground(TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        return label;
    }

    //flat & colored button
    private JButton createButton(String text, Color color) {

        JButton button = new RoundedButton(text, 24);

        button.setFont(
                new Font("Georgia", Font.BOLD, 13)
        );

        button.setForeground(WHITE);
        button.setBackground(color);
        button.setPreferredSize(
                new Dimension(110, 35)
        );

        return button;
    }

    //colored legend entry
    private JLabel createLegend(String text, Color color) {

        JLabel label = new JLabel("● " + text);

        label.setFont(
                new Font("Georgia", Font.BOLD, 12)
        );

        label.setForeground(color);

        return label;
    }

    //functionalities
    private void addButtonActions() {

        //PLAY
        playButton.addActionListener(e -> nextStep());
        //RESET
        resetButton.addActionListener(e -> resetTraversal());
        //BACK
        backButton.addActionListener(e -> {
            new TreeBuilderFrame(root, isBST, startNode.value, target, order);
            dispose();

        });

        //EXIT
        exitButton.addActionListener(e -> {
            dispose();

            SwingUtilities.invokeLater(() -> {
                new Main();
            });

        });
    }

    //logics here
    // recursive walk that fills the visit order (pre, in or post-order)
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

    //plays one step of the traversal
    private void nextStep() {

        //stop once the target is found
        if (targetFound) {
            return;
        }

        //previous node is now visited
        if (stepIndex > 0) {
            treePanel.markVisited(
                    visitOrder.get(stepIndex - 1)
            );
        }

        //all nodes done
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

        //process the next node
        TreeNode node = visitOrder.get(stepIndex);

        stepIndex++;

        treePanel.setCurrentNode(node);

        currentNodeLabel.setForeground(YELLOW);

        currentNodeLabel.setText(
                "Currently Processing: " + node.value
        );

        updateTraversalLabel();

        // check if this node is the target
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

    //clears the steps and highlights
    private void resetTraversal() {

        stepIndex = 0;
        targetFound = false;

        treePanel.reset();

        currentNodeLabel.setForeground(TEXT);

        currentNodeLabel.setText(
                "Currently Processing: None"
        );

        updateTraversalLabel();
    }

    //shows the nodes visited
    private void updateTraversalLabel() {

        String text = "";

        for (int i = 0; i < stepIndex; i++) {

            if (i > 0) {
                text += " -> ";
            }

            text += visitOrder.get(i).value;
        }

        traversalLabel.setText(
                order + " Traversal: " + text
        );
    }



    //steps through a DFS on a general graph, cycle-safe and one vertex at a time
    public static class GraphDFSVisualizerFrame extends JFrame {

        private final Color DARK = new Color(255, 228, 236);
        private final Color TEXT = new Color(122, 52, 82);
        private final Color BLUE = new Color(236, 120, 160);
        private final Color GREEN = new Color(86, 160, 110);
        private final Color YELLOW = new Color(222, 150, 40);
        private final Color GRAY = new Color(190, 150, 170);
        private final Color RED = new Color(200, 70, 90);
        private final Color TEAL = new Color(200, 90, 130);
        private final Color WHITE = Color.WHITE;

        private final TreeBuilderFrame.GraphBuilderFrame.Graph graph;
        private final String start;
        private final String target;
        private final boolean directed;

        //vertices in the order DFS reaches them, with the edge used to reach each one
        private final List<String> visitOrder = new ArrayList<>();
        private final Map<String, String> reachedFrom = new HashMap<>();

        private int stepIndex = 0;
        private boolean targetFound = false;

        private TreePanel.GraphPanel graphPanel;
        private JLabel currentVertexLabel;
        private JLabel traversalLabel;
        private final DefaultListModel<String> stackModel = new DefaultListModel<>();

        private JButton playButton;
        private JButton resetButton;
        private JButton backButton;
        private JButton exitButton;

        public GraphDFSVisualizerFrame(TreeBuilderFrame.GraphBuilderFrame.Graph graph, boolean directed, String start, String target) {
            this.graph = graph;
            this.directed = directed;
            this.start = start;
            this.target = (target == null) ? "" : target.trim();

            buildVisitOrder();

            setTitle("DFS Algorithm");
            setSize(1100, 650);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(true);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(DARK);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

            JLabel title = new JLabel("Depth-First Search Visualization", SwingConstants.CENTER);
            title.setFont(new Font("Segoe Script", Font.BOLD, 24));
            title.setForeground(TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel typeLabel = createLabel("Graph Type: " + (directed ? "Directed Graph" : "Undirected Graph"), 18);
            typeLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 18));
            typeLabel.setForeground(TEAL);

            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.setBackground(DARK);
            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(20));
            titlePanel.add(typeLabel);
            mainPanel.add(titlePanel, BorderLayout.NORTH);

            graphPanel = new TreePanel.GraphPanel();
            graphPanel.setGraph(graph);
            graphPanel.setStartVertex(start);
            graphPanel.setTargetValue(this.target);
            graphPanel.setPreferredSize(new Dimension(850, 350));

            JPanel visualizationPanel = new JPanel(new BorderLayout(12, 0));
            visualizationPanel.setBackground(DARK);
            visualizationPanel.add(graphPanel, BorderLayout.CENTER);
            visualizationPanel.add(buildStackPanel(), BorderLayout.EAST);
            mainPanel.add(visualizationPanel, BorderLayout.CENTER);

            mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);
            add(mainPanel);
            addButtonActions();
            resetTraversal();
        }

        private JPanel buildStackPanel() {
            JPanel stackPanel = new JPanel(new BorderLayout());
            stackPanel.setBackground(DARK);
            stackPanel.setPreferredSize(new Dimension(180, 0));
            stackPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TEAL),
                "DFS Stack (top first)",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Georgia", Font.BOLD, 13),
                TEXT
            ));

            JList<String> stackList = new JList<>(stackModel);
            stackList.setBackground(new Color(255, 240, 245));
            stackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            stackList.setFocusable(false);
            stackList.setCellRenderer(new StackCellRenderer(TEXT));

            JScrollPane scrollPane = new JScrollPane(stackList);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(240, 190, 208)));
            scrollPane.getViewport().setBackground(new Color(255, 240, 245));
            stackPanel.add(scrollPane, BorderLayout.CENTER);
            return stackPanel;
        }

        //design
        private JPanel buildBottomPanel() {
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(DARK);
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

            currentVertexLabel = createLabel("", 15);
            traversalLabel = createLabel("", 14);

            bottomPanel.add(currentVertexLabel);
            bottomPanel.add(Box.createVerticalStrut(5));
            bottomPanel.add(traversalLabel);
            bottomPanel.add(Box.createVerticalStrut(5));

            JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 3));
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

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
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
            label.setFont(new Font("Georgia", Font.PLAIN, size + 1));
            label.setForeground(TEXT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            return label;
        }

        private JButton createButton(String text, Color color) {
            JButton button = new RoundedButton(text, 24);
            button.setFont(new Font("Georgia", Font.BOLD, 13));
            button.setForeground(WHITE);
            button.setBackground(color);
            button.setPreferredSize(new Dimension(110, 35));
            return button;
        }

        private JLabel createLegend(String text, Color color) {
            JLabel label = new JLabel("● " + text);
            label.setFont(new Font("Georgia", Font.BOLD, 12));
            label.setForeground(color);
            return label;
        }

        private void addButtonActions() {
            playButton.addActionListener(e -> nextStep());
            resetButton.addActionListener(e -> resetTraversal());

            backButton.addActionListener(e -> {
                new TreeBuilderFrame.GraphBuilderFrame(graph, directed, start, target);
                dispose();
            });

            exitButton.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(() -> new Main());
            });
        }

        //logic
        //recursive DFS with a visited set so cycles cannot cause infinite recursion
        private void buildVisitOrder() {
            dfs(start, new HashSet<>());
        }

        private void dfs(String value, Set<String> visited) {
            if (value == null || visited.contains(value) || !graph.hasVertex(value)) {
                return;
            }

            visited.add(value);
            visitOrder.add(value);

            for (String neighbor : graph.neighbors(value)) {
                if (!visited.contains(neighbor)) {
                    reachedFrom.put(neighbor, value);
                    dfs(neighbor, visited);
                }
            }
        }

        private void nextStep() {
            if (targetFound) {
                return;
            }

            if (stepIndex >= visitOrder.size()) {
                graphPanel.setCurrentVertex(null);
                graphPanel.setTraversalEdge(null, null);

                if (target.isEmpty()) {
                    currentVertexLabel.setForeground(GREEN);
                    currentVertexLabel.setText("Traversal complete!");
                } else {
                    currentVertexLabel.setForeground(RED);
                    currentVertexLabel.setText("Target \"" + target + "\" was not found.");
                }
                return;
            }

            String vertex = visitOrder.get(stepIndex);

            if (stepIndex > 0) {
                graphPanel.markVisited(visitOrder.get(stepIndex - 1));
                graphPanel.setTraversalEdge(reachedFrom.get(vertex), vertex);
            }

            stackModel.insertElementAt(vertex, 0);
            stepIndex++;
            graphPanel.setCurrentVertex(vertex);

            currentVertexLabel.setForeground(YELLOW);
            currentVertexLabel.setText("Currently Processing: " + vertex);
            updateTraversalLabel();

            if (!target.isEmpty() && vertex.equals(target)) {
                targetFound = true;
                graphPanel.markFound(vertex);
                currentVertexLabel.setForeground(GREEN);
                currentVertexLabel.setText("Target \"" + target + "\" found at step " + stepIndex + "!");
            }
        }

        private void resetTraversal() {
            stepIndex = 0;
            targetFound = false;

            stackModel.clear();
            graphPanel.reset();
            currentVertexLabel.setForeground(TEXT);
            currentVertexLabel.setText("Currently Processing: None");
            updateTraversalLabel();
        }

        private void updateTraversalLabel() {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < stepIndex; i++) {
                if (i > 0) {
                    text.append(" -> ");
                }
                text.append(visitOrder.get(i));
            }
            traversalLabel.setText("DFS Path: " + text);
        }
    }

    //runs depth-limited DFS over and over with a bigger limit until the target is reached.
    //the first iteration that reaches it uses the fewest possible edges, so it is the shortest path.
    public static class GraphIterativeDeepeningFrame extends JFrame {

        private final Color DARK = new Color(255, 228, 236);
        private final Color TEXT = new Color(122, 52, 82);
        private final Color GREEN = new Color(86, 160, 110);
        private final Color YELLOW = new Color(222, 150, 40);
        private final Color GRAY = new Color(190, 150, 170);
        private final Color RED = new Color(200, 70, 90);
        private final Color TEAL = new Color(200, 90, 130);
        private final Color WHITE = Color.WHITE;

        private final TreeBuilderFrame.GraphBuilderFrame.Graph graph;
        private final String start;
        private final String target;
        private final boolean directed;

        private final List<IterativeDeepeningSearch.Step> steps = new ArrayList<>();
        private List<String> shortestPath = new ArrayList<>();
        private int stepIndex = 0;
        private boolean targetFound = false;

        private TreePanel.GraphPanel graphPanel;
        private JLabel currentVertexLabel;
        private JLabel traversalLabel;
        private JLabel iterationLabel;
        private final DefaultListModel<String> stackModel = new DefaultListModel<>();

        private JButton playButton;
        private JButton resetButton;
        private JButton backButton;
        private JButton exitButton;

        public GraphIterativeDeepeningFrame(TreeBuilderFrame.GraphBuilderFrame.Graph graph,
                                            boolean directed, String start, String target) {
            this.graph = graph;
            this.directed = directed;
            this.start = start;
            this.target = (target == null) ? "" : target.trim();

            loadSearchSteps();

            setTitle("Iterative Deepening DFS");
            setSize(1100, 650);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(true);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(DARK);
            mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

            JLabel title = new JLabel("Iterative Deepening DFS Visualization", SwingConstants.CENTER);
            title.setFont(new Font("Segoe Script", Font.BOLD, 24));
            title.setForeground(TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel typeLabel = createLabel("Graph Type: " + (directed ? "Directed Graph" : "Undirected Graph")
                    + "   |   Goal: fewest steps to \"" + this.target + "\"", 18);
            typeLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 16));
            typeLabel.setForeground(TEAL);

            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
            titlePanel.setBackground(DARK);
            titlePanel.add(title);
            titlePanel.add(Box.createVerticalStrut(20));
            titlePanel.add(typeLabel);
            mainPanel.add(titlePanel, BorderLayout.NORTH);

            graphPanel = new TreePanel.GraphPanel();
            graphPanel.setGraph(graph);
            graphPanel.setStartVertex(start);
            graphPanel.setTargetValue(this.target);
            graphPanel.setPreferredSize(new Dimension(850, 350));

            JPanel visualizationPanel = new JPanel(new BorderLayout(12, 0));
            visualizationPanel.setBackground(DARK);
            visualizationPanel.add(graphPanel, BorderLayout.CENTER);
            visualizationPanel.add(buildStackPanel(), BorderLayout.EAST);
            mainPanel.add(visualizationPanel, BorderLayout.CENTER);

            mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);
            add(mainPanel);
            addButtonActions();
            resetTraversal();
        }

        private JPanel buildStackPanel() {
            JPanel stackPanel = new JPanel(new BorderLayout());
            stackPanel.setBackground(DARK);
            stackPanel.setPreferredSize(new Dimension(180, 0));
            stackPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(TEAL),
                "DFS Stack (top first)",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("Georgia", Font.BOLD, 13),
                TEXT
            ));

            JList<String> stackList = new JList<>(stackModel);
            stackList.setBackground(new Color(255, 240, 245));
            stackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            stackList.setFocusable(false);
            stackList.setCellRenderer(new StackCellRenderer(TEXT));

            JScrollPane scrollPane = new JScrollPane(stackList);
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(240, 190, 208)));
            scrollPane.getViewport().setBackground(new Color(255, 240, 245));
            stackPanel.add(scrollPane, BorderLayout.CENTER);
            return stackPanel;
        }

        //design
        private JPanel buildBottomPanel() {
            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(DARK);
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

            currentVertexLabel = createLabel("", 15);
            iterationLabel = createLabel("", 14);
            traversalLabel = createLabel("", 14);

            bottomPanel.add(currentVertexLabel);
            bottomPanel.add(Box.createVerticalStrut(5));
            bottomPanel.add(iterationLabel);
            bottomPanel.add(Box.createVerticalStrut(5));
            bottomPanel.add(traversalLabel);
            bottomPanel.add(Box.createVerticalStrut(5));

            JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 3));
            legendPanel.setBackground(DARK);
            legendPanel.add(createLegend("Not visited", TEAL));
            legendPanel.add(createLegend("Processing", YELLOW));
            legendPanel.add(createLegend("Visited", GRAY));
            legendPanel.add(createLegend("Shortest path", GREEN));
            bottomPanel.add(legendPanel);

            playButton = createButton("PLAY", GREEN);
            resetButton = createButton("RESET", GRAY);
            backButton = createButton("BACK", GRAY);
            exitButton = createButton("EXIT", RED);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
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
            label.setFont(new Font("Georgia", Font.PLAIN, size + 1));
            label.setForeground(TEXT);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            return label;
        }

        private JButton createButton(String text, Color color) {
            JButton button = new RoundedButton(text, 24);
            button.setFont(new Font("Georgia", Font.BOLD, 13));
            button.setForeground(WHITE);
            button.setBackground(color);
            button.setPreferredSize(new Dimension(110, 35));
            return button;
        }

        private JLabel createLegend(String text, Color color) {
            JLabel label = new JLabel("● " + text);
            label.setFont(new Font("Georgia", Font.BOLD, 12));
            label.setForeground(color);
            return label;
        }

        private void addButtonActions() {
            playButton.addActionListener(e -> nextStep());
            resetButton.addActionListener(e -> resetTraversal());

            backButton.addActionListener(e -> {
                new TreeBuilderFrame.GraphBuilderFrame(graph, directed, start, target,
                        TreeBuilderFrame.GraphBuilderFrame.ITERATIVE_DFS);
                dispose();
            });

            exitButton.addActionListener(e -> {
                dispose();
                SwingUtilities.invokeLater(() -> new Main());
            });
        }

        //logic
        private void loadSearchSteps() {
            IterativeDeepeningSearch.Result result =
                    IterativeDeepeningSearch.search(graph, start, target);
            steps.addAll(result.steps);
            shortestPath = result.shortestPath;
        }

        private void nextStep() {
            if (targetFound) {
                return;
            }

            if (stepIndex >= steps.size()) {
                graphPanel.setCurrentVertex(null);
                graphPanel.setTraversalEdge(null, null);
                currentVertexLabel.setForeground(RED);
                currentVertexLabel.setText("Target \"" + target + "\" is unreachable from \"" + start + "\".");
                return;
            }

            IterativeDeepeningSearch.Step step = steps.get(stepIndex);

            //each new iteration starts from scratch: depth limit increases by one
            if (step.newIteration) {
                graphPanel.reset();
                stackModel.clear();
            } else if (stepIndex > 0) {
                graphPanel.markVisited(steps.get(stepIndex - 1).vertex);
            }

            stackModel.insertElementAt(step.vertex, 0);
            graphPanel.setTraversalEdge(step.parent, step.vertex);
            stepIndex++;
            graphPanel.setCurrentVertex(step.vertex);

            currentVertexLabel.setForeground(YELLOW);
            currentVertexLabel.setText("Currently Processing: " + step.vertex);
            iterationLabel.setText("Iteration " + step.iteration
                    + "   |   Depth limit: " + step.depthLimit
                    + "   |   Depth reached: " + step.depth);
            updateTraversalLabel(step.iteration);

            if (step.found) {
                targetFound = true;
                int moves = shortestPath.size() - 1;
                int visits = stepIndex - firstIndexOf(step.iteration) + 1;

                graphPanel.markFound(step.vertex);
                graphPanel.setCurrentVertex(null);
                graphPanel.setShortestPath(shortestPath);

                currentVertexLabel.setForeground(GREEN);
                currentVertexLabel.setText("Target \"" + target + "\" found in " + moves
                        + " move(s): " + String.join(" -> ", shortestPath));
                iterationLabel.setText("Shortest path confirmed after " + step.iteration
                        + " iteration(s). Winning iteration processed " + visits + " vertex visit(s).");
            }
        }

        private int firstIndexOf(int iteration) {
            for (int i = 0; i < steps.size(); i++) {
                if (steps.get(i).iteration == iteration) {
                    return i;
                }
            }
            return 0;
        }

        private void resetTraversal() {
            stepIndex = 0;
            targetFound = false;

            stackModel.clear();
            graphPanel.reset();
            currentVertexLabel.setForeground(TEXT);
            currentVertexLabel.setText("Currently Processing: None");
            iterationLabel.setText("Ready — depth limit starts at 0 and increases each iteration.");
            updateTraversalLabel(1);
        }

        private void updateTraversalLabel(int iteration) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < stepIndex; i++) {
                IterativeDeepeningSearch.Step step = steps.get(i);
                if (step.iteration != iteration) {
                    continue;
                }
                if (text.length() > 0) {
                    text.append(" -> ");
                }
                text.append(step.vertex);
            }
            traversalLabel.setText("Iteration " + iteration + " visits: " + text);
        }
    }
}

//iterative deepening DFS: repeatedly run DFS with a depth limit that grows by one.
//The first iteration that reaches the target uses the fewest edges, so the path is shortest.
class IterativeDeepeningSearch {

    //one vertex visit in one depth-limited iteration
    public static class Step {
        public final String vertex;
        public final String parent;
        public final int iteration;
        public final int depthLimit;
        public final int depth;
        public final boolean found;
        public final boolean newIteration;

        Step(String vertex, String parent, int iteration, int depthLimit,
             int depth, boolean found, boolean newIteration) {
            this.vertex = vertex;
            this.parent = parent;
            this.iteration = iteration;
            this.depthLimit = depthLimit;
            this.depth = depth;
            this.found = found;
            this.newIteration = newIteration;
        }
    }

    public static class Result {
        public final List<Step> steps;
        public final List<String> shortestPath;
        public final boolean found;

        Result(List<Step> steps, List<String> shortestPath, boolean found) {
            this.steps = steps;
            this.shortestPath = shortestPath;
            this.found = found;
        }
    }

    public static Result search(TreeBuilderFrame.GraphBuilderFrame.Graph graph,
                                String start, String target) {
        List<Step> steps = new ArrayList<>();
        List<String> shortestPath = new ArrayList<>();
        boolean goalFound = false;

        //a simple shortest path never needs more than V - 1 edges
        int maxDepth = Math.max(0, graph.vertexCount() - 1);
        String goal = (target == null) ? "" : target.trim();

        for (int limit = 0; limit <= maxDepth && !goalFound; limit++) {
            Set<String> onCurrentPath = new HashSet<>();
            goalFound = depthLimited(graph, start, goal, 0, limit, onCurrentPath,
                    null, limit + 1, new ArrayList<>(), steps, shortestPath);
        }

        return new Result(steps, shortestPath, goalFound);
    }

    //depth-limited DFS. onCurrentPath prevents cycles; vertices are not globally marked
    //because another route to the same vertex may be shorter and must still be explored.
    private static boolean depthLimited(TreeBuilderFrame.GraphBuilderFrame.Graph graph,
                                        String value, String goal, int depth, int limit,
                                        Set<String> onCurrentPath, String parent, int iteration,
                                        List<String> pathSoFar, List<Step> steps,
                                        List<String> shortestPath) {
        if (value == null || !graph.hasVertex(value) || onCurrentPath.contains(value) || depth > limit) {
            return false;
        }

        List<String> path = new ArrayList<>(pathSoFar);
        path.add(value);

        boolean found = !goal.isEmpty() && value.equals(goal);
        boolean newIteration = steps.isEmpty()
                || steps.get(steps.size() - 1).iteration != iteration;

        steps.add(new Step(value, parent, iteration, limit, depth, found, newIteration));

        if (found) {
            shortestPath.addAll(path);
            return true;
        }

        onCurrentPath.add(value);
        for (String neighbor : graph.neighbors(value)) {
            if (depthLimited(graph, neighbor, goal, depth + 1, limit, onCurrentPath,
                    value, iteration, path, steps, shortestPath)) {
                return true;
            }
        }
        onCurrentPath.remove(value);
        return false;
    }
}
