package servlet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

class WelcomeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Read ?user=username from URL
        String query = exchange.getRequestURI().getQuery();
        String username = "User"; // default

        if (query != null && query.startsWith("user=")) {
            username = URLDecoder.decode(query.substring(5), StandardCharsets.UTF_8);
        }

        // Load the HTML file
        File file = new File("C:\\Users\\Nikhil\\Desktop\\VotingApp\\src\\login\\welcome.html");
        String html = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

        // Replace all occurrences of {{username}}
        html = html.replace("{{username}}", username);

        // Send response
        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

}