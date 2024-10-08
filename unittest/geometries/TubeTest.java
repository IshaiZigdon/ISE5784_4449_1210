package geometries;

import geometries.Intersectable.GeoPoint;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * unit test for Geometries.Tube
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class TubeTest {
    /**
     * vector 100 for testing
     */
    private static final Vector v100 = new Vector(1, 0, 0);

    /**
     * Default constructor for TubeTest.
     */
    public TubeTest() {/*just fot the javadoc*/}

    /**
     * Test method for {@link Tube#getNormal(Point)}
     */
    @Test
    void getNormal() {
        Point pM110 = new Point(-1, 1, 0);
        Point p100 = new Point(1, 0, 0);
        Tube t = new Tube(new Ray(p100, v100), 1);
        // ============ Equivalence Partitions Tests ==============

        //TC1: simple test
        // ensure there are no exceptions
        assertDoesNotThrow(() -> t.getNormal(pM110), "");
        // generate the test result
        Vector normal = t.getNormal(pM110);
        // ensure |result| = 1
        assertEquals(1, normal.length(), 0.000001, "Tube's normal is not a unit vector");
        // ensure the result is orthogonal to the tube
        assertEquals(0d, normal.dotProduct(v100),
                "Tube: wrong normal values");

        // =============== Boundary Values Tests ==================

        Point p110 = new Point(1, 1, 0);
        /*
        TC2: vector between the points is vertical to the direction vector
         ensure there are no exceptions
        */
        assertDoesNotThrow(() -> t.getNormal(p110), "");
        // generate the test result
        Vector normal2 = t.getNormal(p110);
        // ensure |result| = 1
        assertEquals(1, normal2.length(), 0.000001, "Tube's normal is not a unit vector");
        // ensure the result is orthogonal to the tube
        assertThrows(IllegalArgumentException.class, () ->
                normal2.crossProduct(p110.subtract(p100)), "Tube: wrong normal values");
    }

    /**
     * Test method for {@link Tube#findIntersections(Ray)}.
     */
    @Test
    public void testFindGeoIntersectionsHelper() {
        Point p0 = new Point(-1, 0, 0);
        Tube tube = new Tube(new Ray(p0, v100), 1);

        // ============ Equivalence Partitions Tests ==============

        Vector v101 = new Vector(1, 0, 1);
        // TC01: no intersection points
        Ray r01 = new Ray(new Point(-5, 5, -5), v101);
        assertNull(tube.findGeoIntersections(r01), "TC01: didn't return null");

        // **** Group: Ray's line crosses the Tube (but not the center)

        Vector v1M11 = new Vector(1, -1, 1);
        // Points of intersection
        Point p010 = new Point(0, 1, 0);
        Point p101 = new Point(1, 0, 1);
        var exp = List.of(new GeoPoint(tube, p010), new GeoPoint(tube, p101));
        // TC02: start before the tube
        Ray r02 = new Ray(new Point(-1, 2, -1), v1M11);
        List<GeoPoint> result02 = tube.findGeoIntersections(r02);
        assertEquals(2, result02.size(), "TC02: wrong number of intersections");
        assertEquals(exp, result02, "TC02: wrong points");
        // TC03: start on the tube and goes inside
        Ray r03 = new Ray(p010, v1M11);
        List<GeoPoint> result03 = tube.findGeoIntersections(r03);
        exp = List.of(new GeoPoint(tube, p101));
        assertEquals(1, result03.size(), "TC03: wrong number of intersections");
        assertEquals(exp, result03, "TC03: wrong points");
        // TC04: start inside the tube
        Ray r04 = new Ray(new Point(0.5, 0.5, 0.5), v1M11);
        List<GeoPoint> result04 = tube.findGeoIntersections(r04);
        // exp = List.of(p101);
        assertEquals(1, result04.size(), "TC04: wrong number of intersections");
        assertEquals(exp, result04, "TC04: wrong points");
        // TC05: start on the tube and goes outside
        Ray r05 = new Ray(p101, v1M11);
        assertNull(tube.findGeoIntersections(r05), "TC05: didn't return null");
        // TC06: start after the tube
        Ray r06 = new Ray(new Point(2, -1, 2), v1M11);
        assertNull(tube.findGeoIntersections(r06), "TC06: didn't return null");

        // =============== Boundary Values Tests ==================

        // **** Group: ray line orthogonal to axis

        // TC07: ray line is on the tube
        assertNull(tube.findGeoIntersections(new Ray(new Point(1, 0, 1), v100)), "TC07: didn't return null");
        // TC08: ray line is on the axis
        assertNull(tube.findGeoIntersections(new Ray(new Point(1, 0, 0), v100)), "TC08: didn't return null");
        // TC09: ray line is inside the tube in the direction of the axis
        assertNull(tube.findGeoIntersections(new Ray(new Point(0.5, 0, 0), v100)), "TC09: didn't return null");
        // TC10: ray line is outside the tube in the direction of the axis
        assertNull(tube.findGeoIntersections(new Ray(new Point(10, 10, 10), v100)), "TC10: didn't return null");

        // **** Group: Ray's line vertical to axis line(but not going through the center axis)

        Vector v001 = new Vector(0, 0, 1);
        // Points of intersection
        Point p01 = new Point(1, 0.5, -0.8660254037844386);
        Point p02 = new Point(1, 0.5, 0.8660254037844384);
        // TC11: starts before the tube (2 points)
        Ray ray11 = new Ray(new Point(1, 0.5, -2), v001);
        List<GeoPoint> result11 = tube.findGeoIntersections(ray11);
        exp = List.of(new GeoPoint(tube, p01), new GeoPoint(tube, p02));
        assertEquals(2, result11.size(), "TC11: wrong number of intersections");
        assertEquals(exp, result11, "TC11: wrong points");
        // TC12: starts on the tube and goes inside (1 point)
        Ray ray12 = new Ray(p01, v001);
        List<GeoPoint> result12 = tube.findGeoIntersections(ray12);
        exp = List.of(new GeoPoint(tube, p02));
        assertEquals(1, result12.size(), "TC12: wrong number of intersections");
        assertEquals(exp, result12, "TC12: wrong points");
        // TC13: starts inside the tube (1 point)
        Ray ray13 = new Ray(new Point(1, 0.5, -0.5), v001);
        List<GeoPoint> result13 = tube.findGeoIntersections(ray13);
        // exp = List.of(p02);
        assertEquals(1, result13.size(), "TC13: wrong number of intersections");
        assertEquals(exp, result13, "TC13: wrong points");
        // TC14: starts on the tube and goes outside (0 points)
        Ray ray14 = new Ray(p02, v001);
        assertNull(tube.findGeoIntersections(ray14), "TC14: didn't return null");
        // TC15: starts after the tube (0 points)
        Ray ray15 = new Ray(new Point(1, 0.5, 2), v001);
        assertNull(tube.findGeoIntersections(ray15), "TC15: didn't return null");

        // **** Group: Ray's line goes through the axis and vertical to axis line

        // Points of intersection
        Point p1 = new Point(1, 0, -1);
        Point p2 = new Point(1, 0, 1);
        // TC16: starts before the tube (2 points)
        Ray ray16 = new Ray(new Point(1, 0, -2), v001);
        List<GeoPoint> result16 = tube.findGeoIntersections(ray16);
        exp = List.of(new GeoPoint(tube, p1), new GeoPoint(tube, p2));
        assertEquals(2, result16.size(), "TC16: wrong number of intersections");
        assertEquals(exp, result16, "TC16: wrong points");
        // TC17: starts on the tube (1 point)
        Ray ray17 = new Ray(p1, v001);
        List<GeoPoint> result17 = tube.findGeoIntersections(ray17);
        exp = List.of(new GeoPoint(tube, p2));
        assertEquals(1, result17.size(), "TC17: wrong number of intersections");
        assertEquals(exp, result17, "TC17: wrong points");
        // TC18: starts inside the tube (1 point)
        Ray ray18 = new Ray(new Point(1, 0, -0.5), v001);
        List<GeoPoint> result18 = tube.findGeoIntersections(ray18);
        // exp = List.of(p2);
        assertEquals(1, result18.size(), "TC18: wrong number of intersections");
        assertEquals(exp, result18, "TC18: wrong points");
        // TC19: starts on the axis (1 point)
        Ray ray19 = new Ray(new Point(1, 0, 0), v001);
        List<GeoPoint> result19 = tube.findGeoIntersections(ray19);
        // exp = List.of(p2);
        assertEquals(1, result19.size(), "TC19: wrong number of intersections");
        assertEquals(exp, result19, "TC19: wrong points");
        // TC20: starts on the tube and goes outside (0 points)
        Ray ray20 = new Ray(p2, v001);
        assertNull(tube.findGeoIntersections(ray20), "TC20: didn't return null");
        // TC21: start after the tube (0 points)
        Ray ray21 = new Ray(new Point(1, 0, 2), v001);
        assertNull(tube.findGeoIntersections(ray21), "TC21: didn't return null");

        // **** Group: Ray's line not vertical to axis and crosses the Tube and goes through the center

        // Points of intersection
        Point p11 = new Point(0.2928932188134525, 0.7071067811865475, -0.7071067811865475);
        Point p12 = new Point(1.7071067811865475, -0.7071067811865475, 0.7071067811865475);
        exp = List.of(new GeoPoint(tube, p11), new GeoPoint(tube, p12));
        // TC22: starts before the tube (2 points)
        Ray ray22 = new Ray(new Point(0, 1, -1), v1M11);
        List<GeoPoint> result22 = tube.findGeoIntersections(ray22);
        assertEquals(2, result22.size(), "TC22: wrong number of intersections");
        assertEquals(exp, result22, "TC22: wrong points");
        // TC23: starts on the tube (1 point)
        Ray ray23 = new Ray(p11, v1M11);
        List<GeoPoint> result23 = tube.findGeoIntersections(ray23);
        exp = List.of(new GeoPoint(tube, p12));
        assertEquals(1, result23.size(), "TC23: wrong number of intersections");
        assertEquals(exp, result23, "TC23: wrong points");
        // TC24: starts inside the tube (1 point)
        Ray ray24 = new Ray(new Point(0.5, 0.5, -0.5), v1M11);
        List<GeoPoint> result24 = tube.findGeoIntersections(ray24);
        assertEquals(1, result24.size(), "TC24: wrong number of intersections");
        assertEquals(exp, result24, "TC24: wrong points");
        // TC25: starts on the axis (1 point)
        Ray ray25 = new Ray(new Point(1, 0, 0), v1M11);
        List<GeoPoint> result25 = tube.findGeoIntersections(ray25);
        assertEquals(1, result25.size(), "TC25: wrong number of intersections");
        assertEquals(exp, result25, "TC25: wrong points");
        // TC26: starts on the tube and goes outside (0 points)
        Ray ray26 = new Ray(p12, v1M11);
        assertNull(tube.findGeoIntersections(ray26), "TC26: didn't return null");
        // TC27: starts after the tube (0 points)
        Ray ray27 = new Ray(new Point(2, -1, 1), v1M11);
        assertNull(tube.findGeoIntersections(ray27), "TC27: didn't return null");

        // **** Group: Ray's line not vertical to axis and crosses the Tube and goes through the center

        // Points of intersection
        Point p21 = new Point(-1.7071067811865475, 0.7071067811865475, -0.7071067811865475);
        Point p22 = new Point(-0.29289321881345254, -0.7071067811865475, 0.7071067811865475);
        exp = List.of(new GeoPoint(tube, p21), new GeoPoint(tube, p22));
        // TC28: starts before the tube (2 points)
        Ray ray28 = new Ray(new Point(-2, 1, -1), v1M11);
        List<GeoPoint> result28 = tube.findGeoIntersections(ray28);
        assertEquals(2, result28.size(), "TC28: wrong number of intersections");
        assertEquals(exp, result28, "TC28: wrong points");
        // TC29: starts on the tube (1 point)
        Ray ray29 = new Ray(p21, v1M11);
        exp = List.of(new GeoPoint(tube, p22));
        List<GeoPoint> result29 = tube.findGeoIntersections(ray29);
        assertEquals(1, result29.size(), "TC29: wrong number of intersections");
        assertEquals(exp, result29, "TC29: wrong points");
        // TC30: starts inside the tube (1 point)
        Ray ray30 = new Ray(new Point(-0.5, -0.5, 0.5), v1M11);
        List<GeoPoint> result30 = tube.findGeoIntersections(ray30);
        assertEquals(1, result30.size(), "TC30: wrong number of intersections");
        assertEquals(exp, result30, "TC30: wrong points");
        // TC31: starts on p0(1 point)
        Ray ray31 = new Ray(p0, v1M11);
        List<GeoPoint> result31 = tube.findGeoIntersections(ray31);
        assertEquals(1, result31.size(), "TC31: wrong number of intersections");
        assertEquals(exp, result31, "TC31: wrong points");
        // TC32: starts on the tube and goes outside (0 points)
        Ray ray32 = new Ray(p22, v1M11);
        assertNull(tube.findGeoIntersections(ray32), "TC32: didn't return null");
        // TC33: starts after the tube (0 points)
        Ray ray33 = new Ray(new Point(2, -2, 2), v1M11);
        assertNull(tube.findGeoIntersections(ray33), "TC33: didn't return null");


        // **** Group: Ray's line vertical to axis line and goes through the p0

        // Points of intersection
        Point p31 = new Point(-1, 0, -1);
        Point p32 = new Point(-1, 0, 1);
        exp = List.of(new GeoPoint(tube, p31), new GeoPoint(tube, p32));
        //TC34: starts before the tube(2 points)
        Ray ray34 = new Ray(new Point(-1, 0, -2), v001);
        List<GeoPoint> result34 = tube.findGeoIntersections(ray34);
        assertEquals(2, result34.size(), "TC34: wrong number of intersections");
        assertEquals(exp, result34, "TC34: wrong points");
        //TC35: starts on the tube(1 point)
        Ray ray35 = new Ray(new Point(-1, 0, -1), v001);
        List<GeoPoint> result35 = tube.findGeoIntersections(ray35);
        exp = List.of(new GeoPoint(tube, p32));
        assertEquals(1, result35.size(), "TC35: wrong number of intersections");
        assertEquals(exp, result35, "TC35: wrong points");
        //TC36: starts inside the tube(1 point)
        Ray ray36 = new Ray(new Point(-1, 0, -0.5), v001);
        List<GeoPoint> result36 = tube.findGeoIntersections(ray36);
        assertEquals(1, result36.size(), "TC36: wrong number of intersections");
        assertEquals(exp, result36, "TC36: wrong points");
        //TC37: starts on p0(1 point)
        Ray ray37 = new Ray(p0, v001);
        List<GeoPoint> result37 = tube.findGeoIntersections(ray37);
        assertEquals(1, result37.size(), "TC37: wrong number of intersections");
        assertEquals(exp, result37, "TC37: wrong points");
        //TC38: starts on the tube and goes outside (0 point)
        Ray ray38 = new Ray(p32, v001);
        assertNull(tube.findGeoIntersections(ray38), "TC38: didn't return null");
        //TC39: start after the tube(0 points)
        Ray ray39 = new Ray(new Point(-1, 0, 2), v1M11);
        assertNull(tube.findGeoIntersections(ray39), "TC39: didn't return null");

        // **** Group: Ray's line is tangent to the Tube (all tests 0 points)

        //TC40: starts before the tube
        Ray ray40 = new Ray(new Point(1, -1, -1), v001);
        assertNull(tube.findGeoIntersections(ray40), "TC40: doesn't return null");
        //TC41: starts on the tube
        Ray ray41 = new Ray(new Point(1, -1, 0), v001);
        assertNull(tube.findGeoIntersections(ray41), "TC41: doesn't return null");
        //TC42: starts after the tube
        Ray ray42 = new Ray(new Point(1, -1, 1), v001);
        assertNull(tube.findGeoIntersections(ray42), "T42: doesn't return null");

        // **** special cases

        //TC43: Ray's line is outside, ray is orthogonal to ray start to axis
        Ray ray43 = new Ray(new Point(1, 2, 0), v001);
        assertNull(tube.findGeoIntersections(ray43), "TC43: doesn't return null");
        //TC44: Ray's line is inside, ray is orthogonal to ray start to axis
        Ray ray44 = new Ray(new Point(1, 0.5, 0), v001);
        List<GeoPoint> result44 = tube.findGeoIntersections(ray44);
        exp = List.of(new GeoPoint(tube, p02));
        assertEquals(1, result44.size(), "TC44: wrong number of results");
        assertEquals(exp, result44, "TC45: wrong points");
    }
}