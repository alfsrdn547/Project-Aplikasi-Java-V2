import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QueryTransactionsCli {
    public static void main(String[] args) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, username, description, type, amount, created_at FROM transactions ORDER BY created_at DESC")) {
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("Rows in transactions:");
                while (rs.next()) {
                    System.out.printf("id=%d username=%s desc=%s type=%s amount=%d created_at=%s\n",
                            rs.getInt("id"), rs.getString("username"), rs.getString("description"), rs.getString("type"), rs.getLong("amount"), rs.getTimestamp("created_at"));
                }
            }
        } catch (Exception e) {
            System.err.println("Query error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
