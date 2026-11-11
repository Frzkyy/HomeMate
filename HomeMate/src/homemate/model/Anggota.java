package homemate.model;

/** Satu anggota keluarga, beserta jumlah tugasnya (dihitung dari tabel tugas). */
public class Anggota {

    private int id;
    private String nama;
    private String peran;
    private int tugasSelesai;
    private int totalTugas;

    /** Dipakai saat tambah/ubah (jumlah tugas belum relevan). */
    public Anggota(int id, String nama, String peran) {
        this(id, nama, peran, 0, 0);
    }

    /** Dipakai saat membaca dari database. */
    public Anggota(int id, String nama, String peran, int tugasSelesai, int totalTugas) {
        this.id = id;
        this.nama = nama;
        this.peran = peran;
        this.tugasSelesai = tugasSelesai;
        this.totalTugas = totalTugas;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getPeran() { return peran; }
    public void setPeran(String peran) { this.peran = peran; }
    public int getTugasSelesai() { return tugasSelesai; }
    public int getTotalTugas() { return totalTugas; }
}
