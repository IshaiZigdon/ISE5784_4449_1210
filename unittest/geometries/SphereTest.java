package geometries;

import org.junit.jupiter.api.Test;
import primitives.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * unit test for Geometries.Sphere
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
class SphereTest {
    /**
     * Test method for {@link Sphere#getNormal(Point)}
     */
    @Test
    void getNormal() {
        // ============ Equivalence Partitions Tests ==============
        Point pt = new Point(1,0, 0);
        Sphere sphere = new Sphere(pt,1);
        Vector n = sphere.getNormal(Point.ZERO);
        //ensure |n| = 1
        assertEquals(1, n.length(),"Sphere's normal is not a unit vector");
        assertThrows(IllegalArgumentException.class,()->
                n.crossProduct(Point.ZERO.subtract(pt)),"Sphere: wrong normal values");

    }
}