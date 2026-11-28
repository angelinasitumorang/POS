package com.unri.pbo.pos.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/** Menghubungkan tombol-tombol sidebar di tiap panel ke MainFrame dan mewarnai menu yang aktif. */
final class Navigasi {

    private static final Color HIJAU_TUA = new Color(27, 77, 62);
    private static final Color HIJAU_MENU = new Color(42, 107, 85);
    private static final Color HIJAU_HOVER = new Color(54, 128, 102);
    private static final Color EMAS = new Color(242, 183, 5);
    private static final Color MERAH = new Color(192, 57, 43);
    private static final Color MERAH_HOVER = new Color(220, 80, 65);

    private Navigasi() {
    }

    /** aktif = nama halaman milik panel ini ("produk", "dashboard", dst). */
    static void pasang(String aktif, JButton dashboard, JButton produk, JButton pelanggan, JButton supplier,
            JButton pembelian, JButton penjualan, JButton riwayat, JButton keluar) {
        sambung(aktif, dashboard, "dashboard");
        sambung(aktif, produk, "produk");
        sambung(aktif, pelanggan, "pelanggan");
        sambung(aktif, supplier, "supplier");
        sambung(aktif, pembelian, "pembelian");
        sambung(aktif, penjualan, "penjualan");
        sambung(aktif, riwayat, "riwayat");
        gayaKeluar(keluar);
        keluar.addActionListener(e -> MainFrame.instance.keluar());
    }

    /** Tombol Keluar dibuat jelas: latar merah, teks putih tebal, ukuran cukup besar. */
    private static void gayaKeluar(JButton tombol) {
        tombol.setContentAreaFilled(false);
        tombol.setOpaque(true);
        tombol.setBorderPainted(false);
        tombol.setFocusPainted(false);
        tombol.setBackground(MERAH);
        tombol.setForeground(Color.WHITE);
        tombol.setFont(tombol.getFont().deriveFont(Font.BOLD, 13f));
        tombol.setMargin(new java.awt.Insets(4, 12, 4, 12));
        tombol.setPreferredSize(new java.awt.Dimension(100, 30));
        tombol.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tombol.setBackground(MERAH_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tombol.setBackground(MERAH);
            }
        });
    }

    private static void sambung(String aktif, JButton tombol, String halaman) {
        boolean menuIni = halaman.equals(aktif);
        // opaque + tanpa isi bawaan supaya warna latar benar-benar tampil di tema Nimbus
        tombol.setContentAreaFilled(false);
        tombol.setOpaque(true);
        tombol.setBorderPainted(false);
        tombol.setFocusPainted(false);
        tombol.setBackground(menuIni ? EMAS : HIJAU_MENU);
        tombol.setForeground(menuIni ? HIJAU_TUA : Color.WHITE);
        tombol.setFont(tombol.getFont().deriveFont(menuIni ? Font.BOLD : Font.PLAIN, 14f));
        tombol.addActionListener(e -> MainFrame.instance.tampilHalaman(halaman));
        if (!menuIni) {
            tombol.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    tombol.setBackground(HIJAU_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    tombol.setBackground(HIJAU_MENU);
                }
            });
        }
    }
}
