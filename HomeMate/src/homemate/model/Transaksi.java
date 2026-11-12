package homemate.model;

/** Satu catatan keuangan (pemasukan atau pengeluaran). Milik Ketrin. */
public class Transaksi {

    public static final String PEMASUKAN = "PEMASUKAN";
    public static final String PENGELUARAN = "PENGELUARAN";

    private int id;
    private String jenis;       // PEMASUKAN / PENGELUARAN
    private String kategori;
    private long nominal;       // Rupiah, bilangan bulat
    private String tanggal;     // yyyy-MM-dd
    private String keterangan;

    public Transaksi() {
    }

    public Transaksi(int id, String jenis, String kategori, long nominal, String tanggal, String keterangan) {
        this.id = id;
        this.jenis = jenis;
        this.kategori = kategori;
        this.nominal = nominal;
        this.tanggal = tanggal;
        this.keterangan = keterangan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public long getNominal() { return nominal; }
    public void setNominal(long nominal) { this.nominal = nominal; }
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public String getKeterangan() { return keterangan; }
    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
}
