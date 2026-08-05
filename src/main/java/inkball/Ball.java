package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;

import static processing.core.PApplet.dist;

/**
 * Represents a ball in the Inkball game.
 * <p>
 * Each {@code Ball} object moves around the game board, interacts with walls, lines, and holes,
 * and updates the player's score based on its color and correct placement in matching-colored holes.
 * </p>
 * The {@code Ball} class handles movement, collisions, and rendering of the ball on the game board.
 */
public class Ball {
    /** The x-coordinate of the ball's position. */
    public float x;

    /** The y-coordinate of the ball's position. */
    public float y;

    /** The ball's horizontal velocity. */
    public float vx;

    /** The ball's vertical velocity. */
    public float vy;

    /** The color of the ball, used to determine score changes upon capture. */
    private String color;

    /** The current image displayed for the ball.
     * The original image used for resizing when the ball is attracted to a hole.  */
    private PImage img,originalImg;

    /** The constant speed of the ball. */
    private static final float speed = 2;

    /** The radius of the ball, used for collision detection. */
    private static final float BALL_RADIUS = 16;

    /** Flag indicating if the ball has been captured. */
    public boolean isCaptured = false;

    /** Reference to the main game object to update score and requeue balls. */
    private App game;

    /**
     * Constructs a new {@code Ball} with the specified position, image, color, and game reference.
     * <p>
     * The ball is initialized with random x and y velocities based on a predefined speed.
     * </p>
     *
     * @param x     The initial x-coordinate of the ball.
     * @param y     The initial y-coordinate of the ball.
     * @param img   The image representing the ball.
     * @param color The color of the ball as a string.
     * @param game  The main game instance for handling score updates and ball requeueing.
     */
    public Ball(float x, float y, PImage img,String color,App game) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.color=color;
        this.originalImg = img.copy();
        this.vx = (Math.random() > 0.5) ? speed : -speed;
        this.vy = (Math.random() > 0.5) ? speed : -speed;
        this.game=game;
    }

    /**
     * Gets the x-coordinate of the ball.
     *
     * @return The x-coordinate of the ball.
     */
    public float getX() {
        return this.x;
    }

    /**
     * Gets the y-coordinate of the ball.
     *
     * @return The y-coordinate of the ball.
     */
    public float getY() {return this.y;}


    /**
     * Sets the position of the ball.
     *
     * @param x The new x-coordinate.
     * @param y The new y-coordinate.
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Updates the ball's position, handles wall and line collisions, and checks for proximity to holes.
     * <p>
     * If the ball is close to a hole, it is gradually attracted towards it and shrinks in size.
     * Once the ball is captured by the hole, the score is updated based on color match.
     * </p>
     *
     * @param walls        The list of walls on the game board for collision detection.
     * @param lines        The list of player-drawn lines for collision detection.
     * @param holes        The list of holes to check for attraction and capture.
     * @param topBarHeight The height of the top bar area.
     * @param width        The width of the game board.
     * @param height       The height of the game board.
     */
    public void update(ArrayList<Wall> walls, ArrayList<ArrayList<int[]>> lines, ArrayList<Hole> holes, int topBarHeight, int width, int height) {
        if (isCaptured) return;
        if (isCollidingWithWalls(x + vx, y, walls)) {
            vx = -vx;
        }
        if (isCollidingWithWalls(x, y + vy, walls)) {
            vy = -vy;
        }
        for (Hole hole : holes) {
            float distanceToHole = dist(x, y, hole.getCenterX(), hole.getCenterY());

            if (distanceToHole < 32) {
                float attractionX = (hole.getCenterX() - x) * 0.005f;
                float attractionY = (hole.getCenterY() - y) * 0.005f;

                vx += attractionX;
                vy += attractionY;

                float shrinkFactor = distanceToHole / 32;
                img.resize((int)(32 * shrinkFactor), (int)(32 * shrinkFactor));
            }

            if (distanceToHole < 5) {
                handleCapture(hole);
                return;
            }
        }
        x += vx;
        y += vy;

        if (x <= 0 || x >= width - 32) vx = -vx;
        if (y <= topBarHeight || y >= height - 32) vy = -vy;

        checkLineCollision(lines);
    }


    /**
     * Handles the capture of the ball by a hole.
     * <p>
     * If the ball color matches the hole color or the hole is grey, the score is increased.
     * If the colors do not match, the score is decreased, and the ball is requeued for spawning.
     * </p>
     *
     * @param hole The hole capturing the ball.
     */
    public void handleCapture(Hole hole) {
        if (color.equals(hole.getColor())|| color.equals("grey") || hole.getColor().equals("grey_hole")) {
            game.increaseScore(color);
        } else if((hole.getColor().equals("yellow_hole")||hole.getColor().equals("green_hole")||hole.getColor().equals("blue_hole")||hole.getColor().equals("orange_hole"))&& !hole.getColor().equals("grey_hole")||((color.equals("yellow")||color.equals("green")||color.equals("blue")||color.equals("orange"))&& !hole.getColor().equals("grey_hole")))  {
            game.decreaseScore(color);
            game.requeueBall(this);
        }
        isCaptured = true;
    }

    /**
     * Checks for collisions between the ball and player-drawn lines, reflecting the ball if a collision is detected.
     *
     * @param lines The list of lines drawn by the player.
     */
    private void checkLineCollision(ArrayList<ArrayList<int[]>> lines) {
        for (ArrayList<int[]> line : lines) {
            for (int i = 0; i < line.size() - 1; i++) {
                int[] p1 = line.get(i);
                int[] p2 = line.get(i + 1);

                if (isCollidingWithLineSegment(p1, p2)) {
                    reflectBall(p1, p2);
                    lines.remove(line);
                    return;
                }
            }
        }
    }


    /**
     * Determines if the ball is colliding with a line segment based on its position and radius.
     *
     * @param p1 The starting point of the line segment.
     * @param p2 The ending point of the line segment.
     * @return {@code true} if the ball is colliding with the line segment; {@code false} otherwise.
     */
    private boolean isCollidingWithLineSegment(int[] p1, int[] p2) {
        float distToLine = distanceToLine(x + vx, y + vy, p1[0], p1[1], p2[0], p2[1]);
        float segmentLength = dist(p1[0], p1[1], p2[0], p2[1]);
        float distP1ToBall = dist(p1[0], p1[1], x + vx, y + vy);
        float distP2ToBall = dist(p2[0], p2[1], x + vx, y + vy);

        return (distToLine < BALL_RADIUS && (distP1ToBall + distP2ToBall <= segmentLength + BALL_RADIUS));
    }

    /**
     * Calculates the shortest distance from the ball to a line segment.
     *
     * @param bx The x-coordinate of the ball.
     * @param by The y-coordinate of the ball.
     * @param x1 The x-coordinate of the start point of the line segment.
     * @param y1 The y-coordinate of the start point of the line segment.
     * @param x2 The x-coordinate of the end point of the line segment.
     * @param y2 The y-coordinate of the end point of the line segment.
     * @return The shortest distance from the ball to the line segment.
     */
    private float distanceToLine(float bx, float by, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) return dist(bx, by, x1, y1);

        float t = ((bx - x1) * dx + (by - y1) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));

        float projX = x1 + t * dx;
        float projY = y1 + t * dy;

        return dist(bx, by, projX, projY);
    }

    /**
     * Reflects the ball's velocity upon colliding with a line segment.
     *
     * @param p1 The start point of the line segment.
     * @param p2 The end point of the line segment.
     */
    public void reflectBall(int[] p1, int[] p2) {
        float dx = p2[0] - p1[0];
        float dy = p2[1] - p1[1];
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float nx = -dy / length;
        float ny = dx / length;

        float dotProduct = vx * nx + vy * ny;
        vx = vx - 2 * dotProduct * nx;
        vy = vy - 2 * dotProduct * ny;
    }

    /**
     * Renders the ball on the game board if it has not been captured.
     *
     * @param p The PApplet instance used for drawing.
     */
    public void renderBall(PApplet p) {
        if (!isCaptured) {
            p.pushMatrix();
            p.translate(x + BALL_RADIUS, y + BALL_RADIUS);
            p.image(originalImg, -BALL_RADIUS, -BALL_RADIUS, BALL_RADIUS * 2, BALL_RADIUS * 2);
            p.popMatrix();
        }
    }

    /**
     * Renders the unspawned ball in a minimized format in the top-left corner.
     *
     * @param p The PApplet instance used for drawing.
     * @param x The x-coordinate for rendering.
     * @param y The y-coordinate for rendering.
     */
    public void renderUnspawned(PApplet p, float x, float y) {
        p.image(originalImg, x, y, 25, 25);
    }


    /**
     * Checks if the ball is colliding with any wall on the game board.
     *
     * @param nextX The next x-coordinate based on the ball's velocity.
     * @param nextY The next y-coordinate based on the ball's velocity.
     * @param walls The list of walls to check for collisions.
     * @return {@code true} if the ball is colliding with a wall; {@code false} otherwise.
     */
    public boolean isCollidingWithWalls(float nextX, float nextY, ArrayList<Wall> walls) {
        for (Wall wall : walls) {
            if (nextX < wall.getX() + 32 && nextX + 32 > wall.getX() &&
                    nextY < wall.getY() + 32 && nextY + 32 > wall.getY()) {
                return true;
            }
        }
        return false;
    }
}
