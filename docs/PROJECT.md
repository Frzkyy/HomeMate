# HomeMate: Panduan Proyek

Aplikasi desktop untuk mengelola rumah tangga. Fokusnya CRUD (tambah, lihat, ubah, hapus) per modul. Tugas kuliah Java, jadi dibuat sederhana.

**Teknologi:** Java, Swing (dibuat dengan NetBeans GUI Builder), SQLite (satu file `homemate.db`, tanpa install server).

## Struktur folder

```
HomeMate/                 <- project NetBeans
├── lib/                  <- jar sqlite-jdbc
└── src/homemate/
    ├── HomeMate.java     <- main (titik mulai)
    ├── MainFrame.java    <- window utama + sidebar menu
    ├── panel/            <- TAMPILAN: satu file JPanel per layar
    │   ├── LoginFrame, DashboardPanel
    │   ├── AnggotaPanel, TugasPanel, PersediaanPanel
    │   └── KeuanganPanel, PerawatanPanel, LaporanPanel
    ├── model/            <- DATA: class biasa (Anggota.java, Tugas.java, ...)
    └── dao/              <- BACKEND: kode yang bicara ke database
        ├── Koneksi.java      (koneksi + bikin tabel, milik Said)
        └── AnggotaDAO.java, TugasDAO.java, ...
```

**Folder backend namanya `dao`** (Data Access Object). Isinya semua kode SQL. `model` isinya class data (field + getter/setter).

**Alurnya:** `Panel` (user klik tombol) memanggil `DAO` (jalankan SQL) yang membaca atau menulis `Model`.

Modul yang selesai duluan (misalnya Anggota) jadi contoh pola CRUD buat yang lain.

## Pembagian tim

| Orang | Modul | File yang dibuat |
|---|---|---|
| **Said** | Setup, Login, Dashboard, Laporan, gabung semua | `HomeMate`, `MainFrame`, `Koneksi`, `LoginFrame`, `DashboardPanel`, `LaporanPanel` |
| **Tifa** | Anggota Keluarga | `AnggotaPanel`, `Anggota`, `AnggotaDAO` |
| **Carlos** | Tugas Rumah | `TugasPanel`, `Tugas`, `TugasDAO` |
| **Devin** | Persediaan + daftar belanja | `PersediaanPanel`, `Barang`, `BarangDAO` |
| **Ketrin** | Keuangan + anggaran | `KeuanganPanel`, `Transaksi`, `TransaksiDAO` |
| **Riva** | Perawatan Rumah | `PerawatanPanel`, `Perawatan`, `PerawatanDAO` |

Aturan: kerjakan file milik sendiri saja. Mau ubah `MainFrame`, `Koneksi`, atau tabel database? Minta Said.

## Daftar fitur

Tandai `[x]` kalau sudah jadi.

**Said**
- [ ] Project jalan, database + tabel, sidebar menu
- [ ] Login (cek username + password di tabel `pengguna`)
- [ ] Dashboard: 4 kartu (tugas selesai, tugas tertunda, stok menipis, total pengeluaran) + daftar pengingat
- [ ] Laporan: pilih rentang tanggal, tampilkan ringkasan tugas, stok, keuangan, perawatan

**Tifa, Anggota:** tabel anggota + tambah / ubah / hapus.
- [ ] CRUD anggota
- [ ] Kolom jumlah tugas selesai / total tugas per anggota

**Carlos, Tugas:**
- [ ] CRUD tugas (judul, penanggung jawab dari daftar anggota, prioritas, tenggat)
- [ ] Ubah status: Belum, Sedang, Selesai
- [ ] Urutkan berdasarkan prioritas / tenggat
- [ ] Cari tugas berdasarkan judul

**Devin, Persediaan:**
- [ ] CRUD barang (jumlah, satuan, batas minimum)
- [ ] Status otomatis: Menipis kalau `jumlah <= batas_minimum`, selain itu Aman
- [ ] Daftar belanja = barang yang Menipis. Tombol "Tandai dibeli" menambah stok

**Ketrin, Keuangan:**
- [ ] CRUD transaksi (jenis, kategori, nominal, tanggal, keterangan)
- [ ] Ringkasan: total pemasukan, total pengeluaran, saldo
- [ ] Anggaran per bulan + sisa anggaran

**Riva, Perawatan:**
- [ ] CRUD jadwal perawatan (nama, lokasi, tanggal)
- [ ] Tombol "Tandai selesai" (status jadi SELESAI = masuk riwayat)

**Pengingat** tidak punya tabel sendiri. Dashboard menghitungnya langsung dari data:
- tugas belum selesai dengan tenggat hari ini / besok / lewat
- barang yang Menipis
- perawatan dalam 7 hari ke depan
- pengeluaran bulan ini sudah >= 80% anggaran

**Opsional (kalau waktu sisa):** sistem poin, bagi tugas otomatis, kalender, tema gelap, grafik pengeluaran, hash password (BCrypt), ekspor laporan.

## Tabel database

Dibuat otomatis oleh `Koneksi.buatTabel()`. Tanggal disimpan teks `yyyy-MM-dd`. Uang disimpan angka bulat (Rupiah).

| Tabel | Kolom |
|---|---|
| `pengguna` | id, username, password, nama |
| `anggota` | id, nama, peran |
| `tugas` | id, judul, deskripsi, anggota_id, prioritas (RENDAH/SEDANG/TINGGI), tenggat, status (BELUM/SEDANG/SELESAI) |
| `barang` | id, nama, jumlah, satuan, batas_minimum |
| `transaksi` | id, jenis (PEMASUKAN/PENGELUARAN), kategori, nominal, tanggal, keterangan |
| `anggaran` | id, bulan (`yyyy-MM`), jumlah |
| `perawatan` | id, nama, lokasi, tanggal, status (TERJADWAL/SELESAI) |

Kalau anggota dihapus, tugasnya tetap ada tapi penanggung jawabnya jadi kosong.

## Aturan validasi
- Field wajib tidak boleh kosong
- Jumlah barang tidak boleh negatif, nominal transaksi harus lebih dari 0
- Penanggung jawab tugas harus dipilih dari daftar anggota
- Hapus data selalu minta konfirmasi
- Pesan error tampil lewat `JOptionPane`, bahasa Indonesia

## Git (sederhana)
- Branch: `main` (stabil), `develop` (gabungan), `fitur/<nama>` per orang, contoh `fitur/tugas-carlos`
- Mau kerja: tarik `develop` dulu. Selesai: buat Pull Request ke `develop`. Said yang merge
- Commit pesan singkat: `feat: tambah CRUD tugas`, `fix: ...`
- Sebelum PR: aplikasi harus bisa jalan

## Pakai AI
Di awal sesi bilang "Gw Carlos" (atau nama lain). AI akan membatasi kerja ke modul itu, sesuai `AGENTS.md`.
