package ghost;

import main.GamePanel;
import java.awt.*;

// Pinky (Pink) - Targets 4 tiles ahead of Pacman
public class Pinky extends Ghost {

    public Pinky(GamePanel gp) {
        super(gp);
        ghostColor = new Color(255, 184, 255);
        spawnCol = 13;
        spawnRow = 14;
        scatterTargetX = 2;
        scatterTargetY = 0;
        releaseDelay = 0; // Released immediately
    }

    @Override
    public int getChaseTargetX() {
        int targetX = gp.player.gridX + getDx(gp.player.direction) * 4;
        // Classic bug: when Pacman faces up, also offset left by 4
        if ("up".equals(gp.player.direction)) {
            targetX -= 4;
        }
        return targetX;
    }

    @Override
    public int getChaseTargetY() {
        int targetY = gp.player.gridY + getDy(gp.player.direction) * 4;
        return targetY;
    }
}
