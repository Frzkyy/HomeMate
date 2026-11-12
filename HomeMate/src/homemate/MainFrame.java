package homemate;

import homemate.model.Pengguna;
import homemate.panel.AnggotaPanel;
import homemate.panel.DashboardPanel;
import homemate.panel.LaporanPanel;
import homemate.panel.LoginFrame;
import homemate.panel.PengaturanPanel;
import homemate.panel.PengingatPanel;
import homemate.panel.PersediaanPanel;
import homemate.util.Ikon;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import homemate.panel.TugasPanel;

import homemate.panel.NotifikasiPanel;
import homemate.dao.PengingatDeadline;
import javax.swing.Timer;
import java.sql.SQLException;
import homemate.panel.KeuanganPanel;


/**
 * Window utama: sidebar biru tua di kiri, isi layar di kanan. Milik Said.
 * Teman-teman cukup ganti panel sementara di bagian DAFTAR LAYAR
 * dengan panel buatan mereka (contoh: new TugasPanel()), lewat Said / PR.
 */
public class MainFrame extends JFrame {

    private final CardLayout kartu = new CardLayout();
    private final JPanel areaIsi = new JPanel(kartu);
    private final Map<String, JPanel> layar = new LinkedHashMap<>();
    private final Map<String, ItemMenu> itemMenu = new LinkedHashMap<>();
    private final Pengguna pengguna;

    public MainFrame(Pengguna pengguna) {
        super("HomeMate");
        this.pengguna = pengguna;
        // TEST NOTIFIKASI
        
                setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                setIconImage(Ikon.gambar(Ikon.Jenis.RUMAH, 64, Tema.PRIMER));
                setPreferredSize(new Dimension(1200, 760));
                setMinimumSize(new Dimension(1000, 640));
                setLayout(new BorderLayout());

        // ===== DAFTAR LAYAR: ganti panel sementara dengan panel asli =====
        layar.put("Dashboard",        new DashboardPanel(pengguna, this::tampilkan)); // Said
        layar.put("Anggota Keluarga", new AnggotaPanel()); // Tifa
        layar.put("Tugas Rumah", new TugasPanel()); // Carlos

        layar.put("Notifikasi", new NotifikasiPanel()); //Carlos
        layar.put("Persediaan",       panelSementara("Persediaan"));       // Devin
        layar.put("Persediaan",       new PersediaanPanel());              // Devin
        layar.put("Keuangan",         new KeuanganPanel());                // Ketrin
        layar.put("Perawatan Rumah",  panelSementara("Perawatan Rumah"));  // Riva
        layar.put("Pengingat",        new PengingatPanel());               // Said
        layar.put("Laporan",          new LaporanPanel());                 // Said
        layar.put("Pengaturan",       new PengaturanPanel(pengguna));      // Said
        // ================================================================

        for (Map.Entry<String, JPanel> e : layar.entrySet()) {
            areaIsi.add(e.getValue(), e.getKey());
        }

        add(buatSidebar(), BorderLayout.WEST);
        add(areaIsi, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        tampilkan("Dashboard");
        
        // Jalankan sistem pengingat deadline
        mulaiPengingatDeadline();
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Tema.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Logo
        JPanel logo = new JPanel(new GridBagLayout());
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createEmptyBorder(26, 20, 22, 20));
        JLabel nama = new JLabel("HomeMate", Ikon.buat(Ikon.Jenis.RUMAH, 30, Color.WHITE), JLabel.LEFT);
        nama.setIconTextGap(10);
        nama.setFont(Tema.font(Font.BOLD, 21f));
        nama.setForeground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.weightx = 1;
        logo.add(nama, g);
        sidebar.add(logo, BorderLayout.NORTH);

        // Menu utama
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        for (String nm : layar.keySet()) {
            if (nm.equals("Pengaturan")) {
                continue; // Pengaturan ada di bawah
            }
            menu.add(buatItem(nm, ikonMenu(nm)));
            menu.add(Box.createVerticalStrut(4));
        }
        sidebar.add(menu, BorderLayout.CENTER);

        // Bawah: Pengaturan + Keluar
        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        bawah.setBorder(BorderFactory.createEmptyBorder(0, 12, 18, 12));
        bawah.add(buatItem("Pengaturan", Ikon.Jenis.GEAR));
        bawah.add(Box.createVerticalStrut(4));
        ItemMenu keluar = new ItemMenu("Keluar", Ikon.Jenis.KELUAR);
        keluar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                keluar();
            }
        });
        bawah.add(keluar);
        sidebar.add(bawah, BorderLayout.SOUTH);
        return sidebar;
    }

    private ItemMenu buatItem(String nama, Ikon.Jenis ikon) {
        ItemMenu item = new ItemMenu(nama, ikon);
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tampilkan(nama);
            }
        });
        itemMenu.put(nama, item);
        return item;
    }

    private Ikon.Jenis ikonMenu(String nama) {
        return switch (nama) {
            case "Dashboard" -> Ikon.Jenis.RUMAH;
            case "Anggota Keluarga" -> Ikon.Jenis.ORANG;
            case "Tugas Rumah" -> Ikon.Jenis.TUGAS;
            case "Persediaan" -> Ikon.Jenis.KOTAK;
            case "Keuangan" -> Ikon.Jenis.DOMPET;
            case "Perawatan Rumah" -> Ikon.Jenis.KUNCI_INGGRIS;
            case "Pengingat" -> Ikon.Jenis.LONCENG;
            case "Laporan" -> Ikon.Jenis.GRAFIK;
            default -> Ikon.Jenis.LINGKARAN;
        };
    }

    /** Pindah layar dan tandai menu yang aktif. Boleh dipanggil dari panel lain lewat Said. */
    public void tampilkan(String nama) {
        kartu.show(areaIsi, nama);
        for (Map.Entry<String, ItemMenu> e : itemMenu.entrySet()) {
            e.getValue().setAktif(e.getKey().equals(nama));
        }
    }

        // ======================================================
