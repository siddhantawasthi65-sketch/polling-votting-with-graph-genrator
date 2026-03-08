

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FetchHandler implements HttpHandler {

    public void handle(HttpExchange exchange, Statement VoteDOB) throws IOException {
    
            try (exchange) {
    
                String table = exchange.getRequestURI().getQuery().split("=")[1];
                List<List<String>> data = new ArrayList<>();
                try (Connection con = VoteDOB.getConnection();
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
                
                int cols = rs.getMetaData().getColumnCount();
                
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= cols; i++) {
                        row.add(rs.getString(i));
                    }
                    data.add(row);
                }

            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
                return;
            }
            String json = data.toString();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.length());
            exchange.getResponseBody().write(json.getBytes());

            }
}

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        throw new UnsupportedOperationException("Unimplemented method 'handle'");
    }

   
}