# Perubahan tata letak

- MainFrame: JScrollPane dihapus; halaman langsung mengisi jendela.
- Setiap halaman: layout tingkat halaman diganti BorderLayout (sidebar kiri, konten tengah).
- gui/Tata.java: tinggi minimum komponen teks/tombol/combo diturunkan ke tinggi preferensinya; panel tabel boleh menyusut sampai 120 px.

Hasil uji headless (Nimbus), tanpa tombol yang terpotong:
- 1920x1010 (layar 1080p dikurangi taskbar dan title bar): semua 7 halaman muat.
- 1366x698 (layar 768p): Produk, Pelanggan, Supplier, Riwayat muat. Dashboard, Pembelian, Penjualan belum.

Catatan: di Penjualan, tombol "Batalkan Transaksi" sedikit (sekitar 6 px) menumpang label Total. Perlu dicek visual di NetBeans.
