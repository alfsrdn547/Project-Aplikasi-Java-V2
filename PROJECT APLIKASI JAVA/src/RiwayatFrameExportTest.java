import java.io.File;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RiwayatFrameExportTest {
    public static void main(String[] args) {
        try {
            Database.setCurrentUsername("admin");
            LocalDate now = LocalDate.now();
            LocalDate start = now.withDayOfMonth(1);
            LocalDate end = start.plusMonths(1);
            Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
            Timestamp endTs = Timestamp.valueOf(end.atStartOfDay());
            List<Database.Transaction> list = Database.getTransactionsBetween(startTs, endTs);
            if (list.isEmpty()) {
                System.out.println("Tidak ada transaksi pada periode saat ini untuk diuji.");
                return;
            }

            RiwayatFrame frame = new RiwayatFrame();
            frame.setVisible(false);

            File outDir = new File("exports");
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            File out = new File(outDir, "riwayat_test_gui_monthly.csv");

            Method method = RiwayatFrame.class.getDeclaredMethod("exportTransactionsToCsv", List.class, File.class);
            method.setAccessible(true);
            method.invoke(frame, list, out);

            System.out.println("GUI export test file created: " + out.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
