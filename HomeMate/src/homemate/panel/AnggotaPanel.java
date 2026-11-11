package homemate.panel;

import homemate.dao.AnggotaDAO;
import homemate.model.Anggota;
import homemate.util.Ikon;
import homemate.util.Tema;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Layar Anggota Keluarga (mockup halaman 3). Milik Tifa. */
public class AnggotaPanel extends JPanel {

    private static final int KOLOM_AKSI = 5;
    private static final int MAKS_KARTU = 4;
    private static final int MAKS_NAMA = 50;
    private static final int MAKS_PERAN = 30;
    private static final String[] PERAN = {"Ayah", "Ibu", "Anak", "Kakek", "Nenek", "Lainnya"};
    private static final Color[] WARNA_AVATAR = {
        new Color(0x2563EB), new Color(0xDB2777), new Color(0x7C3AED), new Color(0x0D9488)
    };

    private final AnggotaDAO dao = new AnggotaDAO();
    private final List<Anggota> daftar = new ArrayList<>();

    private final JPanel barisKartu = new JPanel(new GridLayout(1, MAKS_KARTU, 16, 0));
    private final DefaultTableModel modelTabel = new DefaultTableModel(
            new String[]{"No", "Nama", "Peran", "Tugas Selesai", "Total Tugas", "Aksi"}, 0) {
        @Override
        public boolean isCellEditable(int baris, int kolom) {
            return false;
        }
    };
    private final JTable tabel = new JTable(modelTabel);

