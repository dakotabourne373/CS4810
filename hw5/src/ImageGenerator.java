import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;

class ImageGenerator {
    private static ArrayList<Point> pointList = new ArrayList<>();
    private static ArrayList<Point> ddaList;
    private static Matrix projection = new Matrix();
    private static Map<String, ImageObj> objectMap = new HashMap<>();
    private static Map<String, Double> variableMap = new HashMap<>();
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

        String filename = firstLine[3];
        int frames = Integer.parseInt(firstLine[4]);
        variableMap.put("l", (double) frames);

        if(mainCMD.equals("pngs")){
            for(int frame=0; frame<frames; frame++) {
                variableMap.put("f", (double) frame);
                colorArr = new double[]{255, 255, 255, 255};
                boolean iflt = true;
                in = new Scanner(file);
                in.nextLine();
                ImageObj curObj = new ImageObj("placeholder", "world", false);
                buffer = new double[width][height];
                for(double[] row : buffer) {
                    Arrays.fill(row, 1.0);
                }

                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                raster = image.getRaster();

                while(in.hasNextLine()) {
                    String[] line = in.nextLine().strip().split("\s+");
                    String cmd = line[0];
                    Point point;
                    if(cmd.equals("else")) {
                        iflt = !iflt;
                        continue;
                    }else if(cmd.equals("fi")){
                        iflt = true;
                        continue;
                    }
                    double x, y, z, w;
                    if(iflt) {
                        switch (cmd) {
                            case "track" -> curObj.tracking = line[1];
                            case "trackscale" -> {
                                curObj.tracking = line[1];
                                curObj.willScale = true;
                            }
                            case "trackstretch" -> {
                                curObj.tracking = line[1];
                                curObj.willScale = true;
                                curObj.willStretch = true;
                            }
                            case "trackroll" -> {
                                curObj.tracking = line[1];
                                curObj.axis = line[2];
                                curObj.trackingSec = line[3];
                            }
                            case "bone" -> {
                                double d = checkForVar(line[1]);
                                curObj.setBone(d);
                            }
                            case "add" -> {
                                double a = checkForVar(line[2]);
                                double b = checkForVar(line[3]);
                                variableMap.put(line[1], a + b);
                            }
                            case "sub" -> {
                                double a = checkForVar(line[2]);
                                double b = checkForVar(line[3]);
                                variableMap.put(line[1], a - b);
                            }
                            case "mul" -> {
                                double a = checkForVar(line[2]);
                                double b = checkForVar(line[3]);
                                variableMap.put(line[1], a * b);
                            }
                            case "div" -> {
                                double a = checkForVar(line[2]);
                                double b = checkForVar(line[3]);
                                variableMap.put(line[1], a / b);
                            }
                            case "pow" -> {
                                double a = checkForVar(line[2]);
                                double b = checkForVar(line[3]);
                                variableMap.put(line[1], Math.pow(a, b));
                            }
                            case "sin" -> {
                                double a = checkForVar(line[2]);
                                variableMap.put(line[1], Math.sin(Math.toRadians(a)));
                            }
                            case "cos" -> {
                                double a = checkForVar(line[2]);
                                variableMap.put(line[1], Math.cos(Math.toRadians(a)));
                            }
                            case "object" -> {
                                if (!curObj.computedMV) {
                                    curObj.generateMV(objectMap);
                                }
                                pointList = new ArrayList<>();
                                curObj = new ImageObj(line[1], line[2], false);
                                objectMap.put(line[1], curObj);
                            }
                            case "position" -> {
                                double a = checkForVar(line[1]);
                                double b = checkForVar(line[2]);
                                double c = checkForVar(line[3]);
                                curObj.setPosition(a, b, c);
                            }
                            case "quaternion" -> {
                                w = checkForVar(line[1]);
                                x = checkForVar(line[2]);
                                y = checkForVar(line[3]);
                                z = checkForVar(line[4]);
                                curObj.setOrientation(x, y, z, w);
                            }
                            case "xyz" -> {
                                x = checkForVar(line[1]);
                                y = checkForVar(line[2]);
                                z = checkForVar(line[3]);
                                point = new Point(x, y, z, 1, colorArr);

                                pointList.add(point);
                            }

                            case "trif" -> {
                                if (!curObj.computedMV) {
                                    curObj.generateMV(objectMap);
                                }
                                int ind1 = Integer.parseInt(line[1]);
                                int ind2 = Integer.parseInt(line[2]);
                                int ind3 = Integer.parseInt(line[3]);
                                trif(ind1, ind2, ind3, curObj);
                            }
                            case "color" -> {
                                for (int i = 1; i < 4; i++) {
                                    double color = checkForVar(line[i]);
                                    if (color <= 0.0) {
                                        color = 0;
                                    } else if (color > 0.0 && color < 1) {
                                        color *= 255;
                                    } else {
                                        color = 255;
                                    }
                                    colorArr[i - 1] = color;
                                }
                            }
                            case "loadp" -> {
                                double[][] newMat = new double[4][4];
                                int ind = 1;
                                for (int i = 0; i < 4; i++) {
                                    for (int j = 0; j < 4; j++) {
                                        double value = Double.parseDouble(line[ind]);
                                        newMat[i][j] = value;
                                        ind++;
                                    }
                                }
                                projection = new Matrix(newMat);
                            }
                        }
                    }

                }
                File outFile = new File(filename + String.format("%03d", frame) + ".png");
//                File outFile = new File(filename + "/" + filename + String.format("%03d", frame) + ".png");
//                outFile.getParentFile().mkdirs();
                ImageIO.write(image, "png", outFile);
            }
        }
    }

    public static double checkForVar(String a) {
        Double ret = variableMap.get(a);
        if(ret == null){
            ret = Double.parseDouble(a);
        }
        return ret;
    }

    public static void trif(int i1, int i2, int i3, ImageObj object){
        ddaList = new ArrayList<>();

        i1 = indexHelper(i1);
        i2 = indexHelper(i2);
        i3 = indexHelper(i3);

        Point p1 = pointList.get(i1);
        Point p2 = pointList.get(i2);
        Point p3 = pointList.get(i3);

        p1 = new Point(p1.vector[0], p1.vector[1], p1.vector[2], p1.vector[3], p1.color);
        p2 = new Point(p2.vector[0], p2.vector[1], p2.vector[2], p2.vector[3], p2.color);
        p3 = new Point(p3.vector[0], p3.vector[1], p3.vector[2], p3.vector[3], p3.color);

        //Apply M/V transformations here to each vertex
        Matrix cam = new Matrix();
        Matrix obj = new Matrix(object.getModelView());
        if(objectMap.get("camera") != null){
            if(objectMap.get("camera").getModelView() == null){
                objectMap.get("camera").generateMV(objectMap);
            }
            cam = new Matrix(objectMap.get("camera").inverseMV);
        }
        if(object.bone) {
            //multiply by bone
            if(!object.computedBone/*&& this.parent.equals("world")*/) {
                if(object.trackingSec.equals("") && object.axis.equals("")) {
                    object.trackMatrix(objectMap);
                }else {
                    object.pointAndRoll(objectMap);
                }

            }
            p1.multByMat(object.getBoneMat());
            p2.multByMat(object.getBoneMat());
            p3.multByMat(object.getBoneMat());
        }

        obj.multByMatrix(cam);
        p1.multByMat(obj);
        p2.multByMat(obj);
        p3.multByMat(obj);

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

    private static int indexHelper(int index){
        if(index > 0)
            index -= 1;
        else
            index += pointList.size();

        return index;
    }
}