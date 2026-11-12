package homemate.panel;

import homemate.dao.TugasDAO;
import homemate.model.Tugas;
import homemate.util.Format;
import homemate.util.Ikon;
import homemate.util.Preferensi;
import homemate.util.Tema;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Layar Tugas Rumah (mockup halaman 4). Milik Carlos.
 *
 * Penanggung jawab bisa anggota keluarga atau "Sistem". Tugas Sistem punya durasi
 * (contoh 45 menit, 1 jam, 2 jam). Hitung mundur baru mulai setelah tombol Mulai ditekan,
 * progress bar bergerak, dan saat waktu habis status otomatis berubah menjadi Selesai.
 * Tugas Sistem bisa dijeda / dilanjutkan, dibatalkan, dan diulangi. Saat sisa waktu tinggal
 * 5 menit bar berubah oranye + muncul notifikasi, begitu juga saat tugas selesai
 * (notifikasi ikut saklar "Notifikasi" di Pengaturan).
 */
public class TugasPanel extends JPanel {

    private static final String[] URUTAN = {TugasDAO.URUT_PRIORITAS, TugasDAO.URUT_TENGGAT, TugasDAO.URUT_TERBARU};
    private static final int KOLOM_PROGRES = 6;
    private static final Color WARNA_JEDA = new Color(0x94A3B8);

    private final TugasDAO dao = new TugasDAO();
    private final List<Tugas> daftar = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"No", "Nama Tugas", "Penanggung Jawab", "Prioritas", "Deadline", "Status", "Progres"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JTable tabel = new JTable(model);
    private final JTextField kolomCari = new JTextField();
    private final JComboBox<String> comboUrut = new JComboBox<>(URUTAN);
    private final JLabel info = Tema.label(" ", Font.PLAIN, 13f, Tema.HIJAU_TUA);

    // Kartu detail
    private final JLabel detJudul = Tema.label("Pilih tugas", Font.BOLD, 17f, Tema.TEKS);
    private final JLabel detInfo = Tema.label(" ", Font.PLAIN, 13f, Tema.TEKS_REDUP);
    private final JLabel detWaktu = Tema.label(" ", Font.BOLD, 22f, Tema.PRIMER);
    private final JLabel detKet = Tema.label(" ", Font.PLAIN, 13f, Tema.TEKS_REDUP);
    private final BilahProgres detBilah = new BilahProgres();
    private final Tema.Tombol btnMulai = new Tema.Tombol("Mulai", Tema.Tombol.Gaya.UTAMA, Ikon.Jenis.JAM);
    private final Tema.Tombol btnStatus = new Tema.Tombol("Ubah Status", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.CENTANG);
    private final Tema.Tombol btnUbah = new Tema.Tombol("Ubah", Tema.Tombol.Gaya.TEPI, Ikon.Jenis.EDIT);
    private final Tema.Tombol btnHapus = new Tema.Tombol("Hapus", Tema.Tombol.Gaya.BAHAYA, Ikon.Jenis.TONG);
    private final Tema.Tombol btnJeda = new Tema.Tombol("Jeda", Tema.Tombol.Gaya.TEPI);
    private final Tema.Tombol btnLanjut = new Tema.Tombol("Lanjut", Tema.Tombol.Gaya.UTAMA);
    private final Tema.Tombol btnBatalkan = new Tema.Tombol("Batalkan", Tema.Tombol.Gaya.TEPI);
    private final Tema.Tombol btnUlangi = new Tema.Tombol("Ulangi", Tema.Tombol.Gaya.UTAMA);

    /** Id tugas yang sudah diberi peringatan "hampir selesai" (supaya tidak berulang tiap detik). */
    private final Set<Integer> sudahDiperingatkan = new HashSet<>();

    private final Timer pewaktu;

