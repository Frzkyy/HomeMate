package homemate.model;

/** Satu pengingat otomatis (dihitung dari data, tidak disimpan di database). */
public class Pengingat {

    public static final String TUGAS = "Tugas";
    public static final String PERSEDIAAN = "Persediaan";
    public static final String PERAWATAN = "Perawatan";
    public static final String KEUANGAN = "Keuangan";

    private final String jenis;
    private final String judul;
    private final String keterangan;
    private final String tanggal; // yyyy-MM-dd

    public Pengingat(String jenis, String judul, String keterangan, String tanggal) {
        this.jenis = jenis;
        this.judul = judul;
        this.keterangan = keterangan;
        this.tanggal = tanggal;
    }

    public String getJenis() { return jenis; }
    public String getJudul() { return judul; }
    public String getKeterangan() { return keterangan; }
    public String getTanggal() { return tanggal; }
}
