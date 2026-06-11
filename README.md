# Aplikasi Keuangan

Panduan penggunaan untuk pengguna biasa.

## Apa yang bisa Anda lakukan

- Login dan registrasi akun
- Mencatat pemasukan dan pengeluaran
- Melihat saldo, dashboard, dan riwayat transaksi
- Mengekspor transaksi ke file CSV jika fitur tersedia
- Menggunakan aplikasi secara mandiri tanpa akses ke panel admin

## Prasyarat

- Java JDK 17 atau lebih baru
- MySQL Server yang bisa diakses aplikasi
- File library JDBC tersedia di lib/mysql-connector-j-9.6.0.jar

## Persiapan

1. Pastikan MySQL berjalan.
2. Sesuaikan konfigurasi database di PROJECT APLIKASI JAVA/db.properties.
3. Jalankan aplikasi dengan:

```powershell
.\run.bat
```

## Login

Gunakan akun yang telah Anda daftarkan. Jika belum punya akun, pilih menu registrasi.

## Catatan penting

- Fitur manajemen pengguna dan tombol admin tidak akan muncul untuk akun user biasa.
- Jika Anda ingin mengelola akun pengguna lain, gunakan akun admin yang telah disediakan.

## Bantuan

Jika aplikasi gagal terhubung ke database, cek kembali:

- password MySQL
- konfigurasi DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
- file produk_db.sql dan Database.java
