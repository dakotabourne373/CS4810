public class Matrix {
    double[][] matrix;

    public Matrix(double[][] matrix) {
        this.matrix = matrix;
    }

    public Matrix(){
        this.matrix = new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
    }

    public double getValue(int x, int y){
        return this.matrix[x][y];
    }

    public void addValue(int x, int y, double v) {
        this.matrix[x][y] += v;
    }

    public void multValue(int x, int y, double v) {
        this.matrix[x][y] *= v;
    }

    public void multByMatrix(Matrix x){
        double[][] answer = new double[4][4];
        for(int i=0; i<4; i++){
            for (int j=0; j<4; j++){
                for(int k=0; k<4; k++){
                    answer[i][j] += this.matrix[i][k] * x.getValue(k, j);
                }
            }
        }
        this.matrix = answer;
    }

    public void multByPoint(double[] x){
        double[][] answer = new double[4][4];
        for(int i=0; i<4; i++){
            for(int j=0; j<4; j++){
                answer[i][j] += matrix[i][j] * x[j];
            }
        }
        this.matrix = answer;
    }
}
