package homemate.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;

/**
 * Ikon garis sederhana yang digambar dengan kode (tanpa file gambar).
 * Pakai: label.setIcon(Ikon.buat(Ikon.Jenis.RUMAH, 20, Color.WHITE)).
 * Milik Said, boleh dipakai semua modul.
 */
public class Ikon implements Icon {

    public enum Jenis {
        RUMAH, ORANG, USER, TUGAS, KOTAK, DOMPET, KUNCI_INGGRIS, LONCENG, GRAFIK, GEAR,
        KELUAR, CENTANG, JAM, MATA, MATA_CORET, GEMBOK, PLUS, EDIT, TONG, LINGKARAN, KALENDER,
        BOTOL, DATABASE
    }

    private final Jenis jenis;
    private final int ukuran;
    private final Color warna;

    private Ikon(Jenis jenis, int ukuran, Color warna) {
        this.jenis = jenis;
        this.ukuran = ukuran;
        this.warna = warna;
    }

    public static Ikon buat(Jenis jenis, int ukuran, Color warna) {
        return new Ikon(jenis, ukuran, warna);
    }

    /** Ikon sebagai gambar, untuk ikon jendela (setIconImage). */
    public static BufferedImage gambar(Jenis jenis, int ukuran, Color warna) {
        BufferedImage img = new BufferedImage(ukuran, ukuran, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        buat(jenis, ukuran, warna).paintIcon(null, g, 0, 0);
        g.dispose();
        return img;
    }

    @Override
    public int getIconWidth() {
        return ukuran;
    }

    @Override
    public int getIconHeight() {
        return ukuran;
    }

    @Override
    public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        double s = ukuran / 24.0;
        g.scale(s, s);
        g.setColor(warna);
        g.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        gambarBentuk(g);
        g.dispose();
    }

