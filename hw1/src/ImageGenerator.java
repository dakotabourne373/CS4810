import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class ImageGenerator {
    private static ArrayList<Point> pointList = new ArrayList<>();
    private static ArrayList<Point> ddaList;

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
        Scanner in = new Scanner(file);

        String[] firstLine = in.nextLine().strip().split("\s+");

        String mainCMD = firstLine[0];
        int width = Integer.parseInt(firstLine[1]);
        int height = Integer.parseInt(firstLine[2]);
        String filename = firstLine[3];

        if(mainCMD.equals("png")){
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            WritableRaster raster = image.getRaster();

            while(in.hasNextLine()) {
                String[] line = in.nextLine().strip().split("\s+");
                String cmd = line[0];
                Point point;

                double x, y;
                int r = 255, g = 255, b = 255;
                int a = 255;
                int index1, index2, index3;

                switch (cmd) {
                    case "xyc" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        String hex = line[3];
                        point = new Point(x, y, hex);
                        pointList.add(point);
                    }
                    case "xyrgb" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        r = Integer.parseInt(line[3]);
                        g = Integer.parseInt(line[4]);
                        b = Integer.parseInt(line[5]);
                        point = new Point(x, y, r, g, b);
                        pointList.add(point);
                    }
                    case "xyrgba" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        r = Integer.parseInt(line[3]);
                        g = Integer.parseInt(line[4]);
                        b = Integer.parseInt(line[5]);
                        a = Integer.parseInt(line[6]);
                        point = new Point(x, y, r, g, b, a);
                        pointList.add(point);
                    }
                    case "linec" -> {
                        index1 = Integer.parseInt(line[1]);
                        index2 = Integer.parseInt(line[2]);
                        String hex = line[3];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                        int[] arr = new int[]{r, g, b, 255};
                        linec(index1, index2, arr, raster);
                    }
                    case "lineca" -> {
                        index1 = Integer.parseInt(line[1]);
                        index2 = Integer.parseInt(line[2]);
                        String hex = line[3];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                        a = Integer.valueOf(hex.substring(7, 9), 16);
                        int[] arr = new int[]{r, g, b, a};
                        linec(index1, index2, arr, raster);
                    }
                    case "trig" -> {
                        index1 = Integer.parseInt(line[1]);
                        index2 = Integer.parseInt(line[2]);
                        index3 = Integer.parseInt(line[3]);
                        trig(index1, index2, index3, raster);
                    }
                    case "lineg" -> {
                        index1 = Integer.parseInt(line[1]);
                        index2 = Integer.parseInt(line[2]);
                        lineg(index1, index2, raster);
                    }
                    case "tric" -> {
                        index1 = Integer.parseInt(line[1]);
                        index2 = Integer.parseInt(line[2]);
                        index3 = Integer.parseInt(line[3]);
                        String hex = line[4];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                        int[] arr = new int[]{r, g, b, 255};
                        tric(index1, index2, index3, arr, raster);
                    }
                    case "trica" -> {
                        index1 = Integer.parseInt(line[1]);
                        index2 = Integer.parseInt(line[2]);
                        index3 = Integer.parseInt(line[3]);
                        String hex = line[4];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                        a = Integer.valueOf(hex.substring(7, 9), 16);
                        int[] arr = new int[]{r, g, b, a};
                        tric(index1, index2, index3, arr, raster);
                    }
                    case "fann" -> {
                        int indexes = Integer.parseInt(line[1]);
                        index1 = Integer.parseInt(line[2]);
                        for(int i=1; i < indexes; i++){
                            index2 = Integer.parseInt(line[i+1]);
                            index3 = Integer.parseInt(line[i+2]);
                            fann(index1, index2, index3, raster);
                        }
                    }
                    case "stripn" -> {
                        int indexes = Integer.parseInt(line[1]);
                        for(int i=2; i < indexes; i++){
                            index1 = Integer.parseInt(line[i]);
                            index2 = Integer.parseInt(line[i+1]);
                            index3 = Integer.parseInt(line[i+2]);
                            fann(index1, index2, index3, raster);
                        }
                    }
                    case "polyec" -> {
                        ArrayList<Integer> indexArr = new ArrayList<>();
                        int numIndexes = Integer.parseInt(line[1]);
                        String hex = line[numIndexes+2];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                        int[] arr = new int[]{r, g, b, 255};

                        for(int i=2; i < numIndexes+2; i++){
                            indexArr.add(Integer.parseInt(line[i]));
                        }

                        polyec(indexArr, arr, raster);
                    }
                    case "polynz" -> {
                        ArrayList<Integer> indexArr = new ArrayList<>();
                        int numIndexes = Integer.parseInt(line[1]);
                        String hex = line[numIndexes+2];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                        int[] arr = new int[]{r, g, b, 255};

                        for(int i=2; i < numIndexes+2; i++){
                            indexArr.add(Integer.parseInt(line[i]));
                        }

                        polynz(indexArr, arr, raster);
                    }
                }
                ImageIO.write(image, "png", new File(filename));
            }
        }
    }

    public static void polynz(ArrayList<Integer> indexArr, int[] colorArr, WritableRaster raster){
        ddaList = new ArrayList<>();

        for(int i=0; i < indexArr.size()-1; i++) {
            int i1 = indexHelper(indexArr.get(i));
            int i2 = indexHelper(indexArr.get(i+1));

            Point p1 = pointList.get(i1);
            Point p2 = pointList.get(i2);

            polyecStepInY(p1, p2, colorArr, raster);
        }
        int iFirst = indexHelper(indexArr.get(0));
        int iLast = indexHelper(indexArr.get(indexArr.size()-1));

        polyecStepInY(pointList.get(iLast), pointList.get(iFirst), colorArr, raster);

        Collections.sort(ddaList);

        for(int i=0; i < ddaList.size(); i++){
            Point p4 = ddaList.get(i);
            Point p5 = p4;
            int curDirection = p4.direction;
            while(curDirection != 0) {
                p5 = ddaList.get(i + 1);
                curDirection += p5.direction;
                i++;
            }
            tricStepInX(p4, p5, colorArr, raster);
        }

    }

    public static void polyec(ArrayList<Integer> indexArr, int[] colorArr, WritableRaster raster){
        ddaList = new ArrayList<>();

        for(int i=0; i < indexArr.size()-1; i++) {
            int i1 = indexHelper(indexArr.get(i));
            int i2 = indexHelper(indexArr.get(i+1));


            Point p1 = pointList.get(i1);
            Point p2 = pointList.get(i2);

            polyecStepInY(p1, p2, colorArr, raster);
        }
        int iFirst = indexHelper(indexArr.get(0));
        int iLast = indexHelper(indexArr.get(indexArr.size()-1));

        polyecStepInY(pointList.get(iFirst), pointList.get(iLast), colorArr, raster);

        Collections.sort(ddaList);

        for(int i=0; i < ddaList.size(); i+=2){
            Point p4 = ddaList.get(i);
            Point p5 = ddaList.get(i+1);
            tricStepInX(p4, p5, colorArr, raster);
        }

    }

    private static void polyecStepInY(Point p1, Point p2, int[] colorArr, WritableRaster raster){
        double xStep;
        int yStep, direction;

        if(p1.y > p2.y)
            direction = -1;
        else
            direction = 1;

        if(p1.y > p2.y){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        yStep = (int) Math.ceil(p1.y);

        xStep = (((yStep - p1.y) / (p2.y - p1.y)) * (p2.x - p1.x)) + p1.x;

        double changeInX = (p2.x - p1.x) / (p2.y - p1.y);
        while(yStep < p2.y){
            Point newPoint = new Point(direction, xStep, (double) yStep, colorArr[0], colorArr[1], colorArr[2]);
            ddaList.add(newPoint);
            xStep += changeInX;
            yStep += 1;
        }
    }

    public static void tric(int i1, int i2, int i3, int[] colorArr, WritableRaster raster){
        ddaList = new ArrayList<>();

        i1 = indexHelper(i1);
        i2 = indexHelper(i2);
        i3 = indexHelper(i3);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);
        Point p3 = pointList.get(i3);

        tricStepInY(p1, p2, colorArr, raster);
        tricStepInY(p1, p3, colorArr, raster);
        tricStepInY(p2, p3, colorArr, raster);

        Collections.sort(ddaList);

        for(int i=0; i < ddaList.size(); i+=2){
            Point p4 = ddaList.get(i);
            Point p5 = ddaList.get(i+1);
            tricStepInX(p4, p5, colorArr, raster);
        }

    }

    private static void tricStepInX(Point p1, Point p2, int[] colorArr, WritableRaster raster){
        int xStep;
        double yStep;

        xStep = (int) Math.ceil(p1.x);

        yStep = (((xStep - p1.x) / (p2.x - p1.x)) * (p2.y - p1.y)) + p1.y;

        double changeInY = (p2.y - p1.y ) / (p2.x - p1.x);
        while(xStep < p2.x){
            raster.setPixel(xStep, (int) Math.floor(yStep + 0.5), colorArr);
            xStep += 1;
            yStep += changeInY;
        }
    }

    private static void tricStepInY(Point p1, Point p2, int[] colorArr, WritableRaster raster){
        double xStep;
        int yStep;

        if(p1.y > p2.y){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        yStep = (int) Math.ceil(p1.y);

        xStep = (((yStep - p1.y) / (p2.y - p1.y)) * (p2.x - p1.x)) + p1.x;

        double changeInX = (p2.x - p1.x) / (p2.y - p1.y);
        while(yStep < p2.y){
            Point newPoint = new Point(xStep, yStep, colorArr[0], colorArr[1], colorArr[2], colorArr[3]);
            ddaList.add(newPoint);
            xStep += changeInX;
            yStep += 1;
        }
    }

    private static void lineg(int i1, int i2, WritableRaster raster){
        i1 = indexHelper(i1);
        i2 = indexHelper(i2);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);

        if(Math.abs(p1.x - p2.x) <= Math.abs(p1.y - p2.y)){
            linegStepInY(p1, p2, raster);
        }else {
            linegStepInX(p1, p2, raster);
        }
    }

    private static void linegStepInX(Point p1, Point p2, WritableRaster raster){
        int xStep;
        double yStep;

        if(p1.x > p2.x){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        xStep = (int) Math.ceil(p1.x);

        yStep = (((xStep - p1.x) / (p2.x - p1.x)) * (p2.y - p1.y)) + p1.y;

        double changeInY = (p2.y - p1.y ) / (p2.x - p1.x);
        while(xStep < p2.x){
            int r = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.r) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.r));
            int g = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.g) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.g));
            int b = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.b) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.b));
            raster.setPixel(xStep, (int) Math.floor(yStep + 0.5), new int[]{r, g, b, 255});
            xStep += 1;
            yStep += changeInY;
        }
    }

    private static void linegStepInY(Point p1, Point p2, WritableRaster raster){
        double xStep;
        int yStep;

        if(p1.y > p2.y){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        yStep = (int) Math.ceil(p1.y);

        xStep = (((yStep - p1.y) / (p2.y - p1.y)) * (p2.x - p1.x)) + p1.x;

        double changeInX = (p2.x - p1.x) / (p2.y - p1.y);
        while(yStep < p2.y){
            int r = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.r) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.r));
            int g = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.g) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.g));
            int b = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.b) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.b));
