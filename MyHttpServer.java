import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;

public class MyHttpServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", new RootHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/authenticate", new AuthHandler());
        server.createContext("/welcome", new WelcomeHandler());

        server.createContext("/createTable", new CreateTableHandler());
        server.createContext("/insertData", new InsertHandler());
        server.createContext("/fetchTable", new FetchHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("✅ Server running at http://localhost:8081/login");
    }

    // ================= ROOT =================
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            ex.getResponseHeaders().add("Location", "/login");
            ex.sendResponseHeaders(302, -1);
        }
    }

    // ================= LOGIN =================
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            File file = new File("C:\\Users\\Nikhil\\Desktop\\VotingApp\\src\\login\\hello.html");
            sendFileResponse(ex, file);
        }
    }

    // ================= AUTH =================
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(405, -1);
                return;
            }

            Map<String, String> params = parseForm(ex);
            String username = params.get("username");
            String password = params.get("password");

            if ("admin".equals(username) && "1234".equals(password)) {
                ex.getResponseHeaders().add("Location", "/welcome?user=" + username);
                ex.sendResponseHeaders(302, -1);
            } else {
                sendText(ex, "❌ Invalid login");
            }
        }
    }

    // ================= WELCOME =================
    static class WelcomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            try (ex) {
                String user = "User";
                String q = ex.getRequestURI().getQuery();
                if (q != null && q.startsWith("user="))
                    user = URLDecoder.decode(q.substring(5), "UTF-8");
                
                File file = new File("C:\\Users\\Nikhil\\Desktop\\VotingApp\\src\\login\\welcome.html");
                String html = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                html = html.replace("{{username}}", user);
                
                byte[] res = html.getBytes();
                ex.getResponseHeaders().add("Content-Type", "text/html");
                ex.sendResponseHeaders(200, res.length);
                ex.getResponseBody().write(res);
            }
        }
    }

    // ================= CREATE TABLE =================
    static class CreateTableHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            Map<String, String> p = parseForm(ex);

            try {
                VoteDOB.createTable(
                        p.get("table"),
                        Arrays.asList(p.get("columns").split(","))
                );
                sendText(ex, "✅ Table Created");
            } catch (IOException | SQLException e) {
                sendText(ex, "❌ " + e.getMessage());
            }
        }
    }

    // ================= INSERT =================
    static class InsertHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            Map<String, String> p = parseForm(ex);

            try {
                VoteDOB.insertRow(
                        p.get("table"),
                        Arrays.asList(p.get("values").split(","))
                );
                sendText(ex, "✅ Data Inserted");
            } catch (IOException | SQLException e) {
                sendText(ex, "❌ " + e.getMessage());
            }
        }
    }

    // ================= FETCH =================
    static class FetchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            String query = ex.getRequestURI().getQuery();

            if (query == null || !query.startsWith("table=")) {
                sendText(ex, "❌ Table name missing");
                return;
            }

            String table = query.split("=")[1];

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
                    ex.sendResponseHeaders(200, json.getBytes().length);
                    ex.getResponseBody().write(json.getBytes());
                }
            } catch (IOException | SQLException e) {
                sendText(ex, "❌ " + e.getMessage());
            }
        }
    }

    // ================= UTIL =================
    static Map<String, String> parseForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String p : body.split("&")) {
            String[] kv = p.split("=");
            if (kv.length == 2)
                map.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
        }
        return map;
    }

    static void sendText(HttpExchange ex, String msg) throws IOException {
        try (ex) {
            byte[] b = msg.getBytes();
            ex.sendResponseHeaders(200, b.length);
            ex.getResponseBody().write(b);
        }
    }

    static void sendFileResponse(HttpExchange ex, File file) throws IOException {
        try (ex) {
            byte[] data = Files.readAllBytes(file.toPath());
            ex.getResponseHeaders().add("Content-Type", "text/html");
            ex.sendResponseHeaders(200, data.length);
            ex.getResponseBody().write(data);
        }
    }

    // ================= JSON BUILDER =================
    static String buildJson(List<String> columns, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // columns
        sb.append("\"columns\":[");
        for (String c : columns) sb.append("\"").append(c).append("\",");
        if (!columns.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("],");

        // rows
        sb.append("\"rows\":[");
        for (List<String> row : rows) {
            sb.append("[");
            for (String cell : row) sb.append("\"").append(cell).append("\",");
            if (!row.isEmpty()) sb.setLength(sb.length() - 1); // remove trailing comma
            sb.append("],");
        }
        if (!rows.isEmpty()) sb.setLength(sb.length() - 1); // remove trailing comma
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }
}
