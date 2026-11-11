package homemate.panel;

import homemate.dao.RingkasanDAO;
import homemate.util.Format;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/** Layar Dashboard: ringkasan semua modul. Milik Said. Data dibaca ulang tiap layar ditampilkan. */
public class DashboardPanel extends JPanel {

    private static final Color HIJAU = new Color(0x16A34A);
    private static final Color ORANYE = new Color(0xF59E0B);
    private static final Color MERAH = new Color(0xDC2626);
    private static final Color BIRU = new Color(0x2563EB);

    private final RingkasanDAO dao = new RingkasanDAO();

    private final JLabel lblSelesai = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblTertunda = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblMenipis = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblPengeluaran = new JLabel("Rp 0", SwingConstants.CENTER);

    private final DefaultTableModel modelTugas = new DefaultTableModel(
            new String[]{"Tugas", "Penanggung Jawab", "Prioritas", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultListModel<String> modelBelanja = new DefaultListModel<>();
    private final DefaultListModel<String> modelPengingat = new DefaultListModel<>();

    public DashboardPanel() {
        super(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel judul = new JLabel("Dashboard");
        judul.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(judul, BorderLayout.NORTH);

        JPanel isi = new JPanel(new BorderLayout(0, 16));

        // Empat kartu ringkasan
        JPanel kartu = new JPanel(new GridLayout(1, 4, 16, 0));
        kartu.add(buatKartu("Tugas Selesai", lblSelesai, HIJAU));
        kartu.add(buatKartu("Tugas Tertunda", lblTertunda, ORANYE));
        kartu.add(buatKartu("Stok Menipis", lblMenipis, MERAH));
        kartu.add(buatKartu("Pengeluaran Bulan Ini", lblPengeluaran, BIRU));
        isi.add(kartu, BorderLayout.NORTH);

        // Tabel tugas hari ini (kiri) + daftar belanja dan pengingat (kanan)
        JTable tabel = new JTable(modelTugas);
        tabel.setRowHeight(26);
        JPanel kiri = bungkus("Tugas Hari Ini", new JScrollPane(tabel));

        JPanel kanan = new JPanel(new GridLayout(2, 1, 0, 16));
        kanan.add(bungkus("Perlu Dibeli (Stok Menipis)", new JScrollPane(new JList<>(modelBelanja))));
        kanan.add(bungkus("Pengingat", new JScrollPane(new JList<>(modelPengingat))));

        JPanel tengah = new JPanel(new GridLayout(1, 2, 16, 0));
        tengah.add(kiri);
        tengah.add(kanan);
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

    /** Baca ulang semua data dari database. */
    public void segarkan() {
        try {
            LocalDate hariIni = LocalDate.now();
            YearMonth bulan = YearMonth.from(hariIni);

            lblSelesai.setText(String.valueOf(dao.hitungTugas(true)));
            lblTertunda.setText(String.valueOf(dao.hitungTugas(false)));
            lblMenipis.setText(String.valueOf(dao.hitungStokMenipis()));
            lblPengeluaran.setText(Format.rupiah(
                    dao.totalPengeluaran(bulan.atDay(1).toString(), bulan.atEndOfMonth().toString())));

            modelTugas.setRowCount(0);
            for (String[] r : dao.tugasHariIni(hariIni)) {
                modelTugas.addRow(r);
            }

            modelBelanja.clear();
            for (String[] r : dao.barangMenipis()) {
                modelBelanja.addElement(r[0] + " (sisa " + r[1].trim() + ")");
            }
            if (modelBelanja.isEmpty()) {
                modelBelanja.addElement("Semua stok aman.");
            }

            modelPengingat.clear();
            List<String> pengingat = dao.pengingat(hariIni);
            for (String p : pengingat) {
                modelPengingat.addElement(p);
            }
            if (pengingat.isEmpty()) {
                modelPengingat.addElement("Tidak ada pengingat.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat dashboard:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buatKartu(String teks, JLabel angka, Color warna) {
        JPanel k = new JPanel(new BorderLayout());
        k.setBackground(Color.WHITE);
        k.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        JLabel judul = new JLabel(teks, SwingConstants.CENTER);
        judul.setForeground(Color.GRAY);
        angka.setFont(new Font("SansSerif", Font.BOLD, 26));
        angka.setForeground(warna);
        k.add(judul, BorderLayout.NORTH);
        k.add(angka, BorderLayout.CENTER);
        k.setPreferredSize(new Dimension(0, 100));
        return k;
    }

    private JPanel bungkus(String judul, JScrollPane isi) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        JLabel l = new JLabel(judul);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        p.add(l, BorderLayout.NORTH);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }
}
