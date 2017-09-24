import java.util.ArrayList;
import java.util.List;

  // Adapted from: http://stackoverflow.com/questions/9489736/catmull-rom-curve-with-no-cusps-and-no-self-intersections/23980479

public class CatmullRom {
  public enum SplineType {Uniform, Centripetal, };

  static class Coord {
    double x, y, z;

    Coord (double X, double Y, double Z) {
      this.x = X;
      this.y = Y;
      this.z = Z;
    }

    Coord (double X, double Y) {
      this(X, Y, 0);
    }

    Coord copy () {
      return new Coord(x, y, z);
    }

    boolean intersects2D (Coord p2) {
      return distance(x, y, p2.x, p2.y) <= 1.0;
    }

    private double distance (double x1, double y1, double x2, double y2) {
      double dx = Math.abs(x1 - x2);
      double dy = Math.abs(y1 - y2);
      return Math.sqrt((dx * dx) + (dy * dy));
    }
  }

  /**
   * This method will calculate the Catmull-Rom interpolation curve, returning
   * it as a list of Coord coordinate objects.  This method in particular
   * adds the first and last control points which are not visible, but required
   * for calculating the spline.
   *
   * @param coordinates The list of original straight line points to calculate
   * an interpolation from.
   * @param pointsPerSegment The integer number of equally spaced points to
   * return along each curve.  The actual distance between each
   * point will depend on the spacing between the control points.
   * @return The list of interpolated coordinates.
   * @param curveType Chordal (stiff), Uniform(floppy), or Centripetal(medium)
   * @throws Exception if pointsPerSegment is less than 2.
   */
  public static List<Coord> interpolate (List<Coord> coordinates, int pointsPerSegment, SplineType curveType) throws Exception {
    List<Coord> vertices = new ArrayList<>();
    for (Coord c : coordinates) {
      vertices.add(c.copy());
    }
    if (pointsPerSegment < 2) {
      throw new Exception("The pointsPerSegment parameter must be greater than 2, since 2 points is just the linear segment.");
    }
    // Cannot interpolate curves given only two points.  Two points
    // is best represented as a simple line segment.
    if (vertices.size() < 3) {
      return vertices;
    }
    // Test whether the shape is open or closed by checking to see if
    // the first point intersects with the last point.  M and z are ignored.
    boolean isClosed = vertices.get(0).intersects2D(vertices.get(vertices.size() - 1));
    if (isClosed) {
      // Use the second and second from last points as control points.
      // get the second point.
      Coord p2 = vertices.get(1).copy();
      // get the point before the last point
      Coord pn1 = vertices.get(vertices.size() - 2).copy();

      // insert the second from the last point as the first point in the list
      // because when the shape is closed it keeps wrapping around to
      // the second point.
      vertices.add(0, pn1);
      // add the second point to the end.
      vertices.add(p2);
    } else {
      // The shape is open, so use control points that simply extend
      // the first and last segments
      // Get the change in x and y between the first and second coordinates.
      double dx = vertices.get(1).x - vertices.get(0).x;
      double dy = vertices.get(1).y - vertices.get(0).y;
      // Then using the change, extrapolate backwards to find a control point.
      double x1 = vertices.get(0).x - dx;
      double y1 = vertices.get(0).y - dy;
      // Actaully create the start point from the extrapolated values.
      Coord start = new Coord(x1, y1, vertices.get(0).z);
      // Repeat for the end control point.
      int n = vertices.size() - 1;
      dx = vertices.get(n).x - vertices.get(n - 1).x;
      dy = vertices.get(n).y - vertices.get(n - 1).y;
      double xn = vertices.get(n).x + dx;
      double yn = vertices.get(n).y + dy;
      Coord end = new Coord(xn, yn);
      // insert the start control point at the start of the vertices list.
      vertices.add(0, start);
      // append the end control ponit to the end of the vertices list.
      vertices.add(end);
    }
    // Dimension a result list of coordinates.
    List<Coord> result = new ArrayList<>();
    // When looping, remember that each cycle requires 4 points, starting
    // with ii and ending with ii+3.  So we don't loop through all the points.
    for (int ii = 0; ii < vertices.size() - 3; ii++) {
      // Actually calculate the Catmull-Rom curve for one segment.
      List<Coord> points = interpolate(vertices, ii, pointsPerSegment, curveType);
      // Since the middle points are added twice, once for each bordering
      // segment, we only add the 0 index result point for the first
      // segment.  Otherwise we will have duplicate points.
      if (result.size() > 0) {
        points.remove(0);
      }
      // Add the coordinates for the segment to the result list.
      result.addAll(points);
    }
    return result;
  }

