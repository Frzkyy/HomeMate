package homemate.panel;

import homemate.dao.RingkasanDAO;
import homemate.model.Pengguna;
import homemate.util.Format;
import homemate.util.Ikon;
import homemate.util.Preferensi;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Layar Dashboard (mockup halaman 2): ringkasan semua modul. Milik Said. Data dibaca ulang tiap layar ditampilkan. */
public class DashboardPanel extends JPanel {

    private final RingkasanDAO dao = new RingkasanDAO();
    private final Pengguna pengguna;
    private final Consumer<String> navigasi;

    private final JLabel lblSapa = Tema.label(" ", Font.PLAIN, 13f, Tema.TEKS_REDUP);
    private final JLabel lblTanggal = Tema.label(" ", Font.PLAIN, 13f, Tema.TEKS_REDUP);
    private final Lonceng lonceng = new Lonceng();

    private final JLabel lblSelesai = angka();
    private final JLabel lblTertunda = angka();
    private final JLabel lblMenipis = angka();
    private final JLabel lblPengeluaran = angka();
    private final JLabel subSelesai = subKartu();
    private final JLabel subTertunda = subKartu();
    private final JLabel subMenipis = subKartu();
    private final JLabel subPengeluaran = subKartu();

    private final DefaultTableModel modelTugas = new DefaultTableModel(
            new String[]{"Tugas", "Penanggung Jawab", "Prioritas", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final CardLayout kartuTugas = new CardLayout();
    private final JPanel isiTugas = new JPanel(kartuTugas);
    private final JPanel daftarStok = new JPanel();

    public DashboardPanel(Pengguna pengguna, Consumer<String> navigasi) {
        super(new BorderLayout());
        this.pengguna = pengguna;
        this.navigasi = navigasi;
        Tema.gayaHalaman(this);

        add(buatHeader(), BorderLayout.NORTH);

        JPanel isi = new JPanel(new BorderLayout(0, 18));
        isi.setOpaque(false);

        // Empat kartu ringkasan
        JPanel kartu = new JPanel(new GridLayout(1, 4, 16, 0));
        kartu.setOpaque(false);
        kartu.add(buatKartu("Tugas Selesai", lblSelesai, subSelesai, Ikon.Jenis.CENTANG,
                new Color(0xF0FDF4), new Color(0xBBF7D0), Tema.HIJAU_MUDA, Tema.HIJAU, Tema.HIJAU_TUA));
        kartu.add(buatKartu("Tugas Tertunda", lblTertunda, subTertunda, Ikon.Jenis.JAM,
                new Color(0xFFFBEB), new Color(0xFDE68A), Tema.ORANYE_MUDA, Tema.ORANYE, Tema.ORANYE_TUA));
        kartu.add(buatKartu("Stok Menipis", lblMenipis, subMenipis, Ikon.Jenis.BOTOL,
                new Color(0xFEF2F2), new Color(0xFECACA), Tema.MERAH_MUDA, Tema.MERAH, Tema.MERAH_TUA));
        kartu.add(buatKartu("Total Pengeluaran", lblPengeluaran, subPengeluaran, Ikon.Jenis.DOMPET,
                new Color(0xEFF6FF), new Color(0xBFDBFE), Tema.BIRU_MUDA, Tema.PRIMER, Tema.BIRU_TUA));
        isi.add(kartu, BorderLayout.NORTH);

        // Tugas hari ini (kiri) + stok menipis (kanan)
        JPanel tengah = new JPanel(new GridBagLayout());
        tengah.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1;
        g.gridy = 0;
        g.gridx = 0;
        g.weightx = 0.62;
        g.insets = new Insets(0, 0, 0, 16);
        JPanel kartuKiri = buatKartuTugas();
        kartuKiri.setPreferredSize(new Dimension(10, 10)); // lebar kolom ditentukan weightx, bukan isi
        tengah.add(kartuKiri, g);
        g.gridx = 1;
        g.weightx = 0.38;
        g.insets = new Insets(0, 0, 0, 0);
        JPanel kartuKanan = buatKartuStok();
        kartuKanan.setPreferredSize(new Dimension(10, 10));
        tengah.add(kartuKanan, g);
        isi.add(tengah, BorderLayout.CENTER);
        add(isi, BorderLayout.CENTER);

        // Muat ulang data setiap kali layar ini ditampilkan lagi
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                segarkan();
            }
        });
        segarkan();
    }

    // ================= TAMPILAN =================

    private JPanel buatHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel kiri = new JPanel(new BorderLayout(0, 2));
        kiri.setOpaque(false);
        kiri.add(Tema.label("Dashboard", Font.BOLD, 26f, Tema.TEKS), BorderLayout.NORTH);
        kiri.add(lblSapa, BorderLayout.CENTER);
        p.add(kiri, BorderLayout.WEST);

        JPanel kanan = new JPanel();
        kanan.setOpaque(false);
        kanan.setLayout(new BoxLayout(kanan, BoxLayout.X_AXIS));
        kanan.add(lblTanggal);
        kanan.add(Box.createHorizontalStrut(16));
        kanan.add(lonceng);
        lonceng.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lonceng.setToolTipText("Buka Pengingat");
        lonceng.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigasi.accept("Pengingat");
            }
        });
        p.add(kanan, BorderLayout.EAST);
        return p;
    }

    private static JLabel angka() {
        return new AngkaOtomatis();
    }

    private static JLabel subKartu() {
        return Tema.label(" ", Font.PLAIN, 12f, Tema.TEKS_REDUP);
    }

    private JPanel buatKartu(String judul, JLabel angka, JLabel sub, Ikon.Jenis ikon,
            Color latar, Color garis, Color latarIkon, Color warnaIkon, Color warnaJudul) {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(14, 0)).warna(latar, garis);
        k.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        k.add(new Tema.KotakIkon(ikon, 46, latarIkon, warnaIkon, false), BorderLayout.WEST);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        JLabel j = Tema.label(judul, Font.BOLD, 13f, warnaJudul);
        angka.setForeground(Tema.TEKS);
        teks.add(j);
        teks.add(angka);
        teks.add(sub);
        k.add(teks, BorderLayout.CENTER);
        k.setPreferredSize(new Dimension(0, 104));
        return k;
    }

    private JPanel buatKartuTugas() {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(0, 8));
        k.add(judulKartu("Tugas Hari Ini", "Lihat Semua", "Tugas Rumah"), BorderLayout.NORTH);

        JTable tabel = new JTable(modelTugas);
        Tema.gayaTabel(tabel);
        tabel.setRowHeight(48);
        tabel.getColumnModel().getColumn(0).setCellRenderer(new RendererTugas());
        tabel.getColumnModel().getColumn(2).setCellRenderer(Tema.rendererLencana());
        tabel.getColumnModel().getColumn(3).setCellRenderer(Tema.rendererLencana());
        tabel.getColumnModel().getColumn(0).setPreferredWidth(190);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(165);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(95);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(95);

        isiTugas.setOpaque(false);
        isiTugas.add(Tema.gulir(tabel), "tabel");
        JLabel kosong = Tema.label("Tidak ada tugas dengan tenggat hari ini.", Font.PLAIN, 14f, Tema.TEKS_REDUP);
        kosong.setHorizontalAlignment(SwingConstants.CENTER);
        isiTugas.add(kosong, "kosong");
        k.add(isiTugas, BorderLayout.CENTER);
        return k;
    }

    private JPanel buatKartuStok() {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(0, 8));
        k.add(judulKartu("Stok Menipis", "Lihat Semua", "Persediaan"), BorderLayout.NORTH);
        daftarStok.setOpaque(false);
        daftarStok.setLayout(new BoxLayout(daftarStok, BoxLayout.Y_AXIS));
        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(daftarStok, BorderLayout.NORTH);
        k.add(Tema.gulir(atas), BorderLayout.CENTER);
        return k;
    }

    /** Judul kartu + tautan "Lihat Semua" di kanan yang membuka layar lain. */
    private JPanel judulKartu(String judul, String tautan, String tujuan) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(Tema.label(judul, Font.BOLD, 16f, Tema.TEKS), BorderLayout.WEST);
        JLabel l = Tema.label(tautan, Font.BOLD, 13f, Tema.PRIMER);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigasi.accept(tujuan);
            }
        });
        p.add(l, BorderLayout.EAST);
        return p;
    }

    // ================= DATA =================

    /** Baca ulang semua data dari database. */
    public void segarkan() {
        LocalDate hariIni = LocalDate.now();
        lblSapa.setText("Selamat datang, " + pengguna.getNama() + "!");
        lblTanggal.setText(Format.hariTanggal(hariIni));
        try {
            YearMonth bulan = YearMonth.from(hariIni);
            int selesai = dao.hitungTugas(true);
            int tertunda = dao.hitungTugas(false);
            int semua = selesai + tertunda;

            lblSelesai.setText(String.valueOf(selesai));
            subSelesai.setText("dari " + semua + " tugas");
            lblTertunda.setText(String.valueOf(tertunda));
            subTertunda.setText("dari " + semua + " tugas");
            lblMenipis.setText(String.valueOf(dao.hitungStokMenipis()));
            subMenipis.setText("barang");
            lblPengeluaran.setText(Format.rupiah(
                    dao.totalPengeluaran(bulan.atDay(1).toString(), bulan.atEndOfMonth().toString())));
            subPengeluaran.setText("bulan ini");

            modelTugas.setRowCount(0);
            for (String[] r : dao.tugasHariIni(hariIni)) {
                modelTugas.addRow(new String[]{r[0], r[1], Format.kapital(r[2]), Format.kapital(r[3])});
            }
            kartuTugas.show(isiTugas, modelTugas.getRowCount() == 0 ? "kosong" : "tabel");

            isiStok(dao.barangMenipis());

            lonceng.setJumlah(Preferensi.isNotifikasiAktif() ? dao.pengingat(hariIni).size() : 0);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat dashboard:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void isiStok(List<String[]> barang) {
        daftarStok.removeAll();
        if (barang.isEmpty()) {
            JLabel l = Tema.label("Semua stok aman.", Font.PLAIN, 14f, Tema.HIJAU_TUA,
                    Ikon.buat(Ikon.Jenis.CENTANG, 20, Tema.HIJAU));
            l.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
            daftarStok.add(l);
        }
        for (String[] r : barang) {
            daftarStok.add(barisStok(r[0], r[1].trim(), r[2].trim()));
        }
        daftarStok.revalidate();
        daftarStok.repaint();
    }

    private JPanel barisStok(String nama, String sisa, String minimum) {
        JPanel baris = new JPanel(new BorderLayout(12, 0));
        baris.setOpaque(false);
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        baris.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS),
                BorderFactory.createEmptyBorder(8, 0, 8, 0)));
        baris.add(new Tema.KotakIkon(Ikon.Jenis.BOTOL, 40, Tema.MERAH_MUDA, Tema.MERAH, false), BorderLayout.WEST);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.add(Tema.label(nama, Font.BOLD, 14f, Tema.TEKS));
        teks.add(Tema.label("Sisa " + sisa, Font.PLAIN, 12f, Tema.MERAH_TUA));
        baris.add(teks, BorderLayout.CENTER);
        baris.add(Tema.label("Min. " + minimum, Font.PLAIN, 12f, Tema.TEKS_REDUP), BorderLayout.EAST);
        return baris;
    }

    // ================= KOMPONEN KHUSUS =================

    /** Kolom "Tugas": ikon centang hijau kalau selesai, lingkaran abu kalau belum. */
    private static class RendererTugas extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                boolean fokus, int row, int col) {
            super.getTableCellRendererComponent(table, value, sel, false, row, col);
            boolean selesai = "Selesai".equals(table.getModel().getValueAt(row, 3));
            setIcon(Ikon.buat(selesai ? Ikon.Jenis.CENTANG : Ikon.Jenis.LINGKARAN, 20,
                    selesai ? Tema.PRIMER : new Color(0x94A3B8)));
            setIconTextGap(12);
            setFont(Tema.font(Font.BOLD, 13f));
            setForeground(Tema.TEKS);
            setBackground(sel ? new Color(0xEFF6FF) : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            return this;
        }
    }

    /** Angka besar di kartu ringkasan; font mengecil otomatis kalau teksnya tidak muat (mis. nominal Rupiah). */
    private static class AngkaOtomatis extends JLabel {

        AngkaOtomatis() {
            super("0");
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(10, 40);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Short.MAX_VALUE, 40);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            float ukuran = 30f;
            Font f = Tema.font(Font.BOLD, ukuran);
            while (ukuran > 14f && g.getFontMetrics(f).stringWidth(getText()) > getWidth()) {
                ukuran -= 1f;
                f = Tema.font(Font.BOLD, ukuran);
            }
            g.setFont(f);
            g.setColor(getForeground());
            int y = (getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2;
            g.drawString(getText(), 0, y);
            g.dispose();
        }
    }

    /** Ikon lonceng dengan lingkaran merah berisi jumlah pengingat. */
    private static class Lonceng extends JComponent {

        private int jumlah;

        Lonceng() {
            setPreferredSize(new Dimension(34, 34));
            setMaximumSize(new Dimension(34, 34));
        }

        void setJumlah(int jumlah) {
            this.jumlah = jumlah;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Ikon.buat(Ikon.Jenis.LONCENG, 26, Tema.TEKS).paintIcon(this, g, 2, 6);
            if (jumlah > 0) {
                String t = jumlah > 9 ? "9+" : String.valueOf(jumlah);
                g.setColor(Tema.MERAH);
                g.fillOval(15, 0, 18, 18);
                g.setColor(Color.WHITE);
                g.setFont(Tema.font(Font.BOLD, 10f));
                int w = g.getFontMetrics().stringWidth(t);
                g.drawString(t, 15 + (18 - w) / 2, 13);
            }
            g.dispose();
        }
    }
}
