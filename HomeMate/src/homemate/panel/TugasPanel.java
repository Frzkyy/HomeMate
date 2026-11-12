package homemate.panel;

import homemate.dao.TugasDAO;
import homemate.model.Tugas;
import homemate.util.Format;
import homemate.util.Ikon;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
/**
 * Layar Tugas Rumah (mockup halaman 4). Milik Carlos.
 * Fitur: tambah / ubah / hapus tugas, ubah status, cari berdasarkan judul,
 * urutkan berdasarkan prioritas atau tenggat. Semua SQL ada di TugasDAO.
 */
public class TugasPanel extends JPanel {

    private static final String[] NAMA_PRIORITAS = {"Rendah", "Sedang", "Tinggi"};
    private static final String[] KODE_PRIORITAS = {Tugas.PRIORITAS_RENDAH, Tugas.PRIORITAS_SEDANG, Tugas.PRIORITAS_TINGGI};
    private static final String[] NAMA_STATUS = {"Belum", "Sedang", "Selesai"};
    private static final String[] KODE_STATUS = {Tugas.STATUS_BELUM, Tugas.STATUS_SEDANG, Tugas.STATUS_SELESAI};

    private final TugasDAO dao = new TugasDAO();
    private List<Tugas> daftar = new ArrayList<>();
    // Panel progres anggota keluarga
    private final JPanel panelProgres = new JPanel();

    private final DefaultTableModel modelTabel = new DefaultTableModel(
            new String[]{"No", "Nama Tugas", "Penanggung Jawab", "Prioritas", "Tenggat", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabel = new JTable(modelTabel);
    private final JTextField txtCari = new JTextField();
    private final JComboBox<String> cmbUrut = new JComboBox<>(new String[]{"Prioritas", "Tenggat"});

    public TugasPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        // ===== Bagian atas: judul + tombol Tambah, lalu baris cari/urut/aksi =====
        Tema.Tombol btnTambah = new Tema.Tombol("Tambah Tugas", Tema.Tombol.Gaya.UTAMA, Ikon.Jenis.PLUS);
        btnTambah.addActionListener(e -> tambahTugas());

        JPanel atas = new JPanel(new BorderLayout());
        atas.setOpaque(false);
        atas.add(Tema.header("Tugas Rumah", "Kelola dan pantau tugas rumah tangga", btnTambah), BorderLayout.NORTH);
        atas.add(buatBarisAlat(), BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        // ===== Tabel =====
        Tema.gayaTabel(tabel);
        tabel.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabel.getColumnModel().getColumn(0).setMaxWidth(70);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(260);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabel.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabel.getColumnModel().getColumn(5).setPreferredWidth(110);
        tabel.getColumnModel().getColumn(3).setCellRenderer(Tema.rendererLencana());
        tabel.getColumnModel().getColumn(5).setCellRenderer(Tema.rendererLencana());
        tabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                  tampilDetailTugas();
                }
            
            }
        });

        Tema.Kartu kartu = new Tema.Kartu(new BorderLayout());
        kartu.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JScrollPane gulir = Tema.gulir(tabel);
        kartu.add(gulir, BorderLayout.CENTER);
        // Panel progres anggota
        panelProgres.setLayout(new BoxLayout(panelProgres, BoxLayout.Y_AXIS));
        panelProgres.setBackground(Color.WHITE);
        panelProgres.setBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        // Scroll panel progres
        JScrollPane scrollProgres = new JScrollPane(panelProgres);
        scrollProgres.setPreferredSize(new Dimension(230, 400));
        scrollProgres.setBorder(
                BorderFactory.createLineBorder(new Color(225, 232, 242))
        );
        scrollProgres.getVerticalScrollBar().setUnitIncrement(16);

        // Gabungkan panel progres dan tabel
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                scrollProgres,
                kartu
        );

        split.setResizeWeight(0.25);
        split.setDividerLocation(240);
        split.setDividerSize(5);
        split.setBorder(null);

        add(split, BorderLayout.CENTER);

        // ===== Pasang listener (setelah semua komponen siap) =====
        txtCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                muat();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                muat();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                muat();
            }
        });
        cmbUrut.addActionListener(e -> muat());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                muat();
            }
        });

        muat();
    }

    // ================= tampilan =================

    private JPanel buatBarisAlat() {
        JPanel baris = new JPanel(new BorderLayout(12, 0));
        baris.setOpaque(false);
        baris.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // kiri: cari + urutkan
        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        kiri.setOpaque(false);
        Tema.gayaKolom(txtCari);
        txtCari.setPreferredSize(new Dimension(240, 38));
        txtCari.setToolTipText("Cari berdasarkan judul tugas");
        kiri.add(Tema.label("Cari:", Font.BOLD, 13f, Tema.TEKS_REDUP));
        kiri.add(txtCari);
        cmbUrut.setPreferredSize(new Dimension(120, 38));
        kiri.add(Tema.label("Urutkan:", Font.BOLD, 13f, Tema.TEKS_REDUP));
        kiri.add(cmbUrut);
        baris.add(kiri, BorderLayout.WEST);

        // kanan: aksi untuk tugas yang dipilih di tabel
        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kanan.setOpaque(false);
        Tema.Tombol btnStatus = new Tema.Tombol("Ubah Status", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.CENTANG);
        Tema.Tombol btnUbah = new Tema.Tombol("Ubah", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.EDIT);
        Tema.Tombol btnHapus = new Tema.Tombol("Hapus", Tema.Tombol.Gaya.BAHAYA, Ikon.Jenis.TONG);
        btnStatus.addActionListener(e -> ubahStatus());
        btnUbah.addActionListener(e -> ubahTugas());
        btnHapus.addActionListener(e -> hapusTugas());
        kanan.add(btnStatus);
        kanan.add(btnUbah);
        kanan.add(btnHapus);
        baris.add(kanan, BorderLayout.EAST);
        return baris;
    }

    /** Muat ulang tabel dari database sesuai kata kunci dan urutan yang dipilih. */
    public void muat() {
        try {
            String urut = cmbUrut.getSelectedIndex() == 1 ? TugasDAO.URUT_TENGGAT : TugasDAO.URUT_PRIORITAS;
            daftar = dao.ambilSemua(txtCari.getText(), urut);
            modelTabel.setRowCount(0);
            int no = 1;
            for (Tugas t : daftar) {
                modelTabel.addRow(new Object[]{
                    String.valueOf(no++),
                    t.getJudul(),
                    t.getNamaAnggota() == null ? "-" : t.getNamaAnggota(),
                    Format.kapital(t.getPrioritas()),
                    Format.tampil(t.getTenggat()),
                    Format.kapital(t.getStatus())
                });
            }
            
            muatProgresAnggota();
        } catch (SQLException e) {
            kesalahan("Gagal memuat tugas", e);
        }
    }
    
    
