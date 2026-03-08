import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

class FetchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String query = ex.getRequestURI().getQuery();

        if (query == null || !query.startsWith("table=")) {
            sendText(ex, "❌ Table name missing");
            return;
        }

        String table = query.split("=")[1];

        // 🔐 Validate table name
        if (!table.matches("[a-zA-Z0-9_]+")) {
            sendText(ex, "❌ Invalid table name");
            return;
        }

        try {
            try (ex) {
                List<String> columns = VoteDOB.fetchColumnNames(table);
                List<List<String>> rows = VoteDOB.fetchTable(table);
                
                String json = buildJson(columns, rows);
                
                ex.getResponseHeaders().add("Content-Type", "application/json");
                byte[] data = json.getBytes(StandardCharsets.UTF_8);
                ex.sendResponseHeaders(200, data.length);
                ex.getResponseBody().write(data);
            }

        } catch (SQLException e) {
            sendText(ex, "❌ " + e.getMessage());
        }
    }

    // ✅ Send text response
    private void sendText(HttpExchange ex, String msg) throws IOException {
        try (ex) {
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, data.length);
            ex.getResponseBody().write(data);
        }
    }

    // ✅ Build JSON manually
    private String buildJson(List<String> columns, List<List<String>> rows) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // Columns
        sb.append("\"columns\":[");
        for (String c : columns) {
            sb.append("\"").append(c).append("\",");
        }
        if (!columns.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("],");

        // Rows
        sb.append("\"rows\":[");
        for (List<String> row : rows) {
            sb.append("[");
            for (String cell : row) {
                sb.append("\"").append(cell).append("\",");
            }
            sb.setLength(sb.length() - 1);
            sb.append("],");
        }
        if (!rows.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }
}
