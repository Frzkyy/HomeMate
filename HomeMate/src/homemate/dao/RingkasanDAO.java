package homemate.dao;

import homemate.util.Format;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Query BACA-SAJA untuk Dashboard dan Laporan (menggabungkan data semua modul).
 * Milik Said. Tidak mengubah data apa pun.
 */
public class RingkasanDAO {

    // ================= DASHBOARD =================

    public int hitungTugas(boolean selesai) throws SQLException {
        String sql = selesai
                ? "SELECT COUNT(*) FROM tugas WHERE status = 'SELESAI'"
                : "SELECT COUNT(*) FROM tugas WHERE status <> 'SELESAI'";
        return hitung(sql);
    }

    public int hitungStokMenipis() throws SQLException {
        return hitung("SELECT COUNT(*) FROM barang WHERE jumlah <= batas_minimum");
    }

    /** Total pengeluaran pada rentang tanggal (format yyyy-MM-dd). */
    public long totalPengeluaran(String dari, String sampai) throws SQLException {
        return totalTransaksi("PENGELUARAN", dari, sampai);
    }

    public long totalTransaksi(String jenis, String dari, String sampai) throws SQLException {
        String sql = "SELECT COALESCE(SUM(nominal), 0) FROM transaksi WHERE jenis = ? AND tanggal BETWEEN ? AND ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, jenis);
            ps.setString(2, dari);
            ps.setString(3, sampai);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    /** Tugas dengan tenggat hari ini. Kolom: judul, penanggung jawab, prioritas, status. */
    public List<String[]> tugasHariIni(LocalDate hariIni) throws SQLException {
        String sql = """
            SELECT t.judul, COALESCE(a.nama, '-'), t.prioritas, t.status
            FROM tugas t LEFT JOIN anggota a ON a.id = t.anggota_id
            WHERE t.tenggat = ?
            ORDER BY CASE t.prioritas WHEN 'TINGGI' THEN 1 WHEN 'SEDANG' THEN 2 ELSE 3 END, t.judul""";
        return daftar(sql, 4, hariIni.toString());
    }

    /** Barang yang perlu dibeli. Kolom: nama, jumlah + satuan, batas minimum. */
    public List<String[]> barangMenipis() throws SQLException {
        String sql = """
            SELECT nama, jumlah || ' ' || COALESCE(satuan, ''), batas_minimum
            FROM barang WHERE jumlah <= batas_minimum ORDER BY nama""";
        return daftar(sql, 3);
    }

    /**
     * Pengingat otomatis (dihitung dari data, tidak disimpan):
     * tugas jatuh tempo, stok menipis, perawatan 7 hari ke depan, anggaran >= 80%.
     */
    public List<String> pengingat(LocalDate hariIni) throws SQLException {
        List<String> hasil = new ArrayList<>();

        // 1. Tugas belum selesai dengan tenggat sampai besok (termasuk yang sudah lewat)
        String besok = hariIni.plusDays(1).toString();
        for (String[] r : daftar("SELECT judul, tenggat FROM tugas WHERE status <> 'SELESAI' "
                + "AND tenggat IS NOT NULL AND tenggat <= ? ORDER BY tenggat", 2, besok)) {
            int banding = r[1].compareTo(hariIni.toString());
            String kapan = banding < 0 ? "sudah lewat tenggat (" + r[1] + ")"
                    : banding == 0 ? "tenggat hari ini" : "tenggat besok";
            hasil.add("Tugas \"" + r[0] + "\" " + kapan);
        }

        // 2. Stok menipis
        for (String[] r : barangMenipis()) {
            hasil.add("Stok " + r[0].trim() + " menipis (sisa " + r[1].trim() + ", minimum " + r[2] + ")");
        }

        // 3. Perawatan dalam 7 hari ke depan (atau sudah lewat dan belum selesai)
        String tujuhHari = hariIni.plusDays(7).toString();
        for (String[] r : daftar("SELECT nama, tanggal FROM perawatan WHERE status = 'TERJADWAL' "
                + "AND tanggal <= ? ORDER BY tanggal", 2, tujuhHari)) {
            hasil.add("Perawatan \"" + r[0] + "\" dijadwalkan " + r[1]);
        }

        // 4. Pengeluaran bulan ini sudah >= 80% anggaran
        YearMonth bulan = YearMonth.from(hariIni);
        long anggaran = 0;
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT jumlah FROM anggaran WHERE bulan = ?")) {
            ps.setString(1, bulan.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    anggaran = rs.getLong(1);
                }
            }
        }
        if (anggaran > 0) {
            long keluar = totalPengeluaran(bulan.atDay(1).toString(), bulan.atEndOfMonth().toString());
            if (keluar * 100 >= anggaran * 80) {
                hasil.add("Pengeluaran bulan ini " + Format.rupiah(keluar) + " sudah "
                        + (keluar * 100 / anggaran) + "% dari anggaran " + Format.rupiah(anggaran));
            }
        }
        return hasil;
    }

    // ================= LAPORAN =================

    /** Tugas dengan tenggat di rentang. Kolom: judul, penanggung jawab, prioritas, tenggat, status. */
    public List<String[]> laporanTugas(String dari, String sampai) throws SQLException {
        String sql = """
            SELECT t.judul, COALESCE(a.nama, '-'), t.prioritas, COALESCE(t.tenggat, '-'), t.status
            FROM tugas t LEFT JOIN anggota a ON a.id = t.anggota_id
            WHERE t.tenggat BETWEEN ? AND ?
            ORDER BY t.tenggat, t.judul""";
        return daftar(sql, 5, dari, sampai);
    }

    /** Kondisi stok saat ini. Kolom: nama, jumlah, satuan, batas minimum, status. */
    public List<String[]> laporanPersediaan() throws SQLException {
        String sql = """
            SELECT nama, jumlah, COALESCE(satuan, '-'), batas_minimum,
                   CASE WHEN jumlah <= batas_minimum THEN 'Menipis' ELSE 'Aman' END
            FROM barang ORDER BY nama""";
        return daftar(sql, 5);
    }

    /** Total per jenis + kategori. Kolom: jenis, kategori, total (angka mentah). */
    public List<String[]> keuanganPerKategori(String dari, String sampai) throws SQLException {
        String sql = """
            SELECT jenis, kategori, SUM(nominal)
            FROM transaksi WHERE tanggal BETWEEN ? AND ?
            GROUP BY jenis, kategori ORDER BY jenis, SUM(nominal) DESC""";
        return daftar(sql, 3, dari, sampai);
    }

    /** Perawatan di rentang. Kolom: nama, lokasi, tanggal, status. */
    public List<String[]> laporanPerawatan(String dari, String sampai) throws SQLException {
        String sql = """
            SELECT nama, COALESCE(lokasi, '-'), tanggal, status
            FROM perawatan WHERE tanggal BETWEEN ? AND ?
            ORDER BY tanggal, nama""";
        return daftar(sql, 4, dari, sampai);
    }

    // ================= BANTUAN =================

    private int hitung(String sql) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Jalankan query dan ubah tiap baris jadi array teks sepanjang jumlahKolom. */
    private List<String[]> daftar(String sql, int jumlahKolom, String... parameter) throws SQLException {
        List<String[]> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < parameter.length; i++) {
                ps.setString(i + 1, parameter[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] baris = new String[jumlahKolom];
                    for (int k = 0; k < jumlahKolom; k++) {
                        baris[k] = rs.getString(k + 1);
                    }
                    hasil.add(baris);
                }
            }
        }
        return hasil;
    }
}
