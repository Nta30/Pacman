package ghost;

import main.GamePanel;
import java.awt.*;

// Clyde (Orange) - Chases when far, scatters when close
public class Clyde extends Ghost {

    public Clyde(GamePanel gp) {
        super(gp);
        ghostColor = new Color(255, 184, 82);
        spawnCol = 15;
        spawnRow = 14;
        scatterTargetX = 0;
        scatterTargetY = 29;
        releaseDelay = 360; // 6 seconds
    }

    @Override
    public int getChaseTargetX() {
        double dist = Math.sqrt(
            Math.pow(gridX - gp.player.gridX, 2) +
            Math.pow(gridY - gp.player.gridY, 2)
        );
        // If far (>8 tiles), chase Pacman. If close, go to scatter corner.
        if (dist > 8) {
            return gp.player.gridX;
        }
        return scatterTargetX;
    }

    @Override
    public int getChaseTargetY() {
        double dist = Math.sqrt(
            Math.pow(gridX - gp.player.gridX, 2) +
            Math.pow(gridY - gp.player.gridY, 2)
        );
        if (dist > 8) {
            return gp.player.gridY;
        }
        return scatterTargetY;
    }
}
