package homemate.panel;

import homemate.dao.PerawatanDAO;
import homemate.model.Perawatan;
import homemate.util.Format;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Layar Perawatan Rumah (mockup halaman 7): jadwal perawatan + riwayat. Milik Riva. */
public class PerawatanPanel extends JPanel {

    private final PerawatanDAO dao = new PerawatanDAO();

    private final DefaultTableModel modelJadwal = buatModel("No", "Nama", "Lokasi", "Jadwal Berikutnya", "Status");
    private final DefaultTableModel modelRiwayat = buatModel("No", "Nama", "Lokasi", "Tanggal", "Status");
    private final JTable tabelJadwal = new JTable(modelJadwal);
    private final JTable tabelRiwayat = new JTable(modelRiwayat);

    // Daftar data sejajar dengan baris tabel jadwal (baris ke-n = data ke-n)
    private final List<Perawatan> dataJadwal = new ArrayList<>();
    private final List<Perawatan> dataRiwayat = new ArrayList<>();

    public PerawatanPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        Tema.Tombol tombolTambah = new Tema.Tombol("Tambah Jadwal", Tema.Tombol.Gaya.UTAMA);
        tombolTambah.addActionListener(e -> tambahJadwal());
        add(Tema.header("Perawatan Rumah", "Jadwal dan riwayat perawatan rumah", tombolTambah), BorderLayout.NORTH);

        Tema.gayaTabel(tabelJadwal);
        Tema.gayaTabel(tabelRiwayat);
        tabelJadwal.getColumnModel().getColumn(4).setCellRenderer(Tema.rendererLencana());
        tabelRiwayat.getColumnModel().getColumn(4).setCellRenderer(Tema.rendererLencana());
        tabelJadwal.getColumnModel().getColumn(0).setMaxWidth(50);
        tabelRiwayat.getColumnModel().getColumn(0).setMaxWidth(50);

        // Kartu atas: jadwal + tombol aksi
        Tema.Kartu kartuJadwal = new Tema.Kartu(new BorderLayout(0, 8));
        kartuJadwal.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        kartuJadwal.add(Tema.label("Jadwal Perawatan", Font.BOLD, 16f, Tema.TEKS), BorderLayout.NORTH);
        kartuJadwal.add(Tema.gulir(tabelJadwal), BorderLayout.CENTER);

        JPanel aksi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aksi.setOpaque(false);
        Tema.Tombol tombolSelesai = new Tema.Tombol("Tandai Selesai", Tema.Tombol.Gaya.UTAMA);
        Tema.Tombol tombolUbah = new Tema.Tombol("Ubah", Tema.Tombol.Gaya.TEPI);
        Tema.Tombol tombolHapus = new Tema.Tombol("Hapus", Tema.Tombol.Gaya.BAHAYA);
        tombolSelesai.addActionListener(e -> tandaiSelesai());
        tombolUbah.addActionListener(e -> ubahJadwal());
        tombolHapus.addActionListener(e -> hapusJadwal());
        aksi.add(tombolSelesai);
        aksi.add(tombolUbah);
        aksi.add(tombolHapus);
        kartuJadwal.add(aksi, BorderLayout.SOUTH);

        // Kartu bawah: riwayat
        Tema.Kartu kartuRiwayat = new Tema.Kartu(new BorderLayout(0, 8));
        kartuRiwayat.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        kartuRiwayat.add(Tema.label("Riwayat Perawatan", Font.BOLD, 16f, Tema.TEKS), BorderLayout.NORTH);
        kartuRiwayat.add(Tema.gulir(tabelRiwayat), BorderLayout.CENTER);

        JPanel aksiRiwayat = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        aksiRiwayat.setOpaque(false);
        Tema.Tombol tombolHapusRiwayat = new Tema.Tombol("Hapus Riwayat", Tema.Tombol.Gaya.BAHAYA);
        tombolHapusRiwayat.addActionListener(e -> hapusRiwayat());
        aksiRiwayat.add(tombolHapusRiwayat);
        kartuRiwayat.add(aksiRiwayat, BorderLayout.SOUTH);

        JPanel isi = new JPanel(new GridLayout(2, 1, 0, 14));
        isi.setOpaque(false);
        isi.add(kartuJadwal);
        isi.add(kartuRiwayat);
        add(isi, BorderLayout.CENTER);

