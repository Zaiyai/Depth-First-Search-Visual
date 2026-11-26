import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeIterativeDeepeningFrame extends JFrame {

    private final Color DARK = new Color(255, 228, 236);
    private final Color TEXT = new Color(122, 52, 82);
    private final Color GREEN = new Color(86, 160, 110);
    private final Color YELLOW = new Color(222, 150, 40);
    private final Color GRAY = new Color(190, 150, 170);
    private final Color RED = new Color(200, 70, 90);
    private final Color TEAL = new Color(200, 90, 130);
    private final Color WHITE = Color.WHITE;

    private final TreeNode root;
    private final TreeNode startNode;
    private final String target;
    private final boolean isBST;
    private final List<TreeIterativeDeepeningSearch.Step> steps = new ArrayList<>();
    private List<TreeNode> shortestPath = new ArrayList<>();

    private int stepIndex;
    private boolean targetFound;
    private TreePanel treePanel;
    private JLabel currentNodeLabel;
    private JLabel iterationLabel;
    private JLabel traversalLabel;

    public TreeIterativeDeepeningFrame(TreeNode root, TreeNode startNode, String target, boolean isBST) {
        this.root = root;
        this.startNode = startNode;
        this.target = target.trim();
        this.isBST = isBST;

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

        JLabel typeLabel = createLabel("Tree Type: " + (isBST ? "Binary Search Tree" : "Binary Tree")
                + "   |   Goal: fewest steps to \"" + this.target + "\"", 16);
        typeLabel.setFont(new Font("Georgia", Font.BOLD | Font.ITALIC, 16));
        typeLabel.setForeground(TEAL);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(DARK);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(16));
        titlePanel.add(typeLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        treePanel = new TreePanel();
        treePanel.setRoot(root);
        treePanel.setStartNode(startNode);
        treePanel.setTargetValue(this.target);
        treePanel.setPreferredSize(new Dimension(850, 350));
        mainPanel.add(treePanel, BorderLayout.CENTER);

        mainPanel.add(buildBottomPanel(), BorderLayout.SOUTH);
        add(mainPanel);
        resetTraversal();
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(DARK);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        currentNodeLabel = createLabel("", 15);
        iterationLabel = createLabel("", 14);
        traversalLabel = createLabel("", 14);
        bottomPanel.add(currentNodeLabel);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(iterationLabel);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(traversalLabel);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 3));
        legendPanel.setBackground(DARK);
        legendPanel.add(createLegend("Processing", YELLOW));
        legendPanel.add(createLegend("Visited", GRAY));
        legendPanel.add(createLegend("Shortest path", GREEN));
        bottomPanel.add(legendPanel);

        JButton playButton = createButton("PLAY", GREEN);
        JButton resetButton = createButton("RESET", GRAY);
        JButton backButton = createButton("BACK", GRAY);
        JButton exitButton = createButton("EXIT", RED);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(DARK);
        buttonPanel.add(playButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(backButton);
        buttonPanel.add(exitButton);
        bottomPanel.add(buttonPanel);

        playButton.addActionListener(e -> nextStep());
        resetButton.addActionListener(e -> resetTraversal());
        backButton.addActionListener(e -> {
            new TreeBuilderFrame(root, isBST, startNode.value, target, TreeBuilderFrame.ITERATIVE_SEARCH);
            dispose();
        });
        exitButton.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(Main::new);
        });

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

    private void loadSearchSteps() {
        TreeIterativeDeepeningSearch.Result result =
                TreeIterativeDeepeningSearch.search(root, startNode, target);
        steps.addAll(result.steps);
        shortestPath = result.shortestPath;
    }

    private void nextStep() {
        if (targetFound) {
            return;
        }
        if (stepIndex >= steps.size()) {
            treePanel.setCurrentNode(null);
            currentNodeLabel.setForeground(RED);
            currentNodeLabel.setText("Target \"" + target + "\" is unreachable from \"" + startNode.value + "\".");
            return;
        }

        TreeIterativeDeepeningSearch.Step step = steps.get(stepIndex);
        if (step.newIteration) {
            treePanel.reset();
        } else if (stepIndex > 0) {
            treePanel.markVisited(steps.get(stepIndex - 1).node);
        }

        stepIndex++;
        treePanel.setCurrentNode(step.node);
        currentNodeLabel.setForeground(YELLOW);
        currentNodeLabel.setText("Currently Processing: " + step.node.value);
        iterationLabel.setText("Iteration " + step.iteration + "   |   Depth limit: " + step.depthLimit
                + "   |   Depth reached: " + step.depth);
        updateTraversalLabel(step.iteration);

        if (step.found) {
            targetFound = true;
            int moves = shortestPath.size() - 1;
            int visits = stepIndex - firstIndexOf(step.iteration);
            treePanel.markFound(step.node);
            treePanel.setCurrentNode(null);
            treePanel.setShortestPath(shortestPath);
            currentNodeLabel.setForeground(GREEN);
            currentNodeLabel.setText("Target \"" + target + "\" found in " + moves
                    + " move(s): " + pathValues(shortestPath));
            iterationLabel.setText("Shortest path confirmed after " + step.iteration
                    + " iteration(s). Winning iteration processed " + visits + " node visit(s).");
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

    private String pathValues(List<TreeNode> path) {
        List<String> values = new ArrayList<>();
        for (TreeNode node : path) {
            values.add(node.value);
        }
        return String.join(" -> ", values);
    }

    private void resetTraversal() {
        stepIndex = 0;
        targetFound = false;
        treePanel.reset();
        currentNodeLabel.setForeground(TEXT);
        currentNodeLabel.setText("Currently Processing: None");
        iterationLabel.setText("Ready — depth limit starts at 0 and increases each iteration.");
        updateTraversalLabel(1);
    }

    private void updateTraversalLabel(int iteration) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < stepIndex; i++) {
            TreeIterativeDeepeningSearch.Step step = steps.get(i);
            if (step.iteration != iteration) {
                continue;
            }
            if (text.length() > 0) {
                text.append(" -> ");
            }
            text.append(step.node.value);
        }
        traversalLabel.setText("Iteration " + iteration + " visits: " + text);
    }
}

