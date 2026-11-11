package homemate.panel;

import homemate.dao.RingkasanDAO;
import homemate.util.Format;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Layar Laporan: ringkasan per rentang tanggal (tugas, persediaan, keuangan, perawatan). Milik Said. */
public class LaporanPanel extends JPanel {

    private final RingkasanDAO dao = new RingkasanDAO();

    private final JTextField txtDari = new JTextField(10);
    private final JTextField txtSampai = new JTextField(10);

    private final TabLaporan tabTugas = new TabLaporan(
            "Tugas", "Judul", "Penanggung Jawab", "Prioritas", "Tenggat", "Status");
    private final TabLaporan tabPersediaan = new TabLaporan(
            "Persediaan", "Barang", "Jumlah", "Satuan", "Batas Minimum", "Status");
    private final TabLaporan tabKeuangan = new TabLaporan(
            "Keuangan", "Jenis", "Kategori", "Total");
    private final TabLaporan tabPerawatan = new TabLaporan(
            "Perawatan", "Kegiatan", "Lokasi", "Tanggal", "Status");

    public LaporanPanel() {
        super(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel judul = new JLabel("Laporan");
        judul.setFont(new Font("SansSerif", Font.BOLD, 24));

        // Filter tanggal: bawaan = bulan ini
        YearMonth bulan = YearMonth.now();
        txtDari.setText(bulan.atDay(1).toString());
        txtSampai.setText(bulan.atEndOfMonth().toString());
        JButton btnTampil = new JButton("Tampilkan");
        btnTampil.addActionListener(e -> tampilkan());

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filter.add(new JLabel("Dari (yyyy-MM-dd):"));
        filter.add(txtDari);
        filter.add(new JLabel("Sampai:"));
        filter.add(txtSampai);
        filter.add(btnTampil);

        JPanel atas = new JPanel(new BorderLayout(0, 10));
        atas.add(judul, BorderLayout.NORTH);
        atas.add(filter, BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Tugas", tabTugas);
        tab.addTab("Persediaan", tabPersediaan);
        tab.addTab("Keuangan", tabKeuangan);
        tab.addTab("Perawatan", tabPerawatan);
        add(tab, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                tampilkan();
            }
        });
        tampilkan();
    }

    /** Validasi rentang tanggal, lalu isi semua tab. */
    private void tampilkan() {
        LocalDate dari = Format.tanggal(txtDari.getText());
        LocalDate sampai = Format.tanggal(txtSampai.getText());
        if (dari == null || sampai == null) {
            JOptionPane.showMessageDialog(this, "Format tanggal harus yyyy-MM-dd, contoh 2026-11-30.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (dari.isAfter(sampai)) {
            JOptionPane.showMessageDialog(this, "Tanggal \"Dari\" tidak boleh setelah tanggal \"Sampai\".",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String d = dari.toString();
        String s = sampai.toString();

        try {
            // Tugas
            List<String[]> tugas = dao.laporanTugas(d, s);
            int selesai = 0;
            for (String[] r : tugas) {
                if ("SELESAI".equals(r[4])) {
                    selesai++;
                }
            }
            tabTugas.isi(tugas, "Total " + tugas.size() + " tugas | Selesai " + selesai
                    + " | Belum selesai " + (tugas.size() - selesai));

            // Persediaan (kondisi saat ini, tidak tergantung tanggal)
            List<String[]> barang = dao.laporanPersediaan();
            int menipis = 0;
            for (String[] r : barang) {
                if ("Menipis".equals(r[4])) {
                    menipis++;
                }
            }
            tabPersediaan.isi(barang, "Kondisi saat ini | Total " + barang.size()
                    + " barang | Menipis " + menipis);

            // Keuangan
            long masuk = dao.totalTransaksi("PEMASUKAN", d, s);
            long keluar = dao.totalTransaksi("PENGELUARAN", d, s);
            List<String[]> perKategori = dao.keuanganPerKategori(d, s);
            for (String[] r : perKategori) {
                r[2] = Format.rupiah(Long.parseLong(r[2]));
            }
            tabKeuangan.isi(perKategori, "Pemasukan " + Format.rupiah(masuk)
                    + " | Pengeluaran " + Format.rupiah(keluar)
                    + " | Saldo " + Format.rupiah(masuk - keluar));

            // Perawatan
            List<String[]> rawat = dao.laporanPerawatan(d, s);
            int rawatSelesai = 0;
            for (String[] r : rawat) {
                if ("SELESAI".equals(r[3])) {
                    rawatSelesai++;
                }
            }
            tabPerawatan.isi(rawat, "Total " + rawat.size() + " kegiatan | Selesai " + rawatSelesai
                    + " | Terjadwal " + (rawat.size() - rawatSelesai));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Satu tab: baris ringkasan di atas, tabel di bawah. */
    private static class TabLaporan extends JPanel {

        private final JLabel ringkasan = new JLabel(" ");
        private final DefaultTableModel model;

        TabLaporan(String nama, String... kolom) {
            super(new BorderLayout(0, 8));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            model = new DefaultTableModel(kolom, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            ringkasan.setFont(new Font("SansSerif", Font.BOLD, 14));
            JTable tabel = new JTable(model);
            tabel.setRowHeight(26);
            add(ringkasan, BorderLayout.NORTH);
            add(new JScrollPane(tabel), BorderLayout.CENTER);
        }

        void isi(List<String[]> baris, String teksRingkasan) {
            ringkasan.setText(teksRingkasan);
            model.setRowCount(0);
            for (String[] r : baris) {
                model.addRow(r);
            }
        }
    }
}