  /**
   * Given a list of control points, this will create a list of pointsPerSegment
   * points spaced uniformly along the resulting Catmull-Rom curve.
   *
   * @param points The list of control points, leading and ending with a
   * coordinate that is only used for controling the spline and is not visualized.
   * @param index The index of control point p0, where p0, p1, p2, and p3 are
   * used in order to create a curve between p1 and p2.
   * @param pointsPerSegment The total number of uniformly spaced interpolated
   * points to calculate for each segment. The larger this number, the
   * smoother the resulting curve.
   * @param curveType Clarifies whether the curve should use uniform, chordal
   * or centripetal curve types. Uniform can produce loops, chordal can
   * produce large distortions from the original lines, and centripetal is an
   * optimal balance without spaces.
   * @return the list of coordinates that define the CatmullRom curve
   * between the points defined by index+1 and index+2.
   */
  public static List<Coord> interpolate (List<Coord> points, int index, int pointsPerSegment, SplineType curveType) {
    List<Coord> result = new ArrayList<>();
    double[] x = new double[4];
    double[] y = new double[4];
    double[] time = new double[4];
    for (int ii = 0; ii < 4; ii++) {
      x[ii] = points.get(index + ii).x;
      y[ii] = points.get(index + ii).y;
      time[ii] = ii;
    }
    double tstart = 1;
    double tend = 2;
    if (!curveType.equals(SplineType.Uniform)) {
      double total = 0;
      for (int ii = 1; ii < 4; ii++) {
        double dx = x[ii] - x[ii - 1];
        double dy = y[ii] - y[ii - 1];
        if (curveType.equals(SplineType.Centripetal)) {
          total += Math.pow(dx * dx + dy * dy, .25);
        } else {
          total += Math.pow(dx * dx + dy * dy, .5);
        }
        time[ii] = total;
      }
      tstart = time[1];
      tend = time[2];
    }
    double z1 = 0.0;
    double z2 = 0.0;
    if (!Double.isNaN(points.get(index + 1).z)) {
      z1 = points.get(index + 1).z;
    }
    if (!Double.isNaN(points.get(index + 2).z)) {
      z2 = points.get(index + 2).z;
    }
    double dz = z2 - z1;
    int segments = pointsPerSegment - 1;
    result.add(points.get(index + 1));
    for (int ii = 1; ii < segments; ii++) {
      double xi = interpolate(x, time, tstart + (ii * (tend - tstart)) / segments);
      double yi = interpolate(y, time, tstart + (ii * (tend - tstart)) / segments);
      double zi = z1 + (dz * ii) / segments;
      result.add(new Coord(xi, yi, zi));
    }
    result.add(points.get(index + 2));
    return result;
  }

  /**
   * Unlike the other implementation here, which uses the default "uniform"
   * treatment of t, this computation is used to calculate the same values but
   * introduces the ability to "parameterize" the t values used in the
   * calculation. This is based on Figure 3 from
   * http://www.cemyuksel.com/research/catmullrom_param/catmullrom.pdf
   *
   * @param p An array of double values of length 4, where interpolation
   * occurs from p1 to p2.
   * @param time An array of time measures of length 4, corresponding to each
   * p value.
   * @param t the actual interpolation ratio from 0 to 1 representing the
   * position between p1 and p2 to interpolate the value.
   * @return
   */
  public static double interpolate (double[] p, double[] time, double t) {
    double L01 = p[0] * (time[1] - t) / (time[1] - time[0]) + p[1] * (t - time[0]) / (time[1] - time[0]);
    double L12 = p[1] * (time[2] - t) / (time[2] - time[1]) + p[2] * (t - time[1]) / (time[2] - time[1]);
    double L23 = p[2] * (time[3] - t) / (time[3] - time[2]) + p[3] * (t - time[2]) / (time[3] - time[2]);
    double L012 = L01 * (time[2] - t) / (time[2] - time[0]) + L12 * (t - time[0]) / (time[2] - time[0]);
    double L123 = L12 * (time[3] - t) / (time[3] - time[1]) + L23 * (t - time[1]) / (time[3] - time[1]);
    double C12 = L012 * (time[2] - t) / (time[2] - time[1]) + L123 * (t - time[1]) / (time[2] - time[1]);
    return C12;
  }
}
