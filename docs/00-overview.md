# 00 - Overview

## Ringkasan
HomeMate mengintegrasikan pengelolaan rumah tangga dalam satu aplikasi desktop Java. Perubahan data di satu modul harus tercermin di modul lain (dashboard, pengingat, laporan).

## Masalah yang diselesaikan
- Tugas rumah tidak terjadwal dan pembagiannya sulit dipantau
- Persediaan tidak tercatat, belanja terlewat
- Pengeluaran sulit dirangkum
- Jadwal perawatan rumah terlupa
- Tidak ada ringkasan terpadu

## Pengguna
| Peran | Fokus |
|---|---|
| Kepala Keluarga | Mengelola semua: tugas, keuangan, persediaan |
| Anggota Keluarga | Melihat dan mengubah status tugas sendiri |
| Pengelola Rumah Tangga | Persediaan, belanja, anggaran, perawatan |

## Modul (9)
1. Autentikasi dan akun
2. Dashboard
3. Anggota keluarga
4. Tugas dan jadwal
5. Persediaan dan daftar belanja
6. Keuangan dan anggaran
7. Perawatan rumah
8. Pengingat dan notifikasi
9. Laporan dan pengaturan

## Aturan bisnis
1. Setiap anggota punya ID unik.
2. Setiap tugas punya ID dan status.
3. Penanggung jawab tugas harus berasal dari daftar anggota.
4. Satu tugas hanya memberi poin satu kali.
5. Stok tidak boleh negatif.
6. Barang "Menipis" jika `stok <= batas_minimum`.
7. Transaksi wajib punya jenis, tanggal, nominal, kategori.
8. Pemasukan dan pengeluaran adalah jenis transaksi berbeda.
9. Anggaran dibandingkan dengan pengeluaran pada periode yang sesuai.
10. Pengingat dibuat dari kondisi data (rule-based), bukan input manual.
11. Laporan hanya menghitung data dalam periode terpilih.
12. Relasi data dijaga konsisten saat ubah atau hapus (lihat `decisions.md`).

## Non-fungsional (ringkas)
- UI mudah dan konsisten
- Validasi input (tanggal, nominal, stok, field wajib)
- Password di-hash (BCrypt), tidak pernah plaintext
- Error input atau simpan tidak merusak data tersimpan
- Kode modular, menerapkan OOP (encapsulation, inheritance, polymorphism, abstraction)
- Data persisten setelah aplikasi ditutup

## Alur utama
Login, Dashboard, pilih modul, tambah atau ubah data, validasi, proses, simpan, dashboard + pengingat + laporan ikut ter-update.
