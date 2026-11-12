package homemate.dao;

import homemate.model.Perawatan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Kode database untuk tabel perawatan. Milik Riva. Semua SQL pakai PreparedStatement. */
public class PerawatanDAO {

    /** Ambil semua jadwal berstatus tertentu (TERJADWAL atau SELESAI), urut tanggal. */
    public List<Perawatan> daftar(String status) throws SQLException {
        String urut = status.equals("SELESAI") ? "DESC" : "ASC";
        String sql = "SELECT id, nama, lokasi, tanggal, status FROM perawatan "
                + "WHERE status = ? ORDER BY tanggal " + urut;
        List<Perawatan> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new Perawatan(rs.getInt("id"), rs.getString("nama"),
                            rs.getString("lokasi"), rs.getString("tanggal"), rs.getString("status")));
                }
            }
        }
        return hasil;
    }

    /** Tambah jadwal baru, status awal TERJADWAL. */
    public void tambah(Perawatan p) throws SQLException {
        String sql = "INSERT INTO perawatan (nama, lokasi, tanggal, status) VALUES (?, ?, ?, 'TERJADWAL')";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getLokasi());
            ps.setString(3, p.getTanggal());
            ps.executeUpdate();
        }
    }

    /** Ubah nama, lokasi, dan tanggal jadwal. */
    public void ubah(Perawatan p) throws SQLException {
        String sql = "UPDATE perawatan SET nama = ?, lokasi = ?, tanggal = ? WHERE id = ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNama());
            ps.setString(2, p.getLokasi());
            ps.setString(3, p.getTanggal());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        }
    }

    /** Tandai selesai: status jadi SELESAI, otomatis masuk riwayat. */
    public void tandaiSelesai(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE perawatan SET status = 'SELESAI' WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Hapus satu jadwal. */
    public void hapus(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM perawatan WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