//            int a = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.a) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.a));
//            int[] pixelArr = new int[]{-23, -23, -23, -23};
//            raster.getPixel((int) Math.floor(xStep + 0.5), yStep, pixelArr);
//            a = alphaEquation(a, pixelArr[3]);
//            System.out.println(pixelArr);
            raster.setPixel((int) Math.floor(xStep + 0.5), yStep, new int[]{r, g, b, 255});
//            Point newPoint = new Point(xStep, yStep, r, g, b);
//            ddaList.add(newPoint);
            xStep += changeInX;
            yStep += 1;
        }
    }

    private static boolean isPixelValid(int[] arr){
        boolean ret = false;
        for(int i=0; i < arr.length; i++){
            ret = (arr[i] > 0);
        }
        return ret;
    }

    private static int colorEquation(int Ca, int Cb, int Aa, int Ab, int Ao){
//        return (((Aa / Ao) * Ca) + ((1 - (Aa / Ao)) * Cb));
        return(((Ca * Aa) + (Cb * Ab * (1 - Aa))) / Ao);
    }

    private static int alphaEquation(int a, int b){
        return a + (b * (1 - a));
    }

    public static void trig(int i1, int i2, int i3, WritableRaster raster){
        ddaList = new ArrayList<>();

        i1 = indexHelper(i1);
        i2 = indexHelper(i2);
        i3 = indexHelper(i3);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);
        Point p3 = pointList.get(i3);

        trigStepInY(p1, p2, raster);
        trigStepInY(p1, p3, raster);
        trigStepInY(p2, p3, raster);

        Collections.sort(ddaList);

        for(int i=0; i < ddaList.size(); i+=2){
            Point p4 = ddaList.get(i);
            Point p5 = ddaList.get(i+1);
            trigStepInX(p4, p5, raster);
        }

    }

    private static void trigStepInX(Point p1, Point p2, WritableRaster raster){
        int xStep;
        double yStep;

//        if(p1.x > p2.x){
//            Point temp = p1;
//            p1 = p2;
//            p2 = temp;
//        }
        xStep = (int) Math.ceil(p1.x);

        yStep = (((xStep - p1.x) / (p2.x - p1.x)) * (p2.y - p1.y)) + p1.y;

        double changeInY = (p2.y - p1.y ) / (p2.x - p1.x);
        while(xStep < p2.x){
            int r = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.r) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.r));
            int g = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.g) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.g));
            int b = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.b) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.b));
            raster.setPixel(xStep, (int) Math.floor(yStep + 0.5), new int[]{r, g, b, 255});
            xStep += 1;
            yStep += changeInY;
        }
    }

    private static void trigStepInY(Point p1, Point p2, WritableRaster raster){
        double xStep;
        int yStep;

        if(p1.y > p2.y){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        yStep = (int) Math.ceil(p1.y);

        xStep = (((yStep - p1.y) / (p2.y - p1.y)) * (p2.x - p1.x)) + p1.x;

        double changeInX = (p2.x - p1.x) / (p2.y - p1.y);
        while(yStep < p2.y){
            int r = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.r) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.r));
            int g = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.g) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.g));
            int b = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.b) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.b));
            Point newPoint = new Point(xStep, yStep, r, g, b);
            ddaList.add(newPoint);
            xStep += changeInX;
            yStep += 1;
        }
    }

    public static void fann(int i1, int i2, int i3, WritableRaster raster){
        ddaList = new ArrayList<>();

        i1 = indexHelper(i1);
        i2 = indexHelper(i2);
        i3 = indexHelper(i3);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);
        Point p3 = pointList.get(i3);

        fannStepInY(p1, p2, raster);
        fannStepInY(p1, p3, raster);
        fannStepInY(p2, p3, raster);

        Collections.sort(ddaList, new sortByXRounded());

        for(int i=0; i < ddaList.size(); i+=2){
            Point p4 = ddaList.get(i);
            Point p5 = ddaList.get(i+1);
            fannStepInX(p4, p5, raster);
        }

    }

    private static void fannStepInX(Point p1, Point p2, WritableRaster raster){
        int xStep;
        double yStep;

        xStep = (int) Math.ceil(p1.xRounded);

        yStep = (((xStep - p1.x) / (p2.x - p1.x)) * (p2.y - p1.y)) + p1.y;

        double changeInY = (p2.y - p1.y) / (p2.x - p1.x);
        while(xStep < p2.x){
            int r = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.r) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.r));
            int g = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.g) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.g));
            int b = (int) (((p2.x - xStep) / (p2.x - p1.x)) * (p1.b) + ((xStep - p1.x) / (p2.x - p1.x)) * (p2.b));
            raster.setPixel(xStep, (int) Math.floor(yStep + 0.5), new int[]{r, g, b, 255});
            xStep += 1;
