package tile;

import java.awt.image.BufferedImage;

public class Tile {
    public BufferedImage image;
    public boolean collision = false;
    public int type; // 0=outside, 1=wall, 2=dot, 3=power pellet, 4=ghost house, 5=empty, 6=ghost door
}
