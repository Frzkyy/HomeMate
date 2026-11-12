package homemate.model;

public class Notifikasi {

    private int id;
    private String judul;
    private String pesan;
    private String tipe;
    private Integer tugasId;
    private String waktu;
    private boolean dibaca;

    public Notifikasi(int id, String judul, String pesan,
            String tipe, Integer tugasId, String waktu,
            boolean dibaca) {
        this.id = id;
        this.judul = judul;
        this.pesan = pesan;
        this.tipe = tipe;
        this.tugasId = tugasId;
        this.waktu = waktu;
        this.dibaca = dibaca;
    }

    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getPesan() { return pesan; }
    public String getTipe() { return tipe; }
    public Integer getTugasId() { return tugasId; }
    public String getWaktu() { return waktu; }
    public boolean isDibaca() { return dibaca; }

    public void setDibaca(boolean dibaca) {
        this.dibaca = dibaca;
    }
}