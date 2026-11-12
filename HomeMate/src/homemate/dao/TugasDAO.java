package homemate.dao;
import homemate.dao.NotifikasiDAO;
import homemate.model.Tugas;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Semua SQL untuk tabel tugas. Milik Carlos.
 *
 * Fitur penanggung jawab Sistem butuh 4 kolom tambahan di tabel tugas
 * (pj_sistem, durasi_menit, mulai_pada, sisa_jeda_ms). Kolom itu ditambahkan otomatis oleh
 * {@link #siapkanKolom()} kalau belum ada, jadi database lama tetap aman.
 * Sebaiknya Said memindahkan 4 kolom ini ke Koneksi.buatTabel().
 */
public class TugasDAO {

    public static final String URUT_PRIORITAS = "Prioritas";
    public static final String URUT_TENGGAT = "Tenggat";
    public static final String URUT_TERBARU = "Terbaru";

    private static final String KOLOM = """
        SELECT t.id, t.judul, t.deskripsi, t.anggota_id, a.nama, t.prioritas, t.tenggat,
               t.status, t.pj_sistem, t.durasi_menit, t.mulai_pada, t.sisa_jeda_ms
        FROM tugas t LEFT JOIN anggota a ON a.id = t.anggota_id
        """;

    /** Tambah kolom Sistem kalau belum ada. Aman dipanggil berkali-kali. */
    public static void siapkanKolom() throws SQLException {
        try (Connection c = Koneksi.getConnection()) {
            List<String> ada = new ArrayList<>();
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(tugas)")) {
                while (rs.next()) {
                    ada.add(rs.getString("name"));
                }
            }
            try (Statement st = c.createStatement()) {
                if (!ada.contains("pj_sistem")) {
                    st.execute("ALTER TABLE tugas ADD COLUMN pj_sistem INTEGER NOT NULL DEFAULT 0");
                }
                if (!ada.contains("durasi_menit")) {
                    st.execute("ALTER TABLE tugas ADD COLUMN durasi_menit INTEGER NOT NULL DEFAULT 0");
                }
                if (!ada.contains("mulai_pada")) {
                    st.execute("ALTER TABLE tugas ADD COLUMN mulai_pada INTEGER");
                }
                if (!ada.contains("sisa_jeda_ms")) {
                    st.execute("ALTER TABLE tugas ADD COLUMN sisa_jeda_ms INTEGER");
                }
            }
        }
    }

    /**
     * Daftar tugas. cari = potongan judul (boleh kosong), urut = salah satu konstanta URUT_*.
     * Urutan dipilih dari daftar tetap, bukan disambung dari input pengguna.
     */
    public List<Tugas> semua(String cari, String urut) throws SQLException {
        String orderBy = switch (urut == null ? "" : urut) {
            case URUT_TENGGAT -> "t.tenggat IS NULL, t.tenggat, t.id";
            case URUT_TERBARU -> "t.id DESC";
            default -> "CASE t.prioritas WHEN 'TINGGI' THEN 1 WHEN 'SEDANG' THEN 2 ELSE 3 END, "
                    + "t.tenggat IS NULL, t.tenggat, t.id";
        };
        String sql = KOLOM + "WHERE LOWER(t.judul) LIKE ? ORDER BY " + orderBy;
        List<Tugas> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + (cari == null ? "" : cari.trim().toLowerCase()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(baca(rs));
                }
            }
        }
        return hasil;
    }

    public Tugas cari(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection();
                PreparedStatement ps = c.prepareStatement(KOLOM + "WHERE t.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? baca(rs) : null;
            }
        }
    }

    public int tambah(Tugas t) throws SQLException {
        String sql = """
            INSERT INTO tugas (judul, deskripsi, anggota_id, prioritas, tenggat, status,
                               pj_sistem, durasi_menit, mulai_pada)
            VALUES (?, ?, ?, ?, ?, 'BELUM', ?, ?, NULL)""";
        try (Connection c = Koneksi.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            isiParameter(ps, t);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    /** Ubah data tugas. Status dan waktu mulai tidak ikut diubah di sini. */
    public void ubah(Tugas t) throws SQLException {
        String sql = """
            UPDATE tugas SET judul = ?, deskripsi = ?, anggota_id = ?, prioritas = ?, tenggat = ?,
                             pj_sistem = ?, durasi_menit = ?
            WHERE id = ?""";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            isiParameter(ps, t);
            ps.setInt(8, t.getId());
            ps.executeUpdate();
        }
    }

    public void hapus(int id) throws SQLException {
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM tugas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Tombol Mulai: tugas Sistem yang belum dimulai jadi SEDANG dan hitung mundur dimulai sekarang.
     * @return true kalau berhasil (false kalau tugas bukan tugas Sistem atau sudah jalan)
     */
    public boolean mulai(int id, long sekarang) throws SQLException {
        String sql = "UPDATE tugas SET status = 'SEDANG', mulai_pada = ? "
                + "WHERE id = ? AND pj_sistem = 1 AND status = 'BELUM' AND durasi_menit > 0";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sekarang);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tombol Jeda: simpan sisa waktu lalu hentikan hitung mundur.
     * @return true kalau berhasil (false kalau tidak sedang berjalan atau waktunya sudah habis)
     */
    public boolean jeda(int id, long sekarang) throws SQLException {
        String sql = "UPDATE tugas SET sisa_jeda_ms = mulai_pada + durasi_menit * 60000 - ? "
                + "WHERE id = ? AND pj_sistem = 1 AND status = 'SEDANG' AND sisa_jeda_ms IS NULL "
                + "AND mulai_pada IS NOT NULL AND mulai_pada + durasi_menit * 60000 > ?";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sekarang);
            ps.setInt(2, id);
            ps.setLong(3, sekarang);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Tombol Lanjut: hitung mundur diteruskan dari sisa waktu yang tersimpan.
     * Waktu mulai digeser supaya sisa waktu sama dengan saat dijeda.
     */
    public boolean lanjut(int id, long sekarang) throws SQLException {
        String sql = "UPDATE tugas SET mulai_pada = ? - (durasi_menit * 60000 - sisa_jeda_ms), sisa_jeda_ms = NULL "
                + "WHERE id = ? AND pj_sistem = 1 AND status = 'SEDANG' AND sisa_jeda_ms IS NOT NULL";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sekarang);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Tombol Batalkan: tugas Sistem yang sedang jalan atau dijeda kembali ke BELUM. */
    public boolean batalkan(int id) throws SQLException {
        String sql = "UPDATE tugas SET status = 'BELUM', mulai_pada = NULL, sisa_jeda_ms = NULL "
                + "WHERE id = ? AND pj_sistem = 1 AND status = 'SEDANG'";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Tombol Ulangi: tugas Sistem yang sudah SELESAI dijalankan lagi dari awal dengan durasi yang sama. */
    public boolean ulangi(int id, long sekarang) throws SQLException {
        String sql = "UPDATE tugas SET status = 'SEDANG', mulai_pada = ?, sisa_jeda_ms = NULL "
                + "WHERE id = ? AND pj_sistem = 1 AND status = 'SELESAI' AND durasi_menit > 0";
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, sekarang);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Ubah status manual (untuk tugas yang dikerjakan anggota). Kembali ke BELUM menghapus waktu mulai. */
    
/** Ubah status manual dan buat notifikasi ketika tugas selesai. */
public void ubahStatus(int id, String status) throws SQLException {

    String statusLama = null;
    String judulTugas = null;

    try (Connection c = Koneksi.getConnection()) {

        // Ambil status dan judul tugas sebelumnya
        String cekSQL = "SELECT judul, status FROM tugas WHERE id = ?";

        try (PreparedStatement cek = c.prepareStatement(cekSQL)) {
            cek.setInt(1, id);

            try (ResultSet rs = cek.executeQuery()) {
                if (rs.next()) {
                    judulTugas = rs.getString("judul");
                    statusLama = rs.getString("status");
                }
            }
        }

        // Update status tugas
        String sql = """
            UPDATE tugas
            SET status = ?,
                mulai_pada = CASE WHEN ? = 'BELUM'
                THEN NULL ELSE mulai_pada END,
                sisa_jeda_ms = NULL
            WHERE id = ?
            """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    // Notifikasi hanya dibuat ketika status berubah menjadi SELESAI
    if ("SELESAI".equals(status)
            && !"SELESAI".equals(statusLama)
            && judulTugas != null) {

        NotifikasiDAO notifikasiDAO = new NotifikasiDAO();

        notifikasiDAO.tambah(
            "Tugas Selesai",
            "Tugas \"" + judulTugas
                + "\" telah berhasil diselesaikan.",
            "SELESAI",
            id,
            "TUGAS_SELESAI_" + id + "_" + System.currentTimeMillis()
        );
    }
}


    /**
     * Tandai selesai semua tugas Sistem yang hitung mundurnya sudah habis.
     * Dipanggil saat layar dibuka (menangkap yang habis ketika aplikasi ditutup) dan tiap detik.
     * Tugas yang sedang dijeda tidak ikut diselesaikan.
     * @return jumlah tugas yang baru saja selesai
     */
    
public int selesaikanYangHabis(long sekarang) throws SQLException {

    String kondisi =
        "pj_sistem = 1 AND status = 'SEDANG' "
        + "AND mulai_pada IS NOT NULL "
        + "AND sisa_jeda_ms IS NULL "
        + "AND mulai_pada + durasi_menit * 60000 <= ?";

    List<String[]> tugasSelesai = new ArrayList<>();

    try (Connection c = Koneksi.getConnection()) {

        // Ambil judul dan ID tugas sebelum diperbarui
        String cekSQL =
            "SELECT id, judul FROM tugas WHERE " + kondisi;

        try (PreparedStatement cek = c.prepareStatement(cekSQL)) {
            cek.setLong(1, sekarang);

            try (ResultSet rs = cek.executeQuery()) {
                while (rs.next()) {
                    tugasSelesai.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("judul")
                    });
                }
            }
        }

        // Ubah status menjadi SELESAI
        String updateSQL =
            "UPDATE tugas SET status = 'SELESAI' WHERE " + kondisi;

        try (PreparedStatement update = c.prepareStatement(updateSQL)) {
            update.setLong(1, sekarang);
            update.executeUpdate();
        }
    }

    // Buat notifikasi untuk setiap tugas yang selesai
    NotifikasiDAO notifikasiDAO = new NotifikasiDAO();

    for (String[] tugas : tugasSelesai) {

        int id = Integer.parseInt(tugas[0]);

        notifikasiDAO.tambah(
            "Tugas Selesai",
            "Tugas \"" + tugas[1]
                + "\" telah selesai dikerjakan oleh Sistem.",
            "SELESAI",
            id,
            "TUGAS_SELESAI_SISTEM_" + id + "_" + sekarang
        );
    }

    return tugasSelesai.size();
}


    /**
     * Daftar anggota untuk pilihan penanggung jawab: id -> nama.
     * Sementara dibaca langsung dari tabel anggota sampai AnggotaDAO (Tifa) tersedia.
     */
    public Map<Integer, String> daftarAnggota() throws SQLException {
        Map<Integer, String> hasil = new LinkedHashMap<>();
        try (Connection c = Koneksi.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT id, nama FROM anggota ORDER BY nama");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.put(rs.getInt(1), rs.getString(2));
            }
        }
        return hasil;
    }

    // ===== Bantu =====

    private void isiParameter(PreparedStatement ps, Tugas t) throws SQLException {
        ps.setString(1, t.getJudul());
        ps.setString(2, t.getDeskripsi());
        if (t.isPjSistem() || t.getAnggotaId() == null) {
            ps.setNull(3, Types.INTEGER);
        } else {
            ps.setInt(3, t.getAnggotaId());
        }
        ps.setString(4, t.getPrioritas());
        ps.setString(5, t.getTenggat());
        ps.setInt(6, t.isPjSistem() ? 1 : 0);
        ps.setInt(7, t.isPjSistem() ? t.getDurasiMenit() : 0);
    }

    private Tugas baca(ResultSet rs) throws SQLException {
        Tugas t = new Tugas();
        t.setId(rs.getInt(1));
        t.setJudul(rs.getString(2));
        t.setDeskripsi(rs.getString(3));
        int anggota = rs.getInt(4);
        t.setAnggotaId(rs.wasNull() ? null : anggota);
        t.setNamaAnggota(rs.getString(5));
        t.setPrioritas(rs.getString(6));
        t.setTenggat(rs.getString(7));
        t.setStatus(rs.getString(8));
        t.setPjSistem(rs.getInt(9) == 1);
        t.setDurasiMenit(rs.getInt(10));
        long mulai = rs.getLong(11);
        t.setMulaiPada(rs.wasNull() ? null : mulai);
        long sisa = rs.getLong(12);
        t.setSisaJeda(rs.wasNull() ? null : sisa);
        return t;
    }
}