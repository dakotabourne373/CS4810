public class Vector {
    double[] vector;

    public Vector(double x, double y, double z){
        this.vector = new double[]{x, y, z};
    }

    public Vector(double[] x){
        this.vector = new double[]{x[0], x[1], x[2]};
    }

    public Vector scalarMult(double x){
        Vector ret = new Vector(this.vector);
        for(int i=0; i < vector.length; i++){
            ret.vector[i] *= x;
        }
        return ret;
    }

    public Vector vectorAdd(Vector x){
        Vector ret = new Vector(this.vector);
        for(int i=0; i < vector.length; i++){
            ret.vector[i] += x.vector[i];
        }
        return ret;
    }

    public Vector vectorAdd(Point x){
        Vector ret = new Vector(this.vector);
        for(int i=0; i < vector.length; i++){
            ret.vector[i] += x.point[i];
        }
        return ret;
    }
    public Vector vectorSub(Point x){
        Vector ret = new Vector(this.vector);
        for(int i=0; i < vector.length; i++){
            ret.vector[i] -= x.point[i];
        }
        return ret;
    }

    public Vector vectorMult(Vector x){
        Vector ret = new Vector(this.vector);
        for(int i=0; i < vector.length; i++){
            ret.vector[i] *= x.vector[i];
        }
        return ret;
    }

    public Point toPoint(){
        return new Point(vector[0], vector[1], vector[2]);
    }

    public double magnitude(){
        double ret = 0;
        for(int i=0; i<vector.length; i++){
            ret += Math.pow(this.vector[i], 2);
        }
        return Math.sqrt(ret);
    }

    public double dot(Vector x){
        double ret = 0;
        for(int i=0; i < vector.length; i++){
            ret += this.vector[i] * x.vector[i];
        }
        return ret;
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
}
