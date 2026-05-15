package goldenroad.map;

import java.util.*;

/**
 * A* Pathfinder hoạt động trên lưới tile 16×16 pixel.
 *
 * <p>
 * Coordinate system:
 * <ul>
 * <li>Tile (tx, ty) = pixel (tx*TILE_SIZE, ty*TILE_SIZE) ... (tx*TILE_SIZE+15,
 * ty*TILE_SIZE+15)</li>
 * <li>Pixel center của tile (tx,ty) = (tx*TILE_SIZE + 8, ty*TILE_SIZE + 8)</li>
 * </ul>
 *
 * <p>
 * Tính năng:
 * <ul>
 * <li>8-hướng di chuyển (4 cardinal + 4 diagonal, diagonal chỉ khi cả hai
 * cardinal tự do)</li>
 * <li>Kiểm tra footprint: monster phải lọt vào tile (dựa trên width/height tính
 * bằng tile)</li>
 * <li>Kiểm tra "needs ground": tile phía dưới phải là solid/one-way để đứng
 * được</li>
 * <li>Giới hạn số node để tránh treo game (MAX_NODES)</li>
 * </ul>
 */
public class Pathfinder {

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final int TILE_SIZE = 16;
    private static final int MAX_NODES = 1024; // giới hạn node khi tìm đường

    // Cost di chuyển
    private static final int COST_CARDINAL = 10;
    private static final int COST_DIAGONAL = 14;

    // =========================================================
    // PUBLIC API
    // =========================================================

    /**
     * Tìm đường A* từ vị trí pixel (startPx, startPy) đến (goalPx, goalPy).
     *
     * @param startPx  tọa độ X pixel của điểm bắt đầu (thường là monster.getX())
     * @param startPy  tọa độ Y pixel của điểm bắt đầu (thường là monster.getY())
     * @param goalPx   tọa độ X pixel của đích (thường là player.getX())
     * @param goalPy   tọa độ Y pixel của đích
     * @param monsterW chiều rộng monster (pixel), dùng để tính footprint tile
     * @param monsterH chiều cao monster (pixel)
     * @param map      collision map
     * @return danh sách waypoint pixel [{px, py}, ...], center của mỗi tile trên
     *         đường.
     *         Trả về list rỗng nếu không tìm được đường.
     */
    public List<int[]> findPath(int startPx, int startPy,
            int goalPx, int goalPy,
            int monsterW, int monsterH,
            CollisionMap map) {

        // Chuyển sang tile coords
        int startTx = startPx / TILE_SIZE;
        int startTy = startPy / TILE_SIZE;
        int goalTx = goalPx / TILE_SIZE;
        int goalTy = goalPy / TILE_SIZE;

        // Số tile footprint monster
        int mTileW = Math.max(1, (monsterW + TILE_SIZE - 1) / TILE_SIZE);
        int mTileH = Math.max(1, (monsterH + TILE_SIZE - 1) / TILE_SIZE);

        if (startTx == goalTx && startTy == goalTy) {
            return Collections.emptyList();
        }

        // ---- A* ----
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(n -> n.f));
        Map<Long, Node> openMap = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        Node startNode = new Node(startTx, startTy, null, 0,
                heuristic(startTx, startTy, goalTx, goalTy));
        openSet.add(startNode);
        openMap.put(key(startTx, startTy), startNode);

        int iterations = 0;

