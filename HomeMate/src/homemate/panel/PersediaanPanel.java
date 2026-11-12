package homemate.panel;

import homemate.dao.BarangDAO;
import homemate.model.Barang;
import homemate.util.Ikon;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/** Layar Persediaan dan Daftar Belanja (mockup halaman 5). Milik Devin. */
public class PersediaanPanel extends JPanel {

    private static final int KOLOM_STATUS = 4;
    private static final int KOLOM_AKSI = 5;
    private static final int MAKS_NAMA = 50;
    private static final int MAKS_SATUAN = 20;
    private static final int MAKS_ANGKA = 1_000_000;
    private static final String[] SATUAN = {"pcs", "kg", "gram", "liter", "botol", "bungkus", "pak", "kaleng"};

    private final BarangDAO dao = new BarangDAO();
    private final List<Barang> daftar = new ArrayList<>();
    private final List<Barang> belanja = new ArrayList<>();

    private final DefaultTableModel modelTabel = new DefaultTableModel(
            new String[]{"No", "Nama Barang", "Jumlah", "Batas Minimum", "Status", "Aksi"}, 0) {
        @Override
        public boolean isCellEditable(int baris, int kolom) {
            return false;
        }
    };
    private final JTable tabel = new JTable(modelTabel);
    private final JPanel isiBelanja = new JPanel();
    private final Tema.Kartu kartuPeringatan = new Tema.Kartu(new BorderLayout(0, 8));

    public PersediaanPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        Tema.Tombol btnTambah = new Tema.Tombol("Tambah Barang", Tema.Tombol.Gaya.UTAMA, Ikon.Jenis.PLUS);
        btnTambah.addActionListener(e -> tambah());
        add(Tema.header("Persediaan Rumah", "Pantau stok barang dan daftar belanja", btnTambah), BorderLayout.NORTH);

        add(buatKartuTabel(), BorderLayout.CENTER);

