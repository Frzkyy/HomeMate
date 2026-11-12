
package homemate.panel;

import homemate.dao.NotifikasiDAO;
import homemate.model.Notifikasi;
import homemate.util.Tema;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class NotifikasiPanel extends JPanel {

    private final NotifikasiDAO dao = new NotifikasiDAO();

    private JPanel daftarNotifikasi;
    private JLabel labelJumlah;

    private JButton btnSemua;
    private JButton btnBelumDibaca;

    private String filter = "SEMUA";

    private final Color BIRU = new Color(0x2563EB);
    private final Color LATAR = new Color(0xF8FAFC);
    private final Color PUTIH = Color.WHITE;
    private final Color TEKS = new Color(0x1E293B);
    private final Color REDUP = new Color(0x64748B);
    private final Color BORDER = new Color(0xE2E8F0);

    public NotifikasiPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(LATAR);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        buatHeader();
        buatDaftar();

        refreshNotifikasi();
    }

    // ================= HEADER =================

    private void buatHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel judulPanel = new JPanel();
        judulPanel.setOpaque(false);
        judulPanel.setLayout(
                new BoxLayout(judulPanel, BoxLayout.Y_AXIS));

        JLabel judul = new JLabel("Notifikasi");
        judul.setFont(Tema.font(Font.BOLD, 25f));
        judul.setForeground(TEKS);

        JLabel deskripsi = new JLabel(
                "Pantau aktivitas dan informasi terbaru keluarga.");
        deskripsi.setFont(Tema.font(Font.PLAIN, 13f));
        deskripsi.setForeground(REDUP);

        judulPanel.add(judul);
        judulPanel.add(Box.createVerticalStrut(6));
        judulPanel.add(deskripsi);

        // Tombol refresh
        JButton refresh = tombol("Refresh");
        refresh.addActionListener(e -> refreshNotifikasi());

        header.add(judulPanel, BorderLayout.CENTER);
        header.add(refresh, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // ================= DAFTAR =================

    private void buatDaftar() {

        JPanel konten = new JPanel(new BorderLayout(0, 14));
        konten.setOpaque(false);

        // Filter dan jumlah notifikasi
        JPanel barFilter = new JPanel(new BorderLayout());
        barFilter.setOpaque(false);

        JPanel filterPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        btnSemua = tombol("Semua");
        btnBelumDibaca = tombol("Belum Dibaca");

        btnSemua.addActionListener(e -> {
            filter = "SEMUA";
            refreshNotifikasi();
        });

        btnBelumDibaca.addActionListener(e -> {
            filter = "BELUM";
            refreshNotifikasi();
        });

        filterPanel.add(btnSemua);
        filterPanel.add(btnBelumDibaca);

        labelJumlah = new JLabel();
        labelJumlah.setFont(Tema.font(Font.PLAIN, 13f));
        labelJumlah.setForeground(REDUP);

        barFilter.add(filterPanel, BorderLayout.WEST);
        barFilter.add(labelJumlah, BorderLayout.EAST);

        // Area daftar
        daftarNotifikasi = new JPanel();
        daftarNotifikasi.setLayout(
                new BoxLayout(daftarNotifikasi, BoxLayout.Y_AXIS));
        daftarNotifikasi.setBackground(LATAR);

        JScrollPane scroll = new JScrollPane(daftarNotifikasi);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(LATAR);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // Tombol tandai semua
        JButton tandaiSemua = tombol("Tandai Semua Dibaca");

        tandaiSemua.addActionListener(e -> {
            try {
                dao.tandaiSemuaDibaca();
                refreshNotifikasi();
            } catch (SQLException ex) {
                tampilError(ex);
            }
        });

        JPanel bawah = new JPanel(new FlowLayout(
                FlowLayout.RIGHT, 0, 0));
        bawah.setOpaque(false);
        bawah.add(tandaiSemua);

        konten.add(barFilter, BorderLayout.NORTH);
        konten.add(scroll, BorderLayout.CENTER);
        konten.add(bawah, BorderLayout.SOUTH);

        add(konten, BorderLayout.CENTER);
    }

    // ================= REFRESH =================

    private void refreshNotifikasi() {

        try {
            List<Notifikasi> semua = dao.ambilSemua();

            daftarNotifikasi.removeAll();

            int belumDibaca = dao.hitungBelumDibaca();

            labelJumlah.setText(
                    belumDibaca + " belum dibaca");

            btnSemua.setBackground(
                    filter.equals("SEMUA") ? BIRU : PUTIH);
            btnSemua.setForeground(
                    filter.equals("SEMUA") ? PUTIH : TEKS);

            btnBelumDibaca.setBackground(
                    filter.equals("BELUM") ? BIRU : PUTIH);
            btnBelumDibaca.setForeground(
                    filter.equals("BELUM") ? PUTIH : TEKS);

            int jumlahTampil = 0;

            for (Notifikasi n : semua) {

                if (filter.equals("BELUM") && n.isDibaca()) {
                    continue;
                }

                daftarNotifikasi.add(buatKartu(n));
                daftarNotifikasi.add(Box.createVerticalStrut(10));

                jumlahTampil++;
            }

            if (jumlahTampil == 0) {

                JLabel kosong = new JLabel(
                        filter.equals("BELUM")
                        ? "Tidak ada notifikasi yang belum dibaca."
                        : "Belum ada notifikasi.",
                        SwingConstants.CENTER);

                kosong.setFont(Tema.font(Font.PLAIN, 14f));
                kosong.setForeground(REDUP);

                JPanel panelKosong = new JPanel(new BorderLayout());
                panelKosong.setOpaque(false);
                panelKosong.setPreferredSize(
                        new Dimension(0, 180));

                panelKosong.add(kosong, BorderLayout.CENTER);

                daftarNotifikasi.add(panelKosong);
            }

            daftarNotifikasi.revalidate();
            daftarNotifikasi.repaint();

        } catch (SQLException ex) {
            tampilError(ex);
        }
    }

    // ================= KARTU NOTIFIKASI =================

    private JPanel buatKartu(Notifikasi n) {

        Color warna = warnaTipe(n.getTipe());

        JPanel kartu = new JPanel(new BorderLayout(14, 0));
        kartu.setBackground(PUTIH);

        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        // Ikon kategori
        JLabel ikon = new JLabel(ikonTipe(n.getTipe()));
        ikon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 23));
        ikon.setForeground(warna);
        ikon.setHorizontalAlignment(SwingConstants.CENTER);
        ikon.setPreferredSize(new Dimension(42, 42));

        JPanel ikonPanel = new JPanel(new BorderLayout());
        ikonPanel.setOpaque(false);
        ikonPanel.add(ikon, BorderLayout.NORTH);

        // Informasi notifikasi
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(
                new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel judul = new JLabel(n.getJudul());
        judul.setFont(Tema.font(Font.BOLD, 14f));
        judul.setForeground(TEKS);

        JLabel pesan = new JLabel(
                "<html><body style='width:450px'>"
                + n.getPesan()
                + "</body></html>");

        pesan.setFont(Tema.font(Font.PLAIN, 13f));
        pesan.setForeground(REDUP);

        JLabel waktu = new JLabel(n.getWaktu());
        waktu.setFont(Tema.font(Font.PLAIN, 11f));
        waktu.setForeground(REDUP);

        info.add(judul);
        info.add(Box.createVerticalStrut(7));
        info.add(pesan);
        info.add(Box.createVerticalStrut(9));
        info.add(waktu);

        // Tombol aksi
        JPanel aksi = new JPanel();
        aksi.setOpaque(false);
        aksi.setLayout(
                new BoxLayout(aksi, BoxLayout.Y_AXIS));

        if (!n.isDibaca()) {

            JLabel belum = new JLabel("● Belum dibaca");
            belum.setForeground(BIRU);
            belum.setFont(Tema.font(Font.BOLD, 11f));

            aksi.add(belum);
            aksi.add(Box.createVerticalStrut(10));
        }

        JButton hapus = tombol("Hapus");

        hapus.addActionListener(e -> {

            int pilih = JOptionPane.showConfirmDialog(
                    this,
                    "Hapus notifikasi ini?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION);

            if (pilih == JOptionPane.YES_OPTION) {
                try {
                    dao.hapus(n.getId());
                    refreshNotifikasi();
                } catch (SQLException ex) {
                    tampilError(ex);
                }
            }
        });

        aksi.add(hapus);

        kartu.add(ikonPanel, BorderLayout.WEST);
        kartu.add(info, BorderLayout.CENTER);
        kartu.add(aksi, BorderLayout.EAST);

        // Klik kartu untuk menandai sebagai dibaca
        if (!n.isDibaca()) {

            kartu.setCursor(Cursor.getPredefinedCursor(
                    Cursor.HAND_CURSOR));

            kartu.addMouseListener(
                    new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(
                        java.awt.event.MouseEvent e) {

                    try {
                        dao.tandaiDibaca(n.getId());
                        refreshNotifikasi();
                    } catch (SQLException ex) {
                        tampilError(ex);
                    }
                }
            });
        }

        kartu.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 130));

        return kartu;
    }

    // ================= UTILITAS =================

    private JButton tombol(String teks) {

        JButton b = new JButton(teks);
        b.setFont(Tema.font(Font.PLAIN, 12f));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR));

        b.setBackground(PUTIH);
        b.setForeground(TEKS);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));

        return b;
    }

    private Color warnaTipe(String tipe) {

        if (tipe == null) return BIRU;

        return switch (tipe.toUpperCase()) {
            case "SELESAI" -> new Color(0x16A34A);
            case "DEADLINE" -> new Color(0xEA580C);
            case "TERLAMBAT" -> new Color(0xDC2626);
            case "TUGAS_BARU" -> new Color(0x2563EB);
            default -> new Color(0x7C3AED);
        };
    }

    private String ikonTipe(String tipe) {

        if (tipe == null) return "●";

        return switch (tipe.toUpperCase()) {
            case "SELESAI" -> "✓";
            case "DEADLINE" -> "◷";
            case "TERLAMBAT" -> "!";
            case "TUGAS_BARU" -> "+";
            default -> "●";
        };
    }

    private void tampilError(Exception ex) {

        JOptionPane.showMessageDialog(
                this,
                "Terjadi kesalahan:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
