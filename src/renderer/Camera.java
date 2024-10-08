package renderer;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.stream.IntStream;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * this class represent the camera
 * and view plane
 *
 * @author Ishai zigdon
 * @author Zaki zafrani
 */
public class Camera implements Cloneable {
    /**
     * position of the camera
     */
    private Point p0;
    /**
     * vector representing the vector to the view plane from the camera position
     */
    private Vector vTo;
    /**
     * vector representing the up vector from the camera position
     */
    private Vector vUp;
    /**
     * vector representing the right vector from the camera position
     */
    private Vector vRight;
    /**
     * the width of the view plane
     */
    private double viewPlaneWidth = 0.0;
    /**
     * the height of the view plane
     */
    private double viewPlaneHeight = 0.0;
    /**
     * the distance of the view plane
     */
    private double viewPlaneDistance = 0.0;
    /**
     * the middle of the view plane
     */
    private Point viewPlaneMiddle;
    /**
     * the image writer with the resolution
     */
    private ImageWriter imageWriter;
    /**
     * the ray tracer with the scene
     */
    private RayTracerBase rayTracer;

    /**
     * thread counter
     */
    private int threadsCount = 0; // -2 auto, -1 range/stream, 0 no threads, 1+ number of threads
    /**
     * boundary to the threads
     */
    private final int SPARE_THREADS = 2; // Spare threads if trying to use all the cores
    /**
     * the percent of the project in the run
     */
    private double printInterval = 0;

    /**
     * making the default constructor private
     */
    private Camera() {
    }

    /**
     * creating new camera builder
     *
     * @return new camera builder
     */
    public static Builder getBuilder() {
        return new Builder(new Camera());
    }

    /**
     * get function for p0
     *
     * @return p0
     */
    Point getP0() {
        return p0;
    }

    /**
     * get function for vTo
     *
     * @return vTo
     */
    Vector getVTo() {
        return vTo;
    }

    /**
     * get function for vUp
     *
     * @return vUp
     */
    Vector getVUp() {
        return vUp;
    }

    /**
     * get function for vRight
     *
     * @return vRight
     */
    Vector getVRight() {
        return vRight;
    }

    /**
     * get function for viewPlaneHeight
     *
     * @return viewPlaneHeight
     */
    double getViewPlaneHeight() {
        return viewPlaneHeight;
    }

    /**
     * get function for viewPlaneWidth
     *
     * @return viewPlaneWidth
     */
    double getViewPlaneWidth() {
        return viewPlaneWidth;
    }

    /**
     * get function for viewPlaneDistance
     *
     * @return viewPlaneDistance
     */
    double getViewPlaneDistance() {
        return viewPlaneDistance;
    }

    /**
     * constructing a ray through given pixel
     *
     * @param nX width of pixel
     * @param nY height of pixel
     * @param j  column
     * @param i  line
     * @return the ray
     */
    public Ray constructRay(int nX, int nY, int j, int i) {
        double rY = viewPlaneHeight / nY;
        double rX = viewPlaneWidth / nX;

        double yI = -(i - (nY - 1) / 2.0) * rY;
        double xJ = (j - (nX - 1) / 2.0) * rX;

        Point pIJ = viewPlaneMiddle;
        if (!isZero(xJ))
            pIJ = pIJ.add(vRight.scale(xJ));
        if (!isZero(yI))
            pIJ = pIJ.add(vUp.scale(yI));

        return new Ray(p0, pIJ.subtract(p0));
    }

