public class Point implements  Comparable<Point>{
    double[] vector;
    int xRounded;
    double[] color;

    public Point(double x, double y, double z, double w, double[] color) {
        this.vector = new double[]{x, y, z, w};
        this.color = new double[]{color[0], color[1], color[2], 255};
        this.xRounded = (int) Math.ceil(x);
    }

    public Point(double[] vector, double[] color){
        this.vector = new double[]{vector[0], vector[1], vector[2], vector[3]};
        this.color = new double[]{color[0], color[1], color[2], 255};
        this.xRounded = (int) Math.ceil(vector[0]);
    }

    public Point(double[] vector) {
        this.vector = new double[]{vector[0], vector[1], vector[2], 0};
    }

    public void divideByW(){
        double w = vector[3];
        vector[0] /= w;
        vector[1] /= w;
        vector[2] /= w;
        vector[3] /= (w * w);
        color[0] /= w;
        color[1] /= w;
        color[2] /= w;
        color[3] /= w;
    }

    public double dotProduct(Point p) {
        double ret = this.vector[0] * p.vector[0] + this.vector[1] * p.vector[1] + this.vector[2] * p.vector[2];
        return ret;
    }

    public void applyViewport(int width, int height){
        double x = vector[0];
        double y = vector[1];
        x += 1;
        y += 1;
        vector[0] = x * ((double) width/2);
        vector[1] = y * ((double) height/2);
    }

    public void multByMat(Matrix mat){
        double[] newVec = new double[]{vector[0], vector[1], vector[2], vector[3]};
        for(int i=0; i<4; i++){
            newVec[i] = 0;
            for(int j=0; j<4; j++){
                newVec[i] += mat.getValue(i, j) * vector[j];
            }
        }
        this.vector = newVec;
    }

    public Point crossProduct(double[] o) {
        double[] helper = new double[3];
        helper[0] = this.vector[1] * o[2] - this.vector[2] * o[1];
        helper[1] = this.vector[2] * o[0] - this.vector[0] * o[2];
        helper[2] = this.vector[0] * o[1] - this.vector[1] * o[0];
        return new Point(helper);
    }

    public double magnitude() {
        double x = this.vector[0];
        double y = this.vector[1];
        double z = this.vector[2];
        return Math.sqrt((x*x) + (y*y) + (z*z));
    }

    public Point divideByScalar(double s) {
        return new Point(new double[]{this.vector[0] / s, this.vector[1] / s, this.vector[2] / s});
    }

    public void normalize(){
        double x = this.vector[0];
        double y = this.vector[1];
        double z = this.vector[2];
        double mag = Math.sqrt((x*x) + (y*y) + (z*z));
        this.vector[0] = (x / mag);
        this.vector[1] = (y / mag);
        this.vector[2] = (z / mag);
    }

    @Override
    public int compareTo(Point o) {
        if(this.vector[1] == o.vector[1])
            return (int) (this.vector[0] - o.vector[0]);
        return (int) (this.vector[1] - o.vector[1]);
    }
}
