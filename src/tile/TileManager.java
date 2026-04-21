package tile;

import main.GamePanel;

import java.awt.*;
import java.io.*;

public class TileManager {
    GamePanel gp;
    public int[][] mapTileNumber;

    // Colors
    static final Color WALL_COLOR = new Color(33, 33, 222);
    static final Color WALL_HIGHLIGHT = new Color(60, 60, 255);
    static final Color DOT_COLOR = new Color(255, 183, 174);
    static final Color PATH_COLOR = Color.BLACK;
    static final Color GHOST_DOOR_COLOR = new Color(255, 183, 255);
    static final Color OUTSIDE_COLOR = Color.BLACK;

    // Power pellet blink
    int pelletBlinkCounter = 0;
    boolean pelletVisible = true;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        mapTileNumber = new int[gp.maxScreenCol][gp.maxScreenRow];
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int row = 0; row < gp.maxScreenRow; row++) {
                String line = br.readLine();
                if (line == null) break;
                String[] data = line.trim().split("\\s+");
                for (int col = 0; col < gp.maxScreenCol; col++) {
                    if (col < data.length) {
                        mapTileNumber[col][row] = Integer.parseInt(data[col]);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int countDots() {
        int count = 0;
        for (int col = 0; col < gp.maxScreenCol; col++) {
            for (int row = 0; row < gp.maxScreenRow; row++) {
                if (mapTileNumber[col][row] == 2 || mapTileNumber[col][row] == 3) {
                    count++;
                }
            }
        }
        return count;
    }

    public void reloadMap() {
        loadMap("/map/pacman_classic");
    }

    public void draw(Graphics2D g2d) {
        // Power pellet blink
        pelletBlinkCounter++;
        if (pelletBlinkCounter > 10) {
            pelletVisible = !pelletVisible;
            pelletBlinkCounter = 0;
        }

        for (int row = 0; row < gp.maxScreenRow; row++) {
            for (int col = 0; col < gp.maxScreenCol; col++) {
                int screenX = gp.mapOffsetX + col * gp.tileSize;
                int screenY = gp.mapOffsetY + row * gp.tileSize;
                int tileType = mapTileNumber[col][row];
                int ts = gp.tileSize;

                switch (tileType) {
                    case 0: // Outside
                        g2d.setColor(OUTSIDE_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        break;

                    case 1: // Wall
                        drawWall(g2d, col, row, screenX, screenY, ts);
                        break;

                    case 2: // Dot
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        g2d.setColor(DOT_COLOR);
                        int dotSize = 3;
                        g2d.fillOval(
                            screenX + ts / 2 - dotSize / 2,
                            screenY + ts / 2 - dotSize / 2,
                            dotSize, dotSize
                        );
                        break;

                    case 3: // Power pellet
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        if (pelletVisible) {
                            g2d.setColor(DOT_COLOR);
                            int pelletSize = ts / 2;
                            g2d.fillOval(
                                screenX + ts / 2 - pelletSize / 2,
                                screenY + ts / 2 - pelletSize / 2,
                                pelletSize, pelletSize
                            );
                        }
                        break;

                    case 4: // Ghost house
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        break;

                    case 5: // Empty path
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        break;

                    case 6: // Ghost door
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        g2d.setColor(GHOST_DOOR_COLOR);
                        g2d.fillRect(screenX, screenY + ts / 2 - 2, ts, 4);
                        break;
                }
            }
        }
    }

    private void drawWall(Graphics2D g2d, int col, int row, int x, int y, int ts) {
        g2d.setColor(WALL_COLOR);
        g2d.fillRect(x, y, ts, ts);

        // Draw borders on edges adjacent to non-wall tiles
        g2d.setColor(WALL_HIGHLIGHT);
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2));

        // Check each neighbor
        if (row > 0 && !isWall(col, row - 1)) { // top neighbor is path
            g2d.drawLine(x, y, x + ts, y);
        }
        if (row < gp.maxScreenRow - 1 && !isWall(col, row + 1)) { // bottom
            g2d.drawLine(x, y + ts - 1, x + ts, y + ts - 1);
        }
        if (col > 0 && !isWall(col - 1, row)) { // left
            g2d.drawLine(x, y, x, y + ts);
        }
        if (col < gp.maxScreenCol - 1 && !isWall(col + 1, row)) { // right
            g2d.drawLine(x + ts - 1, y, x + ts - 1, y + ts);
        }

        g2d.setStroke(oldStroke);
    }

    private boolean isWall(int col, int row) {
        if (col < 0 || col >= gp.maxScreenCol || row < 0 || row >= gp.maxScreenRow) {
            return true;
        }
        int type = mapTileNumber[col][row];
        return type == 0 || type == 1;
    }
}
