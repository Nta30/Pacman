package entity;

import main.GamePanel;

import java.awt.*;

public class Entity {

    public GamePanel gp;

    // Position (pixel-based)
    public int worldX, worldY;

    // Grid position
    public int gridX, gridY;

    // Movement
    public String direction = "left";
    public int speed = 3;

    // Animation
    public int spriteCounter = 0;

    // State
    public boolean alive = true;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public void setGridPosition(int col, int row) {
        this.gridX = col;
        this.gridY = row;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
    }

    public boolean isAtGridPosition() {
        return worldX % gp.tileSize == 0 && worldY % gp.tileSize == 0;
    }

    public void updateGridPosition() {
        gridX = worldX / gp.tileSize;
        gridY = worldY / gp.tileSize;
    }

    /**
     * Snap entity to nearest grid-aligned position.
     * Prevents misalignment when speed changes (e.g. FRIGHTENED→EATEN).
     */
    public void snapToGrid() {
        int half = gp.tileSize / 2;
        gridX = (worldX + half) / gp.tileSize;
        gridY = (worldY + half) / gp.tileSize;
        worldX = gridX * gp.tileSize;
        worldY = gridY * gp.tileSize;
    }

    public void move() {
        switch (direction) {
            case "up":    worldY -= speed; break;
            case "down":  worldY += speed; break;
            case "left":  worldX -= speed; break;
            case "right": worldX += speed; break;
        }
    }

    public void tunnelWrap() {
        int mapWidth = gp.maxScreenCol * gp.tileSize;
        if (worldX + gp.tileSize <= 0) worldX += mapWidth;
        if (worldX >= mapWidth) worldX -= mapWidth;
    }

    public static String getReverse(String dir) {
        switch (dir) {
            case "up":    return "down";
            case "down":  return "up";
            case "left":  return "right";
            case "right": return "left";
        }
        return dir;
    }

    public static int getDx(String dir) {
        if ("left".equals(dir))  return -1;
        if ("right".equals(dir)) return 1;
        return 0;
    }

    public static int getDy(String dir) {
        if ("up".equals(dir))   return -1;
        if ("down".equals(dir)) return 1;
        return 0;
    }

    public void update() {}
    public void draw(Graphics2D g2d) {}
}
