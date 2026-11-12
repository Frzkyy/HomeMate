package homemate.panel;

import homemate.dao.TransaksiDAO;
import homemate.model.Transaksi;
import homemate.util.Format;
import homemate.util.Ikon;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Layar Keuangan (mockup halaman 6): ringkasan bulanan, daftar transaksi, dan anggaran.
 * Milik Ketrin. Semua SQL ada di TransaksiDAO.
 */
public class KeuanganPanel extends JPanel {

    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter NAMA_BULAN = DateTimeFormatter.ofPattern("MMMM yyyy", INDONESIA);
    private static final String[] KATEGORI = {
        "Makanan", "Listrik", "Air", "Transportasi", "Belanja", "Kesehatan", "Pendidikan", "Gaji", "Lainnya"
    };
    private static final int BATAS_DIGIT = 15;

    private final TransaksiDAO dao = new TransaksiDAO();
    private YearMonth bulanAktif = YearMonth.now();
    private List<Transaksi> dataBulan = new ArrayList<>();

    private final JLabel lblBulan = Tema.label(" ", Font.BOLD, 14f, Tema.TEKS);

    // Tab Ringkasan
    private final JLabel lblMasuk = Tema.label("-", Font.BOLD, 22f, Tema.HIJAU_TUA);
    private final JLabel lblKeluar = Tema.label("-", Font.BOLD, 22f, Tema.MERAH_TUA);
    private final JLabel lblSisa = Tema.label("-", Font.BOLD, 22f, Tema.BIRU_TUA);
    private final GrafikPie grafik = new GrafikPie();
    private final DefaultTableModel modelTerbaru = modelTabel("Tanggal", "Keterangan", "Jumlah", "Tipe");

    // Tab Transaksi
    private final DefaultTableModel modelTransaksi = modelTabel("Tanggal", "Kategori", "Keterangan", "Jumlah", "Tipe");
    private final JTable tabelTransaksi = new JTable(modelTransaksi);

    // Tab Anggaran
    private final JLabel lblAnggaran = Tema.label("-", Font.BOLD, 22f, Tema.BIRU_TUA);
    private final JLabel lblTerpakai = Tema.label("-", Font.BOLD, 22f, Tema.MERAH_TUA);
    private final JLabel lblSisaAnggaran = Tema.label("-", Font.BOLD, 22f, Tema.HIJAU_TUA);
    private final JLabel lblPersen = Tema.label(" ", Font.PLAIN, 13f, Tema.TEKS_REDUP);
    private final Batang batang = new Batang();
    private final JTextField txtAnggaran = new JTextField(14);

    public KeuanganPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        // ===== Header: pilih bulan + tombol tambah =====
        Tema.Tombol btnSebelum = new Tema.Tombol("<", Tema.Tombol.Gaya.TEPI);
        btnSebelum.setMargin(new Insets(9, 12, 9, 12));
        btnSebelum.addActionListener(e -> geserBulan(-1));
        Tema.Tombol btnBerikut = new Tema.Tombol(">", Tema.Tombol.Gaya.TEPI);
        btnBerikut.setMargin(new Insets(9, 12, 9, 12));
        btnBerikut.addActionListener(e -> geserBulan(1));
        lblBulan.setPreferredSize(new Dimension(130, 20));
        lblBulan.setHorizontalAlignment(JLabel.CENTER);
        Tema.Tombol btnTambah = new Tema.Tombol("Tambah Transaksi", Tema.Tombol.Gaya.UTAMA, Ikon.Jenis.PLUS);
        btnTambah.addActionListener(e -> tambahTransaksi());

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kanan.setOpaque(false);
        kanan.add(btnSebelum);
        kanan.add(lblBulan);
        kanan.add(btnBerikut);
        kanan.add(btnTambah);

        // ===== Isi per tab =====
        CardLayout kartu = new CardLayout();
        JPanel isi = new JPanel(kartu);
        isi.setOpaque(false);
        isi.add(buatTabRingkasan(), "Ringkasan");
        isi.add(buatTabTransaksi(), "Transaksi");
        isi.add(buatTabAnggaran(), "Anggaran");

