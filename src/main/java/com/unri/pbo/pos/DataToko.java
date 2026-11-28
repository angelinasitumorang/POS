package com.unri.pbo.pos;

import com.unri.pbo.DetailPembelian;
import com.unri.pbo.DetailPenjualan;
import com.unri.pbo.Pelanggan;
import com.unri.pbo.Pembelian;
import com.unri.pbo.Penjualan;
import com.unri.pbo.Produk;
import com.unri.pbo.RiwayatTransaksi;
import com.unri.pbo.Supplier;
import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Penghubung antara aplikasi dan database MySQL (aplikasi_pos).
 * Semua halaman membaca dan menyimpan data lewat class ini.
 * Saat aplikasi dibuka, data dibaca dari database. Setiap perubahan langsung disimpan ke database.
 */
public class DataToko {

    public static final String ADMIN = "Admin01";

    // ---------------- data di memori (hasil baca dari database) ----------------
    public static final List<Produk> produk = new ArrayList<>();
    public static final List<Pelanggan> pelanggan = new ArrayList<>();
    public static final List<Supplier> supplier = new ArrayList<>();
    public static final List<RekamPenjualan> penjualan = new ArrayList<>();
    public static final List<RekamPembelian> pembelian = new ArrayList<>();

    private static String galatDb = null;

    /**
     * Draf transaksi (pesanan penjualan / pembelian) yang belum disimpan. Selama ada draf, stok di
     * memori sudah berubah, jadi data produk tidak boleh diubah atau dihapus dulu.
     */
    public static final java.util.Set<Object> draftAktif = new java.util.HashSet<>();

    private DataToko() {
    }

    // =====================================================================
    //  CATATAN TRANSAKSI (data sederhana yang dibaca dari tabel)
    // =====================================================================
    public static class RekamDetail {

        public final String idDetail;
        public final Produk produk;
        public final BigDecimal harga;
        public final int jumlah;
        public final BigDecimal subtotal;

        public RekamDetail(String idDetail, Produk produk, BigDecimal harga, int jumlah, BigDecimal subtotal) {
            this.idDetail = idDetail;
            this.produk = produk;
            this.harga = harga;
            this.jumlah = jumlah;
            this.subtotal = subtotal;
        }
    }

    public static class RekamPenjualan {

        public final String id;
        public final LocalDateTime tanggal;
        public final Pelanggan pelanggan;
        public String status;
        public final BigDecimal total;
        public final List<RekamDetail> detail = new ArrayList<>();

        public RekamPenjualan(String id, LocalDateTime tanggal, Pelanggan pelanggan, String status, BigDecimal total) {
            this.id = id;
            this.tanggal = tanggal;
            this.pelanggan = pelanggan;
            this.status = status;
            this.total = total;
        }
    }

    public static class RekamPembelian {

        public final String id;
        public final LocalDateTime tanggal;
        public final Supplier supplier;
        public String status;
        public final BigDecimal total;
        public final List<RekamDetail> detail = new ArrayList<>();

        public RekamPembelian(String id, LocalDateTime tanggal, Supplier supplier, String status, BigDecimal total) {
            this.id = id;
            this.tanggal = tanggal;
            this.supplier = supplier;
            this.status = status;
            this.total = total;
        }
    }

    // =====================================================================
    //  STATUS KONEKSI
    // =====================================================================
    public static boolean dbTerhubung() {
        return galatDb == null;
    }

    public static String pesanDb() {
        return galatDb;
    }

    /** Mencoba membaca ulang semua data dari database (dipakai kalau MySQL baru dinyalakan). */
    public static boolean sambungUlang() {
        try {
            muatSemua();
            galatDb = null;
            return true;
        } catch (SQLException | RuntimeException e) {
            galatDb = e.getMessage() == null ? e.toString() : e.getMessage();
            return false;
        }
    }

