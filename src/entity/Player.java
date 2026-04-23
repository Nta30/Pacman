package entity;

import main.GamePanel;
import main.KeyHandler;

import java.awt.*;

public class Player extends Entity {

    KeyHandler keyH;

    // Pacman / Cat specific
    public String nextDirection = "left";
    public int mouthAngle = 45;
    public boolean mouthClosing = false;
    public boolean deathAnimation = false;
    public int deathCounter = 0;
    public int deathAngle = 0;

    // Cat colors
    static final Color CAT_BODY = new Color(255, 165, 0);   // orange
    static final Color CAT_EAR  = new Color(255, 130, 60);
    static final Color CAT_INNER = new Color(255, 192, 203); // pink inner ear

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);
        this.keyH = keyH;
        speed = 3;
        direction = "left";
        nextDirection = "left";
    }

    public void setDefaultValues() {
        setGridPosition(14, 22);
        direction = "left";
        nextDirection = "left";
        speed = 3;
        alive = true;
        deathAnimation = false;
        deathCounter = 0;
        deathAngle = 0;
        mouthAngle = 45;
        mouthClosing = false;
    }

    public void startDeathAnimation() {
        deathAnimation = true;
        deathCounter = 0;
        deathAngle = 0;
    }

    @Override
    public void update() {
        if (deathAnimation) {
            deathCounter++;
            deathAngle = Math.min(deathCounter * 4, 360);
            if (deathCounter >= 90) deathAnimation = false;
            return;
        }

        // Read input: P1 always, P2 only in 1-player mode
        boolean up, down, left, right;
        if (gp.twoPlayerMode) {
            // 2P: Pacman uses arrows only (P1)
            up    = keyH.p1Up;
            down  = keyH.p1Down;
            left  = keyH.p1Left;
            right = keyH.p1Right;
        } else {
            // 1P: Pacman uses both WASD and arrows
            up    = keyH.p1Up    || keyH.p2Up;
            down  = keyH.p1Down  || keyH.p2Down;
            left  = keyH.p1Left  || keyH.p2Left;
            right = keyH.p1Right || keyH.p2Right;
        }

        if (up)         nextDirection = "up";
        else if (down)  nextDirection = "down";
        else if (left)  nextDirection = "left";
        else if (right) nextDirection = "right";

        // At grid intersection, try to change direction
        if (isAtGridPosition()) {
            updateGridPosition();
            if (gp.collisionChecker.canMovePacman(gridX, gridY, nextDirection)) {
                direction = nextDirection;
            }
            if (!gp.collisionChecker.canMovePacman(gridX, gridY, direction)) {
                return;
            }
        }

        move();
        tunnelWrap();

        // Animate mouth
        spriteCounter++;
        if (spriteCounter > 2) {
            if (mouthClosing) {
                mouthAngle -= 12;
                if (mouthAngle <= 2)  { mouthAngle = 2;  mouthClosing = false; }
            } else {
                mouthAngle += 12;
                if (mouthAngle >= 45) { mouthAngle = 45; mouthClosing = true; }
            }
            spriteCounter = 0;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        int drawX = gp.mapOffsetX + worldX;
        int drawY = gp.mapOffsetY + worldY;
        int size = gp.tileSize - 2;
        int ox = drawX + 1;
        int oy = drawY + 1;

        if (deathAnimation) {
            int arcAngle = 360 - deathAngle;
            if (arcAngle > 0) {
                g2d.setColor(CAT_BODY);
                g2d.fillArc(ox, oy, size, size, 90 + deathAngle / 2, arcAngle);
            }
            return;
        }

        // Calculate mouth start angle
        int startAngle;
        switch (direction) {
            case "right": startAngle = mouthAngle;       break;
            case "up":    startAngle = 90 + mouthAngle;  break;
            case "left":  startAngle = 180 + mouthAngle; break;
            case "down":  startAngle = 270 + mouthAngle; break;
            default:      startAngle = mouthAngle;
        }
        int arcAngle = 360 - 2 * mouthAngle;

        // Draw ears (behind body)
        int earW = size / 3;
        int earH = size / 2;
        g2d.setColor(CAT_EAR);
        // Left ear
        g2d.fillPolygon(
            new int[]{ox + 2, ox + earW / 2 + 2, ox + earW + 2},
            new int[]{oy + size / 4, oy - earH / 3, oy + size / 4}, 3
        );
        // Right ear
        g2d.fillPolygon(
            new int[]{ox + size - earW - 2, ox + size - earW / 2 - 2, ox + size - 2},
            new int[]{oy + size / 4, oy - earH / 3, oy + size / 4}, 3
        );
        // Inner ear pink
        g2d.setColor(CAT_INNER);
        int ie = earW / 2;
        g2d.fillPolygon(
            new int[]{ox + 4, ox + earW / 2 + 2, ox + earW},
            new int[]{oy + size / 4 - 1, oy - earH / 6, oy + size / 4 - 1}, 3
        );
        g2d.fillPolygon(
            new int[]{ox + size - earW, ox + size - earW / 2 - 2, ox + size - 4},
            new int[]{oy + size / 4 - 1, oy - earH / 6, oy + size / 4 - 1}, 3
        );

        // Body (Pacman arc shape in cat color)
        g2d.setColor(CAT_BODY);
        g2d.fillArc(ox, oy, size, size, startAngle, arcAngle);

        // Eyes (small black dots)
        if (mouthAngle < 40) {
            g2d.setColor(Color.BLACK);
            int eyeSize = 3;
            switch (direction) {
                case "right":
                case "left":
                    g2d.fillOval(ox + size / 3 - 1, oy + size / 4, eyeSize, eyeSize);
                    g2d.fillOval(ox + size / 3 - 1, oy + size * 3 / 4 - eyeSize, eyeSize, eyeSize);
                    break;
                case "up":
                    g2d.fillOval(ox + size / 4,     oy + size / 3 - 1, eyeSize, eyeSize);
                    g2d.fillOval(ox + size * 3 / 4 - eyeSize, oy + size / 3 - 1, eyeSize, eyeSize);
                    break;
                case "down":
                    g2d.fillOval(ox + size / 4,     oy + size * 2 / 3, eyeSize, eyeSize);
                    g2d.fillOval(ox + size * 3 / 4 - eyeSize, oy + size * 2 / 3, eyeSize, eyeSize);
                    break;
            }
        }

        // P1 indicator in 2P mode
        if (gp.twoPlayerMode) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(gp.ui.smallFont.deriveFont(Font.BOLD, 8f));
            g2d.drawString("P1", drawX + gp.tileSize / 2 - 6, drawY - 2);
        }
    }
}
