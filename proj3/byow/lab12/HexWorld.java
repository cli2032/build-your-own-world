package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    public static void addHexagon(TETile[][] World, Location p, TETile palette, int size) {
        int dummy = 0;

        for (int i = 0; i < size; i++) {
            putRow(World, p, palette, size + dummy);
            p.x -= 1;
            p.y += 1;
            dummy += 2;
        }

        dummy -= 2;
        p.x += 1;

        for (int i = 0; i < size; i++) {
            putRow(World, p, palette, size + dummy);
            p.x += 1;
            p.y += 1;
            dummy -= 2;
        }

    }

    public static void createTesselation() {

    }

    private static void putRow(TETile[][] World, Location p, TETile palette, int size) {
        for (int i = 0; i < size; i++) {
            World[p.x + i][p.y] = palette;
        }
    }

    private static class Location {
        int x;
        int y;

        Location(int givenx, int giveny) {
            this.x = givenx;
            this.y = giveny;
        }
    }



    private static final int WIDTH = 60;
    private static final int HEIGHT = 30;

    public static void main(String[] args) {
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        addHexagon(world, new Location(15, 15), Tileset.FLOWER, 4);

        // draws the world to the screen
        ter.renderFrame(world);
    }
}
