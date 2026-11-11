package homemate.panel;

import homemate.MainFrame;
import homemate.dao.PenggunaDAO;
import homemate.model.Pengguna;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/** Layar login. Milik Said. */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField(18);
    private final JPasswordField txtPassword = new JPasswordField(18);
    private final JCheckBox chkLihat = new JCheckBox("Lihat password");
    private final JButton btnMasuk = new JButton("Masuk");
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();

    public LoginFrame() {
        super("HomeMate - Masuk");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel isi = new JPanel(new GridBagLayout());
        isi.setBorder(new EmptyBorder(30, 40, 30, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 4, 0);

        JLabel judul = new JLabel("HomeMate", SwingConstants.CENTER);
        judul.setFont(new Font("SansSerif", Font.BOLD, 28));
        judul.setForeground(new Color(0x1E3A5F));
        JLabel sub = new JLabel("Kelola Rumah, Hidup Lebih Teratur", SwingConstants.CENTER);
        sub.setForeground(Color.GRAY);

        isi.add(judul, g);
        g.insets = new Insets(0, 0, 20, 0);
        isi.add(sub, g);
        g.insets = new Insets(4, 0, 4, 0);
        isi.add(new JLabel("Username"), g);
        isi.add(txtUsername, g);
        isi.add(new JLabel("Password"), g);
        isi.add(txtPassword, g);
        isi.add(chkLihat, g);
        g.insets = new Insets(14, 0, 4, 0);
        btnMasuk.setBackground(new Color(0x1E3A5F));
        btnMasuk.setForeground(Color.WHITE);
        btnMasuk.setPreferredSize(new Dimension(100, 36));
        isi.add(btnMasuk, g);

        // Lihat / sembunyikan password
        char bawaan = txtPassword.getEchoChar();
        chkLihat.addActionListener(e -> txtPassword.setEchoChar(chkLihat.isSelected() ? (char) 0 : bawaan));

        // Tekan Enter = klik Masuk
        btnMasuk.addActionListener(e -> prosesLogin());
        getRootPane().setDefaultButton(btnMasuk);

        setContentPane(isi);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void prosesLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password wajib diisi.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Pengguna p = penggunaDAO.login(username, password);
            if (p == null) {
                JOptionPane.showMessageDialog(this, "Username atau password salah.",
                        "Login Gagal", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
                return;
            }
            dispose();
            new MainFrame(p).setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal membaca database:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }
}
