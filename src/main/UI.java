package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class UI {

    GamePanel gp;
    Graphics2D g2d;
    Font titleFont;
    Font buttonFont;
    public Font smallFont;
    public int commandNumber = 0;
    public int settingRow = 0;
    public int settingButtonPos = 0;
    public boolean showInstructions = false;

    // Title screen images
    BufferedImage imgGhostRed, imgGhostPink, imgGhostBlue, imgGhostOrange;
    BufferedImage imgPacman, imgPacmanLarge;

    // Colors
    static final Color BG_GRADIENT_TOP = new Color(20, 20, 60);
    static final Color BG_GRADIENT_BOTTOM = new Color(60, 40, 120);
    static final Color BUTTON_NORMAL = new Color(50, 50, 80, 220);
    static final Color BUTTON_HOVER = new Color(255, 200, 100);
    static final Color BUTTON_TEXT_NORMAL = new Color(200, 200, 220);
    static final Color BUTTON_TEXT_HOVER = new Color(30, 30, 50);
    static final Color PANEL_BG = new Color(0, 0, 0, 180);
    static final Color TITLE_COLOR = new Color(255, 220, 100);
    static final Color SCORE_COLOR = new Color(255, 180, 80);

    static final Color GHOST_RED = new Color(255, 0, 0);
    static final Color GHOST_PINK = new Color(255, 184, 255);
    static final Color GHOST_CYAN = new Color(0, 255, 255);
    static final Color GHOST_ORANGE = new Color(255, 184, 82);
    static final Color CAT_ORANGE = new Color(255, 165, 0);

    public UI(GamePanel gp) {
        this.gp = gp;
        loadCustomFont();
        loadTitleImages();
    }

    private void loadCustomFont() {
        try {
            Font maruMonica = Font.createFont(Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(maruMonica);
            titleFont = maruMonica.deriveFont(Font.BOLD, 42f);
            buttonFont = maruMonica.deriveFont(Font.BOLD, 18f);
            smallFont = maruMonica.deriveFont(Font.PLAIN, 12f);
        } catch (Exception e) {
            System.err.println("Warning: Could not load MaruMonica font, using Arial: " + e.getMessage());
            titleFont = new Font("Arial", Font.BOLD, 42);
            buttonFont = new Font("Arial", Font.BOLD, 18);
            smallFont = new Font("Arial", Font.PLAIN, 12);
        }
    }

    private void loadTitleImages() {
        try {
            imgGhostRed    = ImageIO.read(getClass().getResourceAsStream("/UI/3d-pixel-ghost-red-512x512.png"));
            imgGhostPink   = ImageIO.read(getClass().getResourceAsStream("/UI/3d-pixel-ghost-pink-128x128.png"));
            imgGhostBlue   = ImageIO.read(getClass().getResourceAsStream("/UI/3d-pixel-ghost-blue-128x128.png"));
            imgGhostOrange = ImageIO.read(getClass().getResourceAsStream("/UI/3d-pixel-ghost-orange-128x128.png"));
            imgPacman      = ImageIO.read(getClass().getResourceAsStream("/UI/3d-pixel-pac-128x128.png"));
            imgPacmanLarge = ImageIO.read(getClass().getResourceAsStream("/UI/pacman-png-25189.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Warning: Could not load UI images: " + e.getMessage());
        }
    }

    public void draw(Graphics2D g2d) {
        this.g2d = g2d;
        switch (gp.gameState) {
            case 0: drawTitleScreen();   break;
            case 1: drawSettingScreen(); break;
            case 2: drawHUD(); drawReady();    break;
            case 3: drawHUD();                 break;
            case 4: drawHUD(); drawPause();    break;
            case 5: drawHUD(); drawGameOver(); break;
            case 6: drawHUD(); drawWin();      break;
        }
    }

    private void drawGradientBG() {
        GradientPaint gradient = new GradientPaint(0, 0, BG_GRADIENT_TOP,
                gp.screenWidth, gp.screenHeight, BG_GRADIENT_BOTTOM);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    }

    // ==================== SOUND BUTTONS (GÓC PHẢI DƯỚI) ====================

    /**
     * Vẽ nút điều khiển âm thanh ở góc phải dưới màn hình
     * @param x - tọa độ x
     * @param y - tọa độ y
     */
    public boolean musicOn = true;
    public boolean sfxOn = true;
    private void drawSoundButtons(int x, int y) {
        int btnSize = 35;

        // Khung nền mờ cho 2 nút
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(x - 5, y - 5, 95, 50, 15, 15);

        // Nút Music (bên trái) - Dùng ký tự nốt nhạc
        g2d.setColor(gp.musicOn ? BUTTON_HOVER : BUTTON_NORMAL);
        g2d.fillRoundRect(x, y, btnSize, btnSize, 10, 10);
        g2d.setColor(gp.musicOn ? BUTTON_TEXT_HOVER : BUTTON_TEXT_NORMAL);
        g2d.setFont(buttonFont.deriveFont(18f));
        g2d.drawString("♪", x + 13, y + 25);

        // Nút SFX (bên phải) - TỰ VẼ HÌNH LOA
        g2d.setColor(gp.sfxOn ? BUTTON_HOVER : BUTTON_NORMAL);
        g2d.fillRoundRect(x + 48, y, btnSize, btnSize, 10, 10);

        // Vẽ hình loa bằng Graphics2D (màu sẽ thay đổi theo trạng thái bật/tắt)
        drawSpeakerIcon(x + 62, y + 17, 14, gp.sfxOn);

        // Nhãn chú thích phía dưới nút
        g2d.setFont(smallFont);
        g2d.setColor(new Color(180, 180, 180));
        g2d.drawString("MUSIC", x + 8, y + 52);
        g2d.drawString("SFX", x + 56, y + 52);
    }

    /**
     * Vẽ biểu tượng loa (speaker icon)
     * @param centerX - tọa độ X trung tâm
     * @param centerY - tọa độ Y trung tâm
     * @param size - kích thước
     * @param on - trạng thái bật/tắt (true: màu đen, false: màu xám mờ)
     */
    private void drawSpeakerIcon(int centerX, int centerY, int size, boolean on) {
        // Màu sắc dựa trên trạng thái
        Color iconColor = on ? BUTTON_TEXT_HOVER : new Color(100, 100, 100);
        g2d.setColor(iconColor);
        g2d.setStroke(new BasicStroke(2));

        int w = size;
        int h = size;

        // Vẽ thân loa (hình thang - phần thân)
        int[] bodyX = {centerX - w/3, centerX - w/3, centerX - w/6, centerX - w/6};
        int[] bodyY = {centerY - h/3, centerY + h/3, centerY + h/4, centerY - h/4};
        g2d.fillPolygon(bodyX, bodyY, 4);

        // Vẽ màng loa (hình elip - phần loa tròn)
        g2d.fillOval(centerX - w/6, centerY - h/4, w/3, h/2);

        // Vẽ sóng âm (các đường cong bên phải)
        if (on) {
            // Sóng âm khi bật - 3 đường cong
            g2d.setStroke(new BasicStroke(1.5f));

            // Sóng thứ nhất
            g2d.drawArc(centerX + w/6, centerY - h/3, w/3, h*2/3, -60, 120);
            // Sóng thứ hai
            g2d.drawArc(centerX + w/3, centerY - h/2, w/3, h, -60, 120);
            // Sóng thứ ba
            g2d.drawArc(centerX + w/2, centerY - h*2/3, w/3, h*4/3, -60, 120);
        } else {
            // Khi tắt - vẽ dấu X (gạch chéo)
            g2d.setStroke(new BasicStroke(2));
            int x1 = centerX - w/4;
            int y1 = centerY - h/3;
            int x2 = centerX + w/3;
            int y2 = centerY + h/3;
            g2d.drawLine(x1, y1, x2, y2);
            g2d.drawLine(x2, y1, x1, y2);
        }

        g2d.setStroke(new BasicStroke(1));
    }

    // ==================== TITLE SCREEN ====================

    private void drawTitleScreen() {
        drawGradientBG();

        if (showInstructions) {
            drawInstructions();
            return;
        }

        // Title "CatWomen" lớn
        g2d.setFont(titleFont);
        g2d.setColor(TITLE_COLOR);
        String title = "PACMAN";
        int titleX = centerX(title);
        g2d.setColor(new Color(80, 60, 40));
        g2d.drawString(title, titleX + 3, 105);
        g2d.setColor(TITLE_COLOR);
        g2d.drawString(title, titleX, 100);

        // Ghost images bên trái
        int ghostSize = 60;
        if (imgGhostRed != null)
            g2d.drawImage(imgGhostRed, 30, 130, ghostSize + 10, ghostSize + 10, null);
        if (imgGhostPink != null)
            g2d.drawImage(imgGhostPink, 100, 190, ghostSize, ghostSize, null);
        if (imgGhostBlue != null)
            g2d.drawImage(imgGhostBlue, 25, 250, ghostSize, ghostSize, null);
        if (imgGhostOrange != null)
            g2d.drawImage(imgGhostOrange, 90, 310, ghostSize, ghostSize, null);

        // Pacman image bên phải
        if (imgPacmanLarge != null)
            g2d.drawImage(imgPacmanLarge, gp.screenWidth - 160, 140, 130, 130, null);
        if (imgPacman != null)
            g2d.drawImage(imgPacman, gp.screenWidth - 120, 280, 70, 70, null);

        // Các nút chính
        int btnW = 220, btnH = 45;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        int startY = 380;
        String[] labels = {"START", "HOW TO PLAY", "EXIT"};

        for (int i = 0; i < 3; i++) {
            int by = startY + i * 60;
            boolean selected = (commandNumber == i);
            drawButton(btnX, by, btnW, btnH, labels[i], selected);
        }

        // Nút âm thanh ở góc phải dưới
        drawSoundButtons(gp.screenWidth - 100, gp.screenHeight - 70);

        // Hướng dẫn điều khiển
        g2d.setFont(smallFont);
        g2d.setColor(new Color(150, 150, 180));
        String hint = "↑/↓: Select   Enter: Confirm   K: Music   L: SFX";
        g2d.drawString(hint, centerX(hint), gp.screenHeight - 30);
    }

    private void drawInstructions() {
        drawGradientBG();

        int px = 50, py = 50, pw = gp.screenWidth - 100, ph = gp.screenHeight - 100;
        drawPanel(px, py, pw, ph);

        g2d.setFont(titleFont.deriveFont(28f));
        g2d.setColor(TITLE_COLOR);
        String title = "HOW TO PLAY";
        g2d.drawString(title, centerX(title), py + 55);

        g2d.setFont(buttonFont.deriveFont(14f));
        g2d.setColor(Color.WHITE);
        int ly = py + 100;
        int lx = px + 50;
        int gap = 30;

        String[] lines = {
                "USE ARROW KEYS OR WASD TO MOVE THE CAT",
                "",
                "EAT ALL DOTS TO COMPLETE THE LEVEL",
                "EAT POWER PELLETS TO CHASE GHOSTS",
                "AVOID GHOSTS OR YOU LOSE A LIFE!",
                "",
                "PRESS P OR ESC TO PAUSE THE GAME",
                "",
                "2 PLAYER MODE:",
                "PLAYER 1 (ARROWS): CONTROL THE CAT",
                "PLAYER 2 (WASD): CONTROL THE RED GHOST"
        };

        for (String line : lines) {
            if (line.isEmpty()) {
                ly += 10;
                continue;
            }
            if (line.contains("2 PLAYER MODE")) {
                g2d.setColor(TITLE_COLOR);
                g2d.setFont(buttonFont.deriveFont(16f));
            } else {
                g2d.setColor(new Color(220, 220, 240));
                g2d.setFont(buttonFont.deriveFont(13f));
            }
            g2d.drawString(line, lx, ly);
            ly += gap;
        }

        int btnW = 140, btnH = 40;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        drawButton(btnX, gp.screenHeight - 70, btnW, btnH, "BACK", true);

        // Nút âm thanh ở góc phải dưới
        drawSoundButtons(gp.screenWidth - 100, gp.screenHeight - 70);
    }

    // ==================== SETTINGS SCREEN ====================

    private void drawSettingScreen() {
        drawGradientBG();

        int pw = 500, ph = 350;
        int px = gp.screenWidth / 2 - pw / 2;
        int py = gp.screenHeight / 2 - ph / 2 - 20;
        drawPanel(px, py, pw, ph);

        g2d.setFont(titleFont.deriveFont(28f));
        g2d.setColor(TITLE_COLOR);
        String title = "GAME SETTINGS";
        g2d.drawString(title, centerX(title), py + 45);

        int labelX = px + 40;
        int optX = px + 180;
        int rowY = py + 95;
        int rowGap = 60;

        // Row 0: PLAYER
        g2d.setFont(buttonFont.deriveFont(16f));
        g2d.setColor(settingRow == 0 ? TITLE_COLOR : Color.WHITE);
        g2d.drawString("PLAYER", labelX, rowY);

        drawSmallButton(optX, rowY - 25, 55, 32, "1P", !gp.twoPlayerMode, settingRow == 0);
        drawSmallButton(optX + 65, rowY - 25, 55, 32, "2P", gp.twoPlayerMode, settingRow == 0);

        // Row 1: DIFFICULTY
        rowY += rowGap;
        g2d.setFont(buttonFont.deriveFont(16f));
        g2d.setColor(settingRow == 1 ? TITLE_COLOR : Color.WHITE);
        g2d.drawString("DIFFICULTY", labelX, rowY);

        String[] diffs = {"EASY", "NORMAL", "HARD"};
        for (int i = 0; i < 3; i++) {
            drawSmallButton(optX + i * 75, rowY - 25, 65, 32, diffs[i],
                    gp.difficulty == i, settingRow == 1);
        }

        // Row 2: MAP
        rowY += rowGap;
        g2d.setFont(buttonFont.deriveFont(16f));
        g2d.setColor(settingRow == 2 ? TITLE_COLOR : Color.WHITE);
        g2d.drawString("MAP", labelX, rowY);

        for (int i = 0; i < 6; i++) {
            drawSmallButton(optX + i * 48, rowY - 25, 40, 32,
                    String.valueOf(i + 1),
                    gp.selectedMap == i + 1,
                    settingRow == 2);
        }

        // Row 3: BACK and START buttons
        rowY += rowGap + 15;
        int bbw = 120, bbh = 40;
        int backX = px + 50;
        int startX = px + pw - bbw - 50;

        boolean isBackSelected = (settingRow == 3 && settingButtonPos == 0);
        boolean isStartSelected = (settingRow == 3 && settingButtonPos == 1);

        drawButton(backX, rowY - 10, bbw, bbh, "BACK", isBackSelected);
        drawButton(startX, rowY - 10, bbw, bbh, "START", isStartSelected);

        // Nút âm thanh ở góc phải dưới
        drawSoundButtons(gp.screenWidth - 100, gp.screenHeight - 70);

        g2d.setFont(smallFont);
        g2d.setColor(new Color(150, 150, 180));
        String hint = "↑/↓: Navigate   ←/→: Change   Enter: Confirm   ESC: Back";
        g2d.drawString(hint, centerX(hint), gp.screenHeight - 30);
    }

    /**
     * Vẽ nút nhỏ cho các tùy chọn (Player, Difficulty, Map)
     * @param x - tọa độ x
     * @param y - tọa độ y
     * @param w - chiều rộng
     * @param h - chiều cao
     * @param text - nội dung text
     * @param active - trạng thái được chọn hay không
     * @param rowFocused - hàng đang được focus
     */
    private void drawSmallButton(int x, int y, int w, int h, String text, boolean active, boolean rowFocused) {
        Color bg = active ? BUTTON_HOVER : BUTTON_NORMAL;
        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, w, h, 10, 10);

        if (active && rowFocused) {
            g2d.setColor(TITLE_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, w, h, 10, 10);
        } else if (active) {
            g2d.setColor(new Color(255, 220, 150));
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRoundRect(x, y, w, h, 10, 10);
        }

        g2d.setFont(buttonFont.deriveFont(13f));
        g2d.setColor(active ? BUTTON_TEXT_HOVER : BUTTON_TEXT_NORMAL);
        int tw = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + w / 2 - tw / 2, y + h / 2 + 6);

        g2d.setStroke(new BasicStroke(1));
    }

    // ==================== HUD ====================

    private void drawHUD() {
        // Điểm số
        g2d.setFont(buttonFont.deriveFont(16f));
        g2d.setColor(Color.WHITE);
        g2d.drawString("SCORE", 10, 25);
        g2d.setFont(titleFont.deriveFont(24f));
        g2d.setColor(SCORE_COLOR);
        g2d.drawString(String.format("%06d", gp.score), 10, 55);

        // Cấp độ
        g2d.setFont(buttonFont.deriveFont(14f));
        g2d.setColor(Color.WHITE);
        String lvl = "LEVEL " + gp.level;
        g2d.drawString(lvl, gp.screenWidth - 100, 25);

        // Chế độ 2 người chơi
        if (gp.twoPlayerMode) {
            g2d.setColor(GHOST_RED);
            g2d.setFont(smallFont.deriveFont(Font.BOLD, 10f));
            g2d.drawString("2P", gp.screenWidth - 50, 45);
        }

        // Độ khó
        String[] dNames = {"EASY", "NORMAL", "HARD"};
        g2d.setFont(smallFont);
        g2d.setColor(new Color(150, 150, 180));
        g2d.drawString(dNames[gp.difficulty], gp.screenWidth - 60, 65);

        // Mạng sống (icon mèo)
        int livesY = gp.screenHeight - 30;
        for (int i = 0; i < gp.lives - 1; i++) {
            drawCatIcon(10 + i * 30, livesY, 22);
        }

        // Số chấm còn lại
        g2d.setFont(buttonFont.deriveFont(12f));
        g2d.setColor(Color.WHITE);
        String dt = "DOTS: " + gp.dotsRemaining;
        g2d.drawString(dt, gp.screenWidth - 80, gp.screenHeight - 18);

        // THÊM DÒNG NÀY VÀO CUỐI METHOD drawHUD()
        drawSoundButtonsHUD();  // Gọi vẽ nút âm thanh trên HUD
    }

    /**
     * Vẽ nút âm thanh trên HUD (trong khi chơi game) - THÊM METHOD NÀY
     */
    private void drawSoundButtonsHUD() {
        int btnSize = 25;
        int x = gp.screenWidth - 65;
        int y = gp.screenHeight - 32;

        // Nút Music
        g2d.setColor(gp.musicOn ? BUTTON_HOVER : BUTTON_NORMAL);
        g2d.fillRoundRect(x, y, btnSize, btnSize, 6, 6);
        g2d.setColor(gp.musicOn ? BUTTON_TEXT_HOVER : BUTTON_TEXT_NORMAL);
        g2d.setFont(smallFont.deriveFont(Font.BOLD, 11f));
        g2d.drawString("♪", x + 7, y + 18);

        // Nút SFX - Tự vẽ hình loa nhỏ
        g2d.setColor(gp.sfxOn ? BUTTON_HOVER : BUTTON_NORMAL);
        g2d.fillRoundRect(x + 30, y, btnSize, btnSize, 6, 6);

        // Vẽ hình loa nhỏ (dùng lại method drawSpeakerIcon đã có)
        drawSpeakerIcon(x + 42, y + 12, 10, gp.sfxOn);
    }

    private void drawCatIcon(int x, int y, int size) {
        g2d.setColor(CAT_ORANGE);
        g2d.fillArc(x, y, size, size, 30, 300);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + size / 3, y + size / 3, 2, 2);
    }

    // ==================== GAME SCREENS ====================

    private void drawReady() {
        drawOverlay();
        g2d.setFont(titleFont.deriveFont(30f));
        g2d.setColor(TITLE_COLOR);
        String txt = "READY!";
        g2d.drawString(txt, centerX(txt), gp.screenHeight / 2);
    }

    private void drawPause() {
        drawOverlay();

        g2d.setFont(titleFont.deriveFont(40f));
        g2d.setColor(TITLE_COLOR);
        String title = "PAUSE";
        g2d.drawString(title, centerX(title), 150);

        int btnW = 200, btnH = 45;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        String[] labels = {"CONTINUE", "RESTART", "MENU"};
        for (int i = 0; i < 3; i++) {
            drawButton(btnX, 280 + i * 60, btnW, btnH, labels[i], commandNumber == i);
        }

        // Nút âm thanh ở góc phải dưới
        drawSoundButtons(gp.screenWidth - 100, gp.screenHeight - 70);
    }

    private void drawGameOver() {
        drawOverlay();

        g2d.setFont(titleFont.deriveFont(40f));
        String title;
        if (gp.twoPlayerMode) {
            title = "GHOST WINS!";
            g2d.setColor(GHOST_RED);
        } else {
            title = "GAME OVER";
            g2d.setColor(GHOST_RED);
        }
        g2d.drawString(title, centerX(title), 150);

        g2d.setFont(buttonFont.deriveFont(22f));
        g2d.setColor(Color.WHITE);
        String sc = "SCORE: " + gp.score;
        g2d.drawString(sc, centerX(sc), 220);

        int btnW = 200, btnH = 45;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        drawButton(btnX, 300, btnW, btnH, "RESTART", commandNumber == 0);
        drawButton(btnX, 370, btnW, btnH, "MENU", commandNumber == 1);

        // Nút âm thanh ở góc phải dưới
        drawSoundButtons(gp.screenWidth - 100, gp.screenHeight - 70);
    }

    private void drawWin() {
        drawOverlay();

        g2d.setFont(titleFont.deriveFont(40f));
        String title;
        if (gp.twoPlayerMode) {
            title = "CAT WINS!";
            g2d.setColor(CAT_ORANGE);
        } else {
            title = "LEVEL COMPLETE!";
            g2d.setColor(TITLE_COLOR);
        }
        g2d.drawString(title, centerX(title), 150);

        g2d.setFont(buttonFont.deriveFont(22f));
        g2d.setColor(Color.WHITE);
        String sc = "SCORE: " + gp.score;
        g2d.drawString(sc, centerX(sc), 220);

        int btnW = 200, btnH = 45;
        int btnX = gp.screenWidth / 2 - btnW / 2;
        String btn1 = gp.twoPlayerMode ? "PLAY AGAIN" : "NEXT LEVEL";
        drawButton(btnX, 300, btnW, btnH, btn1, commandNumber == 0);
        drawButton(btnX, 370, btnW, btnH, "MENU", commandNumber == 1);

        // Nút âm thanh ở góc phải dưới
        drawSoundButtons(gp.screenWidth - 100, gp.screenHeight - 70);
    }

    private void drawOverlay() {
        g2d.setColor(PANEL_BG);
        g2d.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
    }

    // ==================== COMMON DRAWING ====================

    /**
     * Vẽ nút bấm chính
     * @param x - tọa độ x
     * @param y - tọa độ y
     * @param w - chiều rộng
     * @param h - chiều cao
     * @param text - nội dung text
     * @param selected - trạng thái được chọn hay không
     */
    private void drawButton(int x, int y, int w, int h, String text, boolean selected) {
        // Đổ bóng cho nút
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRoundRect(x + 3, y + 3, w, h, 25, 25);

        // Nền nút
        Color bg = selected ? BUTTON_HOVER : BUTTON_NORMAL;
        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, w, h, 25, 25);

        // Viền nếu được chọn
        if (selected) {
            g2d.setColor(TITLE_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, w, h, 25, 25);

            // Mũi tên chỉ chọn (tam giác màu đen)
            g2d.setColor(BUTTON_TEXT_HOVER);
            int[] arrowX = {x + 12, x + 18, x + 12};
            int[] arrowY = {y + h / 2 - 5, y + h / 2, y + h / 2 + 5};
            g2d.fillPolygon(arrowX, arrowY, 3);
        }

        // Text trên nút
        g2d.setFont(buttonFont);
        g2d.setColor(selected ? BUTTON_TEXT_HOVER : BUTTON_TEXT_NORMAL);
        int tw = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, x + w / 2 - tw / 2, y + h / 2 + 7);

        g2d.setStroke(new BasicStroke(1));
    }

    /**
     * Vẽ khung panel bo tròn
     * @param x - tọa độ x
     * @param y - tọa độ y
     * @param w - chiều rộng
     * @param h - chiều cao
     */
    private void drawPanel(int x, int y, int w, int h) {
        g2d.setColor(PANEL_BG);
        g2d.fillRoundRect(x, y, w, h, 20, 20);
        g2d.setColor(new Color(255, 220, 100, 80));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, w, h, 20, 20);
        g2d.setStroke(new BasicStroke(1));
    }

    // drawGhostSprite và drawCatCharacter đã được thay thế bằng ảnh PNG từ res/UI/

    /**
     * Tính toán tọa độ X để căn giữa text
     * @param text - nội dung text cần căn giữa
     * @return tọa độ X
     */
    private int centerX(String text) {
        int w = g2d.getFontMetrics().stringWidth(text);
        return gp.screenWidth / 2 - w / 2;
    }
}