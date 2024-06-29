public class Point implements Comparable<Point>{
    double x, y, xRounded;
    int r,g,b,a, direction;

    public Point(double x, double y, int r, int g, int b, int a) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Point(int direction, double x, double y, int r, int g, int b){
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
        this.direction = direction;
    }

    public Point(double x, double y, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }
    public Point(double x, double y, double xRounded, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.xRounded = xRounded;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }

    public Point(double x, double y, String hex) {
        this.x = x;
        this.y = y;
        this.r = Integer.valueOf(hex.substring(1, 3), 16);
        this.g = Integer.valueOf(hex.substring(3, 5), 16);
        this.b = Integer.valueOf(hex.substring(5, 7), 16);
        if(hex.length() == 7)
            this.a = 255;
        else
            this.a = Integer.valueOf(hex.substring(7, 9), 16);
    }

    @Override
    public int compareTo(Point o) {
        if(this.y == o.y)
            return (int) (this.x - o.x);
        return (int) (this.y - o.y);
    }
}