    public TugasPanel() {
        super(new BorderLayout(0, 14));
        Tema.gayaHalaman(this);

        try {
            TugasDAO.siapkanKolom();
        } catch (SQLException e) {
            tampilError("Gagal menyiapkan tabel tugas", e);
        }

        // ===== Atas: judul + tombol tambah, lalu cari + urut =====
        Tema.Tombol btnTambah = new Tema.Tombol("Tambah Tugas", Tema.Tombol.Gaya.UTAMA, Ikon.Jenis.PLUS);
        btnTambah.addActionListener(e -> bukaForm(null));

        JPanel atas = new JPanel(new BorderLayout(0, 0));
        atas.setOpaque(false);
        atas.add(Tema.header("Tugas Rumah", "Daftar tugas, penanggung jawab, dan hitung mundur Sistem", btnTambah),
                BorderLayout.NORTH);
        atas.add(buatBarisCari(), BorderLayout.CENTER);
        add(atas, BorderLayout.NORTH);

        // ===== Tengah: tabel =====
        Tema.gayaTabel(tabel);
        int[] lebar = {46, 230, 140, 90, 100, 90, 210};
        for (int i = 0; i < lebar.length; i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
        tabel.getColumnModel().getColumn(0).setMaxWidth(56);
        tabel.getColumnModel().getColumn(3).setCellRenderer(Tema.rendererLencana());
        tabel.getColumnModel().getColumn(5).setCellRenderer(Tema.rendererLencana());
        tabel.getColumnModel().getColumn(KOLOM_PROGRES).setCellRenderer(new RendererProgres());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                perbaruiDetail();
            }
        });

        Tema.Kartu kartuTabel = new Tema.Kartu(new BorderLayout());
        kartuTabel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        kartuTabel.add(Tema.gulir(tabel), BorderLayout.CENTER);

        JPanel tengah = new JPanel(new BorderLayout(0, 14));
        tengah.setOpaque(false);
        tengah.add(kartuTabel, BorderLayout.CENTER);
        tengah.add(buatKartuDetail(), BorderLayout.SOUTH);
        add(tengah, BorderLayout.CENTER);

        // ===== Perilaku =====
        kolomCari.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                muat(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                muat(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                muat(null);
            }
        });
        comboUrut.addActionListener(e -> muat(null));
        btnMulai.addActionListener(e -> mulaiTerpilih());
        btnStatus.addActionListener(e -> ubahStatusTerpilih());
        btnJeda.addActionListener(e -> jedaTerpilih());
        btnLanjut.addActionListener(e -> lanjutTerpilih());
        btnBatalkan.addActionListener(e -> batalkanTerpilih());
        btnUlangi.addActionListener(e -> ulangiTerpilih());
        btnUbah.addActionListener(e -> bukaForm(terpilih()));
        btnHapus.addActionListener(e -> hapusTerpilih());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                muat(null);
            }
        });

        muat(null);
        pewaktu = new Timer(1000, e -> tik());
        pewaktu.setInitialDelay(1000);
        pewaktu.start();
    }

    // ================= Tampilan =================

    private JPanel buatBarisCari() {
        Tema.gayaKolom(kolomCari);
        kolomCari.setPreferredSize(new Dimension(240, 38));
        comboUrut.setFont(Tema.font(Font.PLAIN, 13f));
        comboUrut.setPreferredSize(new Dimension(130, 38));

        JPanel kiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        kiri.setOpaque(false);
        kiri.add(Tema.label("Cari:", Font.PLAIN, 13f, Tema.TEKS_REDUP));
        kiri.add(kolomCari);
        kiri.add(Tema.label("Urutkan:", Font.PLAIN, 13f, Tema.TEKS_REDUP));
        kiri.add(comboUrut);

        JPanel baris = new JPanel(new BorderLayout());
        baris.setOpaque(false);
        baris.add(kiri, BorderLayout.WEST);
        baris.add(info, BorderLayout.EAST);
        baris.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        return baris;
    }

    private JPanel buatKartuDetail() {
        Tema.Kartu kartu = new Tema.Kartu(new BorderLayout(0, 12));
        kartu.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Baris atas: judul tugas di kiri, tombol aksi di kanan
        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
        kiri.add(Tema.label("Detail Tugas", Font.PLAIN, 12f, Tema.TEKS_REDUP));
        kiri.add(Box.createVerticalStrut(4));
        kiri.add(detJudul);
        kiri.add(Box.createVerticalStrut(2));
        kiri.add(detInfo);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        kanan.setOpaque(false);
        kanan.add(btnMulai);
        kanan.add(btnLanjut);
        kanan.add(btnJeda);
        kanan.add(btnUlangi);
        kanan.add(btnBatalkan);
        kanan.add(btnStatus);
        kanan.add(btnUbah);
        kanan.add(btnHapus);
        JPanel kananTengah = new JPanel(new GridBagLayout());
        kananTengah.setOpaque(false);
        kananTengah.add(kanan);

        JPanel atas = new JPanel(new BorderLayout(16, 0));
        atas.setOpaque(false);
        atas.add(kiri, BorderLayout.CENTER);
        atas.add(kananTengah, BorderLayout.EAST);
        kartu.add(atas, BorderLayout.NORTH);

        // Baris bawah: hitung mundur + progress bar selebar kartu
        JPanel waktu = new JPanel(new BorderLayout(14, 0));
        waktu.setOpaque(false);
        waktu.add(detWaktu, BorderLayout.WEST);
        waktu.add(detKet, BorderLayout.CENTER);
        waktu.setAlignmentX(LEFT_ALIGNMENT);
        detBilah.setPreferredSize(new Dimension(100, 12));
        detBilah.setMinimumSize(new Dimension(10, 12));
        detBilah.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        detBilah.setAlignmentX(LEFT_ALIGNMENT);

        JPanel bawah = new JPanel();
        bawah.setOpaque(false);
        bawah.setLayout(new BoxLayout(bawah, BoxLayout.Y_AXIS));
        bawah.add(waktu);
        bawah.add(Box.createVerticalStrut(8));
        bawah.add(detBilah);
        kartu.add(bawah, BorderLayout.CENTER);

        perbaruiDetail();
        return kartu;
    }

    // ================= Data =================

    /** Muat ulang dari database. idPilih = id yang ingin dipilih setelahnya (null = pertahankan pilihan). */
    private void muat(Integer idPilih) {
        Tugas lama = terpilih();
        int idLama = idPilih != null ? idPilih : (lama == null ? -1 : lama.getId());
        long sekarang = System.currentTimeMillis();
        try {
            // Catat judul yang waktunya habis (dari daftar sebelumnya), lalu tandai selesai di database.
            List<String> habis = new ArrayList<>();
            for (Tugas t : daftar) {
                if (t.isBerjalan() && t.sisaMillis(sekarang) <= 0) {
                    habis.add(t.getJudul());
                }
            }
            int jumlah = dao.selesaikanYangHabis(sekarang);
            daftar.clear();
            daftar.addAll(dao.semua(kolomCari.getText(), (String) comboUrut.getSelectedItem()));
            if (jumlah > 0) {
                umumkanSelesai(habis, jumlah);
            }
        } catch (SQLException e) {
            tampilError("Gagal memuat tugas", e);
            return;
        }
        model.setRowCount(0);
        int no = 1;
        int barisPilih = -1;
        for (Tugas t : daftar) {
            model.addRow(new Object[]{
                no, t.getJudul(), t.getPenanggungJawab(), Format.kapital(t.getPrioritas()),
                Format.tampil(t.getTenggat()), statusTeks(t), t});
            if (t.getId() == idLama) {
                barisPilih = no - 1;
            }
            no++;
        }
        if (barisPilih >= 0) {
            tabel.setRowSelectionInterval(barisPilih, barisPilih);
        } else {
            tabel.clearSelection();
        }
        perbaruiDetail();
    }

    private Tugas terpilih() {
        int baris = tabel.getSelectedRow();
        return (baris >= 0 && baris < daftar.size()) ? daftar.get(baris) : null;
    }

    /** Dipanggil tiap detik: tutup tugas Sistem yang waktunya habis, beri peringatan 5 menit, gambar ulang. */
    private void tik() {
        long sekarang = System.currentTimeMillis();
        boolean adaHabis = false;
        for (Tugas t : daftar) {
            if (t.isBerjalan() && t.sisaMillis(sekarang) <= 0) {
                adaHabis = true;
            } else if (t.isHampirSelesai(sekarang) && sudahDiperingatkan.add(t.getId())) {
                beritahu("Hampir selesai",
                        potong(t.getJudul()) + " tinggal " + teksSisa(t.sisaMillis(sekarang)) + " lagi.",
                        Tema.ORANYE);
            }
        }
        if (adaHabis) {
            muat(null);   // muat() menandai selesai dan mengumumkannya
            return;
        }
        tabel.repaint();
        perbaruiDetail();
    }

    private void umumkanSelesai(List<String> judul, int jumlah) {
        String teks = judul.size() == jumlah
                ? String.join(", ", judul)
                : jumlah + " tugas Sistem";
        info.setText("Selesai otomatis oleh Sistem: " + teks);
        beritahu("Tugas selesai", potong(teks) + " sudah selesai dikerjakan Sistem.", Tema.HIJAU);
    }

    private void perbaruiDetail() {
        Tugas t = terpilih();
        if (t == null) {
            detJudul.setText("Pilih tugas di tabel");
            detInfo.setText("Hitung mundur muncul di sini untuk tugas Sistem");
            detWaktu.setText(" ");
            detKet.setText(" ");
            detBilah.setNilai(0, Tema.PRIMER);
            for (Tema.Tombol b : new Tema.Tombol[]{btnMulai, btnLanjut, btnJeda, btnUlangi, btnBatalkan, btnStatus}) {
                b.setVisible(false);
            }
            btnUbah.setEnabled(false);
            btnHapus.setEnabled(false);
            return;
        }
        long sekarang = System.currentTimeMillis();
        detJudul.setText(t.getJudul());
        String tenggat = t.getTenggat() == null || t.getTenggat().isBlank() ? "tanpa deadline" : "deadline " + Format.tampil(t.getTenggat());
        detInfo.setText(t.getPenanggungJawab() + "  |  Prioritas " + Format.kapital(t.getPrioritas()) + "  |  " + tenggat);

        btnUbah.setEnabled(true);
        btnHapus.setEnabled(true);
        boolean sistem = t.isPjSistem();
        btnMulai.setVisible(sistem && Tugas.BELUM.equals(t.getStatus()));
        btnJeda.setVisible(t.isBerjalan());
        btnLanjut.setVisible(t.isDijeda());
        btnBatalkan.setVisible(sistem && Tugas.SEDANG.equals(t.getStatus()));
        btnUlangi.setVisible(sistem && Tugas.SELESAI.equals(t.getStatus()));
        btnStatus.setVisible(!sistem);

        if (!sistem) {
            detWaktu.setText(Format.kapital(t.getStatus()));
            detWaktu.setForeground(Tema.warnaStatus(t.getStatus())[1]);
            detKet.setText("Dikerjakan manual oleh " + t.getPenanggungJawab());
            detBilah.setNilai(Tugas.SELESAI.equals(t.getStatus()) ? 1 : 0, Tema.HIJAU);
            return;
        }
        detBilah.setNilai(t.progres(sekarang), warnaBilah(t, sekarang));
        if (Tugas.SELESAI.equals(t.getStatus())) {
            detWaktu.setText("Selesai");
            detWaktu.setForeground(Tema.HIJAU_TUA);
            detKet.setText("Dikerjakan Sistem selama " + teksDurasi(t.getDurasiMenit()) + ". Tekan Ulangi untuk menjalankan lagi.");
        } else if (t.getMulaiPada() == null) {
            detWaktu.setText(teksDurasi(t.getDurasiMenit()));
            detWaktu.setForeground(Tema.TEKS_REDUP);
            detKet.setText("Belum dimulai. Tekan Mulai untuk menjalankan hitung mundur.");
        } else if (t.isDijeda()) {
            detWaktu.setText(jam(t.sisaMillis(sekarang)));
            detWaktu.setForeground(Tema.TEKS_REDUP);
            detKet.setText("Dijeda  |  sisa " + teksSisa(t.sisaMillis(sekarang)) + ". Tekan Lanjut untuk meneruskan.");
        } else if (t.isHampirSelesai(sekarang)) {
            detWaktu.setText(jam(t.sisaMillis(sekarang)));
            detWaktu.setForeground(Tema.ORANYE_TUA);
            detKet.setText("Hampir selesai  |  " + teksSisa(t.sisaMillis(sekarang)) + " lagi");
        } else {
            detWaktu.setText(jam(t.sisaMillis(sekarang)));
            detWaktu.setForeground(Tema.PRIMER);
            detKet.setText("Selesai dalam " + teksSisa(t.sisaMillis(sekarang)) + "  |  "
                    + (int) (t.progres(sekarang) * 100) + "% selesai");
        }
    }

    // ================= Aksi =================

    private void mulaiTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        try {
            if (dao.mulai(t.getId(), System.currentTimeMillis())) {
                sudahDiperingatkan.remove(t.getId());
                info.setText("Sistem mulai mengerjakan: " + t.getJudul());
            }
            muat(t.getId());
        } catch (SQLException e) {
            tampilError("Gagal memulai tugas", e);
        }
    }

    private void jedaTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        try {
            if (dao.jeda(t.getId(), System.currentTimeMillis())) {
                info.setText("Dijeda: " + t.getJudul());
            }
            muat(t.getId());
        } catch (SQLException e) {
            tampilError("Gagal menjeda tugas", e);
        }
    }

    private void lanjutTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        try {
            if (dao.lanjut(t.getId(), System.currentTimeMillis())) {
                info.setText("Dilanjutkan: " + t.getJudul());
            }
            muat(t.getId());
        } catch (SQLException e) {
            tampilError("Gagal melanjutkan tugas", e);
        }
    }

    private void batalkanTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Batalkan pengerjaan \"" + t.getJudul() + "\"?\nHitung mundur dihentikan dan tugas kembali ke status Belum.",
                "Batalkan Tugas", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (dao.batalkan(t.getId())) {
                sudahDiperingatkan.remove(t.getId());
                info.setText("Dibatalkan: " + t.getJudul());
            }
            muat(t.getId());
        } catch (SQLException e) {
            tampilError("Gagal membatalkan tugas", e);
        }
    }

    private void ulangiTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        try {
            if (dao.ulangi(t.getId(), System.currentTimeMillis())) {
                sudahDiperingatkan.remove(t.getId());
                info.setText("Sistem mengulang: " + t.getJudul() + " (" + teksDurasi(t.getDurasiMenit()) + ")");
            }
            muat(t.getId());
        } catch (SQLException e) {
            tampilError("Gagal mengulang tugas", e);
        }
    }

    private void ubahStatusTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        String[] pilihan = {"Belum", "Sedang", "Selesai"};
        Object p = JOptionPane.showInputDialog(this, "Status untuk \"" + t.getJudul() + "\":", "Ubah Status",
                JOptionPane.PLAIN_MESSAGE, null, pilihan, Format.kapital(t.getStatus()));
        if (p == null) {
            return;
        }
        try {
            dao.ubahStatus(t.getId(), p.toString().toUpperCase());
            muat(t.getId());
        } catch (SQLException e) {
            tampilError("Gagal mengubah status", e);
        }
    }

    private void hapusTerpilih() {
        Tugas t = terpilih();
        if (t == null) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Hapus tugas \"" + t.getJudul() + "\"?", "Hapus Tugas",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            dao.hapus(t.getId());
            sudahDiperingatkan.remove(t.getId());
            muat(-1);
        } catch (SQLException e) {
            tampilError("Gagal menghapus tugas", e);
        }
    }

    // ================= Form tambah / ubah =================

    /** Pilihan di combo penanggung jawab. */
    private static class Pilihan {

        final Integer id;      // null kalau Sistem
        final String nama;
        final boolean sistem;

        Pilihan(Integer id, String nama, boolean sistem) {
            this.id = id;
            this.nama = nama;
            this.sistem = sistem;
        }

        @Override
        public String toString() {
            return nama;
        }
    }

    private void bukaForm(Tugas awal) {
        final boolean ubah = awal != null;
        final boolean berjalan = ubah && !Tugas.BELUM.equals(awal.getStatus()) && awal.isPjSistem();

        Map<Integer, String> anggota;
        try {
            anggota = dao.daftarAnggota();
        } catch (SQLException e) {
            tampilError("Gagal memuat daftar anggota", e);
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), ubah ? "Ubah Tugas" : "Tambah Tugas",
                JDialog.DEFAULT_MODALITY_TYPE);
        JPanel isi = new JPanel(new GridBagLayout());
        isi.setBackground(Tema.LATAR);
        isi.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField fJudul = new JTextField(ubah ? awal.getJudul() : "");
        Tema.gayaKolom(fJudul);
        JTextArea fDesk = new JTextArea(ubah && awal.getDeskripsi() != null ? awal.getDeskripsi() : "", 3, 20);
        Tema.gayaKolom(fDesk);
        fDesk.setLineWrap(true);
        fDesk.setWrapStyleWord(true);

        JComboBox<Pilihan> fPj = new JComboBox<>();
        fPj.setFont(Tema.font(Font.PLAIN, 14f));
        fPj.addItem(new Pilihan(null, "Sistem (otomatis)", true));
        for (Map.Entry<Integer, String> e : anggota.entrySet()) {
            fPj.addItem(new Pilihan(e.getKey(), e.getValue(), false));
        }
        if (ubah) {
            for (int i = 0; i < fPj.getItemCount(); i++) {
                Pilihan p = fPj.getItemAt(i);
                if (awal.isPjSistem() ? p.sistem : (!p.sistem && p.id.equals(awal.getAnggotaId()))) {
                    fPj.setSelectedIndex(i);
                }
            }
        }

        JComboBox<String> fPrio = new JComboBox<>(new String[]{"Rendah", "Sedang", "Tinggi"});
        fPrio.setFont(Tema.font(Font.PLAIN, 14f));
        fPrio.setSelectedItem(ubah ? Format.kapital(awal.getPrioritas()) : "Sedang");

        JTextField fTenggat = new JTextField(ubah && awal.getTenggat() != null ? awal.getTenggat() : "");
        Tema.gayaKolom(fTenggat);
        fTenggat.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd (boleh kosong)");

        // Durasi Sistem
        int durAwal = ubah && awal.isPjSistem() ? awal.getDurasiMenit() : 60;
        JSpinner fJam = new JSpinner(new SpinnerNumberModel(durAwal / 60, 0, 99, 1));
        JSpinner fMenit = new JSpinner(new SpinnerNumberModel(durAwal % 60, 0, 59, 1));
        fJam.setFont(Tema.font(Font.PLAIN, 14f));
        fMenit.setFont(Tema.font(Font.PLAIN, 14f));
        JPanel panelDurasi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panelDurasi.setOpaque(false);
        panelDurasi.add(fJam);
        panelDurasi.add(Tema.label("jam", Font.PLAIN, 13f, Tema.TEKS_REDUP));
        panelDurasi.add(fMenit);
        panelDurasi.add(Tema.label("menit", Font.PLAIN, 13f, Tema.TEKS_REDUP));

        JPanel cepat = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        cepat.setOpaque(false);
        int[][] preset = {{45, 0}, {60, 0}, {120, 0}, {180, 0}};
        String[] namaPreset = {"45 menit", "1 jam", "2 jam", "3 jam"};
        for (int i = 0; i < preset.length; i++) {
            final int menit = preset[i][0];
            Tema.Tombol b = new Tema.Tombol(namaPreset[i], Tema.Tombol.Gaya.TEPI);
            b.setFont(Tema.font(Font.BOLD, 12f));
            b.setMargin(new Insets(5, 10, 5, 10));
            b.addActionListener(e -> {
                fJam.setValue(menit / 60);
                fMenit.setValue(menit % 60);
            });
            cepat.add(b);
        }
        JPanel blokDurasi = new JPanel();
        blokDurasi.setOpaque(false);
        blokDurasi.setLayout(new BoxLayout(blokDurasi, BoxLayout.Y_AXIS));
        panelDurasi.setAlignmentX(LEFT_ALIGNMENT);
        cepat.setAlignmentX(LEFT_ALIGNMENT);
        blokDurasi.add(panelDurasi);
        blokDurasi.add(Box.createVerticalStrut(8));
        blokDurasi.add(cepat);
        JLabel catatan = Tema.label("Hitung mundur baru berjalan setelah tombol Mulai ditekan.", Font.PLAIN, 12f, Tema.TEKS_REDUP);
        catatan.setAlignmentX(LEFT_ALIGNMENT);
        blokDurasi.add(Box.createVerticalStrut(6));
        blokDurasi.add(catatan);

        JLabel labelDurasi = Tema.label("Lama pengerjaan", Font.BOLD, 13f, Tema.TEKS);
        Runnable atur = () -> {
            Pilihan p = (Pilihan) fPj.getSelectedItem();
            boolean sistem = p != null && p.sistem;
            labelDurasi.setVisible(sistem);
            blokDurasi.setVisible(sistem);
            dlg.pack();
        };
        fPj.addActionListener(e -> atur.run());
        if (berjalan) {
            // Sudah dimulai / selesai: penanggung jawab dan durasi dikunci supaya hitung mundur tidak kacau.
            fPj.setEnabled(false);
            fJam.setEnabled(false);
            fMenit.setEnabled(false);
            for (Component c : cepat.getComponents()) {
                c.setEnabled(false);
            }
        }

        int baris = 0;
        baris = tambahBaris(isi, baris, "Judul tugas", fJudul);
        baris = tambahBaris(isi, baris, "Deskripsi", fDesk);
        baris = tambahBaris(isi, baris, "Penanggung jawab", fPj);
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0;
        gl.gridy = baris;
        gl.anchor = GridBagConstraints.NORTHWEST;
        gl.insets = new Insets(8, 0, 2, 0);
        isi.add(labelDurasi, gl);
        GridBagConstraints gb = new GridBagConstraints();
        gb.gridx = 0;
        gb.gridy = baris + 1;
        gb.fill = GridBagConstraints.HORIZONTAL;
        gb.weightx = 1;
        isi.add(blokDurasi, gb);
        baris += 2;
        baris = tambahBaris(isi, baris, "Prioritas", fPrio);
        baris = tambahBaris(isi, baris, "Deadline", fTenggat);

        Tema.Tombol btnBatal = new Tema.Tombol("Batal", Tema.Tombol.Gaya.TEPI);
        Tema.Tombol btnSimpan = new Tema.Tombol(ubah ? "Simpan" : "Tambah", Tema.Tombol.Gaya.UTAMA);
        btnBatal.addActionListener(e -> dlg.dispose());
        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombol.setOpaque(false);
        tombol.add(btnBatal);
        tombol.add(btnSimpan);
        GridBagConstraints gt = new GridBagConstraints();
        gt.gridx = 0;
        gt.gridy = baris;
        gt.fill = GridBagConstraints.HORIZONTAL;
        gt.insets = new Insets(18, 0, 0, 0);
        isi.add(tombol, gt);

        btnSimpan.addActionListener(e -> {
            Tugas t = ubah ? awal : new Tugas();
            String judul = fJudul.getText().trim();
            if (judul.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Judul tugas wajib diisi.", "Validasi", JOptionPane.WARNING_MESSAGE);
                fJudul.requestFocus();
                return;
            }
            String tenggat = fTenggat.getText().trim();
            if (!tenggat.isEmpty() && Format.tanggal(tenggat) == null) {
                JOptionPane.showMessageDialog(dlg, "Format deadline harus yyyy-MM-dd, contoh 2026-11-20.",
                        "Validasi", JOptionPane.WARNING_MESSAGE);
                fTenggat.requestFocus();
                return;
            }
            Pilihan p = (Pilihan) fPj.getSelectedItem();
            if (p == null) {
                JOptionPane.showMessageDialog(dlg, "Pilih penanggung jawab.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int durasi = ((Integer) fJam.getValue()) * 60 + (Integer) fMenit.getValue();
            if (p.sistem && durasi <= 0) {
                JOptionPane.showMessageDialog(dlg, "Lama pengerjaan Sistem harus lebih dari 0 menit.",
                        "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            t.setJudul(judul);
            t.setDeskripsi(fDesk.getText().trim());
            t.setPjSistem(p.sistem);
            t.setAnggotaId(p.id);
            t.setDurasiMenit(p.sistem ? durasi : 0);
            t.setPrioritas(((String) fPrio.getSelectedItem()).toUpperCase());
            t.setTenggat(tenggat.isEmpty() ? null : tenggat);
            try {
                int id;
                if (ubah) {
                    dao.ubah(t);
                    id = t.getId();
                } else {
                    id = dao.tambah(t);
                    info.setText(p.sistem
                            ? "Tugas ditambahkan. Tekan Mulai agar Sistem mulai bekerja."
                            : "Tugas ditambahkan.");
                }
                dlg.dispose();
                muat(id);
            } catch (SQLException ex) {
                tampilError("Gagal menyimpan tugas", ex);
            }
        });

        dlg.setContentPane(isi);
        dlg.setMinimumSize(new Dimension(460, 0));
        atur.run();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private int tambahBaris(JPanel isi, int baris, String teks, JComponent komponen) {
        GridBagConstraints gl = new GridBagConstraints();
        gl.gridx = 0;
        gl.gridy = baris;
        gl.anchor = GridBagConstraints.NORTHWEST;
        gl.insets = new Insets(baris == 0 ? 0 : 8, 0, 2, 0);
        isi.add(Tema.label(teks, Font.BOLD, 13f, Tema.TEKS), gl);
        GridBagConstraints gk = new GridBagConstraints();
        gk.gridx = 0;
        gk.gridy = baris + 1;
        gk.fill = GridBagConstraints.HORIZONTAL;
        gk.weightx = 1;
        isi.add(komponen, gk);
        return baris + 2;
    }

    // ================= Bantu tampilan =================

    private void tampilError(String judul, Exception e) {
        JOptionPane.showMessageDialog(this, judul + ":\n" + e.getMessage(), "Kesalahan", JOptionPane.ERROR_MESSAGE);
    }

    /** Biru saat berjalan, oranye saat hampir selesai, abu saat dijeda, hijau saat selesai. */
    private static Color warnaBilah(Tugas t, long sekarang) {
        if (Tugas.SELESAI.equals(t.getStatus())) {
            return Tema.HIJAU;
        }
        if (t.isDijeda()) {
            return WARNA_JEDA;
        }
        if (t.isHampirSelesai(sekarang)) {
            return Tema.ORANYE;
        }
        return Tema.PRIMER;
    }

    /** Teks untuk kolom Status: "Dijeda" kalau dijeda, selain itu Belum / Sedang / Selesai. */
    private static String statusTeks(Tugas t) {
        return t.isDijeda() ? "Dijeda" : Format.kapital(t.getStatus());
    }

    private static String potong(String teks) {
        return teks.length() > 40 ? teks.substring(0, 37) + "..." : teks;
    }

    /**
     * Notifikasi kecil di pojok kanan bawah jendela + bunyi "bip".
     * Hanya muncul kalau saklar Notifikasi di Pengaturan menyala.
     */
    private void beritahu(String judul, String isi, Color aksen) {
        if (!Preferensi.isNotifikasiAktif()) {
            return;
        }
        Window jendela = SwingUtilities.getWindowAncestor(this);
        if (jendela == null || !jendela.isShowing()) {
            return;
        }
        Toolkit.getDefaultToolkit().beep();
        new Toast(jendela, judul, isi, aksen).tampil();
    }

    /** Popup kecil tanpa bingkai, hilang sendiri setelah 6 detik atau saat diklik. */
    private static class Toast extends JWindow {

        private static final List<Toast> AKTIF = new ArrayList<>();
        private final Window pemilik;

        Toast(Window pemilik, String judul, String isi, Color aksen) {
            super(pemilik);
            this.pemilik = pemilik;
            setFocusableWindowState(false);
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Tema.GARIS),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 6, 0, 0, aksen),
                            BorderFactory.createEmptyBorder(12, 14, 12, 18))));
            p.add(Tema.label(judul, Font.BOLD, 14f, Tema.TEKS));
            p.add(Box.createVerticalStrut(3));
            p.add(Tema.label(isi, Font.PLAIN, 13f, Tema.TEKS_REDUP));
            setContentPane(p);
            pack();
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    tutup();
                }
            });
        }

        void tampil() {
            int tumpuk = 0;
            for (Toast t : AKTIF) {
                tumpuk += t.getHeight() + 8;
            }
            Point lokasi = pemilik.getLocationOnScreen();
            setLocation(lokasi.x + pemilik.getWidth() - getWidth() - 24,
                    lokasi.y + pemilik.getHeight() - getHeight() - 24 - tumpuk);
            AKTIF.add(this);
            setAlwaysOnTop(true);
            setVisible(true);
            Timer t = new Timer(6000, e -> tutup());
            t.setRepeats(false);
            t.start();
        }

        void tutup() {
            AKTIF.remove(this);
            dispose();
        }
    }

    /** 125 menit menjadi "2 jam 5 menit". */
    static String teksDurasi(int menit) {
        int j = menit / 60;
        int m = menit % 60;
        if (j > 0 && m > 0) {
            return j + " jam " + m + " menit";
        }
        return j > 0 ? j + " jam" : m + " menit";
    }

    /** Sisa waktu dalam bahasa biasa: "1 jam 59 menit", "45 menit", "30 detik". */
    static String teksSisa(long millis) {
        long detik = (millis + 999) / 1000;
        long j = detik / 3600;
        long m = (detik % 3600) / 60;
        long d = detik % 60;
        if (j > 0) {
            return j + " jam" + (m > 0 ? " " + m + " menit" : "");
        }
        if (m > 0) {
            return m + " menit" + (m < 5 && d > 0 ? " " + d + " detik" : "");
        }
        return d + " detik";
    }

    /** Jam hitung mundur: 01:59:32. */
    static String jam(long millis) {
        long detik = (millis + 999) / 1000;
        return String.format("%02d:%02d:%02d", detik / 3600, (detik % 3600) / 60, detik % 60);
    }

    /** Bilah progres bersudut bulat, warna mengikuti tema (biru, hijau kalau selesai). */
    private static class BilahProgres extends JComponent {

        private double nilai;
        private Color warna = Tema.PRIMER;

        void setNilai(double nilai, Color warna) {
            this.nilai = nilai;
            this.warna = warna;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g.setColor(Tema.BIRU_MUDA);
            g.fillRoundRect(0, 0, w, h, h, h);
            int isi = (int) Math.round(w * nilai);
            if (isi > 0) {
                g.setColor(warna);
                g.fillRoundRect(0, 0, Math.max(isi, h), h, h, h);
            }
            g.dispose();
        }
    }

    /** Sel tabel kolom Progres: sisa waktu + bilah kecil untuk tugas Sistem, "-" untuk tugas manual. */
    private static class RendererProgres extends JPanel implements TableCellRenderer {

        private final JLabel teks = Tema.label(" ", Font.PLAIN, 12f, Tema.TEKS_REDUP);
        private final BilahProgres bilah = new BilahProgres();

        RendererProgres() {
            super(new BorderLayout(0, 4));
            add(teks, BorderLayout.NORTH);
            bilah.setPreferredSize(new Dimension(10, 8));
            add(bilah, BorderLayout.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel,
                boolean fokus, int row, int col) {
            Tugas t = (Tugas) value;
            long sekarang = System.currentTimeMillis();
            setBackground(sel ? new Color(0xEFF6FF) : Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.GARIS),
                    BorderFactory.createEmptyBorder(9, 12, 9, 12)));
            bilah.setVisible(t.isPjSistem());
            if (!t.isPjSistem()) {
                teks.setText("-");
                teks.setForeground(Tema.TEKS_REDUP);
            } else if (Tugas.SELESAI.equals(t.getStatus())) {
                teks.setText("Selesai");
                teks.setForeground(Tema.HIJAU_TUA);
                bilah.setNilai(1, Tema.HIJAU);
            } else if (t.getMulaiPada() == null) {
                teks.setText("Siap dimulai (" + teksDurasi(t.getDurasiMenit()) + ")");
                teks.setForeground(Tema.TEKS_REDUP);
                bilah.setNilai(0, Tema.PRIMER);
            } else if (t.isDijeda()) {
                teks.setText("Dijeda " + jam(t.sisaMillis(sekarang)));
                teks.setForeground(Tema.TEKS_REDUP);
                bilah.setNilai(t.progres(sekarang), WARNA_JEDA);
            } else if (t.isHampirSelesai(sekarang)) {
                teks.setText(jam(t.sisaMillis(sekarang)) + " tersisa");
                teks.setForeground(Tema.ORANYE_TUA);
                bilah.setNilai(t.progres(sekarang), Tema.ORANYE);
            } else {
                teks.setText(jam(t.sisaMillis(sekarang)) + " tersisa");
                teks.setForeground(Tema.PRIMER);
                bilah.setNilai(t.progres(sekarang), Tema.PRIMER);
            }
            return this;
        }
    }
}