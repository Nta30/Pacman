package main;

import java.awt.*;
import java.io.InputStream;

public class UI {

    GamePanel gp;
    Graphics2D g2d;
    Font gameFont;
    public int commandNumber = 0;
    public int settingRow = 0;
    public boolean showInstructions = false;

    // CatWomen theme colors
    static final Color BG_DARK      = new Color(10, 10, 50);
    static final Color BG_BLUE      = new Color(40, 40, 180);
    static final Color GOLD         = new Color(200, 180, 100);
    static final Color GOLD_BRIGHT  = new Color(240, 220, 130);
    static final Color GOLD_DIM     = new Color(150, 130, 70);
    static final Color BUTTON_BG    = new Color(230, 210, 100);
    static final Color BUTTON_SEL   = new Color(255, 240, 140);
    static final Color BUTTON_TEXT  = new Color(40, 30, 10);
    static final Color PANEL_BG     = new Color(60, 80, 180, 200);
    static final Color PANEL_BORDER = new Color(180, 160, 255, 180);
    static final Color CAT_ORANGE   = new Color(255, 165, 0);
    static final Color GHOST_RED    = Color.RED;
    static final Color GHOST_PINK   = new Color(255, 184, 255);
    static final Color GHOST_CYAN   = new Color(0, 255, 255);
    static final Color GHOST_ORANGE = new Color(255, 184, 82);

    public UI(GamePanel gp) {
        this.gp = gp;
        try {
            InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
            if (is != null) gameFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(20f);
            else            gameFont = new Font("Monospaced", Font.BOLD, 20);
        } catch (Exception e) {
            gameFont = new Font("Monospaced", Font.BOLD, 20);
        }
    }

    public void draw(Graphics2D g2d) {
        this.g2d = g2d;
        switch (gp.gameState) {
            case 0: drawTitleScreen();   break; // titleState
            case 1: drawSettingScreen(); break; // settingState
            case 2: drawHUD(); drawReady();    break;
            case 3: drawHUD();                 break;
            case 4: drawHUD(); drawPause();    break;
            case 5: drawHUD(); drawGameOver(); break;
            case 6: drawHUD(); drawWin();      break;
        }
    }

    // ==================== BACKGROUNDS ====================

    private void drawGradientBG() {
        GradientPaint gp2 = new GradientPaint(0, 0, BG_DARK,
                gp.screenWidth, gp.screenHeight, BG_BLUE);
        g2d.setPaint(gp2);
        g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    }

    // ==================== TITLE SCREEN ====================

