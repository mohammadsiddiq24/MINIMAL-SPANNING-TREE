import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Do you want to: \n(1) load graph from CSV file? \n(2) generate a random graph? \nEnter 1 or 2: ");
            int choice = scanner.nextInt();
            int[][] graph = null;

            if (choice == 1) {
                PathInput pathInput = new PathInput();
                String filePath = pathInput.getPathFromUser();
                graph = readGraphFromCSV(filePath);
            } else if (choice == 2) {
                System.out.print("Enter the number of vertices for the random graph: ");
                int vertices = scanner.nextInt();
                graph = generateRandomMatrix(vertices);
            } else {
                LOGGER.severe("Invalid choice. Exiting program.");
                return;
            }

            if (graph == null) {
                LOGGER.severe("Graph could not be loaded or generated.");
                return;
            }

            System.out.print("Choose MST algorithm: \n(1) Prim's Algorithm \n(2) Kruskal's Algorithm \nEnter 1 or 2: ");
            int algoChoice = scanner.nextInt();

            MSTResult mstResult = null;
            if (algoChoice == 1) {
                mstResult = primsMST(graph);
            } else if (algoChoice == 2) {
                mstResult = kruskalsMST(graph);
            } else {
                LOGGER.severe("Invalid choice. Exiting program.");
                return;
            }

            LOGGER.info("Complete Adjacency Matrix:");
            printMatrix(graph);

            LOGGER.info("Minimum Spanning Tree (MST) Adjacency Matrix:");
            printMatrix(mstResult.mstGraph);

            GraphDisplay display = new GraphDisplay(graph, mstResult.mstGraph, mstResult.totalWeight);
            display.displayGraphs();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An error occurred while reading the CSV file", e);
        }
    }

    public static MSTResult primsMST(int[][] graph) {
        int vertices = graph.length;
        boolean[] inMST = new boolean[vertices];
        int[] key = new int[vertices];
        int[] parent = new int[vertices];
        int[][] mstGraph = new int[vertices][vertices];
        int totalWeight = 0;

        Arrays.fill(key, Integer.MAX_VALUE);
        key[0] = 0;
        parent[0] = -1;

        for (int count = 0; count < vertices - 1; count++) {
            int u = minKey(key, inMST, vertices);
            inMST[u] = true;

            for (int v = 0; v < vertices; v++) {
                if (graph[u][v] != 0 && !inMST[v] && graph[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = graph[u][v];
                }
            }
        }

        for (int i = 1; i < vertices; i++) {
            int u = parent[i];
            int v = i;
            mstGraph[u][v] = graph[u][v];
            mstGraph[v][u] = graph[u][v];
            totalWeight += graph[u][v];
        }

        return new MSTResult(mstGraph, totalWeight);
    }

    private static int minKey(int[] key, boolean[] inMST, int vertices) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int v = 0; v < vertices; v++) {
            if (!inMST[v] && key[v] < min) {
                min = key[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    public static MSTResult kruskalsMST(int[][] graph) {
        int vertices = graph.length;
        int[][] mstGraph = new int[vertices][vertices];
        int totalWeight = 0;

        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            for (int j = i + 1; j < vertices; j++) {
                if (graph[i][j] != 0) {
                    edges.add(new Edge(i, j, graph[i][j]));
                }
            }
        }

        Collections.sort(edges);

        UnionFind uf = new UnionFind(vertices);

        for (Edge edge : edges) {
            int u = edge.u;
            int v = edge.v;

            if (uf.find(u) != uf.find(v)) {
                uf.union(u, v);
                mstGraph[u][v] = edge.weight;
                mstGraph[v][u] = edge.weight;
                totalWeight += edge.weight;
            }
        }

        return new MSTResult(mstGraph, totalWeight);
    }

    public static int[][] readGraphFromCSV(String filePath) throws IOException {
        LOGGER.info("Reading graph from: " + filePath);
        BufferedReader br = null;
        int[][] graph = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line;
            int row = 0;

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (graph == null) {
                    graph = new int[values.length][values.length];
                }
                for (int col = 0; col < values.length; col++) {
                    graph[row][col] = Integer.parseInt(values[col].trim());
                }
                row++;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file", e);
            throw e;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Error parsing number", e);
            throw new IOException("Invalid number format in CSV file", e);
        } finally {
            if (br != null) {
                br.close();
            }
        }
        LOGGER.info("Graph successfully read.");
        return graph;
    }

    public static int[][] generateRandomMatrix(int vertices) {
        Random rand = new Random();
        int[][] matrix = new int[vertices][vertices];

        // Create a random spanning tree to ensure the graph is connected
        List<Integer> nodes = new ArrayList<>();
        for (int i = 0; i < vertices; i++) {
            nodes.add(i);
        }
        Collections.shuffle(nodes, rand);

        for (int i = 1; i < vertices; i++) {
            int weight = rand.nextInt(100) + 1; // Random weight between 1 and 100
            matrix[nodes.get(i)][nodes.get(i - 1)] = weight;
            matrix[nodes.get(i - 1)][nodes.get(i)] = weight;
        }

        // Add additional random edges to increase complexity
        int extraEdges = rand.nextInt(vertices * (vertices - 1) / 2 - (vertices - 1)); // Random number of extra edges
        for (int i = 0; i < extraEdges; i++) {
            int u = rand.nextInt(vertices);
            int v = rand.nextInt(vertices);
            if (u != v && matrix[u][v] == 0) {
                int weight = rand.nextInt(100) + 1;
                matrix[u][v] = weight;
                matrix[v][u] = weight;
            }
        }

        LOGGER.info("Random graph generated.");
        return matrix;
    }

    public static void printMatrix(int[][] graph) {
        int vertices = graph.length;
        System.out.print("   ");
        for (int i = 0; i < vertices; ++i) {
            System.out.print(" " + i);
        }
        System.out.println();

        for (int i = 0; i < vertices; ++i) {
            System.out.print(i + " |");
            for (int j = 0; j < vertices; ++j) {
                System.out.print(" " + graph[i][j]);
            }
            System.out.println();
        }
    }
}

