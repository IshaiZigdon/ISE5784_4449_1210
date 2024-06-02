package geometries;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * unit test for Geometries.Cylinder
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class CylinderTest {
    /**
     * Default constructor for CylinderTest.
     */
    public CylinderTest() {/*just fot the javadoc*/}
    /**
     * Test method for {@link Cylinder#getNormal(Point)}
     */
    @Test
    void testGetNormal() {
        Vector v100 = new Vector(1, 0, 0);
        Cylinder c = new Cylinder(
                new Ray(Point.ZERO, v100), 1, 1);

        // ============ Equivalence Partitions Tests ==============

        //TC01: point on the side of the cylinder
        Point pHalf10 = new Point(0.5, 1, 0);
        // ensure there are no exceptions
        assertDoesNotThrow(() -> c.getNormal(pHalf10), "");
        // generate the test result
        Vector normal = c.getNormal(pHalf10);
        // ensure |result| = 1
        assertEquals(1, normal.length(), 0.000001, "TC01: Cylinder's normal is not a unit vector");
        // ensure the result is orthogonal to the cylinder
        assertEquals(0d, normal.dotProduct(v100),
                "TC01: Cylinder: wrong normal values");

        //TC02: point on base 1(not in the middle)
        Point p0half0 = new Point(0, 0.5, 0);
        assertDoesNotThrow(() -> c.getNormal(p0half0), "");
        // generate the test result
        Vector normal2 = c.getNormal(p0half0);
        // ensure |result| = 1
        assertEquals(1, normal2.length(), 0.000001, "TC02: Cylinder's normal is not a unit vector");
        // ensure the result is orthogonal to the Cylinder
        assertEquals(normal2, v100, ": TC02Cylinder: wrong normal values");

        //TC03: point on base 2(not in the middle)
        Point p1half0 = new Point(1, 0.5, 0);
        assertDoesNotThrow(() -> c.getNormal(p1half0), "");
        // generate the test result
        Vector normal3 = c.getNormal(p1half0);
        // ensure |result| = 1
        assertEquals(1, normal3.length(), 0.000001, "TC03: Cylinder's normal is not a unit vector");
        // ensure the result is orthogonal to the Cylinder
        assertEquals(normal3, v100, "TC03: Cylinder: wrong normal values");

        // =============== Boundary Values Tests ==================

        //TC10: point on base 1 IN THE MIDDLE
        assertDoesNotThrow(() -> c.getNormal(Point.ZERO), "");
        // generate the test result
        Vector normal4 = c.getNormal(Point.ZERO);
        // ensure |result| = 1
        assertEquals(1, normal4.length(), 0.000001, "TC10: Cylinder's normal is not a unit vector");
        // ensure the result is orthogonal to the Cylinder
        assertEquals(normal4, v100, "TC10: Cylinder: wrong normal values");

        //TC11: point on base 2 IN THE MIDDLE
        Point p100 = new Point(1, 0, 0);
        assertDoesNotThrow(() -> c.getNormal(p100), "");
        // generate the test result
        Vector normal5 = c.getNormal(p100);
        // ensure |result| = 1
        assertEquals(1, normal5.length(), 0.000001, "TC11: Cylinder's normal is not a unit vector");
        // ensure the result is orthogonal to the Cylinder
        assertEquals(normal5, v100, "TC11: Cylinder: wrong normal values");
    }

    /**
     * Test method for {@link Cylinder#findIntersections(Ray)}.
     */
    @Test
    public void testFindIntersections() {
    }
}