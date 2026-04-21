package ghost;

import main.GamePanel;
import java.awt.*;

// Blinky (Red) - Chases Pacman directly
public class Blinky extends Ghost {

    public Blinky(GamePanel gp) {
        super(gp);
        ghostColor = Color.RED;
        spawnCol = 14;
        spawnRow = 11;
        scatterTargetX = 25;
        scatterTargetY = 0;
        releaseDelay = 0;
    }

    @Override
    public void setDefaultValues() {
        super.setDefaultValues();
        state = SCATTER; // Blinky starts outside
        direction = "left";
    }

    @Override
    public int getChaseTargetX() {
        return gp.player.gridX;
    }

    @Override
    public int getChaseTargetY() {
        return gp.player.gridY;
    }
}
