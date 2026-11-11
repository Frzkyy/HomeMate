package homemate.dao;

import homemate.model.Pengguna;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Kode database untuk tabel pengguna (login). */
public class PenggunaDAO {

    /** Cek username + password. Kembalikan Pengguna kalau cocok, null kalau salah. */
    public Pengguna login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, nama FROM pengguna WHERE username = ? AND password = ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Pengguna(rs.getInt("id"), rs.getString("username"), rs.getString("nama"));
                }
            }
        }
        return null;
    }

    /** Ubah nama tampilan pengguna. */
    public void ubahNama(int id, String nama) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE pengguna SET nama = ? WHERE id = ?")) {
            ps.setString(1, nama);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Ganti password kalau password lama cocok. Kembalikan false kalau password lama salah. */
    public boolean ubahPassword(int id, String lama, String baru) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE pengguna SET password = ? WHERE id = ? AND password = ?")) {
            ps.setString(1, baru);
            ps.setInt(2, id);
            ps.setString(3, lama);
            return ps.executeUpdate() > 0;
        }
    }
}
