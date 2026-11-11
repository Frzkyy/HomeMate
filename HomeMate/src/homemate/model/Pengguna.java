package homemate.model;

/** Data pengguna yang sedang login. */
public class Pengguna {

    private int id;
    private String username;
    private String nama;

    public Pengguna(int id, String username, String nama) {
        this.id = id;
        this.username = username;
        this.nama = nama;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getNama() { return nama; }
}
