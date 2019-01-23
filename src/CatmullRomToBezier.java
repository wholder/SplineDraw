import java.awt.*;
import java.awt.geom.Path2D;

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
   * @param points Array of 2D points
   * @param closePath set true if generated Bezier path should be closed, else false
   * @return Path2D.Double object with generated Berzier Curve as path
   */
  static Path2D.Double convert (Point[] points, boolean closePath) {
    Path2D.Double path = new Path2D.Double();
    path.moveTo(points[0].x, points[0].y);
    int end = closePath ? points.length + 1 : points.length - 1;
    for (int ii = 0;  ii < end - 1; ii++) {
      Point p0, p1, p2, p3;
      if (closePath) {
        int idx0 = Math.floorMod(ii - 1, points.length);
        int idx1 = Math.floorMod(idx0 + 1, points.length);
        int idx2 = Math.floorMod(idx1 + 1, points.length);
        int idx3 = Math.floorMod(idx2 + 1, points.length);
        p0 = new Point(points[idx0].x, points[idx0].y);
        p1 = new Point(points[idx1].x, points[idx1].y);
        p2 = new Point(points[idx2].x, points[idx2].y);
        p3 = new Point(points[idx3].x, points[idx3].y);
      } else {
        p0 = new Point(points[Math.max(ii - 1, 0)].x,
                       points[Math.max(ii - 1, 0)].y);
        p1 = new Point(points[ii].x, points[ii].y);
        p2 = new Point(points[ii + 1].x, points[ii + 1].y);
        p3 = new Point(points[Math.min(ii + 2, points.length - 1)].x,
                       points[Math.min(ii + 2, points.length - 1)].y);
      }
      // Catmull-Rom to Cubic Bezier conversion matrix
      //    0       1       0       0
      //  -1/6      1      1/6      0
      //    0      1/6      1     -1/6
      //    0       0       1       0
      double x1 = (-p0.x + 6 * p1.x + p2.x) / 6;  // First control point
      double y1 = (-p0.y + 6 * p1.y + p2.y) / 6;
      double x2 = ( p1.x + 6 * p2.x - p3.x) / 6;  // Second control point
      double y2 = ( p1.y + 6 * p2.y - p3.y) / 6;
      double x3 = p2.x;                           // End point
      double y3 = p2.y;
      path.curveTo(x1, y1, x2, y2, x3, y3);
    }
    if (closePath) {
      path.closePath();
    }
    return path;
  }
}