class Edge implements Comparable<Edge> {
    int u, v, weight;

    Edge(int u, int v, int weight) {
        this.u = u;
        this.v = v;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return u == edge.u && v == edge.v && weight == edge.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(u, v, weight);
    }
}

class UnionFind {
    private int[] parent, rank;

    public UnionFind(int size) {
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int u) {
        if (parent[u] != u) {
            parent[u] = find(parent[u]);
        }
        return parent[u];
    }

    public void union(int u, int v) {
        int rootU = find(u);
        int rootV = find(v);
        if (rootU != rootV) {
            if (rank[rootU] > rank[rootV]) {
                parent[rootV] = rootU;
            } else if (rank[rootU] < rank[rootV]) {
                parent[rootU] = rootV;
            } else {
                parent[rootV] = rootU;
                rank[rootU]++;
            }
        }
    }
}

class MSTResult {
    int[][] mstGraph;
    int totalWeight;

    public MSTResult(int[][] mstGraph, int totalWeight) {
        this.mstGraph = mstGraph;
        this.totalWeight = totalWeight;
    }
}

class PathInput {

    public String getPathFromUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the file path: ");
        return scanner.nextLine();
    }
}

class GraphDisplay {
    private final int[][] inputGraph;
    private final int[][] mstGraph;
    private final int mstTotalWeight;
    private JFrame frame;
    private GraphPanel graphPanel;
    private JPanel panel;
    private JButton switchButton;
    private boolean showMST = false;
    private int[] posX;
    private int[] posY;
    private Color[] nodeColors;

    public GraphDisplay(int[][] inputGraph, int[][] mstGraph, int mstTotalWeight) {
        this.inputGraph = inputGraph;
        this.mstGraph = mstGraph;
        this.mstTotalWeight = mstTotalWeight;
    }

