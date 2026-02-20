# Modul : Inventory & Katalog (json-inventory-service)

Microservice untuk mengelola **Modul Inventory & Katalog** pada platform JSON (Jasa Titip Online).

## Tanggung Jawab Modul

Modul ini berfungsi sebagai pusat pengelolaan data barang titipan. Modul ini memfasilitasi Jastiper dalam mengatur stok dagangan, memudahkan Titipers dalam mencari dan menelusuri katalog barang yang tersedia, serta menangani mekanisme pengurangan stok secara atomik untuk mencegah *overselling*.

---

## Yang Dilakukan Modul Ini

### Manajemen Katalog (Jastiper)
- Menyediakan fungsionalitas bagi Jastiper untuk membuat, melihat, memperbarui (partial update), dan menghapus produk miliknya dari katalog.
- Mengelola status visibilitas produk secara otomatis (misalnya, stok 0 akan otomatis berstatus `OUT_OF_STOCK`).

### Pencarian & Browsing (Publik)
- Memfasilitasi Titipers untuk mencari produk berdasarkan kata kunci (full-text search), kategori, rentang harga, dan negara asal.
- Menampilkan katalog khusus per Jastiper dan detail lengkap produk beserta ulasan.

### Manajemen Stok (Internal)
- Menangani reservasi stok melalui *database-level atomic operation* dengan *optimistic locking* saat pesanan dibuat oleh Modul Order.
- Mengembalikan stok yang direservasi jika pesanan dibatalkan atau gagal diproses.
- Memperbarui stok permanen serta statistik *rating* dan *review* setelah pesanan selesai.

### Monitoring & Moderasi (Admin)
- Menyediakan endpoint bagi Admin untuk memonitor seluruh produk.
- Memfasilitasi aksi moderasi (seperti menyembunyikan atau menghapus) terhadap produk yang melanggar aturan, lengkap dengan *audit log*.
- Mengelola hierarki Kategori Produk (CRUD) yang digunakan sebagai filter pencarian.

---

## Tech Stack

- **Java** + **Spring Boot** - web framework
- **Hibernate** - ORM
- **PostgreSQL** (Neon DB) - database

---

## Database Schema

### Custom Types

#### `product_status`
| Value |
| --- |
| ACTIVE  |
| OUT_OF_STOCK  |
| HIDDEN  |
| REMOVED_BY_ADMIN  |

#### `reservation_status`
| Value |
| --- |
| PENDING  |
| CONFIRMED  |
| RELEASED  |

#### `moderation_action`
| Value |
| --- |
| REMOVE  |
| RESTORE  |
| HIDE  |
| ACTIVATE  |

---

### Object: Product

Entitas ini menyimpan seluruh informasi detail barang titipan yang dikelola oleh Jastiper, termasuk harga, stok, asal negara, dan metrik statistik order.

| Field | Type | Nullable | Key |
| --- | --- | --- | --- |
| product_id | VARCHAR (36) | NOT NULL  | PK |
| jastiper_id | VARCHAR (36) | NOT NULL  | FK |
| category_id | INTEGER | NULL  | FK |
| name | VARCHAR (255) | NOT NULL  | |
| description | TEXT | NOT NULL  | |
| price | LONG | NOT NULL  | |
| service_fee | LONG | NOT NULL  | |
| stock | INTEGER | NOT NULL  | |
| origin_country | VARCHAR | NOT NULL  | |
| purchase_date | DATE | NOT NULL  | |
| images | TEXT | NOT NULL  | |
| weight_gram | INTEGER | NULL  | |
| tags | TEXT | NOT NULL  | |
| status | *product_status* | NOT NULL  | |
| avg_rating | FLOAT | NULL  | |
| total_reviews | INTEGER | NOT NULL  | |
| total_orders | INTEGER | NOT NULL  | |
| deleted_at | DATETIME (ISO 8601) | NULL  | |
| created_at | DATETIME (ISO 8601) | NOT NULL  | |
| updated_at | DATETIME (ISO 8601) | NOT NULL  | |

**PK:** product_id 

**Notes:**
- **stock**: Jumlah stok tersedia. Constraint di level DB memastikan nilai tidak boleh negatif.
- **deleted_at**: Menyimpan timestamp untuk *soft-delete* produk.
- **images & tags**: Disimpan dalam bentuk *array string* atau format teks berstruktur (JSON/Comma-separated) dengan nilai default array kosong.

---

### Object: Category

Entitas ini menyimpan daftar klasifikasi kategori untuk memudahkan navigasi katalog oleh pengguna.

| Field | Type | Nullable | Key |
| --- | --- | --- | --- |
| category_id | INTEGER | NOT NULL  | PK |
| name | VARCHAR (100) | NOT NULL  | UK |
| slug | VARCHAR | NOT NULL  | UK |
| description | TEXT | NULL  | |
| product_count | INTEGER | NOT NULL  | |
| created_at | DATETIME (ISO 8601) | NOT NULL  | |

**PK:** category_id 

**Notes:**
- **product_count**: *Computed field* yang melacak jumlah produk aktif dalam kategori ini.
- **name & slug**: Bersifat unik (Unique Key).

---

### Object: Stock Reservation

Entitas ini mencatat histori reservasi stok sementara (menahan kuota) saat sebuah order sedang diproses dalam tahap *checkout*, mencegah kondisi *race condition*.

| Field | Type | Nullable | Key |
| --- | --- | --- | --- |
| reservation_id | VARCHAR (36) | NOT NULL  | PK |
| product_id | VARCHAR (36) | NOT NULL  | FK |
| order_id | VARCHAR (36) | NOT NULL  | UK |
| quantity | INTEGER | NOT NULL  | |
| status | *reservation_status*| NOT NULL  | |
| created_at | DATETIME (ISO 8601) | NOT NULL  | |
| expires_at | DATETIME (ISO 8601) | NULL  | |

**PK:** reservation_id 

**Notes:**
- **order_id**: Foreign key ke tabel Order. Digunakan sebagai pencegahan idempotensi (agar 1 order tidak mereservasi stok ganda).
- **expires_at**: Reservasi yang kadaluarsa akan otomatis dilepas kembali ke `stock` utama.

---

### Object: Moderation Log

Entitas ini bertindak sebagai *audit trail* ketika Admin melakukan tindakan terhadap sebuah produk (misalnya menyembunyikan karena pelanggaran).

| Field | Type | Nullable | Key |
| --- | --- | --- | --- |
| log_id | VARCHAR (36) | NOT NULL  | PK |
| product_id | VARCHAR (36) | NOT NULL  | FK |
| admin_id | VARCHAR (36) | NOT NULL  | FK |
| action | *moderation_action* | NOT NULL  | |
| reason | VARCHAR | NOT NULL  | |
| created_at | DATETIME (ISO 8601) | NOT NULL  | |

**PK:** log_id 

**Notes:**
- **admin_id**: Menyimpan referensi User (dengan *role* ADMIN) yang melakukan aksi moderasi tersebut.
- **reason**: Alasan moderasi yang wajib diisi dan dicatat dalam sistem.