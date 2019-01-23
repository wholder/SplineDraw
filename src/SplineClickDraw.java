
  //	SplineClickDraw testbed
  //  Ref: http://www.mvps.org/directx/articles/catmull/
  //  See also: http://www.cemyuksel.com/research/catmullrom_param/catmullrom.pdf   (other types)

  // Convert Catmull-Rom to Bezier: https://gist.github.com/njvack/6925609
  //  And: https://advancedweb.hu/2014/10/28/plotting_charts_with_svg/

  import javax.swing.*;
  import java.awt.*;
  import java.awt.event.*;
  import java.awt.geom.Path2D;
  import java.util.ArrayList;
  import java.util.List;

  public class SplineClickDraw extends JFrame {
  private List<Point>           points = new ArrayList<>();
  private Point                 dragPoint;
  private boolean               closePath, dirty;
  private Path2D.Double         path;

  class Closer extends WindowAdapter {
    public void windowClosing (WindowEvent ev) {
      System.exit(0);
    }
  }

  private SplineClickDraw () {
    super("Catmull-Rom SplineClickDraw");
    setBackground(Color.white);
    addWindowListener(new Closer());
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed (MouseEvent me) {
        super.mousePressed(me);
        int x = me.getX();
        int y = me.getY();
        for (Point cp : points) {
          double dx = cp.x - x;
          double dy = cp.y - y;
          double dist = Math.sqrt(dx * dx + dy * dy);
          if (dist < 5) {
            if (!closePath && cp == points.get(0)) {
              closePath = true;
              dirty = true;
            } else {
              dragPoint = cp;
            }
            return;
          }
        }
        if (me.isShiftDown()) {
          points.clear();
          closePath = false;
          path = null;
        } else if (!closePath) {
          points.add(new Point(x, y));
          if (points.size() > 1) {
            dirty = true;
          }
        }
        repaint();
      }

      @Override
      public void mouseReleased (MouseEvent me) {
        super.mouseReleased(me);
        dragPoint = null;
        repaint();
      }
    });
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged (MouseEvent me) {
        super.mouseDragged(me);
        int x = me.getX();
        int y = me.getY();
        if (dragPoint != null) {
          dragPoint.x = x;
          dragPoint.y = y;
          dirty = true;
          repaint();
        }
      }
    });
    setSize(800, 900);
    setLocation(20, 20);
    setVisible(true);
  }

  public void paint (Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setColor(Color.white);
    g2.fillRect(0, 0, getSize().width, getSize().height);
    g2.setColor(Color.gray);
    if (dirty) {
      dirty = false;
      if (closePath) {
        path = CatmullRomToBezier.convert(points.toArray(new Point[points.size()]), true);
      } else {
        Point[] pnts = points.toArray(new Point[points.size() + 1]);
        // Duplicate last point so we can draw a curve through all points in the path
        pnts[pnts.length -1 ] = pnts[pnts.length - 2];
        path = CatmullRomToBezier.convert(pnts, false);
      }
    }
    if (path != null) {
      g2.draw(path);
    }
    // Draw control points used to define Catmull-Rom Spline
    g2.setColor(Color.blue);
    for (Point pp : points) {
      int x = pp.x;
      int y = pp.y;
      g2.fillRect(x - 2, y - 2, 5, 5);
    }
  }

  public static void main (String[] args) throws Exception {
    SwingUtilities.invokeLater(SplineClickDraw::new);
  }
}
