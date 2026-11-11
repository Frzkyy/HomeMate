package homemate.panel;

import homemate.MainFrame;
import homemate.dao.PenggunaDAO;
import homemate.model.Pengguna;
import homemate.util.Ikon;
import homemate.util.Preferensi;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/** Layar login (mockup halaman 1): ilustrasi di kiri, formulir di kanan. Milik Said. */
public class LoginFrame extends JFrame {

    private final JTextField txtUsername = new JTextField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            gambarPetunjuk(this, g, "Masukkan username");
        }
    };
    private final JPasswordField txtPassword = new JPasswordField() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            gambarPetunjuk(this, g, "Masukkan password");
        }
    };
    private final JCheckBox chkIngat = new JCheckBox("Ingat saya");
    private final Tema.Tombol btnMasuk = new Tema.Tombol("Masuk", Tema.Tombol.Gaya.UTAMA);
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();

    public LoginFrame() {
        super("HomeMate - Masuk");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(Ikon.gambar(Ikon.Jenis.RUMAH, 64, Tema.PRIMER));

        JPanel isi = new JPanel(new BorderLayout());
        isi.add(buatSisiKiri(), BorderLayout.WEST);
        isi.add(buatFormulir(), BorderLayout.CENTER);
        setContentPane(isi);
        setPreferredSize(new Dimension(920, 560));
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        // Isi username kalau sebelumnya dicentang "Ingat saya"
        String ingat = Preferensi.getUsernameIngat();
        if (!ingat.isEmpty()) {
            txtUsername.setText(ingat);
            chkIngat.setSelected(true);
            txtPassword.requestFocusInWindow();
        }
    }

    // ================= SISI KIRI (ilustrasi) =================

    private JPanel buatSisiKiri() {
        JPanel kiri = new Ilustrasi();
        kiri.setLayout(new BorderLayout());
        kiri.setPreferredSize(new Dimension(430, 0));

        JPanel merek = new JPanel();
        merek.setOpaque(false);
        merek.setLayout(new BoxLayout(merek, BoxLayout.Y_AXIS));
        merek.setBorder(BorderFactory.createEmptyBorder(52, 0, 0, 0));

        JLabel logo = new JLabel(Ikon.buat(Ikon.Jenis.RUMAH, 76, Tema.PRIMER));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        JLabel nama = Tema.label("HomeMate", Font.BOLD, 40f, new Color(0x1E4D8C));
        nama.setAlignmentX(CENTER_ALIGNMENT);
        JLabel tagline = Tema.label("Kelola Rumah, Hidup Lebih Teratur", Font.PLAIN, 15f, Tema.TEKS_REDUP);
        tagline.setAlignmentX(CENTER_ALIGNMENT);

        merek.add(logo);
        merek.add(Box.createVerticalStrut(8));
        merek.add(nama);
        merek.add(Box.createVerticalStrut(4));
        merek.add(tagline);
        kiri.add(merek, BorderLayout.NORTH);
        return kiri;
    }

    // ================= SISI KANAN (formulir) =================

    private JPanel buatFormulir() {
        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(340, 400));

        JLabel judul = Tema.label("Login", Font.BOLD, 30f, Tema.TEKS);
        JLabel sub = Tema.label("Masuk untuk melanjutkan ke aplikasi", Font.PLAIN, 14f, Tema.TEKS_REDUP);

        txtUsername.getAccessibleContext().setAccessibleName("Username");
        txtPassword.getAccessibleContext().setAccessibleName("Password");
        KolomIkon kolomUser = new KolomIkon(txtUsername, Ikon.Jenis.USER, null);
        JLabel mata = new JLabel(Ikon.buat(Ikon.Jenis.MATA, 20, Tema.TEKS_REDUP));
        mata.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mata.setToolTipText("Lihat / sembunyikan password");
        KolomIkon kolomPass = new KolomIkon(txtPassword, Ikon.Jenis.GEMBOK, mata);

        // Lihat / sembunyikan password
        final char bawaan = txtPassword.getEchoChar();
        mata.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean tampak = txtPassword.getEchoChar() == (char) 0;
                txtPassword.setEchoChar(tampak ? bawaan : (char) 0);
                mata.setIcon(Ikon.buat(tampak ? Ikon.Jenis.MATA : Ikon.Jenis.MATA_CORET, 20, Tema.TEKS_REDUP));
            }
        });

        chkIngat.setOpaque(false);
        chkIngat.setFont(Tema.font(Font.PLAIN, 13f));
        chkIngat.setForeground(Tema.TEKS_REDUP);
        chkIngat.setFocusPainted(false);

        btnMasuk.setFont(Tema.font(Font.BOLD, 15f));
        btnMasuk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnMasuk.setPreferredSize(new Dimension(340, 46));
        btnMasuk.addActionListener(e -> prosesLogin());
        getRootPane().setDefaultButton(btnMasuk);

        rata(judul);
        rata(sub);
        form.add(judul);
        form.add(Box.createVerticalStrut(6));
        form.add(sub);
        form.add(Box.createVerticalStrut(30));
        form.add(rata(label("Username")));
        form.add(Box.createVerticalStrut(6));
        form.add(rata(kolomUser));
        form.add(Box.createVerticalStrut(18));
        form.add(rata(label("Password")));
        form.add(Box.createVerticalStrut(6));
        form.add(rata(kolomPass));
        form.add(Box.createVerticalStrut(14));
        form.add(rata(chkIngat));
        form.add(Box.createVerticalStrut(22));
        form.add(rata(btnMasuk));

        GridBagConstraints g = new GridBagConstraints();
        kanan.add(form, g);
        return kanan;
    }

    /** Teks abu "Masukkan ..." saat kolom masih kosong. */
    private static void gambarPetunjuk(JTextField k, Graphics g0, String teks) {
        if (!k.getText().isEmpty()) {
            return;
        }
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0x94A3B8));
        g.setFont(k.getFont());
        int y = (k.getHeight() + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent()) / 2;
        g.drawString(teks, k.getInsets().left, y);
        g.dispose();
    }

    private JLabel label(String teks) {
        return Tema.label(teks, Font.BOLD, 13f, Tema.TEKS);
    }

    private <T extends JComponent> T rata(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    // ================= LOGIKA =================

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
            Preferensi.setUsernameIngat(chkIngat.isSelected() ? username : "");
            dispose();
            new MainFrame(p).setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal membaca database:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= KOMPONEN KHUSUS LOGIN =================

    /** Kolom isian dengan ikon di kiri (dan komponen opsional di kanan, misal ikon mata). */
    private static class KolomIkon extends JPanel {

        KolomIkon(JTextField isi, Ikon.Jenis jenis, JComponent kanan) {
            super(new BorderLayout(10, 0));
            setOpaque(true);
            setBackground(Color.WHITE);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(340, 46));
            setBorder(BorderFactory.createCompoundBorder(new Tema.BorderBulat(Tema.GARIS, 10),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));

            isi.setBorder(BorderFactory.createEmptyBorder());
            isi.setFont(Tema.font(Font.PLAIN, 14f));
            isi.setForeground(Tema.TEKS);
            isi.setOpaque(false);

            add(new JLabel(Ikon.buat(jenis, 20, Tema.TEKS_REDUP)), BorderLayout.WEST);
            add(isi, BorderLayout.CENTER);
            if (kanan != null) {
                add(kanan, BorderLayout.EAST);
            }

            isi.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(new Tema.BorderBulat(Tema.PRIMER, 10),
                            BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(new Tema.BorderBulat(Tema.GARIS, 10),
                            BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                    repaint();
                }
            });
        }
    }

    /** Latar langit + rumah + pohon, digambar dengan kode. */
    private static class Ilustrasi extends JPanel {

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            g.setPaint(new GradientPaint(0, 0, new Color(0xE3EEFC), 0, h, new Color(0xF7FAFF)));
            g.fillRect(0, 0, w, h);

            // awan
            g.setColor(new Color(255, 255, 255, 200));
            awan(g, w * 0.16, h * 0.40, 1.0);
            awan(g, w * 0.74, h * 0.47, 0.8);

            // tanah
            g.setColor(new Color(0xBFE3B4));
            g.fill(new Ellipse2D.Double(-w * 0.25, h * 0.80, w * 1.5, h * 0.5));
            g.setColor(new Color(0x9FD48F));
            g.fill(new Ellipse2D.Double(-w * 0.10, h * 0.86, w * 1.2, h * 0.5));

            int dasar = (int) (h * 0.89);
            int cx = w / 2;

            pohon(g, cx - 160, dasar, 1.15);
            pohon(g, cx + 165, dasar - 2, 1.0);
            rumah(g, cx, dasar);
            semak(g, cx - 108, dasar);
            semak(g, cx + 112, dasar);
            g.dispose();
        }

        private void awan(Graphics2D g, double x, double y, double s) {
            g.fill(new Ellipse2D.Double(x, y, 70 * s, 34 * s));
            g.fill(new Ellipse2D.Double(x + 18 * s, y - 16 * s, 44 * s, 40 * s));
            g.fill(new Ellipse2D.Double(x + 40 * s, y - 6 * s, 58 * s, 36 * s));
        }

        private void pohon(Graphics2D g, int x, int dasar, double s) {
            g.setColor(new Color(0x8B5E3C));
            g.fill(new RoundRectangle2D.Double(x - 4 * s, dasar - 64 * s, 8 * s, 64 * s, 4, 4));
            g.setColor(new Color(0x4FAE6A));
            g.fill(new Ellipse2D.Double(x - 30 * s, dasar - 120 * s, 60 * s, 66 * s));
            g.setColor(new Color(0x3D9A5A));
            g.fill(new Ellipse2D.Double(x - 22 * s, dasar - 92 * s, 44 * s, 48 * s));
        }

        private void semak(Graphics2D g, int x, int dasar) {
            g.setColor(new Color(0x4FAE6A));
            g.fill(new Ellipse2D.Double(x - 20, dasar - 18, 40, 24));
            g.setColor(new Color(0x3D9A5A));
            g.fill(new Ellipse2D.Double(x - 8, dasar - 22, 30, 24));
        }

        private void rumah(Graphics2D g, int cx, int dasar) {
            int lebar = 196;
            int tinggi = 116;
            int x = cx - lebar / 2;
            int y = dasar - tinggi;

            // bayangan
            g.setColor(new Color(0, 0, 0, 25));
            g.fill(new Ellipse2D.Double(x - 10, dasar - 8, lebar + 20, 18));

            // dinding
            g.setColor(new Color(0xFFF6E8));
            g.fill(new RoundRectangle2D.Double(x, y, lebar, tinggi, 6, 6));
            g.setColor(new Color(0xF0E2CC));
            g.fillRect(x, dasar - 10, lebar, 10);

            // atap
            Path2D.Double atap = new Path2D.Double();
            atap.moveTo(x - 20, y + 6);
            atap.lineTo(cx, y - 62);
            atap.lineTo(x + lebar + 20, y + 6);
            atap.closePath();
            g.setColor(new Color(0x2F6FED));
            g.fill(atap);
            Path2D.Double sisi = new Path2D.Double();
            sisi.moveTo(cx, y - 62);
            sisi.lineTo(x + lebar + 20, y + 6);
            sisi.lineTo(cx + 10, y + 6);
            sisi.closePath();
            g.setColor(new Color(0x2559C9));
            g.fill(sisi);

            // jendela atap
            g.setColor(new Color(0xBFDBFE));
            g.fill(new Ellipse2D.Double(cx - 13, y - 36, 26, 26));
            g.setColor(Color.WHITE);
            g.drawLine(cx, y - 36, cx, y - 10);
            g.drawLine(cx - 13, y - 23, cx + 13, y - 23);

            // pintu
            g.setColor(new Color(0xC98F57));
            g.fill(new RoundRectangle2D.Double(cx - 17, dasar - 68, 34, 68, 6, 6));
            g.setColor(new Color(0xFFD27A));
            g.fill(new Ellipse2D.Double(cx + 7, dasar - 36, 5, 5));

            // jendela kiri dan kanan
            jendela(g, x + 20, y + 28);
            jendela(g, x + lebar - 20 - 42, y + 28);
        }

        private void jendela(Graphics2D g, int x, int y) {
            g.setColor(new Color(0xBFDBFE));
            g.fill(new RoundRectangle2D.Double(x, y, 42, 42, 5, 5));
            g.setColor(Color.WHITE);
            g.drawLine(x + 21, y, x + 21, y + 42);
            g.drawLine(x, y + 21, x + 42, y + 21);
            g.setColor(new Color(0x93C5FD));
            g.draw(new RoundRectangle2D.Double(x, y, 42, 42, 5, 5));
        }
    }
}
