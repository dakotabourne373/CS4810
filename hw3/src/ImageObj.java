public class ImageObj {
    int sides;
    Point coords;
    double[] info;
    Vector color;

    public ImageObj(int sides, Point x, double[] y, double[] z){
        this.sides = sides;
        this.color = new Vector(z[0], z[1], z[2]);
        if(sides == 0){
            this.coords = new Point(x.point[0], x.point[1], x.point[2]);
            this.info = new double[]{y[0]};
        }
        else{
            //lights
            this.coords = new Point(x.point[0], x.point[1], x.point[2]);
        }
    }

    public Vector generateNormal(Vector intersection){
        Vector normal = null;
        if(this.sides == 0){
            normal = intersection.vectorSub(coords);
        }
        return normal;
    }
}
