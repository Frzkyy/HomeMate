# HomeMate

Aplikasi desktop Java untuk manajemen rumah tangga: anggota keluarga, tugas, persediaan, keuangan, perawatan rumah, pengingat, laporan.

## Stack (usulan, ubah di docs/decisions.md kalau berbeda)
- Java 21, Maven
- GUI: JavaFX
- Database: SQLite (JDBC)
- Hashing password: BCrypt
- Package root: `com.homemate`

## Urutan baca
Baca SELALU sebelum ngoding:
1. `docs/00-overview.md` : tujuan, modul, aturan bisnis
2. `docs/01-architecture.md` : struktur package dan layer
3. `docs/05-conventions.md` : aturan coding
4. `docs/07-team.md` : siapa pemilik modul apa, aturan file bersama, git workflow

Baca HANYA kalau relevan:
- `docs/02-database.md` : kalau nyentuh model, DAO, atau query
- `docs/03-features.md` : cek status fitur, update setelah selesai
- `docs/04-algorithms.md` : kalau nyentuh logika sorting, stok, keuangan, pengingat, poin, auto-assign, laporan
- `docs/06-ui.md` : kalau bikin atau ubah layar
- `docs/decisions.md` : kalau ragu kenapa sesuatu dibuat begitu
- `docs/PRD.md` : dokumen kebutuhan lengkap, hanya kalau docs lain kurang detail (panjang, jangan dibaca penuh)

## Tim (6 orang)
Tim: Said (integrator), Tifa, Carlos, Devin, Ketrin, Riva. Detail pembagian ada di `docs/07-team.md`.
- Di awal sesi, cari tahu siapa user (dari `AGENTS.local.md` kalau ada, atau tanya "lu siapa?"). Jangan menebak.
- Kerjakan HANYA modul milik user itu. Jangan edit file modul orang lain.
- File bersama (`model/` bersama, `config/`, `util/`, navigasi, skema DB) hanya Said yang ubah. Kalau perlu diubah, tulis permintaannya ke user untuk diteruskan ke Said.
- Antar modul: panggil service modul lain, jangan query tabelnya langsung.

## Bahasa
- Semua teks yang tampil ke pengguna (label, tombol, pesan error, notifikasi, laporan) wajib bahasa Indonesia.
- Dokumentasi dan komentar kode: bahasa Indonesia.
- Nama class, method, variabel, tabel DB: bahasa Inggris (lihat `docs/05-conventions.md`).
- Balasan ke user: bahasa Indonesia santai.

## Aturan kerja
- Jangan baca seluruh `src/`. Cari file spesifik pakai grep/glob.
- Jangan ubah struktur package atau skema database tanpa tanya dulu.
- Satu fitur dalam satu waktu. Kerjakan sesuai `docs/03-features.md`.
- Setelah fitur selesai, update status di `docs/03-features.md`.
- Keputusan desain baru dicatat di `docs/decisions.md` (1-3 baris).
- Logika bisnis ada di `service/` dan `algorithm/`, jangan di class UI.
- Kalau docs dan kode bertentangan, tanya user mana yang benar.
