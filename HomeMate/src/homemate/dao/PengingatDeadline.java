
package homemate.dao;
import java.sql.*;

public class PengingatDeadline {

    private final NotifikasiDAO notifikasiDAO =
            new NotifikasiDAO();

    public void periksaDeadline() throws SQLException {

        String sql = "SELECT id, judul, tenggat, status "
                + "FROM tugas "
                + "WHERE status != 'SELESAI' "
                + "AND tenggat IS NOT NULL "
                + "AND tenggat != ''";

        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String judul = rs.getString("judul");
                String tenggat = rs.getString("tenggat");

                String hariIni = new java.text.SimpleDateFormat(
                        "yyyy-MM-dd").format(new java.util.Date());

                // Tenggat hari ini
                if (tenggat.startsWith(hariIni)) {
                    notifikasiDAO.tambah(
                            "Deadline Hari Ini",
                            "Tugas \"" + judul
                            + "\" harus diselesaikan hari ini.",
                            "DEADLINE",
                            id,
                            "DEADLINE_HARI_INI_" + id + "_" + hariIni
                    );
                }

                // Tenggat sudah lewat
                else if (tenggat.compareTo(hariIni) < 0) {
                    notifikasiDAO.tambah(
                            "Tugas Terlambat",
                            "Tugas \"" + judul
                            + "\" telah melewati deadline.",
                            "TERLAMBAT",
                            id,
                            "TERLAMBAT_" + id + "_" + tenggat
                    );
                }
            }
        }
    }
}
