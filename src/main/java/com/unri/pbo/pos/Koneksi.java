package com.unri.pbo.pos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/aplikasi_pos";
                String user = "root";
                String password = ""; 

                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Koneksi database berhasil!");
            } catch (SQLException e) {
                System.out.println("Koneksi database gagal: " + e.getMessage());
            }
        }
        return connection;
    }
    
}
