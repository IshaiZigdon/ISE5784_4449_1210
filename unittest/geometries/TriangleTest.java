package geometries;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * unit test for Geometries.Triangle
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class TriangleTest {
    /**
     * point 100 for testing
     */
    private final Point p100 = new Point(1, 0, 0);
    /**
     * point 010 for testing
     */
    private final Point p010 = new Point(0, 1, 0);
    /**
     * Test method for {@link Triangle#getNormal(Point)}
     */
    @Test
    void getNormal() {
        /// ============ Equivalence Partitions Tests ==============

        //TC1: simple test
        Point[] pts = {p100, p010, new Point(-1, 0, 0)};
        Triangle t1 = new Triangle(pts[0], pts[1], pts[2]); //
        // ensure there are no exceptions
        assertDoesNotThrow(() -> t1.getNormal(p100), "");
        // generate the test result
        Vector normal = t1.getNormal(p100);
        // ensure |result| = 1
        assertEquals(1, normal.length(), 0.000001, "Triangle's normal is not a unit vector");
        // ensure the result is orthogonal to the triangle
        for (int i = 0; i < 2; ++i)
            assertEquals(0d, normal.dotProduct(pts[i].subtract(pts[i == 0 ? 2 : 0])), 0.000001,
                    "Triangle's normal is not orthogonal to one of the vectors");
    }

    /**
     * Test method for {@link Triangle#findIntersections(Ray)}.
     */
    @Test
    public void testFindIntersections() {
        Triangle triangle = new Triangle(Point.ZERO,p100,p010);
        final Vector v001 = new Vector(0,0,1);

        /// ============ Equivalence Partitions Tests ==============

        //TC01: Ray intersects the triangle
        final Point p01 = new Point(0.3,0.3,-1);
        final var result01 = triangle.findIntersections(new Ray(p01,v001));
        var exp = List.of(new Point(0.3,0.3,0));
        assertEquals(exp,result01,"Triangle: TC01 didnt work");
        // **** Group: Ray doesn't intersect the triangle (but does the plane)
        //TC02: in front of edge
        final Point p02 = new Point(1,1,-1);
        assertNull(triangle.findIntersections(new Ray(p02,v001)),"Ray's line is outside of the triangle");
        //TC03: in front of vertex
        final Point p03 = new Point(2,-1,-1);
        assertNull(triangle.findIntersections(new Ray(p03,v001)),"Ray's line is outside of the triangle");

        // =============== Boundary Values Tests ==================

        //TC10: Ray intersects the triangle on the edge
        final Point p10 = new Point(0.5,0.5,-1);
        final var result10 = triangle.findIntersections(new Ray(p10,v001));
        exp = List.of(new Point(0.5,0.5,0));
        assertEquals(exp,result10,"Triangle: TC10 didnt work");
        //TC11: Ray intersects the triangle on the vertex
        final var result11 = triangle.findIntersections(new Ray(Point.ZERO,v001));
        exp = List.of(Point.ZERO);
        assertEquals(exp,result11,"Triangle: TC11 didnt work");
        //TC12: Ray intersects the triangle on the edge line but not on the triangle
        final Point p12 = new Point(-2,3,-1);
        assertNull(triangle.findIntersections(new Ray(p12,v001)),"Ray's line is outside of the triangle");
    }
}