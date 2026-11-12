package homemate.model;

/** Data satu tugas rumah. Milik Carlos (modul Tugas Rumah). */
public class Tugas {

    public static final String BELUM = "BELUM";
    public static final String SEDANG = "SEDANG";
    public static final String SELESAI = "SELESAI";

    /** Sisa waktu (menit) saat peringatan "hampir selesai" muncul. */
    public static final int AMBANG_PERINGATAN_MENIT = 5;

    private int id;
    private String judul;
    private String deskripsi;
    private Integer anggotaId;      // null kalau penanggung jawabnya Sistem / kosong
    private String namaAnggota;
    private String prioritas = "SEDANG";
    private String tenggat;         // yyyy-MM-dd, boleh kosong
    private String status = BELUM;

    /** true = penanggung jawab Sistem (dikerjakan otomatis dengan hitung mundur). */
    private boolean pjSistem;
    private int durasiMenit;        // lama pengerjaan oleh Sistem
    private Long mulaiPada;         // waktu tombol Mulai ditekan (epoch millis), null = belum
    private Long sisaJeda;          // sisa waktu (ms) saat dijeda, null = tidak sedang dijeda

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJudul() {
        return judul;
    }

    public void setJudul(String judul) {
        this.judul = judul;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public Integer getAnggotaId() {
        return anggotaId;
    }

    public void setAnggotaId(Integer anggotaId) {
        this.anggotaId = anggotaId;
    }

    public String getNamaAnggota() {
        return namaAnggota;
    }

    public void setNamaAnggota(String namaAnggota) {
        this.namaAnggota = namaAnggota;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getTenggat() {
        return tenggat;
    }

    public void setTenggat(String tenggat) {
        this.tenggat = tenggat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isPjSistem() {
        return pjSistem;
    }

    public void setPjSistem(boolean pjSistem) {
        this.pjSistem = pjSistem;
    }

    public int getDurasiMenit() {
        return durasiMenit;
    }

    public void setDurasiMenit(int durasiMenit) {
        this.durasiMenit = durasiMenit;
    }

    public Long getMulaiPada() {
        return mulaiPada;
    }

    public void setMulaiPada(Long mulaiPada) {
        this.mulaiPada = mulaiPada;
    }

    public Long getSisaJeda() {
        return sisaJeda;
    }

    public void setSisaJeda(Long sisaJeda) {
        this.sisaJeda = sisaJeda;
    }

    // ===== Bantuan hitung mundur =====

    /** Tugas Sistem yang sedang dijeda (hitung mundur berhenti). */
    public boolean isDijeda() {
        return pjSistem && SEDANG.equals(status) && sisaJeda != null;
    }

    /** Nama penanggung jawab untuk ditampilkan. */
    public String getPenanggungJawab() {
        if (pjSistem) {
            return "Sistem";
        }
        return namaAnggota == null ? "-" : namaAnggota;
    }

    /** Tugas Sistem yang sudah dimulai, belum selesai, dan tidak dijeda (hitung mundur sedang jalan). */
    public boolean isBerjalan() {
        return pjSistem && SEDANG.equals(status) && mulaiPada != null && sisaJeda == null;
    }

    /** Hitung mundur sedang jalan dan sisa waktunya tinggal AMBANG_PERINGATAN_MENIT atau kurang. */
    public boolean isHampirSelesai(long sekarang) {
        if (!isBerjalan() || durasiMenit <= AMBANG_PERINGATAN_MENIT) {
            return false;
        }
        return sisaMillis(sekarang) <= AMBANG_PERINGATAN_MENIT * 60_000L;
    }

    /** Sisa waktu dalam milidetik (0 kalau sudah habis). Untuk tugas yang belum mulai = seluruh durasi. */
    public long sisaMillis(long sekarang) {
        long total = durasiMenit * 60_000L;
        if (SELESAI.equals(status)) {
            return 0;
        }
        if (isDijeda()) {
            return sisaJeda;
        }
        if (mulaiPada == null) {
            return total;
        }
        return Math.max(0, mulaiPada + total - sekarang);
    }

    /** Progres 0.0 sampai 1.0. */
    public double progres(long sekarang) {
        long total = durasiMenit * 60_000L;
        if (SELESAI.equals(status)) {
            return 1.0;
        }
        if (mulaiPada == null || total <= 0) {
            return 0.0;
        }
        if (isDijeda()) {
            return Math.min(1.0, Math.max(0.0, 1.0 - sisaJeda / (double) total));
        }
        return Math.min(1.0, Math.max(0.0, (sekarang - mulaiPada) / (double) total));
    }
}