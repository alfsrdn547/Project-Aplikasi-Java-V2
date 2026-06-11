import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DebugDbCli {
    public static void main(String[] args) {
        try {
            System.out.println("Using DB URL: jdbc:mysql://localhost:3306/produk_db");
            try (Connection conn = Database.getConnection()) {
                System.out.println("Connection ok: " + !conn.isClosed());
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS c FROM transactions")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("transactions count=" + rs.getInt("c"));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Debug failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
