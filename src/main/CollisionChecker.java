package main;

import entity.Entity;
import ghost.Ghost;

public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    /**
     * Check if Pacman can move in the given direction from the given grid position.
     * Pacman cannot enter walls, outside, ghost house, or ghost door.
     */
    public boolean canMovePacman(int col, int row, String direction) {
        int nextCol = col + Entity.getDx(direction);
        int nextRow = row + Entity.getDy(direction);

        // Tunnel wrapping
        if (nextCol < 0) nextCol = gp.maxScreenCol - 1;
        if (nextCol >= gp.maxScreenCol) nextCol = 0;

        // Boundary check
        if (nextRow < 0 || nextRow >= gp.maxScreenRow) return false;

        int tileType = gp.tileManager.mapTileNumber[nextCol][nextRow];

        // Pacman can move on: dot(2), power pellet(3), empty path(5)
        return tileType == 2 || tileType == 3 || tileType == 5;
    }

    /**
     * Check if a Ghost can move in the given direction from the given grid position.
     * Ghost movement depends on its current state.
     */
    public boolean canMoveGhost(int col, int row, String direction, int ghostState) {
        int nextCol = col + Entity.getDx(direction);
        int nextRow = row + Entity.getDy(direction);

        // Tunnel wrapping
        if (nextCol < 0) nextCol = gp.maxScreenCol - 1;
        if (nextCol >= gp.maxScreenCol) nextCol = 0;

        // Boundary check
        if (nextRow < 0 || nextRow >= gp.maxScreenRow) return false;

        int tileType = gp.tileManager.mapTileNumber[nextCol][nextRow];

        // Walls and outside always block
        if (tileType == 0 || tileType == 1) return false;

        // Ghost door: only passable when exiting house or eaten (returning)
        if (tileType == 6) {
            return ghostState == Ghost.EXITING_HOUSE || ghostState == Ghost.EATEN;
        }

        // Ghost house interior: only passable when in house, exiting, or eaten
        if (tileType == 4) {
            return ghostState == Ghost.IN_HOUSE || ghostState == Ghost.EXITING_HOUSE || ghostState == Ghost.EATEN;
        }

        // Paths, dots, pellets are passable
        return true;
    }
}
