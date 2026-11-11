# HOMEMATE

# Product Requirements Document (PRD)

**Sistem Manajemen Aktivitas Rumah Tangga**\
**Versi:** 1.0\
**Platform:** Desktop Application\
**Bahasa Pemrograman:** Java\
**Antarmuka:** Graphical User Interface (GUI)\
**Kategori:** Household Management System

------------------------------------------------------------------------

## 1. Ringkasan Produk

HomeMate merupakan aplikasi manajemen rumah tangga yang dirancang untuk
mengintegrasikan berbagai aktivitas rumah tangga ke dalam satu sistem.
Aplikasi ini menyediakan fasilitas pengelolaan anggota keluarga, tugas
dan jadwal, persediaan barang, keuangan, pemeliharaan rumah, pengingat,
serta laporan.

HomeMate membantu pengguna mengorganisasi aktivitas sehari-hari, membagi
tanggung jawab antaranggota keluarga, memantau kebutuhan rumah tangga,
serta mengelola pengeluaran secara terstruktur. Aplikasi dikembangkan
menggunakan Java dengan antarmuka grafis (GUI).

## 2. Latar Belakang

Aktivitas rumah tangga melibatkan berbagai kegiatan yang saling
berkaitan, seperti membersihkan rumah, mengatur jadwal, membagi tugas,
mengelola persediaan barang, merawat peralatan, dan mencatat
pengeluaran. Pengelolaan aktivitas secara manual dapat menyebabkan
pekerjaan terlupakan, pembagian tanggung jawab kurang teratur,
persediaan tidak terpantau, serta kesulitan mengetahui kondisi keuangan
rumah tangga.

HomeMate dikembangkan sebagai solusi terintegrasi yang menggabungkan
berbagai kebutuhan tersebut dalam satu aplikasi.

## 3. Identifikasi Masalah

1.  Aktivitas rumah tangga belum terorganisasi dalam satu sistem
    penjadwalan.
2.  Pembagian tugas antaranggota keluarga sulit dipantau.
3.  Persediaan kebutuhan rumah tangga tidak selalu tercatat dengan baik.
4.  Pengeluaran rumah tangga sulit dirangkum secara terstruktur.
5.  Jadwal pemeliharaan rumah dan peralatan berpotensi terlewat.
6.  Tidak tersedia sistem terintegrasi untuk memantau seluruh aktivitas
    rumah tangga.
7.  Pengguna kesulitan mengetahui aktivitas yang harus diprioritaskan.

## 4. Tujuan Produk

-   Mengintegrasikan pengelolaan aktivitas rumah tangga dalam satu
    aplikasi.
-   Mempermudah pengaturan jadwal dan pembagian tugas.
-   Membantu pengguna memantau aktivitas setiap anggota keluarga.
-   Mengelola persediaan dan kebutuhan belanja.
-   Menyediakan pencatatan pemasukan, pengeluaran, dan anggaran.
-   Mengatur jadwal pemeliharaan rumah dan peralatan.
-   Menyediakan pengingat otomatis berdasarkan kondisi tertentu.
-   Menyajikan informasi melalui dashboard dan laporan.
-   Meningkatkan keteraturan aktivitas rumah tangga melalui pengelolaan
    data yang terstruktur.

## 5. Target Pengguna

### 5.1 Kepala Keluarga

Mengelola keseluruhan aktivitas rumah tangga, menentukan pembagian
tugas, serta memantau kondisi keuangan dan persediaan.

### 5.2 Anggota Keluarga

Melihat tugas yang menjadi tanggung jawabnya, memperbarui status
pekerjaan, dan mengetahui jadwal kegiatan.

### 5.3 Pengelola Rumah Tangga

Mengelola persediaan, daftar belanja, pengeluaran, anggaran, dan jadwal
pemeliharaan rumah.

## 6. Ruang Lingkup Produk

HomeMate memiliki sembilan modul utama:

1.  Autentikasi dan manajemen akun.
2.  Dashboard.
3.  Manajemen anggota keluarga.
4.  Manajemen aktivitas dan jadwal.
5.  Manajemen persediaan dan daftar belanja.
6.  Manajemen keuangan dan anggaran.
7.  Manajemen pemeliharaan rumah.
8.  Pengingat dan notifikasi.
9.  Laporan dan pengaturan aplikasi.

