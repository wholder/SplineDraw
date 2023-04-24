import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts series of points defining a Catmull-Rom Spline into a Cubic Bezier
 *
 *    Based on: https://advancedweb.hu/2014/10/28/plotting_charts_with_svg/
 *    See also: http://schepers.cc/getting-to-the-point
 *    See also: https://pomax.github.io/bezierinfo/
 */
class CatmullRomToBezier {
  /**
   * Converts series of points defining a Catmull-Rom Spline into a Cubic Bezier
   * @param list List of Point2D.Float objects
   * @param closePath set true if generated Bezier path should be closed, else false
   * @return Path2D.Float object with generated Berzier Curve as path
   */
  static Path2D.Float convert (List<Point2D.Float> list, boolean closePath) {
    List<Point2D.Float> temp = new ArrayList<>(list);
    if (!closePath) {
      // If curve is not closed. duplicate last point to draw a curve through all points in the path
      temp.add(temp.get(temp.size() - 1));
    }
    Point2D.Float[] points = temp.toArray(new Point2D.Float[list.size()]);
    Path2D.Float path = new Path2D.Float();
    path.moveTo(points[0].x, points[0].y);
    int end = closePath ? points.length + 1 : points.length - 1;
    for (int ii = 0;  ii < end - 1; ii++) {
      Point2D.Float p0, p1, p2, p3;
      if (closePath) {
        int idx0 = Math.floorMod(ii - 1, points.length);
        int idx1 = Math.floorMod(idx0 + 1, points.length);
        int idx2 = Math.floorMod(idx1 + 1, points.length);
        int idx3 = Math.floorMod(idx2 + 1, points.length);
        p0 = new Point2D.Float(points[idx0].x, points[idx0].y);
        p1 = new Point2D.Float(points[idx1].x, points[idx1].y);
        p2 = new Point2D.Float(points[idx2].x, points[idx2].y);
        p3 = new Point2D.Float(points[idx3].x, points[idx3].y);
      } else {
        p0 = new Point2D.Float(points[Math.max(ii - 1, 0)].x, points[Math.max(ii - 1, 0)].y);
        p1 = new Point2D.Float(points[ii].x, points[ii].y);
        p2 = new Point2D.Float(points[ii + 1].x, points[ii + 1].y);
        p3 = new Point2D.Float(points[Math.min(ii + 2, points.length - 1)].x, points[Math.min(ii + 2, points.length - 1)].y);
      }
      // Catmull-Rom to Cubic Bezier conversion matrix
      //    0       1       0       0
      //  -1/6      1      1/6      0
      //    0      1/6      1     -1/6
      //    0       0       1       0
      Point2D.Float control1 = new Point2D.Float((-p0.x + 6f * p1.x + p2.x) / 6f, (-p0.y + 6f * p1.y + p2.y) / 6f);
      Point2D.Float control2 = new Point2D.Float(( p1.x + 6f * p2.x - p3.x) / 6f, ( p1.y + 6f * p2.y - p3.y) / 6f);
      Point2D.Float control3 = new Point2D.Float(p2.x,  p2.y);
      // Add curveTo segment to path
      path.curveTo(control1.x, control1.y, control2.x, control2.y, control3.x, control3.y);
    }
    if (closePath) {
      path.closePath();
    }
    return path;
  }
}