class TreeIterativeDeepeningSearch {

    static class Step {
        final TreeNode node;
        final int iteration;
        final int depthLimit;
        final int depth;
        final boolean found;
        final boolean newIteration;

        Step(TreeNode node, int iteration, int depthLimit, int depth,
             boolean found, boolean newIteration) {
            this.node = node;
            this.iteration = iteration;
            this.depthLimit = depthLimit;
            this.depth = depth;
            this.found = found;
            this.newIteration = newIteration;
        }
    }

    static class Result {
        final List<Step> steps;
        final List<TreeNode> shortestPath;

        Result(List<Step> steps, List<TreeNode> shortestPath) {
            this.steps = steps;
            this.shortestPath = shortestPath;
        }
    }

    private static class SearchEntry {
        final TreeNode node;
        final TreeNode previous;
        final int depth;

        SearchEntry(TreeNode node, TreeNode previous, int depth) {
            this.node = node;
            this.previous = previous;
            this.depth = depth;
        }
    }

    static Result search(TreeNode root, TreeNode start, String target) {
        List<Step> steps = new ArrayList<>();
        List<TreeNode> shortestPath = new ArrayList<>();
        if (root == null || start == null || target == null || target.trim().isEmpty()) {
            return new Result(steps, shortestPath);
        }

        TreeNode goal = findNode(root, target.trim());
        if (goal == null) {
            return new Result(steps, shortestPath);
        }

        Map<TreeNode, TreeNode> parents = new HashMap<>();
        Deque<TreeNode> nodes = new ArrayDeque<>();
        nodes.push(root);
        int nodeCount = 0;
        while (!nodes.isEmpty()) {
            TreeNode node = nodes.pop();
            nodeCount++;
            if (node.left != null) {
                parents.put(node.left, node);
                nodes.push(node.left);
            }
            if (node.right != null) {
                parents.put(node.right, node);
                nodes.push(node.right);
            }
        }

        for (int limit = 0; limit < nodeCount; limit++) {
            int iteration = limit + 1;
            Map<TreeNode, TreeNode> reachedFrom = new HashMap<>();
            Deque<SearchEntry> stack = new ArrayDeque<>();
            stack.push(new SearchEntry(start, null, 0));

            while (!stack.isEmpty()) {
                SearchEntry entry = stack.pop();
                reachedFrom.put(entry.node, entry.previous);
                boolean found = entry.node == goal;
                boolean newIteration = steps.isEmpty()
                        || steps.get(steps.size() - 1).iteration != iteration;
                steps.add(new Step(entry.node, iteration, limit, entry.depth, found, newIteration));

                if (found) {
                    for (TreeNode node = goal; node != null; node = reachedFrom.get(node)) {
                        shortestPath.add(0, node);
                        if (node == start) {
                            break;
                        }
                    }
                    return new Result(steps, shortestPath);
                }

                if (entry.depth == limit) {
                    continue;
                }
                List<TreeNode> neighbors = new ArrayList<>(3);
                TreeNode parent = parents.get(entry.node);
                if (parent != null) {
                    neighbors.add(parent);
                }
                if (entry.node.left != null) {
                    neighbors.add(entry.node.left);
                }
                if (entry.node.right != null) {
                    neighbors.add(entry.node.right);
                }

                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    TreeNode neighbor = neighbors.get(i);
                    if (neighbor != entry.previous) {
                        stack.push(new SearchEntry(neighbor, entry.node, entry.depth + 1));
                    }
                }
            }
        }

        return new Result(steps, shortestPath);
    }

    private static TreeNode findNode(TreeNode node, String value) {
        if (node == null) {
            return null;
        }
        if (node.value.equals(value)) {
            return node;
        }
        TreeNode found = findNode(node.left, value);
        return found != null ? found : findNode(node.right, value);
    }
}
