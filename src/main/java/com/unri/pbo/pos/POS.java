/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.unri.pbo;


import java.math.BigDecimal;

/**
 *
 * @author gitasastria
 */
public class POS {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        
        String admin = "Admin01";

        try {

            // =================================
            // 1. DATA PELANGGAN
            // =================================

            Pelanggan pelanggan = new Pelanggan(
                "P001",
                "Andi",
                "Pekanbaru",
                "08123456789",
                "Member"
            );

            // =================================
            // 2. DATA SUPPLIER
            // =================================

            Supplier supplier = new Supplier(
                "S001",
                "Budi",
                "Jakarta",
                "08234567890",
                "PT Sumber Makmur"
            );

            // =================================
            // 3. DATA PRODUK
            // =================================

            Produk laptop = new Produk(
                "BRG001",
                "Laptop",
                new BigDecimal("4000000"),
                new BigDecimal("5000000"),
                10
            );

            Produk mouse = new Produk(
                "BRG002",
                "Mouse",
                new BigDecimal("50000"),
                new BigDecimal("100000"),
                20
            );

            // =================================
            // 4. POLYMORPHISM
            // =================================

            System.out.println("=== POLYMORPHISM ===");

            Orang orang1 = pelanggan;
            Orang orang2 = supplier;

            orang1.aktivitas();
            orang2.aktivitas();

            // =================================
            // 5. TRANSAKSI PEMBELIAN
            // =================================

            System.out.println("\n=== TRANSAKSI PEMBELIAN ===");

            Pembelian pembelian = new Pembelian(
                "PB001", supplier
            );

            pembelian.tambahPembelian(
                laptop, 5, admin, "Restock laptop"
            );

            pembelian.tambahPembelian(
                mouse, 10, admin, "Restock mouse"
            );

            // Mengubah jumlah pembelian
            pembelian.ubahDetail(
                "PB001-D1", 4,
                admin, "Koreksi jumlah pesanan"
            );

            pembelian.cetakBuktiPembelian();

            // =================================
            // 6. TRANSAKSI PENJUALAN
            // =================================

            System.out.println("\n=== TRANSAKSI PENJUALAN ===");

            Penjualan penjualan = new Penjualan(
                "PJ001", pelanggan
            );

            penjualan.tambahPenjualan(
                laptop, 2, admin, "Penjualan pelanggan"
            );

            penjualan.tambahPenjualan(
                mouse, 3, admin, "Penjualan pelanggan"
            );

            // Mengubah jumlah laptop
            penjualan.ubahDetail(
                "PJ001-D1", 3,
                admin, "Koreksi jumlah barang"
            );

            // Menghapus detail mouse
            penjualan.hapusDetail(
                "PJ001-D2",
                admin,
                "Pelanggan membatalkan pembelian mouse"
            );

            penjualan.cetakNota();

            // =================================
            // 7. FINALISASI
            // =================================

            System.out.println("\n=== FINALISASI ===");

            pembelian.finalisasiTransaksi(admin);
            penjualan.finalisasiTransaksi(admin);

            // =================================
            // 8. UJI VALIDASI
            // =================================

            System.out.println("\n=== UJI VALIDASI ===");

            try {
                penjualan.tambahPenjualan(
                    laptop, 1,
                    admin, "Percobaan perubahan"
                );

            } catch (IllegalStateException e) {
                System.out.println(
                    "Validasi berhasil: " + e.getMessage()
                );
            }

            // =================================
            // 9. MENAMPILKAN RIWAYAT
            // =================================

            System.out.println("\n=== RIWAYAT TRANSAKSI ===");

            penjualan.tampilkanSemuaRiwayat();

            pembelian.tampilkanSemuaRiwayat();

            // =================================
            // 10. PEMBATALAN TRANSAKSI
            // =================================

            System.out.println("\n=== UJI PEMBATALAN ===");

            // Pembatalan penjualan yang sudah final
            penjualan.batalkanTransaksi(
                admin,
                "Pelanggan mengajukan pembatalan"
            );

            // Pembatalan pembelian yang sudah final
            pembelian.batalkanTransaksi(
                admin,
                "Pembelian dibatalkan karena kesalahan pesanan"
            );

            // =================================
            // 11. RIWAYAT SETELAH PEMBATALAN
            // =================================

            System.out.println(
                "\n=== RIWAYAT SETELAH PEMBATALAN ==="
            );

            penjualan.tampilkanSemuaRiwayat();

            pembelian.tampilkanSemuaRiwayat();

            // =================================
            // 12. STOK AKHIR
            // =================================

            System.out.println("\n=== STOK AKHIR ===");

            laptop.tampilkanProduk();

            System.out.println();

            mouse.tampilkanProduk();

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            System.out.println(
                "Terjadi kesalahan: " + e.getMessage()
            );
        }
    }
}
