# HomeMate (instruksi untuk AI assistant)

Tugas kuliah Java: aplikasi desktop CRUD sederhana. Java + Swing (NetBeans GUI Builder) + SQLite. Jaga tetap sederhana, jangan menambah lapisan atau fitur di luar `docs/PROJECT.md`.

## Wajib
1. Baca `docs/PROJECT.md` sebelum ngoding.
2. Di awal sesi, cari tahu siapa user (tanya "lu siapa?" kalau belum bilang). Jangan menebak.
3. Kerjakan HANYA file modul milik user itu (lihat tabel tim di `docs/PROJECT.md`). Jangan edit file modul orang lain.
4. `MainFrame.java`, `dao/Koneksi.java`, dan skema tabel hanya Said yang ubah. Kalau perlu diubah, tulis permintaannya ke user untuk diteruskan ke Said.

## Aturan kode
- Struktur: `panel/` (tampilan), `model/` (data), `dao/` (kode database). Panel tidak boleh menulis SQL, panggil DAO.
- Sebelum bikin DAO/model baru, lihat DAO/model modul lain yang sudah ada di repo dan tiru polanya.
- SQL wajib `PreparedStatement`. Jangan string concatenation.
- Jangan edit bagian "Generated Code" di file panel (dibuat NetBeans GUI Builder, nanti rusak). Tambah logika di method handler (klik tombol, dst).
- Validasi input sebelum simpan (field wajib, angka, tanggal). Tampilkan error pakai `JOptionPane`.
- Hapus data selalu minta konfirmasi dulu.
- Semua teks yang tampil ke pengguna: bahasa Indonesia. Nama class/method/variabel: ikuti gaya file yang sudah ada di repo.
- Balasan ke user: bahasa Indonesia santai, singkat.
