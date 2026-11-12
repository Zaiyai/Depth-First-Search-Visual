import javax.swing.*;
import java.awt.*;

public class TreePanel extends JPanel {

    private final Color DARK = new Color(21, 30, 48);
    private final Color BLUE = new Color(35, 119, 210);
    private final Color YELLOW = new Color(255, 193, 7);
    private final Color GREEN = new Color(35, 180, 120);
    private final Color GRAY = new Color(110, 125, 145);
    private final Color WHITE = Color.WHITE;

    private static final int MARGIN = 20;
    private static final int MAX_RADIUS = 20;
    private static final int MIN_RADIUS = 8;

    private TreeNode root;
    private TreeNode currentNode;
    private TreeNode foundNode;

    private final DefaultListModel<TreeNode> visited = new DefaultListModel<>();
    private final DefaultListModel<TreeNode> order = new DefaultListModel<>();
    private final DefaultListModel<Integer> depths = new DefaultListModel<>();
    private int deepestRow;

    public TreePanel() {
        setBackground(DARK);
        setPreferredSize(new Dimension(1000, 350));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (root == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        order.clear();
        depths.clear();
        deepestRow = 0;
        computeLayout(root, 0);

        int columns = order.size();
        int rows = deepestRow + 1;

        double xStep = (getWidth() - 2.0 * MARGIN) / columns;
        double yStep = (getHeight() - 2.0 * MARGIN) / rows;

        int radius = (int) Math.min(MAX_RADIUS, Math.min(xStep, yStep) / 2 - 2);
        radius = Math.max(MIN_RADIUS, radius);

        g2.setFont(new Font("Arial", Font.BOLD, Math.max(9, radius * 2 / 3)));

        drawEdges(g2, root, xStep, yStep);
        drawNodes(g2, root, xStep, yStep, radius);
    }

    private Point positionOf(TreeNode node, double xStep, double yStep) {
        int index = order.indexOf(node);
        int x = (int) (MARGIN + (index + 0.5) * xStep);
        int y = (int) (MARGIN + (depths.get(index) + 0.5) * yStep);
        return new Point(x, y);
    }

    private void drawEdges(Graphics2D g2, TreeNode node, double xStep, double yStep) {
        if (node == null) return;

        Point p = positionOf(node, xStep, yStep);
        g2.setColor(WHITE);

        if (node.left != null) {
            Point c = positionOf(node.left, xStep, yStep);
            g2.drawLine(p.x, p.y, c.x, c.y);
        }
        
        if (node.right != null) {
            Point c = positionOf(node.right, xStep, yStep);
            g2.drawLine(p.x, p.y, c.x, c.y);
        }

        drawEdges(g2, node.left, xStep, yStep);
        drawEdges(g2, node.right, xStep, yStep);
    }

    private void drawNodes(Graphics2D g2, TreeNode node, double xStep, double yStep, int radius) {
        if (node == null) return;

        Point p = positionOf(node, xStep, yStep);

        Color color = BLUE;
        if (node == foundNode) {
            color = GREEN;
        } else if (node == currentNode) {
            color = YELLOW;
        } else if (visited.contains(node)) {
            color = GRAY;
        }

        g2.setColor(color);
        g2.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
        g2.setColor(WHITE);
        g2.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

        FontMetrics fm = g2.getFontMetrics();
        int textX = p.x - fm.stringWidth(node.value) / 2;
        int textY = p.y + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(node.value, textX, textY);

        drawNodes(g2, node.left, xStep, yStep, radius);
        drawNodes(g2, node.right, xStep, yStep, radius);
    }

    public void setRoot(TreeNode root) {
        this.root = root;
        repaint();
    }

    public void setCurrentNode(TreeNode node) {
        currentNode = node;
        repaint();
    }

    public void markVisited(TreeNode node) {
        if (!visited.contains(node)) {
            visited.addElement(node);
        }
        repaint();
    }

    public void markFound(TreeNode node) {
        foundNode = node;
        repaint();
    }

    public void reset() {
        visited.clear();
        currentNode = null;
        foundNode = null;
        repaint();
    }

    private void computeLayout(TreeNode node, int row) {
        if (node == null) return;

        computeLayout(node.left, row + 1);

        order.addElement(node);
        depths.addElement(row);
        deepestRow = Math.max(deepestRow, row);

        computeLayout(node.right, row + 1);
    }
}