package renderer;

import geometries.*;
import lighting.AmbientLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

import static java.awt.Color.*;

/**
 * Testing basic shadows
 *
 * @author Dan
 */
public class ShadowTests {
    /**
     * Scene of the tests
     */
    private final Scene scene = new Scene("Test scene");

    /**
     * Camera builder of the tests
     */
    private final Camera.Builder camera = Camera.getBuilder()
            .setDirection(Point.ZERO, Vector.Y)
            .setLocation(new Point(0, 0, 1000)).setVpDistance(1000)
            .setMultithreading(-1)
            .setDebugPrint(0.1)
            .setVpSize(200, 200);

    /**
     * The sphere in the tests
     */
    private final Intersectable sphere = new Sphere(new Point(0, 0, -200), 60d)
            .setEmission(new Color(BLUE))
            .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30));
    /**
     * The material of the triangles in the tests
     */
    private final Material trMaterial = new Material().setKd(0.5).setKs(0.5).setShininess(30);

    /**
     * Helper function for the tests in this module
     *
     * @param pictName     the name of the picture generated by a test
     * @param triangle     the triangle in the test
     * @param spotLocation the spotlight location in the test
     */
    private void sphereTriangleHelper(String pictName, Triangle triangle, Point spotLocation) {
        scene.geometries.add(sphere, triangle.setEmission(new Color(BLUE)).setMaterial(trMaterial));
        scene.lights.add( //
                new SpotLight(new Color(400, 240, 0), spotLocation, new Vector(1, 1, -3)) //
                        .setKl(1E-5).setKq(1.5E-7));
        camera.setImageWriter(new ImageWriter(pictName, 1024, 1024))
                .setRayTracer(new RegularGrid(scene, new BlackBoard()))
                .build()
                .renderImage() //
                .writeToImage();
    }

    /**
     * Produce a picture of a sphere and triangle with point light and shade
     */
    @Test
    public void sphereTriangleInitial() {
        sphereTriangleHelper("shadowSphereTriangleInitial", //
                new Triangle(new Point(-70, -40, 0), new Point(-40, -70, 0), new Point(-68, -68, -4)), //
                new Point(-100, -100, 200));
    }

    /**
     * Sphere-Triangle shading - move triangle up-right
     */
    @Test
    public void sphereTriangleMove1() {
        sphereTriangleHelper("shadowSphereTriangleMove1", //
                new Triangle(new Point(-60, -30, 0), new Point(-30, -60, 0), new Point(-58, -58, -4)), //
                new Point(-100, -100, 200));
    }

    /**
     * Sphere-Triangle shading - move triangle upper-righter
     */
    @Test
    public void sphereTriangleMove2() {
        sphereTriangleHelper("shadowSphereTriangleMove2", //
                new Triangle(new Point(-50, -20, 0), new Point(-20, -50, 0), new Point(-48, -48, -4)), //
                new Point(-79.79, -79.79, 100));
    }

    /**
     * Sphere-Triangle shading - move spot closer
     */
    @Test
    public void sphereTriangleSpot1() {
        sphereTriangleHelper("shadowSphereTriangleSpot1", //
                new Triangle(new Point(-70, -40, 0), new Point(-40, -70, 0), new Point(-68, -68, -4)), //
                new Point(-88.08, -88.08, 120));
    }

    /**
     * Sphere-Triangle shading - move spot even more close
     */
    @Test
    public void sphereTriangleSpot2() {
        sphereTriangleHelper("shadowSphereTriangleSpot2", //
                new Triangle(new Point(-70, -40, 0), new Point(-40, -70, 0), new Point(-68, -68, -4)), //
                new Point(-79.57, -79.57, 50));
    }

    /**
     * Produce a picture of  two triangles lighted by a spotlight with a Sphere
     * producing a shading
     */
    @Test
    public void trianglesSphere() {
        scene.geometries.add(
                new Triangle(new Point(-150, -150, -115), new Point(150, -150, -135),
                        new Point(75, 75, -150)) //
                        .setMaterial(new Material().setKs(0.8).setShininess(60)), //
                new Triangle(new Point(-150, -150, -115), new Point(-70, 70, -140), new Point(75, 75, -150)) //
                        .setMaterial(new Material().setKs(0.8).setShininess(60)), //
                new Sphere(new Point(0, 0, -11), 30d) //
                        .setEmission(new Color(BLUE)) //
                        .setMaterial(new Material().setKd(0.5).setKs(0.5).setShininess(30)) //
        );
        scene.setAmbientLight(new AmbientLight(new Color(WHITE), 0.15));
        scene.lights.add(
                new SpotLight(new Color(700, 400, 400), new Point(40, 40, 115), new Vector(-1, -1, -4)) //
                        .setKl(4E-4).setKq(2E-5));

        camera.setImageWriter(new ImageWriter("shadowTrianglesSphere", 600, 600))
                .setRayTracer(new RegularGrid(scene, new BlackBoard()))
                .build()
                .renderImage()
                .writeToImage();
    }

    /**
     * test method for soft shadows
     */
    @Test
    public void softShadowsTest() {
        // Adding a plane and other geometries to the scene
        scene.geometries.add(
                // Large reflective floor
                new Polygon(new Point(-1700, -40, 1700), new Point(1700, -40, 1700),
                        new Point(1700, -40, -1700), new Point(-1700, -40, -1700))
                        .setEmission(new Color(GRAY))
                        .setMaterial(new Material().setKd(0.6).setKs(0.3)),

                // Cylinder
                new Cylinder(new Ray(new Point(-95, -60, 140), Vector.Y), 10, 100)
                        .setEmission(new Color(GRAY))
                        .setMaterial(new Material().setKd(0.6).setKs(0.3)),

                // Cylinder
                new Cylinder(new Ray(new Point(5, -60, 130), Vector.Y), 10, 100)
                        .setEmission(new Color(GRAY))
                        .setMaterial(new Material().setKd(0.6).setKs(0.3)),

                // Cylinder
                new Cylinder(new Ray(new Point(-105, 30, 130), new Vector(1, 0, -0.1)), 10, 120)
                        .setEmission(new Color(GRAY))
                        .setMaterial(new Material().setKd(0.6).setKs(0.3)),

                // Sphere 2
                new Sphere(new Point(-45, -20, 140), 30)
                        .setEmission(new Color(GRAY))
                        .setMaterial(new Material().setKd(0.6).setKs(0.3))
        );

        // Setting ambient light
        scene.setAmbientLight(new AmbientLight(new Color(WHITE), 0.15));
        scene.setBackground(new Color(GRAY));

        // Adding a spotlight to the scene, positioned to cast shadows effectively
        scene.lights.add(
                new PointLight(new Color(1200, 900, 500), new Point(-140, -40, 0))//, new Vector(1, -1, 0))
                        .setKl(4E-4).setKq(2E-5).setRadius(15)
        );

        // Setting the camera location to capture the scene with shadows
        camera.setLocation(new Point(100, 100, 1700))
                .setVpDistance(2200)
                .setVpSize(500, 500)
                .setDirection(Point.ZERO, new Vector(0, 1, -1 / 17d))
                .setImageWriter(new ImageWriter("softShadow", 1024, 1024))
                .setRayTracer(new RegularGrid(scene, new BlackBoard()))
                .build()
                .renderImage()
                .writeToImage();
    }
}

