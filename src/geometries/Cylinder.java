package geometries;

import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.LinkedList;
import java.util.List;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Cylinder class models a 3D shape with circular bases and a curved surface in
 * Cartesian coordinates inherits from Tube.
 *
 * @author Ishai
 * @author Zaki
 */
@SuppressWarnings("unused")
public class Cylinder extends Tube {
    /**
     * the height of the cylinder
     */
    final private double height;

    /**
     * constructor to initialize Cylinder based object with its ray, radius and height
     *
     * @param r      the ray that represent the axis
     * @param radius radius of the base of the cylinder
     * @param height height of the cylinder
     */
    @SuppressWarnings("unused")
    public Cylinder(Ray r, double radius, double height) {
        super(r, radius);
        this.height = height;
    }

    @Override
    public void setMinMax() {
        double x = axis.getHead().getX();
        double y = axis.getHead().getY();
        double z = axis.getHead().getZ();

        Point secondBase = axis.getPoint(height);
        double x1 = secondBase.getX();
        double y1 = secondBase.getY();
        double z1 = secondBase.getZ();

        int[] xMinMax = minMax(x, x1);
        int[] yMinMax = minMax(y, y1);
        int[] zMinMax = minMax(z, z1);

        min = new Point(xMinMax[0], yMinMax[0], zMinMax[0]);
        max = new Point(xMinMax[1], yMinMax[1], zMinMax[1]);
    }

    /**
     * function for the creation of a minimum and maximum values
     * between x and y
     * minimum will be in index 0
     * maximum will be in index 1
     *
     * @param x first value
     * @param y second value
     * @return an array with minimum and maximum values
     */
    private int[] minMax(double x, double y) {
        return x < y ? new int[]{(int) Math.floor(x - radius), (int) Math.ceil(y + radius)} :
                new int[]{(int) Math.floor(y - radius), (int) Math.ceil(x + radius)};
    }

    @Override
    public Vector getNormal(Point p) {
        //if p is the same as p0, meaning p is in the middle of the base
        if (p.equals(axis.getHead()))
            return axis.getDirection();
        //the distance between p0 and o
        double t = axis.getDirection().dotProduct(p.subtract(axis.getHead()));
        if (isZero(t) || isZero(t - height))
            //meaning the distance between p0 and o is 0(p is on the base on which p0 is)
            //or height(p is on the other base), that also includes the points on the edges of the bases.
            //so the normal of all those points is the direction vector itself
            return axis.getDirection();
        //p is on the side surface
        return super.getNormal(p);
    }

    @Override
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray, double maxDistance) {
        var intersections = super.findGeoIntersectionsHelper(ray, maxDistance);
        List<GeoPoint> result = null;

        Vector aD = axis.getDirection();
        Point axisStart = axis.getHead();
        Point axisEnd = axisStart.add(aD.scale(height));
        Vector v = ray.getDirection();

        if (intersections != null) {
            //removing the points out of bounds
            for (GeoPoint gp : intersections) {

                Point p = gp.point;

                Vector vectorToAxisStart = axisStart.subtract(p);
                Vector vectorToAxisEnd = axisEnd.subtract(p);

                //if the intersection point is on the edge of the base
                if (isZero(vectorToAxisStart.length() - radius)
                        || isZero(vectorToAxisEnd.length() - radius))
                    continue;

                double d1 = alignZero(vectorToAxisStart.lengthSquared() - radiusSquared);
                double d2 = alignZero(vectorToAxisEnd.lengthSquared() - radiusSquared);
                double heightSquared = height * height;
                if (d1 < heightSquared && d2 < heightSquared) {
                    if (result == null)
                        result = new LinkedList<>();
                    result.add(gp);
                }
            }

            if (result != null && result.size() == 2) return result;
        }

        // Check for intersections with the cylinder caps
        Plane bottomCap = new Plane(axisStart, aD);
        Point p1 = basesIntersection(bottomCap, ray, maxDistance, axisStart);
        Plane topCap = new Plane(axisEnd, aD);
        Point p2 = basesIntersection(topCap, ray, maxDistance, axisEnd);

        if (p1 == null && p2 == null) return result;

        if (result == null) {
            result = new LinkedList<>();
            if (p1 != null) result.add(new GeoPoint(this, p1));
            if (p2 != null) result.add(new GeoPoint(this, p2));
            return result;
        }
        GeoPoint gp = result.getFirst();
        if (gp.equals(intersections.getFirst()) && intersections.size() > 1)
            return List.of(gp, new GeoPoint(this, p1 == null ? p2 : p1));
        return List.of(new GeoPoint(this, p1 == null ? p2 : p1), gp);
    }

    /**
     * a method to check intersect with the bases
     *
     * @param base        the base we are checking
     * @param ray         the rays that should intersect
     * @param maxDistance the max distance of the intersection points
     * @param axisBase    the middle of the current base
     * @return the intersection
     */
    private Point basesIntersection(Plane base, Ray ray, double maxDistance, Point axisBase) {
        var baseIntersections = base.findGeoIntersections(ray, maxDistance);

        if (baseIntersections != null) {
            Point baseIntersection = baseIntersections.getFirst().point;
            if (baseIntersection.distance(axisBase) < radius) {
                return baseIntersection;
            }
        }
        return null;
    }
}
