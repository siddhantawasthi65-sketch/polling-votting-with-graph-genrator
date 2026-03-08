import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

class Dashboard implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        File file = new File(
            "C:\\Users\\Nikhil\\Desktop\\VotingApp\\src\\login\\dashboard.html"
        );

        if (file.exists()) {
            byte[] response = Files.readAllBytes(file.toPath());

            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }

        } else {
            String error = "404 - Dashboard Not Found";
            byte[] err = error.getBytes();

            exchange.sendResponseHeaders(404, err.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(err);
            }
        }
    }
}
