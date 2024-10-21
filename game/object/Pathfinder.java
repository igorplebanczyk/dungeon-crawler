package game.object;

import game.GameState;
import game.object.entity.EntityType;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Pathfinder {
    private final Dungeon dungeon;

    public Pathfinder(Dungeon dungeon) {
        this.dungeon = dungeon;
    }

    // Node class to hold information for A* algorithm
    private static class Node implements Comparable<Node> {
        public Point point;
        public Node parent;
        public double g; // Cost from start to current node
        public double h; // Heuristic cost to goal
        public double f; // Total cost (g + h)

        public Node(Point point, Node parent, double g, double h) {
            this.point = point;
            this.parent = parent;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }

    private boolean isValidTileForPathfinder(int x, int y) {
        if (GameState.isBulldozerMode()) {
            return x >= 0 && x < dungeon.getWidth() && y >= 0 && y < dungeon.getHeight() && (dungeon.getMap()[y][x].getType() == EntityType.FLOOR || dungeon.getMap()[y][x].getType() == EntityType.WALL);
        } else {
            return x >= 0 && x < dungeon.getWidth() && y >= 0 && y < dungeon.getHeight() && dungeon.getMap()[y][x].getType() == EntityType.FLOOR;
        }
    }

    private java.util.List<Point> reconstructPath(Node currentNode) {
        java.util.List<Point> path = new LinkedList<>();
        while (currentNode != null) {
            path.add(currentNode.point);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path); // Reverse the path to get from start to goal
        return path;
    }

    private double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private java.util.List<Point> getNeighbors(Point point) {
        java.util.List<Point> neighbors = new ArrayList<>();
        int x = point.x;
        int y = point.y;
        if (isValidTileForPathfinder(x - 1, y)) neighbors.add(new Point(x - 1, y)); // Left
        if (isValidTileForPathfinder(x + 1, y)) neighbors.add(new Point(x + 1, y)); // Right
        if (isValidTileForPathfinder(x, y - 1)) neighbors.add(new Point(x, y - 1)); // Up
        if (isValidTileForPathfinder(x, y + 1)) neighbors.add(new Point(x, y + 1)); // Down
        return neighbors;
    }

    // A* algorithm
    public List<Point> findPath(int startX, int startY, int goalX, int goalY) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Point> closedSet = new HashSet<>();

        Node startNode = new Node(new Point(startX, startY), null, 0, heuristic(startX, startY, goalX, goalY));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            // Check if we reached the goal
            if (currentNode.point.x == goalX && currentNode.point.y == goalY) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.point);

            // Explore neighbors
            for (Point neighbor : getNeighbors(currentNode.point)) {
                if (closedSet.contains(neighbor) || !isValidTileForPathfinder(neighbor.x, neighbor.y)) {
                    continue; // Skip if already visited or not valid
                }

                double tentativeG = currentNode.g + 1; // Assuming cost of 1 for each step
                Node neighborNode = new Node(neighbor, currentNode, tentativeG, heuristic(neighbor.x, neighbor.y, goalX, goalY));

                // Check if this path to the neighbor is better
                if (!openSet.contains(neighborNode) || tentativeG < neighborNode.g) {
                    openSet.add(neighborNode);
                }
            }
        }

        return null; // No path found
    }
}