//            xRoundedStep += 1;
            yStep += changeInY;
        }
    }

    private static void fannStepInY(Point p1, Point p2, WritableRaster raster){
        double xStep;
        int yStep;

        if(p1.y > p2.y){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        yStep = (int) Math.ceil(p1.y);

        xStep = (((yStep - p1.y) / (p2.y - p1.y)) * (p2.x - p1.x)) + p1.x;

        double changeInX = (p2.x - p1.x) / (p2.y - p1.y);
        while(yStep < p2.y){
            int r = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.r) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.r));
            int g = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.g) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.g));
            int b = (int) (((yStep - p2.y) / (p1.y - p2.y)) * (p1.b) + ((p1.y - yStep) / (p1.y - p2.y)) * (p2.b));
            Point newPoint = new Point(xStep, yStep, Math.ceil(xStep), r, g, b);
            ddaList.add(newPoint);
            xStep += changeInX;
            yStep += 1;
        }
    }

    public static void linec(int i1, int i2, int[] colorArr, WritableRaster raster){
        i1 = indexHelper(i1);
        i2 = indexHelper(i2);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);

        if(Math.abs(p1.x - p2.x) <= Math.abs(p1.y - p2.y)){
            stepInY(p1, p2, colorArr, raster);
        }else {
            stepInX(p1, p2, colorArr, raster);
        }
    }

    private static void stepInY(Point p1, Point p2, int[] colorArr, WritableRaster raster){
        double xStep;
        int yStep;

        if(p1.y > p2.y){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        yStep = (int) Math.ceil(p1.y);

        xStep = (((yStep - p1.y) / (p2.y - p1.y)) * (p2.x - p1.x)) + p1.x;

        double changeInX = (p2.x - p1.x) / (p2.y - p1.y);
        while(yStep < p2.y){
            raster.setPixel((int) Math.floor(xStep + 0.5), yStep, colorArr);
            xStep += changeInX;
            yStep += 1;
        }
    }

    private static void stepInX(Point p1, Point p2, int[] colorArr, WritableRaster raster){
        int xStep;
        double yStep;

        if(p1.x > p2.x){
            Point temp = p1;
            p1 = p2;
            p2 = temp;
        }
        xStep = (int) Math.ceil(p1.x);

        yStep = (((xStep - p1.x) / (p2.x - p1.x)) * (p2.y - p1.y)) + p1.y;

        double changeInY = (p2.y - p1.y) / (p2.x - p1.x);
        while(xStep < p2.x){
            raster.setPixel(xStep, (int) Math.floor(yStep + 0.5), colorArr);
            xStep += 1;
            yStep += changeInY;
        }
    }

    private static int indexHelper(int index){
        if(index > 0)
            index -= 1;
        else
            index += pointList.size();

        return index;
    }
}