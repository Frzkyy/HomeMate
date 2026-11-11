# 03 - Features & Status

Status: `[ ]` belum, `[~]` sedang, `[x]` selesai. Update setelah tiap fitur selesai.
Pemilik tiap fase/modul: lihat `07-team.md`.

## Fase 0 - Setup
- [ ] Project Maven + JavaFX jalan
- [ ] Koneksi SQLite + skema awal
- [ ] Navigasi sidebar + layout dasar + tema

## Fase 1 - Fondasi (FR-01, FR-03)
- [ ] Login / logout (BCrypt)
- [ ] Ubah info akun
- [ ] CRUD anggota keluarga
- [ ] Daftar anggota: tugas selesai / total tugas

## Fase 2 - Tugas (FR-04)
- [ ] CRUD tugas (penanggung jawab, prioritas, tenggat)
- [ ] Ubah status tugas
- [ ] Daftar tugas + urut prioritas/tenggat (algo 8.1)
- [ ] Kalender kegiatan
- [ ] Pencarian (algo 8.2)
- [ ] Sistem poin (algo 8.7)
- [ ] Pembagian tugas otomatis (algo 8.6)

## Fase 3 - Persediaan (FR-05)
- [ ] CRUD barang (jumlah, satuan, batas minimum)
- [ ] Status stok Menipis/Aman (algo 8.3)
- [ ] Daftar belanja, tandai dibeli, stok otomatis bertambah

## Fase 4 - Keuangan (FR-06)
- [ ] CRUD transaksi pemasukan/pengeluaran
- [ ] Anggaran per periode/kategori
- [ ] Ringkasan: saldo, sisa anggaran (algo 8.4)
- [ ] Grafik pengeluaran per kategori

## Fase 5 - Perawatan (FR-07)
- [ ] CRUD jadwal perawatan
- [ ] Tandai selesai, simpan ke riwayat
- [ ] Jadwal berikutnya

## Fase 6 - Pengingat, Dashboard, Laporan (FR-02, FR-08, FR-09)
- [ ] Mesin pengingat otomatis (algo 8.5)
- [ ] Halaman pengingat + tautan ke data terkait
- [ ] Dashboard ringkasan
- [ ] Laporan per periode (algo 8.8): tugas, persediaan, keuangan, perawatan

## Fase 7 - Pengaturan & Polish (FR-10)
- [ ] Tema, notifikasi, preferensi
- [ ] Kelola data (backup/reset)
- [ ] Validasi menyeluruh + penanganan error
- [ ] Uji akhir sesuai indikator keberhasilan PRD

## Opsional / ditunda
- Peran dan hak akses per role (kepala vs anggota)
- Halaman registrasi ("Belum punya akun? Daftar" di mockup login)
- Ekspor laporan (PDF/CSV)
