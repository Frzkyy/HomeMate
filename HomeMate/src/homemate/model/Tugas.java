package homemate.model;

/** Data satu tugas rumah (tabel tugas). */
public class Tugas {

    public static final String PRIORITAS_RENDAH = "RENDAH";
    public static final String PRIORITAS_SEDANG = "SEDANG";
    public static final String PRIORITAS_TINGGI = "TINGGI";

    public static final String STATUS_BELUM = "BELUM";
    public static final String STATUS_SEDANG = "SEDANG";
    public static final String STATUS_SELESAI = "SELESAI";

    private int id;
    private String judul;
    private String deskripsi;
    private Integer anggotaId;      // null kalau belum ada penanggung jawab
    private String namaAnggota;     // hanya untuk tampilan di tabel (hasil JOIN), tidak disimpan
    private String prioritas;       // RENDAH / SEDANG / TINGGI
    private String tenggat;         // yyyy-MM-dd, boleh null
    private String status;          // BELUM / SEDANG / SELESAI

    public Tugas() {
        this.prioritas = PRIORITAS_SEDANG;
        this.status = STATUS_BELUM;
    }

    public Tugas(int id, String judul, String deskripsi, Integer anggotaId, String namaAnggota,
                 String prioritas, String tenggat, String status) {
        this.id = id;
        this.judul = judul;
        this.deskripsi = deskripsi;
        this.anggotaId = anggotaId;
        this.namaAnggota = namaAnggota;
        this.prioritas = prioritas;
        this.tenggat = tenggat;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public Integer getAnggotaId() { return anggotaId; }
    public void setAnggotaId(Integer anggotaId) { this.anggotaId = anggotaId; }

    public String getNamaAnggota() { return namaAnggota; }
    public void setNamaAnggota(String namaAnggota) { this.namaAnggota = namaAnggota; }

    public String getPrioritas() { return prioritas; }
    public void setPrioritas(String prioritas) { this.prioritas = prioritas; }

    public String getTenggat() { return tenggat; }
    public void setTenggat(String tenggat) { this.tenggat = tenggat; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /** Bobot prioritas untuk pengurutan: TINGGI = 3, SEDANG = 2, RENDAH = 1. */
    public int getBobotPrioritas() {
        if (PRIORITAS_TINGGI.equals(prioritas)) return 3;
        if (PRIORITAS_SEDANG.equals(prioritas)) return 2;
        return 1;
    }

    public boolean isSelesai() {
        return STATUS_SELESAI.equals(status);
    }
}