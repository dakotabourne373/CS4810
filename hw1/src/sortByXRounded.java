import java.util.Comparator;

class sortByXRounded implements Comparator<Point> {
    // Used for sorting in ascending order of
    // roll number
    public int compare(Point a, Point b)
    {
        if(a.y == b.y)
            return (int) (a.xRounded - b.xRounded);
        return (int) (a.y - b.y);
    }
}