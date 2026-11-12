package homemate.model;

/** Data satu jadwal perawatan rumah (tabel perawatan). Milik Riva. */
public class Perawatan extends ItemRumah {

    private String nama;
    private String lokasi;
    private String tanggal;   // format yyyy-MM-dd
    private String status;    // TERJADWAL atau SELESAI

    public Perawatan(int id, String nama, String lokasi, String tanggal, String status) {
        super(id);
        this.nama = nama;
        this.lokasi = lokasi;
        this.tanggal = tanggal;
        this.status = status;
    }

    public String getNama() { return nama; }
    public String getLokasi() { return lokasi; }
    public String getTanggal() { return tanggal; }
    public String getStatus() { return status; }

    public void setNama(String nama) { this.nama = nama; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public void setStatus(String status) { this.status = status; }

    // Mengisi method abstract dari ItemRumah
    @Override public String getModul() { return Pengingat.PERAWATAN; }
    @Override public String getJudul() { return nama; }
    @Override public String getDetail() { return (lokasi == null ? "-" : lokasi) + ", " + tanggal + ", " + status; }
}
