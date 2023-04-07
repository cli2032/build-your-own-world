package byow.Core;

import java.util.Random;

public class Room {

    private Point corner;
    private int width;
    private int height;

    /** generate a room of random width and height given a starting point
     *  that is the bottom left corner (a wall tile)
     * @param: corner, bottom left corner of room
     * @param: r, Random object (to generate random numbers)
     * @param: tiles, the world
     */
    public Room(Point corner, Random r) {
        this.corner = corner;
        this.width = r.nextInt(10) + 4;
        this.height = r.nextInt(10) + 4;

        //clip the room short if it goes outside the boundaries of the world
        if (corner.getX() + width >= Engine.WIDTH) {
            this.width = Engine.WIDTH - corner.getX() - 1;
        }
        if (corner.getY() + height >= Engine.HEIGHT) {
            this.height = Engine.HEIGHT - corner.getY() - 1;
        }
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Point getCorner() {
        return this.corner;
    }
}
