import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

class ImageGenerator {
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
                String[] line = in.nextLine().strip().split("\s");
                String cmd = line[0];

                int x = 0, y = 0, r = 255, g = 255, b = 255;
                int a = 255;

                switch (cmd) {
                    case "xy" -> {
                        x = Integer.parseInt(line[1]);
                        y = Integer.parseInt(line[2]);
                    }
                    case "xyc" -> {
                        x = Integer.parseInt(line[1]);
                        y = Integer.parseInt(line[2]);
                        String hex = line[3];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                    }
                    case "xyrgb" -> {
                        x = Integer.parseInt(line[1]);
                        y = Integer.parseInt(line[2]);
                        r = Integer.parseInt(line[3]);
                        g = Integer.parseInt(line[4]);
                        b = Integer.parseInt(line[5]);
                    }
                }

                int[] arr = new int[]{r, g, b, a};
                raster.setPixel(x, y, arr);
                ImageIO.write(image, "png", new File(filename));
            }
        }else if (mainCMD.equals("pngs")){
            int frame = 0;
            while(in.hasNextLine()) {
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                WritableRaster raster = image.getRaster();

                String[] line = in.nextLine().strip().split("\s+");

                int x = 0, y = 0, r = 255, g = 255, b = 255;
                int a = 255;

                String cmd = line[0];
                switch (cmd) {
                    case "frame" -> {
                        frame = Integer.parseInt(line[1]);
                    }
                    case "xy" -> {
                        x = Integer.parseInt(line[1]);
                        y = Integer.parseInt(line[2]);
                    }
                    case "xyc" -> {
                        x = Integer.parseInt(line[1]);
                        y = Integer.parseInt(line[2]);
                        String hex = line[3];
                        r = Integer.valueOf(hex.substring(1, 3), 16);
                        g = Integer.valueOf(hex.substring(3, 5), 16);
                        b = Integer.valueOf(hex.substring(5, 7), 16);
                    }
                    case "xyrgb" -> {
                        x = Integer.parseInt(line[1]);
                        y = Integer.parseInt(line[2]);
                        r = Integer.parseInt(line[3]);
                        g = Integer.parseInt(line[4]);
                        b = Integer.parseInt(line[5]);
                    }
                }

                int[] arr = new int[]{r, g, b, a};
                raster.setPixel(x, y, arr);
                ImageIO.write(image, "png", new File(filename + String.format("%03d", frame) + ".png"));
            }
        }
    }
}