    /** Semua bentuk digambar pada kotak 24 x 24. */
    private void gambarBentuk(Graphics2D g) {
        switch (jenis) {
            case RUMAH -> {
                Path2D.Double atap = new Path2D.Double();
                atap.moveTo(2.5, 11.5);
                atap.lineTo(12, 3.5);
                atap.lineTo(21.5, 11.5);
                g.draw(atap);
                Path2D.Double badan = new Path2D.Double();
                badan.moveTo(5, 9.5);
                badan.lineTo(5, 20.5);
                badan.lineTo(19, 20.5);
                badan.lineTo(19, 9.5);
                g.draw(badan);
                Path2D.Double pintu = new Path2D.Double();
                pintu.moveTo(10, 20.5);
                pintu.lineTo(10, 14);
                pintu.lineTo(14, 14);
                pintu.lineTo(14, 20.5);
                g.draw(pintu);
            }
            case USER -> {
                g.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
                g.draw(new Arc2D.Double(4, 14, 16, 14, 0, 180, Arc2D.OPEN));
            }
            case ORANG -> {
                g.draw(new Ellipse2D.Double(5.5, 4, 7, 7));
                g.draw(new Arc2D.Double(2, 14, 14, 12, 0, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(14, 5, 5.5, 5.5, 90, -180, Arc2D.OPEN));
                g.draw(new Arc2D.Double(15, 14, 7, 8, 0, 100, Arc2D.OPEN));
            }
            case TUGAS -> {
                g.draw(new RoundRectangle2D.Double(5, 4.5, 14, 17, 3, 3));
                g.draw(new RoundRectangle2D.Double(8.5, 2, 7, 4, 2, 2));
                Path2D.Double cek = new Path2D.Double();
                cek.moveTo(8.8, 13.5);
                cek.lineTo(11, 15.8);
                cek.lineTo(15.3, 10.8);
                g.draw(cek);
            }
            case KOTAK -> {
                Path2D.Double k = new Path2D.Double();
                k.moveTo(12, 2.8);
                k.lineTo(20.5, 7.4);
                k.lineTo(20.5, 16.6);
                k.lineTo(12, 21.2);
                k.lineTo(3.5, 16.6);
                k.lineTo(3.5, 7.4);
                k.closePath();
                g.draw(k);
                Path2D.Double d = new Path2D.Double();
                d.moveTo(3.5, 7.4);
                d.lineTo(12, 12);
                d.lineTo(20.5, 7.4);
                d.moveTo(12, 12);
                d.lineTo(12, 21.2);
                g.draw(d);
            }
            case BOTOL -> {
                g.draw(new RoundRectangle2D.Double(8, 2.5, 8, 4, 1.5, 1.5));
                g.draw(new RoundRectangle2D.Double(6.5, 6.5, 11, 15, 3, 3));
                g.draw(new Line2D.Double(6.5, 12.5, 17.5, 12.5));
            }
            case DOMPET -> {
                g.draw(new RoundRectangle2D.Double(2.5, 6, 19, 14, 3.5, 3.5));
                g.draw(new Line2D.Double(5, 6, 16, 3.2));
                g.draw(new Line2D.Double(2.5, 10.5, 21.5, 10.5));
                g.fill(new Ellipse2D.Double(15.5, 13.6, 2.6, 2.6));
            }
            case KUNCI_INGGRIS -> {
                g.draw(new Line2D.Double(4.5, 19.5, 13, 11));
                g.draw(new Arc2D.Double(11.5, 2.8, 9.5, 9.5, 225, 270, Arc2D.OPEN));
                g.draw(new Line2D.Double(14.7, 9.3, 12.5, 6.2));
            }
            case LONCENG -> {
                Path2D.Double b = new Path2D.Double();
                b.moveTo(5.5, 17);
                b.lineTo(5.5, 11);
                b.curveTo(5.5, 3.5, 18.5, 3.5, 18.5, 11);
                b.lineTo(18.5, 17);
                b.lineTo(20.5, 18.5);
                b.lineTo(3.5, 18.5);
                b.closePath();
                g.draw(b);
                g.draw(new Arc2D.Double(9.8, 18.5, 4.4, 4, 180, 180, Arc2D.OPEN));
            }
            case GRAFIK -> {
                g.draw(new Line2D.Double(6, 20, 6, 12));
                g.draw(new Line2D.Double(12, 20, 12, 4.5));
                g.draw(new Line2D.Double(18, 20, 18, 14.5));
            }
            case GEAR -> {
                g.draw(new Ellipse2D.Double(8.8, 8.8, 6.4, 6.4));
                g.draw(new Ellipse2D.Double(5, 5, 14, 14));
                for (int i = 0; i < 8; i++) {
                    double a = Math.toRadians(i * 45);
                    g.draw(new Line2D.Double(12 + 7 * Math.cos(a), 12 + 7 * Math.sin(a),
                            12 + 9.8 * Math.cos(a), 12 + 9.8 * Math.sin(a)));
                }
            }
            case KELUAR -> {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(9.5, 3.5);
                p.lineTo(5.5, 3.5);
                p.quadTo(3.5, 3.5, 3.5, 5.5);
                p.lineTo(3.5, 18.5);
                p.quadTo(3.5, 20.5, 5.5, 20.5);
                p.lineTo(9.5, 20.5);
                g.draw(p);
                Path2D.Double panah = new Path2D.Double();
                panah.moveTo(15.5, 7.5);
                panah.lineTo(20.5, 12);
                panah.lineTo(15.5, 16.5);
                panah.moveTo(20.5, 12);
                panah.lineTo(9, 12);
                g.draw(panah);
            }
            case CENTANG -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                Path2D.Double cek = new Path2D.Double();
                cek.moveTo(7.8, 12.3);
                cek.lineTo(10.7, 15.2);
                cek.lineTo(16.3, 9.2);
                g.draw(cek);
            }
            case LINGKARAN -> g.draw(new Ellipse2D.Double(3, 3, 18, 18));
            case JAM -> {
                g.draw(new Ellipse2D.Double(3, 3, 18, 18));
                Path2D.Double j = new Path2D.Double();
                j.moveTo(12, 7);
                j.lineTo(12, 12);
                j.lineTo(15.5, 14);
                g.draw(j);
            }
            case MATA, MATA_CORET -> {
                Path2D.Double m = new Path2D.Double();
                m.moveTo(1.8, 12);
                m.curveTo(5, 5.5, 19, 5.5, 22.2, 12);
                m.curveTo(19, 18.5, 5, 18.5, 1.8, 12);
                m.closePath();
                g.draw(m);
                g.draw(new Ellipse2D.Double(9, 9, 6, 6));
                if (jenis == Jenis.MATA_CORET) {
                    g.draw(new Line2D.Double(4, 3.5, 20, 20.5));
                }
            }
            case GEMBOK -> {
                g.draw(new RoundRectangle2D.Double(4.5, 10.5, 15, 10.5, 3, 3));
                Path2D.Double p = new Path2D.Double();
                p.moveTo(8, 10.5);
                p.lineTo(8, 7.5);
                p.curveTo(8, 1.5, 16, 1.5, 16, 7.5);
                p.lineTo(16, 10.5);
                g.draw(p);
            }
            case PLUS -> {
                g.draw(new Line2D.Double(12, 5, 12, 19));
                g.draw(new Line2D.Double(5, 12, 19, 12));
            }
            case EDIT -> {
                Path2D.Double p = new Path2D.Double();
                p.moveTo(4, 20);
                p.lineTo(5, 15.5);
                p.lineTo(16.5, 4);
                p.lineTo(20, 7.5);
                p.lineTo(8.5, 19);
                p.closePath();
                g.draw(p);
                g.draw(new Line2D.Double(14, 6.5, 17.5, 10));
            }
            case TONG -> {
                g.draw(new Line2D.Double(4, 6.5, 20, 6.5));
                g.draw(new RoundRectangle2D.Double(9, 3, 6, 3.5, 1, 1));
                Path2D.Double p = new Path2D.Double();
                p.moveTo(6, 6.5);
                p.lineTo(7, 20);
                p.lineTo(17, 20);
                p.lineTo(18, 6.5);
                g.draw(p);
                g.draw(new Line2D.Double(10, 10.5, 10, 16.5));
                g.draw(new Line2D.Double(14, 10.5, 14, 16.5));
            }
            case KALENDER -> {
                g.draw(new RoundRectangle2D.Double(3.5, 5, 17, 15.5, 3, 3));
                g.draw(new Line2D.Double(3.5, 10, 20.5, 10));
                g.draw(new Line2D.Double(8, 2.8, 8, 6.8));
                g.draw(new Line2D.Double(16, 2.8, 16, 6.8));
            }
            case DATABASE -> {
                g.draw(new Ellipse2D.Double(4.5, 3, 15, 5.5));
                Path2D.Double p = new Path2D.Double();
                p.moveTo(4.5, 5.75);
                p.lineTo(4.5, 18.25);
                p.curveTo(4.5, 21.5, 19.5, 21.5, 19.5, 18.25);
                p.lineTo(19.5, 5.75);
                g.draw(p);
                g.draw(new Arc2D.Double(4.5, 9.5, 15, 5.5, 180, 180, Arc2D.OPEN));
            }
            default -> {
            }
        }
    }
}
