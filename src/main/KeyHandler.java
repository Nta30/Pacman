package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

    GamePanel gp;

    // Player 1 (Arrow keys)
    public boolean p1Up, p1Down, p1Left, p1Right;
    // Player 2 (WASD)
    public boolean p2Up, p2Down, p2Left, p2Right;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (gp.gameState) {
            case 0: titleInput(code);    break; // titleState
            case 1: settingInput(code);  break; // settingState
            case 2:                      break; // readyState (no input)
            case 3: playInput(code);     break; // playState
            case 4: pauseInput(code);    break; // pauseState
            case 5: gameOverInput(code); break; // gameOverState
            case 6: winInput(code);      break; // winState
        }
    }

    private void titleInput(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            gp.ui.commandNumber--;
            if (gp.ui.commandNumber < 0) gp.ui.commandNumber = 2;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            gp.ui.commandNumber++;
            if (gp.ui.commandNumber > 2) gp.ui.commandNumber = 0;
        }
        if (code == KeyEvent.VK_ENTER) {
            switch (gp.ui.commandNumber) {
                case 0: // Start → go to settings
                    gp.gameState = gp.settingState;
                    gp.ui.commandNumber = 0;
                    break;
                case 1: // How to Play (toggle instructions)
                    gp.ui.showInstructions = !gp.ui.showInstructions;
                    break;
                case 2: // Exit
                    System.exit(0);
                    break;
            }
        }
    }

    private void settingInput(int code) {
        // Navigate rows
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            gp.ui.settingRow--;
            if (gp.ui.settingRow < 0) gp.ui.settingRow = 2;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            gp.ui.settingRow++;
            if (gp.ui.settingRow > 2) gp.ui.settingRow = 0;
        }

        // Change values
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            switch (gp.ui.settingRow) {
                case 0: gp.twoPlayerMode = false; break;
                case 1: gp.difficulty = Math.max(0, gp.difficulty - 1); break;
            }
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            switch (gp.ui.settingRow) {
                case 0: gp.twoPlayerMode = true; break;
                case 1: gp.difficulty = Math.min(2, gp.difficulty + 1); break;
            }
        }

        // Start game (Enter or row 2 = NEXT)
        if (code == KeyEvent.VK_ENTER) {
            if (gp.ui.settingRow == 2) {
                startGame();
            } else {
                gp.ui.settingRow = 2; // focus NEXT button
            }
        }

        // Back
        if (code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.titleState;
            gp.ui.commandNumber = 0;
        }
    }

    private void startGame() {
        gp.score = 0;
        gp.lives = 3;
        gp.level = 1;
        gp.applyDifficulty();
        gp.tileManager.reloadMap();
        gp.setupLevel();
        gp.gameState = gp.readyState;
        gp.readyTimer = 0;
    }

    private void playInput(int code) {
        // Player 1 - Arrow keys
        if (code == KeyEvent.VK_UP)    p1Up = true;
        if (code == KeyEvent.VK_DOWN)  p1Down = true;
        if (code == KeyEvent.VK_LEFT)  p1Left = true;
        if (code == KeyEvent.VK_RIGHT) p1Right = true;

        // Player 2 - WASD
        if (code == KeyEvent.VK_W)     p2Up = true;
        if (code == KeyEvent.VK_S)     p2Down = true;
        if (code == KeyEvent.VK_A)     p2Left = true;
        if (code == KeyEvent.VK_D)     p2Right = true;

        // Handle P2 ghost direction in 2P mode
        if (gp.twoPlayerMode) {
            if (code == KeyEvent.VK_W) gp.setGhostPlayerDirection("up");
            if (code == KeyEvent.VK_S) gp.setGhostPlayerDirection("down");
            if (code == KeyEvent.VK_A) gp.setGhostPlayerDirection("left");
            if (code == KeyEvent.VK_D) gp.setGhostPlayerDirection("right");
        }

        // Pause
        if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.pauseState;
            gp.ui.commandNumber = 0;
        }
    }

    private void pauseInput(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            gp.ui.commandNumber--;
            if (gp.ui.commandNumber < 0) gp.ui.commandNumber = 2;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            gp.ui.commandNumber++;
            if (gp.ui.commandNumber > 2) gp.ui.commandNumber = 0;
        }
        if (code == KeyEvent.VK_ENTER) {
            switch (gp.ui.commandNumber) {
                case 0: // Continue
                    gp.gameState = gp.playState;
                    break;
                case 1: // Restart
                    startGame();
                    break;
                case 2: // Menu
                    gp.gameState = gp.titleState;
                    gp.ui.commandNumber = 0;
                    break;
            }
        }
        if (code == KeyEvent.VK_P || code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.playState;
        }
    }

    private void gameOverInput(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            gp.ui.commandNumber--;
            if (gp.ui.commandNumber < 0) gp.ui.commandNumber = 1;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            gp.ui.commandNumber++;
            if (gp.ui.commandNumber > 1) gp.ui.commandNumber = 0;
        }
        if (code == KeyEvent.VK_ENTER) {
            if (gp.ui.commandNumber == 0) startGame();
            else {
                gp.gameState = gp.titleState;
                gp.ui.commandNumber = 0;
            }
        }
    }

    private void winInput(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            gp.ui.commandNumber--;
            if (gp.ui.commandNumber < 0) gp.ui.commandNumber = 1;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            gp.ui.commandNumber++;
            if (gp.ui.commandNumber > 1) gp.ui.commandNumber = 0;
        }
        if (code == KeyEvent.VK_ENTER) {
            if (gp.ui.commandNumber == 0) { // Next level / Restart
                gp.level++;
                gp.tileManager.reloadMap();
                gp.setupLevel();
                gp.gameState = gp.readyState;
                gp.readyTimer = 0;
            } else { // Menu
                gp.gameState = gp.titleState;
                gp.ui.commandNumber = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP)    p1Up = false;
        if (code == KeyEvent.VK_DOWN)  p1Down = false;
        if (code == KeyEvent.VK_LEFT)  p1Left = false;
        if (code == KeyEvent.VK_RIGHT) p1Right = false;
        if (code == KeyEvent.VK_W)     p2Up = false;
        if (code == KeyEvent.VK_S)     p2Down = false;
        if (code == KeyEvent.VK_A)     p2Left = false;
        if (code == KeyEvent.VK_D)     p2Right = false;
    }
}