/** Memuat progres penyelesaian tugas setiap anggota. */
private void muatProgresAnggota() {
    panelProgres.removeAll();

    JLabel judul = Tema.label(
            "PROGRES ANGGOTA",
            Font.BOLD,
            14f,
            Tema.TEKS
    );

    judul.setAlignmentX(Component.LEFT_ALIGNMENT);
    panelProgres.add(judul);
    panelProgres.add(Box.createVerticalStrut(12));

    try {
        List<TugasDAO.ProgresAnggota> progres =
                dao.ambilProgresAnggota();

        if (progres.isEmpty()) {
            JLabel kosong = Tema.label(
                    "Belum ada anggota keluarga",
                    Font.PLAIN,
                    12f,
                    Tema.TEKS_REDUP
            );

            kosong.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelProgres.add(kosong);
        }

        for (TugasDAO.ProgresAnggota p : progres) {
            panelProgres.add(buatKartuProgres(p));
            panelProgres.add(Box.createVerticalStrut(12));
        }

    } catch (SQLException e) {
        JLabel error = Tema.label(
                "Gagal memuat progres anggota",
                Font.PLAIN,
                12f,
                Tema.TEKS_REDUP
        );

        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelProgres.add(error);
    }

    panelProgres.revalidate();
    panelProgres.repaint();
}