Seluruh modul saling terintegrasi sehingga perubahan informasi pada satu
modul dapat memengaruhi informasi yang ditampilkan pada modul lainnya.

## 7. Kebutuhan Fungsional

### FR-01 --- Autentikasi dan Manajemen Akun

-   Pengguna dapat masuk menggunakan username dan password.
-   Sistem memvalidasi informasi login.
-   Pengguna dapat keluar dari aplikasi.
-   Pengguna dapat melihat dan mengubah informasi akun.
-   Sistem menyediakan pengaturan preferensi akun.

### FR-02 --- Dashboard

-   Menampilkan jumlah tugas selesai dan tertunda.
-   Menampilkan jumlah persediaan yang menipis.
-   Menampilkan total pemasukan dan pengeluaran.
-   Menampilkan daftar tugas harian.
-   Menampilkan informasi barang yang perlu dibeli.
-   Menampilkan pengingat terbaru dan ringkasan anggaran.

### FR-03 --- Manajemen Anggota Keluarga

-   Menambahkan, melihat, mengubah, dan menghapus anggota keluarga.
-   Menampilkan peran anggota dalam keluarga.
-   Menampilkan jumlah tugas selesai dan total tugas.
-   Menghubungkan anggota keluarga dengan tugas tertentu.

### FR-04 --- Manajemen Aktivitas dan Jadwal

-   Menambahkan, mengubah, dan menghapus tugas.
-   Menentukan penanggung jawab, prioritas, dan tenggat penyelesaian.
-   Mengubah status pekerjaan.
-   Menampilkan daftar tugas dan kalender kegiatan.
-   Mengurutkan tugas berdasarkan prioritas dan tenggat.
-   Status tugas terdiri atas Belum Dimulai, Sedang Dikerjakan, dan
    Selesai.

### FR-05 --- Manajemen Persediaan dan Daftar Belanja

-   Menambahkan, mengubah, dan menghapus barang.
-   Mencatat jumlah dan satuan barang serta batas minimum persediaan.
-   Menampilkan status stok dan mengidentifikasi barang yang hampir
    habis.
-   Menambahkan barang ke daftar belanja.
-   Memperbarui stok setelah pembelian dan menandai barang yang telah
    dibeli.

### FR-06 --- Manajemen Keuangan

-   Mencatat pemasukan dan pengeluaran.
-   Menentukan kategori, tanggal, nominal, dan keterangan transaksi.
-   Mengubah dan menghapus transaksi.
-   Mengatur anggaran.
-   Menghitung total pemasukan, total pengeluaran, dan sisa anggaran.
-   Menampilkan grafik pengeluaran berdasarkan kategori.

### FR-07 --- Manajemen Pemeliharaan Rumah

-   Menambahkan jadwal pemeliharaan.
-   Menentukan nama kegiatan, objek atau peralatan, lokasi, dan tanggal
    pemeliharaan.
-   Memperbarui status kegiatan.
-   Menampilkan jadwal berikutnya dan menyimpan riwayat pemeliharaan.

### FR-08 --- Pengingat dan Notifikasi

-   Mengingatkan tugas yang mendekati atau melewati tenggat.
-   Mengingatkan persediaan di bawah batas minimum.
-   Mengingatkan jadwal pemeliharaan yang mendekat.
-   Mengingatkan pengeluaran yang mendekati batas anggaran.
-   Menampilkan daftar pengingat dan akses ke informasi terkait.

### FR-09 --- Laporan

-   Menampilkan laporan penyelesaian tugas dan aktivitas anggota
    keluarga.
-   Menampilkan laporan persediaan.
-   Menampilkan laporan pemasukan, pengeluaran, dan penggunaan anggaran.
-   Menampilkan laporan pemeliharaan rumah.
-   Menyajikan laporan berdasarkan periode melalui tabel, ringkasan
    angka, atau grafik.

### FR-10 --- Pengaturan Aplikasi

-   Mengubah informasi akun.
-   Mengatur preferensi tema dan notifikasi.
-   Mengatur tampilan aplikasi.
-   Mengelola data aplikasi.

## 8. Algoritma Sistem