        // Muat ulang data tiap kali layar ini dibuka
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                muatData();
            }
        });
        muatData();
    }

    private static DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int baris, int kolom) {
                return false;
            }
        };
    }

    /** Baca database lalu isi ulang kedua tabel. */
    public void muatData() {
        try {
            dataJadwal.clear();
            dataJadwal.addAll(dao.daftar("TERJADWAL"));
            modelJadwal.setRowCount(0);
            int no = 1;
            for (Perawatan p : dataJadwal) {
                modelJadwal.addRow(new Object[]{no++, p.getNama(), tampilLokasi(p), Format.tampil(p.getTanggal()),
                    Format.kapital(p.getStatus())});
            }

            dataRiwayat.clear();
            dataRiwayat.addAll(dao.daftar("SELESAI"));
            modelRiwayat.setRowCount(0);
            no = 1;
            for (Perawatan p : dataRiwayat) {
                modelRiwayat.addRow(new Object[]{no++, p.getNama(), tampilLokasi(p), Format.tampil(p.getTanggal()),
                    Format.kapital(p.getStatus())});
            }
        } catch (SQLException e) {
            tampilError("Gagal memuat data perawatan:\n" + e.getMessage());
        }
    }

    private String tampilLokasi(Perawatan p) {
        return p.getLokasi() == null || p.getLokasi().isBlank() ? "-" : p.getLokasi();
    }

    // ===== Aksi tombol =====

    private void tambahJadwal() {
        Perawatan baru = formulir("Tambah Jadwal", null);
        if (baru == null) {
            return;
        }
        try {
            dao.tambah(baru);
            muatData();
        } catch (SQLException e) {
            tampilError("Gagal menambah jadwal:\n" + e.getMessage());
        }
    }

    private void ubahJadwal() {
        Perawatan dipilih = jadwalDipilih();
        if (dipilih == null) {
            return;
        }
        Perawatan hasil = formulir("Ubah Jadwal", dipilih);
        if (hasil == null) {
            return;
        }
        try {
            dao.ubah(hasil);
            muatData();
        } catch (SQLException e) {
            tampilError("Gagal mengubah jadwal:\n" + e.getMessage());
        }
    }

    private void tandaiSelesai() {
        Perawatan dipilih = jadwalDipilih();
        if (dipilih == null) {
            return;
        }
        try {
            dao.tandaiSelesai(dipilih.getId());
            muatData();
        } catch (SQLException e) {
            tampilError("Gagal menandai selesai:\n" + e.getMessage());
        }
    }

    private void hapusJadwal() {
        Perawatan dipilih = jadwalDipilih();
        if (dipilih == null) {
            return;
        }
        int yakin = JOptionPane.showConfirmDialog(this,
                "Hapus jadwal \"" + dipilih.getNama() + "\"?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (yakin != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.hapus(dipilih.getId());
            muatData();
        } catch (SQLException e) {
            tampilError("Gagal menghapus jadwal:\n" + e.getMessage());
        }
    }

    private void hapusRiwayat() {
        int baris = tabelRiwayat.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih dulu salah satu baris di tabel riwayat.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Perawatan dipilih = dataRiwayat.get(baris);
        int yakin = JOptionPane.showConfirmDialog(this,
                "Hapus riwayat \"" + dipilih.getNama() + "\"?", "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (yakin != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.hapus(dipilih.getId());
            muatData();
        } catch (SQLException e) {
            tampilError("Gagal menghapus riwayat:\n" + e.getMessage());
        }
    }

    // ===== Bantu =====

    /** Jadwal yang sedang dipilih di tabel. Kalau belum ada yang dipilih, tampilkan pesan lalu kembalikan null. */
    private Perawatan jadwalDipilih() {
        int baris = tabelJadwal.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this, "Pilih dulu salah satu jadwal di tabel.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return dataJadwal.get(baris);
    }

    /**
     * Form isian (dialog). lama == null berarti tambah baru, selain itu ubah.
     * Kembalikan Perawatan hasil isian, atau null kalau dibatalkan.
     */
    private Perawatan formulir(String judul, Perawatan lama) {
        JTextField kolomNama = new JTextField(lama == null ? "" : lama.getNama(), 22);
        JTextField kolomLokasi = new JTextField(lama == null || lama.getLokasi() == null ? "" : lama.getLokasi());
        JTextField kolomTanggal = new JTextField(lama == null ? LocalDate.now().toString() : lama.getTanggal());

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 6));
        form.add(new JLabel("Nama perawatan *"));
        form.add(kolomNama);
        form.add(new JLabel("Lokasi / peralatan"));
        form.add(kolomLokasi);
        form.add(new JLabel("Tanggal (yyyy-MM-dd) *"));
        form.add(kolomTanggal);

        while (true) {
            int pilih = JOptionPane.showConfirmDialog(this, form, judul,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (pilih != JOptionPane.OK_OPTION) {
                return null;
            }
            String nama = kolomNama.getText().trim();
            String tanggal = kolomTanggal.getText().trim();
            if (nama.isEmpty()) {
                tampilError("Nama perawatan wajib diisi.");
            } else if (Format.tanggal(tanggal) == null) {
                tampilError("Tanggal tidak valid. Pakai format yyyy-MM-dd, contoh 2026-11-20.");
            } else {
                int id = lama == null ? 0 : lama.getId();
                String status = lama == null ? "TERJADWAL" : lama.getStatus();
                return new Perawatan(id, nama, kolomLokasi.getText().trim(), tanggal, status);
            }
        }
    }

    private void tampilError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }
}
