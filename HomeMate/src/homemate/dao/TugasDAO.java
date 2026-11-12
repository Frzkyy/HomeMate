package homemate.dao;

import homemate.model.Tugas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Kode database untuk tabel tugas. Milik Carlos. */
public class TugasDAO {

    /** Pilihan urutan untuk ambilSemua(). */
    public static final String URUT_PRIORITAS = "PRIORITAS";
    public static final String URUT_TENGGAT = "TENGGAT";

    // Tugas + nama penanggung jawab (LEFT JOIN supaya tugas tanpa penanggung jawab tetap tampil)
    private static final String SELECT_DASAR =
            "SELECT t.id, t.judul, t.deskripsi, t.anggota_id, a.nama AS nama_anggota, "
            + "t.prioritas, t.tenggat, t.status "
            + "FROM tugas t LEFT JOIN anggota a ON a.id = t.anggota_id ";

    // Prioritas: TINGGI dulu, kalau sama tenggat terdekat dulu (tenggat kosong paling bawah)
    private static final String ORDER_PRIORITAS =
            "ORDER BY CASE t.prioritas WHEN 'TINGGI' THEN 3 WHEN 'SEDANG' THEN 2 ELSE 1 END DESC, "
            + "t.tenggat IS NULL, t.tenggat ASC, t.id ASC";

    // Tenggat terdekat dulu (tenggat kosong paling bawah), kalau sama prioritas tertinggi dulu
    private static final String ORDER_TENGGAT =
            "ORDER BY t.tenggat IS NULL, t.tenggat ASC, "
            + "CASE t.prioritas WHEN 'TINGGI' THEN 3 WHEN 'SEDANG' THEN 2 ELSE 1 END DESC, t.id ASC";

    /**
     * Ambil daftar tugas.
     * @param kataKunci cari di judul (tidak peduli huruf besar/kecil). Null atau kosong = semua tugas.
     * @param urutan    URUT_PRIORITAS atau URUT_TENGGAT.
     */
    public List<Tugas> ambilSemua(String kataKunci, String urutan) throws SQLException {
        boolean adaKata = kataKunci != null && !kataKunci.trim().isEmpty();
        String order = URUT_TENGGAT.equals(urutan) ? ORDER_TENGGAT : ORDER_PRIORITAS;

        StringBuilder sql = new StringBuilder(SELECT_DASAR);
        if (adaKata) {
            sql.append("WHERE t.judul LIKE ? ESCAPE '\\' ");
        }
        sql.append(order);

        List<Tugas> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            if (adaKata) {
                String bersih = kataKunci.trim()
                        .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                ps.setString(1, "%" + bersih + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(dariBaris(rs));
                }
            }
        }
        return hasil;
    }

    /** Ambil satu tugas berdasarkan id. Null kalau tidak ketemu. */
    public Tugas ambilById(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_DASAR + "WHERE t.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return dariBaris(rs);
                }
            }
        }
        return null;
    }

    /** Tambah tugas baru. Id yang dibuat database diisi ke objek t. */
    public void tambah(Tugas t) throws SQLException {
        String sql = "INSERT INTO tugas (judul, deskripsi, anggota_id, prioritas, tenggat, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            isiParameter(ps, t);
            ps.setString(6, t.getStatus());
            ps.executeUpdate();
            try (ResultSet kunci = ps.getGeneratedKeys()) {
                if (kunci.next()) {
                    t.setId(kunci.getInt(1));
                }
            }
        }
    }

    /** Ubah data tugas (judul, deskripsi, penanggung jawab, prioritas, tenggat, status). */
    public void ubah(Tugas t) throws SQLException {
        String sql = "UPDATE tugas SET judul = ?, deskripsi = ?, anggota_id = ?, prioritas = ?, "
                + "tenggat = ?, status = ? WHERE id = ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, t);
            ps.setString(6, t.getStatus());
            ps.setInt(7, t.getId());
            ps.executeUpdate();
        }
    }

    /** Ubah status saja: BELUM, SEDANG, atau SELESAI. */
    public void ubahStatus(int id, String status) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tugas SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Hapus tugas. Konfirmasi ke pengguna dilakukan di panel sebelum memanggil ini. */
    public void hapus(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tugas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Daftar anggota (id -> nama) untuk dropdown penanggung jawab. BACA-SAJA.
     * Sementara dipakai sampai AnggotaDAO milik Tifa jadi, lalu bisa diganti.
     */
    public Map<Integer, String> ambilDaftarAnggota() throws SQLException {
        Map<Integer, String> hasil = new LinkedHashMap<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, nama FROM anggota ORDER BY nama");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.put(rs.getInt("id"), rs.getString("nama"));
            }
        }
        return hasil;
    }

    // ================= bantu =================

    /** Isi parameter 1-5 (judul, deskripsi, anggota_id, prioritas, tenggat). */
    private void isiParameter(PreparedStatement ps, Tugas t) throws SQLException {
        ps.setString(1, t.getJudul());
        ps.setString(2, kosongJadiNull(t.getDeskripsi()));
        if (t.getAnggotaId() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, t.getAnggotaId());
        }
        ps.setString(4, t.getPrioritas());
        ps.setString(5, kosongJadiNull(t.getTenggat()));
    }

    private Tugas dariBaris(ResultSet rs) throws SQLException {
        int anggotaId = rs.getInt("anggota_id");
        Integer idAnggota = rs.wasNull() ? null : anggotaId;
        return new Tugas(
                rs.getInt("id"),
                rs.getString("judul"),
                rs.getString("deskripsi"),
                idAnggota,
                rs.getString("nama_anggota"),
                rs.getString("prioritas"),
                rs.getString("tenggat"),
                rs.getString("status"));
    }

    private String kosongJadiNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
    
    //membuat methoide progress anggota
    
    
/** Data progres penyelesaian tugas anggota keluarga. */
public static class ProgresAnggota {
    private final String nama;
    private final int total;
    private final int selesai;

    public ProgresAnggota(String nama, int total, int selesai) {
        this.nama = nama;
        this.total = total;
        this.selesai = selesai;
    }

    public String getNama() {
        return nama;
    }

    public int getTotal() {
        return total;
    }

    public int getSelesai() {
        return selesai;
    }

    public int getPersentase() {
        return total == 0 ? 0 : (int) Math.round(
                selesai * 100.0 / total);
    }
}

/** Mengambil progres tugas setiap anggota keluarga. */
public List<ProgresAnggota> ambilProgresAnggota()
        throws SQLException {

    String sql =
        "SELECT a.nama, " +
        "COUNT(t.id) AS total, " +
        "SUM(CASE WHEN t.status = 'SELESAI' THEN 1 ELSE 0 END) AS selesai " +
        "FROM anggota a " +
        "LEFT JOIN tugas t ON t.anggota_id = a.id " +
        "GROUP BY a.id, a.nama " +
        "ORDER BY a.nama";

    List<ProgresAnggota> hasil = new ArrayList<>();

    try (Connection c = Koneksi.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            hasil.add(new ProgresAnggota(
                    rs.getString("nama"),
                    rs.getInt("total"),
                    rs.getInt("selesai")
            ));
        }
    }

    return hasil;
}

}