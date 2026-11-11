# 05 - Conventions

## Penamaan
- Class: `PascalCase`; method/variabel: `camelCase`; konstanta/enum: `UPPER_SNAKE_CASE`
- Package: huruf kecil semua, di bawah `com.homemate`
- Tabel dan kolom DB: `snake_case`
- Kode dan identifier pakai bahasa Inggris; teks yang tampil ke pengguna pakai bahasa Indonesia

## Struktur kelas
- Satu controller per layar, nama berakhiran `Controller` (`TaskController`)
- Service berakhiran `Service`, repository berakhiran `Repository` (interface) dan `JdbcXxxRepository` (implementasi)
- Enum untuk status/prioritas/jenis, jangan string mentah

## Aturan kode
- Field private, akses lewat getter/setter
- Validasi input di service, UI hanya menampilkan pesan error
- SQL wajib `PreparedStatement`, tidak ada string concatenation
- Operasi yang mengubah beberapa tabel dibungkus transaksi DB
- Jangan menelan exception diam-diam; tampilkan pesan yang jelas ke pengguna
- Format Rupiah dan tanggal lewat `util/`, jangan diformat manual di tiap layar
- Hindari magic number (poin, ambang anggaran) : jadikan konstanta

## Testing
- Algoritma di `algorithm/` wajib punya unit test (JUnit 5)
- Service dites dengan database SQLite in-memory

## Git
- Commit kecil dan jelas, satu fitur satu commit/branch
- Pesan commit: `feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`