        JPanel bawah = new JPanel(new GridLayout(1, 2, 16, 0));
        bawah.setOpaque(false);
        bawah.setPreferredSize(new Dimension(0, 210));
        bawah.add(buatKartuBelanja());
        bawah.add(kartuPeringatan);
        add(bawah, BorderLayout.SOUTH);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                segarkan();
            }
        });
        segarkan();
    }

    // ===================== Tampilan =====================

    private JPanel buatKartuTabel() {
        Tema.Kartu kartu = new Tema.Kartu(new BorderLayout(0, 10));
        kartu.setBorder(BorderFactory.createEmptyBorder(16, 18, 14, 18));
        kartu.add(Tema.label("Daftar Barang", Font.BOLD, 15f, Tema.TEKS), BorderLayout.NORTH);

        Tema.gayaTabel(tabel);
        tabel.getColumnModel().getColumn(0).setMaxWidth(60);
        tabel.getColumnModel().getColumn(KOLOM_STATUS).setCellRenderer(Tema.rendererLencana());
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
                    Rectangle sel = tabel.getCellRect(baris, kolom, false);
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

    private JPanel buatKartuBelanja() {
        Tema.Kartu kartu = new Tema.Kartu(new BorderLayout(0, 8));
        kartu.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));
        kartu.add(Tema.label("Daftar Belanja", Font.BOLD, 15f, Tema.TEKS), BorderLayout.NORTH);

        isiBelanja.setOpaque(false);
        isiBelanja.setLayout(new BoxLayout(isiBelanja, BoxLayout.Y_AXIS));
        JPanel pembungkus = new JPanel(new BorderLayout());
        pembungkus.setOpaque(false);
        pembungkus.add(isiBelanja, BorderLayout.NORTH);
        JScrollPane gulir = Tema.gulir(pembungkus);
        gulir.setOpaque(false);
        gulir.getViewport().setOpaque(false);
        kartu.add(gulir, BorderLayout.CENTER);
        return kartu;
    }

    /** Muat ulang data dari database lalu isi tabel, daftar belanja, dan peringatan. */
    public void segarkan() {
        try {
            daftar.clear();
            daftar.addAll(dao.semua());
            belanja.clear();
            belanja.addAll(dao.menipis());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat persediaan:\n" + e.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
            return;
        }
        isiTabel();
        isiBelanja();
        isiPeringatan();
    }

    private void isiTabel() {
        modelTabel.setRowCount(0);
        int no = 1;
        for (Barang b : daftar) {
            modelTabel.addRow(new Object[]{no++, b.getNama(),
                (b.getJumlah() + " " + satuan(b)).trim(),
                (b.getBatasMinimum() + " " + satuan(b)).trim(),
                b.getStatus(), ""});
        }
    }

    private void isiBelanja() {
        isiBelanja.removeAll();
        if (belanja.isEmpty()) {
            isiBelanja.add(Tema.label("Tidak ada yang perlu dibeli. Semua stok aman.", Font.PLAIN, 13f, Tema.TEKS_REDUP));
        }
        for (Barang b : belanja) {
            isiBelanja.add(buatBarisBelanja(b));
        }
        isiBelanja.revalidate();
        isiBelanja.repaint();
    }

    private JPanel buatBarisBelanja(Barang b) {
        JPanel baris = new JPanel(new BorderLayout(10, 0));
        baris.setOpaque(false);
        baris.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS),
                BorderFactory.createEmptyBorder(6, 0, 6, 0)));
        baris.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel teks = new JPanel(new GridLayout(2, 1));
        teks.setOpaque(false);
        teks.add(Tema.label(b.getNama(), Font.BOLD, 13f, Tema.TEKS));
        teks.add(Tema.label("Sisa " + b.getJumlah() + " " + satuan(b)
                + " (minimum " + b.getBatasMinimum() + ")", Font.PLAIN, 12f, Tema.MERAH_TUA));
        baris.add(teks, BorderLayout.CENTER);

        Tema.Tombol btn = new Tema.Tombol("Tandai dibeli", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.CENTANG);
        btn.setMargin(new Insets(5, 12, 5, 12));
        btn.addActionListener(e -> tandaiDibeli(b));
        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        kanan.setOpaque(false);
        kanan.add(btn);
        baris.add(kanan, BorderLayout.EAST);
        return baris;
    }

    private static String satuan(Barang b) {
        return b.getSatuan() == null ? "" : b.getSatuan();
    }

    private void isiPeringatan() {
        kartuPeringatan.removeAll();
        boolean aman = belanja.isEmpty();
        if (aman) {
            kartuPeringatan.warna(Tema.HIJAU_MUDA, new Color(0xBBF7D0));
        } else {
            kartuPeringatan.warna(Tema.MERAH_MUDA, new Color(0xFECACA));
        }
        kartuPeringatan.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));
        kartuPeringatan.add(Tema.label(aman ? "Stok Aman" : "Peringatan", Font.BOLD, 15f,
                aman ? Tema.HIJAU_TUA : Tema.MERAH_TUA,
                Ikon.buat(aman ? Ikon.Jenis.CENTANG : Ikon.Jenis.LONCENG, 18, aman ? Tema.HIJAU_TUA : Tema.MERAH_TUA)),
                BorderLayout.NORTH);

        JTextArea isi = new JTextArea(aman
                ? "Semua barang masih di atas batas minimum."
                : ringkasanMenipis());
        isi.setEditable(false);
        isi.setFocusable(false);
        isi.setOpaque(false);
        isi.setLineWrap(true);
        isi.setWrapStyleWord(true);
        isi.setFont(Tema.font(Font.PLAIN, 13f));
        isi.setForeground(aman ? Tema.HIJAU_TUA : Tema.MERAH_TUA);
        kartuPeringatan.add(isi, BorderLayout.CENTER);
        kartuPeringatan.revalidate();
        kartuPeringatan.repaint();
    }

    /** Contoh: "Stok Beras, Minyak Goreng, dan Telur hampir habis (4 barang menipis)." */
    private String ringkasanMenipis() {
        int tampil = Math.min(3, belanja.size());
        StringBuilder sb = new StringBuilder("Stok ");
        for (int i = 0; i < tampil; i++) {
            if (i > 0) {
                sb.append(i == tampil - 1 && belanja.size() == tampil ? " dan " : ", ");
            }
            sb.append(belanja.get(i).getNama());
        }
        if (belanja.size() > tampil) {
            sb.append(", dan ").append(belanja.size() - tampil).append(" barang lainnya");
        }
        sb.append(" hampir habis. Silakan beli dan tandai dibeli di daftar belanja.");
        return sb.toString();
    }

    // ===================== Aksi =====================

    private void tambah() {
        Barang baru = formBarang("Tambah Barang", null);
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
        Barang lama = daftar.get(baris);
        Barang hasil = formBarang("Ubah Barang", lama);
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
        Barang b = daftar.get(baris);
        int pilih = JOptionPane.showConfirmDialog(this, "Hapus barang \"" + b.getNama() + "\"?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilih != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.hapus(b.getId());
            segarkan();
        } catch (SQLException e) {
            gagal("menghapus", e);
        }
    }

    /** Tombol "Tandai dibeli": tanya jumlah yang dibeli, lalu tambahkan ke stok. */
    private void tandaiDibeli(Barang b) {
        // saran: cukup supaya stok naik di atas batas minimum (jadi Aman)
        int saran = Math.max(1, b.getBatasMinimum() - b.getJumlah() + 1);
        JTextField txtBeli = new JTextField(String.valueOf(saran), 10);
        Tema.gayaKolom(txtBeli);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        form.add(Tema.label(b.getNama() + " (stok sekarang " + b.getJumlah() + " " + satuan(b) + ")",
                Font.PLAIN, 13f, Tema.TEKS_REDUP), g);
        g.gridy = 1;
        g.gridwidth = 1;
        form.add(Tema.label("Jumlah dibeli", Font.BOLD, 13f, Tema.TEKS), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(txtBeli, g);

        while (true) {
            int pilih = JOptionPane.showConfirmDialog(this, form, "Tandai Dibeli",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (pilih != JOptionPane.OK_OPTION) {
                return;
            }
            int beli = bacaAngka(txtBeli.getText());
            if (beli <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah dibeli harus berupa angka lebih dari 0 (maksimal "
                        + MAKS_ANGKA + ").", "Peringatan", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            if ((long) b.getJumlah() + beli > MAKS_ANGKA) {
                JOptionPane.showMessageDialog(this, "Total stok tidak boleh lebih dari " + MAKS_ANGKA + ".",
                        "Peringatan", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            try {
                dao.tambahStok(b.getId(), beli);
                segarkan();
            } catch (SQLException e) {
                gagal("memperbarui stok", e);
            }
            return;
        }
    }

    private void gagal(String aksi, SQLException e) {
        JOptionPane.showMessageDialog(this, "Gagal " + aksi + " data:\n" + e.getMessage(),
                "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Dialog isian barang. Kembalikan Barang baru, atau null kalau dibatalkan.
     * Diulang sampai isian valid.
     */
    private Barang formBarang(String judul, Barang awal) {
        JTextField txtNama = new JTextField(awal == null ? "" : awal.getNama(), 22);
        Tema.gayaKolom(txtNama);
        JTextField txtJumlah = new JTextField(awal == null ? "" : String.valueOf(awal.getJumlah()), 22);
        Tema.gayaKolom(txtJumlah);
        JTextField txtBatas = new JTextField(awal == null ? "" : String.valueOf(awal.getBatasMinimum()), 22);
        Tema.gayaKolom(txtBatas);
        JComboBox<String> cmbSatuan = new JComboBox<>(SATUAN);
        cmbSatuan.setEditable(true);
        cmbSatuan.setSelectedItem(awal == null || awal.getSatuan() == null ? SATUAN[0] : awal.getSatuan());
        cmbSatuan.setFont(Tema.font(Font.PLAIN, 14f));

        JPanel form = new JPanel(new GridBagLayout());
        tambahBaris(form, 0, "Nama Barang", txtNama);
        tambahBaris(form, 1, "Jumlah", txtJumlah);
        tambahBaris(form, 2, "Satuan", cmbSatuan);
        tambahBaris(form, 3, "Batas Minimum", txtBatas);

        int idAwal = awal == null ? 0 : awal.getId();
        while (true) {
            int pilih = JOptionPane.showConfirmDialog(this, form, judul,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (pilih != JOptionPane.OK_OPTION) {
                return null;
            }
            String nama = txtNama.getText().trim();
            Object s = cmbSatuan.getEditor().getItem();
            String satuan = s == null ? "" : s.toString().trim();

            String error = validasi(nama, txtJumlah.getText(), satuan, txtBatas.getText());
            try {
                if (error == null && dao.namaSudahAda(nama, idAwal)) {
                    error = "Barang \"" + nama + "\" sudah ada di daftar.";
                }
            } catch (SQLException e) {
                gagal("memeriksa", e);
                return null;
            }
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Peringatan", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return new Barang(idAwal, nama, bacaAngka(txtJumlah.getText()), satuan, bacaAngka(txtBatas.getText()));
        }
    }

    private void tambahBaris(JPanel form, int baris, String teks, Component isi) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        g.gridy = baris;
        form.add(Tema.label(teks, Font.BOLD, 13f, Tema.TEKS), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        form.add(isi, g);
    }

    /** Kembalikan pesan error, atau null kalau valid. */
    private String validasi(String nama, String jumlah, String satuan, String batas) {
        if (nama.isEmpty()) {
            return "Nama barang wajib diisi.";
        }
        if (nama.length() > MAKS_NAMA) {
            return "Nama barang maksimal " + MAKS_NAMA + " karakter.";
        }
        if (bacaAngka(jumlah) < 0) {
            return "Jumlah harus berupa angka bulat 0 atau lebih (maksimal " + MAKS_ANGKA + ").";
        }
        if (satuan.isEmpty()) {
            return "Satuan wajib diisi.";
        }
        if (satuan.length() > MAKS_SATUAN) {
            return "Satuan maksimal " + MAKS_SATUAN + " karakter.";
        }
        if (bacaAngka(batas) < 0) {
            return "Batas minimum harus berupa angka bulat 0 atau lebih (maksimal " + MAKS_ANGKA + ").";
        }
        return null;
    }

    /** Ubah teks jadi angka bulat 0..MAKS_ANGKA. Kembalikan -1 kalau kosong, bukan angka, negatif, atau terlalu besar. */
    private int bacaAngka(String teks) {
        String t = teks == null ? "" : teks.trim();
        if (t.isEmpty() || t.length() > 9) {
            return -1;
        }
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) {
                return -1;
            }
        }
        int n = Integer.parseInt(t);
        return n > MAKS_ANGKA ? -1 : n;
    }

    // ===================== Komponen kecil =====================

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
