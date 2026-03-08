import java.sql.*;
import java.util.*;

public class VoteDOB {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/graph?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "nikhil@68";

    // Load MySQL Driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL Driver not found");
        }
    }

    // 🔗 DB CONNECTION
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }

    // 🔐 LOGIN AUTHENTICATION
    public static boolean authenticate(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username=? AND password=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // Utility: sanitize column names (replace spaces & special chars with underscore)
    private static String sanitizeColumn(String col) {
        col = col.trim().replaceAll("\\s+", "_"); // spaces → _
        col = col.replaceAll("[^a-zA-Z0-9_]", ""); // remove special chars
        if(col.isEmpty()) col = "col"; // fallback
        return col;
    }

    // 📋 CREATE TABLE (DYNAMIC COLUMNS)
    public static void createTable(String tableName, List<String> columns)
            throws SQLException {

        if (columns.isEmpty()) throw new SQLException("No columns provided");

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        sql.append("id INT AUTO_INCREMENT PRIMARY KEY,");

        for (String col : columns) {
            sql.append(sanitizeColumn(col)).append(" VARCHAR(255),");
        }

        sql.setLength(sql.length() - 1); // remove last comma
        sql.append(")");

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(sql.toString());
        }
    }

    // ➕ INSERT ROW (SAFE PREPARED STATEMENT)
    public static void insertRow(String tableName, List<String> values)
            throws SQLException {

        if (values.isEmpty()) throw new SQLException("No values provided");

        // We need to get column count dynamically to match values
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" VALUES (NULL,");
        sql.append("?,".repeat(values.size()));
        sql.setLength(sql.length() - 1);
        sql.append(")");

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                ps.setString(i + 1, values.get(i));
            }
            ps.executeUpdate();
        }
    }

    // 📤 FETCH TABLE DATA (WITHOUT ID COLUMN)
    public static List<List<String>> fetchTable(String tableName)
            throws SQLException {

        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 2; i <= colCount; i++) { // skip ID column
                    row.add(rs.getString(i));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    // 📑 FETCH COLUMN NAMES (FOR SEARCH / DROPDOWN)
    public static List<String> fetchColumnNames(String tableName)
            throws SQLException {

        List<String> cols = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " LIMIT 1";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            for (int i = 2; i <= meta.getColumnCount(); i++) {
                cols.add(meta.getColumnName(i));
            }
        }
        return cols;
    }
}
