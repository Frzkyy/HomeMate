
package homemate.dao;


import homemate.model.Notifikasi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotifikasiDAO {

    // Menambahkan notifikasi baru
    public void tambah(String judul, String pesan,
            String tipe, Integer tugasId, String kodeUnik)
            throws SQLException {

        String sql = "INSERT OR IGNORE INTO notifikasi "
                + "(judul, pesan, tipe, tugas_id, waktu, "
                + "dibaca, kode_unik) "
                + "VALUES (?, ?, ?, ?, datetime('now','localtime'), 0, ?)";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, judul);
            ps.setString(2, pesan);
            ps.setString(3, tipe);

            if (tugasId == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, tugasId);
            }

            ps.setString(5, kodeUnik);
            ps.executeUpdate();
        }
    }

    // Mengambil seluruh notifikasi
    public List<Notifikasi> ambilSemua() throws SQLException {
        List<Notifikasi> hasil = new ArrayList<>();

        String sql = "SELECT * FROM notifikasi "
                + "ORDER BY id DESC";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                hasil.add(buatNotifikasi(rs));
            }
        }

        return hasil;
    }

    // Menghitung notifikasi belum dibaca
    public int hitungBelumDibaca() throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifikasi "
                + "WHERE dibaca = 0";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // Menandai satu notifikasi sebagai dibaca
    public void tandaiDibaca(int id) throws SQLException {
        String sql = "UPDATE notifikasi SET dibaca = 1 "
                + "WHERE id = ?";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Menandai seluruh notifikasi sebagai dibaca
    public void tandaiSemuaDibaca() throws SQLException {
        String sql = "UPDATE notifikasi SET dibaca = 1";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    // Menghapus notifikasi
    public void hapus(int id) throws SQLException {
        String sql = "DELETE FROM notifikasi WHERE id = ?";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Notifikasi buatNotifikasi(ResultSet rs)
            throws SQLException {

        int idTugas = rs.getInt("tugas_id");
        Integer tugasId = rs.wasNull() ? null : idTugas;

        return new Notifikasi(
                rs.getInt("id"),
                rs.getString("judul"),
                rs.getString("pesan"),
                rs.getString("tipe"),
                tugasId,
                rs.getString("waktu"),
                rs.getInt("dibaca") == 1
        );
    }
}
