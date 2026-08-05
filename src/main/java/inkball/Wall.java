package inkball;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a wall on the game board in the Inkball game.
 * <p>
 * Each {@code Wall} object is positioned at specified coordinates and displays a specified image.
 * Walls serve as obstacles for balls, causing them to reflect upon collision.
 * </p>
 */
public class Wall {

    /** The x-coordinate of the wall's position.
     *  The y-coordinate of the wall's position. */
    private float x, y;

    /** The image used to represent the wall on the game board. */
    private PImage img;

    /**
     * Constructs a {@code Wall} object at the specified position with the given image.
     *
     * @param x   The x-coordinate of the wall's position.
     * @param y   The y-coordinate of the wall's position.
     * @param img The image to represent the wall on the game board.
     */
    public Wall(float x, float y, PImage img) {
        this.x = x;
        this.y = y;
        this.img = img;
    }

    /**
     * Gets the x-coordinate of the wall's position.
     *
     * @return The x-coordinate of the wall.
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the wall's position.
     *
     * @return The y-coordinate of the wall.
     */
    public float getY() {
        return y;
    }
    /**
     * Renders the wall on the game board at its specified position.
     * <p>
     * The wall is drawn as a 32x32 pixel image at the {@code x} and {@code y} coordinates.
     * </p>
     *
     * @param p The {@link PApplet} instance used for rendering the image.
     */
    public void renderWall(PApplet p) {
        p.image(img, x, y, 32, 32);
    }
}