    private static String pesanSql(SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("23")) {
            return "Data ini tidak bisa diproses: ID sudah ada, atau data sedang dipakai data lain (misalnya sudah dipakai di transaksi).";
        }
        return "Kesalahan database: " + e.getMessage();
    }

    private static BigDecimal skala0(BigDecimal b) {
        return b == null ? BigDecimal.ZERO : b.setScale(0, RoundingMode.HALF_UP);
    }

    // =====================================================================
    //  MEMBACA DATA DARI DATABASE
    // =====================================================================
    public static void muatSemua() throws SQLException {
        produk.clear();
        pelanggan.clear();
        supplier.clear();
        penjualan.clear();
        pembelian.clear();

        try (Connection c = Koneksi.getConnection(); Statement st = c.createStatement()) {

            try (ResultSet rs = st.executeQuery(
                    "SELECT id_produk, nama_produk, harga_beli, harga_jual, stok FROM produk ORDER BY id_produk")) {
                while (rs.next()) {
                    produk.add(new Produk(rs.getString(1), rs.getString(2),
                            skala0(rs.getBigDecimal(3)), skala0(rs.getBigDecimal(4)), rs.getInt(5)));
                }
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT id_pelanggan, nama, alamat, no_telepon, jenis_pelanggan FROM pelanggan ORDER BY id_pelanggan")) {
                while (rs.next()) {
                    pelanggan.add(new Pelanggan(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
            }

            try (ResultSet rs = st.executeQuery(
                    "SELECT id_supplier, nama, alamat, no_telepon, nama_perusahaan FROM supplier ORDER BY id_supplier")) {
                while (rs.next()) {
                    supplier.add(new Supplier(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
                }
            }

            Map<String, RekamPenjualan> petaJual = new HashMap<>();
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_penjualan, tanggal, id_pelanggan, total, status FROM penjualan ORDER BY id_penjualan")) {
                while (rs.next()) {
                    Pelanggan pel = cariPelanggan(rs.getString(3));
                    if (pel == null) {
                        continue;
                    }
                    RekamPenjualan r = new RekamPenjualan(rs.getString(1), rs.getTimestamp(2).toLocalDateTime(),
                            pel, rs.getString(5), skala0(rs.getBigDecimal(4)));
                    petaJual.put(r.id, r);
                    penjualan.add(r);
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_detail, id_penjualan, id_produk, harga_jual, jumlah, subtotal FROM detail_penjualan ORDER BY id_penjualan, id_detail")) {
                while (rs.next()) {
                    RekamPenjualan r = petaJual.get(rs.getString(2));
                    Produk p = cariProduk(rs.getString(3));
                    if (r != null && p != null) {
                        r.detail.add(new RekamDetail(rs.getString(1), p, skala0(rs.getBigDecimal(4)),
                                rs.getInt(5), skala0(rs.getBigDecimal(6))));
                    }
                }
            }

            Map<String, RekamPembelian> petaBeli = new HashMap<>();
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_pembelian, tanggal, id_supplier, total, status FROM pembelian ORDER BY id_pembelian")) {
                while (rs.next()) {
                    Supplier sup = cariSupplier(rs.getString(3));
                    if (sup == null) {
                        continue;
                    }
                    RekamPembelian r = new RekamPembelian(rs.getString(1), rs.getTimestamp(2).toLocalDateTime(),
                            sup, rs.getString(5), skala0(rs.getBigDecimal(4)));
                    petaBeli.put(r.id, r);
                    pembelian.add(r);
                }
            }
            try (ResultSet rs = st.executeQuery(
                    "SELECT id_detail, id_pembelian, id_produk, harga_beli, jumlah, subtotal FROM detail_pembelian ORDER BY id_pembelian, id_detail")) {
                while (rs.next()) {
                    RekamPembelian r = petaBeli.get(rs.getString(2));
                    Produk p = cariProduk(rs.getString(3));
                    if (r != null && p != null) {
                        r.detail.add(new RekamDetail(rs.getString(1), p, skala0(rs.getBigDecimal(4)),
                                rs.getInt(5), skala0(rs.getBigDecimal(6))));
                    }
                }
            }
        }
    }

    // =====================================================================
    //  NOMOR ID OTOMATIS
    // =====================================================================
    private static int nomorTerbesar(String awalan, List<String> daftarId) {
        int max = 0;
        for (String id : daftarId) {
            if (id != null && id.startsWith(awalan)) {
                try {
                    max = Math.max(max, Integer.parseInt(id.substring(awalan.length())));
                } catch (NumberFormatException e) {
                    // abaikan ID yang formatnya lain
                }
            }
        }
        return max;
    }

    private static String nomor(String awalan, int n) {
        return awalan + String.format("%03d", n);
    }

    public static String idProdukBerikut() {
        List<String> ids = new ArrayList<>();
        for (Produk p : produk) {
            ids.add(p.getIdProduk());
        }
        return nomor("BRG", nomorTerbesar("BRG", ids) + 1);
    }

    public static String idPelangganBerikut() {
        List<String> ids = new ArrayList<>();
        for (Pelanggan p : pelanggan) {
            ids.add(p.getId());
        }
        return nomor("P", nomorTerbesar("P", ids) + 1);
    }

    public static String idSupplierBerikut() {
        List<String> ids = new ArrayList<>();
        for (Supplier s : supplier) {
            ids.add(s.getId());
        }
        return nomor("S", nomorTerbesar("S", ids) + 1);
    }

    public static String idPenjualanBerikut() {
        List<String> ids = new ArrayList<>();
        for (RekamPenjualan r : penjualan) {
            ids.add(r.id);
        }
        return nomor("PJ", nomorTerbesar("PJ", ids) + 1);
    }

    public static String idPembelianBerikut() {
        List<String> ids = new ArrayList<>();
        for (RekamPembelian r : pembelian) {
            ids.add(r.id);
        }
        return nomor("PB", nomorTerbesar("PB", ids) + 1);
    }

    // =====================================================================
    //  PENCARIAN
    // =====================================================================
    public static Produk cariProduk(String id) {
        for (Produk p : produk) {
            if (p.getIdProduk().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public static Pelanggan cariPelanggan(String id) {
        for (Pelanggan p : pelanggan) {
            if (p.getId().equalsIgnoreCase(id)) {
                return p;
            }
        }
        return null;
    }

    public static Supplier cariSupplier(String id) {
        for (Supplier s : supplier) {
            if (s.getId().equalsIgnoreCase(id)) {
                return s;
            }
        }
        return null;
    }

    // =====================================================================
    //  BANTUAN SQL
    // =====================================================================
    /** Menjalankan satu perintah INSERT / UPDATE / DELETE. */
    private static void jalankan(String sql, Object... parameter) {
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < parameter.length; i++) {
                ps.setObject(i + 1, parameter[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(pesanSql(e));
        }
    }

    private static void sisipPelanggan(Connection c, Pelanggan p) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO pelanggan (id_pelanggan, nama, alamat, no_telepon, jenis_pelanggan) VALUES (?, ?, ?, ?, ?)")) {
            ps.setString(1, p.getId());
            ps.setString(2, p.getNama());
            ps.setString(3, p.getAlamat());
            ps.setString(4, p.getNoTelepon());
            ps.setString(5, p.getJenisPelanggan());
            ps.executeUpdate();
        }
    }

    /** Mengubah stok di database secara relatif (jumlah positif = tambah, negatif = kurang). */
    private static void ubahStokDb(Connection c, Map<String, Integer> perubahan) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE produk SET stok = stok + ? WHERE id_produk = ?")) {
            for (Map.Entry<String, Integer> e : perubahan.entrySet()) {
                ps.setInt(1, e.getValue());
                ps.setString(2, e.getKey());
                ps.executeUpdate();
            }
        }
    }

    private static Map<String, Integer> perubahanStok(List<RekamDetail> daftar, int tanda) {
        Map<String, Integer> hasil = new HashMap<>();
        for (RekamDetail d : daftar) {
            hasil.merge(d.produk.getIdProduk(), tanda * d.jumlah, Integer::sum);
        }
        return hasil;
    }

    private static final String SQL_RIWAYAT
            = "INSERT INTO riwayat_transaksi (waktu, jenis_transaksi, id_transaksi, aktivitas, detail_aktivitas, "
            + "jumlah_lama, jumlah_baru, total_lama, total_baru, pengguna, alasan) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static void isiRiwayat(PreparedStatement ps, LocalDateTime waktu, String jenis, String idTransaksi,
            String aktivitas, String detail, Integer lama, Integer baru, BigDecimal totalLama,
            BigDecimal totalBaru, String pengguna, String alasan) throws SQLException {
        ps.setTimestamp(1, Timestamp.valueOf(waktu == null ? LocalDateTime.now() : waktu));
        ps.setString(2, jenis);
        ps.setString(3, idTransaksi);
        ps.setString(4, aktivitas);
        ps.setString(5, detail);
        if (lama == null) {
            ps.setNull(6, Types.INTEGER);
        } else {
            ps.setInt(6, lama);
        }
        if (baru == null) {
            ps.setNull(7, Types.INTEGER);
        } else {
            ps.setInt(7, baru);
        }
        if (totalLama == null) {
            ps.setNull(8, Types.DECIMAL);
        } else {
            ps.setBigDecimal(8, totalLama);
        }
        if (totalBaru == null) {
            ps.setNull(9, Types.DECIMAL);
        } else {
            ps.setBigDecimal(9, totalBaru);
        }
        ps.setString(10, pengguna);
        ps.setString(11, alasan);
    }

    /** Menyimpan semua catatan riwayat milik objek Penjualan / Pembelian. */
    private static void simpanRiwayat(Connection c, List<?> daftar) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_RIWAYAT)) {
            for (Object o : daftar) {
                RiwayatTransaksi r = (RiwayatTransaksi) o;
                isiRiwayat(ps, r.getWaktu(), r.getJenisTransaksi(), r.getIdTransaksi(), r.getAktivitas(),
                        r.getDetailAktivitas(), r.getJumlahLama(), r.getJumlahBaru(), r.getTotalLama(),
                        r.getTotalBaru(), r.getPengguna(), r.getAlasan());
                ps.executeUpdate();
            }
        }
    }

    private static void simpanRiwayatBatal(Connection c, String jenis, String idTransaksi,
            BigDecimal total, String alasan) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SQL_RIWAYAT)) {
            isiRiwayat(ps, LocalDateTime.now(), jenis, idTransaksi, "BATAL TRANSAKSI",
                    "Pembatalan transaksi " + idTransaksi, null, null, total, BigDecimal.ZERO, ADMIN, alasan);
            ps.executeUpdate();
        }
    }

    // =====================================================================
    //  PRODUK
    // =====================================================================
    public static void tambahProduk(Produk p) {
        jalankan("INSERT INTO produk (id_produk, nama_produk, harga_beli, harga_jual, stok) VALUES (?, ?, ?, ?, ?)",
                p.getIdProduk(), p.getNamaProduk(), p.getHargaBeli(), p.getHargaJual(), p.getStok());
        produk.add(p);
    }

    public static void ubahProduk(Produk p, String nama, BigDecimal beli, BigDecimal jual, int stokBaru) {
        jalankan("UPDATE produk SET nama_produk = ?, harga_beli = ?, harga_jual = ?, stok = ? WHERE id_produk = ?",
                nama, beli, jual, stokBaru, p.getIdProduk());
        p.setNamaProduk(nama);
        p.setHargaBeli(beli);
        p.setHargaJual(jual);
        int selisih = stokBaru - p.getStok();   // class Produk tidak punya setStok
        if (selisih > 0) {
            p.tambahStok(selisih);
        } else if (selisih < 0) {
            p.kurangiStok(-selisih);
        }
    }

    public static void hapusProduk(Produk p) {
        jalankan("DELETE FROM produk WHERE id_produk = ?", p.getIdProduk());
        produk.remove(p);
    }

    // =====================================================================
    //  PELANGGAN
    // =====================================================================
    public static void tambahPelanggan(Pelanggan p) {
        jalankan("INSERT INTO pelanggan (id_pelanggan, nama, alamat, no_telepon, jenis_pelanggan) VALUES (?, ?, ?, ?, ?)",
                p.getId(), p.getNama(), p.getAlamat(), p.getNoTelepon(), p.getJenisPelanggan());
        pelanggan.add(p);
    }

    public static void ubahPelanggan(Pelanggan p, String nama, String alamat, String telepon, String jenis) {
        jalankan("UPDATE pelanggan SET nama = ?, alamat = ?, no_telepon = ?, jenis_pelanggan = ? WHERE id_pelanggan = ?",
                nama, alamat, telepon, jenis, p.getId());
        p.setNama(nama);
        p.setAlamat(alamat);
        p.setNoTelepon(telepon);
        p.setJenisPelanggan(jenis);
    }

    public static void hapusPelanggan(Pelanggan p) {
        jalankan("DELETE FROM pelanggan WHERE id_pelanggan = ?", p.getId());
        pelanggan.remove(p);
    }

    // =====================================================================
    //  SUPPLIER
    // =====================================================================
    public static void tambahSupplier(Supplier s) {
        jalankan("INSERT INTO supplier (id_supplier, nama, alamat, no_telepon, nama_perusahaan) VALUES (?, ?, ?, ?, ?)",
                s.getId(), s.getNama(), s.getAlamat(), s.getNoTelepon(), s.getNamaPerusahaan());
        supplier.add(s);
    }

    public static void ubahSupplier(Supplier s, String nama, String alamat, String telepon, String perusahaan) {
        jalankan("UPDATE supplier SET nama = ?, alamat = ?, no_telepon = ?, nama_perusahaan = ? WHERE id_supplier = ?",
                nama, alamat, telepon, perusahaan, s.getId());
        s.setNama(nama);
        s.setAlamat(alamat);
        s.setNoTelepon(telepon);
        s.setNamaPerusahaan(perusahaan);
    }

    public static void hapusSupplier(Supplier s) {
        jalankan("DELETE FROM supplier WHERE id_supplier = ?", s.getId());
        supplier.remove(s);
    }

    // =====================================================================
    //  PENJUALAN
    // =====================================================================
    /** Membuat catatan sementara dari pesanan yang belum disimpan (untuk cetak nota draft). */
    public static RekamPenjualan rekamDraft(Penjualan p, Pelanggan pel) {
        RekamPenjualan r = new RekamPenjualan(p.getIdPenjualan(), LocalDateTime.now(), pel, "Draft", p.hitungTotal());
        for (Object o : p.getDaftarDetail()) {
            DetailPenjualan d = (DetailPenjualan) o;
            r.detail.add(new RekamDetail(d.getIdDetailPenjualan(), d.getProduk(), d.getHargaJual(),
                    d.getJumlah(), d.getSubtotal()));
        }
        return r;
    }

    /**
     * Memfinalisasi pesanan: stok berkurang, lalu semuanya disimpan ke database
     * (penjualan, detail, pelanggan baru, riwayat, stok). Semua berhasil atau semua dibatalkan.
     */
    public static RekamPenjualan finalisasiPenjualan(Penjualan p, Pelanggan pel) {
        RekamPenjualan r = rekamDraft(p, pel);
        LocalDateTime sekarang = LocalDateTime.now();
        boolean pelangganBaru = cariPelanggan(pel.getId()) == null;
        boolean sudahFinal = false;

        try (Connection c = Koneksi.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (pelangganBaru) {
                    sisipPelanggan(c, pel);
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO penjualan (id_penjualan, tanggal, id_pelanggan, total, status) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, r.id);
                    ps.setTimestamp(2, Timestamp.valueOf(sekarang));
                    ps.setString(3, pel.getId());
                    ps.setBigDecimal(4, r.total);
                    ps.setString(5, "Selesai");
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO detail_penjualan (id_detail, id_penjualan, id_produk, harga_jual, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)")) {
                    for (RekamDetail d : r.detail) {
                        ps.setString(1, d.idDetail);
                        ps.setString(2, r.id);
                        ps.setString(3, d.produk.getIdProduk());
                        ps.setBigDecimal(4, d.harga);
                        ps.setInt(5, d.jumlah);
                        ps.setBigDecimal(6, d.subtotal);
                        ps.executeUpdate();
                    }
                }

                p.finalisasiTransaksi(ADMIN);   // stok di memori berkurang di sini
                sudahFinal = true;

                simpanRiwayat(c, p.getDaftarRiwayat());
                ubahStokDb(c, perubahanStok(r.detail, -1));
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (sudahFinal) {
                    for (RekamDetail d : r.detail) {
                        d.produk.tambahStok(d.jumlah);   // kembalikan stok di memori
                    }
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(pesanSql(e));
        }

        if (pelangganBaru) {
            pelanggan.add(pel);
        }
        RekamPenjualan simpan = new RekamPenjualan(r.id, sekarang, pel, "Selesai", r.total);
        simpan.detail.addAll(r.detail);
        penjualan.add(simpan);
        return simpan;
    }

    public static void batalkanPenjualan(RekamPenjualan r, String alasan) {
        if (!"Selesai".equals(r.status)) {
            throw new IllegalStateException("Hanya penjualan berstatus Selesai yang bisa dibatalkan.");
        }
        try (Connection c = Koneksi.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE penjualan SET status = ? WHERE id_penjualan = ?")) {
                    ps.setString(1, "Dibatalkan");
                    ps.setString(2, r.id);
                    ps.executeUpdate();
                }
                simpanRiwayatBatal(c, "PENJUALAN", r.id, r.total, alasan);
                ubahStokDb(c, perubahanStok(r.detail, 1));   // stok dikembalikan
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(pesanSql(e));
        }
        for (RekamDetail d : r.detail) {
            d.produk.tambahStok(d.jumlah);
        }
        r.status = "Dibatalkan";
    }

    // =====================================================================
    //  PEMBELIAN
    // =====================================================================
    public static RekamPembelian rekamDraft(Pembelian p, Supplier sup) {
        RekamPembelian r = new RekamPembelian(p.getIdPembelian(), LocalDateTime.now(), sup, "Draft", p.hitungTotal());
        for (Object o : p.getDaftarDetail()) {
            DetailPembelian d = (DetailPembelian) o;
            r.detail.add(new RekamDetail(d.getIdDetailPembelian(), d.getProduk(), d.getHargaBeli(),
                    d.getJumlah(), d.getSubtotal()));
        }
        return r;
    }

    /** Memfinalisasi pembelian: stok bertambah, lalu semuanya disimpan ke database. */
    public static RekamPembelian finalisasiPembelian(Pembelian p, Supplier sup) {
        RekamPembelian r = rekamDraft(p, sup);
        LocalDateTime sekarang = LocalDateTime.now();
        boolean sudahFinal = false;

        try (Connection c = Koneksi.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO pembelian (id_pembelian, tanggal, id_supplier, total, status) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setString(1, r.id);
                    ps.setTimestamp(2, Timestamp.valueOf(sekarang));
                    ps.setString(3, sup.getId());
                    ps.setBigDecimal(4, r.total);
                    ps.setString(5, "Selesai");
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO detail_pembelian (id_detail, id_pembelian, id_produk, harga_beli, jumlah, subtotal) VALUES (?, ?, ?, ?, ?, ?)")) {
                    for (RekamDetail d : r.detail) {
                        ps.setString(1, d.idDetail);
                        ps.setString(2, r.id);
                        ps.setString(3, d.produk.getIdProduk());
                        ps.setBigDecimal(4, d.harga);
                        ps.setInt(5, d.jumlah);
                        ps.setBigDecimal(6, d.subtotal);
                        ps.executeUpdate();
                    }
                }

                p.finalisasiTransaksi(ADMIN);   // stok di memori bertambah di sini
                sudahFinal = true;

                simpanRiwayat(c, p.getDaftarRiwayat());
                ubahStokDb(c, perubahanStok(r.detail, 1));
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (sudahFinal) {
                    for (RekamDetail d : r.detail) {
                        d.produk.kurangiStok(d.jumlah);   // kembalikan stok di memori
                    }
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(pesanSql(e));
        }

        RekamPembelian simpan = new RekamPembelian(r.id, sekarang, sup, "Selesai", r.total);
        simpan.detail.addAll(r.detail);
        pembelian.add(simpan);
        return simpan;
    }

    public static void batalkanPembelian(RekamPembelian r, String alasan) {
        if (!"Selesai".equals(r.status)) {
            throw new IllegalStateException("Hanya pembelian berstatus Selesai yang bisa dibatalkan.");
        }
        // stok harus cukup untuk dikurangi lagi
        Map<String, Integer> perlu = perubahanStok(r.detail, 1);
        for (RekamDetail d : r.detail) {
            int butuh = perlu.get(d.produk.getIdProduk());
            if (d.produk.getStok() < butuh) {
                throw new IllegalStateException("Stok " + d.produk.getNamaProduk()
                        + " tidak cukup untuk membatalkan pembelian ini (stok " + d.produk.getStok()
                        + ", perlu " + butuh + ").");
            }
        }
        try (Connection c = Koneksi.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE pembelian SET status = ? WHERE id_pembelian = ?")) {
                    ps.setString(1, "Dibatalkan");
                    ps.setString(2, r.id);
                    ps.executeUpdate();
                }
                simpanRiwayatBatal(c, "PEMBELIAN", r.id, r.total, alasan);
                ubahStokDb(c, perubahanStok(r.detail, -1));   // stok dikurangi lagi
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(pesanSql(e));
        }
        for (RekamDetail d : r.detail) {
            d.produk.kurangiStok(d.jumlah);
        }
        r.status = "Dibatalkan";
    }

    // =====================================================================
    //  RIWAYAT (dibaca langsung dari database)
    // =====================================================================
    /**
     * Mengambil riwayat. Setiap baris berisi 9 teks:
     * waktu, jenis, id transaksi, aktivitas, detail, jumlah lama, jumlah baru, pengguna, alasan.
     * jenis: "Semua", "Penjualan", atau "Pembelian".
     */
    public static List<String[]> ambilRiwayat(String jenis, String kataId) {
        StringBuilder sql = new StringBuilder(
                "SELECT waktu, jenis_transaksi, id_transaksi, aktivitas, detail_aktivitas, jumlah_lama, "
                + "jumlah_baru, pengguna, alasan FROM riwayat_transaksi WHERE 1 = 1");
        List<Object> param = new ArrayList<>();
        if (jenis != null && !jenis.equals("Semua")) {
            sql.append(" AND UPPER(jenis_transaksi) = ?");
            param.add(jenis.toUpperCase());
        }
        if (kataId != null && !kataId.trim().isEmpty()) {
            sql.append(" AND id_transaksi LIKE ?");
            param.add("%" + kataId.trim() + "%");
        }
        sql.append(" ORDER BY id_riwayat");

        List<String[]> hasil = new ArrayList<>();
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < param.size(); i++) {
                ps.setObject(i + 1, param.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp(1);
                    hasil.add(new String[]{
                        t == null ? "-" : waktu(t.toLocalDateTime()),
                        teks(rs.getString(2)), teks(rs.getString(3)), teks(rs.getString(4)),
                        teks(rs.getString(5)), teks(rs.getObject(6)), teks(rs.getObject(7)),
                        teks(rs.getString(8)), teks(rs.getString(9))});
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(pesanSql(e));
        }
        return hasil;
    }

    private static String teks(Object o) {
        return o == null ? "-" : String.valueOf(o);
    }

    // =====================================================================
    //  FORMAT TAMPILAN
    // =====================================================================
    private static final DecimalFormat FORMAT_ANGKA
            = new DecimalFormat("#,##0", new DecimalFormatSymbols(Locale.forLanguageTag("id-ID")));

    /** 45000 menjadi "45.000" */
    public static String angka(BigDecimal b) {
        return b == null ? "0" : FORMAT_ANGKA.format(b);
    }

    /** 45000 menjadi "Rp 45.000" */
    public static String rp(BigDecimal b) {
        return "Rp " + angka(b);
    }

    /** 45000.00 menjadi "45000" (untuk mengisi kotak isian) */
    public static String plain(BigDecimal b) {
        return skala0(b).toPlainString();
    }

    /** "45.000" atau "Rp 45.000" atau "45000" menjadi BigDecimal 45000 */
    public static BigDecimal parseAngka(String teks) {
        String bersih = teks == null ? "" : teks.replaceAll("[^0-9]", "");
        if (bersih.isEmpty()) {
            throw new NumberFormatException("Angka kosong");
        }
        return new BigDecimal(bersih);
    }

    public static String tanggal(LocalDateTime t) {
        return t == null ? "-" : t.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public static String waktu(LocalDateTime t) {
        return t == null ? "-" : t.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }

    public static String labelProduk(Produk p) {
        return p.getIdProduk() + " - " + p.getNamaProduk();
    }

    public static String labelSupplier(Supplier s) {
        return s.getId() + " - " + s.getNamaPerusahaan();
    }

    // =====================================================================
    //  BANTUAN TAMPILAN
    // =====================================================================
    /** Membuat model tabel yang tidak bisa diketik langsung. */
    public static DefaultTableModel buatModel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            @Override
            public boolean isCellEditable(int baris, int kolom) {
                return false;
            }
        };
    }

    public static void pesan(Component induk, String isi) {
        JOptionPane.showMessageDialog(induk, isi, "KhasKita", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void galat(Component induk, String isi) {
        JOptionPane.showMessageDialog(induk, isi, "KhasKita", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean tanya(Component induk, String isi) {
        return JOptionPane.showConfirmDialog(induk, isi, "Konfirmasi",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String pesanGalat(RuntimeException e) {
        return e.getMessage() == null ? e.toString() : e.getMessage();
    }

    public static void tampilTeks(Component induk, String judul, String isi) {
        JTextArea area = new JTextArea(isi);
        area.setEditable(false);
        area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
        JScrollPane gulung = new JScrollPane(area);
        gulung.setPreferredSize(new java.awt.Dimension(430, 360));
        JOptionPane.showMessageDialog(induk, gulung, judul, JOptionPane.PLAIN_MESSAGE);
    }

    // =====================================================================
    //  NOTA DAN BUKTI
    // =====================================================================
    private static final String GARIS = "------------------------------------------\n";

    public static String notaPenjualan(RekamPenjualan r) {
        StringBuilder sb = new StringBuilder();
        sb.append("               KHASKITA\n");
        sb.append("          Oleh-oleh Khas Riau\n");
        sb.append(GARIS);
        sb.append("No. Nota  : ").append(r.id).append("\n");
        sb.append("Tanggal   : ").append(tanggal(r.tanggal)).append("\n");
        sb.append("Pelanggan : ").append(r.pelanggan.getNama()).append(" (").append(r.pelanggan.getId()).append(")\n");
        sb.append("Kasir     : ").append(ADMIN).append("\n");
        sb.append(GARIS);
        for (RekamDetail d : r.detail) {
            sb.append(d.produk.getNamaProduk()).append("\n");
            sb.append("   ").append(d.jumlah).append(" x ").append(rp(d.harga))
                    .append(" = ").append(rp(d.subtotal)).append("\n");
        }
        sb.append(GARIS);
        sb.append("TOTAL  : ").append(rp(r.total)).append("\n");
        sb.append("STATUS : ").append(r.status).append("\n");
        return sb.toString();
    }

    public static String buktiPembelian(RekamPembelian r) {
        StringBuilder sb = new StringBuilder();
        sb.append("               KHASKITA\n");
        sb.append("          BUKTI PEMBELIAN\n");
        sb.append(GARIS);
        sb.append("No. Bukti : ").append(r.id).append("\n");
        sb.append("Tanggal   : ").append(tanggal(r.tanggal)).append("\n");
        sb.append("Supplier  : ").append(r.supplier.getNama()).append(" (").append(r.supplier.getId()).append(")\n");
        sb.append("Perusahaan: ").append(r.supplier.getNamaPerusahaan()).append("\n");
        sb.append("Petugas   : ").append(ADMIN).append("\n");
        sb.append(GARIS);
        for (RekamDetail d : r.detail) {
            sb.append(d.produk.getNamaProduk()).append("\n");
            sb.append("   ").append(d.jumlah).append(" x ").append(rp(d.harga))
                    .append(" = ").append(rp(d.subtotal)).append("\n");
        }
        sb.append(GARIS);
        sb.append("TOTAL  : ").append(rp(r.total)).append("\n");
        sb.append("STATUS : ").append(r.status).append("\n");
        return sb.toString();
    }

    // =====================================================================
    //  DIJALANKAN SEKALI SAAT APLIKASI DIBUKA: BACA DATA DARI DATABASE
    // =====================================================================
    static {
        try {
            muatSemua();
        } catch (SQLException | RuntimeException e) {
            galatDb = e.getMessage() == null ? e.toString() : e.getMessage();
        }
    }
}