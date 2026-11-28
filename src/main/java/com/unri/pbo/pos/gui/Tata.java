package com.unri.pbo.pos.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * Membuat tinggi minimum komponen sama dengan tinggi preferensinya.
 *
 * Tampilan Nimbus memberi tinggi minimum yang jauh lebih besar dari tinggi aslinya
 * (kotak teks, combo, spinner, tombol). Layout memakai minimum itu, sehingga halaman
 * tidak bisa mengecil dan tombol di bagian bawah terpotong di layar yang lebih pendek.
 * Hanya tinggi yang diturunkan; lebar tidak diubah.
 */
final class Tata {

    private static final int TINGGI_TABEL_MIN = 120;

    private Tata() {
    }

    /**
     * Setiap halaman hanya berisi sidebar (anak ke-0) dan konten (anak ke-1).
     * Layout GroupLayout tingkat halaman mengunci tinggi konten ke tinggi preferensinya,
     * jadi diganti BorderLayout: sidebar di kiri, konten mengisi sisa jendela.
     * Layout di dalam masing-masing panel tetap GroupLayout bawaan.
     */
    static void pakaiBorderLayout(JPanel halaman) {
        if (halaman.getComponentCount() != 2) {
            return;
        }
        java.awt.Component sidebar = halaman.getComponent(0);
        java.awt.Component konten = halaman.getComponent(1);
        halaman.removeAll();
        halaman.setLayout(new java.awt.BorderLayout());
        halaman.add(sidebar, java.awt.BorderLayout.WEST);
        halaman.add(konten, java.awt.BorderLayout.CENTER);
    }

    private static boolean memuatTabel(JPanel p) {
        for (Component k : p.getComponents()) {
            if (k instanceof JScrollPane) {
                return true;
            }
        }
        return false;
    }

    static void ringkasTinggi(Container induk) {
        for (Component k : induk.getComponents()) {
            if (k instanceof AbstractButton || k instanceof JTextField || k instanceof JComboBox
                    || k instanceof JSpinner || k instanceof JLabel) {
                Dimension min = k.getMinimumSize();
                Dimension pref = k.getPreferredSize();
                if (pref.height < min.height) {
                    k.setMinimumSize(new Dimension(min.width, pref.height));
                }
            }
            if (k instanceof JPanel && memuatTabel((JPanel) k)) {
                // panel tabel boleh menyusut; tabelnya bisa digulir
                Dimension min = k.getMinimumSize();
                if (min.height > TINGGI_TABEL_MIN) {
                    k.setMinimumSize(new Dimension(min.width, TINGGI_TABEL_MIN));
                }
            }
            if (k instanceof Container) {
                ringkasTinggi((Container) k);
            }
        }
        // minta layout menghitung ulang ukuran minimum setelah diubah
        induk.invalidate();
    }
}
