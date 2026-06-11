import java.sql.Connection;
import java.sql.DriverManager;

public class TestKoneksi {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "Alfi_syahrin54789";

        try {
            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Mantap! Koneksi MySQL Berhasil.");
            conn.close();
        } catch (Exception e) {
            System.out.println("Waduh, Gagal: " + e.getMessage());
            System.out.println("\nKonfigurasi MySQL:");
            System.out.println("- User: root");
            System.out.println("- Password: (kosong)");
            System.out.println("\nPeriksa di Database.java jika perlu mengubah kredensial.");
        }
    }
}