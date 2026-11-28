package com.unri.pbo.pos.gui;

import com.unri.pbo.Pembelian;
import com.unri.pbo.Penjualan;
import com.unri.pbo.pos.DataToko;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

/** Fungsi-fungsi kecil yang dipakai bersama oleh semua halaman. */
final class Bantu {

    private Bantu() {
    }

    /** Mengatur lebar kolom tabel (urut dari kolom pertama). */
    static void lebarKolom(JTable tabel, int... lebar) {
        for (int i = 0; i < lebar.length && i < tabel.getColumnModel().getColumnCount(); i++) {
            tabel.getColumnModel().getColumn(i).setPreferredWidth(lebar[i]);
        }
    }

    /** Membaca uang rupiah bulat: "35000", "35.000" atau "Rp 35.000". */
    static BigDecimal uang(String teks, String namaIsian) {
        String t = teks == null ? "" : teks.trim().replace("Rp", "").replace("rp", "").replace(" ", "");
        if (t.isEmpty()) {
            throw new IllegalArgumentException(namaIsian + " wajib diisi.");
        }
        if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", "");
        }
        if (!t.matches("\\d+")) {
            throw new IllegalArgumentException(namaIsian + " harus berupa angka bulat (contoh: 35000).");
        }
        return new BigDecimal(t);
    }

    /** Membaca bilangan bulat tidak negatif. */
    static int bulat(String teks, String namaIsian) {
        String t = teks == null ? "" : teks.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException(namaIsian + " wajib diisi.");
        }
        if (!t.matches("\\d{1,9}")) {
            throw new IllegalArgumentException(namaIsian + " harus berupa bilangan bulat tidak negatif.");
        }
        return Integer.parseInt(t);
    }

    static String wajib(String teks, String namaIsian) {
        String t = teks == null ? "" : teks.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException(namaIsian + " wajib diisi.");
        }
        return t;
    }

    /** Mengisi combo box; pilihan dipertahankan kalau teksnya masih ada. */
    static void isiCombo(JComboBox<String> combo, List<String> isi) {
        Object lama = combo.getSelectedItem();
        combo.setModel(new DefaultComboBoxModel<>(isi.toArray(new String[0])));
        if (lama != null && isi.contains(lama.toString())) {
            combo.setSelectedItem(lama);
        } else if (!isi.isEmpty()) {
            combo.setSelectedIndex(0);
        }
    }

    /** Meminta satu baris teks dari pengguna. Mengembalikan null kalau dibatalkan atau kosong. */
    static String minta(Component induk, String judul, String pertanyaan) {
        String jawab = JOptionPane.showInputDialog(induk, pertanyaan, judul, JOptionPane.QUESTION_MESSAGE);
        if (jawab == null || jawab.trim().isEmpty()) {
            return null;
        }
        return jawab.trim();
    }

    /** Membuang pesanan yang belum disimpan; stok yang sempat dipesan dikembalikan. */
    static void buang(Penjualan p) {
        if (p == null) {
            return;
        }
        DataToko.draftAktif.remove(p);
        try {
            p.batalkanTransaksi(DataToko.ADMIN, "Pesanan dibuang sebelum disimpan");
        } catch (RuntimeException e) {
            // draf kosong atau sudah dibatalkan: tidak ada stok yang perlu dikembalikan
        }
    }

    static void buang(Pembelian p) {
        if (p == null) {
            return;
        }
        DataToko.draftAktif.remove(p);
        try {
            p.batalkanTransaksi(DataToko.ADMIN, "Pembelian dibuang sebelum disimpan");
        } catch (RuntimeException e) {
            // draf kosong atau sudah dibatalkan
        }
    }

    /** Memuat ulang semua data dari database (dipakai kalau penyimpanan gagal di tengah jalan). */
    static void muatUlang(Component induk) {
        try {
            DataToko.muatSemua();
        } catch (java.sql.SQLException e) {
            DataToko.galat(induk, "Gagal memuat ulang data dari database: " + e.getMessage());
        }
    }

    /** Mengecek apakah database masih bisa dihubungi. */
    static boolean dbHidup() {
        try (java.sql.Connection c = com.unri.pbo.pos.Koneksi.getConnection()) {
            return c != null;
        } catch (java.sql.SQLException e) {
            return false;
        }
    }
}
