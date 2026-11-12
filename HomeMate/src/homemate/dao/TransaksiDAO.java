package homemate.dao;

import homemate.model.Transaksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Kode database untuk tabel transaksi dan anggaran. Milik Ketrin. */
public class TransaksiDAO {

    // ===== Transaksi =====

    public void tambah(Transaksi t) throws SQLException {
        String sql = "INSERT INTO transaksi (jenis, kategori, nominal, tanggal, keterangan) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, t);
            ps.executeUpdate();
        }
    }

    public void ubah(Transaksi t) throws SQLException {
        String sql = "UPDATE transaksi SET jenis = ?, kategori = ?, nominal = ?, tanggal = ?, keterangan = ? WHERE id = ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, t);
            ps.setInt(6, t.getId());
            ps.executeUpdate();
        }
    }

    public void hapus(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM transaksi WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Semua transaksi pada satu bulan ("yyyy-MM"), yang terbaru di atas. */
    public List<Transaksi> daftarBulan(String bulan) throws SQLException {
        YearMonth ym = YearMonth.parse(bulan);
        String sql = "SELECT id, jenis, kategori, nominal, tanggal, keterangan FROM transaksi "
                + "WHERE tanggal BETWEEN ? AND ? ORDER BY tanggal DESC, id DESC";
        List<Transaksi> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ym.atDay(1).toString());
            ps.setString(2, ym.atEndOfMonth().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new Transaksi(rs.getInt("id"), rs.getString("jenis"), rs.getString("kategori"),
                            rs.getLong("nominal"), rs.getString("tanggal"), rs.getString("keterangan")));
                }
            }
        }
        return hasil;
    }

    /** Total nominal satu jenis (PEMASUKAN / PENGELUARAN) pada satu bulan. */
    public long totalBulan(String jenis, String bulan) throws SQLException {
        YearMonth ym = YearMonth.parse(bulan);
        String sql = "SELECT COALESCE(SUM(nominal), 0) FROM transaksi WHERE jenis = ? AND tanggal BETWEEN ? AND ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, jenis);
            ps.setString(2, ym.atDay(1).toString());
            ps.setString(3, ym.atEndOfMonth().toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    /** Pengeluaran per kategori pada satu bulan (terbesar dulu): kategori -> total. */
    public Map<String, Long> pengeluaranPerKategori(String bulan) throws SQLException {
        YearMonth ym = YearMonth.parse(bulan);
        String sql = "SELECT kategori, SUM(nominal) AS total FROM transaksi "
                + "WHERE jenis = 'PENGELUARAN' AND tanggal BETWEEN ? AND ? "
                + "GROUP BY kategori ORDER BY total DESC";
        Map<String, Long> hasil = new LinkedHashMap<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ym.atDay(1).toString());
            ps.setString(2, ym.atEndOfMonth().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.put(rs.getString("kategori"), rs.getLong("total"));
                }
            }
        }
        return hasil;
    }

    // ===== Anggaran =====

    /** Anggaran satu bulan ("yyyy-MM"). Kembalikan 0 kalau belum diatur. */
    public long ambilAnggaran(String bulan) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT jumlah FROM anggaran WHERE bulan = ?")) {
            ps.setString(1, bulan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    /** Simpan anggaran bulan itu: baru kalau belum ada, ganti kalau sudah ada. */
    public void simpanAnggaran(String bulan, long jumlah) throws SQLException {
        String sql = "INSERT INTO anggaran (bulan, jumlah) VALUES (?, ?) "
                + "ON CONFLICT(bulan) DO UPDATE SET jumlah = excluded.jumlah";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, bulan);
            ps.setLong(2, jumlah);
            ps.executeUpdate();
        }
    }

    public void hapusAnggaran(String bulan) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM anggaran WHERE bulan = ?")) {
            ps.setString(1, bulan);
            ps.executeUpdate();
        }
    }

    private void isiParameter(PreparedStatement ps, Transaksi t) throws SQLException {
        ps.setString(1, t.getJenis());
        ps.setString(2, t.getKategori());
        ps.setLong(3, t.getNominal());
        ps.setString(4, t.getTanggal());
        ps.setString(5, t.getKeterangan());
    }
}