        while (!openSet.isEmpty() && iterations < MAX_NODES) {
            iterations++;
            Node current = openSet.poll();
            openMap.remove(key(current.tx, current.ty));

            if (current.tx == goalTx && current.ty == goalTy) {
                return reconstructPath(current);
            }

            closedSet.add(key(current.tx, current.ty));

            for (int[] dir : DIRECTIONS) {
                int nx = current.tx + dir[0];
                int ny = current.ty + dir[1];
                boolean isDiagonal = (dir[0] != 0 && dir[1] != 0);
                long nKey = key(nx, ny);

                if (closedSet.contains(nKey))
                    continue;
                if (!isWalkable(nx, ny, mTileW, mTileH, map))
                    continue;

                // Diagonal: cả hai cardinal phải đi được (tránh "cắt góc")
                if (isDiagonal) {
                    if (!isWalkable(current.tx + dir[0], current.ty, mTileW, mTileH, map))
                        continue;
                    if (!isWalkable(current.tx, current.ty + dir[1], mTileW, mTileH, map))
                        continue;
                }

                int tentativeG = current.g + (isDiagonal ? COST_DIAGONAL : COST_CARDINAL);

                Node existing = openMap.get(nKey);
                if (existing != null && tentativeG >= existing.g)
                    continue;

                int h = heuristic(nx, ny, goalTx, goalTy);
                Node neighbor = new Node(nx, ny, current, tentativeG, tentativeG + h);

                if (existing != null)
                    openSet.remove(existing);
                openSet.add(neighbor);
                openMap.put(nKey, neighbor);
            }
        }

        // Không tìm được đường
        return Collections.emptyList();
    }

    // =========================================================
    // WALKABILITY
    // =========================================================

    /**
     * Tile (tx, ty) có thể đi được không?
     * Điều kiện:
     * 1. Không có solid tile nào trong footprint [tx..tx+mTileW-1] x
     * [ty..ty+mTileH-1]
     * 2. Có ground bên dưới footprint (solid hoặc one-way)
     */
    private boolean isWalkable(int tx, int ty, int mTileW, int mTileH, CollisionMap map) {
        // Kiểm tra body không bị chèn vào solid tile
        for (int bx = tx; bx < tx + mTileW; bx++) {
            for (int by = ty; by < ty + mTileH; by++) {
                if (map.isSolid(bx * TILE_SIZE, by * TILE_SIZE))
                    return false;
            }
        }

        // Kiểm tra có đất bên dưới (chân monster)
        int groundY = (ty + mTileH) * TILE_SIZE; // pixel y chân monster
        boolean hasGround = false;

        for (int bx = tx; bx < tx + mTileW; bx++) {
            int checkX = bx * TILE_SIZE + TILE_SIZE / 2; // center pixel
            if (map.isSolid(checkX, groundY) || map.isOneWay(checkX, groundY)) {
                hasGround = true;
                break;
            }
        }

        return hasGround;
    }

    // =========================================================
    // HEURISTIC (Octile distance)
    // =========================================================

    private int heuristic(int tx, int ty, int goalTx, int goalTy) {
        int dx = Math.abs(tx - goalTx);
        int dy = Math.abs(ty - goalTy);
        // Octile: tốt hơn Manhattan khi có diagonal movement
        return COST_CARDINAL * (dx + dy) + (COST_DIAGONAL - 2 * COST_CARDINAL) * Math.min(dx, dy);
    }

    // =========================================================
    // PATH RECONSTRUCTION
    // =========================================================

    /**
     * Đi ngược từ goal về start theo parent link,
     * trả về danh sách pixel center mỗi tile (theo thứ tự start→goal).
     */
    private List<int[]> reconstructPath(Node goal) {
        List<int[]> path = new ArrayList<>();
        Node curr = goal;

        while (curr != null) {
            int px = curr.tx * TILE_SIZE + TILE_SIZE / 2;
            int py = curr.ty * TILE_SIZE + TILE_SIZE / 2;
            path.add(new int[] { px, py });
            curr = curr.parent;
        }

        Collections.reverse(path);
        return path;
    }

    // =========================================================
    // HELPERS
    // =========================================================

    /** Encode (tx, ty) thành long key cho HashMap. */
    private static long key(int tx, int ty) {
        return ((long) tx << 32) | (ty & 0xFFFFFFFFL);
    }

    /** 8 hướng: 4 cardinal + 4 diagonal */
    private static final int[][] DIRECTIONS = {
            { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // cardinal
            { 1, 1 }, { -1, 1 }, { 1, -1 }, { -1, -1 } // diagonal
    };

    // =========================================================
    // NODE
    // =========================================================

    private static class Node {
        final int tx, ty;
        final Node parent;
        final int g; // cost from start
        final int f; // g + h

        Node(int tx, int ty, Node parent, int g, int f) {
            this.tx = tx;
            this.ty = ty;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }
    }
}