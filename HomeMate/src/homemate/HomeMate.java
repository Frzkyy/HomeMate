package homemate;

import homemate.dao.Koneksi;
import homemate.panel.LoginFrame;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Titik mulai aplikasi: siapkan database, lalu tampilkan layar login. */
public class HomeMate {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // pakai tampilan bawaan kalau gagal
        }

        try {
            Koneksi.buatTabel();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Gagal menyiapkan database:\n" + e.getMessage()
                    + "\n\nPastikan jar sqlite-jdbc sudah ditambahkan di Libraries.",
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
