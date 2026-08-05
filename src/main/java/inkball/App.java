/**
 * The main class for the Inkball game.
 * <p>
 * The {@code App} class extends {@link PApplet} and is responsible for the primary setup, rendering, and game loop
 * functionality for the Inkball game. This class handles the initialization of game components, loading of level configurations,
 * and management of game states such as pausing, scoring, and level transitions.
 * </p>
 *
 * <p>
 * The game consists of multiple levels, each defined in a JSON configuration file. The player draws lines to guide balls
 * to colored holes, and each correct or incorrect placement affects the player's score. Additionally, this class handles
 * UI components such as the score display and unspawned balls.
 * </p>
 */
package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class App extends PApplet {

    /** The player's score accumulated over the game.*/
    public int score = 0;

    /**
     * The time limit for the current level in seconds.
     * This value is specified per level in the JSON configuration file.
     */
    public int levelTime;

    /**
     * The frame count for tracking elapsed time since the level started.
     * This is reset when a new level loads.
     */
    public int frameCount = 0;

    /**
     * A modifier applied to the score when a ball is correctly placed in a hole.
     * The modifier value is loaded from the level configuration to adjust the score increase.
     */
    public float scoreIncreaseModifier;

    /**
     * A modifier applied to the score when a ball is incorrectly placed in a hole.
     * The modifier value is loaded from the level configuration to adjust the score decrease.
     */
    public float scoreDecreaseModifier;

    /**
     * A JSON object containing the base score increases for each ball color.
     * The base scores are combined with the {@code scoreIncreaseModifier} to calculate the final score increase.
     */
    public JSONObject scoreIncreaseBase;

  /**
   * A JSON object containing the base score decreases for each ball color.
   * The base penalties are combined with the {@code scoreDecreaseModifier} to calculate the final score decrease.
   */
    public JSONObject scoreDecreaseBase;

    /**
     * Indicates whether the game is paused.
     * When {@code true}, the game loop is halted, and player interactions like drawing lines are still allowed.
     */
    public boolean paused = false;

    /**
     * Indicates whether the game has ended for the current level.
     * This flag triggers the display of the game-over message and prevents further gameplay actions.
     */
    public boolean gameOver = false;
// Game components
    /** The list of active {@link Ball} objects in the game. */
    public ArrayList<Ball> balls;

    /** The list of {@link Hole} objects in the game where balls can be placed. */
    public ArrayList<Hole> holes;

    /** The list of {@link Wall} objects in the game that affect ball movement. */
    public ArrayList<Wall> walls;

    /** The list of spawner coordinates, each represented by an integer array for x and y positions. */
    public ArrayList<int[]> spawners;

    /**
     * A map of sprite images, where each key represents the element name (e.g., "wall", "ball")
     * and the value is the corresponding {@link PImage} object.
     */
    public HashMap<String, PImage> sprites = new HashMap<>();

    /**
     * The list of {@link Ball} objects that are queued to spawn but are not yet on the game board.
     * Only a limited number are displayed in the top-left area.
     */
    public ArrayList<Ball> unspawnedBalls;

    /** Maximum number of unspawned balls displayed in the top-left corner of the game screen. */
    public static final int MAX_UNSPAWNED_BALLS = 5;

    // Game layout constants
    /** Size of each grid cell in pixels, used for layout and positioning calculations. */
    public static final int CELLSIZE = 32;

    /** Height of the top bar used to display the scoreboard and timer. */
    public static final int TOPBAR_HEIGHT = 64;

    /** Width of the game window in pixels. */
    public static final int WIDTH = 576;

    /** Height of the game window in pixels. */
    public static final int HEIGHT = 640;

    /**
     * Height of the actual game area, excluding the top bar.
     * Calculated as {@code HEIGHT - TOPBAR_HEIGHT}.
     */
    public static final int GAME_HEIGHT = HEIGHT - TOPBAR_HEIGHT;

    /**
     * List of lines drawn by the player to direct the balls towards the holes.
     * Each line is represented as an {@link ArrayList} of integer arrays, where each array stores
     * the coordinates of the line segment.
     */
    public ArrayList<ArrayList<int[]>> lines = new ArrayList<>();

    /**
     * Represents the currently drawn line by the player.
     * This line is added to the {@code lines} list when drawing is complete.
     */
    public ArrayList<int[]> currentLine = null;

    // Level and spawn management
    /** The current level number, starting from 0. */
    public int CurrentLevel = 0;

    /** Timer for controlling the ball spawn interval. */
    public float spawnTimer;

    /** Interval between ball spawns, loaded from the level configuration. */
    public float spawnInterval;

    /** Path to the JSON configuration file containing level details. */
    public String configPath;

    /** Configuration JSON object loaded from the configuration file. */
    public JSONObject config;

    /** The currently spawning {@link Ball} object. */
    public Ball newBall;

    /**
     * Constructs an instance of the Inkball game application.
     * <p>
     * Sets the path to the JSON configuration file. This constructor prepares
     * the application for setup and is called once when the game starts.
     * </p>
     */
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Sets up the game window dimensions.
     * <p>
     * This method configures the size of the game window based on the
     * {@code WIDTH} and {@code HEIGHT} constants. It is called automatically by Processing.
     * </p>
     */
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Initializes the game state, including loading configuration, setting up the initial level,
     * and preparing game elements such as balls, walls, and holes.
     * <p>
     * This method is called once when the game starts and is also used to reset the game
     * when the player advances to a new level or restarts the current level.
     * </p>
     */
    public void setup() {
        frameRate(30);
        balls = new ArrayList<>();
        holes = new ArrayList<>();
        walls = new ArrayList<>();
        spawners = new ArrayList<>();
        unspawnedBalls = new ArrayList<>();

        config = loadJSONObject(configPath);
        JSONArray levels = config.getJSONArray("levels");

        JSONObject currentLevel = levels.getJSONObject(CurrentLevel);
        loadSprites();
        loadLevel(currentLevel);

        levelTime = currentLevel.getInt("time");
        spawnInterval = currentLevel.getFloat("spawn_interval");
        spawnTimer = spawnInterval;

        scoreIncreaseModifier = currentLevel.getFloat("score_increase_from_hole_capture_modifier");
        scoreDecreaseModifier = currentLevel.getFloat("score_decrease_from_wrong_hole_modifier");

        scoreIncreaseBase = config.getJSONObject("score_increase_from_hole_capture");
        scoreDecreaseBase = config.getJSONObject("score_decrease_from_wrong_hole");

        JSONArray ballsFromConfig = currentLevel.getJSONArray("balls");
        for (int i = 0; i < ballsFromConfig.size(); i++) {
            String ballColorName = ballsFromConfig.getString(i);
            PImage ballImage = getBallImageByColorName(ballColorName);
            unspawnedBalls.add(new Ball(0, 0, ballImage,ballColorName,this));
        }
        if (!unspawnedBalls.isEmpty()) {
            spawnNextBall();
        }

        frameCount=0;
    }

    /**
     * Spawns the next ball from the list of unspawned balls.
     * <p>
     * This method is triggered based on the {@code spawnTimer} interval. A ball is randomly assigned
     * to a spawner location and added to the list of active balls on the game board.
     * </p>
     */
    void spawnNextBall() {
        spawnTimer = spawnInterval;
        newBall = unspawnedBalls.remove(0);
        if (!spawners.isEmpty()) {
            int[] spawner = spawners.get((int) random(spawners.size()));
            newBall.setPosition(spawner[0], spawner[1]);
            balls.add(newBall);
        }
    }

    /**
     * Loads sprite images for game elements such as walls, balls, and holes.
     * <p>
     * This method populates the {@code sprites} map with images from the file paths specified
     * for each element type. These images are used for rendering the game elements.
     * </p>
     */
    private void loadSprites() {
        sprites.put("wall", loadImage("inkball/wall0.png"));
        sprites.put("orange_wall", loadImage("inkball/wall1.png"));
        sprites.put("blue_wall", loadImage("inkball/wall2.png"));
        sprites.put("green_wall", loadImage("inkball/wall3.png"));
        sprites.put("yellow_wall", loadImage("inkball/wall4.png"));
        sprites.put("grey_hole", loadImage("inkball/hole0.png"));
        sprites.put("orange_hole", loadImage("inkball/hole1.png"));
        sprites.put("blue_hole", loadImage("inkball/hole2.png"));
        sprites.put("green_hole", loadImage("inkball/hole3.png"));
        sprites.put("yellow_hole", loadImage("inkball/hole4.png"));
        sprites.put("tile", loadImage("inkball/tile.png"));  // Basic tile image
        sprites.put("grey", loadImage("inkball/ball0.png"));
        sprites.put("orange", loadImage("inkball/ball1.png"));
        sprites.put("blue", loadImage("inkball/ball2.png"));
        sprites.put("green", loadImage("inkball/ball3.png"));
        sprites.put("yellow", loadImage("inkball/ball4.png"));
        sprites.put("spawner", loadImage("inkball/entrypoint.png"));
    }

    /**
     * Loads and initializes the level layout and game components based on the provided configuration.
     * <p>
     * This method parses the layout file for the current level, creating {@link Wall}, {@link Hole},
     * and {@link Ball} objects and initializing the positions of each element.
     * </p>
     *
     * @param levelConfig The {@link JSONObject} containing level configuration details.
     */
   public  void loadLevel(JSONObject levelConfig) {
        String layoutFile = levelConfig.getString("layout");
        String[] lines = loadStrings(layoutFile);

        balls.clear();
        spawners.clear();
        unspawnedBalls.clear();

        for (int y = 0; y < lines.length; y++) {
            for (int x = 0; x < lines[y].length(); x++) {
                char tile = lines[y].charAt(x);
                int tileX = x * CELLSIZE;
                int tileY = y * CELLSIZE + TOPBAR_HEIGHT;
                switch (tile) {
                    case 'X':
                        walls.add(new Wall(tileX, tileY, sprites.get("wall")));
                        break;
                    case '1':
                        walls.add(new Wall(tileX, tileY, sprites.get("orange_wall")));
                        break;
                    case '2':
                        walls.add(new Wall(tileX, tileY, sprites.get("blue_wall")));
                        break;
                    case '3':
                        walls.add(new Wall(tileX, tileY, sprites.get("green_wall")));
                        break;
                    case '4':
                        walls.add(new Wall(tileX, tileY, sprites.get("yellow_wall")));
                        break;
                    case 'H':
                        char holeColor = lines[y].charAt(x + 1);
                        PImage holeImage = getHoleImageByColor(holeColor);
                        String holeColorName = getColorNameByCode(holeColor);
                        holes.add(new Hole(tileX, tileY, holeImage, holeColorName));
                        x++;
                        break;

                    case 'B':
                        char ballColor = lines[y].charAt(x + 1);
                        PImage ballImage = getBallImageByColor(ballColor);
                        String ballColorName = getColorNameByCode(ballColor);
                        balls.add(new Ball(tileX, tileY, ballImage, ballColorName,this));
                        x++;
                        break;
                    case 'S':
                        spawners.add(new int[]{tileX, tileY});
                        break;
                }
            }
        }
    }

    /**
     * Renders the main game screen, including the game board, scoreboard, and interactive elements.
     * <p>
     * This method is called repeatedly at a fixed frame rate. It updates the game state by
     * handling game-over conditions, rendering player-drawn lines, spawning balls, and
     * displaying pause or loss messages as needed.
     * </p>
     */
    public void draw() {
        background(200);
        drawBoard();
        drawScoreboard();

        if (unspawnedBalls.isEmpty() && balls.isEmpty()) {
            int remainingTimeInFrames = (levelTime * 30) - frameCount;
            int timeBonus = (int) ((remainingTimeInFrames / 30.0) / 0.067);
            score += timeBonus;
            loadNextLevel();
            return;
        }
            stroke(0);
            strokeWeight(10);
            for (ArrayList<int[]> line : lines) {
                for (int[] segment : line) {
                    line(segment[0], segment[1], segment[2], segment[3]);
                }
            }
        if (!paused) {
            spawnTimer -= 1.0 / 30;
            frameCount++;

            if (spawnTimer <= 0 && !unspawnedBalls.isEmpty()) {
                spawnNextBall();
            }

            for (Ball ball : balls) {
                ball.update(walls, lines, holes, TOPBAR_HEIGHT, WIDTH, HEIGHT);
            }
        }
        if ((levelTime - frameCount / 30) <= 0) {
            displayLossMessage();
            return;
        }
        drawUnspawnedBalls();
        for (int[] spawner : spawners) {
            image(sprites.get("spawner"), spawner[0], spawner[1], 32, 32);
        }
        for (Wall wall : walls) {
            wall.renderWall(this);
        }
        for (Hole hole : holes) {
            hole.renderHole(this);
        }
        for (Ball ball : balls) {
            ball.renderBall(this);
        }

        if (paused) {
            drawPausedScreen();
        }
    }

    /**
     * Displays the "Time's Up" message when the player runs out of time in the current level.
     * <p>
     * This method sets the {@code gameOver} flag to {@code true} and draws a centered "=== TIME'S UP ==="
     * message in the top bar to indicate that the level has ended.
     * </p>
     */
    public void displayLossMessage() {
        gameOver=true;
        fill(255);
        rect(0, 0, WIDTH, TOPBAR_HEIGHT);
        fill(0);
        textSize(30);

        String message = "=== TIME'S UP ===";
        float textWidth = textWidth(message);
        float xPosition = (WIDTH / 2) - (textWidth / 2);
        float yPosition = TOPBAR_HEIGHT / 2 + 10;

        text(message, xPosition, yPosition);
    }

    /**
     * Displays the pause message at the top of the screen when the game is paused.
     * <p>
     * The message "*** PAUSED ***" is centered in the top bar to inform the player that the game is paused.
     * </p>
     */
    public void drawPausedScreen() {
        fill(255);
        rect(0, 0, WIDTH, TOPBAR_HEIGHT);
        fill(0);
        textSize(30);

        float textWidth = textWidth("*** PAUSED ***");
        float xPosition = (WIDTH / 2) - (textWidth / 2);
        float yPosition = TOPBAR_HEIGHT / 2 + 10;

        text("*** PAUSED ***", xPosition, yPosition);
    }

    /**
     * Draws the game board background by placing tile images in a grid layout.
     * <p>
     * This method fills the game area with the default tile image, excluding the top bar.
     * </p>
     */
    private void drawBoard() {
        for (int y = 0; y < GAME_HEIGHT / CELLSIZE; y++) {
            for (int x = 0; x < WIDTH / CELLSIZE; x++) {
                image(sprites.get("tile"), x * CELLSIZE, y * CELLSIZE + TOPBAR_HEIGHT, CELLSIZE, CELLSIZE);
            }
        }
    }

    /**
     * Draws the score and remaining time in the top bar of the game window.
     * <p>
     * The score and time are displayed in the top-right corner. The remaining time
     * is calculated from the total level time minus the elapsed frames, and it decreases each frame.
     * </p>
     */
    void drawScoreboard() {
        fill(255);
        rect(0, 0, WIDTH, TOPBAR_HEIGHT);

        fill(0);
        textSize(20);
        if(score<0){
            score=0;
        }
        text("Score: " + score, WIDTH - 120, 30);
        text("Time: " + (levelTime - frameCount / 30), WIDTH - 120, 50);
        if(!unspawnedBalls.isEmpty()){
            String formattedSpawnTimer = String.format("%.1f", spawnTimer);
            text(formattedSpawnTimer, WIDTH - 350, 40);
        }
    }

    /**
     * Renders unspawned balls in a rectangular section in the top-left corner.
     * <p>
     * This method displays up to {@link #MAX_UNSPAWNED_BALLS} unspawned balls in a horizontal row
     * within the defined rectangle, each represented by an image corresponding to its color.
     * </p>
     */
    private void drawUnspawnedBalls() {
        int rectX = 10;
        int rectY = 10;
        int rectWidth = 190;
        int rectHeight = 45;

        noStroke();
        fill(0);

        rect(rectX, rectY, rectWidth, rectHeight);

        int ballX = rectX + 10;
        int ballY = rectY + (rectHeight / 2) - (CELLSIZE / 2)+5;

        for (int i = 0; i < Math.min(unspawnedBalls.size(), MAX_UNSPAWNED_BALLS); i++) {
            Ball ball = unspawnedBalls.get(i);
            ball.renderUnspawned(this, ballX, ballY);

            ballX += 35;

            if (ballX + CELLSIZE > rectX + rectWidth) {
                break;
            }
        }
    }
    public String getColorNameByCode(char colorCode) {
        switch (colorCode) {
            case '0': return "grey";
            case '1': return "orange";
            case '2': return "blue";
            case '3': return "green";
            case '4':
            default:return "yellow";
        }
    }
    private PImage getBallImageByColorName(String colorName) {
        switch (colorName) {
            case "grey": return sprites.get("grey");
            case "orange": return sprites.get("orange");
            case "blue": return sprites.get("blue");
            case "green": return sprites.get("green");
            case "yellow":
            default:return sprites.get("yellow");
        }
    }
    public PImage getHoleImageByColor(char colorCode) {
        switch (colorCode) {
            case '0': return sprites.get("grey_hole");
            case '1': return sprites.get("orange_hole");
            case '2': return sprites.get("blue_hole");
            case '3': return sprites.get("green_hole");
            case '4':
            default: return sprites.get("yellow_hole");
        }
    }
    private PImage getBallImageByColor(char colorCode) {
        switch (colorCode) {
            case '0': return sprites.get("grey");
            case '1': return sprites.get("orange");
            case '2': return sprites.get("blue");
            case '3': return  sprites.get("green");
            case '4':
            default: return sprites.get("yellow");
        }
    }

    /**
     * Advances to the next level and resets the game state.
     * <p>
     * This method increments the {@code currentLevel} variable, clears the game board,
     * and reloads elements based on the configuration of the next level.
     * </p>
     */
    void loadNextLevel() {
        CurrentLevel++;
        setup();
    }

    /**
     * Increases the score based on the color of the ball successfully placed in the correct hole.
     * <p>
     * This method retrieves the base score associated with the specified ball color from
     * {@code scoreIncreaseBase} and applies the {@code scoreIncreaseModifier} to calculate
     * the final score increase.
     * </p>
     *
     * @param ballColor A {@link String} representing the color of the ball that was successfully
     *                  placed in the correct hole. Accepted values include "grey", "orange",
     *                  "blue", "green", and "yellow".
     */
    public void increaseScore(String ballColor) {
        int baseIncrease = scoreIncreaseBase.getInt(ballColor);
        score += baseIncrease * scoreIncreaseModifier;
    }

    /**
     * Decreases the score based on the color of the ball incorrectly placed in a hole.
     * <p>
     * This method retrieves the base penalty associated with the specified ball color from
     * {@code scoreDecreaseBase} and applies the {@code scoreDecreaseModifier} to calculate
     * the final score decrease.
     * </p>
     *
     * @param ballColor A {@link String} representing the color of the ball that was placed
     *                  in the incorrect hole. Accepted values include "grey", "orange",
     *                  "blue", "green", and "yellow".
     */
    public void decreaseScore(String ballColor) {
        int baseDecrease = scoreDecreaseBase.getInt(ballColor);
        score -= baseDecrease * scoreDecreaseModifier;
    }

    /**
     * Re-adds a ball to the list of unspawned balls for later spawning.
     * <p>
     * This method is typically called when a ball needs to be respawned after a failed attempt.
     * It ensures that the ball is queued for re-spawning.
     * </p>
     *
     * @param ball The {@link Ball} object to be requeued for spawning.
     */
    public void requeueBall(Ball ball) {
        unspawnedBalls.add(ball);
        drawUnspawnedBalls();
    }

    /**
     * Handles key press events to manage game states like pausing and restarting the level.
     * <p>
     * Pressing the spacebar toggles the paused state, allowing the player to temporarily
     * halt the game. Pressing 'r' restarts the level from the initial configuration.
     * </p>
     */
    public void keyPressed() {
        if (key == ' ') {
            paused = !paused;
        }else if (key == 'r') {
            setup();
        }
    }

    /**
     * Handles the drawing of lines by the player as the mouse is dragged.
     * <p>
     * If the mouse is dragged within the game area (below the top bar), this method
     * adds line segments to the {@code currentLine} array, allowing players to create paths for balls.
     * </p>
     */
    public void mouseDragged() {
        if (keyPressed && keyCode == CONTROL) {
            return;
        }

        if (mouseY > TOPBAR_HEIGHT && currentLine != null) {
            currentLine.add(new int[]{pmouseX, pmouseY, mouseX, mouseY});
        }
    }

    /**
     * Handles mouse press events to start or remove lines based on the button pressed.
     * <p>
     * Left-clicking within the game area starts a new line, and right-clicking or
     * pressing control with the left button removes the last line drawn.
     * </p>
     */
    public void mousePressed() {
        if (mouseButton == RIGHT || (mouseButton == LEFT && keyPressed && keyCode == CONTROL)) {
            if (!lines.isEmpty()) {
                lines.remove(lines.size() - 1);
            }
        } else if (mouseButton == LEFT && mouseY > TOPBAR_HEIGHT) {
            currentLine = new ArrayList<>();
            lines.add(currentLine);
        }
    }

    /**
     * The main entry point for the Inkball game.
     * <p>
     * This method initializes and runs the game application by calling {@link PApplet#main(String)}.
     * </p>
     *
     * @param args Command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }
}
