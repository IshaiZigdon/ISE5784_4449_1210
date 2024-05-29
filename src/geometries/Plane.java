package geometries;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

/**
 * this class represent an infinite plane field with a point and a vector
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class Plane implements Geometry {
    /**
     * point on the plane
     */
    private final Point q;
    /**
     * normal of the plane
     */
    private final Vector normal;

    /**
     * a constructor to calculate the plane with 3 points, calculates the normal
     * based on the calculations of normal to a triangle
     *
     * @param a first point
     * @param b second point
     * @param c third point
     */
    public Plane(Point a, Point b, Point c) {
        q = a;
        Vector v1 = b.subtract(a);
        Vector v2 = c.subtract(a);
        normal = v1.crossProduct(v2).normalize();
    }

    /**
     * constructor to initialize Plane with a point and a vector
     *
     * @param q      the point
     * @param normal the vector
     */
    @SuppressWarnings("unused")
    public Plane(Point q, Vector normal) {
        this.q = q;
        this.normal = normal.normalize();
    }

    @Override
    public Vector getNormal(Point p) {
        return normal;
    }

    /**
     * a function to return the normal of the plane
     *
     * @return the normal vector of the plane
     */
    public Vector getNormal() {
        return normal;
    }

    @Override
    public List<Point> findIntersections(Ray ray) {

        return null;
    }
}
