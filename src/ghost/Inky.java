package ghost;

import main.GamePanel;
import java.awt.*;

// Inky (Cyan) - Complex targeting using Blinky's position
public class Inky extends Ghost {

    public Inky(GamePanel gp) {
        super(gp);
        ghostColor = new Color(0, 255, 255);
        spawnCol = 11;
        spawnRow = 14;
        scatterTargetX = 27;
        scatterTargetY = 29;
        releaseDelay = 180; // 3 seconds
    }

    @Override
    public int getChaseTargetX() {
        // Vector from Blinky to 2 tiles ahead of Pacman, doubled
        int aheadX = gp.player.gridX + getDx(gp.player.direction) * 2;
        Ghost blinky = gp.ghosts[0];
        if (blinky != null) {
            return aheadX + (aheadX - blinky.gridX);
        }
        return gp.player.gridX;
    }

    @Override
    public int getChaseTargetY() {
        int aheadY = gp.player.gridY + getDy(gp.player.direction) * 2;
        Ghost blinky = gp.ghosts[0];
        if (blinky != null) {
            return aheadY + (aheadY - blinky.gridY);
        }
        return gp.player.gridY;
    }
}
