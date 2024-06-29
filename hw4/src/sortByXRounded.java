import java.util.Comparator;

class sortByXRounded implements Comparator<Point> {
    public int compare(Point a, Point b)
    {
        if(a.vector[1] == b.vector[1])
            return (int) (a.xRounded - b.xRounded);
        return (int) (a.vector[1] - b.vector[1]);
    }
}