    public void displayGraphs() {
        frame = new JFrame("Graph Display");
        frame.setSize(1500, 750);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(new BorderLayout());
        setNodePositions(inputGraph.length);
        nodeColors = generateNodeColors(inputGraph.length);

        switchButton = new JButton("Switch to MST");
        switchButton.addActionListener(e -> {
            showMST = !showMST;
            switchButton.setText(showMST ? "Switch to Input Graph" : "Switch to MST");
            updateGraphDisplay();
        });

        // Initially show the input graph
        graphPanel = new GraphPanel(inputGraph, "Input Graph", posX, posY, nodeColors);
        panel.add(graphPanel, BorderLayout.CENTER);
        panel.add(switchButton, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void updateGraphDisplay() {
        panel.remove(graphPanel);
        if (showMST) {
            graphPanel = new GraphPanel(mstGraph, "Minimum Spanning Tree (MST)", posX, posY, nodeColors, mstTotalWeight, true);
        } else {
            graphPanel = new GraphPanel(inputGraph, "Input Graph", posX, posY, nodeColors);
        }
        panel.add(graphPanel, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    private void setNodePositions(int vertices) {
        posX = new int[vertices];
        posY = new int[vertices];
        Random rand = new Random();
        int padding = 50; // Padding from the edges
        int panelWidth = 1500; // Width of the panel to center the graph
        int panelHeight = 750; // Height of the panel

        for (int i = 0; i < vertices; i++) {
            posX[i] = rand.nextInt(panelWidth - 2 * padding) + padding;
            posY[i] = rand.nextInt(panelHeight - 2 * padding) + padding;
        }
    }

    public Color[] generateNodeColors(int count) {
        Color[] colors = new Color[count];
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            colors[i] = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        }
        return colors;
    }
}

class GraphPanel extends JPanel {
    private final int[][] graph;
    private final String title;
    private final int[] posX;
    private final int[] posY;
    private final Color[] nodeColors;
    private final int totalWeight;
    private final boolean animate;
    private javax.swing.Timer timer; // Explicitly use javax.swing.Timer
    private int currentEdgeIndex;
    private List<Edge> edges;
    private int animStep;
    private static final int MAX_ANIM_STEPS = 100; // Number of steps to animate each edge

    public GraphPanel(int[][] graph, String title, int[] posX, int[] posY, Color[] nodeColors) {
        this(graph, title, posX, posY, nodeColors, 0, false);
    }

    public GraphPanel(int[][] graph, String title, int[] posX, int[] posY, Color[] nodeColors, int totalWeight) {
        this(graph, title, posX, posY, nodeColors, totalWeight, false);
    }

    public GraphPanel(int[][] graph, String title, int[] posX, int[] posY, Color[] nodeColors, int totalWeight, boolean animate) {
        this.graph = graph;
        this.title = title;
        this.posX = posX;
        this.posY = posY;
        this.nodeColors = nodeColors;
        this.totalWeight = totalWeight;
        this.animate = animate;
        this.currentEdgeIndex = 0;
        this.animStep = 0;
        if (animate) {
            edges = new ArrayList<>();
            for (int i = 0; i < graph.length; i++) {
                for (int j = i + 1; j < graph[i].length; j++) {
                    if (graph[i][j] > 0) {
                        edges.add(new Edge(i, j, graph[i][j]));
                    }
                }
            }
            Collections.sort(edges);
            timer = new javax.swing.Timer(10, e -> animateEdges()); // Set interval to 10 ms for smooth animation
            timer.start();
        }
    }

    private void animateEdges() {
        if (currentEdgeIndex < edges.size()) {
            if (animStep < MAX_ANIM_STEPS) {
                animStep++;
            } else {
                animStep = 0;
                currentEdgeIndex++;
            }
            repaint();
        } else {
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setStroke(new BasicStroke(3)); // Increased edge thickness
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(title, 20, 20);

        // Draw edges
        for (int i = 0; i < graph.length; i++) {
            for (int j = i + 1; j < graph[i].length; j++) {
                if (graph[i][j] > 0 && (!animate || edges.indexOf(new Edge(i, j, graph[i][j])) < currentEdgeIndex)) {
                    g2d.setColor(Color.BLACK);
                    g2d.drawLine(posX[i], posY[i], posX[j], posY[j]);
                    g2d.setColor(Color.RED);
                    g2d.drawString(Integer.toString(graph[i][j]), (posX[i] + posX[j]) / 2, (posY[i] + posY[j]) / 2);
                }
            }
        }

        // Draw animated edge
        if (animate && currentEdgeIndex < edges.size()) {
            Edge currentEdge = edges.get(currentEdgeIndex);
            int x1 = posX[currentEdge.u];
            int y1 = posY[currentEdge.u];
            int x2 = posX[currentEdge.v];
            int y2 = posY[currentEdge.v];
            int xCurrent = x1 + (x2 - x1) * animStep / MAX_ANIM_STEPS;
            int yCurrent = y1 + (y2 - y1) * animStep / MAX_ANIM_STEPS;
            g2d.setColor(Color.BLACK);
            g2d.drawLine(x1, y1, xCurrent, yCurrent);
            g2d.setColor(Color.RED);
            g2d.drawString(Integer.toString(currentEdge.weight), (x1 + x2) / 2, (y1 + y2) / 2);
        }

        // Draw nodes
        for (int i = 0; i < posX.length; i++) {
            g2d.setColor(nodeColors[i]);
            g2d.fillOval(posX[i] - 15, posY[i] - 15, 30, 30); // Increased node size
            g2d.setColor(Color.BLACK);
            g2d.drawOval(posX[i] - 15, posY[i] - 15, 30, 30);
            g2d.drawString(Integer.toString(i), posX[i] - 10, posY[i] + 5);
        }

        // Draw total weight if MST
        if (totalWeight > 0) {
            g2d.setColor(Color.BLACK);
            g2d.drawString("Total Weight: " + totalWeight, 20, 40);
        }
    }
}