    public AnggotaPanel() {
        super(new BorderLayout(0, 0));
        Tema.gayaHalaman(this);

        Tema.Tombol btnTambah = new Tema.Tombol("Tambah Anggota", Tema.Tombol.Gaya.UTAMA, Ikon.Jenis.PLUS);
        btnTambah.addActionListener(e -> tambah());

        barisKartu.setOpaque(false);
        barisKartu.setPreferredSize(new Dimension(0, 150));
        JPanel atas = new JPanel(new BorderLayout(0, 0));
        atas.setOpaque(false);
        atas.add(Tema.header("Anggota Keluarga", "Kelola anggota rumah tangga", btnTambah), BorderLayout.NORTH);
        atas.add(barisKartu, BorderLayout.CENTER);
        atas.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(atas, BorderLayout.NORTH);

        add(buatKartuTabel(), BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                segarkan(); // jumlah tugas bisa berubah dari modul Tugas
            }
        });
        segarkan();
    }

    // ===================== Tampilan =====================

    private JPanel buatKartuTabel() {
        Tema.Kartu kartu = new Tema.Kartu(new BorderLayout(0, 10));
        kartu.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));
        kartu.add(Tema.label("Daftar Anggota Keluarga", Font.BOLD, 15f, Tema.TEKS), BorderLayout.NORTH);

        Tema.gayaTabel(tabel);
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        tabel.getColumnModel().getColumn(KOLOM_AKSI).setMaxWidth(120);
        tabel.getColumnModel().getColumn(KOLOM_AKSI).setMinWidth(120);
        tabel.getColumnModel().getColumn(KOLOM_AKSI).setCellRenderer(new RendererAksi());
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int baris = tabel.rowAtPoint(e.getPoint());
                int kolom = tabel.columnAtPoint(e.getPoint());
                if (baris < 0) {
                    return;
                }
                if (kolom == KOLOM_AKSI) {
                    java.awt.Rectangle sel = tabel.getCellRect(baris, kolom, false);
                    boolean kiri = e.getX() - sel.x < sel.width / 2;
                    if (kiri) {
                        ubah(baris);
                    } else {
                        hapus(baris);
                    }
                } else if (e.getClickCount() == 2) {
                    ubah(baris);
                }
            }
        });
        kartu.add(Tema.gulir(tabel), BorderLayout.CENTER);
        return kartu;
    }

    /** Muat ulang data dari database lalu isi kartu dan tabel. */
    public void segarkan() {
        try {
            daftar.clear();
            daftar.addAll(dao.semua());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat anggota:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        isiTabel();
        isiKartu();
    }

    private void isiTabel() {
        modelTabel.setRowCount(0);
        int no = 1;
        for (Anggota a : daftar) {
            modelTabel.addRow(new Object[]{no++, a.getNama(), a.getPeran(),
                a.getTugasSelesai(), a.getTotalTugas(), ""});
        }
    }

    private void isiKartu() {
        barisKartu.removeAll();
        for (int i = 0; i < MAKS_KARTU; i++) {
            if (i < daftar.size()) {
                barisKartu.add(buatKartuAnggota(daftar.get(i), i));
            } else if (i == 0) {
                Tema.Kartu kosong = new Tema.Kartu(new GridBagLayout());
                kosong.add(Tema.label("Belum ada anggota. Klik \"Tambah Anggota\".", Font.PLAIN, 13f, Tema.TEKS_REDUP));
                barisKartu.add(kosong);
            } else {
                JPanel spasi = new JPanel();
                spasi.setOpaque(false);
                barisKartu.add(spasi);
            }
        }
        barisKartu.revalidate();
        barisKartu.repaint();
    }

    private JPanel buatKartuAnggota(Anggota a, int urutan) {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout());
        k.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        JPanel isi = new JPanel();
        isi.setOpaque(false);
        isi.setLayout(new BoxLayout(isi, BoxLayout.Y_AXIS));

        Avatar avatar = new Avatar(a.getNama(), a.getPeran(), WARNA_AVATAR[urutan % WARNA_AVATAR.length]);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        isi.add(avatar);
        isi.add(Box.createVerticalStrut(8));
        isi.add(tengah(Tema.label(a.getNama(), Font.BOLD, 15f, Tema.TEKS)));
        isi.add(tengah(Tema.label(a.getPeran(), Font.PLAIN, 13f, Tema.TEKS_REDUP)));
        isi.add(Box.createVerticalStrut(4));
        isi.add(tengah(Tema.label(a.getTugasSelesai() + " tugas selesai", Font.BOLD, 12f, Tema.HIJAU_TUA)));
        k.add(isi, BorderLayout.CENTER);
        return k;
    }

    private JLabel tengah(JLabel l) {
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    // ===================== Aksi =====================

    private void tambah() {
        Anggota baru = formAnggota("Tambah Anggota", null);
        if (baru == null) {
            return;
        }
        try {
            dao.tambah(baru);
            segarkan();
        } catch (SQLException e) {
            gagal("menyimpan", e);
        }
    }

    private void ubah(int baris) {
        Anggota lama = daftar.get(baris);
        Anggota hasil = formAnggota("Ubah Anggota", lama);
        if (hasil == null) {
            return;
        }
        hasil.setId(lama.getId());
        try {
            dao.ubah(hasil);
            segarkan();
        } catch (SQLException e) {
            gagal("menyimpan", e);
        }
    }

    private void hapus(int baris) {
        Anggota a = daftar.get(baris);
        try {
            int aktif = dao.hitungTugasAktif(a.getId());
            String pesan = "Hapus anggota \"" + a.getNama() + "\"?";
            if (aktif > 0) {
                pesan += "\n\nAnggota ini masih punya " + aktif + " tugas yang belum selesai."
                        + "\nTugasnya tetap ada, tapi penanggung jawabnya jadi kosong.";
            }
            int pilih = JOptionPane.showConfirmDialog(this, pesan, "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pilih != JOptionPane.YES_OPTION) {
                return;
            }
            dao.hapus(a.getId());
            segarkan();
        } catch (SQLException e) {
            gagal("menghapus", e);
        }
    }

    private void gagal(String aksi, SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal " + aksi + " data:\n" + e.getMessage(),
                "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Dialog isian nama + peran. Kembalikan Anggota baru, atau null kalau dibatalkan.
     * Diulang sampai isian valid.
     */
    private Anggota formAnggota(String judul, Anggota awal) {
        JTextField txtNama = new JTextField(awal == null ? "" : awal.getNama(), 22);
        Tema.gayaKolom(txtNama);
        JComboBox<String> cmbPeran = new JComboBox<>(PERAN);
        cmbPeran.setEditable(true);
        cmbPeran.setSelectedItem(awal == null ? PERAN[0] : awal.getPeran());
        cmbPeran.setFont(Tema.font(Font.PLAIN, 14f));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        form.add(Tema.label("Nama", Font.BOLD, 13f, Tema.TEKS), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(txtNama, g);
        g.gridx = 0;
        g.gridy = 1;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0;
        form.add(Tema.label("Peran", Font.BOLD, 13f, Tema.TEKS), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(cmbPeran, g);

        while (true) {
            int pilih = JOptionPane.showConfirmDialog(this, form, judul,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (pilih != JOptionPane.OK_OPTION) {
                return null;
            }
            String nama = txtNama.getText().trim();
            Object p = cmbPeran.getEditor().getItem();
            String peran = p == null ? "" : p.toString().trim();

            String error = validasi(nama, peran);
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Peringatan", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return new Anggota(awal == null ? 0 : awal.getId(), nama, peran);
        }
    }

    /** Kembalikan pesan error, atau null kalau valid. */
    private String validasi(String nama, String peran) {
        if (nama.isEmpty()) {
            return "Nama wajib diisi.";
        }
        if (nama.length() > MAKS_NAMA) {
            return "Nama maksimal " + MAKS_NAMA + " karakter.";
        }
        if (peran.isEmpty()) {
            return "Peran wajib diisi.";
        }
        if (peran.length() > MAKS_PERAN) {
            return "Peran maksimal " + MAKS_PERAN + " karakter.";
        }
        return null;
    }

    // ===================== Komponen kecil =====================

    /** Menggambar Avatar Kartun Lucu secara dinamis berdasarkan Nama & Peran. */
    private static class Avatar extends JComponent {

        private final String nama;
        private final String peran;
        private final Color warnaBg;

        Avatar(String nama, String peran, Color warnaBg) {
            this.nama = nama;
            this.peran = peran;
            this.warnaBg = warnaBg;
            setPreferredSize(new Dimension(64, 64));
            setMaximumSize(new Dimension(64, 64));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int uk = Math.min(getWidth(), getHeight());

            // 1. Background Lingkaran
            g.setColor(warnaBg);
            g.fillOval(0, 0, uk - 1, uk - 1);

            // 2. Setup Warna Karakter berdasarkan Peran
            String pr = (peran != null) ? peran.toLowerCase() : "";
            boolean isIbu = pr.contains("ibu") || pr.contains("mama") || pr.contains("nenek") || pr.contains("istri");
            boolean isAyah = pr.contains("ayah") || pr.contains("bapak") || pr.contains("kakek") || pr.contains("suami");

            Color warnaKulit = new Color(255, 224, 189);
            Color warnaRambut = isIbu ? new Color(101, 67, 33) : (isAyah ? new Color(40, 40, 40) : new Color(139, 69, 19));
            Color warnaBaju = isIbu ? new Color(244, 114, 182) : (isAyah ? new Color(59, 130, 246) : new Color(52, 211, 153));

            // 3. Gambar Baju / Badan
            g.setColor(warnaBaju);
            g.fillOval((int)(uk * 0.2), (int)(uk * 0.65), (int)(uk * 0.6), (int)(uk * 0.45));

            // 4. Gambar Kepala
            g.setColor(warnaKulit);
            g.fillOval((int)(uk * 0.25), (int)(uk * 0.2), (int)(uk * 0.5), (int)(uk * 0.5));

            // 5. Gambar Rambut
            g.setColor(warnaRambut);
            if (isIbu) {
                g.fillOval((int)(uk * 0.22), (int)(uk * 0.16), (int)(uk * 0.56), (int)(uk * 0.35));
            } else {
                g.fillArc((int)(uk * 0.23), (int)(uk * 0.16), (int)(uk * 0.54), (int)(uk * 0.38), 0, 180);
            }

            // 6. Gambar Mata Berbinar
            g.setColor(new Color(40, 40, 40));
            int mataY = (int)(uk * 0.42);
            g.fillOval((int)(uk * 0.38), mataY, (int)(uk * 0.07), (int)(uk * 0.09));
            g.fillOval((int)(uk * 0.55), mataY, (int)(uk * 0.07), (int)(uk * 0.09));

            g.setColor(Color.WHITE);
            g.fillOval((int)(uk * 0.39), mataY + 1, (int)(uk * 0.03), (int)(uk * 0.03));
            g.fillOval((int)(uk * 0.56), mataY + 1, (int)(uk * 0.03), (int)(uk * 0.03));

            // 7. Pipi Merona (Blush Lucu)
            g.setColor(new Color(255, 150, 150, 160));
            int blushY = (int)(uk * 0.48);
            g.fillOval((int)(uk * 0.34), blushY, (int)(uk * 0.09), (int)(uk * 0.05));
            g.fillOval((int)(uk * 0.57), blushY, (int)(uk * 0.09), (int)(uk * 0.05));

            // 8. Senyuman
            g.setColor(new Color(60, 60, 60));
            g.setStroke(new BasicStroke(2f));
            g.drawArc((int)(uk * 0.43), (int)(uk * 0.47), (int)(uk * 0.14), (int)(uk * 0.12), 200, 140);

            // 9. Frame Ring Putih Pinggir Avatar
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.5f));
            g.drawOval(1, 1, uk - 3, uk - 3);

            g.dispose();
        }
    }

    /** Sel kolom Aksi: ikon ubah (kiri) dan hapus (kanan). Klik ditangani di mouse listener tabel. */
    private static class RendererAksi extends DefaultTableCellRenderer {

        private final JPanel sel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        private final JLabel ubah = new JLabel(Ikon.buat(Ikon.Jenis.EDIT, 18, Tema.PRIMER));
        private final JLabel hapus = new JLabel(Ikon.buat(Ikon.Jenis.TONG, 18, Tema.MERAH));

        RendererAksi() {
            sel.add(ubah);
            sel.add(hapus);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel,
                boolean fokus, int baris, int kolom) {
            sel.setBackground(isSel ? new Color(0xEFF6FF) : Color.WHITE);
            sel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS));
            int atas = Math.max(0, (t.getRowHeight(baris) - 18) / 2 - 1);
            sel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, atas));
            return sel;
        }
    }
}