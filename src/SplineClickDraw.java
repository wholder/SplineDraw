import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/*
    SplineClickDraw testbed
    Ref: http://www.mvps.org/directx/articles/catmull/
    See also: http://www.cemyuksel.com/research/catmullrom_param/catmullrom.pdf   (other types)

    Convert Catmull-Rom to Bezier: https://gist.github.com/njvack/6925609
    And: https://advancedweb.hu/2014/10/28/plotting_charts_with_svg/

    How to use:
      Click first point to start curve
      Cluck second point to continue curve
      Continute clicking points to extend curve
      Click any point and drag to move point and alter curve
      Click on first point again to close curve (if neeeded
      Hold Shift Key down and click to erase screen and start again
*/

  public class SplineClickDraw extends JFrame {
  private List<Point2D.Float>   points = new ArrayList<>();
  private Point2D.Float         dragPoint;
  private boolean               closePath;
  private Path2D.Float          path;

  private SplineClickDraw () {
    super("Catmull-Rom SplineClickDraw");
    setBackground(Color.white);
    // Add listener to process mouse clicked and mouse released wvents
    addMouseListener(new MouseAdapter() {
      // Process mouse clicked events
      @Override
      public void mousePressed (MouseEvent me) {
        super.mousePressed(me);
        int x = me.getX();                            // mouse x position
        int y = me.getY();                            // mouse y position
        if (me.isShiftDown()) {
          // Clear screen and restart
          points.clear();
          closePath = false;
        } else {
          // Scan though all points to see if mouse clicked close to one them
          for (Point2D.Float cp : points) {
            double dx = cp.x - x;
            double dy = cp.y - y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < 5) {
              if (!closePath && cp == points.get(0)) {
                // If clicked on first point, then close the curve
                closePath = true;
              } else {
                // Setup for mounse drag code to move the point
                dragPoint = cp;
              }
              return;
            }
          }
          // Add new point
          points.add(new Point2D.Float(x, y));
        }
        repaint();
      }
      // Process mouse released events
      @Override
      public void mouseReleased (MouseEvent me) {
        super.mouseReleased(me);
        dragPoint = null;
        repaint();
      }
    });
    // Add listener to process mouse dragged events
    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged (MouseEvent me) {
        super.mouseDragged(me);
        int x = me.getX();
        int y = me.getY();
        if (dragPoint != null) {
          // Move point to new position by setting dragPoint to mouse position
          dragPoint.x = x;
          dragPoint.y = y;
          repaint();
        }
      }
    });
    // Add code to close window on quit
    addWindowListener(new WindowAdapter() {
      public void windowClosing (WindowEvent e) {
        System.exit(0);
      }
    });
    setSize(600, 600);                                // Set size of window
    setLocationRelativeTo(null);                      // center window pn screen
    setVisible(true);
  }

  // Code to redraw the screen
  public void paint (Graphics g) {
    // Create on off-screen drawing surface
    Graphics2D g2 = (Graphics2D) g;
    // Clear the off-screen drawing surface to white
    g2.setColor(Color.white);
    g2.fillRect(0, 0, getSize().width, getSize().height);
    g2.setColor(Color.gray);
    // Concert points that define the Catmull-Rom curve to Cubis Bezier control points and draw
    if (points != null && points.size() > 0) {
      path = CatmullRomToBezier.convert(points, closePath);
      g2.draw(path);
    }
    // Draw control points used to define Catmull-Rom Spline
    g2.setColor(Color.blue);
    for (Point2D.Float pnt : points) {
      g2.fill(new Rectangle2D.Float(pnt.x - 2, pnt.y - 2, 5, 5));
    }
  }

  public static void main (String[] args) throws Exception {
    new SplineClickDraw();
  }
}
