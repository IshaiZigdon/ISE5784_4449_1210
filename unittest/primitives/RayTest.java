package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * unit test for primitives.Ray
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class RayTest {
    /**
     * Default constructor for RayTest.
     */
    public RayTest() {/*just fot the javadoc*/}
    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     */
    @Test
    public void testConstructor() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: simple test
        assertDoesNotThrow(() -> new Ray(new Point(1, 2, 3), new Vector(1, 0, 0)), "TC01: Ray Constructor: failed");
    }

    /**
     * Test method for {@link Ray#getPoint(double)}
     */
    @Test
    void testGetPoint() {
        // ============ Equivalence Partitions Tests ==============
        //TC01: t positive
        Ray r1 = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
        Point p01 = r1.getPoint(2);
        assertEquals(new Point(3, 2, 3), p01, "TC01: wrong point");

        //TC02: t negative
        Ray r2 = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
        Point p02 = r2.getPoint(-2);
        assertEquals(new Point(-1, 2, 3), p02, "TC02: wrong point");

        // =============== Boundary Values Tests ==================
        //TC10: t is 0
        Ray r3 = new Ray(new Point(1, 2, 3), new Vector(1, 0, 0));
        Point p10 = r2.getPoint(0);
        assertEquals(r3.getHead(), p10, "TC10: wrong point");
    }

    /**
     * Test method for {@link Ray#equals(Object)}
     */
    @Test
    void testEquals() {
        // ============ Equivalence Partitions Tests ==============

        //TC01: simple test
        Point p123 = new Point(1, 2, 3);
        Vector v100 = new Vector(1, 0, 0);
        Ray r1 = new Ray(p123, v100);
        Ray r2 = new Ray(p123, v100);
        assertEquals(r1, r2, "TC01: Ray equals didn't work");
        Ray r3 = new Ray(new Point(1, 2, 4), new Vector(1, 0, 1));
        assertNotEquals(r1, r3, "TC01: Ray equals didn't work");
    }
}