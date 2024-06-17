package geometries;

import primitives.Point;
import primitives.Ray;

import java.util.List;

/**
 * class for intersecting points of all the shapes
 * with given ray
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public abstract class Intersectable {

    /**
     * calculates where are the intersecting GeoPoints of the
     * shapes with the given ray
     *
     * @param ray the ray
     * @return list of intersecting GeoPoints
     */
    protected List<GeoPoint> findGeoIntersectionsHelper(Ray ray) {
        return null;
    }

    /**
     * calculates where are the intersecting GeoPoints of the
     * shapes with the given ray
     *
     * @param ray the ray
     * @return list of intersecting GeoPoints
     */
    public List<GeoPoint> findGeoIntersections(Ray ray) {
        return findGeoIntersectionsHelper(ray);
    }

    /**
     * calculates where are the intersecting points of the
     * shape with the given ray
     *
     * @param ray the ray
     * @return list of intersecting points
     */
    public List<Point> findIntersections(Ray ray) {
        var geoList = findGeoIntersections(ray);
        return geoList == null ? null : geoList.stream().map(gp -> gp.point).toList();
    }

    /**
     * class for geoPoint, withe shape and point on the shape
     */
    public static class GeoPoint {
        /**
         * the shape
         */
        public Geometry geometry;
        /**
         * the point on the shape
         */
        public Point point;

        /**
         * ctor
         *
         * @param geometry the given geometry
         * @param point    the given point
         */
        public GeoPoint(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof GeoPoint && geometry.equals(((GeoPoint) obj).geometry)
                    && point.equals(((GeoPoint) obj).point);
        }

        @Override
        public String toString() {
            return geometry.toString() + " " + point.toString();
        }
    }
}
