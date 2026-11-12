package homemate.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Memeriksa tenggat tugas lalu membuat notifikasi "Deadline Hari Ini" / "Tugas Terlambat".
 * Tiap notifikasi punya kode unik, jadi tidak dobel walau dicek berulang kali.
 */
public class PengingatDeadline {

    private final NotifikasiDAO notifikasiDAO = new NotifikasiDAO();

    public void periksaDeadline() throws SQLException {

        String sql = "SELECT id, judul, tenggat "
                + "FROM tugas "
                + "WHERE status <> 'SELESAI' "
                + "AND tenggat IS NOT NULL "
                + "AND tenggat <> ''";

        String hariIni = LocalDate.now().toString();

        // Baca dulu semuanya lalu tutup koneksi. Menulis notifikasi sambil hasil query masih
        // terbuka bisa bikin SQLite "database is locked".
        List<Object[]> tugas = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tugas.add(new Object[]{rs.getInt("id"), rs.getString("judul"), rs.getString("tenggat")});
            }
        }

        for (Object[] t : tugas) {
            int id = (Integer) t[0];
            String judul = (String) t[1];
            String tenggat = (String) t[2];

            if (tenggat.equals(hariIni)) {
                notifikasiDAO.tambah(
                        "Deadline Hari Ini",
                        "Tugas \"" + judul + "\" harus diselesaikan hari ini.",
                        "DEADLINE",
                        id,
                        "DEADLINE_HARI_INI_" + id + "_" + hariIni);
            } else if (tenggat.compareTo(hariIni) < 0) {
                notifikasiDAO.tambah(
                        "Tugas Terlambat",
                        "Tugas \"" + judul + "\" telah melewati deadline.",
                        "TERLAMBAT",
                        id,
                        "TERLAMBAT_" + id + "_" + tenggat);
            }
        }
    }
}
