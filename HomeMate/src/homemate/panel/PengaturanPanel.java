package homemate.panel;

import homemate.dao.PenggunaDAO;
import homemate.model.Pengguna;
import homemate.util.Ikon;
import homemate.util.Preferensi;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Layar Pengaturan (mockup halaman 9): tab Akun, Aplikasi, Database. Milik Said. */
public class PengaturanPanel extends JPanel {

    private static final String FILE_DB = "homemate.db";

    private final Pengguna pengguna;
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();

    private final JTextField txtUsername = new JTextField();
    private final JTextField txtNama = new JTextField();
    private final JPasswordField txtLama = new JPasswordField();
    private final JPasswordField txtBaru = new JPasswordField();
    private final JPasswordField txtUlang = new JPasswordField();
    private final Tema.Saklar saklarNotif = new Tema.Saklar(true);
    private final JLabel lblDbLokasi = Tema.label(" ", Font.PLAIN, 14f, Tema.TEKS);
    private final JLabel lblDbUkuran = Tema.label(" ", Font.PLAIN, 14f, Tema.TEKS);
    private final Avatar avatar = new Avatar();

    private final CardLayout kartu = new CardLayout();
    private final JPanel isi = new JPanel(kartu);

    public PengaturanPanel(Pengguna pengguna) {
        super(new BorderLayout(0, 16));
        this.pengguna = pengguna;
        Tema.gayaHalaman(this);

        JPanel atas = new JPanel(new BorderLayout(0, 14));
        atas.setOpaque(false);
        atas.add(Tema.header("Pengaturan", "Atur akun dan aplikasi HomeMate", null), BorderLayout.NORTH);
        atas.add(new Tema.TabBar(nama -> kartu.show(isi, nama), "Akun", "Aplikasi", "Database"), BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        for (JTextField f : new JTextField[]{txtUsername, txtNama, txtLama, txtBaru, txtUlang}) {
            Tema.gayaKolom(f);
        }
        txtUsername.setEditable(false);
        txtUsername.setBackground(new Color(0xF1F5F9));

        isi.setOpaque(false);
        isi.add(buatTabAkun(), "Akun");
        isi.add(buatTabAplikasi(), "Aplikasi");
        isi.add(buatTabDatabase(), "Database");
        add(isi, BorderLayout.CENTER);

        muatData();
    }

    /** Isi ulang kolom dari data pengguna dan preferensi. */
    private void muatData() {
        txtUsername.setText(pengguna.getUsername());
        txtNama.setText(pengguna.getNama());
        txtLama.setText("");
        txtBaru.setText("");
        txtUlang.setText("");
        saklarNotif.setNyala(Preferensi.isNotifikasiAktif());
        avatar.setNama(pengguna.getNama());
        File db = new File(FILE_DB).getAbsoluteFile();
        lblDbLokasi.setText(db.getPath());
        lblDbUkuran.setText(db.exists() ? (db.length() + 1023) / 1024 + " KB" : "-");
    }

    // ================= TAB AKUN =================

    private JPanel buatTabAkun() {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(40, 0));
        k.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        int y = 0;
        y = judulBagian(form, "Informasi Akun", y);
        y = baris(form, "Username", txtUsername, y);
        y = baris(form, "Nama Pengguna", txtNama, y);
        y = judulBagian(form, "Ubah Password (kosongkan kalau tidak diganti)", y + 1);
        y = baris(form, "Password Lama", txtLama, y);
        y = baris(form, "Password Baru", txtBaru, y);
        y = baris(form, "Ulangi Password Baru", txtUlang, y);

        Tema.Tombol simpan = new Tema.Tombol("Simpan", Tema.Tombol.Gaya.UTAMA);
        simpan.addActionListener(e -> simpanAkun());
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(18, 0, 0, 0);
        form.add(simpan, g);
        g.gridy = y + 1;
        g.weighty = 1;
        form.add(new JPanel() {{ setOpaque(false); }}, g);

        // lebar form tetap 440, tingginya mengikuti isi
        JPanel formBungkus = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(440, super.getPreferredSize().height);
            }
        };
        formBungkus.setOpaque(false);
        formBungkus.add(form, BorderLayout.NORTH);
        k.add(formBungkus, BorderLayout.WEST);

        JPanel kanan = new JPanel(new GridBagLayout());
        kanan.setOpaque(false);
        GridBagConstraints gk = new GridBagConstraints();
        gk.anchor = GridBagConstraints.NORTH;
        gk.weighty = 1;
        gk.insets = new Insets(36, 0, 0, 0);
        kanan.add(avatar, gk);
        k.add(kanan, BorderLayout.CENTER);

        JPanel luar = new JPanel(new BorderLayout());
        luar.setOpaque(false);
        luar.add(k, BorderLayout.NORTH);
        return luar;
    }

    private void simpanAkun() {
        String nama = txtNama.getText().trim();
        String lama = new String(txtLama.getPassword());
        String baru = new String(txtBaru.getPassword());
        String ulang = new String(txtUlang.getPassword());

        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama pengguna wajib diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtNama.requestFocus();
            return;
        }
        boolean gantiPassword = !(lama.isEmpty() && baru.isEmpty() && ulang.isEmpty());
        if (gantiPassword) {
            if (lama.isEmpty() || baru.isEmpty() || ulang.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Untuk ganti password, isi ketiga kolom password.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (baru.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password baru minimal 4 karakter.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                txtBaru.requestFocus();
                return;
            }
            if (!baru.equals(ulang)) {
                JOptionPane.showMessageDialog(this, "Password baru dan pengulangannya tidak sama.",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                txtUlang.requestFocus();
                return;
            }
        }

        try {
            if (gantiPassword && !penggunaDAO.ubahPassword(pengguna.getId(), lama, baru)) {
                JOptionPane.showMessageDialog(this, "Password lama salah.", "Gagal", JOptionPane.ERROR_MESSAGE);
                txtLama.setText("");
                txtLama.requestFocus();
                return;
            }
            penggunaDAO.ubahNama(pengguna.getId(), nama);
            pengguna.setNama(nama);
            muatData();
            JOptionPane.showMessageDialog(this, "Perubahan akun disimpan.", "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= TAB APLIKASI =================

    private JPanel buatTabAplikasi() {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(0, 20));
        k.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        k.add(Tema.label("Preferensi Aplikasi", Font.BOLD, 16f, Tema.TEKS), BorderLayout.NORTH);

        JPanel barisNotif = new JPanel(new BorderLayout(20, 0));
        barisNotif.setOpaque(false);
        JPanel teks = new JPanel();
        teks.setOpaque(false);
        teks.setLayout(new BoxLayout(teks, BoxLayout.Y_AXIS));
        teks.add(Tema.label("Notifikasi", Font.BOLD, 14f, Tema.TEKS));
        teks.add(Tema.label("Tampilkan jumlah pengingat di ikon lonceng pada Dashboard.", Font.PLAIN, 13f, Tema.TEKS_REDUP));
        barisNotif.add(teks, BorderLayout.CENTER);
        JPanel sak = new JPanel(new GridBagLayout());
        sak.setOpaque(false);
        sak.add(saklarNotif);
        barisNotif.add(sak, BorderLayout.EAST);
        barisNotif.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GARIS));
        barisNotif.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.GARIS),
                BorderFactory.createEmptyBorder(16, 0, 0, 0)));

        Tema.Tombol simpan = new Tema.Tombol("Simpan", Tema.Tombol.Gaya.UTAMA);
        simpan.addActionListener(e -> {
            Preferensi.setNotifikasiAktif(saklarNotif.isNyala());
            JOptionPane.showMessageDialog(this, "Pengaturan aplikasi disimpan.", "Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.add(simpan, BorderLayout.WEST);

        JPanel tengah = new JPanel(new BorderLayout(0, 20));
        tengah.setOpaque(false);
        tengah.add(barisNotif, BorderLayout.NORTH);
        tengah.add(bawah, BorderLayout.CENTER);
        k.add(tengah, BorderLayout.CENTER);

        JPanel luar = new JPanel(new BorderLayout());
        luar.setOpaque(false);
        luar.add(k, BorderLayout.NORTH);
        return luar;
    }

    // ================= TAB DATABASE =================

    private JPanel buatTabDatabase() {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(0, 18));
        k.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        k.add(Tema.label("Database", Font.BOLD, 16f, Tema.TEKS), BorderLayout.NORTH);

        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        int y = 0;
        y = baris(info, "Lokasi file", lblDbLokasi, y);
        y = baris(info, "Ukuran file", lblDbUkuran, y);

        Tema.Tombol cadang = new Tema.Tombol("Cadangkan Database", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.DATABASE);
        cadang.addActionListener(e -> cadangkan());
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(16, 0, 0, 0);
        info.add(cadang, g);
        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(info, BorderLayout.NORTH);
        k.add(atas, BorderLayout.CENTER);

        JPanel luar = new JPanel(new BorderLayout());
        luar.setOpaque(false);
        luar.add(k, BorderLayout.NORTH);
        return luar;
    }

    private void cadangkan() {
        File sumber = new File(FILE_DB);
        if (!sumber.exists()) {
            JOptionPane.showMessageDialog(this, "File database tidak ditemukan.", "Gagal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JFileChooser pilih = new JFileChooser();
        pilih.setDialogTitle("Simpan cadangan database");
        pilih.setSelectedFile(new File("homemate-cadangan-" + LocalDate.now() + ".db"));
        if (pilih.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File tujuan = pilih.getSelectedFile();
        if (tujuan.exists() && JOptionPane.showConfirmDialog(this,
                "File \"" + tujuan.getName() + "\" sudah ada. Timpa?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            Files.copy(sumber.toPath(), tujuan.toPath(), StandardCopyOption.REPLACE_EXISTING);
            JOptionPane.showMessageDialog(this, "Cadangan disimpan di:\n" + tujuan.getAbsolutePath(),
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Gagal menyalin file:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================= BANTUAN TATA LETAK =================

    private int judulBagian(JPanel form, String teks, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 12, 0);
        form.add(Tema.label(teks, Font.BOLD, 16f, Tema.TEKS), g);
        return y + 1;
    }

    private int baris(JPanel form, String label, JComponent isi, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = y;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(0, 0, 4, 0);
        form.add(Tema.label(label, Font.BOLD, 13f, Tema.TEKS), g);
        g.gridy = y + 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 14, 0);
        form.add(isi, g);
        return y + 2;
    }

    /** Lingkaran berisi huruf pertama nama (pengganti foto profil). */
    private static class Avatar extends JComponent {

        private String huruf = "?";
        private String nama = "";

        Avatar() {
            setPreferredSize(new Dimension(150, 190));
        }

        void setNama(String nama) {
            this.nama = nama == null ? "" : nama;
            this.huruf = this.nama.isEmpty() ? "?" : this.nama.substring(0, 1).toUpperCase();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int d = 110;
            int x = (getWidth() - d) / 2;
            g.setColor(Tema.BIRU_MUDA);
            g.fillOval(x, 0, d, d);
            g.setColor(Tema.PRIMER);
            g.setFont(Tema.font(Font.BOLD, 46f));
            int w = g.getFontMetrics().stringWidth(huruf);
            g.drawString(huruf, (getWidth() - w) / 2, d / 2 + g.getFontMetrics().getAscent() / 2 - 4);
            g.setColor(Tema.TEKS);
            g.setFont(Tema.font(Font.BOLD, 15f));
            int wn = g.getFontMetrics().stringWidth(nama);
            g.drawString(nama, Math.max(0, (getWidth() - wn) / 2), d + 34);
            g.dispose();
        }
    }
}
