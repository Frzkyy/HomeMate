# Decisions

Format: tanggal, keputusan, alasan singkat. Tambah di paling bawah.

## 2026-11-11 - Awal proyek
- **Nama:** HomeMate (sebelumnya HomeFlow di PRD awal).
- **Stack awal:** Java 21, Maven, JavaFX, SQLite, BCrypt. *Usulan, belum dikonfirmasi. Ganti kalau lu pakai Swing atau DB lain.*
- **Arsitektur:** layered (ui, service, repository, model) + package `algorithm` terpisah supaya mudah dites.
- **Uang:** disimpan sebagai integer Rupiah, menghindari error floating point.
- **Hapus anggota (usulan):** tugas milik anggota jadi "tanpa penanggung jawab", riwayat poin tetap tersimpan. Menghapus anggota yang punya tugas aktif butuh konfirmasi.
- **Hapus barang inventaris (usulan):** item daftar belanja terkait tetap ada, tapi `inventory_item_id` menjadi null.
- **Poin:** dijamin satu kali per tugas lewat UNIQUE `task_id` di `PointHistory`.
- **Peran/hak akses:** PRD menyebut 3 jenis pengguna tapi tidak merinci hak akses. Ditunda ke fase akhir, awalnya semua pengguna punya akses penuh.
- **Registrasi:** mockup login punya tautan "Daftar", PRD tidak punya fiturnya. Ditandai opsional.

## Pertanyaan terbuka
- Apakah satu rumah satu database lokal, atau perlu multi-rumah?
- Apakah anggota keluarga punya akun login sendiri, atau hanya satu akun untuk satu rumah?
- Ambang pengingat anggaran: 80% atau bisa diatur pengguna?
