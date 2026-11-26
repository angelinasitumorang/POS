/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.unri.pbo.pos.gui;
import com.unri.pbo.pos.DataToko;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

    public class MainFrame extends javax.swing.JFrame {
    private static final Color HIJAU_TUA = new Color(27, 77, 62);
    private static final Color HIJAU_MENU = new Color(42, 107, 85);
    private static final Color HIJAU_HOVER = new Color(54, 128, 102);
    private static final Color EMAS = new Color(242, 183, 5);
    private static final Color KREM = new Color(247, 243, 232);
    private static final Color MERAH_TUA = new Color(142, 42, 32);
    private static final Color MERAH_HOVER = new Color(168, 54, 42);
    private static final String FONT = "Segoe UI";

    // ---------------- menu sidebar ----------------
    private static final String[] KUNCI = {"dashboard", "produk", "pelanggan", "supplier",
        "pembelian", "penjualan", "riwayat"};
    private static final String[] JUDUL = {"Dashboard", "Produk", "Pelanggan", "Supplier",
        "Pembelian", "Penjualan", "Riwayat Transaksi"};

    /** Dipakai halaman lain untuk pindah halaman: MainFrame.instance.tampilHalaman("dashboard") */
    public static MainFrame instance;

    // ---------------- komponen ----------------
    private final JPanel pnlNavbar = new JPanel(new BorderLayout());
    private final JPanel pnlSidebar = new JPanel(new BorderLayout());
    private final JPanel pnlContent = new JPanel(new CardLayout());

    private final JLabel lblLogo = new JLabel("KhasKita");
    private final JLabel lblTagline = new JLabel("Oleh-oleh Khas Riau");
    private final JLabel lblTanggal = new JLabel();
    private final JLabel lblAdmin = new JLabel();

    private final JButton[] btnMenu = new JButton[KUNCI.length];
    private final JButton btnKeluar = new JButton("Keluar");

    // ---------------- halaman ----------------
    private final FrameDashboard pDashboard = new FrameDashboard();
    private final FrameProduk pProduk = new FrameProduk();
    private final FramePelanggan pPelanggan = new FramePelanggan();
    private final FrameSupplier pSupplier = new FrameSupplier();
    private final FramePembelian pPembelian = new FramePembelian();
    private final FramePenjualan pPenjualan = new FramePenjualan();
    private final FrameRiwayat pRiwayat = new FrameRiwayat();

    private String halamanAktif = "";

    public MainFrame() {
        setTitle("KhasKita - Sistem Penjualan Oleh-oleh Khas Riau");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        buatNavbar();
        buatSidebar();
        buatKonten();

        Container isi = getContentPane();
        isi.setLayout(new BorderLayout());
        isi.add(pnlNavbar, BorderLayout.NORTH);
        isi.add(pnlSidebar, BorderLayout.WEST);
        isi.add(pnlContent, BorderLayout.CENTER);

        pack();                         // ukuran frame mengikuti panel: 1000 x 600
        setLocationRelativeTo(null);    // tampil di tengah layar
        instance = this;

        tampilHalaman("dashboard");

        // peringatan kalau database belum terhubung (muncul setelah jendela terbuka)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (!DataToko.dbTerhubung()) {
                    DataToko.galat(MainFrame.this, "Aplikasi belum terhubung ke database:\n"
                            + DataToko.pesanDb()
                            + "\n\nPastikan MySQL di XAMPP sudah Start dan tabel sudah dibuat (aplikasi_pos.sql).");
                }
            }
        });
    }

    // =====================================================================
    //  NAVBAR (atas)
    // =====================================================================
    private void buatNavbar() {
        pnlNavbar.setPreferredSize(new Dimension(1000, 50));
        pnlNavbar.setBackground(EMAS);
        pnlNavbar.setBorder(new EmptyBorder(0, 20, 0, 0));

        lblLogo.setFont(new Font(FONT, Font.BOLD, 22));
        lblLogo.setForeground(HIJAU_TUA);
        lblTagline.setFont(new Font(FONT, Font.PLAIN, 11));
        lblTagline.setForeground(HIJAU_TUA);

        JPanel kiri = new JPanel();
        kiri.setOpaque(false);
        kiri.setLayout(new BoxLayout(kiri, BoxLayout.Y_AXIS));
        kiri.add(Box.createVerticalGlue());
        kiri.add(lblLogo);
        kiri.add(lblTagline);
        kiri.add(Box.createVerticalGlue());

        lblTanggal.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id-ID"))));
        lblTanggal.setFont(new Font(FONT, Font.PLAIN, 13));
        lblTanggal.setForeground(HIJAU_TUA);

        lblAdmin.setText("Admin: " + DataToko.ADMIN);
        lblAdmin.setFont(new Font(FONT, Font.BOLD, 13));
        lblAdmin.setForeground(HIJAU_TUA);

        JPanel kanan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 14));
        kanan.setOpaque(false);
        kanan.add(lblTanggal);
        kanan.add(lblAdmin);

        pnlNavbar.add(kiri, BorderLayout.WEST);
        pnlNavbar.add(kanan, BorderLayout.EAST);
    }

    // =====================================================================
    //  SIDEBAR (kiri)
    // =====================================================================
    private void buatSidebar() {
        pnlSidebar.setPreferredSize(new Dimension(200, 550));
        pnlSidebar.setBackground(HIJAU_TUA);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(14, 10, 0, 10));

        for (int i = 0; i < KUNCI.length; i++) {
            final String kunci = KUNCI[i];
            JButton b = buatTombol(JUDUL[i]);
            b.addActionListener(e -> tampilHalaman(kunci));
            b.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!kunci.equals(halamanAktif)) {
                        b.setBackground(HIJAU_HOVER);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!kunci.equals(halamanAktif)) {
                        b.setBackground(HIJAU_MENU);
                    }
                }
            });
            btnMenu[i] = b;
            menu.add(b);
            menu.add(Box.createVerticalStrut(8));
        }

        // tombol Keluar di dasar sidebar
        gayaTombol(btnKeluar, MERAH_TUA, Color.WHITE, true);
        btnKeluar.addActionListener(e -> keluar());
        btnKeluar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnKeluar.setBackground(MERAH_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnKeluar.setBackground(MERAH_TUA);
            }
        });
        JPanel bawah = new JPanel(new BorderLayout());
        bawah.setOpaque(false);
        bawah.setBorder(new EmptyBorder(0, 10, 14, 10));
        bawah.add(btnKeluar, BorderLayout.CENTER);

        pnlSidebar.add(menu, BorderLayout.NORTH);
        pnlSidebar.add(bawah, BorderLayout.SOUTH);
    }

    private JButton buatTombol(String teks) {
        JButton b = new JButton(teks);
        gayaTombol(b, HIJAU_MENU, Color.WHITE, false);
        return b;
    }

    /** Ukuran 180 x 38, rata kiri, margin [4, 16, 4, 8], tanpa garis tepi. */
    private void gayaTombol(JButton b, Color latar, Color teks, boolean tebal) {
        Dimension ukuran = new Dimension(180, 38);
        b.setPreferredSize(ukuran);
        b.setMinimumSize(ukuran);
        b.setMaximumSize(ukuran);
        b.setFont(new Font(FONT, tebal ? Font.BOLD : Font.PLAIN, 14));
        b.setBackground(latar);
        b.setForeground(teks);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMargin(new Insets(4, 16, 4, 8));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    // =====================================================================
    //  KONTEN (tengah)
    // =====================================================================
    private void buatKonten() {
        pnlContent.setPreferredSize(new Dimension(800, 550));
        pnlContent.setBackground(KREM);

        pnlContent.add(pDashboard, "dashboard");
        pnlContent.add(pProduk, "produk");
        pnlContent.add(pPelanggan, "pelanggan");
        pnlContent.add(pSupplier, "supplier");
        pnlContent.add(pPembelian, "pembelian");
        pnlContent.add(pPenjualan, "penjualan");
        pnlContent.add(pRiwayat, "riwayat");
    }

    // =====================================================================
    //  PINDAH HALAMAN
    // =====================================================================
    public void tampilHalaman(String nama) {
        switch (nama) {
            case "dashboard": pDashboard.refresh(); break;
            case "produk": pProduk.refresh(); break;
            case "pelanggan": pPelanggan.refresh(); break;
            case "supplier": pSupplier.refresh(); break;
            case "pembelian": pPembelian.refresh(); break;
            case "penjualan": pPenjualan.refresh(); break;
            case "riwayat": pRiwayat.refresh(); break;
            default: break;
        }
        ((CardLayout) pnlContent.getLayout()).show(pnlContent, nama);
        halamanAktif = nama;
        warnaiMenu();
    }

    private void warnaiMenu() {
        for (int i = 0; i < btnMenu.length; i++) {
            boolean aktif = KUNCI[i].equals(halamanAktif);
            btnMenu[i].setBackground(aktif ? EMAS : HIJAU_MENU);
            btnMenu[i].setForeground(aktif ? HIJAU_TUA : Color.WHITE);
            btnMenu[i].setFont(new Font(FONT, aktif ? Font.BOLD : Font.PLAIN, 14));
        }
    }

    private void keluar() {
        if (DataToko.tanya(this, "Tutup aplikasi KhasKita?")) {
            System.exit(0);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
   
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
