import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class ImageGenerator {
    private static ArrayList<ImageObj> objects = new ArrayList<>();
    private static ArrayList<ImageObj> lights = new ArrayList<>();
    private static Point eye = new Point(0, 0, 0);
    private static Vector forward = new Vector(0, 0, -1);
    private static Vector right = new Vector(1, 0 ,0);
    private static Vector up = new Vector(0 , 1, 0);
    private static WritableRaster raster;
    private static boolean willExpose = false;
    private static boolean willFisheye = false;
//    private static boolean inside = false;
    private static double exposure;
//    private static double[][] buffer;
    private static int width;
    private static int height;
    private static double[] colorArr = new double[]{1, 1, 1, 255};

    public static void main(String[] args) throws IOException {
        File file = new File(args[0]);
        Scanner in = new Scanner(file);

        String[] firstLine = in.nextLine().strip().split("\s+");

        String mainCMD = firstLine[0];
        width = Integer.parseInt(firstLine[1]);
        height = Integer.parseInt(firstLine[2]);
//        buffer = new double[width][height];
//        for(double[] row : buffer) {
//            Arrays.fill(row, 1.0);
//        }
        String filename = firstLine[3];

        if(mainCMD.equals("png")){
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
            raster = image.getRaster();

            while(in.hasNextLine()) {
                String[] line = in.nextLine().strip().split("\s+");
                String cmd = line[0];

                double x, y, z, w, r, g, b;

                switch (cmd) {
                    case "sphere" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        z = Double.parseDouble(line[3]);
                        r = Double.parseDouble(line[4]);
                        objects.add(new ImageObj(0, new Point(x, y, z), new double[]{r}, colorArr));
                    }
                    case "sun" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        z = Double.parseDouble(line[3]);
                        lights.add(new ImageObj(-1, new Point(x, y, z), null, colorArr));
                    }
                    case "bulb" -> {
                        x = Double.parseDouble(line[1]);
                        y = Double.parseDouble(line[2]);
                        z = Double.parseDouble(line[3]);
                        lights.add(new ImageObj(-2, new Point(x, y, z), null, colorArr));
                    }
                    case "color" -> {
                        r = Double.parseDouble(line[1]);
                        g = Double.parseDouble(line[2]);
                        b = Double.parseDouble(line[3]);

                        colorArr[0] = r;
                        colorArr[1] = g;
                        colorArr[2] = b;

                    }
                    case "eye" -> {
                        eye.point[0] = Double.parseDouble(line[1]);
                        eye.point[1] = Double.parseDouble(line[2]);
                        eye.point[2] = Double.parseDouble(line[3]);
                    }
                    case "expose" -> {
                        willExpose = true;
                        exposure = Double.parseDouble(line[1]);
                    }
                    case "fisheye" -> willFisheye = true;
                }
            }
            generateRays();
            ImageIO.write(image, "png", new File(filename));
        }
    }

    public static void generateRays(){
        int max = Math.max(width, height);
        for(int y=0; y < height; y++){
            double sy = (height - 2 * (double) y) / max;
            for(int x=0; x < width; x++){
                double sx = (2 * (double) x - width) / max;
                Vector dir = new Vector(0, 0, 0);
                double r2 = 0;

                if(willFisheye){
                    double forwardMag = forward.magnitude();
                    sx /= forwardMag;
                    sy /= forwardMag;
                    r2 = sx*sx + sy*sy;
                    if(Math.sqrt(r2) > 1)
                        continue;
                }

                dir = dir.vectorAdd(forward.scalarMult(Math.sqrt(1 - r2)));
                dir = dir.vectorAdd(right.scalarMult(sx));
                dir = dir.vectorAdd(up.scalarMult(sy));
                Ray ray = new Ray(eye, dir);

                Vector intersection = null;
                ImageObj closestObj = null;
                double t = Double.MAX_VALUE;
//                boolean[] inside = new boolean[]{false};
                for(ImageObj object : objects){
                    if(object.sides == 0){
                        double newT = sphereIntersection(ray, object, t);
                        if(newT < t && newT > 0) {
                            t = newT;
                            closestObj = object;
                            intersection = ray.direction.scalarMult(newT).vectorAdd(ray.origin);
                        }

                    }

                }
                if(intersection != null) {
                    double[] pixelColor = illumination(closestObj, intersection);

                    if(willExpose)
                        expose(pixelColor);

                    convertSRGB(pixelColor);
                    checkRGB(pixelColor);

                    raster.setPixel(x, y, new double[]{pixelColor[0], pixelColor[1], pixelColor[2], 255});
                }
            }
        }
    }

    public static double sphereIntersection(Ray ray, ImageObj sphere, double t){
        ray.direction.normalize();
        Vector c_r0 = sphere.coords.pointSub(ray.origin);
        double r2 = Math.pow(sphere.info[0], 2);
        double rayMag = ray.direction.magnitude();
        boolean inside = Math.pow(c_r0.magnitude(), 2) < r2;

        double tc = c_r0.dot(ray.direction) / rayMag;

        if(!inside && tc < 0){
            return t;
        }
        Vector tc_rd = ray.direction.scalarMult(tc);
        tc_rd = tc_rd.vectorAdd(ray.origin);
        tc_rd = tc_rd.vectorSub(sphere.coords);

        double d = Math.pow(tc_rd.magnitude(), 2);

        if(!inside && d > r2){
            return t;
        }

        double newT;
        double t_off = Math.sqrt(r2 - d) / rayMag;
        if(inside){
            newT = tc + t_off;
        }else{
            newT = tc - t_off;
        }
        return newT;

    }

    public static double[] illumination(ImageObj object, Vector intersection){
        Vector color = new Vector(0, 0, 0);
        for (ImageObj light : lights) {
            Vector normal = object.generateNormal(intersection);
//            if(inside)
//                normal.scalarMult(-1);
            Vector dirLight = new Vector(light.coords.point[0], light.coords.point[1], light.coords.point[2]);

            double d2_1 = 1;
            if (light.sides == -2){
                dirLight = light.coords.pointSub(intersection.toPoint());
                d2_1 = Math.pow(1 / light.coords.pointSub(intersection.toPoint()).magnitude(), 2);

            }

            normal.normalize();

            dirLight.normalize();

            Vector shadowNormal = normal.scalarMult(0.000001);
            shadowNormal = shadowNormal.vectorAdd(intersection);
            Ray shadowRay = new Ray(new Point(shadowNormal.vector[0], shadowNormal.vector[1], shadowNormal.vector[2]), dirLight);
            boolean shadow = false;
            double t = Double.MAX_VALUE;
            for (ImageObj obj : objects) {

                if (obj.sides == 0) {
                    double newT = sphereIntersection(shadowRay, obj, t);
                    if (light.sides == -2 && newT > 0 && newT < light.coords.pointSub(intersection.toPoint()).magnitude()) {
                        shadow = true;
                        break;
                    }else if(light.sides == -1 && newT > 0 && newT < Double.MAX_VALUE){
                        shadow = true;
                        break;
                    }
                }
            }
            if (shadow)
                continue;

            double d = normal.dot(dirLight);
            d = Math.max(d, 0);

            Vector colorVec = object.color.vectorMult(light.color.scalarMult(d2_1)).scalarMult(d);

            color = color.vectorAdd(colorVec);
        }
        return color.vector;
    }

    public static void convertSRGB(double[] color){
        for(int i=0; i<3; i++){
            if(color[i] <= 0.0031308){
                color[i] *= 12.92 * 255;
            }else{
                color[i] = (1.055 * Math.pow(color[i], (1/2.4)) - 0.055) * 255;
            }
        }
    }

    public static void checkRGB(double[] color){
        for(int i = 0; i < 3; i++){
            if(color[i] > 255){
                color[i] = 255;
            }else if(color[i] < 0){
                color[i] = 0;
            }
        }
    }

    public static void expose(double[] color){
        for(int i=0; i<3; i++){
            color[i] = 1 - (Math.exp(-1 * color[i] * exposure));
        }
    }

}