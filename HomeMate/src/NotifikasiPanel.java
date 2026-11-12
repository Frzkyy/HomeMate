
package homemate.panel;

import homemate.dao.NotifikasiDAO;
import homemate.model.Notifikasi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class NotifikasiPanel extends JPanel {

    private final NotifikasiDAO dao = new NotifikasiDAO();
    private final JPanel daftarPanel = new JPanel();
    private JLabel lblJumlah;

    private final Color BIRU = new Color(52, 120, 246);
    private final Color TEKS = new Color(35, 45, 65);
    private final Color REDUP = new Color(120, 130, 150);

    public NotifikasiPanel() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(245, 248, 253));
        setBorder(new EmptyBorder(22, 25, 22, 25));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel judul = new JLabel("Notification Center");
        judul.setFont(new Font("Segoe UI", Font.BOLD, 25));
        judul.setForeground(TEKS);

        lblJumlah = new JLabel("0 notifikasi belum dibaca");
        lblJumlah.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblJumlah.setForeground(REDUP);

        JPanel judulPanel = new JPanel();
        judulPanel.setLayout(new BoxLayout(judulPanel, BoxLayout.Y_AXIS));
        judulPanel.setOpaque(false);
        judulPanel.add(judul);
        judulPanel.add(Box.createVerticalStrut(5));
        judulPanel.add(lblJumlah);

        JButton btnSemua = new JButton("Tandai Semua Dibaca");
        btnSemua.setForeground(BIRU);
        btnSemua.setBackground(Color.WHITE);
        btnSemua.setFocusPainted(false);

        btnSemua.addActionListener(e -> {
            try {
                dao.tandaiSemuaDibaca();
                muatNotifikasi();
            } catch (SQLException ex) {
                tampilError(ex);
            }
        });

        header.add(judulPanel, BorderLayout.WEST);
        header.add(btnSemua, BorderLayout.EAST);

        daftarPanel.setLayout(new BoxLayout(daftarPanel, BoxLayout.Y_AXIS));
        daftarPanel.setBackground(new Color(245, 248, 253));

        JScrollPane scroll = new JScrollPane(daftarPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        muatNotifikasi();
    }

    public void muatNotifikasi() {
        daftarPanel.removeAll();

        try {
            List<Notifikasi> daftar = dao.ambilSemua();
            int belumDibaca = dao.hitungBelumDibaca();

            lblJumlah.setText(
                    belumDibaca + " notifikasi belum dibaca");

            if (daftar.isEmpty()) {
                JLabel kosong = new JLabel(
                        "Belum ada notifikasi.");
                kosong.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                kosong.setForeground(REDUP);
                kosong.setBorder(new EmptyBorder(30, 10, 30, 10));
                daftarPanel.add(kosong);
            }

            for (Notifikasi n : daftar) {
                daftarPanel.add(buatItem(n));
                daftarPanel.add(Box.createVerticalStrut(10));
            }

        } catch (SQLException ex) {
            tampilError(ex);
        }

        daftarPanel.revalidate();
        daftarPanel.repaint();
    }

    private JPanel buatItem(Notifikasi n) {
        JPanel item = new JPanel(new BorderLayout(15, 5));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 232, 242)),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JPanel isi = new JPanel();
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));
        isi.setOpaque(false);

        JLabel judul = new JLabel(
                (n.isDibaca() ? "" : "● ") + n.getJudul());
        judul.setFont(new Font("Segoe UI", Font.BOLD, 14));
        judul.setForeground(n.isDibaca() ? REDUP : TEKS);

        JTextArea pesan = new JTextArea(n.getPesan());
        pesan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pesan.setForeground(REDUP);
        pesan.setEditable(false);
        pesan.setOpaque(false);
        pesan.setLineWrap(true);
        pesan.setWrapStyleWord(true);
        pesan.setFocusable(false);

        JLabel waktu = new JLabel(n.getWaktu());
        waktu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        waktu.setForeground(REDUP);

        isi.add(judul);
        isi.add(Box.createVerticalStrut(7));
        isi.add(pesan);
        isi.add(Box.createVerticalStrut(7));
        isi.add(waktu);

        JPanel tombol = new JPanel();
        tombol.setLayout(new BoxLayout(tombol, BoxLayout.Y_AXIS));
        tombol.setOpaque(false);

        JButton btnHapus = new JButton("Hapus");
        btnHapus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnHapus.setForeground(new Color(220, 70, 70));

        btnHapus.addActionListener(e -> {
            try {
                dao.hapus(n.getId());
                muatNotifikasi();
            } catch (SQLException ex) {
                tampilError(ex);
            }
        });

        tombol.add(btnHapus);

        if (!n.isDibaca()) {
            JButton btnBaca = new JButton("Baca");
            btnBaca.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnBaca.setForeground(BIRU);

            btnBaca.addActionListener(e -> {
                try {
                    dao.tandaiDibaca(n.getId());
                    muatNotifikasi();
                } catch (SQLException ex) {
                    tampilError(ex);
                }
            });

            tombol.add(Box.createVerticalStrut(5));
            tombol.add(btnBaca);
        }

        item.add(isi, BorderLayout.CENTER);
        item.add(tombol, BorderLayout.EAST);

        item.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 130));

        return item;
    }

    private void tampilError(Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Notifikasi Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
