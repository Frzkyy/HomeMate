package homemate.dao;

import homemate.model.Anggota;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Kode database untuk tabel anggota. */
public class AnggotaDAO {

    /** Semua anggota + jumlah tugas selesai dan total tugas masing-masing. */
    public List<Anggota> semua() throws SQLException {
        String sql = """
            SELECT a.id, a.nama, a.peran,
                   COALESCE(SUM(CASE WHEN t.status = 'SELESAI' THEN 1 ELSE 0 END), 0) AS selesai,
                   COUNT(t.id) AS total
            FROM anggota a LEFT JOIN tugas t ON t.anggota_id = a.id
            GROUP BY a.id, a.nama, a.peran
            ORDER BY a.id""";
        List<Anggota> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(new Anggota(rs.getInt("id"), rs.getString("nama"), rs.getString("peran"),
                        rs.getInt("selesai"), rs.getInt("total")));
            }
        }
        return hasil;
    }

    public void tambah(Anggota a) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO anggota (nama, peran) VALUES (?, ?)")) {
            ps.setString(1, a.getNama());
            ps.setString(2, a.getPeran());
            ps.executeUpdate();
        }
    }

    public void ubah(Anggota a) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE anggota SET nama = ?, peran = ? WHERE id = ?")) {
            ps.setString(1, a.getNama());
            ps.setString(2, a.getPeran());
            ps.setInt(3, a.getId());
            ps.executeUpdate();
        }
    }

    /** Hapus anggota. Tugasnya tetap ada, penanggung jawabnya jadi kosong (ON DELETE SET NULL). */
    public void hapus(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM anggota WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Jumlah tugas milik anggota ini yang belum selesai (untuk peringatan sebelum hapus). */
    public int hitungTugasAktif(int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tugas WHERE anggota_id = ? AND status <> 'SELESAI'";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
