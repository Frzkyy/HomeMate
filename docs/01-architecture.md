# 01 - Architecture

Pola: layered (UI, service, repository, model). UI tidak boleh akses database langsung.

```
UI (JavaFX controller/view)
  -> Service (logika bisnis, validasi)
      -> Algorithm (logika murni, tanpa DB/UI, mudah dites)
      -> Repository (akses SQLite)
          -> Model (POJO/entity)
```

## Struktur package

```
com.homemate
├── App.java                 # entry point
├── model/                   # entity + enum
├── repository/              # interface + implementasi JDBC (SQLite)
├── service/                 # logika bisnis per modul
├── algorithm/               # algoritma murni (lihat 04-algorithms.md)
├── ui/
│   ├── controller/          # satu controller per layar
│   ├── component/           # komponen UI yang dipakai ulang
│   └── navigation/          # navigasi antar layar
├── util/                    # validator, date helper, formatter Rupiah
└── config/                  # koneksi DB, session, tema
```

Resource: `src/main/resources/` berisi FXML, CSS (tema terang/gelap), ikon.

## Tanggung jawab per layer
| Layer | Boleh | Tidak boleh |
|---|---|---|
| ui | Tampilkan data, tangkap input, panggil service | SQL, logika bisnis |
| service | Validasi, orkestrasi, panggil repository dan algorithm | Import class JavaFX |
| algorithm | Hitung, urutkan, evaluasi aturan | Akses DB, UI, waktu sistem langsung (terima tanggal sebagai parameter) |
| repository | CRUD SQL | Logika bisnis |
| model | Data + getter/setter sederhana | Akses DB |

## Pemakaian OOP (sesuai PRD)
- **Abstraction:** interface repository (`TaskRepository`, dst) dan `ReminderRule`.
- **Inheritance:** `FinancialTransaction` dengan jenis income/expense, atau base class `BaseEntity` (id, createdAt).
- **Polymorphism:** tiap `ReminderRule` (tugas, stok, perawatan, anggaran) dievaluasi lewat interface yang sama.
- **Encapsulation:** field private, validasi di setter atau service.

## Sinkronisasi antar modul
Service yang mengubah data penting memanggil `ReminderService.refresh()` agar dashboard dan pengingat selalu terbaru. Dashboard membaca dari service, bukan menyimpan salinan data.
