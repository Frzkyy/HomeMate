# 04 - Algorithms

Semua di package `com.homemate.algorithm`. Fungsi murni: input masuk, hasil keluar, tanpa DB/UI. Tanggal "hari ini" selalu dikirim sebagai parameter supaya bisa dites.

## 8.1 Pengurutan prioritas tugas
- Ambil tugas yang belum selesai
- Urut: prioritas TINGGI > SEDANG > RENDAH; jika sama, tenggat terdekat dulu
- Metode: `Comparator` multi-key

## 8.2 Pencarian data
- Linear search, case-insensitive, cocokkan kata kunci ke atribut teks (nama/judul/keterangan)
- Dipakai untuk tugas, anggota, barang, transaksi

## 8.3 Pemeriksaan persediaan
- `stok <= min_stock` => "Menipis", tawarkan masuk daftar belanja
- Contoh: stok 2 kg, min 5 kg => Menipis

## 8.4 Perhitungan keuangan
- Ambil transaksi pada periode, pisahkan jenis, jumlahkan
- `Saldo Bersih = Total Pemasukan - Total Pengeluaran`
- `Sisa Anggaran = Anggaran - Total Pengeluaran (periode/kategori terkait)`

## 8.5 Pengingat otomatis (rule-based)
Rule yang dievaluasi, masing-masing sebagai `ReminderRule`:
- Tugas belum selesai dengan tenggat hari ini / besok / lewat
- Stok <= minimum
- Perawatan jatuh tempo dalam N hari
- Pengeluaran mendekati anggaran (mis. >= 80%, ambang bisa diatur)

Hindari pengingat duplikat untuk kondisi yang sama (cek `ref_type` + `ref_id`).

## 8.6 Pembagian tugas otomatis (greedy)
- Kandidat: anggota dengan `can_receive_tasks = true`
- Hitung tugas aktif (status bukan SELESAI) per anggota
- Tiap tugas tanpa penanggung jawab diberikan ke anggota dengan jumlah tugas aktif paling sedikit, lalu hitung diperbarui
- Tie-break: urut ID (supaya deterministik)
- Contoh: Budi 4, Siti 2, Rina 3, Andi 1 => Andi

## 8.7 Sistem poin
- Saat status berubah ke SELESAI: cek `PointHistory` belum ada untuk `task_id`
- Poin: RENDAH 5, SEDANG 10, TINGGI 15
- Tambah ke `total_points`, simpan riwayat, dalam satu transaksi DB
- Jika tugas dibuka ulang lalu selesai lagi, poin TIDAK diberikan lagi

## 8.8 Laporan berkala
- Filter data pada rentang tanggal, kelompokkan transaksi per kategori
- Hitung pemasukan/pengeluaran, tugas selesai vs belum, rangkum persediaan, rekap perawatan
