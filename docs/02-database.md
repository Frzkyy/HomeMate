# 02 - Database

SQLite, satu file `homemate.db`. Semua tanggal disimpan ISO-8601 (`yyyy-MM-dd`), nominal sebagai integer Rupiah (tanpa desimal).

## Entitas

### User
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| username | TEXT UNIQUE | |
| password_hash | TEXT | BCrypt |
| email | TEXT | |
| role | TEXT | KEPALA_KELUARGA, ANGGOTA, PENGELOLA |
| theme | TEXT | TERANG / GELAP |
| notifications_enabled | INTEGER | 0/1 |

### HouseholdMember
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| name | TEXT | |
| family_role | TEXT | Ayah, Ibu, Anak, dll |
| user_id | INTEGER FK -> User, nullable | akun login (opsional) |
| total_points | INTEGER | default 0 |
| can_receive_tasks | INTEGER | untuk auto-assign |

### HouseholdTask
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| title | TEXT | wajib |
| description | TEXT | |
| assignee_id | INTEGER FK -> HouseholdMember, nullable | |
| priority | TEXT | RENDAH, SEDANG, TINGGI |
| due_date | TEXT | |
| status | TEXT | BELUM_DIMULAI, SEDANG_DIKERJAKAN, SELESAI |
| completed_at | TEXT | nullable |

### PointHistory
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| member_id | INTEGER FK | |
| task_id | INTEGER FK, UNIQUE | menjamin 1 tugas = 1x poin |
| points | INTEGER | 5 / 10 / 15 |
| awarded_at | TEXT | |

### InventoryItem
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| name | TEXT | |
| quantity | REAL | CHECK >= 0 |
| unit | TEXT | kg, pcs, botol, dll |
| min_stock | REAL | |

### ShoppingListItem
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| inventory_item_id | INTEGER FK, nullable | null = barang di luar inventaris |
| name | TEXT | |
| quantity | REAL | |
| unit | TEXT | |
| purchased | INTEGER | 0/1 |

### FinancialTransaction
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| type | TEXT | PEMASUKAN / PENGELUARAN |
| category | TEXT | Makanan, Listrik, Air, Transportasi, Lainnya, ... |
| amount | INTEGER | > 0 |
| date | TEXT | |
| note | TEXT | |

### Budget
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| category | TEXT nullable | null = anggaran total |
| amount | INTEGER | |
| period_start | TEXT | |
| period_end | TEXT | |

### MaintenanceSchedule
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| name | TEXT | Servis AC, Cek Pompa Air |
| object | TEXT | peralatan/objek |
| location | TEXT | Ruang Tamu, Halaman, dll |
| scheduled_date | TEXT | |
| status | TEXT | TERJADWAL, SELESAI |
| recurrence_days | INTEGER nullable | untuk jadwal berulang |

### MaintenanceHistory
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| schedule_id | INTEGER FK | |
| done_date | TEXT | |
| notes | TEXT | |

### Reminder
| Kolom | Tipe | Catatan |
|---|---|---|
| id | INTEGER PK | |
| type | TEXT | TUGAS, PERSEDIAAN, PERAWATAN, KEUANGAN |
| message | TEXT | |
| ref_type / ref_id | TEXT / INTEGER | menuju data terkait |
| remind_date | TEXT | |
| is_read | INTEGER | |

## Relasi
- HouseholdMember 1 - N HouseholdTask
- HouseholdMember 1 - N PointHistory; HouseholdTask 1 - 0..1 PointHistory
- InventoryItem 1 - N ShoppingListItem
- MaintenanceSchedule 1 - N MaintenanceHistory
- User 1 - 0..1 HouseholdMember

## Constraint penting
- `InventoryItem.quantity >= 0`
- `PointHistory.task_id` UNIQUE
- `FinancialTransaction.amount > 0`
- Perilaku saat hapus: lihat `decisions.md`
