-- =====================================================================
--  Database aplikasi KhasKita (POS oleh-oleh khas Riau)
--  Cara pakai (XAMPP): buka phpMyAdmin > tab "Import" > pilih file ini > Go.
--  Atau lewat terminal:  mysql -u root < aplikasi_pos.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS aplikasi_pos
    CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE aplikasi_pos;

-- Hapus tabel lama (urutan: anak dulu, baru induk)
DROP TABLE IF EXISTS riwayat_transaksi;
DROP TABLE IF EXISTS detail_penjualan;
DROP TABLE IF EXISTS detail_pembelian;
DROP TABLE IF EXISTS penjualan;
DROP TABLE IF EXISTS pembelian;
DROP TABLE IF EXISTS produk;
DROP TABLE IF EXISTS pelanggan;
DROP TABLE IF EXISTS supplier;

CREATE TABLE produk (
    id_produk   VARCHAR(20)   NOT NULL,
    nama_produk VARCHAR(100)  NOT NULL,
    harga_beli  DECIMAL(15,2) NOT NULL DEFAULT 0,
    harga_jual  DECIMAL(15,2) NOT NULL DEFAULT 0,
    stok        INT           NOT NULL DEFAULT 0,
    PRIMARY KEY (id_produk)
) ENGINE=InnoDB;

CREATE TABLE pelanggan (
    id_pelanggan    VARCHAR(20)  NOT NULL,
    nama            VARCHAR(100) NOT NULL,
    alamat          VARCHAR(255) NULL,
    no_telepon      VARCHAR(30)  NULL,
    jenis_pelanggan VARCHAR(30)  NOT NULL DEFAULT 'Umum',
    PRIMARY KEY (id_pelanggan)
) ENGINE=InnoDB;

CREATE TABLE supplier (
    id_supplier     VARCHAR(20)  NOT NULL,
    nama            VARCHAR(100) NOT NULL,
    alamat          VARCHAR(255) NULL,
    no_telepon      VARCHAR(30)  NULL,
    nama_perusahaan VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_supplier)
) ENGINE=InnoDB;

CREATE TABLE penjualan (
    id_penjualan VARCHAR(20)   NOT NULL,
    tanggal      DATETIME      NOT NULL,
    id_pelanggan VARCHAR(20)   NOT NULL,
    total        DECIMAL(15,2) NOT NULL DEFAULT 0,
    status       VARCHAR(20)   NOT NULL DEFAULT 'Selesai',
    PRIMARY KEY (id_penjualan),
    CONSTRAINT fk_penjualan_pelanggan FOREIGN KEY (id_pelanggan) REFERENCES pelanggan (id_pelanggan)
) ENGINE=InnoDB;

CREATE TABLE detail_penjualan (
    id_detail    VARCHAR(30)   NOT NULL,
    id_penjualan VARCHAR(20)   NOT NULL,
    id_produk    VARCHAR(20)   NOT NULL,
    harga_jual   DECIMAL(15,2) NOT NULL,
    jumlah       INT           NOT NULL,
    subtotal     DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id_detail),
    CONSTRAINT fk_dpj_penjualan FOREIGN KEY (id_penjualan) REFERENCES penjualan (id_penjualan),
    CONSTRAINT fk_dpj_produk    FOREIGN KEY (id_produk)    REFERENCES produk (id_produk)
) ENGINE=InnoDB;

CREATE TABLE pembelian (
    id_pembelian VARCHAR(20)   NOT NULL,
    tanggal      DATETIME      NOT NULL,
    id_supplier  VARCHAR(20)   NOT NULL,
    total        DECIMAL(15,2) NOT NULL DEFAULT 0,
    status       VARCHAR(20)   NOT NULL DEFAULT 'Selesai',
    PRIMARY KEY (id_pembelian),
    CONSTRAINT fk_pembelian_supplier FOREIGN KEY (id_supplier) REFERENCES supplier (id_supplier)
) ENGINE=InnoDB;

CREATE TABLE detail_pembelian (
    id_detail    VARCHAR(30)   NOT NULL,
    id_pembelian VARCHAR(20)   NOT NULL,
    id_produk    VARCHAR(20)   NOT NULL,
    harga_beli   DECIMAL(15,2) NOT NULL,
    jumlah       INT           NOT NULL,
    subtotal     DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (id_detail),
    CONSTRAINT fk_dpb_pembelian FOREIGN KEY (id_pembelian) REFERENCES pembelian (id_pembelian),
    CONSTRAINT fk_dpb_produk    FOREIGN KEY (id_produk)    REFERENCES produk (id_produk)
) ENGINE=InnoDB;

CREATE TABLE riwayat_transaksi (
    id_riwayat       INT           NOT NULL AUTO_INCREMENT,
    waktu            DATETIME      NOT NULL,
    jenis_transaksi  VARCHAR(20)   NOT NULL,
    id_transaksi     VARCHAR(20)   NOT NULL,
    aktivitas        VARCHAR(50)   NOT NULL,
    detail_aktivitas VARCHAR(255)  NULL,
    jumlah_lama      INT           NULL,
    jumlah_baru      INT           NULL,
    total_lama       DECIMAL(15,2) NULL,
    total_baru       DECIMAL(15,2) NULL,
    pengguna         VARCHAR(50)   NULL,
    alasan           VARCHAR(255)  NULL,
    PRIMARY KEY (id_riwayat),
    KEY idx_riwayat_transaksi (id_transaksi)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
--  Data contoh (boleh dihapus / diganti lewat aplikasi)
-- ---------------------------------------------------------------------
INSERT INTO produk (id_produk, nama_produk, harga_beli, harga_jual, stok) VALUES
    ('BRG001', 'Bolu Kemojo',          25000, 35000, 40),
    ('BRG002', 'Kain Songket Siak',   150000, 225000, 12),
    ('BRG003', 'Keripik Sale Pisang',  12000, 18000, 60),
    ('BRG004', 'Dodol Nenas',          15000, 22000, 35),
    ('BRG005', 'Kerupuk Ikan Selais',  20000, 30000, 50);

INSERT INTO pelanggan (id_pelanggan, nama, alamat, no_telepon, jenis_pelanggan) VALUES
    ('P001', 'Andi',  'Pekanbaru', '08123456789', 'Member'),
    ('P002', 'Siti',  'Dumai',     '08122334455', 'Umum');

INSERT INTO supplier (id_supplier, nama, alamat, no_telepon, nama_perusahaan) VALUES
    ('S001', 'Budi',  'Pekanbaru', '08234567890', 'PT Sumber Makmur'),
    ('S002', 'Rina',  'Siak',      '08567891234', 'CV Songket Riau');
