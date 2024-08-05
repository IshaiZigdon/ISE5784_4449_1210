package renderer;

import geometries.Geometries;
import geometries.Intersectable;
import geometries.Intersectable.GeoPoint;
import geometries.Polygon;
import lighting.LightSource;
import primitives.*;
import scene.Scene;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static primitives.Util.isZero;

public class RegularGrid extends SimpleRayTracer {

    /**
     * Delta value for accuracy
     */
    private static final double DELTA = 0.1;

    class Voxel {
        private Geometries geometries;
        private double[] max = new double[3];
        private double[] min = new double[3];

        private boolean inside(Point value) {
            return value.getX() <= max[0] + DELTA && value.getX() >= min[0] - DELTA
                    && value.getY() <= max[1] + DELTA && value.getY() >= min[1] - DELTA
                    && value.getZ() <= max[2] + DELTA && value.getZ() >= min[2] - DELTA;
        }

        private boolean between(Point maxValue, Point minValue) {
            return overlap(maxValue.getX(), minValue.getX(), max[0], min[0]) &&
                    overlap(maxValue.getY(), minValue.getY(), max[1], min[1]) &&
                    overlap(maxValue.getZ(), minValue.getZ(), max[2], min[2]);
        }

        private boolean overlap(double maxX, double minX, double max, double min) {
            return (maxX > max && minX < min) ||
                    (maxX <= max && maxX >= min) ||
                    (minX <= max && minX >= min);
        }
    }

    private Voxel[][][] cells;

    private int[] gridMax;
    private int[] gridMin;

    private int nX;
    private int nY;
    private int nZ;

    private double[] cellSize = new double[3];

    private Geometries gridLimits;

    public RegularGrid(Scene s) {
        super(s);
        initiateGrid();
    }

    public RegularGrid(Scene s, BlackBoard blackBoard) {
        super(s,blackBoard);
        initiateGrid();
    }

