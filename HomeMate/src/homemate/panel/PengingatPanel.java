package homemate.panel;

import homemate.dao.RingkasanDAO;
import homemate.model.Pengingat;
import homemate.util.Format;
import homemate.util.Ikon;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Layar Pengingat (mockup halaman 8). Milik Said.
 * Pengingat tidak disimpan: dihitung dari data tugas, persediaan, perawatan, dan anggaran.
 */
public class PengingatPanel extends JPanel {

    private final RingkasanDAO dao = new RingkasanDAO();
    private final List<Pengingat> semua = new ArrayList<>();
    private final JPanel daftar = new JPanel();
    private String filter = "Semua";

    public PengingatPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        JPanel atas = new JPanel(new BorderLayout(0, 14));
        atas.setOpaque(false);
        atas.add(Tema.header("Pengingat", "Hal penting yang perlu diperhatikan hari ini", null), BorderLayout.NORTH);
        atas.add(new Tema.TabBar(nama -> {
            filter = nama;
            tampilkan();
        }, "Semua", "Tugas", "Persediaan", "Perawatan", "Keuangan"), BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        daftar.setOpaque(false);
        daftar.setLayout(new BoxLayout(daftar, BoxLayout.Y_AXIS));
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.add(daftar, BorderLayout.NORTH);
        javax.swing.JScrollPane gulir = Tema.gulir(pembungkus);
        gulir.setOpaque(false);
        gulir.getViewport().setOpaque(false);
        add(gulir, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                segarkan();
            }
        });
        segarkan();
    }

    /** Hitung ulang pengingat dari database. */
    public void segarkan() {
        try {
            semua.clear();
            semua.addAll(dao.pengingat(LocalDate.now()));
            tampilkan();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat pengingat:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tampilkan() {
        daftar.removeAll();
        int jumlah = 0;
        for (Pengingat p : semua) {
            if (filter.equals("Semua") || filter.equals(p.getJenis())) {
                daftar.add(buatBaris(p));
                daftar.add(Box.createVerticalStrut(10));
                jumlah++;
            }
        }
        if (jumlah == 0) {
            JLabel l = Tema.label("Tidak ada pengingat.", Font.PLAIN, 15f, Tema.TEKS_REDUP,
                    Ikon.buat(Ikon.Jenis.CENTANG, 22, Tema.HIJAU));
            l.setBorder(BorderFactory.createEmptyBorder(30, 4, 0, 0));
            daftar.add(l);
        }
        daftar.revalidate();
        daftar.repaint();
    }

    private JPanel buatBaris(Pengingat p) {
        Tema.Kartu baris = new Tema.Kartu(new BorderLayout(16, 0));
        baris.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        baris.setPreferredSize(new Dimension(0, 82));

        Color[] w = Tema.warnaStatus(p.getJenis());
        baris.add(new Tema.KotakIkon(ikonJenis(p.getJenis()), 50, w[0], w[1], false), BorderLayout.WEST);

        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.add(Box.createVerticalGlue());
        teks.add(Tema.label(huruf(p.getJudul()), Font.BOLD, 15f, Tema.TEKS));
        teks.add(Box.createVerticalStrut(2));
        teks.add(Tema.label(p.getKeterangan(), Font.PLAIN, 13f, Tema.TEKS_REDUP));
        teks.add(Box.createVerticalGlue());
        baris.add(teks, BorderLayout.CENTER);

        JPanel kanan = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 24, 0));
        kanan.setOpaque(false);
        kanan.add(new Tema.Lencana(p.getJenis()));
        JLabel tgl = Tema.label(Format.tampil(p.getTanggal()), Font.PLAIN, 13f, Tema.TEKS_REDUP);
        tgl.setHorizontalAlignment(SwingConstants.RIGHT);
        tgl.setPreferredSize(new Dimension(84, 20));
        kanan.add(tgl);
        JPanel tengahVertikal = new JPanel(new java.awt.GridBagLayout());
        tengahVertikal.setOpaque(false);
        tengahVertikal.add(kanan);
        baris.add(tengahVertikal, BorderLayout.EAST);
        return baris;
    }

    private Ikon.Jenis ikonJenis(String jenis) {
        return switch (jenis) {
            case Pengingat.TUGAS -> Ikon.Jenis.TUGAS;
            case Pengingat.PERSEDIAAN -> Ikon.Jenis.BOTOL;
            case Pengingat.PERAWATAN -> Ikon.Jenis.KUNCI_INGGRIS;
            default -> Ikon.Jenis.DOMPET;
        };
    }

    /** Huruf pertama kapital ("stok beras menipis" menjadi "Stok beras menipis"). */
    private String huruf(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
