import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Database {
    private static final String SQL_FILE = "produk_db.sql";
    private static String currentUsername = null;
    private static String currentUserRole = "user";

    private static String getPasswordFromConfig() {
        String pass = System.getenv("MYSQL_PASSWORD");
        if (pass == null) {
            pass = getConfigValue("DB_PASSWORD", "Alfi_syahrin54789");
        }
        return pass;
    }

    private static String getConfigValue(String key, String defaultValue) {
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String fileValue = loadConfigValue(key);
        return (fileValue != null && !fileValue.isBlank()) ? fileValue : defaultValue;
    }

    private static String loadConfigValue(String key) {
        for (Path configPath : new Path[] {
                Path.of("db.properties"),
                Path.of("PROJECT APLIKASI JAVA/db.properties"),
                Path.of("src/db.properties")
        }) {
            if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    Properties props = new Properties();
                    props.load(input);
                    String value = props.getProperty(key);
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                } catch (IOException e) {
                    System.err.println("Gagal membaca file konfigurasi " + configPath + ": " + e.getMessage());
                }
            }
        }
        return null;
    }

    private static String getDbHost() {
        return getConfigValue("DB_HOST", "localhost");
    }

    private static String getDbPort() {
        return getConfigValue("DB_PORT", "3306");
    }

    private static String getDbName() {
        return getConfigValue("DB_NAME", "produk_db");
    }

    private static String getDbUser() {
        return getConfigValue("DB_USER", "root");
    }

    private static String getDbPassword() {
        return getPasswordFromConfig();
    }

    public static boolean isAdminFeaturesEnabled() {
        return Boolean.parseBoolean(getConfigValue("ADMIN_FEATURES_ENABLED", "true"));
    }

    public static boolean canAccessAdminFeatures() {
        return isAdminFeaturesEnabled() && "admin".equalsIgnoreCase(currentUserRole);
    }

    private static String getServerUrl() {
        return "jdbc:mysql://" + getDbHost() + ":" + getDbPort() + "/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    private static String getDbUrl() {
        return "jdbc:mysql://" + getDbHost() + ":" + getDbPort() + "/" + getDbName() + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC MySQL tidak ditemukan. Pastikan lib/mysql-connector-j-9.6.0.jar ada di classpath.", e);
        }
    }

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(getServerUrl(), getDbUser(), getDbPassword());
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + getDbName());
            st.executeUpdate("USE " + getDbName());
            executeSqlFile(st, SQL_FILE);

            st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(100) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "role VARCHAR(20) NOT NULL DEFAULT 'user', "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "deleted_date TIMESTAMP NULL DEFAULT NULL"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(100) NOT NULL, "
                    + "description VARCHAR(255) NOT NULL, "
                    + "type VARCHAR(20) NOT NULL, "
                    + "amount BIGINT NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "deleted_date TIMESTAMP NULL DEFAULT NULL"
                    + ")");
            ensureCreatedAtColumn(conn);
            ensureDeletedDateColumn(conn);
            ensureRoleColumn(conn);

            if (!userExists(conn, "admin")) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, "123");
                    ps.setString(3, "admin");
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal inisialisasi database: " + e.getMessage(), e);
        }
    }

    private static void executeSqlFile(Statement st, String sqlFile) {
        Path path = Path.of(sqlFile);
        if (Files.notExists(path)) {
            return;
        }

        try {
            String sql = Files.readString(path);
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty() || trimmed.toUpperCase().startsWith("SELECT")) {
                    continue;
                }
                st.execute(trimmed);
            }
        } catch (IOException e) {
            throw new RuntimeException("Gagal membaca file SQL: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengeksekusi file SQL: " + e.getMessage(), e);
        }
    }

    private static boolean userExists(Connection conn, String username) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void ensureCreatedAtColumn(Connection conn) {
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, "transactions", "created_at");
            if (!rs.next()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "ALTER TABLE transactions ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP")) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memastikan kolom created_at: " + e.getMessage());
        }
    }

    private static void ensureDeletedDateColumn(Connection conn) {
        ensureColumn(conn, "users", "deleted_date", "TIMESTAMP NULL DEFAULT NULL");
        ensureColumn(conn, "transactions", "deleted_date", "TIMESTAMP NULL DEFAULT NULL");
    }

    private static void ensureRoleColumn(Connection conn) {
        ensureColumn(conn, "users", "role", "VARCHAR(20) NOT NULL DEFAULT 'user'");
    }

    private static void ensureColumn(Connection conn, String tableName, String columnName, String columnDefinition) {
        try {
            ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName);
            if (!rs.next()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Gagal memastikan kolom " + columnName + " pada " + tableName + ": " + e.getMessage());
        }
    }

    public static java.util.List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, username, role FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String role = rs.getString("role");
                users.add(new User(id, username, role));
            }
        } catch (SQLException e) {
            System.err.println("Error membaca daftar pengguna: " + e.getMessage());
        }
        return users;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getDbUrl(), getDbUser(), getDbPassword());
    }

    public static boolean validateUser(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT password, role FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String actualPassword = rs.getString("password");
                    if (actualPassword.equals(password)) {
                        setCurrentUsername(username);
                        setCurrentUserRole(rs.getString("role"));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validasi user: " + e.getMessage());
        }
        return false;
    }

    public static boolean registerUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }

        try (Connection conn = getConnection()) {
            if (userExists(conn, username)) {
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO users (username, password, role) VALUES (?, ?, 'user')") ) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error registrasi user: " + e.getMessage());
            return false;
        }
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }

    public static void setCurrentUserRole(String role) {
        currentUserRole = (role == null || role.isBlank()) ? "user" : role;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static boolean isCurrentUserAdmin() {
        return canAccessAdminFeatures();
    }

    public static boolean addTransaction(String description, String type, long amount) {
        return addTransactionForUser(currentUsername, description, type, amount);
    }

    public static boolean addTransactionForUser(String targetUsername, String description, String type, long amount) {
        if (targetUsername == null || targetUsername.isBlank()) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO transactions (username, description, type, amount) VALUES (?, ?, ?, ?)") ) {
            ps.setString(1, targetUsername);
            ps.setString(2, description);
            ps.setString(3, type);
            ps.setLong(4, amount);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error menambahkan transaksi: " + e.getMessage());
            return false;
        }
    }

    public static List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        if (currentUsername == null) {
            return transactions;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, description, type, amount, created_at FROM transactions WHERE username = ? AND deleted_date IS NULL ORDER BY created_at ASC")) {
            ps.setString(1, currentUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    String type = rs.getString("type");
                    long amount = rs.getLong("amount");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    transactions.add(new Transaction(id, description, type, amount, createdAt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error membaca transaksi: " + e.getMessage());
        }
        return transactions;
    }

    public static List<Transaction> getTransactionsBetween(Timestamp start, Timestamp end) {
        List<Transaction> transactions = new ArrayList<>();
        if (currentUsername == null) {
            return transactions;
        }
        String sql = "SELECT id, description, type, amount, created_at FROM transactions WHERE username = ? AND deleted_date IS NULL AND created_at >= ? AND created_at < ? ORDER BY created_at ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, currentUsername);
            ps.setTimestamp(2, start);
            ps.setTimestamp(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    String type = rs.getString("type");
                    long amount = rs.getLong("amount");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    transactions.add(new Transaction(id, description, type, amount, createdAt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error membaca transaksi antara tanggal: " + e.getMessage());
        }
        return transactions;
    }

    public static List<Transaction> getTransactionsForUser(String username) {
        List<Transaction> transactions = new ArrayList<>();
        if (username == null || username.isBlank()) {
            return transactions;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, description, type, amount, created_at FROM transactions WHERE username = ? AND deleted_date IS NULL ORDER BY created_at ASC")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String description = rs.getString("description");
                    String type = rs.getString("type");
                    long amount = rs.getLong("amount");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    transactions.add(new Transaction(id, description, type, amount, createdAt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error membaca transaksi pengguna: " + e.getMessage());
        }
        return transactions;
    }

    public static boolean updateTransaction(int transactionId, String description, String type, long amount) {
        if (transactionId <= 0) {
            return false;
        }
        try (Connection conn = getConnection()) {
            String ownerUsername = getTransactionOwner(conn, transactionId);
            if (ownerUsername == null) {
                return false;
            }
            if (!isCurrentUserAdmin() && !ownerUsername.equals(currentUsername)) {
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE transactions SET description = ?, type = ?, amount = ? WHERE id = ?")) {
                ps.setString(1, description);
                ps.setString(2, type);
                ps.setLong(3, amount);
                ps.setInt(4, transactionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error memperbarui transaksi: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteTransaction(int transactionId) {
        if (transactionId <= 0) {
            return false;
        }
        try (Connection conn = getConnection()) {
            String ownerUsername = getTransactionOwner(conn, transactionId);
            if (ownerUsername == null) {
                return false;
            }
            if (!isCurrentUserAdmin() && !ownerUsername.equals(currentUsername)) {
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE transactions SET deleted_date = CURRENT_TIMESTAMP WHERE id = ? AND deleted_date IS NULL")) {
                ps.setInt(1, transactionId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error menghapus transaksi: " + e.getMessage());
            return false;
        }
    }

    private static String getTransactionOwner(Connection conn, int transactionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT username FROM transactions WHERE id = ?")) {
            ps.setInt(1, transactionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("username") : null;
            }
        }
    }

    public static Totals getTotals() {
        Totals totals = new Totals(0, 0, 0);
        if (currentUsername == null) {
            return totals;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT "
                             + "COALESCE(SUM(CASE WHEN type = 'Pemasukan' THEN amount ELSE 0 END), 0) AS totalPemasukan, "
                             + "COALESCE(SUM(CASE WHEN type = 'Pengeluaran' THEN amount ELSE 0 END), 0) AS totalPengeluaran "
                             + "FROM transactions WHERE username = ? AND deleted_date IS NULL")) {
            ps.setString(1, currentUsername);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long pemasukan = rs.getLong("totalPemasukan");
                    long pengeluaran = rs.getLong("totalPengeluaran");
                    long totalSaldo = pemasukan - pengeluaran;
                    totals = new Totals(totalSaldo, pemasukan, pengeluaran);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error menghitung total: " + e.getMessage());
        }
        return totals;
    }

    public static boolean resetTransactions() {
        if (currentUsername == null) {
            return false;
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE username = ?")) {
            ps.setString(1, currentUsername);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error mereset transaksi: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteAllUsersExceptAdmin() {
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transactions WHERE username <> ?")) {
                ps.setString(1, "admin");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE username <> ?")) {
                ps.setString(1, "admin");
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error menghapus semua user: " + e.getMessage());
            return false;
        }
    }

    public static class Transaction {
        public final int id;
        public final String description;
        public final String type;
        public final long amount;
        public final Timestamp createdAt;

        public Transaction(int id, String description, String type, long amount, Timestamp createdAt) {
            this.id = id;
            this.description = description;
            this.type = type;
            this.amount = amount;
            this.createdAt = createdAt;
        }
    }

    public static class User {
        public final int id;
        public final String username;
        public final String role;

        public User(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }
    }

    public static class Totals {
        public final long totalSaldo;
        public final long totalPemasukan;
        public final long totalPengeluaran;

        public Totals(long totalSaldo, long totalPemasukan, long totalPengeluaran) {
            this.totalSaldo = totalSaldo;
            this.totalPemasukan = totalPemasukan;
            this.totalPengeluaran = totalPengeluaran;
        }
    }
}