/** Membuat kartu progres untuk satu anggota keluarga. */
private JPanel buatKartuProgres(
        TugasDAO.ProgresAnggota p) {

    JPanel kartu = new JPanel();
    kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
    kartu.setBackground(new Color(245, 248, 253));
    kartu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                    new Color(225, 232, 242)
            ),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
    ));

    kartu.setAlignmentX(Component.LEFT_ALIGNMENT);
    kartu.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 150)
    );

    // Nama anggota
    JLabel nama = Tema.label(
            p.getNama(),
            Font.BOLD,
            13f,
            Tema.TEKS
    );

    nama.setAlignmentX(Component.LEFT_ALIGNMENT);
    kartu.add(nama);
    kartu.add(Box.createVerticalStrut(8));

    // Jumlah tugas selesai
    JLabel jumlah = Tema.label(
            p.getSelesai() + " / " + p.getTotal() + " tugas selesai",
            Font.PLAIN,
            11f,
            Tema.TEKS_REDUP
    );

    jumlah.setAlignmentX(Component.LEFT_ALIGNMENT);
    kartu.add(jumlah);
    kartu.add(Box.createVerticalStrut(8));

    // Progress bar
    JProgressBar progress = new JProgressBar(
            0, 100
    );

    progress.setValue(p.getPersentase());
    progress.setStringPainted(false);
    progress.setForeground(new Color(52, 120, 246));
    progress.setBackground(new Color(225, 232, 242));
    progress.setBorderPainted(false);
    progress.setPreferredSize(new Dimension(180, 10));
    progress.setMaximumSize(new Dimension(
            Integer.MAX_VALUE, 10
    ));
    progress.setAlignmentX(Component.LEFT_ALIGNMENT);

    kartu.add(progress);
    kartu.add(Box.createVerticalStrut(6));

    // Persentase
    JLabel persen = Tema.label(
            p.getPersentase() + "%",
            Font.BOLD,
            12f,
            new Color(52, 120, 246)
    );

    persen.setAlignmentX(Component.LEFT_ALIGNMENT);
    kartu.add(persen);

    return kartu;
}

    // ================= aksi tombol =================

    private void tambahTugas() {
        Tugas baru = tampilForm("Tambah Tugas", null);
        if (baru == null) {
            return;
        }
        try {
            dao.tambah(baru);
            muat();
        } catch (SQLException e) {
            kesalahan("Gagal menyimpan tugas", e);
        }
    }

    private void ubahTugas() {
        Tugas t = tugasTerpilih();
        if (t == null) {
            return;
        }
        Tugas hasil = tampilForm("Ubah Tugas", t);
        if (hasil == null) {
            return;
        }
        try {
            dao.ubah(hasil);
            muat();
        } catch (SQLException e) {
            kesalahan("Gagal mengubah tugas", e);
        }
    }

    
    private void ubahStatus() {
        Tugas t = tugasTerpilih();

        if (t == null) {
            return;
        }

        int pilihan = JOptionPane.showOptionDialog(this,
                "Ubah status tugas \"" + t.getJudul() + "\" menjadi:",
                "Ubah Status",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                NAMA_STATUS,
                Format.kapital(t.getStatus()));

        if (pilihan < 0) {
            return;
        }

        try {
            // Status baru yang dipilih
            String statusBaru = KODE_STATUS[pilihan];

            // Simpan perubahan status tugas
            dao.ubahStatus(t.getId(), statusBaru);

            // Buat notifikasi jika tugas baru selesai
            if ("SELESAI".equals(statusBaru)
                    && !"SELESAI".equals(t.getStatus())) {

               new homemate.dao.NotifikasiDAO().tambah(
                "Tugas Selesai",
                "Tugas \"" + t.getJudul()
                + "\" telah diselesaikan.",
                "SELESAI",
                t.getId(),
                "TUGAS_SELESAI_" + t.getId()
                + "_" + System.currentTimeMillis()
        );

        System.out.println("NOTIFIKASI TUGAS SELESAI BERHASIL DIBUAT!");
            }

            // Muat ulang tabel tugas
            muat();

        } catch (SQLException e) {
            kesalahan("Gagal mengubah status", e);
        }
    }


    private void hapusTugas() {
        Tugas t = tugasTerpilih();
        if (t == null) {
            return;
        }
        int yakin = JOptionPane.showConfirmDialog(this,
                "Hapus tugas \"" + t.getJudul() + "\"?\nTindakan ini tidak bisa dibatalkan.",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (yakin != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.hapus(t.getId());
            muat();
        } catch (SQLException e) {
            kesalahan("Gagal menghapus tugas", e);
        }
    }

    /** Tugas pada baris yang dipilih. Null (dan beri pesan) kalau belum ada yang dipilih. */
    private Tugas tugasTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0 || baris >= daftar.size()) {
            JOptionPane.showMessageDialog(this, "Pilih dulu satu tugas di tabel.",
                    "Belum ada yang dipilih", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return daftar.get(baris);
    }

    
/** Menampilkan detail tugas ketika baris tabel di-double-click. */
    
private void tampilDetailTugas() {
    Tugas t = tugasTerpilih();

    if (t == null) {
        return;
    }

    // Panel utama detail
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(8, 5, 8, 5);
    g.anchor = GridBagConstraints.NORTHWEST;
    g.fill = GridBagConstraints.HORIZONTAL;

    // Judul tugas
    JLabel lblJudul = new JLabel(t.getJudul());
    lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 20));
    lblJudul.setForeground(new Color(30, 64, 120));

    g.gridx = 0;
    g.gridy = 0;
    g.gridwidth = 2;
    g.weightx = 1;
    panel.add(lblJudul, g);

    // Garis pemisah
    g.gridy = 1;
    panel.add(new javax.swing.JSeparator(), g);

    // Deskripsi
    JLabel lblDeskripsi = new JLabel("Deskripsi Tugas");
    lblDeskripsi.setFont(new Font("Segoe UI", Font.BOLD, 13));

    g.gridy = 2;
    panel.add(lblDeskripsi, g);

    JTextArea txtDeskripsi = new JTextArea(
            t.getDeskripsi() == null || t.getDeskripsi().trim().isEmpty()
                    ? "Tidak ada deskripsi."
                    : t.getDeskripsi()
    );

    txtDeskripsi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    txtDeskripsi.setLineWrap(true);
    txtDeskripsi.setWrapStyleWord(true);
    txtDeskripsi.setEditable(false);
    txtDeskripsi.setOpaque(false);
    txtDeskripsi.setFocusable(false);

    JScrollPane scrollDeskripsi = new JScrollPane(txtDeskripsi);
    scrollDeskripsi.setPreferredSize(new Dimension(400, 100));
    scrollDeskripsi.setBorder(BorderFactory.createLineBorder(
            new Color(220, 225, 235)));

    g.gridy = 3;
    panel.add(scrollDeskripsi, g);

    // Informasi tugas
    tambahDetail(panel, g, 4, "Penanggung Jawab",
            t.getNamaAnggota() == null ? "-" : t.getNamaAnggota());

    tambahDetail(panel, g, 5, "Prioritas",
            Format.kapital(t.getPrioritas()));

    tambahDetail(panel, g, 6, "Tenggat",
            Format.tampil(t.getTenggat()));

    tambahDetail(panel, g, 7, "Status",
            Format.kapital(t.getStatus()));

    // Tampilkan popup
    JOptionPane.showMessageDialog(
            this,
            panel,
            "Detail Tugas",
            JOptionPane.INFORMATION_MESSAGE
    );
}

