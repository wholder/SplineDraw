
  //	SplineMovement testbed
  //  Ref: http://www.mvps.org/directx/articles/catmull/
  //  See also: http://www.cemyuksel.com/research/catmullrom_param/catmullrom.pdf   (other types)

  // Convert Catmull-Rom to Bezier: https://gist.github.com/njvack/6925609
  //  And: https://advancedweb.hu/2014/10/28/plotting_charts_with_svg/

import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

import javax.swing.*;

  public class SplineMovement extends JFrame {
  private static final int      STEPS = 16;       // Interpolated points between recorded points
  private static final int      DIST = 64;        // Distance mouse must move to record another point
  private List<Point>           points = new ArrayList<>();
  private int                   lastX, lastY;
  private Matrix                M;                // Transformation matrix
  private boolean               drawBezier;

  public static class Matrix implements Cloneable, Serializable {
    private int     rows;
    private int     cols;
    private int[][] mtrx;

    Matrix (int i, int j) {
      rows = i;
      cols = j;
      mtrx = new int[rows][cols];
    }

    Matrix (int i, int j, int[][] mtrx) {
      rows = i;
      cols = j;
      this.mtrx = mtrx;
    }

    public Object clone () {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }

    void mul (Matrix matrix1) {
      if (cols != matrix1.rows) {
        return;
      }
      Matrix matrix2 = new Matrix(rows, matrix1.cols);
      for (int i = 0; i < matrix2.rows; i++) {
        for (int j = 0; j < matrix2.cols; j++) {
          for (int k = 0; k < cols; k++) {
            matrix2.mtrx[i][j] += mtrx[i][k] * matrix1.mtrx[k][j];
          }
        }
      }
      rows = matrix2.rows;
      cols = matrix2.cols;
      mtrx = matrix2.mtrx;
    }
  }

  class Closer extends WindowAdapter {
    public void windowClosing (WindowEvent ev) {
      System.exit(0);
    }
  }

  private SplineMovement () {
    super("Catmull-Rom SplineMovement");
    setBackground(Color.white);
    addWindowListener(new Closer());
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed (MouseEvent me) {
        super.mousePressed(me);
        drawBezier = false;
        int x = me.getX();
        int y = me.getY();
        lastX = x;
        lastY = y;
        points.clear();
        points.add(new Point(x, y));
        repaint();
      }

      @Override
      public void mouseReleased (MouseEvent me) {
        super.mouseReleased(me);
        drawBezier = true;
        repaint();
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged (MouseEvent me) {
        super.mouseDragged(me);
        int x = me.getX();
        int y = me.getY();
        if (distance(x, y, lastX, lastY) > DIST) {
          lastX = x;
          lastY = y;
          points.add(new Point(x, y));
          repaint();
        }
      }
    });
    setSize(800, 900);
    setLocation(20, 20);
    int[][] transform = {
        { -1,  3, -3,  1},		// transformation matrix
        {  2, -5,  4, -1},
        { -1,  0,  1,  0},
        {  0,  2,  0,  0}};
    M = new Matrix(4, 4, transform);
    setVisible(true);
  }

  private void drawCatmullRomCurve (Graphics g, List<Point> sPoints) {
    if (sPoints.size() > 1) {
      // Draw Classic Catmull-Rom Spline
      // Note: can't draw line from first point to second because there's no control point before first point
      Matrix G = new Matrix(4, 2);
      List<Point> tmp = new ArrayList<>(sPoints);
      Point last = tmp.get(tmp.size() - 1);
      tmp.add(new Point(last.x, last.y));
      Point[] ctrlPoint = tmp.toArray(new Point[0]);
      for (int cnt2 = 0; cnt2 < (ctrlPoint.length - 3); cnt2++) {
        Matrix C = (Matrix) M.clone();
        for (int cnt = 0; cnt < 4; cnt++) {
          G.mtrx[cnt][0] = ctrlPoint[cnt + cnt2].x;
          G.mtrx[cnt][1] = ctrlPoint[cnt + cnt2].y;
        }
        C.mul(G);
        double delta = 1.0 / (double) STEPS;
        double t = 0.0;
        int oldX = 0, oldY = 0;
        for (int cnt = 0; cnt < STEPS; cnt++) {
          t += delta;
          double t2 = t * t;
          double t3 = t2 * t;
          int x = (int) (C.mtrx[0][0] * t3 + C.mtrx[1][0] * t2 + C.mtrx[2][0] * t + C.mtrx[3][0]) >> 1;
          int y = (int) (C.mtrx[0][1] * t3 + C.mtrx[1][1] * t2 + C.mtrx[2][1] * t + C.mtrx[3][1]) >> 1;
          if (false) {
            g.fillRect(x, y, 1, 1);
          } else {
            if (cnt > 0) {
              g.drawLine(x, y, oldX, oldY);
            }
            oldX = x;
            oldY = y;
          }
        }
      }
    }
  }

  void drawBezierPoints (Graphics g, Path2D.Double path) {
    // Draw Bezier control and end points
    PathIterator pi = path.getPathIterator(new AffineTransform());
    while (!pi.isDone()) {
      float[] coords = new float[6];      // p1.x, p1.y, p2.x, p2.y, p3.x, p3.y
      int type = pi.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_QUADTO:   // 2
          // Write 3 point, quadratic bezier curve from previous point to new point using one control point
          g.setColor(Color.red);
          g.fillOval((int) coords[0] - 1, (int) coords[1] - 2, 5, 5);
          g.setColor(Color.blue);
          g.fillOval((int) coords[2] - 1, (int) coords[3] - 2, 5, 5);
          break;
        case PathIterator.SEG_CUBICTO:  // 3
          // Write 4 point, cubic bezier curve from previous point to new point using two control points
          g.setColor(Color.red);
          g.fillOval((int) coords[0] - 1, (int) coords[1] - 2, 5, 5);
          g.fillOval((int) coords[2] - 1, (int) coords[3] - 2, 5, 5);
          g.setColor(Color.blue);
          g.fillOval((int) coords[4] - 1, (int) coords[5] - 2, 5, 5);
          break;
      }
      pi.next();
    }
  }

  public void paint (Graphics gg) {
    Image offscreenImage = createImage(getSize().width, getSize().height);
    Graphics offscr = offscreenImage.getGraphics();
    offscr.setColor(Color.white);
    offscr.fillRect(0, 0, getSize().width, getSize().height);
    offscr.setColor(Color.gray);
    if (drawBezier) {
      Path2D.Double path = CatmullRomToBezier.convert(points.toArray(new Point[points.size()]), true);
      Graphics2D g2 = (Graphics2D) offscr.create();
      g2.draw(path);
      drawBezierPoints(offscr, path);
    } else {
      drawCatmullRomCurve(offscr, points);
      // Draw control points used to define Catmull-Rom Spline
      offscr.setColor(Color.blue);
      for (Point pp : points) {
        int x = pp.x;
        int y = pp.y;
        offscr.fillRect(x - 2, y - 2, 5, 5);
      }
    }
    gg.drawImage(offscreenImage, 0, 0, this);
  }

  private int distance (int x1, int y1, int x2, int y2) {
    int dx = Math.abs(x1 - x2);
    int dy = Math.abs(y1 - y2);
    return (int) Math.sqrt((double) ((dx * dx) + (dy * dy)));
  }

  public static void main (String[] args) throws Exception {
    SwingUtilities.invokeLater(SplineMovement::new);
  }
}
