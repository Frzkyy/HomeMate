package homemate.model;

import java.time.LocalDate;

/**
 * KELAS INDUK (PARENT) sekaligus KELAS ABSTRAK untuk semua data utama HomeMate.
 * Barang, Tugas, Perawatan, dan Transaksi adalah anak (CHILD) dari kelas ini.
 *
 * Empat pilar PBO di kelas ini:
 *  - Abstraction   : kelas ini abstract, tidak bisa dibuat objeknya (new ItemRumah() error).
 *                    Ia hanya menetapkan "kontrak": setiap item rumah WAJIB punya jenis,
 *                    judul, dan detail (3 method abstract di bawah).
 *  - Encapsulation : field id private, hanya bisa diakses lewat getId()/setId().
 *  - Inheritance   : id, getId(), setId(), ringkas(), toString() diwariskan ke semua anak,
 *                    jadi tidak perlu ditulis ulang di tiap kelas.
 *  - Polymorphism  : method yang sama (getModul, getJudul, getDetail, buatPengingat)
 *                    dijalankan berbeda oleh tiap anak. Lihat RingkasanDAO.pengingat().
 */
public abstract class ItemRumah {

    // ENCAPSULATION: private, tidak bisa disentuh langsung dari luar kelas
    private int id;

    protected ItemRumah() {
    }

    protected ItemRumah(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // ===== ABSTRACTION: method tanpa isi, wajib diisi (override) oleh setiap anak =====

    /** Modul asal item, misalnya "Persediaan", "Tugas", "Perawatan", "Keuangan". */
    public abstract String getModul();

    /** Judul singkat item untuk ditampilkan (nama barang, judul tugas, dst). */
    public abstract String getJudul();

    /** Keterangan tambahan satu baris (jumlah stok, tenggat, lokasi, nominal, dst). */
    public abstract String getDetail();

    // ===== POLYMORPHISM: method biasa yang BOLEH di-override anak =====

    /**
     * Buat pengingat otomatis untuk item ini pada tanggal hariIni.
     * Bawaan: tidak ada pengingat (null). Anak yang punya aturan pengingat meng-override-nya.
     */
    public Pengingat buatPengingat(LocalDate hariIni) {
        return null;
    }

    // ===== INHERITANCE: dipakai bersama oleh semua anak =====

    /** Contoh: "[Persediaan] Beras - Sisa 2 kg (batas minimum 5, Menipis)". */
    public final String ringkas() {
        return "[" + getModul() + "] " + getJudul() + " - " + getDetail();
    }

    @Override
    public String toString() {
        return ringkas();
    }
}