/** Menambahkan baris informasi pada popup detail. */
private void tambahDetail(
        JPanel panel,
        GridBagConstraints g,
        int baris,
        String label,
        String nilai) {

    g.gridy = baris;
    g.gridwidth = 1;
    g.weightx = 0;

    JLabel lbl = new JLabel(label);
    lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
    lbl.setForeground(new Color(90, 100, 120));

    g.gridx = 0;
    panel.add(lbl, g);

    JLabel lblNilai = new JLabel(
            nilai == null || nilai.trim().isEmpty() ? "-" : nilai
    );

    lblNilai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    lblNilai.setForeground(new Color(30, 40, 60));

    g.gridx = 1;
    g.weightx = 1;
    panel.add(lblNilai, g);
}

    // ================= form tambah / ubah =================

     
    /** Pilihan penanggung jawab di dropdown: tampil nama, menyimpan id. */
    private static class ItemAnggota {

        final int id;
        final String nama;

        ItemAnggota(int id, String nama) {
            this.id = id;
            this.nama = nama;
        }

        @Override
        public String toString() {
            return nama;
        }
    }

    /**
     * Tampilkan form. awal = null untuk tugas baru, atau tugas yang mau diubah.
     * Kembalikan Tugas hasil isian, atau null kalau dibatalkan.
     */
    private Tugas tampilForm(String judulDialog, Tugas awal) {
        // penanggung jawab harus dipilih dari daftar anggota
        Map<Integer, String> anggota;
        try {
            anggota = dao.ambilDaftarAnggota();
        } catch (SQLException e) {
            kesalahan("Gagal memuat daftar anggota", e);
            return null;
        }
        if (anggota.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Belum ada anggota keluarga.\nTambahkan anggota dulu di menu Anggota Keluarga.",
                    "Anggota kosong", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        JTextField txtJudul = new JTextField(awal == null ? "" : awal.getJudul());
        JTextArea txtDeskripsi = new JTextArea(awal == null || awal.getDeskripsi() == null ? "" : awal.getDeskripsi(), 3, 20);
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        JComboBox<ItemAnggota> cmbAnggota = new JComboBox<>();
        for (Map.Entry<Integer, String> a : anggota.entrySet()) {
            cmbAnggota.addItem(new ItemAnggota(a.getKey(), a.getValue()));
        }
        cmbAnggota.setSelectedIndex(-1); // kosong dulu, pengguna wajib memilih
        if (awal != null && awal.getAnggotaId() != null) {
            for (int i = 0; i < cmbAnggota.getItemCount(); i++) {
                if (cmbAnggota.getItemAt(i).id == awal.getAnggotaId()) {
                    cmbAnggota.setSelectedIndex(i);
                }
            }
        }
        JComboBox<String> cmbPrioritas = new JComboBox<>(NAMA_PRIORITAS);
        cmbPrioritas.setSelectedIndex(1); // default Sedang
        JTextField txtTenggat = new JTextField(awal == null || awal.getTenggat() == null ? "" : awal.getTenggat());
        txtTenggat.setToolTipText("Format: tahun-bulan-tanggal, contoh 2026-11-20");
        JComboBox<String> cmbStatus = new JComboBox<>(NAMA_STATUS);
        if (awal != null) {
            cmbPrioritas.setSelectedIndex(indeksKode(KODE_PRIORITAS, awal.getPrioritas()));
            cmbStatus.setSelectedIndex(indeksKode(KODE_STATUS, awal.getStatus()));
        }
        Tema.gayaKolom(txtJudul);
        Tema.gayaKolom(txtDeskripsi);
        Tema.gayaKolom(txtTenggat);

        JPanel form = new JPanel(new GridBagLayout());
        form.setPreferredSize(new Dimension(420, awal == null ? 330 : 330));
        tambahBaris(form, 0, "Judul *", txtJudul);
        tambahBaris(form, 1, "Deskripsi", new JScrollPane(txtDeskripsi));
        tambahBaris(form, 2, "Penanggung jawab *", cmbAnggota);
        tambahBaris(form, 3, "Prioritas", cmbPrioritas);
        tambahBaris(form, 4, "Tenggat * (yyyy-MM-dd)", txtTenggat);
        tambahBaris(form, 5, "Status", cmbStatus);

        // ulangi form sampai isian valid atau pengguna membatalkan (isian tidak hilang)
        while (true) {
            int pilih = JOptionPane.showConfirmDialog(this, form, judulDialog,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (pilih != JOptionPane.OK_OPTION) {
                return null;
            }
            String pesan = validasi(txtJudul.getText(), cmbAnggota.getSelectedItem(), txtTenggat.getText());
            if (pesan != null) {
                JOptionPane.showMessageDialog(this, pesan, "Data belum benar", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            Tugas t = new Tugas();
            if (awal != null) {
                t.setId(awal.getId());
            }
            t.setJudul(txtJudul.getText().trim());
            t.setDeskripsi(txtDeskripsi.getText().trim());
            t.setAnggotaId(((ItemAnggota) cmbAnggota.getSelectedItem()).id);
            t.setPrioritas(KODE_PRIORITAS[cmbPrioritas.getSelectedIndex()]);
            t.setTenggat(Format.tanggal(txtTenggat.getText()).toString());
            t.setStatus(KODE_STATUS[cmbStatus.getSelectedIndex()]);
            return t;
        }
    }

    /** Cek isian form. Kembalikan pesan error (bahasa Indonesia) atau null kalau valid. */
    private String validasi(String judul, Object anggotaDipilih, String tenggat) {
        if (judul.trim().isEmpty()) {
            return "Judul tugas wajib diisi.";
        }
        if (anggotaDipilih == null) {
            return "Penanggung jawab wajib dipilih dari daftar anggota.";
        }
        if (tenggat.trim().isEmpty()) {
            return "Tenggat wajib diisi.";
        }
        LocalDate tgl = Format.tanggal(tenggat);
        if (tgl == null) {
            return "Format tenggat salah.\nGunakan yyyy-MM-dd, contoh: " + LocalDate.now().plusDays(3);
        }
        return null;
    }

    private void tambahBaris(JPanel form, int baris, String teks, java.awt.Component isi) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = baris;
        g.insets = new Insets(5, 0, 5, 12);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridx = 0;
        g.weightx = 0;
        JLabel l = Tema.label(teks, Font.BOLD, 13f, Tema.TEKS);
        l.setPreferredSize(new Dimension(150, 28));
        form.add(l, g);
        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        form.add(isi, g);
    }

    private int indeksKode(String[] kode, String nilai) {
        for (int i = 0; i < kode.length; i++) {
            if (kode[i].equals(nilai)) {
                return i;
            }
        }
        return 0;
    }

    private void kesalahan(String judul, SQLException e) {
        JOptionPane.showMessageDialog(this, judul + ":\n" + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }
}