Bagian ini menjelaskan logika pengolahan data yang digunakan HomeMate
untuk menghasilkan informasi dan menjalankan proses otomatis. Algoritma
diimplementasikan menggunakan Java.

### 8.1 Algoritma Pengurutan Prioritas Tugas

**Tujuan:** Mengurutkan tugas agar pengguna mengetahui pekerjaan yang
harus diselesaikan terlebih dahulu.

**Input:** Daftar tugas, tingkat prioritas, dan tanggal tenggat.

**Proses:** 1. Ambil tugas yang belum selesai. 2. Urutkan berdasarkan
prioritas dari tinggi ke rendah. 3. Jika prioritas sama, bandingkan
tenggat dari yang paling dekat. 4. Tampilkan hasil pengurutan.

**Metode:** Sorting dengan multi-key comparison.

**Contoh:** Tugas prioritas tinggi ditempatkan sebelum tugas prioritas
sedang atau rendah. Untuk prioritas yang sama, tenggat lebih dekat
ditempatkan lebih dahulu.

### 8.2 Algoritma Pencarian Data

**Tujuan:** Menemukan tugas, anggota keluarga, barang, atau transaksi.

**Input:** Kata kunci dan kumpulan data.

**Proses:** 1. Baca data satu per satu. 2. Bandingkan kata kunci dengan
atribut pencarian. 3. Kumpulkan data yang cocok. 4. Tampilkan hasil.

**Metode:** Linear Search dengan pencocokan teks tanpa membedakan huruf
besar dan kecil.

**Contoh:** Pencarian kata "Beras" menampilkan data persediaan yang
namanya cocok.

### 8.3 Algoritma Pemeriksaan Persediaan

**Tujuan:** Menentukan apakah persediaan masih mencukupi atau perlu
dibeli.

**Input:** Jumlah stok dan batas minimum.

**Proses:** 1. Ambil jumlah stok. 2. Ambil batas minimum. 3. Bandingkan
keduanya. 4. Jika stok kurang dari atau sama dengan batas minimum,
tandai sebagai menipis dan sediakan opsi memasukkannya ke daftar
belanja.

**Metode:** Conditional Checking dan Threshold Comparison.

**Contoh:** Stok beras 2 kg dengan batas minimum 5 kg menghasilkan
status "Menipis".

### 8.4 Algoritma Perhitungan Keuangan

**Tujuan:** Menghasilkan ringkasan kondisi keuangan rumah tangga.

**Input:** Daftar pemasukan, pengeluaran, dan anggaran.

**Proses:** 1. Ambil transaksi pada periode terpilih. 2. Pisahkan
pemasukan dan pengeluaran. 3. Jumlahkan masing-masing. 4. Hitung saldo
bersih. 5. Bandingkan pengeluaran dengan anggaran. 6. Tampilkan hasil.

**Metode:** Sequential Summation dan Aggregation.

**Rumus:** - Saldo Bersih = Total Pemasukan − Total Pengeluaran. - Sisa
Anggaran = Anggaran − Total Pengeluaran pada periode atau kategori
terkait.

### 8.5 Algoritma Pengingat Otomatis

**Tujuan:** Mengidentifikasi kondisi yang membutuhkan perhatian
pengguna.

**Input:** Tenggat tugas, tanggal pemeliharaan, stok dan batas minimum,
pengeluaran dan anggaran, serta tanggal pemeriksaan.

**Proses:** 1. Periksa tugas yang belum selesai. 2. Bandingkan tenggat
dengan tanggal saat ini. 3. Periksa stok. 4. Periksa jadwal
pemeliharaan. 5. Bandingkan pengeluaran dengan anggaran. 6. Buat dan
tampilkan pengingat untuk kondisi yang terpenuhi.

**Metode:** Date Comparison, Conditional Checking, dan Rule-Based
Evaluation.

**Contoh:** Tugas yang belum selesai dan memiliki tenggat hari ini
menghasilkan pengingat.

### 8.6 Algoritma Pembagian Tugas Otomatis

**Tujuan:** Membagi tugas berdasarkan jumlah pekerjaan aktif setiap
anggota keluarga.

**Input:** Daftar anggota, tugas yang belum memiliki penanggung jawab,
dan jumlah tugas aktif tiap anggota.

