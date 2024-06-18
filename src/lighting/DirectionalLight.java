package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class DirectionalLight extends Light implements LightSource {
    private Vector direction;

    /**
     * ctor with given intensity and direction
     *
     * @param intensity the intensity
     * @param direction the direction
     */
    protected DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        this.direction = direction.normalize();
    }

    /**
     * ctor with given intensity
     *
     * @param intensity the intensity
     */
    protected DirectionalLight(Color intensity) {
        super(intensity);
    }

    /**
     * @param p
     * @return
     */
    @Override
    public Color getIntensity(Point p) {
        return null;
    }

    /**
     * @param p
     * @return
     */
    @Override
    public Vector getL(Point p) {
        return direction;
    }
}
