import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreePanel extends JPanel {

    private final Color DARK = new Color(255, 246, 249);
    private final Color BLUE = new Color(236, 120, 160);
    private final Color YELLOW = new Color(222, 150, 40);
    private final Color GREEN = new Color(86, 160, 110);
    private final Color GRAY = new Color(190, 150, 170);
    private final Color PURPLE = new Color(160, 110, 200);
    private final Color ORANGE = new Color(200, 70, 90);
    private final Color EDGE = new Color(240, 170, 196);
    private final Color WHITE = Color.WHITE;

    //drawing limits
    private static final int MARGIN = 20;
    private static final int MAX_RADIUS = 20;
    private static final int MIN_RADIUS = 8;

    //tree and highlight state
    private TreeNode root;
    private TreeNode currentNode;
    private TreeNode foundNode;
    private TreeNode startNode;
    private String targetValue = "";

    //nodes in left-to-right order and their depth rows
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
        g2.setStroke(new BasicStroke(2f));

        //recompute the layout on every refresh
        order.clear();
        depths.clear();
        deepestRow = 0;
        computeLayout(root, 0);

        //spacing for one column per node & one row per level
        int columns = order.size();
        int rows = deepestRow + 1;

        double xStep = (getWidth() - 2.0 * MARGIN) / columns;
        double yStep = (getHeight() - 2.0 * MARGIN) / rows;

        //shrink nodes to fit within the min and max size
        int radius = (int) Math.min(MAX_RADIUS, Math.min(xStep, yStep) / 2 - 2);
        radius = Math.max(MIN_RADIUS, radius);

        g2.setFont(new Font("Arial", Font.BOLD, Math.max(9, radius * 2 / 3)));

        drawEdges(g2, root, xStep, yStep);
        drawNodes(g2, root, xStep, yStep, radius);
    }

    //pixel position of a node from its column and depth
    private Point positionOf(TreeNode node, double xStep, double yStep) {
        int index = order.indexOf(node);
        int x = (int) (MARGIN + (index + 0.5) * xStep);
        int y = (int) (MARGIN + (depths.get(index) + 0.5) * yStep);
        return new Point(x, y);
    }

    //draws the lines from each node to its children
    private void drawEdges(Graphics2D g2, TreeNode node, double xStep, double yStep) {
        if (node == null) return;

        Point p = positionOf(node, xStep, yStep);
        g2.setColor(EDGE);

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

     //draws each node and its value
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

        //centering the value inside the circle
        FontMetrics fm = g2.getFontMetrics();
        int textX = p.x - fm.stringWidth(node.value) / 2;
        int textY = p.y + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(node.value, textX, textY);

        drawTags(g2, node, p, radius);

        drawNodes(g2, node.left, xStep, yStep, radius);
        drawNodes(g2, node.right, xStep, yStep, radius);
    }

    private void drawTags(Graphics2D g2, TreeNode node, Point p, int radius) {
        boolean isStart = (node == startNode);
        boolean isTarget = !targetValue.isEmpty() && node.value.equals(targetValue);
        if (!isStart && !isTarget) return;

        Font original = g2.getFont();
        g2.setFont(original.deriveFont(Font.BOLD, Math.max(8f, radius * 0.55f)));
        FontMetrics fm = g2.getFontMetrics();

        int y = p.y + radius + fm.getAscent() + 2;

        if (isStart) {
            g2.setColor(PURPLE);
            g2.drawString("START", p.x - fm.stringWidth("START") / 2, y);
            y += fm.getHeight() - 2;
        }
        if (isTarget) {
            g2.setColor(ORANGE);
            g2.drawString("TARGET", p.x - fm.stringWidth("TARGET") / 2, y);
        }

        g2.setFont(original);
    }

    public void setStartNode(TreeNode node) {
        startNode = node;
        repaint();
    }

    public void setTargetValue(String value) {
        targetValue = (value == null) ? "" : value.trim();
        repaint();
    }

    public void setRoot(TreeNode root) {
        this.root = root;
        repaint();
    }

    public void setCurrentNode(TreeNode node) {
        currentNode = node;
        repaint();
    }

    //used by the DFS animation
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

    //assigns each node a unique column position to prevent overlapping in the tree
    private void computeLayout(TreeNode node, int row) {
        if (node == null) return;

        computeLayout(node.left, row + 1);

        order.addElement(node);
        depths.addElement(row);
        deepestRow = Math.max(deepestRow, row);

        computeLayout(node.right, row + 1);
    }



    //draws the graph and the current DFS highlight state
    public static class GraphPanel extends JPanel {

        private final Color BACKGROUND = new Color(255, 246, 249);
        private final Color BLUE = new Color(236, 120, 160);
        private final Color YELLOW = new Color(222, 150, 40);
        private final Color GREEN = new Color(86, 160, 110);
        private final Color GRAY = new Color(190, 150, 170);
        private final Color PURPLE = new Color(160, 110, 200);
        private final Color ORANGE = new Color(200, 70, 90);
        private final Color EDGE = new Color(240, 170, 196);
        private final Color WHITE = Color.WHITE;

        private static final int PADDING = 48;
        private static final int MAX_RADIUS = 20;
        private static final int MIN_RADIUS = 9;

        private TreeBuilderFrame.GraphBuilderFrame.Graph graph;
        private String currentVertex;
        private String foundVertex;
        private String startVertex;
        private String targetValue = "";
        private String edgeFrom;
        private String edgeTo;

        private final Set<String> visited = new LinkedHashSet<>();
        private final Map<String, Point> positions = new HashMap<>();
        private boolean edgeCreationEnabled;
        private String dragFrom;
        private Point dragPoint;
        private java.util.function.BiConsumer<String, String> edgeCreationHandler;

        public GraphPanel() {
            setBackground(BACKGROUND);
            setPreferredSize(new Dimension(1000, 350));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (edgeCreationEnabled && SwingUtilities.isLeftMouseButton(e)) {
                        dragFrom = vertexAt(e.getPoint());
                        dragPoint = e.getPoint();
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragFrom != null) {
                        String dragTo = vertexAt(e.getPoint());
                        if (dragTo != null && !dragFrom.equals(dragTo) && edgeCreationHandler != null) {
                            edgeCreationHandler.accept(dragFrom, dragTo);
                        }
                    }
                    dragFrom = null;
                    dragPoint = null;
                    repaint();
                }
            });
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragFrom != null) {
                        dragPoint = e.getPoint();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (graph == null || graph.vertexCount() == 0) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            List<String> vertices = graph.getVertices();
            int count = vertices.size();
            int radius = vertexRadius(count);

            computePositions(vertices);
            drawEdges(g2, radius);
            drawPendingEdge(g2);
            drawVertices(g2, vertices, radius);
        }

        private int vertexRadius(int count) {
            return Math.max(MIN_RADIUS,
                Math.min(MAX_RADIUS, Math.min(getWidth(), getHeight()) / (count + 3)));
        }

        private String vertexAt(Point point) {
            if (graph == null || graph.vertexCount() == 0) {
                return null;
            }

            List<String> vertices = graph.getVertices();
            computePositions(vertices);
            int hitRadius = vertexRadius(vertices.size()) + 4;
            for (String vertex : vertices) {
                Point center = positions.get(vertex);
                if (center.distance(point) <= hitRadius) {
                    return vertex;
                }
            }
            return null;
        }

        private void drawPendingEdge(Graphics2D g2) {
            if (dragFrom == null || dragPoint == null) {
                return;
            }
            Point from = positions.get(dragFrom);
            if (from != null) {
                g2.setColor(GREEN);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(from.x, from.y, dragPoint.x, dragPoint.y);
                g2.setStroke(new BasicStroke(1f));
            }
        }

        //vertices are placed evenly around a circle
        private void computePositions(List<String> vertices) {
            positions.clear();

            int count = vertices.size();
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int radiusX = Math.max(1, getWidth() / 2 - PADDING);
            int radiusY = Math.max(1, getHeight() / 2 - PADDING);

            if (count == 1) {
                positions.put(vertices.get(0), new Point(centerX, centerY));
                return;
            }

            double startAngle = (count == 2) ? 0 : -Math.PI / 2;
            for (int i = 0; i < count; i++) {
                double angle = startAngle + i * 2 * Math.PI / count;
                int x = (int) Math.round(centerX + radiusX * Math.cos(angle));
                int y = (int) Math.round(centerY + radiusY * Math.sin(angle));
                positions.put(vertices.get(i), new Point(x, y));
            }
        }

        private void drawEdges(Graphics2D g2, int radius) {
            for (TreeBuilderFrame.GraphBuilderFrame.Graph.Edge edge : graph.getEdges()) {
                Point from = positions.get(edge.from);
                Point to = positions.get(edge.to);
                if (from == null || to == null) {
                    continue;
                }

                boolean active = edge.connects(edgeFrom == null ? "\u0000" : edgeFrom,
                    edgeTo == null ? "\u0000" : edgeTo);

                g2.setColor(active ? GREEN : EDGE);
                g2.setStroke(new BasicStroke(active ? 3f : 2f));

                if (edge.isLoop()) {
                    drawLoop(g2, from, radius);
                } else if (graph.isDirected()) {
                    drawDirectedEdge(g2, from, to, radius);
                } else {
                    g2.drawLine(from.x, from.y, to.x, to.y);
                }
            }
            g2.setStroke(new BasicStroke(1f));
        }

        private void drawDirectedEdge(Graphics2D g2, Point from, Point to, int radius) {
            double dx = to.x - from.x;
            double dy = to.y - from.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) {
                return;
            }

            double ux = dx / distance;
            double uy = dy / distance;

            int startX = (int) Math.round(from.x + ux * radius);
            int startY = (int) Math.round(from.y + uy * radius);
            int tipX = (int) Math.round(to.x - ux * (radius + 4));
            int tipY = (int) Math.round(to.y - uy * (radius + 4));

            g2.drawLine(startX, startY, tipX, tipY);

            int arrowLength = 11;
            int baseX = (int) Math.round(tipX - ux * arrowLength);
            int baseY = (int) Math.round(tipY - uy * arrowLength);
            int halfWidth = 5;

            g2.fillPolygon(
                new int[]{tipX, baseX - (int) (uy * halfWidth), baseX + (int) (uy * halfWidth)},
                new int[]{tipY, baseY + (int) (ux * halfWidth), baseY - (int) (ux * halfWidth)},
                3);
        }

        private void drawLoop(Graphics2D g2, Point p, int radius) {
            int size = radius * 2;
            g2.drawOval(p.x - size / 2, p.y - radius - size, size, size);
        }

        private void drawVertices(Graphics2D g2, List<String> vertices, int radius) {
            g2.setFont(new Font("Arial", Font.BOLD, Math.max(9, radius)));

            for (String value : vertices) {
                Point p = positions.get(value);

                Color color = BLUE;
                if (value.equals(foundVertex)) {
                    color = GREEN;
                } else if (value.equals(currentVertex)) {
                    color = YELLOW;
                } else if (visited.contains(value)) {
                    color = GRAY;
                }

                g2.setColor(color);
                g2.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

                g2.setColor(WHITE);
                g2.drawOval(p.x - radius, p.y - radius, radius * 2, radius * 2);

                FontMetrics fm = g2.getFontMetrics();
                int textX = p.x - fm.stringWidth(value) / 2;
                int textY = p.y + (fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(value, textX, textY);

                drawTags(g2, value, p, radius);
            }
        }

        private void drawTags(Graphics2D g2, String value, Point p, int radius) {
            boolean isStart = value.equals(startVertex);
            boolean isTarget = !targetValue.isEmpty() && value.equals(targetValue);
            if (!isStart && !isTarget) {
                return;
            }

            Font original = g2.getFont();
            g2.setFont(original.deriveFont(Font.BOLD, Math.max(8f, radius * 0.55f)));

            FontMetrics fm = g2.getFontMetrics();
            int y = p.y + radius + fm.getAscent() + 2;

            if (isStart) {
                g2.setColor(PURPLE);
                g2.drawString("START", p.x - fm.stringWidth("START") / 2, y);
                y += fm.getHeight() - 2;
            }
            if (isTarget) {
                g2.setColor(ORANGE);
                g2.drawString("TARGET", p.x - fm.stringWidth("TARGET") / 2, y);
            }

            g2.setFont(original);
        }

        public void setGraph(TreeBuilderFrame.GraphBuilderFrame.Graph graph) {
            this.graph = graph;
            repaint();
        }

        public void setEdgeCreationEnabled(boolean enabled) {
            edgeCreationEnabled = enabled;
            dragFrom = null;
            dragPoint = null;
            setCursor(Cursor.getPredefinedCursor(
                enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        public void setEdgeCreationHandler(java.util.function.BiConsumer<String, String> handler) {
            edgeCreationHandler = handler;
        }

        public void setStartVertex(String value) {
            startVertex = value;
            repaint();
        }

        public void setTargetValue(String value) {
            targetValue = (value == null) ? "" : value.trim();
            repaint();
        }

        public void setCurrentVertex(String value) {
            currentVertex = value;
            repaint();
        }

        public void setTraversalEdge(String from, String to) {
            edgeFrom = from;
            edgeTo = to;
            repaint();
        }

        public void markVisited(String value) {
            if (value != null) {
                visited.add(value);
            }
            repaint();
        }

        public void markFound(String value) {
            foundVertex = value;
            repaint();
        }

        public void reset() {
            visited.clear();
            currentVertex = null;
            foundVertex = null;
            edgeFrom = null;
            edgeTo = null;
            repaint();
        }

        //used by the builder when a vertex is selected for editing
        public void highlightVertex(String value) {
            setCurrentVertex(value);
        }

        //kept for symmetry with the tree preview helper
        public List<String> getVertexOrder() {
            return new ArrayList<>(positions.keySet());
        }
    }
}
