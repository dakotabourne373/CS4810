import java.util.Map;

public class ImageObj {
    private String parent, name;
    private Matrix modelView, orientation;
    public Matrix inverseMV;
    private double[] origin, scale, position;
    public boolean computedMV;
    private boolean camera;

    public ImageObj(String name, String parent, boolean cam) {
        this.parent = parent;
        this.name = name;
        this.camera = cam;
        this.computedMV = false;
        this.origin = new double[]{0,0,0};
        this.scale = new double[]{1,1,1,1};
        this.position = new double[]{0,0,0};
        this.orientation = new Matrix(new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}});
    }

    private static double checkForVar(Map<String, Double> varMap, String a) {
        Double ret = varMap.get(a);
        if(ret == null){
            ret = Double.parseDouble(a);
        }
        return ret;
    }

    public void setOrigin(Map<String, Double> varMap, double x, double y, double z) {
        this.origin[0] = x;
        this.origin[1] = y;
        this.origin[2] = z;
    }

    public void setScale(Map<String, Double> varMap, double x, double y, double z, double w) {
        this.scale[0] = x;
        this.scale[1] = y;
        this.scale[2] = z;
        this.scale[3] = w;
    }
    public void setPosition(Map<String, Double> varMap, double x, double y, double z) {
        this.position[0] = x;
        this.position[1] = y;
        this.position[2] = z;
    }
    public void setOrientation(Map<String, Double> varMap, double x, double y, double z, double w) {
        double x2 = x*x;
        double y2 = y*y;
        double z2 = z*z;
        double w2 = w*w;

        double n = w2 + x2 + y2 + z2;

        double s = (n == 0 ? 0 : (2/n));

        this.orientation.matrix[0][0] = (1 - s*(y2 + z2));
        this.orientation.matrix[0][1] = s*(x*y - z*w);
        this.orientation.matrix[0][2] = s*(x*z + y*w);

        this.orientation.matrix[1][0] = s*(x*y + z*w);
        this.orientation.matrix[1][1] = (1 - s*(x2 + z2));
        this.orientation.matrix[1][2] = s*(y*z - x*w);

        this.orientation.matrix[2][0] = s*(x*z - y*w);
        this.orientation.matrix[2][1] = s*(y*z + x*w);
        this.orientation.matrix[2][2] = (1 - s*(y2 + x2));
    }

    public Matrix transposeMatrix(Matrix oldMat) {
        Matrix ret = new Matrix();
        for(int i=0; i<4; i++){
            for (int j=0; j<4; j++){
                ret.matrix[i][j] = oldMat.matrix[j][i];
            }
        }
        return ret;
    }

    public void generateInverseMV(Map<String, ImageObj> objectMap) {
        Matrix mv = new Matrix();

        if (!this.parent.equals("world") && !this.parent.equals("camera")) {
            mv.multByMatrix(objectMap.get(this.parent).inverseMV);
        } else if(this.parent.equals("camera") && objectMap.get("camera").inverseMV == null){
            objectMap.get("camera").generateMV(objectMap);
            mv.multByMatrix(objectMap.get("camera").inverseMV);
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
//            mv.multByMatrix(this.orientation);

        //inverse scale
        for (int i = 0; i < 4; i++) {
            mv.multValue(i, i, 1 / this.scale[i]);
        }

        //translate - inverse inverse origin
        for (int i = 0; i < 3; i++) {
            mv.addValue(i, 3, this.origin[i]);
        }

        if(this.camera)
            this.modelView = mv;
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

        if (!this.parent.equals("world") && !this.parent.equals("camera"))
            mv.multByMatrix(objectMap.get(this.parent).modelView);
        else if(this.parent.equals("camera") && objectMap.get("camera").getModelView() == null){
            objectMap.get("camera").generateMV(objectMap);
            mv.multByMatrix(objectMap.get("camera").getModelView());
        }

        this.modelView = mv;

        generateInverseMV(objectMap);

        this.computedMV = true;
    }

    public Matrix getModelView() {
        return this.modelView;
    }
}