    private void drawTitleScreen() {
        drawGradientBG();

        if (showInstructions) {
            drawInstructions();
            return;
        }

        // Title "CatWomen"
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 52f));
        String title = "CatWomen";
        int titleX = centerX(title);
        // Shadow
        g2d.setColor(new Color(60, 40, 120));
        g2d.drawString(title, titleX + 3, 83);
        // Main
        GradientPaint titleGrad = new GradientPaint(titleX, 50, new Color(100, 80, 200),
                titleX + 300, 80, new Color(180, 120, 220));
        g2d.setPaint(titleGrad);
        g2d.drawString(title, titleX, 80);

        // Ghost sprites (left side)
        Color[] gColors = {GHOST_ORANGE, GHOST_CYAN, GHOST_RED, GHOST_PINK};
        int[][] gPos = {{50, 130}, {120, 170}, {40, 230}, {100, 280}};
        for (int i = 0; i < 4; i++) {
            drawGhostSprite(gPos[i][0], gPos[i][1], 40, gColors[i]);
        }

        // Cat character (right side)
        drawCatCharacter(gp.screenWidth - 160, 120, 120);

        // Buttons
        int btnW = 280, btnH = 48;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        int startY = 390;
        String[] labels = {"Start", "How To Play", "Exit"};

        for (int i = 0; i < 3; i++) {
            int by = startY + i * 65;
            boolean selected = (commandNumber == i);
            drawButton(btnX, by, btnW, btnH, labels[i], selected);
        }

        // Controls hint
        g2d.setFont(gameFont.deriveFont(12f));
        g2d.setColor(new Color(150, 150, 180));
        String hint = "Arrow Keys / WASD   Enter to Select";
        g2d.drawString(hint, centerX(hint), gp.screenHeight - 20);
    }

    private void drawInstructions() {
        drawGradientBG();

        // Panel
        int px = 40, py = 30, pw = gp.screenWidth - 80, ph = gp.screenHeight - 60;
        drawPanel(px, py, pw, ph);

        g2d.setFont(gameFont.deriveFont(Font.BOLD, 28f));
        g2d.setColor(GOLD_BRIGHT);
        String title = "HOW TO PLAY";
        g2d.drawString(title, centerX(title), py + 50);

        g2d.setFont(gameFont.deriveFont(16f));
        g2d.setColor(Color.WHITE);
        int ly = py + 95;
        int lx = px + 30;
        int gap = 28;

        String[] lines = {
            "--- 1 PLAYER MODE ---",
            "Use Arrow Keys or WASD to move the Cat",
            "Eat all dots to complete the level",
            "Eat Power Pellets to chase ghosts",
            "Avoid ghosts or you lose a life!",
            "",
            "--- 2 PLAYER MODE ---",
            "Player 1 (Arrows): Control the Cat",
            "Player 2 (WASD): Control the Red Ghost",
            "",
            "Cat wins by eating all dots",
            "Ghost wins by catching the Cat",
            "",
            "--- CONTROLS ---",
            "P / ESC : Pause",
            "Enter   : Select",
            "",
            "--- DIFFICULTY ---",
            "Easy: Slower ghosts, longer power-ups",
            "Normal: Standard speed",
            "Hard: Faster ghosts, shorter power-ups"
        };

        for (String line : lines) {
            if (line.startsWith("---")) {
                g2d.setColor(GOLD);
                g2d.setFont(gameFont.deriveFont(Font.BOLD, 16f));
            } else {
                g2d.setColor(new Color(220, 220, 240));
                g2d.setFont(gameFont.deriveFont(15f));
            }
            g2d.drawString(line, lx, ly);
            ly += gap;
        }

        // Back button
        drawButton(gp.screenWidth / 2 - 80, gp.screenHeight - 70, 160, 40, "BACK", true);
    }

    // ==================== SETTINGS SCREEN ====================

    private void drawSettingScreen() {
        drawGradientBG();

        int pw = 500, ph = 340;
        int px = gp.screenWidth / 2 - pw / 2;
        int py = gp.screenHeight / 2 - ph / 2 - 30;
        drawPanel(px, py, pw, ph);

        // Title
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 30f));
        g2d.setColor(GOLD_BRIGHT);
        String title = "GAME SETTING";
        g2d.drawString(title, centerX(title), py + 50);

        int labelX = px + 40;
        int optX = px + 200;
        int rowY = py + 110;
        int rowGap = 70;

        // Row 0: PLAYER
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 20f));
        g2d.setColor(settingRow == 0 ? GOLD_BRIGHT : GOLD_DIM);
        g2d.drawString("PLAYER", labelX, rowY);

        drawSettingOption(optX, rowY - 25, "1", !gp.twoPlayerMode, settingRow == 0);
        drawSettingOption(optX + 80, rowY - 25, "2", gp.twoPlayerMode, settingRow == 0);

        // Row 1: DIFFICULTY
        rowY += rowGap;
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 20f));
        g2d.setColor(settingRow == 1 ? GOLD_BRIGHT : GOLD_DIM);
        g2d.drawString("DIFFICULTY", labelX, rowY);

        String[] diffs = {"EASY", "NORMAL", "HARD"};
        for (int i = 0; i < 3; i++) {
            drawSettingOption(optX + i * 90, rowY - 25, diffs[i],
                    gp.difficulty == i, settingRow == 1);
        }

        // Row 2: NEXT / BACK
        rowY += rowGap + 10;
        int bbw = 120, bbh = 40;
        drawButton(px + 30, rowY - 10, bbw, bbh, "BACK", settingRow == 2);
        drawButton(px + pw - bbw - 30, rowY - 10, bbw, bbh, "NEXT", settingRow == 2);

        // Navigation hint
        g2d.setFont(gameFont.deriveFont(12f));
        g2d.setColor(new Color(150, 150, 180));
        String hint = "Up/Down: Navigate   Left/Right: Change   Enter: Confirm   Esc: Back";
        g2d.drawString(hint, centerX(hint), gp.screenHeight - 20);
    }

    private void drawSettingOption(int x, int y, String text, boolean active, boolean rowFocused) {
        int w = 70, h = 32;
        if (text.length() > 3) w = 80;

        Color bg = active ? BUTTON_BG : new Color(160, 150, 120, 150);
        Color border = (active && rowFocused) ? GOLD_BRIGHT : new Color(120, 110, 80);

        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, w, h, 15, 15);
        g2d.setColor(border);
        g2d.setStroke(new BasicStroke(active ? 2 : 1));
        g2d.drawRoundRect(x, y, w, h, 15, 15);
        g2d.setStroke(new BasicStroke(1));

        g2d.setFont(gameFont.deriveFont(Font.BOLD, 14f));
        g2d.setColor(active ? BUTTON_TEXT : new Color(100, 90, 60));
        int tw = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + w / 2 - tw / 2, y + h / 2 + 5);
    }

    // ==================== HUD ====================

    private void drawHUD() {
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 18f));

        // Score
        g2d.setColor(Color.WHITE);
        g2d.drawString("SCORE", 10, 20);
        g2d.setColor(GOLD_BRIGHT);
        g2d.drawString(String.format("%06d", gp.score), 10, 42);

        // Level
        g2d.setColor(Color.WHITE);
        String lvl = "LEVEL " + gp.level;
        g2d.drawString(lvl, centerX(lvl), 20);

        // Mode indicator
        if (gp.twoPlayerMode) {
            g2d.setColor(GHOST_CYAN);
            g2d.setFont(gameFont.deriveFont(12f));
            g2d.drawString("2 PLAYERS", gp.screenWidth - 100, 20);
        }

        // Difficulty
        String[] dNames = {"EASY", "NORMAL", "HARD"};
        g2d.setFont(gameFont.deriveFont(12f));
        g2d.setColor(new Color(180, 180, 180));
        g2d.drawString(dNames[gp.difficulty], gp.screenWidth - 70, 42);

        // Lives
        int livesY = gp.screenHeight - 24;
        for (int i = 0; i < gp.lives - 1; i++) {
            g2d.setColor(CAT_ORANGE);
            g2d.fillArc(10 + i * 28, livesY, 20, 20, 30, 300);
        }

        // Dots remaining
        g2d.setFont(gameFont.deriveFont(14f));
        g2d.setColor(Color.WHITE);
        String dt = "DOTS: " + gp.dotsRemaining;
        g2d.drawString(dt, gp.screenWidth - g2d.getFontMetrics().stringWidth(dt) - 10,
                gp.screenHeight - 8);
    }

    // ==================== GAME SCREENS ====================

    private void drawReady() {
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 24f));
        g2d.setColor(GOLD_BRIGHT);
        String txt = "READY!";
        int x = gp.mapOffsetX + (gp.maxScreenCol * gp.tileSize) / 2
                - g2d.getFontMetrics().stringWidth(txt) / 2;
        g2d.drawString(txt, x, gp.mapOffsetY + 14 * gp.tileSize + gp.tileSize / 2 + 8);
    }

    private void drawPause() {
        // Overlay
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Cat character top-right
        drawCatCharacter(gp.screenWidth - 130, 60, 100);

        // Title
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 42f));
        g2d.setColor(GOLD_BRIGHT);
        String title = "Pause";
        g2d.drawString(title, centerX(title), 150);

        // Buttons
        int btnW = 220, btnH = 44;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        String[] labels = {"CONTINUE", "RESTART", "MENU"};
        for (int i = 0; i < 3; i++) {
            drawButton(btnX, 320 + i * 60, btnW, btnH, labels[i], commandNumber == i);
        }
    }

    private void drawGameOver() {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Title
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 44f));
        String title;
        if (gp.twoPlayerMode) {
            title = "GHOST WINS!";
            g2d.setColor(GHOST_RED);
        } else {
            title = "GAME OVER";
            g2d.setColor(GHOST_RED);
        }
        g2d.drawString(title, centerX(title), gp.screenHeight / 2 - 80);

        // Score
        g2d.setFont(gameFont.deriveFont(22f));
        g2d.setColor(Color.WHITE);
        String sc = "SCORE: " + gp.score;
        g2d.drawString(sc, centerX(sc), gp.screenHeight / 2 - 30);

        // Buttons
        int btnW = 220, btnH = 44;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        drawButton(btnX, gp.screenHeight / 2 + 20, btnW, btnH, "RESTART", commandNumber == 0);
        drawButton(btnX, gp.screenHeight / 2 + 80, btnW, btnH, "MENU", commandNumber == 1);
    }

    private void drawWin() {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Cat character
        drawCatCharacter(gp.screenWidth - 150, 80, 110);

        // Title
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 36f));
        String title;
        if (gp.twoPlayerMode) {
            title = "CAT WINS!";
            g2d.setColor(CAT_ORANGE);
        } else {
            title = "Level Complete";
            g2d.setColor(GOLD_BRIGHT);
        }
        g2d.drawString(title, centerX(title), gp.screenHeight / 2 - 80);

        // Score
        g2d.setFont(gameFont.deriveFont(22f));
        g2d.setColor(Color.WHITE);
        String sc = "SCORE: " + gp.score;
        g2d.drawString(sc, centerX(sc), gp.screenHeight / 2 - 30);

        // Buttons
        int btnW = 220, btnH = 44;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        String btn1 = gp.twoPlayerMode ? "PLAY AGAIN" : "NEXT LEVEL";
        drawButton(btnX, gp.screenHeight / 2 + 20, btnW, btnH, btn1, commandNumber == 0);
        drawButton(btnX, gp.screenHeight / 2 + 80, btnW, btnH, "MENU", commandNumber == 1);
    }

    // ==================== COMMON DRAWING ====================

    private void drawButton(int x, int y, int w, int h, String text, boolean selected) {
        Color bg = selected ? BUTTON_SEL : BUTTON_BG;
        // Shadow
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRoundRect(x + 3, y + 3, w, h, 20, 20);
        // Background
        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        // Border
        if (selected) {
            g2d.setColor(GOLD_BRIGHT);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, w, h, 20, 20);
            g2d.setStroke(new BasicStroke(1));
        }
        // Text
        g2d.setFont(gameFont.deriveFont(Font.BOLD, 20f));
        g2d.setColor(BUTTON_TEXT);
        int tw = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + w / 2 - tw / 2, y + h / 2 + 7);

        // Selection arrow
        if (selected) {
            g2d.setColor(BUTTON_TEXT);
            g2d.drawString("\u25B6", x + 10, y + h / 2 + 7);
        }
    }

    private void drawPanel(int x, int y, int w, int h) {
        g2d.setColor(PANEL_BG);
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(PANEL_BORDER);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, w, h, 20, 20);
        g2d.setStroke(new BasicStroke(1));
    }

    private void drawGhostSprite(int x, int y, int size, Color color) {
        g2d.setColor(color);
        g2d.fillArc(x, y, size, size, 0, 180);
        g2d.fillRect(x, y + size / 2, size, size / 2);

        // Skirt
        int ww = size / 3;
        g2d.setColor(BG_DARK);
        for (int i = 0; i < 3; i++) {
            g2d.fillArc(x + i * ww, y + size - 5, ww, 8, 0, 180);
        }

        // Eyes
        int ew = size / 4, eh = size / 3;
        int lx = x + size / 4 - ew / 2;
        int rx = x + 3 * size / 4 - ew / 2;
        int ey = y + size / 4;
        g2d.setColor(Color.WHITE);
        g2d.fillOval(lx, ey, ew, eh);
        g2d.fillOval(rx, ey, ew, eh);
        int ps = ew / 2 + 1;
        g2d.setColor(new Color(33, 33, 255));
        g2d.fillOval(lx + ew / 2 - ps / 2 + 1, ey + eh / 2 - ps / 2, ps, ps);
        g2d.fillOval(rx + ew / 2 - ps / 2 + 1, ey + eh / 2 - ps / 2, ps, ps);
    }

    private void drawCatCharacter(int x, int y, int size) {
        // Body
        g2d.setColor(CAT_ORANGE);
        g2d.fillOval(x + size / 6, y + size / 3, size * 2 / 3, size * 2 / 3);

        // Head
        int hw = size / 2, hh = size / 2;
        int hx = x + size / 4, hy = y + size / 6;
        g2d.fillOval(hx, hy, hw, hh);

        // Ears
        int earW = hw / 3;
        g2d.fillPolygon(
            new int[]{hx + 2, hx + earW, hx + earW * 2},
            new int[]{hy + hh / 4, hy - earW, hy + hh / 4}, 3);
        g2d.fillPolygon(
            new int[]{hx + hw - earW * 2, hx + hw - earW, hx + hw - 2},
            new int[]{hy + hh / 4, hy - earW, hy + hh / 4}, 3);

        // Inner ears
        g2d.setColor(new Color(255, 192, 203));
        int ie = earW / 2;
        g2d.fillPolygon(
            new int[]{hx + 5, hx + earW, hx + earW * 2 - 3},
            new int[]{hy + hh / 4 - 2, hy - earW + 5, hy + hh / 4 - 2}, 3);
        g2d.fillPolygon(
            new int[]{hx + hw - earW * 2 + 3, hx + hw - earW, hx + hw - 5},
            new int[]{hy + hh / 4 - 2, hy - earW + 5, hy + hh / 4 - 2}, 3);

        // Belly
        g2d.setColor(new Color(255, 230, 200));
        g2d.fillOval(x + size / 3, y + size / 2, size / 3, size / 3);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(hx + hw / 3 - 3, hy + hh / 3, 5, 6);
        g2d.fillOval(hx + 2 * hw / 3 - 2, hy + hh / 3, 5, 6);

        // Eye shine
        g2d.setColor(Color.WHITE);
        g2d.fillOval(hx + hw / 3 - 2, hy + hh / 3 + 1, 2, 2);
        g2d.fillOval(hx + 2 * hw / 3 - 1, hy + hh / 3 + 1, 2, 2);

        // Nose
        g2d.setColor(new Color(255, 120, 120));
        g2d.fillOval(hx + hw / 2 - 2, hy + hh / 2 + 2, 4, 3);

        // Whiskers
        g2d.setColor(new Color(80, 60, 40));
        g2d.setStroke(new BasicStroke(1));
        int wx = hx + hw / 2, wy = hy + hh / 2 + 5;
        g2d.drawLine(wx - 3, wy, wx - hw / 2 - 5, wy - 3);
        g2d.drawLine(wx - 3, wy + 2, wx - hw / 2 - 5, wy + 4);
        g2d.drawLine(wx + 3, wy, wx + hw / 2 + 5, wy - 3);
        g2d.drawLine(wx + 3, wy + 2, wx + hw / 2 + 5, wy + 4);
    }

    private int centerX(String text) {
        int w = (int) g2d.getFontMetrics().getStringBounds(text, g2d).getWidth();
        return gp.screenWidth / 2 - w / 2;
    }
}
