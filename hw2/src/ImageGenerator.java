import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

class ImageGenerator {
    private static ArrayList<Point> pointList = new ArrayList<>();
    private static ArrayList<Point> ddaList;
    private static Matrix modelView = new Matrix();
    private static Matrix projection = new Matrix();
    private static boolean cull = false;
    private static WritableRaster raster;
    private static double[][] buffer;
    private static int width;
    private static int height;
    private static double[] colorArr = new double[]{255, 255, 255, 255};

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
        Scanner in = new Scanner(file);

        String[] firstLine = in.nextLine().strip().split("\s+");

        String mainCMD = firstLine[0];
        width = Integer.parseInt(firstLine[1]);
        height = Integer.parseInt(firstLine[2]);
        buffer = new double[width][height];
        for(double[] row : buffer) {
            Arrays.fill(row, 1.0);
        }
        String filename = firstLine[3];

        if(mainCMD.equals("png")){
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            raster = image.getRaster();

            while(in.hasNextLine()) {
                String[] line = in.nextLine().strip().split("\s+");
                String cmd = line[0];
                Point point;

                double x, y, z, w;

                switch (cmd) {
                    case "xyz" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        z = Double.parseDouble(line[3]);
                        point = new Point(x, y, z, 1, colorArr);
                        pointList.add(point);
                    }
                    case "xyzw" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        z = Double.parseDouble(line[3]);
                        w = Double.parseDouble(line[4]);
                        point = new Point(x, y, z, w, colorArr);
                        pointList.add(point);
                    }
                    case "trif" -> {
                        int ind1 = Integer.parseInt(line[1]);
                        int ind2 = Integer.parseInt(line[2]);
                        int ind3 = Integer.parseInt(line[3]);
                        trif(ind1, ind2, ind3);
                    }
                    case "trig" -> {
                        int ind1 = Integer.parseInt(line[1]);
                        int ind2 = Integer.parseInt(line[2]);
                        int ind3 = Integer.parseInt(line[3]);
                        trig(ind1, ind2, ind3);
                    }
                    case "color" -> {
                        for(int i=1; i < 4; i++){
                             double color = Double.parseDouble(line[i]);
                            if(color <= 0.0){
                                color = 0;
                            }else if(color > 0.0 && color < 1){
                                color *= 255;
                            }else{
                                color = 255;
                            }
                            colorArr[i-1] = color;
                        }
                    }
                    case "multmv" -> {
                        double[][] newMat = new double[4][4];
                        int ind = 1;
                        for(int i=0; i < 4; i++){
                            for(int j=0; j < 4; j++){
                                double value = Double.parseDouble(line[ind]);
                                newMat[i][j] = value;
                                ind++;
                            }
                        }
                        Matrix multMV = new Matrix(newMat);
                        modelView.multByMatrix(multMV);
                    }
                    case "loadmv" -> {
                        double[][] newMat = new double[4][4];
                        int ind = 1;
                        for(int i=0; i < 4; i++){
                            for(int j=0; j < 4; j++){
                                double value = Double.parseDouble(line[ind]);
                                newMat[i][j] = value;
                                ind++;
                            }
                        }
                        modelView = new Matrix(newMat);
                    }
                    case "translate" -> {
                        for(int i=1; i<4; i++){
                            double t = Double.parseDouble(line[i]);
                            modelView.addValue(i-1, 3, t);
                        }
                    }
                    case "scale" -> {
                        for(int i=1; i<4; i++){
                            double t = Double.parseDouble(line[i]);
                            modelView.multValue(i-1, i-1, t);
                        }
                    }
                    case "rotatex", "rotatey", "rotatez" -> {
                        double degrees = Double.parseDouble(line[1]);
                        rotatexyz(Math.toRadians(degrees), cmd.substring(6));
                    }
                    case "rotate" -> {
                        double degrees = Double.parseDouble(line[1]);
                        x = Double.parseDouble(line[2]);
                        y = Double.parseDouble(line[3]);
                        z = Double.parseDouble(line[4]);
                        rotate(Math.toRadians(degrees), x, y, z);
                    }
                    case "loadp" -> {
                        double[][] newMat = new double[4][4];
                        int ind = 1;
                        for(int i=0; i < 4; i++){
                            for(int j=0; j < 4; j++){
                                double value = Double.parseDouble(line[ind]);
                                newMat[i][j] = value;
                                ind++;
                            }
                        }
                        projection = new Matrix(newMat);
                    }
                    case "ortho" -> {
                        double l = Double.parseDouble(line[1]);
                        double r = Double.parseDouble(line[2]);
                        double b = Double.parseDouble(line[3]);
                        double t = Double.parseDouble(line[4]);
                        double n = Double.parseDouble(line[5]);
                        double f = Double.parseDouble(line[6]);

                        n = (2 * n) - f;

                        ortho(l, r, b, t, n, f);
                    }
                    case "frustum" -> {
                        double l = Double.parseDouble(line[1]);
                        double r = Double.parseDouble(line[2]);
                        double b = Double.parseDouble(line[3]);
                        double t = Double.parseDouble(line[4]);
                        double n = Double.parseDouble(line[5]);
                        double f = Double.parseDouble(line[6]);

                        frustum(l, r, b, t, n, f);
                    }
                    case "cull" ->
                        cull = true;
                }
                ImageIO.write(image, "png", new File(filename));
            }
        }
    }

    public static void ortho(double l, double r, double b, double t, double n, double f){
        double[][] proj = new double[4][4];

        double tx = - (r + l) / (r - l);
        double ty = - (t + b) / (t - b);
        double tz = - (f + n) / (f - n);

        proj[0][0] = 2 / (r - l);
        proj[1][1] = 2 / (t - b);
        proj[2][2] = -2 / (f - n);
        proj[3][3] = 1;
        proj[0][3] = tx;
        proj[1][3] = ty;
        proj[2][3] = tz;

        projection = new Matrix(proj);
    }

    public static void frustum(double l, double r, double b, double t, double n, double f){
        double[][] proj = new double[4][4];

        double A = (r + l) / (r - l);
        double B = (t + b) / (t - b);
        double C = - (f + n) / (f - n);
        double D = - (2 * n * f) / (f - n);

        proj[0][0] = (2 * n) / (r - l);
        proj[1][1] = (2 * n) / (t - b);
        proj[3][2] = -1;
        proj[0][2] = A;
        proj[1][2] = B;
        proj[2][2] = C;
        proj[2][3] = D;

        projection = new Matrix(proj);
    }

    public static void rotate(double radians, double x, double y, double z){
        Matrix xmat, ymat, zmat;
        double[][] mat = new double[4][4];
        Point p = new Point(x, y, z, 1, colorArr);
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        double t = 1 - c;
        if(x + y + z != 1) {
            p.normalize();
            x = p.vector[0];
            y = p.vector[1];
            z = p.vector[2];
        }

        double txy = t * x * y;
        double txz = t * x * z;
        double tyz = t * y * z;

        double sx = s * x;
        double sy = s * y;
        double sz = s * z;

        mat[3][3] = 1;
        mat[0][0] = (t * x * x) + c;
        mat[0][1] = txy - sz;
        mat[0][2] = txz + sy;
        mat[1][0] = txy + sz;
        mat[1][1] = (t * y * y) + c;
        mat[1][2] = tyz - sx;
        mat[2][0] = txz - sy;
        mat[2][1] = tyz + sx;
        mat[2][2] = (t * z * z) + c;

        zmat = new Matrix(mat);
//        xmat = rotatexyz(radians, "x");
//        ymat = rotatexyz(radians, "y");
//        zmat = rotatexyz(radians, "z");
//
//        zmat.multByMatrix(ymat);
//        zmat.multByMatrix(xmat);
//
//        zmat.multByPoint(p.vector);

        modelView.multByMatrix(zmat);
    }

    public static Matrix rotatexyz(double radians, String axis){
        double[][] rotatemat = new double[4][4];
        rotatemat[3][3] = 1;
        double c = Math.cos(radians);
        double s = Math.sin(radians);
        switch (axis){
            case "x":
                rotatemat[0][0] = 1.0;
                rotatemat[1][1] = c;
                rotatemat[1][2] = -1 * s;
                rotatemat[2][1] = s;
                rotatemat[2][2] = c;
                break;
            case "y":
                rotatemat[1][1] = 1.0;
                rotatemat[0][0] = c;
                rotatemat[0][2] = s;
                rotatemat[2][0] = -1 * s;
                rotatemat[2][2] = c;
                break;
            case "z":
                rotatemat[2][2] = 1.0;
                rotatemat[0][0] = c;
                rotatemat[0][1] = -1 * s;
                rotatemat[1][0] = s;
                rotatemat[1][1] = c;
        }
        Matrix ret = new Matrix(rotatemat);
        modelView.multByMatrix(ret);
        return ret;
    }

    public static void trig(int i1, int i2, int i3){
        ddaList = new ArrayList<>();

        i1 = indexHelper(i1);
        i2 = indexHelper(i2);
        i3 = indexHelper(i3);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);
        Point p3 = pointList.get(i3);

        if(cull && cullCheck(p1, p2, p3))
            return;

        p1 = new Point(p1.vector[0], p1.vector[1], p1.vector[2], p1.vector[3], p1.color);
        p2 = new Point(p2.vector[0], p2.vector[1], p2.vector[2], p2.vector[3], p2.color);
        p3 = new Point(p3.vector[0], p3.vector[1], p3.vector[2], p3.vector[3], p3.color);

        //Apply M/V transformations here to each vertex
        p1.multByMat(modelView);
        p2.multByMat(modelView);
        p3.multByMat(modelView);

        //Apply projection matrix to each vertex
        p1.multByMat(projection);
        p2.multByMat(projection);
        p3.multByMat(projection);

        //Divide each vertex by w
        p1.divideByW();
        p2.divideByW();
        p3.divideByW();

        //apply viewport transformation
        p1.applyViewport(width, height);
        p2.applyViewport(width, height);
        p3.applyViewport(width, height);

        //step in Y
        trigStepInY(p1, p2);
        trigStepInY(p1, p3);
        trigStepInY(p2, p3);

        Collections.sort(ddaList);

        for(int i=0; i < ddaList.size(); i+=2){
            Point p4 = ddaList.get(i);
            Point p5 = ddaList.get(i+1);
            trigStepInX(p4, p5);
        }

    }

    private static void trigStepInY(Point p1, Point p2){
        if(p1.vector[1] > p2.vector[1]){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        double p2y;

        p2y = p2.vector[1];

        Point iteratedPoint = p1.getInitialOffset(p2, "y");
        Point offset = p1.getOffset(p2, "y");

        if(iteratedPoint.vector[1] < 0)
            iteratedPoint.vector[1] = 0;

        while(iteratedPoint.vector[1] < p2y){

            Point newPoint = new Point(iteratedPoint.vector, iteratedPoint.color);

            ddaList.add(newPoint);
            iteratedPoint.vectorAdd(offset);

        }
    }

    private static void trigStepInX(Point p1, Point p2){
        int xStep;
        double yStep;

        double p2x;

        p2x = p2.vector[0];

        Point iteratedPoint = p1.getInitialOffset(p2, "x");
        Point offset = p1.getOffset(p2, "x");

        if(iteratedPoint.vector[0] < 0) {
            iteratedPoint.vector[0] = 0;
        }else if(iteratedPoint.vector[0] > width) {
            return;
        }

        while(iteratedPoint.vector[0] < p2x){
            xStep = (int) Math.ceil(iteratedPoint.vector[0]);
            yStep = iteratedPoint.vector[1];

            double z = iteratedPoint.vector[2];

            if(xStep >= 0 && xStep < width && yStep >= 0 && yStep < height && z > 0 && z < 1) {
                int y = (int) Math.floor(yStep + 0.5);
                if(z < buffer[xStep][y]) {
                    double w_1 = iteratedPoint.vector[3];
                    double r = iteratedPoint.color[0];
                    double g = iteratedPoint.color[1];
                    double b = iteratedPoint.color[2];

                    r /= w_1;
                    g /= w_1;
                    b /= w_1;

                    buffer[xStep][y] = z;
                    raster.setPixel(xStep, y, new double[]{r, g, b, 255});
                }
            }
            iteratedPoint.vectorAdd(offset);
        }
    }

    public static void trif(int i1, int i2, int i3){
        ddaList = new ArrayList<>();

        i1 = indexHelper(i1);
        i2 = indexHelper(i2);
        i3 = indexHelper(i3);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);
        Point p3 = pointList.get(i3);

        if(cull && cullCheck(p1, p2, p3))
            return;

        p1 = new Point(p1.vector[0], p1.vector[1], p1.vector[2], p1.vector[3], p1.color);
        p2 = new Point(p2.vector[0], p2.vector[1], p2.vector[2], p2.vector[3], p2.color);
        p3 = new Point(p3.vector[0], p3.vector[1], p3.vector[2], p3.vector[3], p3.color);

        //Apply M/V transformations here to each vertex
        p1.multByMat(modelView);
        p2.multByMat(modelView);
        p3.multByMat(modelView);

        //Apply projection matrix to each vertex
        p1.multByMat(projection);
        p2.multByMat(projection);
        p3.multByMat(projection);

        //Divide each vertex by w
        p1.divideByW();
        p2.divideByW();
        p3.divideByW();

        //apply viewport transformation
        p1.applyViewport(width, height);
        p2.applyViewport(width, height);
        p3.applyViewport(width, height);

        //step in Y
        trifStepInY(p1, p2);
        trifStepInY(p1, p3);
        trifStepInY(p2, p3);

        Collections.sort(ddaList, new sortByXRounded());

        for(int i=0; i < ddaList.size(); i+=2){
            Point p4 = ddaList.get(i);
            Point p5 = ddaList.get(i+1);
            trifStepInX(p4, p5);
        }

    }

    private static void trifStepInX(Point p1, Point p2){
        int xStep;
        double yStep;
        double p1x,p1y, p2x, p2y;
        p1x = p1.vector[0];
        p2x = p2.vector[0];
        p1y = p1.vector[1];
        p2y = p2.vector[1];

        xStep = (int) Math.ceil(p1x);
        if(xStep < 0) {
            xStep = 0;
        }else if(xStep > width) {
            return;
        }

        yStep = (((xStep - p1x) / (p2x - p1x)) * (p2y - p1y)) + p1y;

        double changeInY = (p2y - p1y) / (p2x - p1x);
        while(xStep < p2x && xStep < width){
            double z = ((p2x - xStep) / (p2x - p1x)) * (p1.vector[2]) + ((xStep - p1x) / (p2x - p1x)) * (p2.vector[2]);
            if(xStep >= 0 && xStep < width && yStep >= 0 && yStep < height && z > 0 && z < 1) {
                int y = (int) Math.floor(yStep + 0.5);
                if(z <= buffer[xStep][y]) {
                    buffer[xStep][y] = z;
                    raster.setPixel(xStep, y, colorArr);
                }
            }
            xStep += 1;
            yStep += changeInY;
        }
    }

    private static void trifStepInY(Point p1, Point p2){
        double xStep;
        int yStep;
        if(p1.vector[1] > p2.vector[1]){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        double p1x,p1y, p2x, p2y;
        p1x = p1.vector[0];
        p2x = p2.vector[0];
        p1y = p1.vector[1];
        p2y = p2.vector[1];

        yStep = (int) Math.ceil(p1y);
        if(yStep < 0)
            yStep = 0;

        xStep = (((yStep - p1y) / (p2y - p1y)) * (p2x - p1x)) + p1x;

        double changeInX = (p2x - p1x) / (p2y - p1y);
        while(yStep < p2y && yStep < height){
            double z = ((yStep - p2y) / (p1y - p2y)) * (p1.vector[2]) + ((p1y - yStep) / (p1y - p2y)) * (p2.vector[2]);
            Point newPoint = new Point(xStep, yStep, z, 1, colorArr);
            ddaList.add(newPoint);
            xStep += changeInX;
            yStep += 1;
        }
    }

    private static boolean cullCheck(Point p1, Point p2, Point p3){
        int sum = 0;
        sum += (p2.vector[0] - p1.vector[0]) * (p2.vector[1] + p1.vector[1]);
        sum += (p3.vector[0] - p2.vector[0]) * (p3.vector[1] + p2.vector[1]);
        sum += (p1.vector[0] - p3.vector[0]) * (p3.vector[1] + p1.vector[1]);

        if(sum < 0)
            return true;
        else
            return false;
    }

    private static int indexHelper(int index){
        if(index > 0)
            index -= 1;
        else
            index += pointList.size();

        return index;
    }
}