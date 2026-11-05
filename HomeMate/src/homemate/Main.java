package homemate;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.mycompany.mavenproject2.Dashboard;
import com.mycompany.mavenproject2.Pengaturan_GUI;
import com.mycompany.mavenproject2.lihatAktivitas_GUI;

public class Main extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private ScalePane scalePane;
    private Dimension capSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    public double getScale() {
        return scalePane.getScale();
    }

    public Main() {
        setTitle("HomeMate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(900, 600));
        setSize(Math.min(1200, (int) (screen.width * 0.9)), Math.min(760, (int) (screen.height * 0.9)));
        setLocationRelativeTo(null);

        Dashboard dashboard = new Dashboard();
        lihatAktivitas_GUI aktivitas = new lihatAktivitas_GUI();
        keuangan keuanganPanel = new keuangan();
        Pengaturan_GUI pengaturan = new Pengaturan_GUI();

        dashboard.hideLocalNavigation();
        aktivitas.hideLocalNavigation();
        pengaturan.hideLocalNavigation();

        content.setBackground(new Color(204, 204, 204));
        content.add(dashboard, "dashboard");
        content.add(aktivitas, "aktivitas");
        content.add(createPlaceholder("Persediaan", "Halaman persediaan belum tersedia."), "persediaan");
        content.add(createPlaceholder("Anggota", "Halaman anggota keluarga belum tersedia."), "anggota");
        content.add(keuanganPanel, "keuangan");
        content.add(pengaturan, "pengaturan");

        // Sidebar + konten dijadikan satu tampilan, lalu diskalakan seragam mengikuti ukuran jendela.
        JPanel root = new JPanel(new BorderLayout());
        root.add(createSidebar(), BorderLayout.WEST);
        // Konten dibatasi maksimal sedikit di atas ukuran desain (anti "berserakan"); sisanya abu-abu polos.
        JPanel contentHost = new JPanel(null) {
            @Override
            public void doLayout() {
                Dimension cap = capSize;
                int w = Math.min(getWidth(), cap.width), h = Math.min(getHeight(), cap.height);
                content.setBounds(0, 0, w, h);
            }
        };
        contentHost.setBackground(new Color(204, 204, 204));
        contentHost.add(content);
        root.add(contentHost, BorderLayout.CENTER);

        Dimension base = new Dimension(0, 0);
        for (java.awt.Component page : content.getComponents()) {
            Dimension d = page.getPreferredSize();
            base.width = Math.max(base.width, d.width);
            base.height = Math.max(base.height, d.height);
        }
        base.width += 114;
        base.width = Math.max(base.width, 1100);
        base.height = Math.max(base.height, 700);
        capSize = new Dimension((int) ((base.width - 114) * 1.08), (int) (base.height * 1.08));

        scalePane = new ScalePane(root, base);
        setContentPane(scalePane);
        showPage("dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new java.awt.GridBagLayout());
        sidebar.setBackground(new Color(0, 204, 255));
        sidebar.setPreferredSize(new Dimension(114, 0));
        sidebar.setMinimumSize(new Dimension(114, 0));
        sidebar.setMaximumSize(new Dimension(114, Integer.MAX_VALUE));
        sidebar.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));

        JLabel brand = new JLabel("HomeMate");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 14));

        JLabel greeting = new JLabel("Halo pengguna!");
        greeting.setForeground(Color.BLACK);
        greeting.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));

        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 8, 0);
        sidebar.add(brand, c);
        sidebar.add(greeting, c);

        // Tombol menyebar merata sepanjang tinggi sidebar; ukuran tombol tetap.
        addMenuButton(sidebar, "Dashboard", "dashboard");
        addMenuButton(sidebar, "Lihat Aktivitas", "aktivitas");
        addMenuButton(sidebar, "Persediaan", "persediaan");
        addMenuButton(sidebar, "Anggota", "anggota");
        addMenuButton(sidebar, "Keuangan", "keuangan");
        addMenuButton(sidebar, "Pengaturan", "pengaturan");
        return sidebar;
    }

    private void addMenuButton(JPanel sidebar, String label, String page) {
        JButton button = new JButton(label);
        button.setMaximumSize(new Dimension(96, 36));
        button.setPreferredSize(new Dimension(96, 36));
        button.setMinimumSize(new Dimension(96, 36));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setBackground(new Color(0, 204, 204));
        button.setForeground(new Color(51, 51, 51));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 4, 5, 4));
        button.addActionListener(e -> showPage(page));
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0;
        c.weighty = 1;
        c.anchor = java.awt.GridBagConstraints.CENTER;
        sidebar.add(button, c);
    }

    private JPanel createPlaceholder(String title, String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(204, 204, 204));
        JLabel heading = new JLabel(title, SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel description = new JLabel(message, SwingConstants.CENTER);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panel.add(heading, BorderLayout.CENTER);
        panel.add(description, BorderLayout.SOUTH);
        return panel;
    }

    private void showPage(String page) {
        cardLayout.show(content, page);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main mainFrame = new Main();
            mainFrame.setVisible(true);

            infoPopUp_HomeMate welcome = new infoPopUp_HomeMate(mainFrame, true);
            welcome.setTitle("HomeMate - Informasi Aplikasi");
            welcome.setResizable(false);
            // Popup ikut diskalakan sesuai skala jendela utama (proporsional) dan di-center.
            javax.swing.JComponent popupContent = (javax.swing.JComponent) welcome.getContentPane();
            welcome.pack();
            Dimension popupBase = popupContent.getSize();
            double k = Math.max(1.0, mainFrame.getScale());
            ScalePane popupPane = new ScalePane(popupContent, popupBase);
            popupPane.setPreferredSize(new Dimension((int) (popupBase.width * k), (int) (popupBase.height * k)));
            welcome.setContentPane(popupPane);
            welcome.pack();
            welcome.setLocationRelativeTo(mainFrame);
            welcome.setVisible(true);
        });
    }
}
