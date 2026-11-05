package homemate;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * Membungkus satu tampilan (desain asli) dan menskalakannya seragam mengikuti
 * ukuran jendela. Artwork, warna, font, dan susunan tidak diubah; hanya diperbesar/diperkecil.
 */
public class ScalePane extends JPanel {

    private static boolean managerInstalled = false;

    private final JComponent view;
    private final Dimension base;
    private final JPanel holder = new JPanel(new BorderLayout()) {
        @Override
        public boolean contains(int x, int y) {
            return false; // semua mouse event ditangkap ScalePane lalu diteruskan dengan koordinat terskala
        }
    };
    private double scale = 1.0;
    private Component pressTarget, hoverTarget;

    public ScalePane(JComponent view, Dimension base) {
        super(null);
        this.view = view;
        this.base = new Dimension(base);
        holder.setOpaque(false);
        holder.add(view, BorderLayout.CENTER);
        add(holder);
        enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
        installRepaintManager();
        disableBlitScrolling(view);
        disableDoubleBuffering(this);
    }

    public double getScale() {
        return scale;
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();
        return new Dimension(base);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(1, 1);
    }

    @Override
    public void doLayout() {
        int w = Math.max(1, getWidth()), h = Math.max(1, getHeight());
        scale = Math.min(w / (double) base.width, h / (double) base.height);
        if (scale <= 0) scale = 1.0;
        holder.setBounds(0, 0, (int) Math.ceil(w / scale), (int) Math.ceil(h / scale));
        holder.doLayout();
        holder.validate();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.scale(scale, scale);
            g2.clip(new java.awt.Rectangle(0, 0, holder.getWidth(), holder.getHeight()));
            holder.paint(g2);
        } finally {
            g2.dispose();
        }
    }

    // ---------- mouse forwarding ----------

    private Component targetAt(MouseEvent e) {
        int vx = (int) Math.floor(e.getX() / scale), vy = (int) Math.floor(e.getY() / scale);
        Component c = SwingUtilities.getDeepestComponentAt(view, vx, vy);
        return c != null ? c : view;
    }

    private void forward(MouseEvent e, Component target, int id) {
        if (target == null) return;
        int vx = (int) Math.floor(e.getX() / scale), vy = (int) Math.floor(e.getY() / scale);
        Point p = SwingUtilities.convertPoint(view, vx, vy, target);
        MouseEvent ne;
        if (e instanceof MouseWheelEvent) {
            MouseWheelEvent w = (MouseWheelEvent) e;
            ne = new MouseWheelEvent(target, id, e.getWhen(), e.getModifiersEx(), p.x, p.y, e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(), e.isPopupTrigger(), w.getScrollType(), w.getScrollAmount(), w.getWheelRotation(), w.getPreciseWheelRotation());
        } else {
            ne = new MouseEvent(target, id, e.getWhen(), e.getModifiersEx(), p.x, p.y, e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(), e.isPopupTrigger(), e.getButton());
        }
        target.dispatchEvent(ne);
    }

    private void updateHover(MouseEvent e, Component t) {
        if (t != hoverTarget) {
            if (hoverTarget != null) forward(e, hoverTarget, MouseEvent.MOUSE_EXITED);
            hoverTarget = t;
            if (t != null) {
                forward(e, t, MouseEvent.MOUSE_ENTERED);
                setCursor(t.getCursor());
            }
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                pressTarget = targetAt(e);
                updateHover(e, pressTarget);
                forward(e, pressTarget, e.getID());
                break;
            case MouseEvent.MOUSE_RELEASED:
            case MouseEvent.MOUSE_CLICKED:
                forward(e, pressTarget != null ? pressTarget : targetAt(e), e.getID());
                if (e.getID() == MouseEvent.MOUSE_RELEASED) updateHover(e, targetAt(e));
                break;
            case MouseEvent.MOUSE_ENTERED:
                updateHover(e, targetAt(e));
                break;
            case MouseEvent.MOUSE_EXITED:
                if (hoverTarget != null) forward(e, hoverTarget, MouseEvent.MOUSE_EXITED);
                hoverTarget = null;
                break;
            default:
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
        if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
            forward(e, pressTarget, e.getID());
        } else {
            Component t = targetAt(e);
            updateHover(e, t);
            forward(e, t, e.getID());
        }
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        Component t = targetAt(e);
        forward(e, t, e.getID());
    }

    // ---------- repaint & scroll ----------

    // Buffer ganda per-komponen bikin posisi/clip meleset saat digambar dengan skala (terutama di Windows).
    // Buffer ganda tingkat jendela tetap aktif, jadi nggak kedip.
    private static void disableDoubleBuffering(Component c) {
        if (c instanceof JComponent && c.getClass() != ScalePane.class) ((JComponent) c).setDoubleBuffered(false);
        if (c instanceof java.awt.Container)
            for (Component child : ((java.awt.Container) c).getComponents()) disableDoubleBuffering(child);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        disableDoubleBuffering(this);
    }

    private static void disableBlitScrolling(Component c) {
        if (c instanceof JViewport) ((JViewport) c).setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        if (c instanceof java.awt.Container)
            for (Component child : ((java.awt.Container) c).getComponents()) disableBlitScrolling(child);
    }

    private static synchronized void installRepaintManager() {
        if (managerInstalled) return;
        managerInstalled = true;
        RepaintManager.setCurrentManager(new RepaintManager() {
            @Override
            public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
                ScalePane sp = (ScalePane) SwingUtilities.getAncestorOfClass(ScalePane.class, c);
                if (sp != null && c != sp) {
                    super.addDirtyRegion(sp, 0, 0, sp.getWidth(), sp.getHeight());
                } else {
                    super.addDirtyRegion(c, x, y, w, h);
                }
            }
        });
    }
}
