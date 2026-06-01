package goldenroad.map;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class GridPathfinder {
    private static final int[][] DIRECTIONS = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    private static final int OPEN_CELL_SEARCH_RADIUS = 4;

    private final int tileSize;
    private final int searchPaddingTiles;

    public GridPathfinder(int tileSize) {
        this(tileSize, 10);
    }

    public GridPathfinder(int tileSize, int searchPaddingTiles) {
        this.tileSize = Math.max(1, tileSize);
        this.searchPaddingTiles = Math.max(0, searchPaddingTiles);
    }

    public List<Point> findPath(
        CollisionMap map,
        int actorWidth,
        int actorHeight,
        double startCenterX,
        double startCenterY,
        double goalCenterX,
        double goalCenterY,
        int maxVisitedNodes
    ) {
        if (map == null || !map.isLoaded() || maxVisitedNodes <= 0) {
            return Collections.emptyList();
        }

        int cellsWide = Math.max(1, (map.getWidth() + tileSize - 1) / tileSize);
        int cellsHigh = Math.max(1, (map.getHeight() + tileSize - 1) / tileSize);

        Map<Integer, Boolean> blockedCellCache = new HashMap<>();

        Cell start = findNearestOpenCell(map, centerToCell(startCenterX, cellsWide), centerToCell(startCenterY, cellsHigh), cellsWide, cellsHigh, actorWidth, actorHeight, blockedCellCache);
        Cell goal = findNearestOpenCell(map, centerToCell(goalCenterX, cellsWide), centerToCell(goalCenterY, cellsHigh), cellsWide, cellsHigh, actorWidth, actorHeight, blockedCellCache);

        if (start == null || goal == null) {
            return Collections.emptyList();
        }

        if (start.x == goal.x && start.y == goal.y) {
            return Collections.emptyList();
        }

        int minX = Math.max(0, Math.min(start.x, goal.x) - searchPaddingTiles);
        int maxX = Math.min(cellsWide - 1, Math.max(start.x, goal.x) + searchPaddingTiles);
        int minY = Math.max(0, Math.min(start.y, goal.y) - searchPaddingTiles);
        int maxY = Math.min(cellsHigh - 1, Math.max(start.y, goal.y) + searchPaddingTiles);

        PriorityQueue<Node> open = new PriorityQueue<>((a, b) -> Double.compare(a.priority, b.priority));
        Map<Integer, Double> bestCost = new HashMap<>();
        Map<Integer, Integer> parentByKey = new HashMap<>();

        int startKey = key(start.x, start.y, cellsWide);
        int goalKey = key(goal.x, goal.y, cellsWide);

        open.add(new Node(start.x, start.y, 0, heuristic(start.x, start.y, goal.x, goal.y)));
        bestCost.put(startKey, 0.0);

        Node closest = null;
        double closestHeuristic = Double.MAX_VALUE;
        int visitedNodes = 0;

        while (!open.isEmpty() && visitedNodes < maxVisitedNodes) {
            Node current = open.poll();
            int currentKey = key(current.x, current.y, cellsWide);
            double knownBest = bestCost.getOrDefault(currentKey, Double.MAX_VALUE);

            if (current.cost > knownBest + 0.001) {
                continue;
            }

            visitedNodes++;

            double currentHeuristic = heuristic(current.x, current.y, goal.x, goal.y);
            if (currentHeuristic < closestHeuristic) {
                closestHeuristic = currentHeuristic;
                closest = current;
            }

            if (currentKey == goalKey) {
                return reconstructPath(parentByKey, goalKey, startKey, cellsWide);
            }

            for (int[] direction : DIRECTIONS) {
                int nextX = current.x + direction[0];
                int nextY = current.y + direction[1];

                if (nextX < minX || nextY < minY || nextX > maxX || nextY > maxY) {
                    continue;
                }

                if (isCellBlocked(map, nextX, nextY, actorWidth, actorHeight, cellsWide, blockedCellCache)) {
                    continue;
                }

                if (direction[0] != 0 && direction[1] != 0
                        && (isCellBlocked(map, current.x + direction[0], current.y, actorWidth, actorHeight, cellsWide, blockedCellCache)
                        || isCellBlocked(map, current.x, current.y + direction[1], actorWidth, actorHeight, cellsWide, blockedCellCache))) {
                    continue;
                }

                double stepCost = direction[0] != 0 && direction[1] != 0 ? 1.414 : 1.0;
                double nextCost = current.cost + stepCost;
                int nextKey = key(nextX, nextY, cellsWide);

                if (nextCost >= bestCost.getOrDefault(nextKey, Double.MAX_VALUE)) {
                    continue;
                }

                bestCost.put(nextKey, nextCost);
                parentByKey.put(nextKey, currentKey);
                double priority = nextCost + heuristic(nextX, nextY, goal.x, goal.y);
                open.add(new Node(nextX, nextY, nextCost, priority));
            }
        }

        if (closest != null) {
            int closestKey = key(closest.x, closest.y, cellsWide);
            if (closestKey != startKey && parentByKey.containsKey(closestKey)) {
                return reconstructPath(parentByKey, closestKey, startKey, cellsWide);
            }
        }

        return Collections.emptyList();
    }

    private Cell findNearestOpenCell(
        CollisionMap map,
        int cellX,
        int cellY,
        int cellsWide,
        int cellsHigh,
        int actorWidth,
        int actorHeight,
        Map<Integer, Boolean> blockedCellCache
    ) {
        if (!isCellBlocked(map, cellX, cellY, actorWidth, actorHeight, cellsWide, blockedCellCache)) {
            return new Cell(cellX, cellY);
        }

        for (int radius = 1; radius <= OPEN_CELL_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) {
                        continue;
                    }

                    int nextX = cellX + dx;
                    int nextY = cellY + dy;

                    if (nextX < 0 || nextY < 0 || nextX >= cellsWide || nextY >= cellsHigh) {
                        continue;
                    }

                    if (!isCellBlocked(map, nextX, nextY, actorWidth, actorHeight, cellsWide, blockedCellCache)) {
                        return new Cell(nextX, nextY);
                    }
                }
            }
        }

        return null;
    }

    private boolean isCellBlocked(
        CollisionMap map,
        int cellX,
        int cellY,
        int actorWidth,
        int actorHeight,
        int cellsWide,
        Map<Integer, Boolean> blockedCellCache
    ) {
        int key = key(cellX, cellY, cellsWide);
        Boolean cached = blockedCellCache.get(key);
        if (cached != null) {
            return cached;
        }

        int x = cellX * tileSize + tileSize / 2 - actorWidth / 2;
        int y = cellY * tileSize + tileSize / 2 - actorHeight / 2;
        boolean blocked = map.isAreaSolid(x, y, actorWidth, actorHeight);
        blockedCellCache.put(key, blocked);
        return blocked;
    }

    private List<Point> reconstructPath(Map<Integer, Integer> parentByKey, int endKey, int startKey, int cellsWide) {
        List<Point> path = new ArrayList<>();
        int currentKey = endKey;

        while (currentKey != startKey) {
            path.add(keyToPoint(currentKey, cellsWide));
            Integer parentKey = parentByKey.get(currentKey);
            if (parentKey == null) {
                break;
            }
            currentKey = parentKey;
        }

        Collections.reverse(path);
        return path;
    }

    private Point keyToPoint(int key, int cellsWide) {
        int cellX = key % cellsWide;
        int cellY = key / cellsWide;
        return new Point(cellX * tileSize + tileSize / 2, cellY * tileSize + tileSize / 2);
    }

    private int centerToCell(double value, int maxCells) {
        int cell = (int) Math.floor(value / tileSize);
        if (cell < 0) {
            return 0;
        }
        if (cell >= maxCells) {
            return maxCells - 1;
        }
        return cell;
    }

    private int key(int x, int y, int cellsWide) {
        return y * cellsWide + x;
    }

    private double heuristic(int x, int y, int goalX, int goalY) {
        int dx = Math.abs(goalX - x);
        int dy = Math.abs(goalY - y);
        return Math.max(dx, dy) + (1.414 - 1.0) * Math.min(dx, dy);
    }

    private static class Cell {
        private final int x;
        private final int y;

        private Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static class Node {
        private final int x;
        private final int y;
        private final double cost;
        private final double priority;

        private Node(int x, int y, double cost, double priority) {
            this.x = x;
            this.y = y;
            this.cost = cost;
            this.priority = priority;
        }
    }
}
