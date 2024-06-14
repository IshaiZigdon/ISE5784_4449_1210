package geometries;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * this interface will serve all geometry shapes in the program
 * 2D and 3D
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public abstract class Geometry extends Intersectable {
    /**
     * emission light
     */
    protected Color emission = Color.BLACK;

    /**
     * set function for emission
     * @param emission the given emission
     * @return the updated Geometry
     */
    public Geometry setEmission(Color emission) {
        this.emission = emission;
        return this;
    }

    /**
     * get function for emission
     * @return emission
     */
    public Color getEmission() {
        return emission;
    }

    /**
     * this function calculates the
     * vertical (normal) vector for the various shapes at a point
     * on the surface of the shape
     *
     * @param p a point on the shape
     * @return a vertical vector for the shape
     */
    public abstract Vector getNormal(Point p);

}
