# Aplikasi Keuangan

Aplikasi ini digunakan untuk mencatat pemasukan, pengeluaran, dan melihat ringkasan keuangan pribadi. Aplikasi dibuat dengan Java Swing dan tersambung ke database MySQL.

## Fitur utama

- Login dan registrasi akun
- Catat pemasukan dan pengeluaran
- Lihat saldo, dashboard, dan riwayat transaksi
- Export data transaksi ke file CSV jika fitur tersedia
- Akses admin hanya untuk akun yang memiliki role admin

## Syarat awal

- Java JDK 17 atau lebih baru
- Server MySQL yang aktif
- File JDBC tersedia di folder lib/mysql-connector-j-9.6.0.jar

## Cara menjalankan

1. Pastikan MySQL sudah berjalan.
2. Atur koneksi database di file PROJECT APLIKASI JAVA/db.properties.
3. Jalankan aplikasi dengan perintah berikut:

```powershell
.\run.bat
```

## Login

Gunakan akun yang sudah didaftarkan. Jika belum punya akun, pilih menu registrasi.

## Catatan penting

- Fitur manajemen pengguna dan tombol admin hanya tersedia untuk akun admin.
- Untuk mengelola akun pengguna lain, gunakan akun admin.

## Jika aplikasi tidak bisa terhubung ke database

Periksa kembali:

- password MySQL
- pengaturan DB_HOST, DB_PORT, DB_NAME, DB_USER, dan DB_PASSWORD
- file produk_db.sql serta file Database.java