    /**
     * render the image and color each pixel
     *
     * @return the updated camera
     */
    public Camera renderImage() {
        final int nY = imageWriter.getNy();
        final int nX = imageWriter.getNx();
        Pixel.initialize(nY, nX, printInterval);
        if (threadsCount == 0) {
            for (int i = 0; i < nY; i++)
                for (int j = 0; j < nX; j++)
                    castRay(nX, nY, j, i);
        } else if (threadsCount == -1) {
            IntStream.range(0, nY).parallel() //
                    .forEach(i -> IntStream.range(0, nX).parallel() //
                            .forEach(j -> castRay(nX, nY, j, i)));
        } else {
            int count = threadsCount;
            var threads = new LinkedList<Thread>();
            while (count-- > 0) {
                threads.add(new Thread(() -> {
                    Pixel pixel;
                    while ((pixel = Pixel.nextPixel()) != null) {
                        castRay(nX, nY, pixel.col(), pixel.row());
                    }
                }));
            }
            for (var thread : threads) thread.start();
            try {
                for (var thread : threads) thread.join();
            } catch (InterruptedException ignore) {
            }
        }
        return this;
    }

    /**
     * print a grid with given width and height with given color
     *
     * @param interval the width and height
     * @param color    the color
     * @return the updated camera
     */
    public Camera printGrid(int interval, Color color) {
        int nY = imageWriter.getNy();
        int nX = imageWriter.getNx();
        for (int i = 0; i < nY; i++)
            for (int j = 0; j < nX; j++)
                if (i % interval == 0 || j % interval == 0)
                    imageWriter.writePixel(j, i, color);
        return this;
    }

    /**
     * rotate the camera given degrees to the right
     *
     * @param angleDegrees the given degrees
     * @return the updated camera
     */
    public Camera rotate(double angleDegrees) {
        double angleRadians = Math.toRadians(angleDegrees);

        double cosTheta = alignZero(Math.cos(angleRadians));
        double sinTheta = alignZero(Math.sin(angleRadians));

        Vector newRight, newUp;
        if (cosTheta == 0) {
            // Handle the case where cosTheta is zero
            newRight = vUp.scale(sinTheta);
            newUp = vRight.scale(-sinTheta);
        } else if (sinTheta == 0) {
            // Handle the case where sinTheta is zero
            newRight = vRight.scale(cosTheta);
            newUp = vUp.scale(cosTheta);
        } else {
            // General case
            newRight = vRight.scale(cosTheta).add(vUp.scale(sinTheta));
            newUp = vRight.scale(-sinTheta).add(vUp.scale(cosTheta));
        }

        vRight = newRight;
        vUp = newUp;
        return this;
    }

    /**
     * cast a ray through a given pixel and colors it
     *
     * @param nX the width of the pixel
     * @param nY the height of the pixel
     * @param j  the x parameter
     * @param i  the y parameter
     */
    private void castRay(int nX, int nY, int j, int i) {
        Ray ray = constructRay(nX, nY, j, i);
        Color color = rayTracer.traceRay(ray);
        imageWriter.writePixel(j, i, color);
        Pixel.pixelDone();
    }

    /**
     * write to image for camera
     */
    public void writeToImage() {
        imageWriter.writeToImage();
    }

    /**
     * class for builder
     */
    public static class Builder {
        /**
         * camera
         */
        final private Camera camera;

        /**
         * constructor that initialize camera with given Camera object
         *
         * @param c given camera
         */
        public Builder(Camera c) {
            camera = c;
        }

        /**
         * function for set location
         *
         * @param p the location point
         * @return builder with given location
         */
        public Builder setLocation(Point p) {
            camera.p0 = p;
            return this;
        }

        /**
         * function for set direction
         *
         * @param vUp vertical vector
         * @param vTo vector to distance
         * @return builder with given direction
         */
        public Builder setDirection(Vector vTo, Vector vUp) {
            if (!isZero(vTo.dotProduct(vUp)))
                throw new IllegalArgumentException("camera vectors must be vertical to each other");
            camera.vRight = vTo.crossProduct(vUp).normalize();
            camera.vTo = vTo.normalize();
            camera.vUp = vUp.normalize();
            return this;
        }

