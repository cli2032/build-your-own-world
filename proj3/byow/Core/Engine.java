package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.Edge;
import edu.princeton.cs.algs4.EdgeWeightedGraph;
import edu.princeton.cs.algs4.PrimMST;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class Engine {
    TERenderer ter = new TERenderer();
    public static final int WIDTH = 72;
    public static final int HEIGHT = 32;

    private static final double PERCENTFILLED = 0.6;
    private double netRoomArea = 0.0;
    private double areaRatio = 0.0;
    private int roomCount;
    private int numOpenings;
    //Maps opening points to the room number that they occur on
    private HashMap<Point, Integer> openingsMap = new HashMap<>();
    //Maps a number to each opening (to use for graph)
    private HashMap<Integer, Point> numberedOpeningsMap = new HashMap<>();
    private Point playerPos;
    private boolean gameOver = false;
    private boolean win = false;
    private boolean torchOn;
    private int torchRadius;

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        System.out.println("interact with input string" + input);
        input = input.toLowerCase();

        if (input.charAt(0) == 'l') {
            return interactWithInputString(loadWorld(input.substring(1)));
        }

        //get the seed from input string and create a Random object
        long seed = getSeed(input);
        Random r = new Random(seed);

        // ter.initialize(WIDTH, HEIGHT + 1, 0, 1);
        TETile[][] finalWorldFrame = fillVoid();
        drawWorld(finalWorldFrame, r);

        // ter.renderFrame(finalWorldFrame);
        play(input, finalWorldFrame);
        return finalWorldFrame;
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        String input = startMenu();
        interactWithInputString(input); //generates the world
    }

    /**
     * parse a string "n###s" to get seed
     */
    private long getSeed(String input) {
        int start = input.indexOf("n") + 1;
        int end = input.indexOf("s");
        return Long.parseLong(input.substring(start, end));
    }

    /**
     * main playing function of game
     */
    private void play(String input, TETile[][] world) {
        if (input.charAt(0) != 'l') {
            placePlayer(world);
            placeTarget(world);
        }
        TETile[][] fullWorld = TETile.copyOf(world);
        if (torchOn) {
            world = createTorchLitWorld(fullWorld);
        }
        // ter.renderFrame(world);
        String prevInput = input;
        int i = 0;
        int loopCounter = 0;
        String prev = "";
        while (!gameOver && !win && loopCounter < 80) {
            // StdDraw.setPenColor(Color.white);
            char next = 0;
            if (i < prevInput.length()) { //interact with the rest of the input string
                next = prevInput.charAt(i);
                i++;
            }
            if ((next != 0 /* || StdDraw.hasNextKeyTyped() */) && torchOn) {
                if (next == 0) {
                    // next = StdDraw.nextKeyTyped();
                    input += Character.toString(next);
                }
                move(next, fullWorld);
                world = createTorchLitWorld(fullWorld);
                if (prev.equals(":") && next == 'q') {
                    saveWorld(input);
                    // System.exit(0);
                }
                prev = Character.toString(next);
            } else if (next != 0 /* || StdDraw.hasNextKeyTyped() */) {
                if (next == 0) {
                    // next = StdDraw.nextKeyTyped();
                    input += Character.toString(next);
                }
                move(next, world);
                if (prev.equals(":") && next == 'q') {
                    saveWorld(input);
                    // System.exit(0);
                }
                prev = Character.toString(next);
            }
            //display text description of tile where mouse is hovering
            /* try {
                int x = (int) StdDraw.mouseX();
                int y = (int) StdDraw.mouseY() - 1;

                if (!torchOn || (Math.hypot(x - playerPos.getX(),
                        y - playerPos.getY()) <= torchRadius && torchOn)) {
                    StdDraw.text(2, 3, fullWorld[x][y].description());
                    StdDraw.show();
                    StdDraw.pause(15);
                }
            } catch (Exception e) {
                continue;
            }
            ter.renderFrame(world);
            StdDraw.show();

            if (win) {
                renderTextCenter("You won!");
                StdDraw.pause(500);
                System.exit(0);
            } else if (gameOver) {
                renderTextCenter("Game over! You lost!");
                StdDraw.pause(500);
                System.exit(0);
            }
            */
            loopCounter++;
        }
    }

    /**
     * displays start menu and returns the player's input string as entered into newSeedEntry().
     */
    private String startMenu() {
        StdDraw.clear();
        StdDraw.setPenColor(Color.black);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.text(WIDTH / 2, HEIGHT - HEIGHT / 4, "CS61B: The Project");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "New Game (Torch Disabled) (N)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 2, "New Game (Torch Enabled) (P)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 4, "Load Game (L)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 6, "Quit Game (Q)");
        StdDraw.show();
        String input = "";

        while (!StdDraw.hasNextKeyTyped()) {
            //do nothing
        }

        if (StdDraw.hasNextKeyTyped()) {
            String key = Character.toString(StdDraw.nextKeyTyped()).toLowerCase();

            if (key.equals("n")) {
                renderTextCenter("Enter a seed followed by 'S'");
            } else if (key.equals("p")) {
                torchOn = true;
                setTorch();
                renderTextCenter("Enter a seed followed by 'S'");
            } else if (key.equals("l")) {
                StdDraw.clear();
                interactWithInputString(loadWorld(""));
            } else if (key.equals("q")) {
                renderTextCenter("Bye!");
                StdDraw.pause(300);
                // System.exit(0);
            } else {
                return startMenu();
            }

            input = newSeedEntry();
        }

        return input;
    }

    /**
     * allows the user to type in a seed into the menu.
     */
    private String newSeedEntry() {
        //parse an input string for a seed to generate a world
        boolean hasSeed = false;
        String input = "";
        while (!hasSeed) {
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                input += Character.toString(key);
                if (key == 's') {
                    hasSeed = true;
                }
                renderTextCenter(input);
            }
        }

        return input;
    }

    /**
     * determines whether the torch feature is on for the game, and how large to make the torch.
     */
    private void setTorch() {
        //implement menu for torch light option
        StdDraw.clear();
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 7, "What size do you want the torch radius to be?");
        StdDraw.text(WIDTH / 2, HEIGHT / 2, "Small (a)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 3, "Medium (b)");
        StdDraw.text(WIDTH / 2, HEIGHT / 2 - 6, "Large (c)");
        StdDraw.show();

        boolean setTorch = false;

        while (!setTorch) {
            if (StdDraw.hasNextKeyTyped()) {
                String size = Character.toString(StdDraw.nextKeyTyped()).toLowerCase();

                if (size.equals("a")) {
                    torchRadius = 3;
                    setTorch = true;
                } else if (size.equals("b")) {
                    torchRadius = 5;
                    setTorch = true;
                } else if (size.equals("c")) {
                    torchRadius = 7;
                    setTorch = true;
                }
            }
        }
    }

    /**
     * renders a Torchlit view of a given complete world.
     */
    private TETile[][] createTorchLitWorld(TETile[][] fullWorld) {
        TETile[][] litWorld = fillVoid();
        for (int i = playerPos.getX() - torchRadius; i <= playerPos.getX() + torchRadius; i++) {
            for (int j = playerPos.getY() - torchRadius; j <= playerPos.getY() + torchRadius; j++) {
                if (i < litWorld.length && i >= 0 && j < litWorld[0].length && j >= 0
                        && (Math.hypot(i - playerPos.getX(),
                        j - playerPos.getY()) <= torchRadius)) {
                    litWorld[i][j] = fullWorld[i][j];
                }
            }
        }
        return litWorld;
    }

    /**
     * displays text in the center of the canvas
     */
    private void renderTextCenter(String text) {
        StdDraw.clear();
        StdDraw.text(WIDTH / 2, HEIGHT / 2, text);
        StdDraw.show();
    }

    /**
     * saves the seed to a textfile
     */
    private void saveWorld(String input) {
        /**@source: SaveDemo*/
        File f = new File("./save_world.txt");
        try {
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            String toSave = input;
            if (input.contains(":q")) {
                toSave = input.substring(0, input.indexOf(":"));
            }
            os.writeObject(toSave);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            // System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            // System.exit(0);
        }
    }

    /**
     * loads the world saved in the textfile
     */
    private String loadWorld(String newInput) {
        /**@source: SaveDemo*/
        File f = new File("./save_world.txt");
        try {
            FileInputStream fs = new FileInputStream(f);
            ObjectInputStream os = new ObjectInputStream(fs);
            String input = (String) os.readObject();
            return input + newInput;
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            // System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            // System.exit(0);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            // System.exit(0);
        }

        return null;
    }

    private void move(char direction, TETile[][] world) {
        if (direction == 'w') { //up
            if (world[playerPos.getX()][playerPos.getY() + 1] == Tileset.FLOOR) {
                world[playerPos.getX()][playerPos.getY() + 1] = Tileset.AVATAR;
                world[playerPos.getX()][playerPos.getY()] = Tileset.FLOOR;
                playerPos = new Point(playerPos.getX(), playerPos.getY() + 1);
            } else if (world[playerPos.getX()][playerPos.getY() + 1] == Tileset.FLOWER) {
                win = true;
            }
        } else if (direction == 's') { //down
            if (world[playerPos.getX()][playerPos.getY() - 1] == Tileset.FLOOR) {
                world[playerPos.getX()][playerPos.getY() - 1] = Tileset.AVATAR;
                world[playerPos.getX()][playerPos.getY()] = Tileset.FLOOR;
                playerPos = new Point(playerPos.getX(), playerPos.getY() - 1);
            } else if (world[playerPos.getX()][playerPos.getY() - 1] == Tileset.FLOWER) {
                win = true;
            }
        } else if (direction == 'a') { //left
            if (world[playerPos.getX() - 1][playerPos.getY()] == Tileset.FLOOR) {
                world[playerPos.getX() - 1][playerPos.getY()] = Tileset.AVATAR;
                world[playerPos.getX()][playerPos.getY()] = Tileset.FLOOR;
                playerPos = new Point(playerPos.getX() - 1, playerPos.getY());
            } else if (world[playerPos.getX() - 1][playerPos.getY()] == Tileset.FLOWER) {
                win = true;
            }
        } else if (direction == 'd') { //right
            if (world[playerPos.getX() + 1][playerPos.getY()] == Tileset.FLOOR) {
                world[playerPos.getX() + 1][playerPos.getY()] = Tileset.AVATAR;
                world[playerPos.getX()][playerPos.getY()] = Tileset.FLOOR;
                playerPos = new Point(playerPos.getX() + 1, playerPos.getY());
            } else if (world[playerPos.getX() + 1][playerPos.getY()] == Tileset.FLOWER) {
                win = true;
            }
        }
    }

    /**
     * draws the world
     */
    private void drawWorld(TETile[][] world, Random r) {
        //place random rooms
        while (areaRatio < PERCENTFILLED) {
            int x = r.nextInt(WIDTH - 6) + 2;
            int y = r.nextInt(HEIGHT - 6) + 2;
            Point corner = new Point(x, y);
            Room rm = new Room(corner, r);
            placeRoom(rm, world, r);

            //update ratios and areas
            netRoomArea += rm.getHeight() * rm.getWidth();
            areaRatio = netRoomArea / (WIDTH * HEIGHT);
        }

        //connect rooms with hallways
        connectRooms(world);
        drawWalls(world);
    }

    /**
     * put avatar in first floor tile you iterate over
     */
    private void placePlayer(TETile[][] world) {
        boolean placed = false;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y].equals(Tileset.FLOOR)) {
                    world[x][y] = Tileset.AVATAR;
                    playerPos = new Point(x, y);
                    placed = true;
                    break;
                }
            }
            if (placed) {
                break;
            }
        }
    }

    /**
     * place a target at a position with a floor tile opposite
     * world from the player
     */
    private void placeTarget(TETile[][] world) {
        boolean placed = false;
        for (int x = WIDTH - 1; x >= 0; x--) {
            for (int y = HEIGHT - 1; y >= 0; y--) {
                if (world[x][y].equals(Tileset.FLOOR)) {
                    world[x][y] = Tileset.FLOWER;
                    placed = true;
                    break;
                }
            }
            if (placed) {
                break;
            }
        }
    }

    /**
     * Method used to generate a blank starting world, filled with "NOTHING" tiles.
     *
     * @return the 2D TETile[][] representing the state of the world
     */
    private TETile[][] fillVoid() {
        TETile[][] start = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                start[i][j] = Tileset.NOTHING;
            }
        }
        return start;
    }

    /**
     * fill out the tiles of the world with a room of given dimensions
     *
     * @param rm,    the room to be added
     * @param world, the tiles that make up the world
     */
    private void placeRoom(Room rm, TETile[][] world, Random r) {
        int startX = rm.getCorner().getX();
        int endX = rm.getCorner().getX() + rm.getWidth();
        int startY = rm.getCorner().getY();
        int endY = rm.getCorner().getY() + rm.getHeight();
        if (isValid(world, startX, endX, startY, endY)) {
            roomCount++;
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    if (x == startX || x == endX - 1 || y == startY || y == endY - 1) {
                        world[x][y] = Tileset.WALL;
                    } else {
                        world[x][y] = Tileset.FLOOR;
                    }
                }
            }

            //add openings into openingsMap and numberedOpeningsMap
            List<Point> openings = generateOpenings(startX, endX - 1, startY, endY - 1, r);
            for (Point p : openings) {
                int x = p.getX();
                int y = p.getY();
                world[x][y] = Tileset.FLOWER;
                openingsMap.put(p, roomCount);
                numberedOpeningsMap.put(numOpenings, p);
                numOpenings++;
            }
        }
    }

    /**
     * checks if a room is valid to be placed
     *
     * @param world,  the tiles of the world
     * @param startX, endX, startY, endY, the dimension of the room
     */
    private boolean isValid(TETile[][] world, int startX, int endX, int startY, int endY) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                if (!world[x][y].equals(Tileset.NOTHING)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * determine the openings of a room
     *
     * @param startX, endX, startY, endY, the corners of the room, WALLS INCLUSIVE!
     * @param r,      Random object used to generate random integers
     * @return a list of the points along the perimeter of the room that are openings
     */
    private List<Point> generateOpenings(int startX, int endX, int startY, int endY, Random r) {
        ArrayList<Point> openings = new ArrayList<>();
        int countOpenings = r.nextInt(3) + 1; //1-3 openings
        //keeps track of which sides already have an opening
        HashSet<Integer> cache = new HashSet<>();
        int w = endX - startX - 1;
        int h = endY - startY - 1;
        while (cache.size() < countOpenings) {
            int side = r.nextInt(4);
            Point p;
            if (cache.contains(side)) {
                continue;
            }
            if (side == 0) { //top
                int x = r.nextInt(w) + 1 + startX;
                p = new Point(x, endY);
            } else if (side == 1) { //left
                int y = r.nextInt(h) + 1 + startY;
                p = new Point(startX, y);
            } else if (side == 2) { //bottom
                int x = r.nextInt(w) + 1 + startX;
                p = new Point(x, startY);
            } else { //right
                int y = r.nextInt(h) + 1 + startY;
                p = new Point(endX, y);
            }
            cache.add(side);
            openings.add(p);
        }
        return openings;
    }

    /**
     * Generates a list of connections to make between openings.
     *
     * @return an array of point pairings that represent connections to be drawn.
     * Points are represented by their Keys from numberedOpeningsMap.
     */
    private ArrayList<int[]> generateConnections() {
        EdgeWeightedGraph placeholder = new EdgeWeightedGraph(numberedOpeningsMap.size());

        for (int i = 0; i < numberedOpeningsMap.size() - 1; i++) {
            for (int j = i + 1; j < numberedOpeningsMap.size(); j++) {
                if (canConnect(i, j)) {
                    placeholder.addEdge(new Edge(i, j, getDistance(i, j)));
                }
            }
        }

        PrimMST mst = new PrimMST(placeholder);
        ArrayList<int[]> connectionList = new ArrayList<>();

        for (Edge e : mst.edges()) {
            int[] openingPair = new int[2];
            openingPair[0] = e.either();
            openingPair[1] = e.other(e.either());

            connectionList.add(openingPair);
        }

        return connectionList;
    }

    /**
     * connects rooms with hallways opening to opening
     */
    private void connectRooms(TETile[][] world) {
        ArrayList<int[]> connections = generateConnections();
        for (int[] hall : connections) {
            drawHallway(hall[0], hall[1], isVerticalOpening(hall[0], world), world);
        }
    }

    /**
     * Returns true if two openings can be connected (aka they are not from the same room)
     *
     * @param door1, door2, the openings that want to be connected
     * @return boolean determining if points can be connected
     */
    private boolean canConnect(int door1, int door2) {
        Point p1 = numberedOpeningsMap.get(door1);
        Point p2 = numberedOpeningsMap.get(door2);
        return !openingsMap.get(p1).equals(openingsMap.get(p2));
    }

    /**
     * draws the hallway floor connecting two points
     *
     * @param door1Key, start
     * @param door2Key, end
     */
    private void drawHallway(int door1Key, int door2Key,
                             boolean door1IsVertical, TETile[][] world) {
        int xDiff = xyDifference(door1Key, door2Key)[0];
        int yDiff = xyDifference(door1Key, door2Key)[1];
        int door1XVal = numberedOpeningsMap.get(door1Key).getX();
        int door1YVal = numberedOpeningsMap.get(door1Key).getY();
        int door2XVal = numberedOpeningsMap.get(door2Key).getX();
        int door2YVal = numberedOpeningsMap.get(door2Key).getY();

        if (door1IsVertical) {
            if (yDiff >= 0) {
                buildVerticalHallway(door1XVal, door1YVal, door2YVal, world);
                buildHorizontalHallway(door2YVal, door1XVal, door2XVal, world);
            } else {
                buildVerticalHallway(door1XVal, door1YVal, door2YVal, world);
                buildHorizontalHallway(door2YVal, door1XVal, door2XVal, world);
            }
        } else {
            if (xDiff >= 0) {
                buildHorizontalHallway(door1YVal, door1XVal, door2XVal, world);
                buildVerticalHallway(door2XVal, door1YVal, door2YVal, world);
            } else {
                buildHorizontalHallway(door1YVal, door1XVal, door2XVal, world);
                buildVerticalHallway(door2XVal, door1YVal, door2YVal, world);
            }
        }
    }

    private void buildVerticalHallway(int x, int yStart, int yEnd, TETile[][] world) {
        if (yStart < yEnd) {
            for (int i = yStart; i <= yEnd; i++) {
                world[x][i] = Tileset.FLOOR;
            }
        } else if (yStart > yEnd) {
            for (int i = yStart; i >= yEnd; i--) {
                world[x][i] = Tileset.FLOOR;
            }
        }
    }

    private void buildHorizontalHallway(int y, int xStart, int xEnd, TETile[][] world) {
        if (xStart < xEnd) {
            for (int i = xStart; i <= xEnd; i++) {
                world[i][y] = Tileset.FLOOR;
            }
        } else if (xStart > xEnd) {
            for (int i = xStart; i >= xEnd; i--) {
                world[i][y] = Tileset.FLOOR;
            }
        }
    }

    private boolean isVerticalOpening(int door1Key, TETile[][] world) {
        int xVal = numberedOpeningsMap.get(door1Key).getX();
        int yVal = numberedOpeningsMap.get(door1Key).getY();

        return !(world[xVal + 1][yVal].equals(Tileset.FLOOR)
                || world[xVal - 1][yVal].equals(Tileset.FLOOR));
    }

    /**
     * Returns the difference in x-coordinate and y-coordinate from point 1 to point 2.
     *
     * @param door1Key, the key of the first point
     * @param door2Key, the key of the second point
     * @return int[] which takes the form of [x-difference, y-difference]
     */
    private int[] xyDifference(int door1Key, int door2Key) {
        int[] result = new int[2];
        result[0] = numberedOpeningsMap.get(door2Key).getX()
                - numberedOpeningsMap.get(door1Key).getX();
        result[1] = numberedOpeningsMap.get(door2Key).getY()
                - numberedOpeningsMap.get(door1Key).getY();

        return result;
    }

    /**
     * draws walls around hallway floor tiles where necessary
     *
     * @param world, the tiles grid
     */
    private void drawWalls(TETile[][] world) {
        for (int x = 0; x < WIDTH; x++) {
            if (world[x][0].equals(Tileset.FLOOR)) {
                world[x][0] = Tileset.WALL;
            }
            if (world[x][HEIGHT - 1].equals(Tileset.FLOOR)) {
                world[x][HEIGHT - 1] = Tileset.WALL;
            }
        }
        for (int y = 0; y < HEIGHT; y++) {
            if (world[0][y].equals(Tileset.FLOOR)) {
                world[0][y] = Tileset.WALL;
            }
            if (world[WIDTH - 1][y].equals(Tileset.FLOOR)) {
                world[WIDTH - 1][y] = Tileset.WALL;
            }
        }
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (world[x][y].equals(Tileset.FLOOR)) {
                    addWall(x - 1, y + 1, world);
                    addWall(x - 1, y, world);
                    addWall(x - 1, y - 1, world);
                    addWall(x, y - 1, world);
                    addWall(x + 1, y - 1, world);
                    addWall(x + 1, y, world);
                    addWall(x + 1, y + 1, world);
                    addWall(x, y + 1, world);
                }
            }
        }
    }

    /**
     * add a wall tile to the x, y position if the tile
     * originally there is NOTHING
     *
     * @param x,     x position
     * @param y,     y position
     * @param world, the tiles grid
     */
    private void addWall(int x, int y, TETile[][] world) {
        if (world[x][y].equals(Tileset.NOTHING)) {
            world[x][y] = Tileset.WALL;
        }
    }

    /**
     * returns the distance between doors that is the same as
     * the length of the hallway.
     *
     * @param door1, door2 the respective Keys of the openings as recorded in numberedOpeningsMap.
     */
    private int getDistance(int door1, int door2) {
        Point p1 = numberedOpeningsMap.get(door1);
        Point p2 = numberedOpeningsMap.get(door2);
        int xDist = Math.abs(p1.getX() - p2.getX());
        int yDist = Math.abs(p1.getY() - p2.getY());
        return xDist + yDist;
    }

}
