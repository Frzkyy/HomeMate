package homemate.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;

/**
 * Gaya tampilan bersama (warna, font, komponen) supaya semua layar mirip mockup.
 * Milik Said. Modul lain cukup memanggil, contoh:
 * <pre>
 *   Tema.gayaHalaman(this);
 *   add(Tema.header("Anggota Keluarga", "Kelola anggota rumah", btnTambah), BorderLayout.NORTH);
 *   Tema.gayaTabel(tabel);
 *   tabel.getColumnModel().getColumn(4).setCellRenderer(Tema.rendererLencana());
 * </pre>
 */
public final class Tema {

    private Tema() {
    }

    // ===== Warna =====
    public static final Color SIDEBAR = new Color(0x143A73);
    public static final Color SIDEBAR_HOVER = new Color(0x1E4A8C);
    public static final Color AKTIF = new Color(0x2F6FED);
    public static final Color PRIMER = new Color(0x2563EB);
    public static final Color PRIMER_HOVER = new Color(0x1D4ED8);
    public static final Color LATAR = new Color(0xF5F8FC);
    public static final Color KARTU = Color.WHITE;
    public static final Color GARIS = new Color(0xE2E8F0);
    public static final Color TEKS = new Color(0x0F172A);
    public static final Color TEKS_REDUP = new Color(0x64748B);
    public static final Color BIRU_MUDA = new Color(0xDBEAFE);
    public static final Color BIRU_TUA = new Color(0x1D4ED8);
    public static final Color HIJAU = new Color(0x16A34A);
    public static final Color HIJAU_MUDA = new Color(0xDCFCE7);
    public static final Color HIJAU_TUA = new Color(0x15803D);
    public static final Color ORANYE = new Color(0xF59E0B);
    public static final Color ORANYE_MUDA = new Color(0xFEF3C7);
    public static final Color ORANYE_TUA = new Color(0xB45309);
    public static final Color MERAH = new Color(0xDC2626);
    public static final Color MERAH_MUDA = new Color(0xFEE2E2);
    public static final Color MERAH_TUA = new Color(0xB91C1C);
    private static final Color BARIS_PILIH = new Color(0xEFF6FF);

    // ===== Font =====
    private static final String KELUARGA_FONT = pilihFont("Segoe UI", "Inter", "SF Pro Text", "Helvetica Neue", "SansSerif");

