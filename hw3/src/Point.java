public class Point implements  Comparable<Point>{
    double[] point;

    public Point(double x, double y, double z){
        this.point = new double[]{x, y, z};
    }

    public Vector pointSub(Point x){
        Vector ret = new Vector(this.point[0], this.point[1], this.point[2]);
        for(int i=0; i < 3; i++) {
            ret.vector[i] -= x.point[i];
        }
        return ret;
    }

    public Point pointAdd(Point x){
        Point ret = new Point(this.point[0], this.point[1], this.point[2]);
        for(int i=0; i < 3; i++) {
            ret.point[i] -= x.point[i];
        }
        return ret;
    }

    @Override
    public int compareTo(Point o) {
        if(this.point[1] == o.point[1])
            return (int) (this.point[0] - o.point[0]);
        return (int) (this.point[1] - o.point[1]);
    }
}
