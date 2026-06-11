public class SeedTransactionsCli {
    public static void main(String[] args) {
        try {
            Database.initDatabase();
        } catch (RuntimeException ex) {
            System.err.println("Gagal inisialisasi database: " + ex.getMessage());
            System.exit(1);
        }

        Database.setCurrentUsername("admin");

        boolean ok1 = Database.addTransaction("Saldo awal", "Pemasukan", 1000000);
        boolean ok2 = Database.addTransaction("Beli alat tulis", "Pengeluaran", 50000);
        boolean ok3 = Database.addTransaction("Penjualan kecil", "Pemasukan", 200000);

        System.out.println("Seed selesai: " + ok1 + ", " + ok2 + ", " + ok3);
    }
}
