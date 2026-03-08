
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class InsertHandler implements HttpHandler {

    public void handle(HttpExchange exchange, Statement VoteDOB) throws IOException {
    
            Map<String, String> p = parse(exchange);
            String table = p.get("table");
            String values = p.get("values");
    
            String[] vals = values.split(",");
            StringBuilder sql = new StringBuilder(
                    "INSERT INTO " + table + " VALUES (NULL,"
            );
    
            for (String v : vals) {
                sql.append("'").append(v.trim()).append("',");
            }
            sql.deleteCharAt(sql.length() - 1);
            sql.append(")");
    
            try (Connection con = VoteDOB.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate(sql.toString());
            send(exchange, "✅ Data inserted");

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

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

   
}