        /**
         * set direction to the given point with the given up vector
         *
         * @param p   the point to direct the camera to
         * @param vUp the given up vector
         * @return builder with given directions of all the vectors
         */
        public Builder setDirection(Point p, Vector vUp) {
            camera.vUp = vUp.normalize();
            Point p0 = camera.getP0();
            if (p0 == null || p0.equals(p))
                camera.vTo = Vector.MINUS_Z;
            else {
                camera.vTo = p.subtract(p0).normalize();
                if (camera.vTo.equals(Vector.Y) || camera.vTo.equals(Vector.MINUS_Y))
                    camera.vUp = Vector.Z;
            }

            camera.vRight = camera.vTo.crossProduct(camera.vUp).normalize();
            return this;
        }

        /**
         * function for set size of plane
         *
         * @param width  the width
         * @param height the height
         * @return builder with plane with the given size
         */
        public Builder setVpSize(double width, double height) {
            if (alignZero(width * height) <= 0 || alignZero(width) <= 0)
                throw new IllegalArgumentException("view plane width and height values must be greater than 0");
            camera.viewPlaneWidth = width;
            camera.viewPlaneHeight = height;
            return this;
        }

        /**
         * function for set distance
         *
         * @param distance the distance
         * @return builder with given distance
         */
        public Builder setVpDistance(double distance) {
            if (distance <= 0)
                throw new IllegalArgumentException("view plane distance value must be greater than 0");
            camera.viewPlaneDistance = distance;
            return this;
        }

        /**
         * function for set imageWriter
         *
         * @param imageWriter the imageWriter
         * @return builder with given imageWriter
         */
        public Builder setImageWriter(ImageWriter imageWriter) {
            camera.imageWriter = imageWriter;
            return this;
        }

        /**
         * function for set rayTracer
         *
         * @param rayTracer the rayTracer
         * @return builder with given rayTracer
         */
        public Builder setRayTracer(RayTracerBase rayTracer) {
            camera.rayTracer = rayTracer;
            return this;
        }

        /**
         * function to set multithreading
         *
         * @param threads the choosing number of the user
         * @return the builder with the given position of the thread usage
         */
        public Builder setMultithreading(int threads) {
            if (threads < -2) throw new IllegalArgumentException("Multithreading must be -2 or higher");
            if (threads >= -1) camera.threadsCount = threads;
            else { // == -2
                int cores = Runtime.getRuntime().availableProcessors() - camera.SPARE_THREADS;
                camera.threadsCount = cores <= 2 ? 1 : cores;
            }
            return this;
        }

        /**
         * function to print the percent of the project while it is running
         *
         * @param interval the percent
         * @return the builder with the new percent
         */
        public Builder setDebugPrint(double interval) {
            camera.printInterval = interval;
            return this;
        }

        /**
         * function to check and build camera with valid values
         *
         * @return the camera
         */
        public Camera build() {
            final String message = "Missing render resource. ";
            String fields = "";

            if (alignZero(camera.viewPlaneWidth) <= 0)
                fields += "viewPlaneWidth ";
            if (alignZero(camera.viewPlaneHeight) <= 0)
                fields += "viewPlaneHeight ";
            if (alignZero(camera.viewPlaneDistance) <= 0)
                fields += "viewPlaneDistance ";

            if (camera.vTo == null)
                fields += "vTo ";
            if (camera.vUp == null)
                fields += "vUp ";

            if (camera.imageWriter == null)
                fields += "imageWriter ";
            if (camera.rayTracer == null)
                fields += "rayTracer ";

            if (!fields.isEmpty())
                throw new MissingResourceException(message + fields, camera.getClass().getName(), "");

            if (!isZero(camera.vTo.dotProduct(camera.vUp)))
                throw new IllegalArgumentException("camera vectors must be vertical to each other");
            if (camera.vRight == null)
                camera.vRight = camera.vTo.crossProduct(camera.vUp).normalize();

            camera.viewPlaneMiddle = camera.p0.add(camera.vTo.scale(camera.viewPlaneDistance));

            try {
                return (Camera) camera.clone();
            } catch (CloneNotSupportedException e) {
                throw new AssertionError(e); // never reached code
            }
        }
    }
}
