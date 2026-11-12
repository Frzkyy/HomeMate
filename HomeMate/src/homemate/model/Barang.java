package homemate.model;

/** Satu barang persediaan rumah tangga (tabel barang). */
public class Barang {

    private int id;
    private String nama;
    private int jumlah;
    private String satuan;
    private int batasMinimum;

    public Barang(int id, String nama, int jumlah, String satuan, int batasMinimum) {
        this.id = id;
        this.nama = nama;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.batasMinimum = batasMinimum;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public int getJumlah() { return jumlah; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    public int getBatasMinimum() { return batasMinimum; }
    public void setBatasMinimum(int batasMinimum) { this.batasMinimum = batasMinimum; }

    /** Menipis kalau jumlah <= batas minimum (aturan bisnis nomor 6). */
    public boolean isMenipis() {
        return jumlah <= batasMinimum;
    }

    /** Teks status untuk badge: "Menipis" atau "Aman". */
    public String getStatus() {
        return isMenipis() ? "Menipis" : "Aman";
    }
}