    private void initiateGrid() {
        scene.geometries.setMinMax();

        gridMax = new int[]{
                (int) scene.geometries.max.getX(),
                (int) scene.geometries.max.getY(),
                (int) scene.geometries.max.getZ()
        };

        gridMin = new int[]{
                (int) scene.geometries.min.getX(),
                (int) scene.geometries.min.getY(),
                (int) scene.geometries.min.getZ()
        };

        double lambda = 4; // example value, replace with your actual value
        int n = scene.geometries.getSize();

        //grid size
        int dx = gridMax[0] - gridMin[0];
        int dy = gridMax[1] - gridMin[1];
        int dz = gridMax[2] - gridMin[2];

        int v = dx * dy * dz;
        double formula = Math.cbrt((lambda * n) / v);

        //Calculate initial grid resolution
        int nXt = (int) Math.round(dx * formula);
        int nYt = (int) Math.round(dy * formula);
        int nZt = (int) Math.round(dz * formula);

        // Adjust nX, nY, nZ to closest values
        nX = closestDivisor(dx, nXt);
        nY = closestDivisor(dy, nYt);
        nZ = closestDivisor(dz, nZt);

        cellSize[0] = dx / nX;
        cellSize[1] = dy / nY;
        cellSize[2] = dz / nZ;

        //initialize all the voxels max min
        cells = new Voxel[nX][nY][nZ];
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                for (int k = 0; k < nZ; k++) {
                    cells[i][j][k] = new Voxel();
                    cells[i][j][k].min[0] = gridMin[0] + i * cellSize[0];
                    cells[i][j][k].min[1] = gridMin[1] + j * cellSize[1];
                    cells[i][j][k].min[2] = gridMin[2] + k * cellSize[2];

                    cells[i][j][k].max[0] = cells[i][j][k].min[0] + cellSize[0];
                    cells[i][j][k].max[1] = cells[i][j][k].min[1] + cellSize[1];
                    cells[i][j][k].max[2] = cells[i][j][k].min[2] + cellSize[2];
                }
            }
        }

        //insert geometries to voxels
        for (Intersectable i : scene.geometries.getIntersectables()) {
            insert(i);
        }

        buildBox();
    }

    @Override
    public Color traceRay(Ray ray) {
        GeoPoint closestPoint = traversGrid(ray);
        return closestPoint == null ? scene.background
                : calcColor(closestPoint, ray);
    }

    private GeoPoint traversGrid(Ray ray) {
        //if rey intersect the grid
        var intersection = gridLimits.findIntersections(ray);
        if (intersection == null) return null;

        //the first voxel that the ray intersects
        Point p = fixPoint(ray.getHead());
        int[] cellIndex = findVoxel(p);
        if (cellIndex == null) {
            if (intersection.size() == 2) {
                p = intersection.getFirst().distance(p) < intersection.get(1).distance(p) ?
                        intersection.getFirst() : intersection.get(1);
            } else
                p = intersection.getFirst();
            p = fixPoint(p);
            cellIndex = findVoxel(p);
        }
        Vector v = ray.getDirection();

        //we want to separate the coordinates
        double rX = v.getX();
        double rY = v.getY();
        double rZ = v.getZ();
        double oX = p.getX();
        double oY = p.getY();
        double oZ = p.getZ();

        double[] rayOrigGrid = {
                oX - gridMin[0],
                oY - gridMin[1],
                oZ - gridMin[2],
        };
        double[] deltaT = {
                abs(cellSize[0] / rX),
                abs(cellSize[1] / rY),
                abs(cellSize[2] / rZ),
        };

        double t_x = rX < 0 ? (floor(rayOrigGrid[0] / cellSize[0]) * cellSize[0] - rayOrigGrid[0]) / rX
                : ((floor(rayOrigGrid[0] / cellSize[0]) + 1) * cellSize[0] - rayOrigGrid[0]) / rX;
        double t_y = rY < 0 ? (floor(rayOrigGrid[1] / cellSize[1]) * cellSize[1] - rayOrigGrid[1]) / rY
                : ((floor(rayOrigGrid[1] / cellSize[1]) + 1) * cellSize[1] - rayOrigGrid[1]) / rY;
        double t_z = rZ < 0 ? (floor(rayOrigGrid[2] / cellSize[2]) * cellSize[2] - rayOrigGrid[2]) / rZ
                : ((floor(rayOrigGrid[2] / cellSize[2]) + 1) * cellSize[2] - rayOrigGrid[2]) / rZ;

        GeoPoint firstIntersection = null;
        if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].geometries != null) {
            GeoPoint closestPoint =
                    findClosestIntersection(ray, cells[cellIndex[0]][cellIndex[1]][cellIndex[2]]);

            if (closestPoint != null) {
                if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].inside(closestPoint.point))
                    return closestPoint;
                firstIntersection = closestPoint;
            }
        }

        while (true) {

            if (t_x <= t_y && t_x <= t_z) {
                t_x += deltaT[0];
                if (rX < 0)
                    cellIndex[0]--;
                else
                    cellIndex[0]++;
            } else if (t_y <= t_z) {
                t_y += deltaT[1];
                if (rY < 0)
                    cellIndex[1]--;
                else
                    cellIndex[1]++;
            } else {
                t_z += deltaT[2];
                if (rZ < 0)
                    cellIndex[2]--;
                else
                    cellIndex[2]++;
            }

            if (cellIndex[0] < 0 || cellIndex[1] < 0 || cellIndex[2] < 0 ||
                    cellIndex[0] >= nX || cellIndex[1] >= nY || cellIndex[2] >= nZ)
                return null;

            if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].geometries != null) {
                if (firstIntersection != null && cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].inside(firstIntersection.point)) {
                    GeoPoint closestPoint =
                            findClosestIntersection(ray, cells[cellIndex[0]][cellIndex[1]][cellIndex[2]]);

                    if (closestPoint != null) {
                        if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].inside(closestPoint.point)) {
                            if (ray.getHead().distance(closestPoint.point) <= ray.getHead().distance(firstIntersection.point))
                                return closestPoint;
                        }
                    }
                    return firstIntersection;

                } else {
                    GeoPoint closestPoint =
                            findClosestIntersection(ray, cells[cellIndex[0]][cellIndex[1]][cellIndex[2]]);
                    if (closestPoint != null) {
                        if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].inside(closestPoint.point)) {
                            return closestPoint;
                        } else {
                            if (firstIntersection == null ||
                                    ray.getHead().distance(closestPoint.point) <= ray.getHead().distance(firstIntersection.point))
                                firstIntersection = closestPoint;
                        }
                    }
                }
            }
        }
    }

    private Geometries geometriesInPath(Ray ray) {
        //if rey intersect the grid
        var intersection = gridLimits.findIntersections(ray);
        if (intersection == null) return null;

        //the first voxel that the ray intersects
        Point p = fixPoint(ray.getHead());
        int[] cellIndex = findVoxel(p);
        if (cellIndex == null) {
            if (intersection.size() == 2) {
                p = intersection.getFirst().distance(p) < intersection.get(1).distance(p) ?
                        intersection.getFirst() : intersection.get(1);
            } else
                p = intersection.getFirst();
            p = fixPoint(p);
            cellIndex = findVoxel(p);
        }
        Vector v = ray.getDirection();

        //we want to separate the coordinates
        double rX = v.getX();
        double rY = v.getY();
        double rZ = v.getZ();
        double oX = p.getX();
        double oY = p.getY();
        double oZ = p.getZ();

        double[] rayOrigGrid = {
                oX - gridMin[0],
                oY - gridMin[1],
                oZ - gridMin[2],
        };
        double[] deltaT = {
                abs(cellSize[0] / rX),
                abs(cellSize[1] / rY),
                abs(cellSize[2] / rZ),
        };

        double t_x = rX < 0 ? (floor(rayOrigGrid[0] / cellSize[0]) * cellSize[0] - rayOrigGrid[0]) / rX
                : ((floor(rayOrigGrid[0] / cellSize[0]) + 1) * cellSize[0] - rayOrigGrid[0]) / rX;
        double t_y = rY < 0 ? (floor(rayOrigGrid[1] / cellSize[1]) * cellSize[1] - rayOrigGrid[1]) / rY
                : ((floor(rayOrigGrid[1] / cellSize[1]) + 1) * cellSize[1] - rayOrigGrid[1]) / rY;
        double t_z = rZ < 0 ? (floor(rayOrigGrid[2] / cellSize[2]) * cellSize[2] - rayOrigGrid[2]) / rZ
                : ((floor(rayOrigGrid[2] / cellSize[2]) + 1) * cellSize[2] - rayOrigGrid[2]) / rZ;

        Geometries geometries = new Geometries();
        if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].geometries != null)
            geometries.add(cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].geometries);

        while (true) {

            if (t_x <= t_y && t_x <= t_z) {
                t_x += deltaT[0];
                if (rX < 0)
                    cellIndex[0]--;
                else
                    cellIndex[0]++;
            } else if (t_y <= t_z) {
                t_y += deltaT[1];
                if (rY < 0)
                    cellIndex[1]--;
                else
                    cellIndex[1]++;
            } else {
                t_z += deltaT[2];
                if (rZ < 0)
                    cellIndex[2]--;
                else
                    cellIndex[2]++;
            }

            if (cellIndex[0] < 0 || cellIndex[1] < 0 || cellIndex[2] < 0 ||
                    cellIndex[0] >= nX || cellIndex[1] >= nY || cellIndex[2] >= nZ)
                return geometries;

            if (cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].geometries != null)
                geometries.add(cells[cellIndex[0]][cellIndex[1]][cellIndex[2]].geometries);
        }
    }

    private GeoPoint findClosestIntersection(Ray ray, Voxel cell) {
        var gp = cell.geometries.findGeoIntersections(ray);
        return ray.findClosestGeoPoint(gp);
    }

    private void insert(Intersectable shape) {
        for (int i = 0; i < nX; i++) {
            for (int j = 0; j < nY; j++) {
                for (int k = 0; k < nZ; k++) {
                    if (cells[i][j][k].between(shape.max, shape.min)) {
                        if (cells[i][j][k].geometries == null) cells[i][j][k].geometries = new Geometries();
                        cells[i][j][k].geometries.add(shape);
                    }
                }
            }
        }
    }

    private void buildBox() {
        Point minminmin = new Point(gridMin[0], gridMin[1], gridMin[2]);
        Point minminmax = new Point(gridMin[0], gridMin[1], gridMax[2]);
        Point minmaxmin = new Point(gridMin[0], gridMax[1], gridMin[2]);
        Point minmaxmax = new Point(gridMin[0], gridMax[1], gridMax[2]);
        Point maxminmin = new Point(gridMax[0], gridMin[1], gridMin[2]);
        Point maxminmax = new Point(gridMax[0], gridMin[1], gridMax[2]);
        Point maxmaxmin = new Point(gridMax[0], gridMax[1], gridMin[2]);
        Point maxmaxmax = new Point(gridMax[0], gridMax[1], gridMax[2]);
        gridLimits = new Geometries();
        gridLimits.add(
                new Polygon(minminmin, minmaxmin, maxmaxmin, maxminmin),
                new Polygon(minminmin, minmaxmin, minmaxmax, minminmax),
                new Polygon(maxminmin, maxmaxmin, maxmaxmax, maxminmax),
                new Polygon(minmaxmax, maxmaxmax, maxminmax, minminmax),
                new Polygon(minmaxmax, maxmaxmax, maxmaxmin, minmaxmin),
                new Polygon(minminmin, minminmax, maxminmax, maxminmin)
        );
    }

    private int[] findVoxel(Point point) {
        boolean flag = (point.getX() - gridMin[0]) % cellSize[0] == 0;
        int i = (int) ((point.getX() - gridMin[0]) / cellSize[0]);
        if (i < 0 || i >= nX) {
            if (i == nX && flag)
                i -= 1;
            else return null;
        }

        flag = (point.getY() - gridMin[1]) % cellSize[1] == 0;
        int j = (int) ((point.getY() - gridMin[1]) / cellSize[1]);
        if (j < 0 || j >= nY) {
            if (j == nY && flag)
                j -= 1;
            else return null;
        }

        flag = (point.getZ() - gridMin[2]) % cellSize[2] == 0;
        int k = (int) ((point.getZ() - gridMin[2]) / cellSize[2]);
        if (k < 0 || k >= nZ) {
            if (k == nZ && flag)
                k -= 1;
            else return null;
        }

        return new int[]{i, j, k};
    }

    // Function to find the closest integer that divides the size without a remainder
    private int closestDivisor(int size, int estimate) {
        if (estimate == 0) return 1;

        int lower = estimate;
        int upper = estimate;

        while (lower > 0 && size % lower != 0) {
            lower--;
        }

        while (size % upper != 0) {
            upper++;
        }

        if (lower == 0) return upper;
        return (estimate - lower <= upper - estimate) ? lower : upper;
    }

    private Point fixPoint(Point p) {
        if (isZero((p.getX() - gridMin[0]))) {
            p = p.add(Vector.X.scale(DELTA));
        }
        if (isZero((p.getX() - gridMax[0]))) {
            p = p.add(Vector.X.scale(-DELTA));
        }
        if (isZero((p.getY() - gridMin[1]))) {
            p = p.add(Vector.Y.scale(DELTA));
        }
        if (isZero((p.getY() - gridMax[1]))) {
            p = p.add(Vector.Y.scale(-DELTA));
        }
        if (isZero((p.getZ() - gridMin[2]))) {
            p = p.add(Vector.Z.scale(DELTA));
        }
        if (isZero((p.getZ() - gridMax[2]))) {
            p = p.add(Vector.Z.scale(-DELTA));
        }
        return p;
    }

    @Override
    protected Color calcGlobalEffect(Ray ray, Double3 kx, int level, Double3 k) {
        Double3 kkx = kx.product(k);
        if (kkx.lowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        GeoPoint gp = traversGrid(ray);
        return gp == null ? scene.background
                : calcColor(gp, ray, level - 1, kkx).scale(kx);
    }

    @Override
    protected Double3 transparency(GeoPoint gp, LightSource light, Vector l, Vector n) {
        Ray ray = new Ray(gp.point, l.scale(-1), n);
        Geometries geometries = geometriesInPath(ray);
        if (geometries == null) return Double3.ONE;
        var intersections = geometries.findGeoIntersections(ray, light.getDistance(gp.point));
        if (intersections == null) return Double3.ONE;

        Double3 ktr = Double3.ONE;
        for (var intersection : intersections)
            ktr = ktr.product(intersection.geometry.getMaterial().kT);
        return ktr;
    }
}