        JPanel atas = new JPanel(new BorderLayout(0, 14));
        atas.setOpaque(false);
        atas.add(Tema.header("Keuangan Rumah Tangga", "Catat pemasukan, pengeluaran, dan anggaran bulanan", kanan),
                BorderLayout.NORTH);
        atas.add(new Tema.TabBar(nama -> kartu.show(isi, nama), "Ringkasan", "Transaksi", "Anggaran"),
                BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);
        add(isi, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                muat();
            }
        });
        muat();
    }

    // =====================================================================
    // Susunan tampilan
    // =====================================================================

    private JPanel buatTabRingkasan() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel ringkas = new JPanel(new GridLayout(1, 3, 16, 0));
        ringkas.setOpaque(false);
        ringkas.add(kartuAngka("Total Pemasukan", lblMasuk, Tema.HIJAU_MUDA, Tema.HIJAU_TUA));
        ringkas.add(kartuAngka("Total Pengeluaran", lblKeluar, Tema.MERAH_MUDA, Tema.MERAH_TUA));
        ringkas.add(kartuAngka("Sisa Anggaran", lblSisa, Tema.BIRU_MUDA, Tema.BIRU_TUA));
        p.add(ringkas, BorderLayout.NORTH);

        Tema.Kartu kartuGrafik = new Tema.Kartu(new BorderLayout(0, 8));
        kartuGrafik.add(Tema.label("Grafik Pengeluaran", Font.BOLD, 15f, Tema.TEKS), BorderLayout.NORTH);
        kartuGrafik.add(grafik, BorderLayout.CENTER);

        JTable tabelTerbaru = new JTable(modelTerbaru);
        Tema.gayaTabel(tabelTerbaru);
        tabelTerbaru.setRowHeight(42);
        tabelTerbaru.getColumnModel().getColumn(3).setCellRenderer(Tema.rendererLencana());
        aturLebar(tabelTerbaru, 105, 125, 100, 125);
        Tema.Kartu kartuTerbaru = new Tema.Kartu(new BorderLayout(0, 8));
        kartuTerbaru.add(Tema.label("Transaksi Terbaru", Font.BOLD, 15f, Tema.TEKS), BorderLayout.NORTH);
        kartuTerbaru.add(Tema.gulir(tabelTerbaru), BorderLayout.CENTER);

        JPanel tengah = new JPanel(new GridBagLayout());
        tengah.setOpaque(false);
        GridBagConstraints gb = new GridBagConstraints();
        gb.fill = GridBagConstraints.BOTH;
        gb.weighty = 1;
        gb.gridy = 0;
        gb.gridx = 0;
        gb.weightx = 2;
        gb.insets = new Insets(0, 0, 0, 16);
        tengah.add(kartuGrafik, gb);
        gb.gridx = 1;
        gb.weightx = 3;
        gb.insets = new Insets(0, 0, 0, 0);
        tengah.add(kartuTerbaru, gb);
        p.add(tengah, BorderLayout.CENTER);
        return p;
    }

    private JPanel buatTabTransaksi() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        Tema.gayaTabel(tabelTransaksi);
        tabelTransaksi.getColumnModel().getColumn(4).setCellRenderer(Tema.rendererLencana());
        aturLebar(tabelTransaksi, 110, 150, 260, 140, 150);
        tabelTransaksi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    ubahTransaksi();
                }
            }
        });

        Tema.Tombol btnUbah = new Tema.Tombol("Ubah", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.EDIT);
        btnUbah.addActionListener(e -> ubahTransaksi());
        Tema.Tombol btnHapus = new Tema.Tombol("Hapus", Tema.Tombol.Gaya.BAHAYA, Ikon.Jenis.TONG);
        btnHapus.addActionListener(e -> hapusTransaksi());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(btnUbah);
        tombol.add(btnHapus);
        tombol.add(Tema.label("Pilih satu baris dulu. Klik dua kali untuk mengubah.",
                Font.PLAIN, 12f, Tema.TEKS_REDUP));
        p.add(tombol, BorderLayout.NORTH);

        Tema.Kartu isi = new Tema.Kartu(new BorderLayout());
        isi.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        isi.add(Tema.gulir(tabelTransaksi), BorderLayout.CENTER);
        p.add(isi, BorderLayout.CENTER);
        return p;
    }

    private JPanel buatTabAnggaran() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setOpaque(false);

        JPanel angka = new JPanel(new GridLayout(1, 3, 16, 0));
        angka.setOpaque(false);
        angka.add(kartuAngka("Anggaran Bulan Ini", lblAnggaran, Tema.BIRU_MUDA, Tema.BIRU_TUA));
        angka.add(kartuAngka("Sudah Terpakai", lblTerpakai, Tema.MERAH_MUDA, Tema.MERAH_TUA));
        angka.add(kartuAngka("Sisa Anggaran", lblSisaAnggaran, Tema.HIJAU_MUDA, Tema.HIJAU_TUA));

        Tema.Kartu progres = new Tema.Kartu(new BorderLayout(0, 8));
        progres.add(Tema.label("Pemakaian Anggaran", Font.BOLD, 15f, Tema.TEKS), BorderLayout.NORTH);
        progres.add(batang, BorderLayout.CENTER);
        progres.add(lblPersen, BorderLayout.SOUTH);

        Tema.gayaKolom(txtAnggaran);
        Tema.Tombol btnSimpan = new Tema.Tombol("Simpan Anggaran", Tema.Tombol.Gaya.UTAMA);
        btnSimpan.addActionListener(e -> simpanAnggaran());
        Tema.Tombol btnHapus = new Tema.Tombol("Hapus Anggaran", Tema.Tombol.Gaya.BAHAYA);
        btnHapus.addActionListener(e -> hapusAnggaran());

        Tema.Kartu form = new Tema.Kartu(new FlowLayout(FlowLayout.LEFT, 10, 0));
        form.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        form.add(Tema.label("Anggaran bulan ini (Rp)", Font.BOLD, 13f, Tema.TEKS));
        form.add(txtAnggaran);
        form.add(btnSimpan);
        form.add(btnHapus);

        JPanel bawah = new JPanel(new BorderLayout(0, 16));
        bawah.setOpaque(false);
        bawah.add(progres, BorderLayout.NORTH);
        bawah.add(form, BorderLayout.CENTER);
        JPanel atasSaja = new JPanel(new BorderLayout());
        atasSaja.setOpaque(false);
        atasSaja.add(bawah, BorderLayout.NORTH);

        p.add(angka, BorderLayout.NORTH);
        p.add(atasSaja, BorderLayout.CENTER);
        return p;
    }

    private Tema.Kartu kartuAngka(String judul, JLabel angka, Color latar, Color warnaJudul) {
        Tema.Kartu k = new Tema.Kartu(new BorderLayout(0, 6)).warna(latar, null);
        k.add(Tema.label(judul, Font.PLAIN, 13f, warnaJudul), BorderLayout.NORTH);
        k.add(angka, BorderLayout.CENTER);
        return k;
    }

    /** Lebar awal tiap kolom (piksel); sisa ruang dibagi otomatis oleh tabel. */
    private static void aturLebar(JTable tabel, int... lebar) {
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Seperti Format.rupiah, tapi angka minus ditulis "-Rp 400.000" (bukan "Rp -400.000"). */
    private static String rupiahBertanda(long nominal) {
        return nominal < 0 ? "-" + Format.rupiah(-nominal) : Format.rupiah(nominal);
    }

    private static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // =====================================================================
    // Muat data
    // =====================================================================

    private void geserBulan(int selisih) {
        bulanAktif = bulanAktif.plusMonths(selisih);
        muat();
    }

    /** Ambil ulang semua data bulan yang dipilih, lalu isi semua tab. */
    private void muat() {
        String bulan = bulanAktif.toString();
        lblBulan.setText(kapitalAwal(bulanAktif.format(NAMA_BULAN)));
        try {
            dataBulan = dao.daftarBulan(bulan);
            long masuk = dao.totalBulan(Transaksi.PEMASUKAN, bulan);
            long keluar = dao.totalBulan(Transaksi.PENGELUARAN, bulan);
            long anggaran = dao.ambilAnggaran(bulan);
            Map<String, Long> perKategori = dao.pengeluaranPerKategori(bulan);

            // Ringkasan
            lblMasuk.setText(Format.rupiah(masuk));
            lblKeluar.setText(Format.rupiah(keluar));
            lblSisa.setText(anggaran > 0 ? rupiahBertanda(anggaran - keluar) : "Belum diatur");
            lblSisa.setForeground(anggaran > 0 && keluar > anggaran ? Tema.MERAH_TUA : Tema.BIRU_TUA);
            grafik.setData(perKategori);

            modelTerbaru.setRowCount(0);
            for (int i = 0; i < Math.min(5, dataBulan.size()); i++) {
                Transaksi t = dataBulan.get(i);
                modelTerbaru.addRow(new Object[]{Format.tampil(t.getTanggal()), teksKeterangan(t),
                    Format.rupiah(t.getNominal()), Format.kapital(t.getJenis())});
            }

            // Transaksi
            modelTransaksi.setRowCount(0);
            for (Transaksi t : dataBulan) {
                modelTransaksi.addRow(new Object[]{Format.tampil(t.getTanggal()), t.getKategori(),
                    t.getKeterangan() == null || t.getKeterangan().isBlank() ? "-" : t.getKeterangan(),
                    Format.rupiah(t.getNominal()), Format.kapital(t.getJenis())});
            }

            // Anggaran
            if (anggaran > 0) {
                lblAnggaran.setText(Format.rupiah(anggaran));
                lblTerpakai.setText(Format.rupiah(keluar));
                lblSisaAnggaran.setText(rupiahBertanda(anggaran - keluar));
                lblSisaAnggaran.setForeground(keluar > anggaran ? Tema.MERAH_TUA : Tema.HIJAU_TUA);
                int persen = (int) (keluar * 100 / anggaran);
                lblPersen.setText(persen + "% dari anggaran terpakai"
                        + (keluar > anggaran ? " (melebihi anggaran)" : ""));
                batang.setRasio((double) keluar / anggaran);
                txtAnggaran.setText(String.valueOf(anggaran));
            } else {
                lblAnggaran.setText("Belum diatur");
                lblTerpakai.setText(Format.rupiah(keluar));
                lblSisaAnggaran.setText("-");
                lblPersen.setText("Isi anggaran di bawah untuk melihat sisa anggaran bulan ini.");
                batang.setRasio(0);
                txtAnggaran.setText("");
            }
        } catch (SQLException e) {
            tampilkanError("Gagal memuat data keuangan:\n" + e.getMessage());
        }
    }

    private static String teksKeterangan(Transaksi t) {
        String k = t.getKeterangan();
        return k == null || k.isBlank() ? t.getKategori() : k;
    }

    private static String kapitalAwal(String teks) {
        return teks.isEmpty() ? teks : teks.substring(0, 1).toUpperCase(INDONESIA) + teks.substring(1);
    }

    // =====================================================================
    // Aksi transaksi
    // =====================================================================

    private void tambahTransaksi() {
        Transaksi hasil = formTransaksi(null);
        if (hasil != null) {
            pindahKeBulanTransaksi(hasil);
            muat();
        }
    }

    private void ubahTransaksi() {
        Transaksi t = baris();
        if (t == null) {
            return;
        }
        Transaksi hasil = formTransaksi(t);
        if (hasil != null) {
            pindahKeBulanTransaksi(hasil);
            muat();
        }
    }

    private void hapusTransaksi() {
        Transaksi t = baris();
        if (t == null) {
            return;
        }
        int pilihan = JOptionPane.showConfirmDialog(this,
                "Hapus transaksi \"" + teksKeterangan(t) + "\" sebesar " + Format.rupiah(t.getNominal()) + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (pilihan != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.hapus(t.getId());
            muat();
        } catch (SQLException e) {
            tampilkanError("Gagal menghapus transaksi:\n" + e.getMessage());
        }
    }

    /** Transaksi pada baris yang dipilih di tabel, atau null (dengan peringatan) kalau belum ada yang dipilih. */
    private Transaksi baris() {
        int r = tabelTransaksi.getSelectedRow();
        if (r < 0 || r >= dataBulan.size()) {
            JOptionPane.showMessageDialog(this, "Pilih dulu satu transaksi di tabel.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return dataBulan.get(r);
    }

    /** Kalau tanggal transaksi yang baru disimpan ada di bulan lain, tampilkan bulan itu. */
    private void pindahKeBulanTransaksi(Transaksi t) {
        LocalDate tgl = Format.tanggal(t.getTanggal());
        if (tgl != null) {
            bulanAktif = YearMonth.from(tgl);
        }
    }

    /**
     * Form tambah (lama == null) atau ubah transaksi. Kembalikan transaksi yang tersimpan,
     * atau null kalau dibatalkan.
     */
    private Transaksi formTransaksi(Transaksi lama) {
        Window pemilik = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(pemilik, lama == null ? "Tambah Transaksi" : "Ubah Transaksi",
                Dialog.ModalityType.APPLICATION_MODAL);

        JComboBox<String> cmbJenis = new JComboBox<>(new String[]{"Pengeluaran", "Pemasukan"});
        cmbJenis.setFont(Tema.font(Font.PLAIN, 14f));
        JComboBox<String> cmbKategori = new JComboBox<>(KATEGORI);
        cmbKategori.setEditable(true);
        cmbKategori.setFont(Tema.font(Font.PLAIN, 14f));
        JTextField txtNominal = new JTextField(18);
        JTextField txtTanggal = new JTextField(18);
        JTextField txtKeterangan = new JTextField(18);
        Tema.gayaKolom(txtNominal);
        Tema.gayaKolom(txtTanggal);
        Tema.gayaKolom(txtKeterangan);

        if (lama != null) {
            cmbJenis.setSelectedItem(Format.kapital(lama.getJenis()));
            cmbKategori.setSelectedItem(lama.getKategori());
            txtNominal.setText(String.valueOf(lama.getNominal()));
            txtTanggal.setText(lama.getTanggal());
            txtKeterangan.setText(lama.getKeterangan() == null ? "" : lama.getKeterangan());
        } else {
            LocalDate hariIni = LocalDate.now();
            txtTanggal.setText((YearMonth.from(hariIni).equals(bulanAktif) ? hariIni : bulanAktif.atDay(1)).toString());
        }

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 8, 24));
        tambahBaris(form, 0, "Jenis", cmbJenis);
        tambahBaris(form, 1, "Kategori", cmbKategori);
        tambahBaris(form, 2, "Nominal (Rp)", txtNominal);
        tambahBaris(form, 3, "Tanggal (yyyy-MM-dd)", txtTanggal);
        tambahBaris(form, 4, "Keterangan (opsional)", txtKeterangan);

        Transaksi[] tersimpan = {null};
        Tema.Tombol btnSimpan = new Tema.Tombol("Simpan", Tema.Tombol.Gaya.UTAMA);
        Tema.Tombol btnBatal = new Tema.Tombol("Batal", Tema.Tombol.Gaya.TEPI);
        btnBatal.addActionListener(e -> dialog.dispose());
        btnSimpan.addActionListener(e -> {
            // --- Validasi ---
            Object kat = cmbKategori.getEditor().getItem();
            String kategori = kat == null ? "" : kat.toString().trim();
            if (kategori.isEmpty()) {
                peringatan(dialog, "Kategori wajib diisi.");
                return;
            }
            Long nominal = bacaNominal(txtNominal.getText());
            if (nominal == null) {
                peringatan(dialog, "Nominal harus berupa angka bulat lebih dari 0, contoh 150000.");
                return;
            }
            LocalDate tgl = Format.tanggal(txtTanggal.getText());
            if (tgl == null) {
                peringatan(dialog, "Format tanggal harus yyyy-MM-dd, contoh 2026-11-30.");
                return;
            }
            Transaksi t = new Transaksi();
            t.setJenis("Pemasukan".equals(cmbJenis.getSelectedItem()) ? Transaksi.PEMASUKAN : Transaksi.PENGELUARAN);
            t.setKategori(kategori);
            t.setNominal(nominal);
            t.setTanggal(tgl.toString());
            t.setKeterangan(txtKeterangan.getText().trim());
            try {
                if (lama == null) {
                    dao.tambah(t);
                } else {
                    t.setId(lama.getId());
                    dao.ubah(t);
                }
                tersimpan[0] = t;
                dialog.dispose();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan transaksi:\n" + ex.getMessage(),
                        "Kesalahan", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setBackground(Color.WHITE);
        tombol.setBorder(BorderFactory.createEmptyBorder(8, 24, 18, 24));
        tombol.add(btnBatal);
        tombol.add(btnSimpan);

        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(tombol, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnSimpan);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(pemilik);
        dialog.setVisible(true);
        return tersimpan[0];
    }

    private static void tambahBaris(JPanel p, int baris, String teks, JComponent isian) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = baris;
        g.insets = new Insets(6, 0, 6, 14);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0;
        p.add(Tema.label(teks, Font.BOLD, 13f, Tema.TEKS), g);
        g.gridx = 1;
        g.insets = new Insets(6, 0, 6, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        p.add(isian, g);
    }

    /**
     * Ubah teks nominal jadi angka. Titik, koma, spasi, dan awalan "Rp" diabaikan
     * ("1.500.000" dan "Rp 1500000" sama-sama 1500000). Kembalikan null kalau bukan angka bulat > 0.
     */
    private static Long bacaNominal(String teks) {
        String bersih = teks == null ? "" : teks.trim().toLowerCase().replace("rp", "")
                .replace(".", "").replace(",", "").replace(" ", "");
        if (!bersih.matches("\\d{1," + BATAS_DIGIT + "}")) {
            return null;
        }
        long n = Long.parseLong(bersih);
        return n > 0 ? n : null;
    }

    // =====================================================================
    // Aksi anggaran
    // =====================================================================

    private void simpanAnggaran() {
        Long jumlah = bacaNominal(txtAnggaran.getText());
        if (jumlah == null) {
            peringatan(this, "Anggaran harus berupa angka bulat lebih dari 0, contoh 3000000.");
            return;
        }
        try {
            dao.simpanAnggaran(bulanAktif.toString(), jumlah);
            muat();
            JOptionPane.showMessageDialog(this, "Anggaran " + lblBulan.getText() + " disimpan.",
                    "Berhasil", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            tampilkanError("Gagal menyimpan anggaran:\n" + e.getMessage());
        }
    }

    private void hapusAnggaran() {
        try {
            if (dao.ambilAnggaran(bulanAktif.toString()) == 0) {
                peringatan(this, "Bulan ini belum punya anggaran.");
                return;
            }
            int pilihan = JOptionPane.showConfirmDialog(this,
                    "Hapus anggaran " + lblBulan.getText() + "?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (pilihan == JOptionPane.YES_OPTION) {
                dao.hapusAnggaran(bulanAktif.toString());
                muat();
            }
        } catch (SQLException e) {
            tampilkanError("Gagal menghapus anggaran:\n" + e.getMessage());
        }
    }

    private void peringatan(java.awt.Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Peringatan", JOptionPane.WARNING_MESSAGE);
    }

    private void tampilkanError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================================
    // Komponen gambar sederhana
    // =====================================================================

    /** Diagram lingkaran pengeluaran per kategori, lengkap dengan legenda persen. */
    private static class GrafikPie extends JPanel {

        private static final Color[] WARNA = {
            new Color(0x3B82F6), new Color(0xF87171), new Color(0x22C55E),
            new Color(0xF59E0B), new Color(0x14B8A6), new Color(0x94A3B8)
        };
        private static final int MAKS_IRISAN = 5;

        private final List<String> nama = new ArrayList<>();
        private final List<Long> nilai = new ArrayList<>();
        private long total;

        GrafikPie() {
            setOpaque(false);
            setPreferredSize(new Dimension(320, 220));
        }

        void setData(Map<String, Long> data) {
            nama.clear();
            nilai.clear();
            total = 0;
            long sisa = 0;
            int i = 0;
            for (Map.Entry<String, Long> e : data.entrySet()) {
                total += e.getValue();
                if (i < MAKS_IRISAN) {
                    nama.add(e.getKey());
                    nilai.add(e.getValue());
                } else {
                    sisa += e.getValue();
                }
                i++;
            }
            if (sisa > 0) {
                nama.add("Kategori lain");
                nilai.add(sisa);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g.setFont(Tema.font(Font.PLAIN, 13f));
            FontMetrics fm = g.getFontMetrics();

            if (total <= 0) {
                g.setColor(Tema.TEKS_REDUP);
                String pesan = "Belum ada pengeluaran bulan ini";
                g.drawString(pesan, (w - fm.stringWidth(pesan)) / 2, h / 2);
                g.dispose();
                return;
            }

            int d = Math.max(60, Math.min(h - 16, w / 2 - 8));
            int x0 = 8;
            int y0 = (h - d) / 2;
            double mulai = 90;
            for (int i = 0; i < nilai.size(); i++) {
                double sudut = 360.0 * nilai.get(i) / total;
                g.setColor(WARNA[i % WARNA.length]);
                g.fill(new Arc2D.Double(x0, y0, d, d, mulai, -sudut, Arc2D.PIE));
                mulai -= sudut;
            }

            // Legenda
            int xLegenda = x0 + d + 22;
            int tinggiBaris = 28;
            int yAwal = (h - nilai.size() * tinggiBaris) / 2 + tinggiBaris / 2 + fm.getAscent() / 2 - 2;
            for (int i = 0; i < nilai.size(); i++) {
                int y = yAwal + i * tinggiBaris;
                g.setColor(WARNA[i % WARNA.length]);
                g.fillOval(xLegenda, y - 10, 10, 10);

                String persen = Math.round(100.0 * nilai.get(i) / total) + "%";
                int lebarPersen = fm.stringWidth(persen);
                int lebarNama = Math.max(20, w - xLegenda - 18 - lebarPersen - 12);
                g.setColor(Tema.TEKS);
                g.drawString(potong(nama.get(i), fm, lebarNama), xLegenda + 18, y);
                g.setColor(Tema.TEKS_REDUP);
                g.drawString(persen, w - lebarPersen - 6, y);
            }
            g.dispose();
        }

        private static String potong(String teks, FontMetrics fm, int lebar) {
            if (fm.stringWidth(teks) <= lebar) {
                return teks;
            }
            String s = teks;
            while (s.length() > 1 && fm.stringWidth(s + "...") > lebar) {
                s = s.substring(0, s.length() - 1);
            }
            return s + "...";
        }
    }

    /** Batang pemakaian anggaran: biru normal, oranye mulai 80%, merah kalau lewat 100%. */
    private static class Batang extends JComponent {

        private double rasio;

        Batang() {
            setPreferredSize(new Dimension(0, 16));
        }

        void setRasio(double rasio) {
            this.rasio = Math.max(0, rasio);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g.setColor(Tema.GARIS);
            g.fillRoundRect(0, 0, w, h, h, h);
            if (rasio > 0) {
                g.setColor(rasio > 1 ? Tema.MERAH : rasio >= 0.8 ? Tema.ORANYE : Tema.PRIMER);
                int isi = (int) Math.round(w * Math.min(1.0, rasio));
                g.fillRoundRect(0, 0, Math.max(isi, h), h, h, h);
            }
            g.dispose();
        }
    }
}
