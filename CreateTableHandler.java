import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class CreateTableHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = parse(exchange);
        String table = params.get("table");
        String columns = params.get("columns");

        StringBuilder sql = new StringBuilder(
                "CREATE TABLE IF NOT EXISTS " + table + " (id INT AUTO_INCREMENT PRIMARY KEY,"
        );

        for (String c : columns.split(",")) {
            sql.append(c.trim()).append(" VARCHAR(255),");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");

        try (Connection con = VoteDOB.getConnection();
             Statement st = con.createStatement()) {

            st.execute(sql.toString());
            send(exchange, "✅ Table created successfully");

        } catch (Exception e) {
            send(exchange, "❌ Error: " + e.getMessage());
        }
    }

    private Map<String, String> parse(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();

        for (String p : body.split("&")) {
            String[] kv = p.split("=");
            map.put(kv[0], URLDecoder.decode(kv[1], "UTF-8"));
        }
        return map;
    }

    private void send(HttpExchange ex, String msg) throws IOException {
        try (ex) {
            ex.sendResponseHeaders(200, msg.length());
            ex.getResponseBody().write(msg.getBytes());
        }
    }
}
