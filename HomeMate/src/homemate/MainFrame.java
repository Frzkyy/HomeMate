package homemate;

import homemate.model.Pengguna;
import homemate.panel.DashboardPanel;
import homemate.panel.LaporanPanel;
import homemate.panel.LoginFrame;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

/**
 * Window utama: sidebar di kiri, isi layar di kanan. Milik Said.
 * Teman-teman cukup ganti panel sementara di bagian DAFTAR LAYAR
 * dengan panel buatan mereka (contoh: new TugasPanel()), lewat Said / PR.
 */
public class MainFrame extends JFrame {

    private static final Color WARNA_SIDEBAR = new Color(0x1E3A5F);
    private static final Color WARNA_HOVER = new Color(0x2C5282);
    private static final Color WARNA_AKTIF = new Color(0x3B82F6);

    private final CardLayout kartu = new CardLayout();
    private final JPanel areaIsi = new JPanel(kartu);
    private final Map<String, JPanel> layar = new LinkedHashMap<>();
    private final List<JButton> tombolMenu = new ArrayList<>();
    private final Pengguna pengguna;

    public MainFrame(Pengguna pengguna) {
        super("HomeMate");
        this.pengguna = pengguna;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1150, 720));
        setLayout(new BorderLayout());

        // ===== DAFTAR LAYAR: ganti panel sementara dengan panel asli =====
        layar.put("Dashboard",        new DashboardPanel());               // Said
        layar.put("Anggota Keluarga", panelSementara("Anggota Keluarga")); // Tifa
        layar.put("Tugas Rumah",      panelSementara("Tugas Rumah"));      // Carlos
        layar.put("Persediaan",       panelSementara("Persediaan"));       // Devin
        layar.put("Keuangan",         panelSementara("Keuangan"));         // Ketrin
        layar.put("Perawatan Rumah",  panelSementara("Perawatan Rumah"));  // Riva
        layar.put("Laporan",          new LaporanPanel());                 // Said
        // ================================================================

        for (Map.Entry<String, JPanel> e : layar.entrySet()) {
            areaIsi.add(e.getValue(), e.getKey());
        }

        add(buatSidebar(), BorderLayout.WEST);
        add(areaIsi, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        tampilkan("Dashboard");
    }

    private JPanel buatSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(WARNA_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));

        JPanel atas = new JPanel(new GridLayout(0, 1));
        atas.setOpaque(false);
        JLabel judul = new JLabel("HomeMate", SwingConstants.CENTER);
        judul.setFont(new Font("SansSerif", Font.BOLD, 22));
        judul.setForeground(Color.WHITE);
        judul.setBorder(BorderFactory.createEmptyBorder(24, 0, 4, 0));
        JLabel sapa = new JLabel("Halo, " + pengguna.getNama(), SwingConstants.CENTER);
        sapa.setForeground(new Color(0xBFD4EE));
        sapa.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        atas.add(judul);
        atas.add(sapa);
        sidebar.add(atas, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(0, 1));
        menu.setOpaque(false);
        for (String nama : layar.keySet()) {
            JButton b = buatTombol(nama);
            b.addActionListener(e -> tampilkan(nama));
            tombolMenu.add(b);
            menu.add(b);
        }
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.add(menu, BorderLayout.NORTH);
        sidebar.add(pembungkus, BorderLayout.CENTER);

        JPanel bawah = new JPanel(new GridLayout(0, 1));
        bawah.setOpaque(false);
        JButton keluar = buatTombol("Keluar");
        keluar.addActionListener(e -> keluar());
        bawah.add(keluar);
        bawah.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        sidebar.add(bawah, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton buatTombol(String teks) {
        JButton b = new JButton(teks);
        b.setForeground(Color.WHITE);
        b.setBackground(WARNA_SIDEBAR);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 12));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        return b;
    }

    /** Pindah layar dan tandai menu yang aktif. */
    private void tampilkan(String nama) {
        kartu.show(areaIsi, nama);
        for (JButton b : tombolMenu) {
            b.setBackground(b.getText().equals(nama) ? WARNA_AKTIF : WARNA_SIDEBAR);
        }
    }

    private void keluar() {
        int pilih = JOptionPane.showConfirmDialog(this, "Yakin ingin keluar?", "Keluar",
                JOptionPane.YES_NO_OPTION);
        if (pilih == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /** Panel kosong sementara sampai pemilik modulnya selesai. */
    private JPanel panelSementara(String judul) {
        JPanel p = new JPanel(new BorderLayout());
        JLabel l = new JLabel(judul + " (belum dikerjakan)", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 18));
        p.add(l, BorderLayout.CENTER);
        return p;
    }
}
