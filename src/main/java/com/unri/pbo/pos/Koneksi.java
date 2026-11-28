package com.unri.pbo.pos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Pengatur koneksi ke database MySQL (XAMPP).
 * Kalau password MySQL Anda tidak kosong, ubah PASSWORD di bawah.
 */
public class Koneksi {

    private static final String HOST = "localhost";
    private static final String PORT = "8111";
    private static final String DATABASE = "aplikasi_pos";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
            + "&serverTimezone=Asia/Jakarta";

    private Koneksi() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Nama lain, kalau kode lama Anda memakai getKoneksi(). */
    public static Connection getKoneksi() throws SQLException {
        return getConnection();
    }

    /**
     * Untuk mengecek koneksi: klik kanan file ini > Run File.
     * Di jendela Output akan muncul KONEKSI BERHASIL atau KONEKSI GAGAL.
     */
    public static void main(String[] args) {
        try (Connection c = getConnection()) {
            System.out.println("KONEKSI BERHASIL ke database: " + c.getCatalog());
        } catch (SQLException e) {
            System.out.println("KONEKSI GAGAL: " + e.getMessage());
        }
    }
}