**Proses:** 1. Ambil anggota yang dapat menerima tugas. 2. Hitung tugas
aktif setiap anggota. 3. Pilih anggota dengan jumlah tugas aktif paling
sedikit. 4. Tetapkan tugas. 5. Perbarui jumlah tugas aktif. 6. Ulangi
untuk tugas berikutnya.

**Metode:** Greedy Algorithm dan Minimum Value Selection.

**Contoh:** Jika Budi memiliki 4 tugas, Siti 2, Rina 3, dan Andi 1,
tugas baru diberikan kepada Andi.

### 8.7 Algoritma Sistem Poin

**Tujuan:** Memberikan penghargaan virtual kepada anggota setelah
menyelesaikan tugas.

**Input:** Identitas anggota, tugas selesai, dan nilai poin berdasarkan
tugas.

**Proses:** 1. Periksa perubahan status. 2. Pastikan poin belum pernah
diberikan untuk tugas tersebut. 3. Tentukan nilai poin berdasarkan
prioritas. 4. Tambahkan poin ke total anggota. 5. Simpan riwayat poin.

**Metode:** Conditional Checking, Accumulation, dan Rule-Based Scoring.

**Aturan contoh:** - Prioritas rendah: 5 poin. - Prioritas sedang: 10
poin. - Prioritas tinggi: 15 poin.

Satu tugas hanya memberikan poin satu kali.

### 8.8 Algoritma Laporan Berkala

**Tujuan:** Menghasilkan ringkasan aktivitas rumah tangga berdasarkan
periode.

**Input:** Data tugas, transaksi, persediaan, dan periode laporan.

**Proses:** 1. Ambil data sesuai rentang tanggal. 2. Kelompokkan
transaksi berdasarkan kategori. 3. Hitung pemasukan dan pengeluaran. 4.
Hitung tugas selesai dan belum selesai. 5. Rangkum persediaan. 6. Susun
dan tampilkan laporan.

**Metode:** Filtering, Grouping, Aggregation, dan Sequential Processing.

## 9. Kebutuhan Nonfungsional

### NFR-01 --- Kemudahan Penggunaan

Antarmuka mudah dipahami dan memiliki navigasi konsisten.

### NFR-02 --- Konsistensi Data

Perubahan data tercermin pada seluruh modul yang berkaitan.

### NFR-03 --- Validasi Data

Sistem memvalidasi tanggal, nominal transaksi, jumlah persediaan, dan
data wajib lainnya.

### NFR-04 --- Keamanan

Password disimpan menggunakan mekanisme hashing yang aman. Akses
terhadap akun dan data rumah tangga dibatasi.

### NFR-05 --- Keandalan

Sistem menangani kesalahan input dan penyimpanan tanpa merusak data yang
sudah tersimpan.

### NFR-06 --- Kemudahan Pemeliharaan

Struktur program modular dan mengikuti prinsip Pemrograman Berorientasi
Objek.

### NFR-07 --- Performa

Operasi umum seperti membuka daftar, mencari data, menyimpan transaksi,
dan memperbarui dashboard berjalan responsif.

### NFR-08 --- Persistensi Data

Data tetap tersimpan setelah aplikasi ditutup dan dibuka kembali.

## 10. Aturan Bisnis

1.  Setiap anggota keluarga memiliki identitas data yang unik.
2.  Setiap tugas memiliki identitas dan status.
3.  Tugas memiliki penanggung jawab yang berasal dari daftar anggota
    keluarga.
4.  Tugas yang selesai tidak menerima poin lebih dari satu kali.
5.  Jumlah persediaan tidak boleh bernilai negatif.
6.  Barang ditandai menipis apabila jumlah stok kurang dari atau sama
    dengan batas minimum.
7.  Setiap transaksi memiliki jenis, tanggal, nominal, dan kategori.
8.  Pemasukan dan pengeluaran dicatat sebagai jenis transaksi berbeda.
9.  Anggaran dibandingkan dengan total pengeluaran pada periode yang
    sesuai.
10. Pengingat dibuat berdasarkan kondisi data yang memenuhi aturan
    tertentu.
11. Laporan hanya menghitung data yang termasuk dalam periode yang
    dipilih.
12. Data yang saling berkaitan dijaga konsistensinya ketika dilakukan
    perubahan atau penghapusan.

