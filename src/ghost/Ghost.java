package ghost;

import entity.Entity;
import main.GamePanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ghost extends Entity {

    // Ghost states
    public static final int SCATTER = 0;
    public static final int CHASE = 1;
    public static final int FRIGHTENED = 2;
    public static final int EATEN = 3;
    public static final int IN_HOUSE = 4;
    public static final int EXITING_HOUSE = 5;

    public int state = SCATTER;
    public Color ghostColor;
    public int scatterTargetX, scatterTargetY;
    public int releaseTimer = 0;
    public int releaseDelay = 0;
    public int spawnCol, spawnRow;

    // Player control (2-player mode)
    public boolean isPlayerControlled = false;
    public String playerNextDirection = "left";

    // Animation
    public int animCounter = 0;
    public boolean animFrame = false;
    public int frightFlashCounter = 0;

    protected Random random = new Random();

    public Ghost(GamePanel gp) {
        super(gp);
        speed = 3;
    }

    public void setDefaultValues() {
        setGridPosition(spawnCol, spawnRow);
        direction = "left";
        state = (spawnRow >= 13 && spawnRow <= 15) ? IN_HOUSE : SCATTER;
        releaseTimer = 0;
        alive = true;
        speed = gp.ghostBaseSpeed;
        playerNextDirection = "left";
        frightFlashCounter = 0;
    }

    // Override in subclasses
    public int getChaseTargetX() { return gp.player.gridX; }
    public int getChaseTargetY() { return gp.player.gridY; }

    public void reverseDirection() {
        snapToGrid();
        direction = getReverse(direction);
    }

    /**
     * Safe state transition - snaps to grid to prevent speed-change misalignment.
     */
    public void setState(int newState) {
        if (state != newState) {
            snapToGrid();
            state = newState;
        }
    }

    @Override
    public void update() {
        // Animation
        animCounter++;
        if (animCounter > 8) {
            animFrame = !animFrame;
            animCounter = 0;
        }

        // State-specific updates
        switch (state) {
            case IN_HOUSE:    updateInHouse(); return;
            case EXITING_HOUSE: updateExitingHouse(); return;
            case EATEN:       updateEaten(); return;
        }

        // Player-controlled ghost (2P mode)
        if (isPlayerControlled) {
            updatePlayerControlled();
            return;
        }

        // AI: SCATTER, CHASE, FRIGHTENED
        updateAI();
    }

    private void updatePlayerControlled() {
        if (isAtGridPosition()) {
            updateGridPosition();
            speed = (state == FRIGHTENED) ? 2 : gp.ghostBaseSpeed;

            // Try player's desired direction
            if (gp.collisionChecker.canMoveGhost(gridX, gridY, playerNextDirection, state)) {
                direction = playerNextDirection;
            }
            // If current direction blocked, stop
            if (!gp.collisionChecker.canMoveGhost(gridX, gridY, direction, state)) {
                return;
            }
        }
        move();
        tunnelWrap();
    }

    private void updateAI() {
        if (isAtGridPosition()) {
            updateGridPosition();

            boolean inTunnel = (gridY == 14 && (gridX <= 5 || gridX >= 22));
            if (state == FRIGHTENED) speed = 2;
            else if (inTunnel) speed = 2;
            else speed = gp.ghostBaseSpeed;

            chooseDirectionAtIntersection();
        }
        move();
        tunnelWrap();
    }

    private void updateInHouse() {
        releaseTimer++;
        if (releaseTimer >= releaseDelay) {
            state = EXITING_HOUSE;
            releaseTimer = 0;
        }
    }

    private void updateExitingHouse() {
        speed = 3;
        if (isAtGridPosition()) {
            updateGridPosition();
            if (gridX < 13)       direction = "right";
            else if (gridX > 13)  direction = "left";
            else if (gridY > 11)  direction = "up";
            else {
                // Exited the house
                state = (gp.ghostMode == 0) ? SCATTER : CHASE;
                direction = "left";
                playerNextDirection = "left";
                return;
            }
        }
        move();
    }

    private void updateEaten() {
        speed = 6;
        if (isAtGridPosition()) {
            updateGridPosition();

            // Reached above ghost house door
            if (gridX == 13 && gridY == 11) {
                setGridPosition(13, 14);
                state = EXITING_HOUSE;
                return;
            }

            // Navigate towards ghost house door
            chooseDirectionToTarget(13, 11);
        }
        move();
        tunnelWrap();
    }

    protected void chooseDirectionAtIntersection() {
        if (state == FRIGHTENED) {
            chooseRandomDirection();
            return;
        }

        int targetX, targetY;
        if (state == SCATTER) {
            targetX = scatterTargetX;
            targetY = scatterTargetY;
        } else {
            targetX = getChaseTargetX();
            targetY = getChaseTargetY();
        }
        chooseDirectionToTarget(targetX, targetY);
    }

    protected void chooseDirectionToTarget(int targetX, int targetY) {
        String reverse = getReverse(direction);
        String[] candidates = {"up", "left", "down", "right"};

        String bestDir = null;
        double bestDist = Double.MAX_VALUE;

        for (String dir : candidates) {
            if (dir.equals(reverse)) continue;
            if (!gp.collisionChecker.canMoveGhost(gridX, gridY, dir, state)) continue;

            int nextCol = gridX + getDx(dir);
            int nextRow = gridY + getDy(dir);
            double dist = Math.sqrt(Math.pow(nextCol - targetX, 2) + Math.pow(nextRow - targetY, 2));

            if (dist < bestDist) {
                bestDist = dist;
                bestDir = dir;
            }
        }

        if (bestDir != null) {
            direction = bestDir;
        } else {
            // Dead end: must reverse
            if (gp.collisionChecker.canMoveGhost(gridX, gridY, reverse, state)) {
                direction = reverse;
            }
        }
    }

    protected void chooseRandomDirection() {
        String reverse = getReverse(direction);
        String[] candidates = {"up", "left", "down", "right"};
        List<String> valid = new ArrayList<>();

        for (String dir : candidates) {
            if (dir.equals(reverse)) continue;
            if (gp.collisionChecker.canMoveGhost(gridX, gridY, dir, state)) {
                valid.add(dir);
            }
        }

        if (!valid.isEmpty()) {
            direction = valid.get(random.nextInt(valid.size()));
        } else if (gp.collisionChecker.canMoveGhost(gridX, gridY, reverse, state)) {
            direction = reverse;
        }
    }

    // ==================== RENDERING ====================

    @Override
    public void draw(Graphics2D g2d) {
        int drawX = gp.mapOffsetX + worldX;
        int drawY = gp.mapOffsetY + worldY;
        int ts = gp.tileSize;

        if (state == EATEN) {
            drawEyes(g2d, drawX, drawY, ts);
            return;
        }

        Color bodyColor;
        if (state == FRIGHTENED) {
            if (gp.frightTimer < 120) {
                frightFlashCounter++;
                bodyColor = (frightFlashCounter / 10 % 2 == 0) ?
                        new Color(33, 33, 255) : Color.WHITE;
            } else {
                bodyColor = new Color(33, 33, 255);
                frightFlashCounter = 0;
            }
        } else {
            bodyColor = ghostColor;
        }

        // Ghost body
        g2d.setColor(bodyColor);
        g2d.fillArc(drawX + 1, drawY + 1, ts - 2, ts - 2, 0, 180);
        g2d.fillRect(drawX + 1, drawY + ts / 2, ts - 2, ts / 2 - 1);

        // Wavy bottom
        int waveW = (ts - 2) / 3;
        int waveY = drawY + ts - 4;
        g2d.setColor(Color.BLACK);
        for (int i = 0; i < 3; i++) {
            int wx = drawX + 1 + i * waveW;
            if (animFrame) g2d.fillArc(wx, waveY, waveW, 6, 0, 180);
            else           g2d.fillArc(wx, waveY - 1, waveW, 6, 180, 180);
        }

        // Face
        if (state == FRIGHTENED) drawFrightenedFace(g2d, drawX, drawY, ts);
        else                    drawEyes(g2d, drawX, drawY, ts);

        // Player indicator (2P mode)
        if (isPlayerControlled && state != FRIGHTENED) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(gp.ui.smallFont.deriveFont(Font.BOLD, 8f));
            g2d.drawString("P2", drawX + ts / 2 - 6, drawY - 2);
        }
    }

    private void drawEyes(Graphics2D g2d, int x, int y, int ts) {
        int eyeW = ts / 4, eyeH = ts / 3;
        int leftX = x + ts / 4 - eyeW / 2;
        int rightX = x + 3 * ts / 4 - eyeW / 2;
        int eyeY = y + ts / 4;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(leftX, eyeY, eyeW, eyeH);
        g2d.fillOval(rightX, eyeY, eyeW, eyeH);

        int ps = eyeW / 2 + 1;
        int offX = 0, offY = 0;
        switch (direction) {
            case "up":    offY = -2; break;
            case "down":  offY = 2;  break;
            case "left":  offX = -2; break;
            case "right": offX = 2;  break;
        }

        g2d.setColor(new Color(33, 33, 255));
        int lpx = leftX + (eyeW - ps) / 2 + offX;
        int lpy = eyeY + (eyeH - ps) / 2 + offY;
        g2d.fillOval(lpx, lpy, ps, ps);
        g2d.fillOval(lpx + (rightX - leftX), lpy, ps, ps);
    }

    private void drawFrightenedFace(Graphics2D g2d, int x, int y, int ts) {
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + ts / 4, y + ts / 3, 3, 3);
        g2d.fillOval(x + 3 * ts / 4 - 3, y + ts / 3, 3, 3);

        int mouthY = y + ts * 2 / 3;
        for (int i = 0; i < 4; i++) {
            int mx  = x + 4 + i * (ts - 8) / 4;
            int my  = mouthY + (i % 2 == 0 ? 0 : 3);
            int mx2 = x + 4 + (i + 1) * (ts - 8) / 4;
            int my2 = mouthY + ((i + 1) % 2 == 0 ? 0 : 3);
            g2d.drawLine(mx, my, mx2, my2);
        }
    }
}
