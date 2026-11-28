# KhasKita POS - cara menjalankan

1. Nyalakan **MySQL** di XAMPP (Start).
2. Buka phpMyAdmin > tab **Import** > pilih `aplikasi_pos.sql` > Go.
   (Atau: `mysql -u root < aplikasi_pos.sql`). Ini membuat database `aplikasi_pos` + data contoh.
   Kalau password MySQL tidak kosong, ubah `PASSWORD` di `Koneksi.java`.
3. Gunakan **JDK 26** (jar `lib/ProjectPOS.jar` dikompilasi dengan Java 26).
4. NetBeans: klik kanan proyek > **Run** (main class: `com.unri.pbo.pos.POS`).
   Maven: `mvn compile exec:java`  atau  `mvn package` lalu `java -cp target/POS-1.0-SNAPSHOT.jar:lib/ProjectPOS.jar:<mysql-connector.jar> com.unri.pbo.pos.POS`
