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
}
