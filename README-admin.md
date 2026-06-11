# Aplikasi Keuangan - Panduan Admin

Panduan ini khusus untuk akun admin.

## Fitur admin yang tersedia

Akun admin dapat:

- melihat daftar pengguna
- membuka transaksi pengguna lain
- menghapus semua akun non-admin melalui tombol Hapus Semua User
- mengelola data pengguna secara terbatas dari panel admin

## Pengaturan khusus

Admin features dapat diaktifkan atau dinonaktifkan melalui file konfigurasi:

```text
PROJECT APLIKASI JAVA/db.properties
```

Tambahkan atau ubah nilai berikut:

```properties
ADMIN_FEATURES_ENABLED=true
```

- `true` = fitur admin aktif dan terlihat oleh akun admin
- `false` = fitur admin disembunyikan untuk semua akun

## Cara masuk sebagai admin

Gunakan akun default berikut:

- Username: admin
- Password: 123

## Langkah admin

1. Jalankan aplikasi.
2. Login menggunakan akun admin.
3. Buka dashboard.
4. Gunakan tombol Lihat Pengguna untuk mengakses panel manajemen pengguna.

## Tindakan hati-hati

- Tombol Hapus Semua User akan menghapus semua akun non-admin.
- Akun admin tidak ikut terhapus.
- Pastikan Anda yakin sebelum menjalankan tindakan penghapusan massal.

## Catatan teknis

Jika Anda ingin mengubah role pengguna menjadi admin, pastikan role pada tabel users di database bernilai `admin`.
