package homemate.model;

import java.time.LocalDate;

/**
 * Satu barang persediaan rumah tangga (tabel barang).
 * INHERITANCE: Barang adalah anak dari ItemRumah (id, getId, setId, ringkas, toString diwariskan).
 */
public class Barang extends ItemRumah {

    // ENCAPSULATION: semua field private
    private String nama;
    private int jumlah;
    private String satuan;
    private int batasMinimum;

    public Barang(int id, String nama, int jumlah, String satuan, int batasMinimum) {
        super(id); // memanggil constructor kelas induk
        this.nama = nama;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.batasMinimum = batasMinimum;
    }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public int getJumlah() { return jumlah; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    public int getBatasMinimum() { return batasMinimum; }

    /** ENCAPSULATION: nilai tidak boleh negatif, ditolak di sini. */
    public void setJumlah(int jumlah) {
        if (jumlah < 0) {
            throw new IllegalArgumentException("Jumlah tidak boleh negatif: " + jumlah);
        }
        this.jumlah = jumlah;
    }

    public void setBatasMinimum(int batasMinimum) {
        if (batasMinimum < 0) {
            throw new IllegalArgumentException("Batas minimum tidak boleh negatif: " + batasMinimum);
        }
        this.batasMinimum = batasMinimum;
    }

    /** Tambah stok (misalnya saat "Tandai dibeli"). */
    public void tambahStok(int banyak) {
        if (banyak <= 0) {
            throw new IllegalArgumentException("Jumlah tambahan harus lebih dari 0: " + banyak);
        }
        jumlah += banyak;
    }

    /** Kurangi stok (dipakai). Stok tidak boleh jadi negatif. */
    public void kurangiStok(int banyak) {
        if (banyak <= 0 || banyak > jumlah) {
            throw new IllegalArgumentException("Pengurangan tidak valid: " + banyak + " (stok " + jumlah + ")");
        }
        jumlah -= banyak;
    }

    /** Menipis kalau jumlah <= batas minimum (aturan bisnis nomor 6). */
    public boolean isMenipis() {
        return jumlah <= batasMinimum;
    }

    /** Teks status untuk badge: "Menipis" atau "Aman". */
    public String getStatus() {
        return isMenipis() ? "Menipis" : "Aman";
    }

    // ===== ABSTRACTION: mengisi 3 method abstract dari ItemRumah =====

    @Override
    public String getModul() {
        return Pengingat.PERSEDIAAN;
    }

    @Override
    public String getJudul() {
        return nama;
    }

    @Override
    public String getDetail() {
        return "Sisa " + jumlahDenganSatuan(jumlah) + " (batas minimum " + batasMinimum + ", " + getStatus() + ")";
    }

    // ===== POLYMORPHISM: aturan pengingat khusus Barang =====

    /** Pengingat hanya muncul kalau stok Menipis. */
    @Override
    public Pengingat buatPengingat(LocalDate hariIni) {
        if (!isMenipis()) {
            return null;
        }
        return new Pengingat(Pengingat.PERSEDIAAN,
                "Stok " + nama.trim().toLowerCase() + " menipis",
                "Sisa " + jumlahDenganSatuan(jumlah) + " (batas minimum " + jumlahDenganSatuan(batasMinimum) + ")",
                hariIni.toString());
    }

    /** Contoh: "2 kg"; kalau satuan kosong cukup "2". */
    private String jumlahDenganSatuan(int angka) {
        return (angka + " " + (satuan == null ? "" : satuan)).trim();
    }
}
