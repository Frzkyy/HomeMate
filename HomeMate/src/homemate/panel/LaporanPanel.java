package homemate.panel;

import homemate.dao.RingkasanDAO;
import homemate.util.Format;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Layar Laporan: ringkasan per rentang tanggal (tugas, persediaan, keuangan, perawatan). Milik Said. */
public class LaporanPanel extends JPanel {

    private final RingkasanDAO dao = new RingkasanDAO();

    private final JTextField txtDari = new JTextField(9);
    private final JTextField txtSampai = new JTextField(9);

    private final TabLaporan tabTugas = new TabLaporan(new int[]{2, 4},
            "Judul", "Penanggung Jawab", "Prioritas", "Tenggat", "Status");
    private final TabLaporan tabPersediaan = new TabLaporan(new int[]{4},
            "Barang", "Jumlah", "Satuan", "Batas Minimum", "Status");
    private final TabLaporan tabKeuangan = new TabLaporan(new int[]{0},
            "Jenis", "Kategori", "Total");
    private final TabLaporan tabPerawatan = new TabLaporan(new int[]{3},
            "Kegiatan", "Lokasi", "Tanggal", "Status");

    public LaporanPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        // Filter tanggal: bawaan = bulan ini
        YearMonth bulan = YearMonth.now();
        txtDari.setText(bulan.atDay(1).toString());
        txtSampai.setText(bulan.atEndOfMonth().toString());
        Tema.gayaKolom(txtDari);
        Tema.gayaKolom(txtSampai);
        Tema.Tombol btnTampil = new Tema.Tombol("Tampilkan", Tema.Tombol.Gaya.UTAMA);
        btnTampil.addActionListener(e -> tampilkan());

        Tema.Kartu filter = new Tema.Kartu(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        filter.add(Tema.label("Dari", Font.BOLD, 13f, Tema.TEKS));
        filter.add(txtDari);
        filter.add(Tema.label("Sampai", Font.BOLD, 13f, Tema.TEKS));
        filter.add(txtSampai);
        filter.add(Tema.label("(yyyy-MM-dd)", Font.PLAIN, 12f, Tema.TEKS_REDUP));
        filter.add(btnTampil);

        JPanel atas = new JPanel(new BorderLayout(0, 0));
        atas.setOpaque(false);
        atas.add(Tema.header("Laporan", "Ringkasan tugas, persediaan, keuangan, dan perawatan", null), BorderLayout.NORTH);
        atas.add(filter, BorderLayout.CENTER);
        JPanel atasTab = new JPanel(new BorderLayout(0, 14));
        atasTab.setOpaque(false);
        atasTab.add(atas, BorderLayout.NORTH);

        CardLayout kartu = new CardLayout();
        JPanel isi = new JPanel(kartu);
        isi.setOpaque(false);
        isi.add(tabTugas, "Tugas");
        isi.add(tabPersediaan, "Persediaan");
        isi.add(tabKeuangan, "Keuangan");
        isi.add(tabPerawatan, "Perawatan");
        atasTab.add(new Tema.TabBar(nama -> kartu.show(isi, nama),
                "Tugas", "Persediaan", "Keuangan", "Perawatan"), BorderLayout.CENTER);

        add(atasTab, BorderLayout.NORTH);
        add(isi, BorderLayout.CENTER);

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
                r[2] = Format.kapital(r[2]);
                r[3] = Format.tampil(r[3]);
                r[4] = Format.kapital(r[4]);
            }
            tabTugas.isi(tugas, "Total " + tugas.size() + " tugas  |  Selesai " + selesai
                    + "  |  Belum selesai " + (tugas.size() - selesai));

            // Persediaan (kondisi saat ini, tidak tergantung tanggal)
            List<String[]> barang = dao.laporanPersediaan();
            int menipis = 0;
            for (String[] r : barang) {
                if ("Menipis".equals(r[4])) {
                    menipis++;
                }
            }
            tabPersediaan.isi(barang, "Kondisi saat ini  |  Total " + barang.size()
                    + " barang  |  Menipis " + menipis);

            // Keuangan
            long masuk = dao.totalTransaksi("PEMASUKAN", d, s);
            long keluar = dao.totalTransaksi("PENGELUARAN", d, s);
            List<String[]> perKategori = dao.keuanganPerKategori(d, s);
            for (String[] r : perKategori) {
                r[0] = Format.kapital(r[0]);
                r[2] = Format.rupiah(Long.parseLong(r[2]));
            }
            tabKeuangan.isi(perKategori, "Pemasukan " + Format.rupiah(masuk)
                    + "  |  Pengeluaran " + Format.rupiah(keluar)
                    + "  |  Saldo " + Format.rupiah(masuk - keluar));

            // Perawatan
            List<String[]> rawat = dao.laporanPerawatan(d, s);
            int rawatSelesai = 0;
            for (String[] r : rawat) {
                if ("SELESAI".equals(r[3])) {
                    rawatSelesai++;
                }
                r[2] = Format.tampil(r[2]);
                r[3] = Format.kapital(r[3]);
            }
            tabPerawatan.isi(rawat, "Total " + rawat.size() + " kegiatan  |  Selesai " + rawatSelesai
                    + "  |  Terjadwal " + (rawat.size() - rawatSelesai));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Satu tab: baris ringkasan di atas, tabel di bawah. */
    private static class TabLaporan extends JPanel {

        private final JLabel ringkasan = Tema.label(" ", Font.BOLD, 14f, Tema.BIRU_TUA);
        private final DefaultTableModel model;

        TabLaporan(int[] kolomLencana, String... kolom) {
            super(new BorderLayout(0, 12));
            setOpaque(false);
            model = new DefaultTableModel(kolom, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable tabel = new JTable(model);
            Tema.gayaTabel(tabel);
            tabel.setRowHeight(44);
            for (int k : kolomLencana) {
                tabel.getColumnModel().getColumn(k).setCellRenderer(Tema.rendererLencana());
            }

            Tema.Kartu info = new Tema.Kartu(new BorderLayout()).warna(Tema.BIRU_MUDA, null);
            info.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            info.add(ringkasan, BorderLayout.CENTER);
            add(info, BorderLayout.NORTH);

            Tema.Kartu isi = new Tema.Kartu(new BorderLayout());
            isi.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            isi.add(Tema.gulir(tabel), BorderLayout.CENTER);
            add(isi, BorderLayout.CENTER);
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
