package renderer;

import org.junit.jupiter.api.Test;
import primitives.*;
import geometries.*;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
/**
 *
 */
public class integrationTest {
    /** Camera builder for the tests */
    private final Camera.Builder cameraBuilder = Camera.getBuilder()
            //.setRayTracer(new SimpleRayTracer(new Scene("Test")))
            // .setImageWriter(new ImageWriter("Test", 1, 1))
            .setLocation(Point.ZERO)
            .setDirection( new Vector(0, -1, 0),new Vector(0, 0, -1))
            .setVpSize(3,3)
            .setVpDistance(1);
    /**
     * Test method for
     *
     */
    @Test
    public void testSphere()
    {
        //TC1:
        Camera sphereCamera1 = cameraBuilder.build();
        Sphere sphere1 = new Sphere(new Point(0,0,-3),1);
        int count = 0;
        List<Point> result;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = sphere1.findIntersections(sphereCamera1.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(2, count,"sphere tc1: wrong number");

        //TC2:
        Camera sphereCamera2 = cameraBuilder.setLocation(new Point(0,0,0.5)).build();
        Sphere sphere2 = new Sphere(new Point(0,0,-2.5),2.5);
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = sphere2.findIntersections(sphereCamera2.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(18, count,"sphere tc2: wrong number");

        //TC3:
        Sphere sphere3 = new Sphere(new Point(0,0,-2),2);
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = sphere3.findIntersections(sphereCamera2.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(10, count,"sphere tc3: wrong number");

        //TC4:
        Sphere sphere4 = new Sphere(new Point(0,0,-2),4);
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = sphere4.findIntersections(sphereCamera2.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(9, count,"sphere tc4: wrong number");

        //TC5:
        Sphere sphere5 = new Sphere(new Point(0,0,1),0.5);
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = sphere5.findIntersections(sphereCamera2.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(0, count,"sphere tc5: wrong number");
    }

    @Test
    public void planeTest() {
        Camera planeCamera = cameraBuilder.build();

        //TC1:
        Plane plane1 = new Plane(new Point(1,0,-10),new Point(0,1,-10),new Point(1,1,-10));
        int count = 0;
        List<Point> result;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = plane1.findIntersections(planeCamera.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(9, count,"plane tc1: wrong number");
        //TC2:
        Plane plane2 = new Plane(new Point(0,0,-10),new Point(2,5,-8),new Point(-2,2,-10.6));
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = plane1.findIntersections(planeCamera.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(9, count,"plane tc2: wrong number");

        //TC3:todo check
        Plane plane3 = new Plane(new Point(0,0,-8),new Point(1,2,-7),new Point(-2,5,-10));
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = plane3.findIntersections(planeCamera.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(6, count,"plane tc3: wrong number");
    }

    @Test
    public void TriangleTest() {
        Camera triangleCamera = cameraBuilder.build();

        //TC1:
        Triangle triangle1 = new Triangle(new Point(0,1,-2),new Point(1,-1,-2),new Point(-1,-1,-2));
        int count = 0;
        List<Point> result;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = triangle1.findIntersections(triangleCamera.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(1, count,"plane tc1: wrong number");

        //TC1:
        Triangle triangle2 = new Triangle(new Point(0,20,-2),new Point(1,-1,-2),new Point(-1,-1,-2));
        count = 0;
        for(int i = 0 ;i < 3;i++)
        {
            for(int j = 0 ;j < 3;j++){
                result = triangle2.findIntersections(triangleCamera.constructRay(3,3,i,j));
                if (result != null){
                    count += result.size();
                }
            }
        }
        assertEquals(2, count,"plane tc1: wrong number");

    }
}
