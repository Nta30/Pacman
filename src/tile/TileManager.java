package tile;

import main.GamePanel;
import java.awt.*;
import java.io.*;

public class TileManager {
    GamePanel gp;
    public int[][] mapTileNumber;

    // Map-specific colors
    static final Color[] WALL_COLORS = {
            new Color(33, 33, 222),   // Classic - Blue
            new Color(34, 139, 34),   // Forest - Green
            new Color(210, 180, 140), // Desert - Tan
            new Color(100, 149, 237), // Ice - Cornflower Blue
            new Color(178, 34, 34),   // Volcano - Firebrick
            new Color(25, 25, 112)    // Night - Midnight Blue
    };

    static final Color[] WALL_HIGHLIGHTS = {
            new Color(60, 60, 255),
            new Color(60, 179, 60),
            new Color(240, 210, 170),
            new Color(173, 216, 230),
            new Color(220, 80, 80),
            new Color(65, 65, 150)
    };

    static final Color DOT_COLOR = new Color(255, 183, 174);
    static final Color PATH_COLOR = Color.BLACK;
    static final Color GHOST_DOOR_COLOR = new Color(255, 183, 255);
    static final Color OUTSIDE_COLOR = Color.BLACK;

    int pelletBlinkCounter = 0;
    boolean pelletVisible = true;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        mapTileNumber = new int[gp.maxScreenCol][gp.maxScreenRow];
    }

//    public void loadMap(String filePath) {
//        try {
//            InputStream is = getClass().getResourceAsStream(filePath);
//            if (is == null) {
//                System.err.println("Map file not found: " + filePath);
//                createEmptyMap();
//                return;
//            }
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//
//            for (int row = 0; row < gp.maxScreenRow; row++) {
//                String line = br.readLine();
//                if (line == null) break;
//                String[] data = line.trim().split("\\s+");
//                for (int col = 0; col < gp.maxScreenCol; col++) {
//                    if (col < data.length) {
//                        mapTileNumber[col][row] = Integer.parseInt(data[col]);
//                    }
//                }
//            }
//            br.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            createEmptyMap();
//        }
//    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.err.println("Không tìm thấy file map: " + filePath);
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            for (int row = 0; row < gp.maxScreenRow; row++) {
                String line = br.readLine();
                if (line == null) {
                    System.err.println("File map thiếu dòng tại row " + row);
                    break;
                }

                // Bỏ qua dòng trống
                if (line.trim().isEmpty()) {
                    row--; // Không tăng row nếu dòng trống
                    continue;
                }

                String[] data = line.trim().split("\\s+");

                // Đảm bảo đủ số cột
                for (int col = 0; col < gp.maxScreenCol; col++) {
                    if (col < data.length && !data[col].isEmpty()) {
                        mapTileNumber[col][row] = Integer.parseInt(data[col]);
                    } else {
                        // Nếu thiếu số, đặt mặc định là 1 (wall)
                        mapTileNumber[col][row] = 1;
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createEmptyMap() {
        for (int col = 0; col < gp.maxScreenCol; col++) {
            for (int row = 0; row < gp.maxScreenRow; row++) {
                if (row == 0 || row == gp.maxScreenRow - 1 || col == 0 || col == gp.maxScreenCol - 1) {
                    mapTileNumber[col][row] = 1;
                } else {
                    mapTileNumber[col][row] = 5;
                }
            }
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

    public void reloadMap(int mapIndex) {
        loadMap(gp.mapPaths[mapIndex]);
    }

    public void draw(Graphics2D g2d) {
        pelletBlinkCounter++;
        if (pelletBlinkCounter > 10) {
            pelletVisible = !pelletVisible;
            pelletBlinkCounter = 0;
        }

        int mapIdx = gp.selectedMap - 1;
        Color wallColor = WALL_COLORS[mapIdx];
        Color wallHighlight = WALL_HIGHLIGHTS[mapIdx];

        for (int row = 0; row < gp.maxScreenRow; row++) {
            for (int col = 0; col < gp.maxScreenCol; col++) {
                int screenX = gp.mapOffsetX + col * gp.tileSize;
                int screenY = gp.mapOffsetY + row * gp.tileSize;
                int tileType = mapTileNumber[col][row];
                int ts = gp.tileSize;

                switch (tileType) {
                    case 0:
                        g2d.setColor(OUTSIDE_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        break;
                    case 1:
                        drawWall(g2d, col, row, screenX, screenY, ts, wallColor, wallHighlight);
                        break;
                    case 2:
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        g2d.setColor(DOT_COLOR);
                        int dotSize = 4;
                        g2d.fillOval(screenX + ts / 2 - dotSize / 2,
                                screenY + ts / 2 - dotSize / 2, dotSize, dotSize);
                        break;
                    case 3:
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        if (pelletVisible) {
                            g2d.setColor(DOT_COLOR);
                            int pelletSize = ts / 2;
                            g2d.fillOval(screenX + ts / 2 - pelletSize / 2,
                                    screenY + ts / 2 - pelletSize / 2, pelletSize, pelletSize);
                        }
                        break;
                    case 4:
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        break;
                    case 5:
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        break;
                    case 6:
                        g2d.setColor(PATH_COLOR);
                        g2d.fillRect(screenX, screenY, ts, ts);
                        g2d.setColor(GHOST_DOOR_COLOR);
                        g2d.fillRect(screenX, screenY + ts / 2 - 2, ts, 4);
                        break;
                }
            }
        }
    }

    private void drawWall(Graphics2D g2d, int col, int row, int x, int y, int ts, Color wallColor, Color wallHighlight) {
        g2d.setColor(wallColor);
        g2d.fillRect(x, y, ts, ts);

        g2d.setColor(wallHighlight);
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(2));

        if (row > 0 && !isWall(col, row - 1)) {
            g2d.drawLine(x, y, x + ts, y);
        }
        if (row < gp.maxScreenRow - 1 && !isWall(col, row + 1)) {
            g2d.drawLine(x, y + ts - 1, x + ts, y + ts - 1);
        }
        if (col > 0 && !isWall(col - 1, row)) {
            g2d.drawLine(x, y, x, y + ts);
        }
        if (col < gp.maxScreenCol - 1 && !isWall(col + 1, row)) {
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