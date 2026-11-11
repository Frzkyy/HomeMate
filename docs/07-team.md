# 07 - Tim & Pembagian Tugas

> **STATUS: DRAFT.** Pembagian di bawah ini usulan awal. Said (integrator) boleh ubah kapan saja. Kalau ada perubahan, update tabel di sini supaya AI ikut tahu.

## Tim
| Nama | Peran |
|---|---|
| Said | Integrator + pemilik fondasi. Merge semua kerjaan, jaga arsitektur dan skema DB |
| Tifa | Developer modul |
| Carlos | Developer modul |
| Devin | Developer modul |
| Ketrin | Developer modul |
| Riva | Developer modul |

## Pembagian modul (draft)

| Pemilik | Modul | Fase | Algoritma | Paket/file utama |
|---|---|---|---|---|
| **Said** | Setup project, DB + skema, navigasi + tema, Login/Autentikasi, Dashboard, Pengingat, Laporan, integrasi akhir | 0, 1 (login), 6, 7 | 8.5, 8.8 | `config/`, `ui/navigation/`, `model/` (bersama), `Auth*`, `Dashboard*`, `Reminder*`, `Report*` |
| **Tifa** | Anggota Keluarga + Pengaturan akun | 1, 7 | (tampilan poin) | `Member*`, `Settings*` |
| **Carlos** | Tugas Rumah: CRUD, status, sorting, kalender, pencarian, auto-assign, poin | 2 | 8.1, 8.2, 8.6, 8.7 | `Task*`, `Point*` |
| **Devin** | Persediaan + Daftar Belanja | 3 | 8.2, 8.3 | `Inventory*`, `ShoppingList*` |
| **Ketrin** | Keuangan + Anggaran + grafik | 4 | 8.4 | `Transaction*`, `Budget*`, `Finance*` |
| **Riva** | Perawatan Rumah + Riwayat | 5 | (jadwal berulang) | `Maintenance*` |

Catatan: Dashboard, Pengingat, dan Laporan sengaja dipegang Said karena mengambil data dari semua modul.

## Urutan kerja
1. **Said dulu:** Fase 0 (project jalan, DB, navigasi, tema). Yang lain belum bisa mulai coding UI sebelum ini selesai.
2. Setelah Fase 0, Tifa, Carlos, Devin, Ketrin, Riva kerja paralel di modul masing-masing.
3. Said mengerjakan Pengingat, Dashboard, Laporan setelah service tiap modul tersedia.
4. Integrasi dan uji akhir oleh Said.

Kalau modul butuh data dari modul lain yang belum jadi (misal Carlos butuh daftar anggota dari Tifa), pakai **stub/data dummy** di belakang interface service, jangan tunggu.

## Aturan kepemilikan file (penting buat AI)
- Kerjakan **hanya modul milik orang yang sedang memakai AI**. Jangan edit file milik modul orang lain.
- **File bersama (hanya Said yang ubah):** `model/` yang dipakai lebih dari satu modul, enum global, `config/`, `util/`, `ui/navigation/`, skema DB (`02-database.md`), semua file di `docs/` kecuali bagian yang jelas milik modul sendiri.
- Perlu mengubah file bersama? **Jangan langsung ubah.** Tulis permintaannya (apa dan kenapa) ke user supaya diteruskan ke Said.
- Boleh menambah file baru untuk modul sendiri tanpa izin.
- Perubahan skema tabel selalu lewat Said, supaya tidak bentrok antar modul.

## Antar modul: pakai service, bukan tabel orang lain
- Modul A butuh data modul B: panggil **service** modul B (mis. `MemberService.findAll()`), jangan query tabel B langsung.
- Tanda tangan (signature) method service publik dianggap kontrak. Mau ubah? Kabari Said dan modul yang memakainya.
- Event lintas modul (contoh: tugas selesai memicu poin dan pengingat) dipicu dari service pemilik data, lalu `ReminderService.refresh()` dipanggil sesuai `01-architecture.md`.

## Git workflow (usulan)
- Branch: `develop` (hasil integrasi), `main` (stabil), dan `feature/<modul>-<nama>` per orang. Contoh: `feature/task-carlos`.
- Commit kecil, ikuti format di `05-conventions.md`.
- Merge ke `develop` lewat Pull Request, **Said yang merge**.
- Sebelum PR: aplikasi harus bisa jalan dan unit test algoritma modulnya lolos.
- Tarik `develop` ke branch sendiri sebelum mulai kerja hari itu.

## Definition of done per fitur
- [ ] Jalan sesuai `03-features.md` dan aturan bisnis di `00-overview.md`
- [ ] Validasi input + pesan error bahasa Indonesia
- [ ] Algoritma punya unit test
- [ ] Status di `03-features.md` diupdate (`[x]`)
- [ ] Tidak menyentuh file bersama tanpa lewat Said
- [ ] PR dibuat

## Cara AI tahu siapa penggunanya
Satu repo dipakai enam orang, jadi AI harus tahu sedang melayani siapa:
- Di awal sesi bilang: **"Gw Carlos"** (atau nama lain). AI lalu membaca tabel di atas dan membatasi kerja ke modul itu.
- Opsional: buat file `AGENTS.local.md` di root repo berisi satu baris, misal `Gw Carlos, modul: Tugas Rumah.` Tambahkan `AGENTS.local.md` ke `.gitignore` supaya tidak ikut ke-commit dan tidak menimpa orang lain.
