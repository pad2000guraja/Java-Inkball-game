package inkball;
import processing.core.PApplet;
import processing.core.PImage;

/**
 * Represents a hole on the game board in the Inkball game.
 * <p>
 * Each {@code Hole} object is positioned at specified coordinates, displays an image,
 * and is assigned a color. Balls are attracted to holes, and successful placement in
 * a matching-colored hole affects the score.
 * </p>
 */
public class Hole {

    /** The x-coordinate of the hole's position.
     * The y-coordinate of the hole's position. */
    private float x, y;

    /** The image used to represent the hole on the game board. */
    private PImage img;

    /** The color of the hole, used to determine if a ball is correctly captured. */
    private String color;

    /**
     * Constructs a {@code Hole} object at the specified position with the given image and color.
     *
     * @param x     The x-coordinate of the hole's position.
     * @param y     The y-coordinate of the hole's position.
     * @param img   The image to represent the hole on the game board.
     * @param color The color of the hole as a string.
     */
    public Hole(float x, float y, PImage img,String color) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.color=color;
    }

    /**
     * Gets the x-coordinate of the center of the hole.
     * <p>
     * The center is calculated by adding 32 to the x-coordinate, assuming a 64x64 pixel hole.
     * </p>
     *
     * @return The x-coordinate of the hole's center.
     */
    public float getCenterX() {
        return x + 32;
    }

    /**
     * Gets the y-coordinate of the center of the hole.
     * <p>
     * The center is calculated by adding 32 to the y-coordinate, assuming a 64x64 pixel hole.
     * </p>
     *
     * @return The y-coordinate of the hole's center.
     */
    public float getCenterY() {
        return y + 32;
    }

    /**
     * Gets the color of the hole.
     *
     * @return The color of the hole as a string.
     */
    public String getColor() {
        return color;
    }

    /**
     * Renders the hole on the game board at its specified position.
     * <p>
     * The hole is drawn as a 64x64 pixel image at the {@code x} and {@code y} coordinates.
     * </p>
     *
     * @param p The {@link PApplet} instance used for rendering the image.
     */
    public void renderHole(PApplet p) {
        p.image(img, x, y, 64, 64);
    }
}
