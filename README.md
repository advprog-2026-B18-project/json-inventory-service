## Database Schema (Neon DB - PostgreSQL)

### Custom Types

* **product_status**: `ACTIVE`, `OUT_OF_STOCK`, `HIDDEN`, `REMOVED_BY_ADMIN`
* **reservation_status**: `PENDING`, `CONFIRMED`, `RELEASED`
* **moderation_action**: `REMOVE`, `RESTORE`, `HIDE`, `ACTIVATE`
* **shopping_mode**: `LIVE`, `PRE_ORDER`, `FLASH_SALE`

---

### 1. Object: Category (`categories`)

| Field | Type | Nullable | Key | Default |
| --- | --- | --- | --- | --- |
| category_id | SERIAL | NOT NULL | PK |  |
| name | VARCHAR(100) | NOT NULL | UK |  |
| slug | VARCHAR(150) | NOT NULL | UK |  |
| description | TEXT | NULL |  |  |
| product_count | INTEGER | NOT NULL |  | 0 |
| created_at | TIMESTAMP | NULL |  | CURRENT_TIMESTAMP |
| updated_at | TIMESTAMP | NULL |  |  |

### 2. Object: Product (`products`)

| Field | Type | Nullable | Key | Default | Notes |
| --- | --- | --- | --- | --- | --- |
| product_id | UUID | NOT NULL | PK | uuid_generate_v4() |  |
| jastiper_id | UUID | NOT NULL | Logical FK |  | Refers to Modul Auth |
| category_id | INTEGER | NULL | FK |  | Refers to `categories` |
| name | VARCHAR(255) | NOT NULL |  |  |  |
| description | TEXT | NOT NULL |  |  |  |
| price | BIGINT | NOT NULL |  |  | CHECK (price >= 0) |
| service_fee | BIGINT | NOT NULL |  | 0 | CHECK (service_fee >= 0) |
| stock | INTEGER | NOT NULL |  |  | CHECK (stock >= 0) |
| origin_country | VARCHAR(255) | NOT NULL |  |  |  |
| purchase_date | DATE | NOT NULL |  |  |  |
| weight_gram | INTEGER | NULL |  |  |  |
| images | TEXT[] | NULL |  | '{}' |  |
| tags | TEXT[] | NULL |  | '{}' |  |
| status | VARCHAR(30) | NOT NULL |  | 'ACTIVE' | Semantically maps to `product_status` |
| avg_rating | FLOAT | NULL |  |  |  |
| total_reviews | INTEGER | NOT NULL |  | 0 |  |
| total_orders | INTEGER | NOT NULL |  | 0 |  |
| deleted_at | TIMESTAMP | NULL |  |  |  |
| created_at | TIMESTAMP | NULL |  | CURRENT_TIMESTAMP |  |
| updated_at | TIMESTAMP | NULL |  | CURRENT_TIMESTAMP |  |
| mode | VARCHAR(30) | NOT NULL |  | 'LIVE' | Semantically maps to `shopping_mode` |
| flash_sale_start | TIMESTAMP| NULL |  |  |  |
| flash_sale_end | TIMESTAMP | NULL |  |  |  |

### 3. Object: Stock Reservation (`stock_reservations`)

| Field | Type | Nullable | Key | Default | Notes |
| --- | --- | --- | --- | --- | --- |
| reservation_id | UUID | NOT NULL | PK | uuid_generate_v4() |  |
| product_id | UUID | NOT NULL | FK, UK |  | Refers to `products` |
| order_id | UUID | NOT NULL | UK |  | Refers to Modul Order |
| quantity | INTEGER | NOT NULL |  |  | CHECK (quantity > 0) |
| status | VARCHAR(30) | NOT NULL |  | 'PENDING' | Semantically maps to `reservation_status` |
| created_at | TIMESTAMP | NULL |  | CURRENT_TIMESTAMP |  |
| expires_at | TIMESTAMP | NULL |  |  |  |

> **Note:** Terdapat constraint `UNIQUE (order_id, product_id)` untuk mencegah duplikasi reservasi produk pada pesanan yang sama.

### 4. Object: Moderation Log (`moderation_logs`)

| Field | Type | Nullable | Key | Default | Notes |
| --- | --- | --- | --- | --- | --- |
| log_id | UUID | NOT NULL | PK | uuid_generate_v4() |  |
| product_id | UUID | NOT NULL | FK |  | Refers to `products` |
| admin_id | UUID | NOT NULL | Logical FK |  | Refers to Modul Auth |
| action | VARCHAR(30) | NOT NULL |  |  | Semantically maps to `moderation_action` |
| reason | TEXT | NOT NULL |  |  |  |
| created_at | TIMESTAMP | NULL |  | CURRENT_TIMESTAMP |  |

### 5. Object: Product Images & Tags (`product_images` & `product_tags`)

| Table | Field | Type | Nullable | Key | Notes |
| --- | --- | --- | --- | --- | --- |
| `product_images` | product_id | UUID | NOT NULL | FK | Refers to `products` |
| `product_images` | image | VARCHAR(255) | NULL |  |  |
| `product_images` | image_url | VARCHAR(255) | NULL |  |  |
| `product_tags` | product_id | UUID | NOT NULL | FK | Refers to `products` |
| `product_tags` | tag | VARCHAR(255) | NULL |  |  |

---

### Database Indexes

1. `idx_products_search`: Menggunakan algoritma **GIN** (`to_tsvector`) untuk optimasi pencarian full-text pada kolom `name` dan `description`.
2. `idx_products_jastiper`: Optimasi query berdasarkan `jastiper_id`.
3. `idx_products_status`: Optimasi query untuk filter produk berdasarkan statusnya (`ACTIVE`, dll).
4. `idx_reservation_order_product`: Optimasi query reservasi berdasarkan `order_id` dan `product_id`.