# HomeMate

> Kelola Rumah, Hidup Lebih Teratur.

HomeMate adalah aplikasi desktop (Java + GUI) untuk mengelola aktivitas rumah tangga dalam satu tempat: anggota keluarga, tugas dan jadwal, persediaan dan belanja, keuangan, perawatan rumah, pengingat, dan laporan.

## Struktur repo

```
HomeMate/
├── README.md
├── AGENTS.md        # instruksi untuk AI assistant (tool apa pun)
├── docs/            # dokumentasi proyek (mulai dari 00-overview.md)
└── HomeMate/        # source code (folder src)
```

## Dokumentasi

| File | Isi |
|---|---|
| `docs/PRD.md` | Product Requirements Document lengkap (sumber asli, belum final) |
| `docs/00-overview.md` | Tujuan, pengguna, modul, aturan bisnis |
| `docs/01-architecture.md` | Struktur package dan layer |
| `docs/02-database.md` | Entitas dan relasi |
| `docs/03-features.md` | Daftar fitur dan status pengerjaan |
| `docs/04-algorithms.md` | Algoritma sistem |
| `docs/05-conventions.md` | Aturan coding |
| `docs/06-ui.md` | Layar dan navigasi |
| `docs/07-team.md` | Tim, pembagian modul, aturan kerja bersama, git workflow |
| `docs/decisions.md` | Catatan keputusan |

## Menjalankan

```
mvn clean javafx:run
```

(Sesuaikan setelah project setup selesai.)
