package main;

import entity.Player;
import ghost.*;
import tile.TileManager;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel implements Runnable {

    // Screen settings
    public final int tileSize = 24;
    public final int maxScreenCol = 28;
    public final int maxScreenRow = 30;
    public final int screenWidth = maxScreenCol * tileSize;       // 672
    public final int screenHeight = maxScreenRow * tileSize + 72; // 792
    public final int mapOffsetX = 0;
    public final int mapOffsetY = 48;

    int fps = 60;

    // Systems
    public TileManager tileManager = new TileManager(this);
    public KeyHandler keyHandler = new KeyHandler(this);
    public CollisionChecker collisionChecker = new CollisionChecker(this);
    public UI ui = new UI(this);
    public Sound sound = new Sound();
    Thread gameThread;

    // Entities
    public Player player = new Player(this, keyHandler);
    public Ghost[] ghosts = new Ghost[4];

    // Game states
    public int gameState;
    public final int titleState    = 0;
    public final int settingState  = 1;
    public final int readyState    = 2;
    public final int playState     = 3;
    public final int pauseState    = 4;
    public final int gameOverState = 5;
    public final int winState      = 6;

    // Game data
    public int score = 0;
    public int lives = 3;
    public int level = 1;
    public int dotsRemaining = 0;
    public int totalDots = 0;

    // Mode & difficulty
    public boolean twoPlayerMode = false;
    public int difficulty = 1;         // 0=easy, 1=normal, 2=hard
    public int ghostBaseSpeed = 3;

    // Map selection - ĐÚNG VỚI FILE MAP CỦA BẠN
    public int selectedMap = 1;         // 1-6
    public String[] mapNames = {
            "CLASSIC",
            "MAP 01",
            "WORLD 01",
            "WORLD 02",
            "WORLD 03",
            "INTERIOR 01"
    };
    public String[] mapPaths = {
            "/map/pacman_classic",
            "/map/map01",
            "/map/world01",
            "/map/worldV2",
            "/map/worldV3",
            "/map/interior01"
    };

    // Power pellet
    public boolean powerPelletActive = false;
    public int frightTimer = 0;
    public int frightDuration = 360;

    // Ghost mode cycling
    public int ghostMode = 0;
    public int modeTimer = 0;
    public int modePhase = 0;
    public int[] scatterDurations = {420, 420, 300, 300};
    public int[] chaseDurations   = {1200, 1200, 1200, 1200};

    // Timers
    public int readyTimer = 0;
    public int readyDuration = 120;
    private int deathPauseTimer = 0;
    private boolean deathPause = false;
    private int ghostEatMultiplier = 1;
    public int winTimer = 0;

    // Sound settings
    public boolean musicOn = true;
    public boolean sfxOn = true;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyHandler);
        this.setFocusable(true);
    }

    public void setupGame() {
        gameState = titleState;
    }

    public void applyDifficulty() {
        switch (difficulty) {
            case 0:
                ghostBaseSpeed = 2;
                frightDuration = 480;
                break;
            case 1:
                ghostBaseSpeed = 3;
                frightDuration = 360;
                break;
            case 2:
                ghostBaseSpeed = 3;
                frightDuration = 240;
                scatterDurations = new int[]{300, 300, 240, 240};
                break;
        }
    }

    public void setupLevel() {
        tileManager.loadMap(mapPaths[selectedMap - 1]);
        dotsRemaining = tileManager.countDots();
        totalDots = dotsRemaining;

        player.setDefaultValues();

        ghosts[0] = new Blinky(this);
        ghosts[1] = new Pinky(this);
        ghosts[2] = new Inky(this);
        ghosts[3] = new Clyde(this);

        if (twoPlayerMode) {
            ghosts[0].isPlayerControlled = true;
        }

        for (Ghost g : ghosts) g.setDefaultValues();

        powerPelletActive = false;
        frightTimer = 0;
        modeTimer = 0;
        modePhase = 0;
        ghostMode = 0;
        ghostEatMultiplier = 1;
        deathPause = false;
        deathPauseTimer = 0;
        winTimer = 0;
    }

    public void resetAfterDeath() {
        player.setDefaultValues();
        for (Ghost g : ghosts) if (g != null) g.setDefaultValues();
        if (twoPlayerMode) ghosts[0].isPlayerControlled = true;

        powerPelletActive = false;
        frightTimer = 0;
        modeTimer = 0;
        modePhase = 0;
        ghostMode = 0;
        ghostEatMultiplier = 1;
    }

    public void setGhostPlayerDirection(String dir) {
        if (ghosts[0] != null && ghosts[0].isPlayerControlled) {
            ghosts[0].playerNextDirection = dir;
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / fps;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    public void update() {
        if (gameState == playState)      updatePlay();
        else if (gameState == readyState) {
            readyTimer++;
            if (readyTimer >= readyDuration) {
                readyTimer = 0;
                gameState = playState;
            }
        }
        else if (gameState == winState)  winTimer++;
    }

    private void updatePlay() {
        if (deathPause) {
            deathPauseTimer++;
            if (deathPauseTimer >= 90) {
                deathPause = false;
                deathPauseTimer = 0;
                resetAfterDeath();
                gameState = readyState;
                readyTimer = 0;
            }
            return;
        }

        updateGhostMode();
        player.update();
        if (player.deathAnimation) return;

        checkDotEating();

        for (Ghost g : ghosts) if (g != null) g.update();

        checkGhostCollisions();

        if (powerPelletActive) {
            frightTimer--;
            if (frightTimer <= 0) {
                powerPelletActive = false;
                ghostEatMultiplier = 1;
                for (Ghost g : ghosts) {
                    if (g != null && g.state == Ghost.FRIGHTENED) {
                        g.setState((ghostMode == 0) ? Ghost.SCATTER : Ghost.CHASE);
                    }
                }
            }
        }

        if (dotsRemaining <= 0) {
            gameState = winState;
            winTimer = 0;
            ui.commandNumber = 0;
        }
    }

    private void updateGhostMode() {
        if (powerPelletActive) return;
        modeTimer++;

        int phaseIndex = modePhase / 2;
        int currentDuration;
        if (ghostMode == 0) {
            currentDuration = (phaseIndex < scatterDurations.length) ?
                    scatterDurations[phaseIndex] : 300;
        } else {
            currentDuration = (phaseIndex < chaseDurations.length) ?
                    chaseDurations[phaseIndex] : Integer.MAX_VALUE;
        }

        if (modeTimer >= currentDuration) {
            modeTimer = 0;
            modePhase++;
            ghostMode = (ghostMode == 0) ? 1 : 0;

            for (Ghost g : ghosts) {
                if (g != null && (g.state == Ghost.SCATTER || g.state == Ghost.CHASE)) {
                    g.setState((ghostMode == 0) ? Ghost.SCATTER : Ghost.CHASE);
                    g.reverseDirection();
                }
            }
        }
    }

    private void checkDotEating() {
        if (!player.isAtGridPosition()) return;
        int col = player.gridX, row = player.gridY;
        if (col < 0 || col >= maxScreenCol || row < 0 || row >= maxScreenRow) return;

        int type = tileManager.mapTileNumber[col][row];
        if (type == 2) {
            tileManager.mapTileNumber[col][row] = 5;
            score += 10;
            dotsRemaining--;
            if (sfxOn) sound.playSE(1);
        } else if (type == 3) {
            tileManager.mapTileNumber[col][row] = 5;
            score += 50;
            dotsRemaining--;
            activatePowerPellet();
            if (sfxOn) sound.playSE(2);
        }
    }

    private void activatePowerPellet() {
        powerPelletActive = true;
        frightTimer = frightDuration;
        ghostEatMultiplier = 1;

        for (Ghost g : ghosts) {
            if (g != null && g.state != Ghost.EATEN &&
                    g.state != Ghost.IN_HOUSE && g.state != Ghost.EXITING_HOUSE) {
                g.setState(Ghost.FRIGHTENED);
                g.reverseDirection();
            }
        }
    }

    private void checkGhostCollisions() {
        for (Ghost g : ghosts) {
            if (g == null) continue;
            int dx = Math.abs(player.worldX - g.worldX);
            int dy = Math.abs(player.worldY - g.worldY);

            if (dx < tileSize * 0.7 && dy < tileSize * 0.7) {
                if (g.state == Ghost.FRIGHTENED) {
                    g.setState(Ghost.EATEN);
                    score += 200 * ghostEatMultiplier;
                    ghostEatMultiplier *= 2;
                    if (sfxOn) sound.playSE(3);
                } else if (g.state == Ghost.SCATTER || g.state == Ghost.CHASE) {
                    pacmanDeath();
                    return;
                }
            }
        }
    }

    private void pacmanDeath() {
        lives--;
        if (sfxOn) sound.playSE(4);

        if (lives <= 0) {
            gameState = gameOverState;
            ui.commandNumber = 0;
        } else {
            player.startDeathAnimation();
            deathPause = true;
            deathPauseTimer = 0;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, screenWidth, screenHeight);

        if (gameState == titleState || gameState == settingState) {
            ui.draw(g2d);
        } else {
            tileManager.draw(g2d);
            for (Ghost g1 : ghosts) if (g1 != null) g1.draw(g2d);
            player.draw(g2d);
            ui.draw(g2d);
        }
        g2d.dispose();
    }

    public void playMusic(int i) {
        if (!musicOn) return;
        sound.setFile(i);
        sound.play();
        sound.loop();
    }

    public void stopMusic() { sound.stop(); }
}