    private static String pilihFont(String... kandidat) {
        String[] ada = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String k : kandidat) {
            if (Arrays.asList(ada).contains(k)) {
                return k;
            }
        }
        return "SansSerif";
    }

    public static Font font(int gaya, float ukuran) {
        return new Font(KELUARGA_FONT, gaya, 1).deriveFont(gaya, ukuran);
    }

    // ===== Warna status (konsisten di semua layar) =====

    /** Pasangan {latar, teks} untuk badge. Hijau: Selesai/Aman/Terjadwal. Oranye: Sedang. Merah: Belum/Menipis/Tinggi. */
    public static Color[] warnaStatus(String teks) {
        String k = teks == null ? "" : teks.trim().toLowerCase();
        switch (k) {
            case "selesai", "aman", "terjadwal", "rendah", "pemasukan", "keuangan":
                return new Color[]{HIJAU_MUDA, HIJAU_TUA};
            case "sedang", "sedang dikerjakan", "tugas":
                return new Color[]{ORANYE_MUDA, ORANYE_TUA};
            case "belum", "belum dimulai", "menipis", "tinggi", "pengeluaran", "persediaan":
                return new Color[]{MERAH_MUDA, MERAH_TUA};
            case "perawatan":
                return new Color[]{BIRU_MUDA, BIRU_TUA};
            default:
                return new Color[]{new Color(0xF1F5F9), TEKS_REDUP};
        }
    }

    // ===== Helper umum =====

    /** Beri latar abu muda + jarak tepi untuk panel layar penuh. */
    public static void gayaHalaman(JPanel p) {
        p.setBackground(LATAR);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
    }

    /** Judul halaman: judul besar + subjudul abu, dan komponen opsional di kanan (tombol, tanggal, dll). */
    public static JPanel header(String judul, String sub, JComponent kanan) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel teks = new JPanel(new BorderLayout(0, 2));
        teks.setOpaque(false);
        JLabel l = new JLabel(judul);
        l.setFont(font(Font.BOLD, 26f));
        l.setForeground(TEKS);
        teks.add(l, BorderLayout.NORTH);
        if (sub != null) {
            JLabel s = new JLabel(sub);
            s.setFont(font(Font.PLAIN, 13f));
            s.setForeground(TEKS_REDUP);
            teks.add(s, BorderLayout.CENTER);
        }
        p.add(teks, BorderLayout.WEST);
        if (kanan != null) {
            p.add(kanan, BorderLayout.EAST);
        }
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        return p;
    }

    public static JLabel label(String teks, int gaya, float ukuran, Color warna) {
        JLabel l = new JLabel(teks);
        l.setFont(font(gaya, ukuran));
        l.setForeground(warna);
        return l;
    }

    public static JLabel label(String teks, int gaya, float ukuran, Color warna, javax.swing.Icon ikon) {
        JLabel l = label(teks, gaya, ukuran, warna);
        l.setIcon(ikon);
        l.setIconTextGap(8);
        return l;
    }

    /** Gaya kolom isian: sudut bulat, garis abu, biru saat fokus. */
    public static void gayaKolom(JTextComponent k) {
        k.setFont(font(Font.PLAIN, 14f));
        k.setForeground(TEKS);
        k.setBackground(Color.WHITE);
        k.setBorder(borderKolom(GARIS));
        k.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                k.setBorder(borderKolom(PRIMER));
            }

            @Override
            public void focusLost(FocusEvent e) {
                k.setBorder(borderKolom(GARIS));
            }
        });
    }

    public static Border borderKolom(Color garis) {
        return BorderFactory.createCompoundBorder(new BorderBulat(garis, 10),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    // ===== Tabel =====

    /** Gaya tabel mockup: tanpa garis kotak, header polos, baris dengan garis tipis. */
    public static void gayaTabel(JTable t) {
        t.setRowHeight(46);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setBackground(Color.WHITE);
        t.setSelectionBackground(BARIS_PILIH);
        t.setSelectionForeground(TEKS);
        t.setFont(font(Font.PLAIN, 13f));
        t.setFocusable(false);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setDefaultRenderer(Object.class, new TeksRenderer());
        t.setDefaultRenderer(String.class, new TeksRenderer());
        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 40));
        h.setDefaultRenderer(new HeaderRenderer());
    }

    /** Bungkus tabel dengan scroll tanpa garis tepi. */
    public static JScrollPane gulir(JComponent isi) {
        JScrollPane sp = new JScrollPane(isi);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public static TableCellRenderer rendererLencana() {
        return new LencanaRenderer();
    }

    private static class TeksRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                boolean fokus, int row, int col) {
            super.getTableCellRendererComponent(table, value, sel, false, row, col);
            setFont(font(Font.PLAIN, 13f));
            setForeground(TEKS);
            setBackground(sel ? BARIS_PILIH : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                boolean fokus, int row, int col) {
            super.getTableCellRendererComponent(table, value, false, false, row, col);
            setFont(font(Font.BOLD, 12f));
            setForeground(TEKS_REDUP);
            setBackground(Color.WHITE);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    /** Sel tabel berisi badge berwarna sesuai teksnya (Selesai, Menipis, Tinggi, dst). */
    private static class LencanaRenderer implements TableCellRenderer {

        private final JPanel sel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        private final Lencana lencana = new Lencana("");

        LencanaRenderer() {
            sel.add(lencana);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel,
                boolean fokus, int row, int col) {
            lencana.setTeks(value == null ? "-" : value.toString());
            sel.setBackground(isSel ? BARIS_PILIH : Color.WHITE);
            sel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS));
            // pusatkan secara vertikal
            int atas = Math.max(0, (table.getRowHeight(row) - lencana.getPreferredSize().height) / 2 - 1);
            sel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, atas));
            return sel;
        }
    }

    // ===== Komponen =====

    /** Panel putih bersudut bulat dengan garis tipis (kartu di mockup). */
    public static class Kartu extends JPanel {

        private Color latar = Color.WHITE;
        private Color garis = GARIS;
        private final int radius;

        public Kartu(LayoutManager lm) {
            this(lm, 14);
        }

        public Kartu(LayoutManager lm, int radius) {
            super(lm);
            this.radius = radius;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        }

        public Kartu warna(Color latar, Color garis) {
            this.latar = latar;
            this.garis = garis;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(latar);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (garis != null) {
                g.setColor(garis);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Lingkaran/kotak bulat berisi ikon (untuk kartu ringkasan dan daftar). */
    public static class KotakIkon extends JComponent {

        private final Ikon ikon;
        private final Color latar;
        private final int ukuran;
        private final boolean bulat;

        public KotakIkon(Ikon.Jenis jenis, int ukuran, Color latar, Color warnaIkon, boolean bulat) {
            this.ukuran = ukuran;
            this.latar = latar;
            this.bulat = bulat;
            this.ikon = Ikon.buat(jenis, ukuran / 2, warnaIkon);
            setPreferredSize(new Dimension(ukuran, ukuran));
            setMinimumSize(new Dimension(ukuran, ukuran));
            setMaximumSize(new Dimension(ukuran, ukuran));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(latar);
            if (bulat) {
                g.fillOval(0, 0, ukuran, ukuran);
            } else {
                g.fillRoundRect(0, 0, ukuran, ukuran, ukuran / 3, ukuran / 3);
            }
            int o = (ukuran - ikon.getIconWidth()) / 2;
            ikon.paintIcon(this, g, o, o);
            g.dispose();
        }
    }

    /** Badge kecil berbentuk pil, warna otomatis dari teks (lihat warnaStatus). */
    public static class Lencana extends JLabel {

        private Color latar;

        public Lencana(String teks) {
            setFont(font(Font.BOLD, 12f));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
            setTeks(teks);
        }

        public Lencana(String teks, Color latar, Color warnaTeks) {
            this(teks);
            this.latar = latar;
            setForeground(warnaTeks);
        }

        public void setTeks(String teks) {
            Color[] w = warnaStatus(teks);
            latar = w[0];
            setForeground(w[1]);
            setText(teks);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(latar);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Tombol bersudut bulat: UTAMA (biru penuh), TEPI (garis biru), BAHAYA (merah). */
    public static class Tombol extends JButton {

        public enum Gaya { UTAMA, TEPI, BAHAYA }

        private final Gaya gaya;

        public Tombol(String teks, Gaya gaya) {
            this(teks, gaya, null);
        }

        public Tombol(String teks, Gaya gaya, Ikon.Jenis jenisIkon) {
            super(teks);
            this.gaya = gaya;
            setFont(font(Font.BOLD, 13f));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setMargin(new Insets(9, 18, 9, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Color teksWarna = gaya == Gaya.TEPI ? PRIMER : Color.WHITE;
            setForeground(teksWarna);
            if (jenisIkon != null) {
                setIcon(Ikon.buat(jenisIkon, 16, teksWarna));
                setIconTextGap(8);
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover() || getModel().isPressed();
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            switch (gaya) {
                case UTAMA -> {
                    g.setColor(hover ? PRIMER_HOVER : PRIMER);
                    g.fillRoundRect(0, 0, w, h, 10, 10);
                }
                case BAHAYA -> {
                    g.setColor(hover ? MERAH_TUA : MERAH);
                    g.fillRoundRect(0, 0, w, h, 10, 10);
                }
                case TEPI -> {
                    g.setColor(hover ? BIRU_MUDA : Color.WHITE);
                    g.fillRoundRect(0, 0, w, h, 10, 10);
                    g.setColor(PRIMER);
                    g.drawRoundRect(0, 0, w, h, 10, 10);
                }
            }
            g.dispose();
            super.paintComponent(g0);
        }
    }

    /** Deretan tab teks dengan garis bawah biru (Semua | Tugas | Persediaan ...). */
    public static class TabBar extends JPanel {

        private final JLabel[] item;
        private final Consumer<String> saatPilih;

        public TabBar(Consumer<String> saatPilih, String... nama) {
            super(new FlowLayout(FlowLayout.LEFT, 0, 0));
            this.saatPilih = saatPilih;
            setOpaque(false);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GARIS));
            item = new JLabel[nama.length];
            for (int i = 0; i < nama.length; i++) {
                final String n = nama[i];
                JLabel l = new JLabel(n, SwingConstants.CENTER);
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                l.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        pilih(n);
                    }
                });
                item[i] = l;
                add(l);
            }
            tandai(nama[0]);
        }

        public void pilih(String nama) {
            tandai(nama);
            saatPilih.accept(nama);
        }

        private void tandai(String nama) {
            for (JLabel l : item) {
                boolean aktif = l.getText().equals(nama);
                l.setFont(font(aktif ? Font.BOLD : Font.PLAIN, 14f));
                l.setForeground(aktif ? PRIMER : TEKS_REDUP);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 3, 0, aktif ? PRIMER : new Color(0, 0, 0, 0)),
                        BorderFactory.createEmptyBorder(8, 20, 9, 20)));
            }
            revalidate();
            repaint();
        }
    }

    /** Saklar on/off (toggle). */
    public static class Saklar extends JComponent {

        private boolean nyala;

        public Saklar(boolean nyala) {
            this.nyala = nyala;
            setPreferredSize(new Dimension(46, 26));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setNyala(!Saklar.this.nyala);
                }
            });
        }

        public boolean isNyala() {
            return nyala;
        }

        public void setNyala(boolean nyala) {
            this.nyala = nyala;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(nyala ? PRIMER : new Color(0xCBD5E1));
            g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g.setColor(Color.WHITE);
            int d = getHeight() - 6;
            g.fillOval(nyala ? getWidth() - d - 3 : 3, 3, d, d);
            g.dispose();
        }
    }

    /** Border sudut bulat satu warna. */
    public static class BorderBulat extends AbstractBorder {

        private final Color warna;
        private final int radius;

        public BorderBulat(Color warna, int radius) {
            this.warna = warna;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g0, int x, int y, int w, int h) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(warna);
            g.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }
}
