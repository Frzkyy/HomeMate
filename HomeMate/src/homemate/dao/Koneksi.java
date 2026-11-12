package homemate.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Koneksi ke database SQLite (file homemate.db) dan pembuatan tabel.
 * Milik Said. Mau ubah/tambah tabel? Bilang Said dulu.
 */
public class Koneksi {

    private static final String URL = "jdbc:sqlite:homemate.db";

    /** Buka koneksi. Pakai: try (Connection c = Koneksi.getConnection()) { ... } */
    public static Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(URL);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    /** Bikin semua tabel kalau belum ada. Aman dipanggil berkali-kali. */
    public static void buatTabel() throws SQLException {
        String[] sql = {
            """
            CREATE TABLE IF NOT EXISTS pengguna (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                nama TEXT
            )""",
            """
            CREATE TABLE IF NOT EXISTS anggota (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                peran TEXT
            )""",
            """
            CREATE TABLE IF NOT EXISTS tugas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                judul TEXT NOT NULL,
                deskripsi TEXT,
                anggota_id INTEGER REFERENCES anggota(id) ON DELETE SET NULL,
                prioritas TEXT NOT NULL DEFAULT 'SEDANG'
                    CHECK (prioritas IN ('RENDAH','SEDANG','TINGGI')),
                tenggat TEXT,
                status TEXT NOT NULL DEFAULT 'BELUM'
                    CHECK (status IN ('BELUM','SEDANG','SELESAI'))
            )""",
            """
            CREATE TABLE IF NOT EXISTS barang (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                jumlah INTEGER NOT NULL DEFAULT 0 CHECK (jumlah >= 0),
                satuan TEXT,
                batas_minimum INTEGER NOT NULL DEFAULT 0
            )""",
            """
            CREATE TABLE IF NOT EXISTS transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                jenis TEXT NOT NULL CHECK (jenis IN ('PEMASUKAN','PENGELUARAN')),
                kategori TEXT NOT NULL,
                nominal INTEGER NOT NULL CHECK (nominal > 0),
                tanggal TEXT NOT NULL,
                keterangan TEXT
            )""",
            """
            CREATE TABLE IF NOT EXISTS anggaran (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bulan TEXT NOT NULL UNIQUE,
                jumlah INTEGER NOT NULL CHECK (jumlah > 0)
            )""",
            """
            CREATE TABLE IF NOT EXISTS perawatan (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                lokasi TEXT,
                tanggal TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'TERJADWAL'
                    CHECK (status IN ('TERJADWAL','SELESAI'))
            )""",
            
            """
            CREATE TABLE IF NOT EXISTS notifikasi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                judul TEXT NOT NULL,
                pesan TEXT NOT NULL,
                tipe TEXT NOT NULL,
                tugas_id INTEGER,
                waktu TEXT NOT NULL,
                dibaca INTEGER DEFAULT 0,
                kode_unik TEXT UNIQUE,
                FOREIGN KEY (tugas_id)
                    REFERENCES tugas(id)
                    ON DELETE SET NULL
            )""",

            
            // Akun awal supaya bisa login: admin / admin123
            "INSERT OR IGNORE INTO pengguna (username, password, nama) VALUES ('admin','admin123','Admin')"
                
            
        };
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            for (String s : sql) {
                st.execute(s);
            }
        }
    }
}
