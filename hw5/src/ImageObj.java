import java.awt.*;
import java.util.Map;

public class ImageObj {
    private String parent, name;
    public String tracking, trackingSec, axis;
    private Matrix modelView, orientation, boneMat;
    public Matrix inverseMV;
    private double[] origin, scale, position;
    public double[] tip;
    public boolean computedMV, willScale, willStretch;
    public boolean bone, computedBone;

    public ImageObj(String name, String parent, boolean cam) {
        this.parent = parent;
        this.name = name;
        this.computedMV = false;
        this.bone = false;
        this.willScale = false;
        this.willStretch = false;
        this.trackingSec = "";
        this.axis = "";
        this.origin = new double[]{0,0,0};
        this.scale = new double[]{1,1,1,1};
        this.position = new double[]{0,0,0};
        this.orientation = new Matrix(new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}});
    }

    public void setBone(double d) {
        this.bone = true;
        this.computedBone = false;
        this.tip = new double[]{0, 0, d};
    }

    public void setPosition(double x, double y, double z) {
        this.position[0] = x;
        this.position[1] = y;
        this.position[2] = z;
    }
    public void setOrientation(double x, double y, double z, double w) {
        double x2 = x*x;
        double y2 = y*y;
        double z2 = z*z;
        double w2 = w*w;

        double n = w2 + x2 + y2 + z2;

        double s = (n == 0 ? 0 : (2/n));

        if(n == 1 && s == 2){
            this.orientation.matrix[0][0] = w2 +x2 - y2 - z2;
            this.orientation.matrix[0][1] = 2 * (x * y - z * w);
            this.orientation.matrix[0][2] = 2 * (x * z + y * w);

            this.orientation.matrix[1][0] = 2 * (x * y + z * w);
            this.orientation.matrix[1][1] = w2 - x2 + y2 - z2;
            this.orientation.matrix[1][2] = 2 * (y * z - x * w);

            this.orientation.matrix[2][0] = 2 * (x * z - y * w);
            this.orientation.matrix[2][1] = 2 * (y * z + x * w);
            this.orientation.matrix[2][2] = w2 - x2 - y2 + z2;

        }else {
            this.orientation.matrix[0][0] = (1 - s * (y2 + z2));
            this.orientation.matrix[0][1] = s * (x * y - z * w);
            this.orientation.matrix[0][2] = s * (x * z + y * w);

            this.orientation.matrix[1][0] = s * (x * y + z * w);
            this.orientation.matrix[1][1] = (1 - s * (x2 + z2));
            this.orientation.matrix[1][2] = s * (y * z - x * w);

            this.orientation.matrix[2][0] = s * (x * z - y * w);
            this.orientation.matrix[2][1] = s * (y * z + x * w);
            this.orientation.matrix[2][2] = (1 - s * (y2 + x2));
        }
    }

    public Matrix transposeMatrix(Matrix oldMat) {
        Matrix ret = new Matrix();
        for(int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                ret.matrix[i][j] = oldMat.matrix[j][i];
            }
        }
        return ret;
    }

    public Matrix trackMatrix(Map<String, ImageObj> objectMap) {

        ImageObj trackedObj = objectMap.get(this.tracking);
        Point oldP = new Point(trackedObj.origin[0], trackedObj.origin[1], trackedObj.origin[2], 1, new double[]{0, 0,0,0});
// convert oldP to world by mult mv, then convert t4 by mult oldP by t4's inv
        Point p;

        oldP.multByMat(trackedObj.modelView);
        generateInverseMV(objectMap);
        oldP.multByMat(this.inverseMV);

        p = new Point(new double[]{oldP.vector[0], oldP.vector[1], oldP.vector[2]});

        Point a = new Point(new double[]{this.tip[0], this.tip[1], this.tip[2]});

        p.normalize();
        a.normalize();
        Point rP = a.crossProduct(p.vector);
        double c = a.dotProduct(p);
        double s = rP.magnitude();
        Point r = rP.divideByScalar(s);

        Matrix matr = new Matrix();
        double c_1 = (1 - c);
        double x = r.vector[0];
        double y = r.vector[1];
        double z = r.vector[2];

        Matrix scal = new Matrix();
        if(willScale){
            double[] newZ = new double[]{trackedObj.position[0], trackedObj.position[1], trackedObj.position[2]};

            Point point = new Point(new double[]{newZ[0]-this.position[0], newZ[1]-this.position[1], newZ[2]-this.position[2]});

            double scale = point.magnitude() / Math.abs(this.tip[2]);
            scal.multValue(2, 2, scale);
            if(willStretch){
                scal.multValue(0, 0, 1/Math.sqrt(scale));
                scal.multValue(1, 1, 1/Math.sqrt(scale));
            }
        }

        matr.matrix[0][0] = ((x * x) * c_1) + c;
        matr.matrix[1][0] = ((x * y) * c_1) + (z * s);
        matr.matrix[2][0] = ((x * z) * c_1) - (y * s);

        matr.matrix[0][1] = ((x * y) * c_1) - (z * s);
        matr.matrix[1][1] = ((y * y) * c_1) + c;
        matr.matrix[2][1] = ((y * z) * c_1) + (x * s);

        matr.matrix[0][2] = ((x * z) * c_1) + (y * s);
        matr.matrix[1][2] = ((y * z) * c_1) - (x * s);
        matr.matrix[2][2] = ((z * z) * c_1) + c;

        this.computedBone = true;
        scal.multByMatrix(matr);

        this.boneMat = scal;
        return scal;

    }

    public Matrix pointAndRoll(Map<String, ImageObj> objectMap) {
        ImageObj primaryObj = objectMap.get(this.tracking);
        ImageObj secondaryObj = objectMap.get(this.trackingSec);

        Point primary = new Point(primaryObj.origin[0], primaryObj.origin[1], primaryObj.origin[2], 1, new double[]{0, 0, 0, 0});
        Point secondary = new Point(secondaryObj.origin[0], secondaryObj.origin[1], secondaryObj.origin[2], 1, new double[]{0, 0, 0, 0});

        generateInverseMV(objectMap);

        primary.multByMat(primaryObj.modelView);
        primary.multByMat(this.inverseMV);

        secondary.multByMat(secondaryObj.modelView);
        secondary.multByMat(this.inverseMV);

        primary.normalize();
        Point res = primary.crossProduct(secondary.vector);
        Point x, y;
        if(this.axis.equals("+x")) {
            x = res.crossProduct(primary.vector);
            x.normalize();
            y = primary.crossProduct(x.vector);
        }else if(this.axis.equals("-x")) {
            x = res.crossProduct(primary.vector);
            x.normalize();
            for(int i=0; i<3; i++)
                x.vector[i] *= -1;
            y = primary.crossProduct(x.vector);
        }else if(this.axis.equals("+y")) {
            y = res.crossProduct(primary.vector);
            y.normalize();
            x = y.crossProduct(primary.vector);
        }else {
            y = res.crossProduct(primary.vector);
            y.normalize();
            for(int i=0; i<3; i++)
                y.vector[i] *= -1;
            x = y.crossProduct(primary.vector);
        }

        Matrix mat = new Matrix();
        for(int i=0; i < 3; i++){
            mat.matrix[i][0] = x.vector[i];
            mat.matrix[i][1] = y.vector[i];
            mat.matrix[i][2] = primary.vector[i];
        }
        this.boneMat = mat;
        this.computedBone = true;
        return mat;
    }

    public void generateInverseMV(Map<String, ImageObj> objectMap) {
        Matrix mv = new Matrix();

        if (!this.parent.equals("world")) {
            ImageObj parObj = objectMap.get(this.parent);
            mv.multByMatrix(objectMap.get(this.parent).inverseMV);
            if(parObj.bone)
                mv.multByMatrix(transposeMatrix(parObj.boneMat));
        }

        //translate - inverse origin
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, (-1) * this.origin[i]);
        }

        //translate - inverse position
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, (-1) * this.position[i]);
        }

        //rotate - inverse orientation
        mv.multByMatrix(transposeMatrix(this.orientation));

        //inverse scale
        for (int i = 0; i < 4; i++) {
            mv.multValue(i, i, 1 / this.scale[i]);
        }

        //translate - inverse inverse origin
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, this.origin[i]);
        }

        this.inverseMV = mv;
    }

    public void generateMV(Map<String, ImageObj> objectMap) {
        Matrix mv = new Matrix();

        //translate - inverse origin
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, (-1) * this.origin[i]);
        }

        //scale
        for (int i = 0; i < 4; i++) {
            mv.multValue(i, i, this.scale[i]);
        }


        //rotate - orientation
        mv.multByMatrix(this.orientation);


        //translate - position
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, this.position[i]);
        }

        //translate - origin
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, this.origin[i]);
        }

        if (!this.parent.equals("world")) {
            ImageObj parObj = objectMap.get(this.parent);
            if(parObj.bone)
                mv.multByMatrix(parObj.boneMat);
            mv.multByMatrix(parObj.modelView);
        }

        this.modelView = mv;

        generateInverseMV(objectMap);

        this.computedMV = true;
    }

    public Matrix getModelView() {
        return this.modelView;
    }

    public Matrix getBoneMat() { return this.boneMat; }

}