## 11. Struktur Navigasi Aplikasi

Navigasi utama: 1. Dashboard. 2. Anggota Keluarga. 3. Tugas Rumah. 4.
Persediaan. 5. Keuangan. 6. Perawatan Rumah. 7. Pengingat. 8. Laporan.
9. Pengaturan. 10. Keluar.

Halaman tambahan meliputi Login, Form Tambah dan Edit Data, Kalender
Kegiatan, Daftar Belanja, Detail Tugas, Detail Transaksi, dan Riwayat
Pemeliharaan.

## 12. Data Utama yang Dikelola

  Entitas                Fungsi
  ---------------------- -------------------------------------------
  User                   Menyimpan informasi akun pengguna.
  HouseholdMember        Menyimpan informasi anggota keluarga.
  HouseholdTask          Menyimpan data tugas rumah tangga.
  InventoryItem          Menyimpan data persediaan barang.
  ShoppingListItem       Menyimpan daftar kebutuhan belanja.
  FinancialTransaction   Menyimpan data pemasukan dan pengeluaran.
  Budget                 Menyimpan informasi anggaran.
  MaintenanceSchedule    Menyimpan jadwal pemeliharaan.
  MaintenanceHistory     Menyimpan riwayat pemeliharaan.
  Reminder               Menyimpan informasi pengingat.
  PointHistory           Menyimpan riwayat perolehan poin anggota.

## 13. Penerapan Teknologi

HomeMate dikembangkan menggunakan Java sebagai bahasa pemrograman utama
dan GUI sebagai antarmuka pengguna. Pengembangan menerapkan konsep
Object-Oriented Programming (OOP).

-   **Encapsulation:** Melindungi dan mengatur akses terhadap data
    objek.
-   **Inheritance:** Memungkinkan kelas turunan menggunakan
    karakteristik kelas induk.
-   **Polymorphism:** Memungkinkan perilaku berbeda pada objek melalui
    metode atau antarmuka yang sama.
-   **Abstraction:** Menyederhanakan kompleksitas implementasi dengan
    menampilkan karakteristik penting.

Java digunakan untuk mengimplementasikan logika sistem, pengolahan data,
algoritma, dan interaksi antarmuka.

## 14. Indikator Keberhasilan

1.  Pengguna dapat masuk dan mengelola akun.
2.  Pengguna dapat melihat ringkasan kondisi rumah tangga melalui
    dashboard.
3.  Pengguna dapat mengelola anggota keluarga.
4.  Pengguna dapat membuat, mengubah, menghapus, dan menyelesaikan
    tugas.
5.  Sistem dapat mengurutkan dan mencari tugas.
6.  Pengguna dapat mengelola persediaan serta daftar belanja.
7.  Sistem dapat mengidentifikasi persediaan yang menipis.
8.  Pengguna dapat mencatat pemasukan dan pengeluaran.
9.  Sistem dapat menghitung ringkasan keuangan dan anggaran.
10. Pengguna dapat mengelola jadwal pemeliharaan rumah.
11. Sistem dapat menghasilkan pengingat berdasarkan kondisi tertentu.
12. Sistem dapat membagikan tugas berdasarkan beban pekerjaan anggota.
13. Sistem dapat memberikan poin setelah tugas diselesaikan.
14. Pengguna dapat melihat laporan aktivitas rumah tangga.
15. Data tersimpan dan dapat digunakan kembali setelah aplikasi dibuka
    kembali.

## 15. Alur Penggunaan Sistem

1.  Pengguna membuka aplikasi HomeMate.
2.  Pengguna melakukan login.
3.  Sistem menampilkan dashboard.
4.  Pengguna memilih modul yang ingin digunakan.
5.  Pengguna menambahkan atau memperbarui informasi.
6.  Sistem memvalidasi input.
7.  Sistem menjalankan proses pengolahan data sesuai kebutuhan modul.
8.  Sistem menyimpan perubahan.
9.  Sistem memperbarui dashboard, pengingat, dan laporan yang berkaitan.
10. Pengguna melihat informasi terbaru melalui aplikasi.

------------------------------------------------------------------------

**HOMEMATE --- Kelola Rumah, Hidup Lebih Teratur.**
