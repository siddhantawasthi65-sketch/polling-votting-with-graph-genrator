import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        // Read the form data from request body
        String formData = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        String username = null;
        String password = null;

        // Parse form data (format: username=admin&password=1234)
        for (String pair : formData.split("&")) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                String key = java.net.URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8);

                if ("username".equals(key)) {
                    username = value;
                } else if ("password".equals(key)) {
                    password = value;
                }
            }
        }

        // Simple credential check
        if ("admin".equals(username) && "1234".equals(password)) {
            exchange.getResponseHeaders().add("Location", "/dashboard");
            exchange.sendResponseHeaders(302, -1);
        } else {
            exchange.getResponseHeaders().add("Location", "/?error=true");
            exchange.sendResponseHeaders(302, -1);
        }
    }
}
