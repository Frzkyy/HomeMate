package homemate.dao;

import homemate.model.Barang;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Kode database untuk tabel barang (persediaan dan daftar belanja). */
public class BarangDAO {

    private static final String KOLOM = "id, nama, jumlah, satuan, batas_minimum";

    /** Semua barang, urut nama. */
    public List<Barang> semua() throws SQLException {
        return ambil("SELECT " + KOLOM + " FROM barang ORDER BY nama COLLATE NOCASE");
    }

    /** Daftar belanja: barang yang Menipis (jumlah <= batas_minimum). */
    public List<Barang> menipis() throws SQLException {
        return ambil("SELECT " + KOLOM + " FROM barang WHERE jumlah <= batas_minimum ORDER BY nama COLLATE NOCASE");
    }

    private List<Barang> ambil(String sql) throws SQLException {
        List<Barang> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(new Barang(rs.getInt("id"), rs.getString("nama"), rs.getInt("jumlah"),
                        rs.getString("satuan"), rs.getInt("batas_minimum")));
            }
        }
        return hasil;
    }

    public void tambah(Barang b) throws SQLException {
        String sql = "INSERT INTO barang (nama, jumlah, satuan, batas_minimum) VALUES (?, ?, ?, ?)";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getNama());
            ps.setInt(2, b.getJumlah());
            ps.setString(3, b.getSatuan());
            ps.setInt(4, b.getBatasMinimum());
            ps.executeUpdate();
        }
    }

    public void ubah(Barang b) throws SQLException {
        String sql = "UPDATE barang SET nama = ?, jumlah = ?, satuan = ?, batas_minimum = ? WHERE id = ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getNama());
            ps.setInt(2, b.getJumlah());
            ps.setString(3, b.getSatuan());
            ps.setInt(4, b.getBatasMinimum());
            ps.setInt(5, b.getId());
            ps.executeUpdate();
        }
    }

    public void hapus(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM barang WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** "Tandai dibeli": tambahkan stok barang sebanyak jumlahBeli. */
    public void tambahStok(int id, int jumlahBeli) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE barang SET jumlah = jumlah + ? WHERE id = ?")) {
            ps.setInt(1, jumlahBeli);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Cek apakah nama barang sudah dipakai (tanpa membedakan huruf besar/kecil). kecualiId = 0 untuk barang baru. */
    public boolean namaSudahAda(String nama, int kecualiId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM barang WHERE LOWER(nama) = LOWER(?) AND id <> ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setInt(2, kecualiId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
