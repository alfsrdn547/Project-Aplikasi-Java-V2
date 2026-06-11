import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportTransactionsCli {
    public static void main(String[] args) {
        // Gunakan user admin untuk pengujian
        Database.setCurrentUsername("admin");

        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(end.atStartOfDay());

        List<Database.Transaction> list = Database.getTransactionsBetween(startTs, endTs);

        File outDir = new File("exports");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        File out = new File(outDir, "riwayat_test_bulanan.csv");

        if (list.isEmpty()) {
            System.out.println("Tidak ada transaksi untuk periode ini. Tidak dibuat file CSV.");
            System.exit(0);
        }

        try {
            writeCsv(list, out);
            System.out.println("CSV dibuat: " + out.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Gagal menulis CSV: " + e.getMessage());
            System.exit(2);
        }
    }

    private static void writeCsv(List<Database.Transaction> list, File file) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("Tanggal,Keterangan,Jenis,Jumlah\n");
            for (Database.Transaction tx : list) {
                String tanggal = "";
                if (tx.createdAt != null) {
                    LocalDateTime ldt = tx.createdAt.toLocalDateTime();
                    tanggal = ldt.format(fmt);
                }
                String jumlah = (tx.type.equals("Pengeluaran") ? "- " : "+ ") + formatRupiah(tx.amount);
                String ket = tx.description.replace("\"", "\"\"");
                fw.write(String.format("%s,%s,%s,%s\n", tanggal, ket, tx.type, jumlah));
            }
        }
    }

    private static String formatRupiah(long nominal) {
        return "Rp " + String.format("%,d", nominal).replace(",", ".");
    }
}
