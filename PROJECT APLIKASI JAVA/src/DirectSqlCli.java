import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DirectSqlCli {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/produk_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = System.getenv("MYSQL_PASSWORD");
        if (password == null) {
            password = "Alfi_syahrin54789";
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("Connected: " + !conn.isClosed());
                try (PreparedStatement psCreate = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS transactions (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(100) NOT NULL, description VARCHAR(255) NOT NULL, type VARCHAR(20) NOT NULL, amount BIGINT NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")) {
                    psCreate.execute();
                }
                try (PreparedStatement psInsert = conn.prepareStatement(
                        "INSERT INTO transactions (username, description, type, amount) VALUES (?, ?, ?, ?)") ) {
                    psInsert.setString(1, "admin");
                    psInsert.setString(2, "Direct test");
                    psInsert.setString(3, "Pemasukan");
                    psInsert.setLong(4, 12345);
                    int count = psInsert.executeUpdate();
                    System.out.println("Inserted rows=" + count);
                }
                try (PreparedStatement psCount = conn.prepareStatement("SELECT COUNT(*) AS c FROM transactions")) {
                    try (ResultSet rs = psCount.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Count after insert=" + rs.getInt("c"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