// SISTEM PENGINGAT DEADLINE OTOMATIS
// ======================================================
    private void mulaiPengingatDeadline() {

        PengingatDeadline pengingat = new PengingatDeadline();

        // Periksa deadline saat aplikasi pertama kali dibuka
        try {
            pengingat.periksaDeadline();
        } catch (Exception e) {
            System.err.println("Gagal memeriksa deadline: " + e.getMessage());
        }

        // Periksa deadline secara berkala setiap 60 detik
        Timer timer = new Timer(60_000, e -> {
            try {
                pengingat.periksaDeadline();
            } catch (Exception ex) {
                System.err.println("Gagal memeriksa deadline: " + ex.getMessage());
            }
        });

        timer.start();
    }
    
    private void keluar() {
        int pilih = JOptionPane.showConfirmDialog(this,
                "Yakin ingin keluar, " + pengguna.getNama() + "?", "Keluar",
                JOptionPane.YES_NO_OPTION);
        if (pilih == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** Layar kosong bergaya mockup sampai pemilik modulnya selesai. */
    private JPanel panelSementara(String judul) {
        JPanel p = new JPanel(new BorderLayout());
        Tema.gayaHalaman(p);
        p.add(Tema.header(judul, "Modul ini sedang dikerjakan", null), BorderLayout.NORTH);

        Tema.Kartu isi = new Tema.Kartu(new GridBagLayout());
        JPanel isiTengah = new JPanel();
        isiTengah.setOpaque(false);
        isiTengah.setLayout(new BoxLayout(isiTengah, BoxLayout.Y_AXIS));
        JLabel ikon = new JLabel(Ikon.buat(ikonMenu(judul), 48, new Color(0x94A3B8)));
        ikon.setAlignmentX(CENTER_ALIGNMENT);
        JLabel teks = Tema.label(judul + " (belum dikerjakan)", Font.PLAIN, 16f, Tema.TEKS_REDUP);
        teks.setAlignmentX(CENTER_ALIGNMENT);
        isiTengah.add(ikon);
        isiTengah.add(Box.createVerticalStrut(12));
        isiTengah.add(teks);
        isi.add(isiTengah);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }

    /** Satu baris menu di sidebar: ikon + teks, sorot saat aktif atau disentuh mouse. */
    private static class ItemMenu extends JPanel {

        private boolean aktif;
        private boolean hover;

        ItemMenu(String teks, Ikon.Jenis ikon) {
            super(new BorderLayout(12, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(0, 46));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel i = new JLabel(Ikon.buat(ikon, 20, Color.WHITE));
            JLabel t = new JLabel(teks);
            t.setFont(Tema.font(Font.PLAIN, 14f));
            t.setForeground(Color.WHITE);
            add(i, BorderLayout.WEST);
            add(t, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setAktif(boolean aktif) {
            this.aktif = aktif;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            if (aktif || hover) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(aktif ? Tema.AKTIF : Tema.SIDEBAR_HOVER);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g.dispose();
            }
            super.paintComponent(g0);
        }
    }
}
